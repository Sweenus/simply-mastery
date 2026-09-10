package net.sweenus.simplymastery.mastery.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.mastery.progression.ProgressionOwner;

import java.util.UUID;

public final class UnlockNodePacket extends BaseC2SMessage {

    private final UUID weaponId;
    private final ProgressionOwner.Kind ownership;
    private final int protocolVersion;
    private final int screenSyncId;
    private final int definitionEpoch;
    private final long expectedMutationRevision;
    private final Identifier profileId;
    private final String nodeId;
    private final Operation operation;
    private final long clientActionId;

    public UnlockNodePacket(int screenSyncId, int definitionEpoch, long expectedMutationRevision,
                            Identifier profileId, String nodeId, long clientActionId, UUID weaponId,
                            ProgressionOwner.Kind ownership) {
        this(MasteryNetwork.PROTOCOL_VERSION, screenSyncId, definitionEpoch, expectedMutationRevision,
                profileId, nodeId, Operation.UNLOCK, clientActionId, weaponId, ownership);
    }

    public static UnlockNodePacket respec(int screenSyncId, int definitionEpoch, long expectedMutationRevision,
                                          Identifier profileId, long clientActionId, UUID weaponId,
                                          ProgressionOwner.Kind ownership) {
        return new UnlockNodePacket(MasteryNetwork.PROTOCOL_VERSION, screenSyncId, definitionEpoch,
                expectedMutationRevision, profileId, "", Operation.RESPEC, clientActionId, weaponId, ownership);
    }

    private UnlockNodePacket(int protocolVersion, int screenSyncId, int definitionEpoch,
                             long expectedMutationRevision, Identifier profileId, String nodeId,
                             Operation operation, long clientActionId, UUID weaponId,
                             ProgressionOwner.Kind ownership) {
        this.weaponId = weaponId;
        this.ownership = ownership;
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
                buf.readIdentifier(), buf.readString(64), buf.readEnumConstant(Operation.class), buf.readVarLong(), buf.readUuid(),
                buf.readEnumConstant(ProgressionOwner.Kind.class));
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
        buf.writeUuid(weaponId);
        buf.writeEnumConstant(ownership);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (context.getPlayer() instanceof ServerPlayerEntity player) {
            context.queue(() -> UnlockService.handle(player, this));
        }
    }

    UUID weaponId() { return weaponId; }
    ProgressionOwner.Kind ownership() { return ownership; }

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
