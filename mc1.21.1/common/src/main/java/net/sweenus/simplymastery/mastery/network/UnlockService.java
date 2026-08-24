package net.sweenus.simplymastery.mastery.network;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sweenus.simplymastery.config.MasteryConfig;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplymastery.mastery.state.MasteryState;
import net.sweenus.simplymastery.mastery.state.MasteryStateAccess;
import net.sweenus.simplyswords.screen.RunicForgeScreenHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class UnlockService {

    private static final Map<UUID, Long> LAST_ACTIONS = new HashMap<>();

    private UnlockService() {
    }

    public static void handle(ServerPlayerEntity player, UnlockNodePacket request) {
        UnlockResult result = validateEnvelope(player, request);
        long revision = 0L;
        if (result == UnlockResult.SUCCESS) {
            RunicForgeScreenHandler handler = (RunicForgeScreenHandler) player.currentScreenHandler;
            ItemStack stack = handler.getForgeInventory().getStack(RunicForgeScreenHandler.WEAPON_SLOT);
            MasteryProfile profile = MasteryProfile.resolve(stack).orElseThrow();
            int initialPoints = Math.min(MasteryConfig.SERVER.verticalSliceStartingPoints,
                    MasteryConfig.SERVER.maximumEarnedPoints);
            MasteryState state = MasteryStateAccess.read(stack, profile, initialPoints);
            if (state.earnedPoints() > MasteryConfig.SERVER.maximumEarnedPoints) {
                state = new MasteryState(state.schemaVersion(), state.profileId(), state.profileVersion(),
                        state.masteryXp(), MasteryConfig.SERVER.maximumEarnedPoints,
                        state.unlockedNodeIds(), state.mutationRevision());
            }
            revision = state.mutationRevision();
            MasteryProfile.Node node = profile.node(request.nodeId()).orElse(null);
            if (node == null) {
                result = UnlockResult.UNKNOWN_NODE;
            } else {
                result = UnlockRules.validate(profile, state, node, request.expectedMutationRevision());
                if (result == UnlockResult.SUCCESS) {
                    MasteryState updated = state.unlock(node.id());
                    MasteryStateAccess.write(stack, updated);
                    handler.getForgeInventory().markDirty();
                    handler.sendContentUpdates();
                    revision = updated.mutationRevision();
                }
            }
        }
        new UnlockResultPacket(result, request.clientActionId(), revision).sendTo(player);
    }

    private static UnlockResult validateEnvelope(ServerPlayerEntity player, UnlockNodePacket request) {
        if (!MasteryConfig.SERVER.enabled) {
            return UnlockResult.DISABLED;
        }
        if (request.protocolVersion() != MasteryNetwork.PROTOCOL_VERSION
                || request.operation() != UnlockNodePacket.Operation.UNLOCK
                || request.profileId() == null
                || request.nodeId() == null
                || request.nodeId().isBlank()
                || request.nodeId().length() > 64
                || request.clientActionId() < 0L) {
            return UnlockResult.INVALID_REQUEST;
        }
        Long previousAction = LAST_ACTIONS.put(player.getUuid(), request.clientActionId());
        if (previousAction != null && previousAction == request.clientActionId()) {
            return UnlockResult.DUPLICATE_ACTION;
        }
        if (!(player.currentScreenHandler instanceof RunicForgeScreenHandler handler)
                || handler.syncId != request.screenSyncId()) {
            return UnlockResult.WRONG_SCREEN;
        }
        if (!handler.canUse(player)) {
            return UnlockResult.OUT_OF_RANGE;
        }
        ItemStack stack = handler.getForgeInventory().getStack(RunicForgeScreenHandler.WEAPON_SLOT);
        MasteryProfile profile = MasteryProfile.resolve(stack).orElse(null);
        if (profile == null || !profile.id().equals(request.profileId())) {
            return UnlockResult.UNSUPPORTED_WEAPON;
        }
        if (request.definitionEpoch() != MasteryProfile.DEFINITION_EPOCH) {
            return UnlockResult.STALE_DEFINITION;
        }
        return UnlockResult.SUCCESS;
    }
}
