package net.sweenus.simplyswordsmastery.mastery.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

import java.util.LinkedHashMap;
import java.util.Map;

public record MasteryCooldownState(Map<String, Long> deadlines) {

    public static final int MAX_COOLDOWNS = 128;
    public static final MasteryCooldownState EMPTY = new MasteryCooldownState(Map.of());
    public static final Codec<MasteryCooldownState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.LONG).fieldOf("deadlines").forGetter(MasteryCooldownState::deadlines)
    ).apply(instance, MasteryCooldownState::new));
    public static final PacketCodec<RegistryByteBuf, MasteryCooldownState> PACKET_CODEC = PacketCodec.ofStatic(
            MasteryCooldownState::encode, MasteryCooldownState::decode);

    public MasteryCooldownState {
        Map<String, Long> bounded = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : deadlines.entrySet()) {
            if (bounded.size() >= MAX_COOLDOWNS) break;
            if (entry.getKey() != null && !entry.getKey().isBlank() && entry.getKey().length() <= 96
                    && entry.getValue() != null && entry.getValue() > 0L) {
                bounded.put(entry.getKey(), entry.getValue());
            }
        }
        deadlines = Map.copyOf(bounded);
    }

    public boolean ready(String key, long tick) {
        return deadlines.getOrDefault(key, 0L) <= tick;
    }

    public MasteryCooldownState start(String key, long deadline) {
        Map<String, Long> updated = new LinkedHashMap<>(deadlines);
        updated.entrySet().removeIf(entry -> entry.getValue() <= deadline - 72000L);
        if (!updated.containsKey(key) && updated.size() >= MAX_COOLDOWNS) {
            updated.remove(updated.keySet().iterator().next());
        }
        updated.put(key, deadline);
        return new MasteryCooldownState(updated);
    }

    private static void encode(RegistryByteBuf buf, MasteryCooldownState state) {
        buf.writeVarInt(state.deadlines.size());
        state.deadlines.forEach((key, value) -> {
            buf.writeString(key, 96);
            buf.writeVarLong(value);
        });
    }

    private static MasteryCooldownState decode(RegistryByteBuf buf) {
        int count = buf.readVarInt();
        if (count < 0 || count > MAX_COOLDOWNS) throw new IllegalArgumentException("Invalid mastery cooldown count " + count);
        Map<String, Long> deadlines = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) deadlines.put(buf.readString(96), buf.readVarLong());
        return new MasteryCooldownState(deadlines);
    }
}
