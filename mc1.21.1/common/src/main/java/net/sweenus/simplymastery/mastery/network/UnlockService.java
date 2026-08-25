package net.sweenus.simplymastery.mastery.network;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.inventory.Inventory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sweenus.simplymastery.config.MasteryConfig;
import net.sweenus.simplymastery.mastery.RunicForgeMasteryContext;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplymastery.mastery.definition.MasteryProfileRegistry;
import net.sweenus.simplymastery.mastery.state.MasteryState;
import net.sweenus.simplymastery.mastery.state.MasteryStateAccess;
import net.sweenus.simplymastery.mastery.effect.SkillRuntime;
import net.sweenus.simplymastery.mastery.effect.StormMasteryRuntime;
import net.sweenus.simplyswords.screen.RunicForgeScreenHandler;
import dev.architectury.event.events.common.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public final class UnlockService {

    private static final Map<UUID, Long> LAST_ACTIONS = new HashMap<>();

    private UnlockService() {
    }

    public static void init() {
        PlayerEvent.PLAYER_QUIT.register(player -> LAST_ACTIONS.remove(player.getUuid()));
    }

    public static void handle(ServerPlayerEntity player, UnlockNodePacket request) {
        UnlockResult result = validateEnvelope(player, request);
        long revision = 0L;
        if (result == UnlockResult.SUCCESS) {
            RunicForgeScreenHandler handler = (RunicForgeScreenHandler) player.currentScreenHandler;
            ItemStack stack = RunicForgeMasteryContext.stateStack(handler);
            MasteryProfile profile = MasteryProfileRegistry.resolveServer(
                    RunicForgeMasteryContext.identityStack(handler)).orElseThrow();
            int initialPoints = Math.min(MasteryConfig.SERVER.verticalSliceStartingPoints,
                    MasteryConfig.SERVER.maximumEarnedPoints);
            MasteryState state = MasteryStateAccess.read(stack, profile, initialPoints);
            if (state.earnedPoints() > MasteryConfig.SERVER.maximumEarnedPoints) {
                state = new MasteryState(state.schemaVersion(), state.profileId(), state.profileVersion(),
                        state.masteryXp(), MasteryConfig.SERVER.maximumEarnedPoints,
                        state.unlockedNodeIds(), state.mutationRevision());
            }
            revision = state.mutationRevision();
            if (request.operation() == UnlockNodePacket.Operation.RESPEC) {
                result = state.mutationRevision() == request.expectedMutationRevision()
                        ? respec(player, handler, stack, profile, state) : UnlockResult.STALE_STATE;
                if (result == UnlockResult.SUCCESS) revision = state.mutationRevision() + 1L;
            } else {
                MasteryProfile.Node node = profile.node(request.nodeId()).orElse(null);
                if (node == null) {
                    result = UnlockResult.UNKNOWN_NODE;
                } else {
                    result = UnlockRules.validate(profile, state, node, request.expectedMutationRevision());
                    if (result == UnlockResult.SUCCESS) {
                        MasteryState updated = state.unlock(node.id());
                        MasteryStateAccess.write(stack, profile, updated);
                        handler.getForgeInventory().markDirty();
                        handler.sendContentUpdates();
                        revision = updated.mutationRevision();
                    }
                }
            }
        }
        new UnlockResultPacket(result, request.clientActionId(), revision).sendTo(player);
        if (result == UnlockResult.STALE_DEFINITION || result == UnlockResult.UNSUPPORTED_WEAPON) {
            MasteryNetwork.sync(player);
        }
    }

    private static UnlockResult validateEnvelope(ServerPlayerEntity player, UnlockNodePacket request) {
        if (!MasteryConfig.SERVER.enabled) {
            return UnlockResult.DISABLED;
        }
        if (request.protocolVersion() != MasteryNetwork.PROTOCOL_VERSION
                || request.operation() == null
                || request.profileId() == null
                || request.nodeId() == null
                || (request.operation() == UnlockNodePacket.Operation.UNLOCK && request.nodeId().isBlank())
                || (request.operation() == UnlockNodePacket.Operation.RESPEC && !request.nodeId().isEmpty())
                || request.nodeId().length() > 64
                || request.clientActionId() < 0L) {
            return UnlockResult.INVALID_REQUEST;
        }
        Long previousAction = LAST_ACTIONS.get(player.getUuid());
        if (previousAction != null && request.clientActionId() <= previousAction) {
            return UnlockResult.DUPLICATE_ACTION;
        }
        LAST_ACTIONS.put(player.getUuid(), request.clientActionId());
        if (!(player.currentScreenHandler instanceof RunicForgeScreenHandler handler)
                || handler.syncId != request.screenSyncId()) {
            return UnlockResult.WRONG_SCREEN;
        }
        if (!handler.canUse(player)) {
            return UnlockResult.OUT_OF_RANGE;
        }
        MasteryProfile profile = MasteryProfileRegistry.resolveServer(
                RunicForgeMasteryContext.identityStack(handler)).orElse(null);
        if (profile == null || !profile.id().equals(request.profileId())) {
            return UnlockResult.UNSUPPORTED_WEAPON;
        }
        if (request.definitionEpoch() != MasteryProfileRegistry.server().epoch()) {
            return UnlockResult.STALE_DEFINITION;
        }
        return UnlockResult.SUCCESS;
    }

    private static UnlockResult respec(ServerPlayerEntity player, RunicForgeScreenHandler handler,
                                       ItemStack stack, MasteryProfile profile, MasteryState state) {
        if (!MasteryConfig.SERVER.respecEnabled) return UnlockResult.RESPEC_DISABLED;
        if (state.unlockedNodeIds().isEmpty()) return UnlockResult.NOTHING_TO_RESPEC;
        Predicate<ItemStack> payment = paymentPredicate();
        if (payment == null) return UnlockResult.INVALID_PAYMENT_ITEM;
        boolean free = player.isCreative() && MasteryConfig.SERVER.creativeRespecIsFree;
        int cost = MasteryConfig.SERVER.respecCostCount;
        if (!free && !consumeIfPresent(player.getInventory(), payment, cost)) return UnlockResult.PAYMENT_MISSING;
        MasteryStateAccess.write(stack, profile, state.respec());
        MasteryStateAccess.writeCooldowns(stack,
                net.sweenus.simplymastery.mastery.state.MasteryCooldownState.EMPTY);
        MasteryStateAccess.writeRuntime(stack,
                net.sweenus.simplymastery.mastery.state.MasteryRuntimeState.EMPTY);
        SkillRuntime.clear(player);
        StormMasteryRuntime.clear(player.getUuid());
        handler.getForgeInventory().markDirty();
        player.getInventory().markDirty();
        handler.sendContentUpdates();
        return UnlockResult.SUCCESS;
    }

    private static Predicate<ItemStack> paymentPredicate() {
        if (MasteryConfig.SERVER.respecUsesTag) {
            TagKey<Item> tag = TagKey.of(RegistryKeys.ITEM, MasteryConfig.SERVER.respecCostTag.get());
            return stack -> stack.isIn(tag);
        }
        Item item = Registries.ITEM.getOrEmpty(MasteryConfig.SERVER.respecCostItem.get()).orElse(null);
        return item == null ? null : stack -> stack.isOf(item);
    }

    static boolean consumeIfPresent(Inventory inventory, Predicate<ItemStack> payment, int amount) {
        int[] available = new int[inventory.size()];
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (payment.test(stack)) available[slot] = stack.getCount();
        }
        int[] removals = paymentPlan(available, amount);
        if (removals == null) return false;
        for (int slot = 0; slot < removals.length; slot++) {
            if (removals[slot] > 0) inventory.getStack(slot).decrement(removals[slot]);
        }
        return true;
    }

    static int[] paymentPlan(int[] available, int amount) {
        long total = 0L;
        for (int count : available) total += Math.max(0, count);
        if (total < amount) return null;
        int[] removals = new int[available.length];
        int remaining = amount;
        for (int slot = 0; slot < available.length && remaining > 0; slot++) {
            removals[slot] = Math.min(remaining, Math.max(0, available[slot]));
            remaining -= removals[slot];
        }
        return removals;
    }
}
