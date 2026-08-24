package net.sweenus.simplymastery.mastery.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sweenus.simplyswords.screen.RunicForgeScreenHandler;

public final class RequestProfileSyncPacket extends BaseC2SMessage {

    private final int protocolVersion;
    private final int screenSyncId;

    public RequestProfileSyncPacket(int screenSyncId) {
        this(MasteryNetwork.PROTOCOL_VERSION, screenSyncId);
    }

    private RequestProfileSyncPacket(int protocolVersion, int screenSyncId) {
        this.protocolVersion = protocolVersion;
        this.screenSyncId = screenSyncId;
    }

    public RequestProfileSyncPacket(RegistryByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt());
    }

    @Override
    public MessageType getType() {
        return MasteryNetwork.REQUEST_PROFILE_SYNC;
    }

    @Override
    public void write(RegistryByteBuf buf) {
        buf.writeVarInt(protocolVersion);
        buf.writeVarInt(screenSyncId);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (context.getPlayer() instanceof ServerPlayerEntity player) {
            context.queue(() -> {
                if (protocolVersion == MasteryNetwork.PROTOCOL_VERSION
                        && player.currentScreenHandler instanceof RunicForgeScreenHandler handler
                        && handler.syncId == screenSyncId && handler.canUse(player)) {
                    MasteryNetwork.sync(player);
                }
            });
        }
    }
}
