package net.sweenus.simplyswordsmastery.mastery.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.network.RegistryByteBuf;
import net.sweenus.simplyswordsmastery.client.mastery.MasteryClientFeedback;

public final class UnlockResultPacket extends BaseS2CMessage {

    private final UnlockResult result;
    private final long clientActionId;
    private final long mutationRevision;

    public UnlockResultPacket(UnlockResult result, long clientActionId, long mutationRevision) {
        this.result = result;
        this.clientActionId = clientActionId;
        this.mutationRevision = mutationRevision;
    }

    public UnlockResultPacket(RegistryByteBuf buf) {
        this(buf.readEnumConstant(UnlockResult.class), buf.readVarLong(), buf.readVarLong());
    }

    @Override
    public MessageType getType() {
        return MasteryNetwork.UNLOCK_RESULT;
    }

    @Override
    public void write(RegistryByteBuf buf) {
        buf.writeEnumConstant(result);
        buf.writeVarLong(clientActionId);
        buf.writeVarLong(mutationRevision);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> EnvExecutor.runInEnv(Env.CLIENT,
                () -> () -> MasteryClientFeedback.accept(result, clientActionId, mutationRevision)));
    }
}
