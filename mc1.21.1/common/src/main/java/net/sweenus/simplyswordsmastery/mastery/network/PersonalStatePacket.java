package net.sweenus.simplyswordsmastery.mastery.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.network.RegistryByteBuf;
import net.sweenus.simplyswordsmastery.client.mastery.PersonalMasteryView;
import net.sweenus.simplyswordsmastery.mastery.progression.ProgressionOwner;
import net.sweenus.simplyswordsmastery.mastery.state.MasteryState;

import java.util.UUID;

public final class PersonalStatePacket extends BaseS2CMessage {
    private final UUID subject;
    private final ProgressionOwner.Kind mode;
    private final int epoch;
    private final MasteryState state;

    public PersonalStatePacket(UUID subject, ProgressionOwner.Kind mode, int epoch, MasteryState state) {
        this.subject = subject;
        this.mode = mode;
        this.epoch = epoch;
        this.state = state;
    }

    public PersonalStatePacket(RegistryByteBuf buf) {
        this(buf.readUuid(), buf.readEnumConstant(ProgressionOwner.Kind.class), buf.readVarInt(),
                buf.readBoolean() ? MasteryState.PACKET_CODEC.decode(buf) : null);
    }

    @Override
    public MessageType getType() { return MasteryNetwork.PERSONAL_STATE; }

    @Override
    public void write(RegistryByteBuf buf) {
        buf.writeUuid(subject);
        buf.writeEnumConstant(mode);
        buf.writeVarInt(epoch);
        buf.writeBoolean(state != null);
        if (state != null) MasteryState.PACKET_CODEC.encode(buf, state);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> EnvExecutor.runInEnv(Env.CLIENT, () -> () ->
                PersonalMasteryView.install(subject, mode, epoch, state)));
    }
}
