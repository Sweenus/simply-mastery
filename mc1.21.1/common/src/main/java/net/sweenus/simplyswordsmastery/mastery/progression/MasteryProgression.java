package net.sweenus.simplyswordsmastery.mastery.progression;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sweenus.simplyswordsmastery.config.MasteryConfig;
import net.sweenus.simplyswordsmastery.config.MasteryServerConfig;
import net.sweenus.simplyswordsmastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswordsmastery.mastery.definition.MasteryProfileRegistry;
import net.sweenus.simplyswordsmastery.mastery.effect.SkillOriginGuard;
import net.sweenus.simplyswordsmastery.mastery.effect.SkillRuntime;
import net.sweenus.simplyswordsmastery.mastery.state.MasteryComponents;
import net.sweenus.simplyswords.api.SimplySwordsAPI;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MasteryProgression {

    private static final int MAX_TRACKED_TARGET_TYPES = 2048;
    private static final Map<FarmKey, ArrayDeque<Long>> RECENT_KILLS = new HashMap<>();

    private MasteryProgression() {
    }

    public static void init() {
        EntityEvent.LIVING_DEATH.register(MasteryProgression::onDeath);
        PlayerEvent.PLAYER_QUIT.register(player -> RECENT_KILLS.keySet()
                .removeIf(key -> key.player.equals(player.getUuid())));
    }

    public static int calculateXp(float maxHealth, boolean hostile, boolean boss, int repeatedKills,
                                  MasteryServerConfig config) {
        long xp = config.killBaseXp + Math.max(0, (int) Math.ceil(maxHealth / 20.0F))
                * (long) config.maxHealthXpPerTwenty;
        int categoryPercent = boss ? config.bossPercent : hostile ? config.hostilePercent : 100;
        xp = xp * categoryPercent / 100L;
        if (repeatedKills >= config.antiFarmFullValueKills) xp = xp * config.antiFarmRepeatedPercent / 100L;
        return (int) Math.max(0L, Math.min(config.maximumKillXp, xp));
    }

    public static boolean eligibleClassification(boolean friendly, boolean ownedTame, boolean playerTarget,
                                                 boolean invalidPvpTarget, boolean hostileOrBoss,
                                                 MasteryServerConfig config) {
        if (friendly || ownedTame) return false;
        if (playerTarget) return config.allowPvpXp && !invalidPvpTarget;
        return config.allowPassiveMobXp || hostileOrBoss;
    }

    private static EventResult onDeath(LivingEntity target, DamageSource source) {
        if (!MasteryConfig.SERVER.enabled || !(source.getAttacker() instanceof ServerPlayerEntity player)) {
            return EventResult.pass();
        }
        ItemStack stack = exactSourceStack(player, source);
        if (stack == null || stack.isEmpty()) return EventResult.pass();
        if (!stack.contains(MasteryComponents.WEAPON_ID.get())
                && !MasteryRewardService.accessible(player, stack)) return EventResult.pass();
        MasteryProfileRegistry.ProgressionResolution resolution =
                MasteryProfileRegistry.resolveProgressionServer(stack).orElse(null);
        if (resolution == null || (!resolution.profileIds().isEmpty() && resolution.profileIds().stream()
                .allMatch(MasteryConfig.SERVER.disabledProfiles::contains))) {
            return EventResult.pass();
        }
        MasteryRewardService.synchronize(player.getServer(), stack);
        MasteryProfile profile = resolution.visibleProfile().orElse(null);
        if (profile != null && !MasteryConfig.SERVER.disabledProfiles.contains(profile.id())
                && !SkillOriginGuard.active()) SkillRuntime.onKill(player, target, stack);
        return EventResult.pass();
    }

    private static boolean eligible(ServerPlayerEntity player, LivingEntity target) {
        if (MasteryConfig.SERVER.excludedEntityIds.contains(EntityType.getId(target.getType()))) return false;
        boolean playerTarget = target instanceof ServerPlayerEntity;
        boolean invalidPvp = playerTarget && (((ServerPlayerEntity) target).isCreative()
                || ((ServerPlayerEntity) target).isSpectator());
        boolean ownedTame = target instanceof TameableEntity tameable
                && (tameable.isOwner(player) || player.getUuid().equals(tameable.getOwnerUuid()));
        var definitions = MasteryProfileRegistry.server().rewards();
        boolean boss = definitions == null ? target instanceof WitherEntity || target instanceof EnderDragonEntity
                : definitions.entity(EntityType.getId(target.getType())).flatMap(rule -> rule.value().boss())
                .orElse(target instanceof WitherEntity || target instanceof EnderDragonEntity);
        boolean hostileOrBoss = target instanceof HostileEntity || boss;
        return eligibleClassification(target == player || target.isTeammate(player), ownedTame,
                playerTarget, invalidPvp, hostileOrBoss, MasteryConfig.SERVER);
    }

    private static ItemStack exactSourceStack(ServerPlayerEntity player, DamageSource source) {
        SkillOriginGuard.Origin origin = SkillOriginGuard.current();
        if (origin != null && origin.player() == player) return origin.stack();
        if (!source.isOf(DamageTypes.PLAYER_ATTACK)
                || SimplySwordsAPI.getDelegatedWeaponHitContext() != null) return null;
        ItemStack sourceStack = source.getWeaponStack();
        if (sourceStack == null || sourceStack.isEmpty()) return null;
        if (sourceStack == player.getMainHandStack()) return player.getMainHandStack();
        if (sourceStack == player.getOffHandStack()) return player.getOffHandStack();
        boolean main = ItemStack.areItemsAndComponentsEqual(sourceStack, player.getMainHandStack());
        boolean off = ItemStack.areItemsAndComponentsEqual(sourceStack, player.getOffHandStack());
        if (main == off) return null;
        return main ? player.getMainHandStack() : player.getOffHandStack();
    }

    private static int recordKill(ServerPlayerEntity player, LivingEntity target, long tick) {
        FarmKey key = new FarmKey(player.getUuid(), EntityType.getId(target.getType()).toString());
        ArrayDeque<Long> kills = RECENT_KILLS.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        int repeats = repeatedKills(kills, tick, MasteryConfig.SERVER.antiFarmWindowTicks);
        if (RECENT_KILLS.size() > MAX_TRACKED_TARGET_TYPES) RECENT_KILLS.remove(RECENT_KILLS.keySet().iterator().next());
        return repeats;
    }

    static int repeatedKills(ArrayDeque<Long> kills, long tick, int windowTicks) {
        long cutoff = tick - windowTicks;
        while (!kills.isEmpty() && kills.peekFirst() < cutoff) kills.removeFirst();
        int repeats = kills.size();
        kills.addLast(tick);
        return repeats;
    }

    private record FarmKey(UUID player, String targetType) {
    }
}
