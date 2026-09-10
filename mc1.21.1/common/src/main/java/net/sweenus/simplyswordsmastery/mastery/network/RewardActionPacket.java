package net.sweenus.simplyswordsmastery.mastery.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sweenus.simplyswordsmastery.mastery.reward.ForgeRewards;

public final class RewardActionPacket extends BaseC2SMessage {
    private final NbtCompound request;
    public RewardActionPacket(NbtCompound request) { this.request = request.copy(); }
    public RewardActionPacket(RegistryByteBuf buf) { var value = buf.readNbt(NbtSizeTracker.of(65536)); request = value instanceof NbtCompound compound ? compound : null; }
    @Override public MessageType getType() { return MasteryNetwork.REWARD_ACTION; }
    @Override public void write(RegistryByteBuf buf) { buf.writeNbt(request); }
    @Override public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (request != null && context.getPlayer() instanceof ServerPlayerEntity player) {
                ForgeRewards.handle(player, request);
            }
        });
    }
}
