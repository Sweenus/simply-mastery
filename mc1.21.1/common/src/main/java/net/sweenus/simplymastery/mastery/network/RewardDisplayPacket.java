package net.sweenus.simplymastery.mastery.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.RegistryByteBuf;
import net.sweenus.simplymastery.client.mastery.MasteryRewardsScreen;

public final class RewardDisplayPacket extends BaseS2CMessage {
    private final NbtCompound display;
    public RewardDisplayPacket(NbtCompound display) { this.display = display.copy(); }
    public RewardDisplayPacket(RegistryByteBuf buf) { var value = buf.readNbt(NbtSizeTracker.of(65536)); display = value instanceof NbtCompound compound ? compound : null; }
    @Override public MessageType getType() { return MasteryNetwork.REWARD_DISPLAY; }
    @Override public void write(RegistryByteBuf buf) { buf.writeNbt(display); }
    @Override public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            var client = MinecraftClient.getInstance();
            if (display != null && client.currentScreen instanceof MasteryRewardsScreen screen) {
                screen.install(display);
            }
        }));
    }
}
