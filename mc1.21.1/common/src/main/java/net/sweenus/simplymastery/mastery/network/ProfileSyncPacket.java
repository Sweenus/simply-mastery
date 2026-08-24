package net.sweenus.simplymastery.mastery.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.network.RegistryByteBuf;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplymastery.mastery.definition.MasteryProfileRegistry;

import java.util.List;

public final class ProfileSyncPacket extends BaseS2CMessage {

    private final int protocolVersion;
    private final int epoch;
    private final String canonicalJson;

    public ProfileSyncPacket(MasteryProfileRegistry.Snapshot snapshot) {
        this(MasteryNetwork.PROTOCOL_VERSION, snapshot.epoch(), encode(snapshot.orderedProfiles()));
    }

    private ProfileSyncPacket(int protocolVersion, int epoch, String canonicalJson) {
        this.protocolVersion = protocolVersion;
        this.epoch = epoch;
        this.canonicalJson = canonicalJson;
    }

    public ProfileSyncPacket(RegistryByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt(), buf.readString(MasteryProfile.MAX_CANONICAL_JSON_BYTES));
    }

    @Override
    public MessageType getType() {
        return MasteryNetwork.PROFILE_SYNC;
    }

    @Override
    public void write(RegistryByteBuf buf) {
        buf.writeVarInt(protocolVersion);
        buf.writeVarInt(epoch);
        buf.writeString(canonicalJson, MasteryProfile.MAX_CANONICAL_JSON_BYTES);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> EnvExecutor.runInEnv(Env.CLIENT, () -> () -> apply(protocolVersion, epoch, canonicalJson)));
    }

    private static void apply(int protocolVersion, int epoch, String canonicalJson) {
        if (protocolVersion != MasteryNetwork.PROTOCOL_VERSION) return;
        try {
            JsonElement json = JsonParser.parseString(canonicalJson);
            MasteryProfile.CODEC.listOf().parse(JsonOps.INSTANCE, json).result()
                    .ifPresent(profiles -> MasteryProfileRegistry.installClient(epoch, profiles));
        } catch (RuntimeException ignored) {
        }
    }

    private static String encode(List<MasteryProfile> profiles) {
        return MasteryProfile.CODEC.listOf().encodeStart(JsonOps.INSTANCE, profiles).getOrThrow().toString();
    }
}
