package net.sweenus.simplyswordsmastery.mastery.effect;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.EventResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.sweenus.simplyswordsmastery.config.MasteryConfig;
import net.sweenus.simplyswordsmastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswordsmastery.mastery.definition.MasteryProfileRegistry;
import net.sweenus.simplyswordsmastery.mastery.state.MasteryState;
import net.sweenus.simplyswordsmastery.mastery.state.MasteryStateAccess;
import net.sweenus.simplyswords.api.SimplySwordsAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SkillRuntime {

    private static final ComboTracker COMBOS = new ComboTracker();
    private static final Map<UUID, HeldSnapshot> HELD = new HashMap<>();
    private static final Map<AttackKey, Long> ATTACK_DEDUPLICATION = new HashMap<>();

    private SkillRuntime() {
    }

    public static void init() {
        TickEvent.PLAYER_POST.register(player -> {
            if (player instanceof ServerPlayerEntity serverPlayer) tick(serverPlayer);
        });
        PlayerEvent.PLAYER_QUIT.register(SkillRuntime::clear);
        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayerEntity player) clear(player);
            return EventResult.pass();
        });
        PlayerEvent.PLAYER_RESPAWN.register((player, conqueredEnd, reason) -> clear(player));
        PlayerEvent.CHANGE_DIMENSION.register((player, oldLevel, newLevel) -> clear(player));
    }

    public static void onDamageApplied(LivingEntity target, DamageSource source, float amount) {
        if (!MasteryConfig.SERVER.enabled || SkillOriginGuard.active()) return;
        long targetTick = target instanceof ServerPlayerEntity victim ? persistentTick(victim) : 0L;
        if (target instanceof ServerPlayerEntity victim) {
            dispatchDamageReceived(victim, source,
                    source.getAttacker() instanceof LivingEntity attacker ? attacker : null, amount, targetTick);
        }
        if (!source.isOf(DamageTypes.PLAYER_ATTACK)
                || !(source.getAttacker() instanceof ServerPlayerEntity player)) return;
        if (SimplySwordsAPI.getDelegatedWeaponHitContext() != null) return;
        Resolved resolved = resolve(player, source.getWeaponStack());
        if (resolved == null) return;
        long tick = persistentTick(player);
        AttackKey key = new AttackKey(player.getUuid(), target.getUuid(), resolved.hand, resolved.profile.id());
        Long previous = ATTACK_DEDUPLICATION.put(key, tick);
        if (previous != null && previous == tick) return;
        SkillEffectContext.Attack context = new SkillEffectContext.Attack(player, target, resolved.stack,
                resolved.hand, resolved.profile, resolved.state, tick,
                new SkillEffectAccess(player, resolved.stack, resolved.profile.id().toString()));
        dispatchAttack(context);
    }

    public static void onKill(ServerPlayerEntity player, LivingEntity target, ItemStack sourceStack) {
        Resolved resolved = resolve(player, sourceStack);
        if (resolved == null) return;
        long tick = persistentTick(player);
        SkillEffectContext.Kill context = new SkillEffectContext.Kill(player, target, resolved.stack,
                resolved.hand, resolved.profile, resolved.state, tick,
                new SkillEffectAccess(player, resolved.stack, resolved.profile.id().toString()));
        for (MasteryProfile.Node node : resolved.profile.nodes()) {
            if (!resolved.state.owns(node.id())) continue;
            SkillEffectType type = enabledType(node.effect().type());
            if (type != null) type.onKill(context, node);
        }
    }

    static boolean advanceCombo(ServerPlayerEntity player, SkillEffectAccess.HandKey hand, String key,
                                long tick, int windowTicks, int hits) {
        return COMBOS.advance(player.getUuid(), hand, key, tick, windowTicks, hits);
    }

    public static void clear(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        COMBOS.clear(id);
        ATTACK_DEDUPLICATION.keySet().removeIf(key -> key.player.equals(id));
        HELD.remove(id);
        StormMasteryRuntime.clear(id);
        PersonalRuntime.clear(player.getServer(), id);
    }

    private static void tick(ServerPlayerEntity player) {
        if (!MasteryConfig.SERVER.enabled) {
            clear(player);
            return;
        }
        ItemStack main = player.getMainHandStack();
        ItemStack off = player.getOffHandStack();
        HeldSnapshot now = new HeldSnapshot(main, off);
        HeldSnapshot previous = HELD.put(player.getUuid(), now);
        if (previous != null) {
            if (previous.main != now.main) clearHand(player, SkillEffectAccess.HandKey.MAIN_HAND);
            if (previous.off != now.off) clearHand(player, SkillEffectAccess.HandKey.OFF_HAND);
        }
        dispatchHeld(player, main, Hand.MAIN_HAND);
        if (off != main) dispatchHeld(player, off, Hand.OFF_HAND);
        long tick = persistentTick(player);
        ATTACK_DEDUPLICATION.entrySet().removeIf(entry -> entry.getValue() < tick - 2L);
    }

    private static void dispatchHeld(ServerPlayerEntity player, ItemStack stack, Hand hand) {
        Resolved resolved = resolveExact(player, stack, hand);
        if (resolved == null) return;
        SkillEffectAccess access = new SkillEffectAccess(player, stack, resolved.profile.id().toString());
        SkillEffectContext.HeldTick context = new SkillEffectContext.HeldTick(player, stack, hand,
                resolved.profile, resolved.state, persistentTick(player), access);
        for (MasteryProfile.Node node : resolved.profile.nodes()) {
            if (!resolved.state.owns(node.id())) continue;
            SkillEffectType type = enabledType(node.effect().type());
            if (type != null && type.needsHeldTick()) type.onHeldTick(context, node);
        }
    }

    private static void dispatchAttack(SkillEffectContext.Attack context) {
        for (MasteryProfile.Node node : context.profile().nodes()) {
            if (!context.state().owns(node.id())) continue;
            SkillEffectType type = enabledType(node.effect().type());
            if (type != null) type.onAttack(context, node);
        }
    }

    private static void dispatchDamageReceived(ServerPlayerEntity victim, DamageSource source,
                                               LivingEntity attacker, float amount, long tick) {
        dispatchDamageReceivedHand(victim, source, attacker, amount, tick,
                victim.getMainHandStack(), Hand.MAIN_HAND);
        if (victim.getOffHandStack() != victim.getMainHandStack()) {
            dispatchDamageReceivedHand(victim, source, attacker, amount, tick,
                    victim.getOffHandStack(), Hand.OFF_HAND);
        }
    }

    private static void dispatchDamageReceivedHand(ServerPlayerEntity victim, DamageSource source,
                                                   LivingEntity attacker, float amount,
                                                   long tick, ItemStack stack, Hand hand) {
        Resolved resolved = resolveExact(victim, stack, hand);
        if (resolved == null) return;
        SkillEffectContext.DamageReceived context = new SkillEffectContext.DamageReceived(victim, attacker, amount, source,
                stack, hand, resolved.profile, resolved.state, tick,
                new SkillEffectAccess(victim, stack, resolved.profile.id().toString()));
        for (MasteryProfile.Node node : resolved.profile.nodes()) {
            if (!resolved.state.owns(node.id())) continue;
            SkillEffectType type = enabledType(node.effect().type());
            if (type != null) type.onDamageReceived(context, node);
        }
    }

    private static Resolved resolve(ServerPlayerEntity player, ItemStack sourceStack) {
        if (sourceStack != null && !sourceStack.isEmpty()) {
            if (sourceStack == player.getOffHandStack()) return resolveExact(player, sourceStack, Hand.OFF_HAND);
            if (sourceStack == player.getMainHandStack()) return resolveExact(player, sourceStack, Hand.MAIN_HAND);
            if (ItemStack.areItemsAndComponentsEqual(sourceStack, player.getOffHandStack())
                    && !ItemStack.areItemsAndComponentsEqual(sourceStack, player.getMainHandStack())) {
                return resolveExact(player, player.getOffHandStack(), Hand.OFF_HAND);
            }
            if (ItemStack.areItemsAndComponentsEqual(sourceStack, player.getMainHandStack())) {
                return resolveExact(player, player.getMainHandStack(), Hand.MAIN_HAND);
            }
        }
        return resolveExact(player, player.getMainHandStack(), Hand.MAIN_HAND);
    }

    private static Resolved resolveExact(ServerPlayerEntity player, ItemStack stack, Hand hand) {
        if (stack == null || stack.isEmpty()) return null;
        MasteryProfile profile = MasteryProfileRegistry.resolveServer(stack).orElse(null);
        if (profile == null || MasteryConfig.SERVER.disabledProfiles.contains(profile.id())) return null;
        MasteryState state = MasteryStateAccess.read(player, stack, profile,
                Math.min(MasteryConfig.SERVER.verticalSliceStartingPoints, MasteryProfileRegistry.server().policy(profile.progressionGroupId()).pointCap()));
        return new Resolved(stack, hand, profile, state);
    }

    private static SkillEffectType enabledType(net.minecraft.util.Identifier id) {
        return MasteryConfig.SERVER.disabledEffects.contains(id) ? null : SkillEffectRegistry.get(id);
    }

    private static long persistentTick(ServerPlayerEntity player) {
        return player.getServerWorld().getServer().getOverworld().getTime();
    }

    private static void clearHand(ServerPlayerEntity player, SkillEffectAccess.HandKey hand) {
        UUID id = player.getUuid();
        COMBOS.clear(id, hand);
    }

    private record Resolved(ItemStack stack, Hand hand, MasteryProfile profile, MasteryState state) {
    }

    private record HeldSnapshot(ItemStack main, ItemStack off) {
    }

    private record AttackKey(UUID player, UUID target, Hand hand, net.minecraft.util.Identifier profile) {
    }
}
