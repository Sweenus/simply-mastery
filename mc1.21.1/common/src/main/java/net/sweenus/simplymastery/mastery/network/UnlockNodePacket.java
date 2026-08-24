package net.sweenus.simplymastery.mastery.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class UnlockNodePacket extends BaseC2SMessage {

    private final int protocolVersion;
    private final int screenSyncId;
    private final int definitionEpoch;
    private final long expectedMutationRevision;
    private final Identifier profileId;
    private final String nodeId;
    private final Operation operation;
    private final long clientActionId;

    public UnlockNodePacket(int screenSyncId, int definitionEpoch, long expectedMutationRevision,
                            Identifier profileId, String nodeId, long clientActionId) {
        this(MasteryNetwork.PROTOCOL_VERSION, screenSyncId, definitionEpoch, expectedMutationRevision,
                profileId, nodeId, Operation.UNLOCK, clientActionId);
    }

    public static UnlockNodePacket respec(int screenSyncId, int definitionEpoch, long expectedMutationRevision,
                                          Identifier profileId, long clientActionId) {
        return new UnlockNodePacket(MasteryNetwork.PROTOCOL_VERSION, screenSyncId, definitionEpoch,
                expectedMutationRevision, profileId, "", Operation.RESPEC, clientActionId);
    }

    private UnlockNodePacket(int protocolVersion, int screenSyncId, int definitionEpoch,
                             long expectedMutationRevision, Identifier profileId, String nodeId,
                             Operation operation, long clientActionId) {
        this.protocolVersion = protocolVersion;
        this.screenSyncId = screenSyncId;
        this.definitionEpoch = definitionEpoch;
        this.expectedMutationRevision = expectedMutationRevision;
        this.profileId = profileId;
        this.nodeId = nodeId;
        this.operation = operation;
        this.clientActionId = clientActionId;
    }

    public UnlockNodePacket(RegistryByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarLong(),
                buf.readIdentifier(), buf.readString(64), buf.readEnumConstant(Operation.class), buf.readVarLong());
    }

    @Override
    public MessageType getType() {
        return MasteryNetwork.UNLOCK_NODE;
    }

    @Override
    public void write(RegistryByteBuf buf) {
        buf.writeVarInt(protocolVersion);
        buf.writeVarInt(screenSyncId);
        buf.writeVarInt(definitionEpoch);
        buf.writeVarLong(expectedMutationRevision);
        buf.writeIdentifier(profileId);
        buf.writeString(nodeId, 64);
        buf.writeEnumConstant(operation);
        buf.writeVarLong(clientActionId);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (context.getPlayer() instanceof ServerPlayerEntity player) {
            context.queue(() -> UnlockService.handle(player, this));
        }
    }

    int protocolVersion() {
        return protocolVersion;
    }

    int screenSyncId() {
        return screenSyncId;
    }

    int definitionEpoch() {
        return definitionEpoch;
    }

    long expectedMutationRevision() {
        return expectedMutationRevision;
    }

    Identifier profileId() {
        return profileId;
    }

    String nodeId() {
        return nodeId;
    }

    Operation operation() {
        return operation;
    }

    long clientActionId() {
        return clientActionId;
    }

    enum Operation {
        UNLOCK,
        RESPEC
    }
}
