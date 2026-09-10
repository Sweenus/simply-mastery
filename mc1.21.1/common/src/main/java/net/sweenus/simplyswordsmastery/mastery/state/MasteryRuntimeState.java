package net.sweenus.simplyswordsmastery.mastery.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

import java.util.LinkedHashMap;
import java.util.Map;

public record MasteryRuntimeState(Map<String, Value> values) {
    public static final int MAX_VALUES = 64;
    public static final MasteryRuntimeState EMPTY = new MasteryRuntimeState(Map.of());
    public static final Codec<MasteryRuntimeState> CODEC = Codec.unboundedMap(Codec.STRING, Value.CODEC)
            .xmap(MasteryRuntimeState::new, MasteryRuntimeState::values);
    public static final PacketCodec<RegistryByteBuf, MasteryRuntimeState> PACKET_CODEC = PacketCodec.ofStatic(
            MasteryRuntimeState::encode, MasteryRuntimeState::decode);

    public MasteryRuntimeState {
        Map<String, Value> bounded = new LinkedHashMap<>();
        for (Map.Entry<String, Value> entry : values.entrySet()) {
            if (bounded.size() >= MAX_VALUES) break;
            if (entry.getKey() != null && !entry.getKey().isBlank() && entry.getKey().length() <= 96
                    && entry.getValue() != null) bounded.put(entry.getKey(), entry.getValue());
        }
        values = Map.copyOf(bounded);
    }

    public Value get(String key, long tick) {
        Value value = values.get(key);
        return value != null && value.expiresAt() > tick ? value : Value.EMPTY;
    }

    public MasteryRuntimeState put(String key, int amount, long expiresAt, long tick) {
        Map<String, Value> updated = new LinkedHashMap<>(values);
        updated.entrySet().removeIf(entry -> entry.getValue().expiresAt() <= tick);
        if (!updated.containsKey(key) && updated.size() >= MAX_VALUES) {
            updated.remove(updated.keySet().iterator().next());
        }
        if (amount == 0 || expiresAt <= tick) updated.remove(key);
        else updated.put(key, new Value(amount, expiresAt));
        return new MasteryRuntimeState(updated);
    }

    public MasteryRuntimeState clear(String key) {
        if (!values.containsKey(key)) return this;
        Map<String, Value> updated = new LinkedHashMap<>(values);
        updated.remove(key);
        return new MasteryRuntimeState(updated);
    }

    private static void encode(RegistryByteBuf buf, MasteryRuntimeState state) {
        buf.writeVarInt(state.values.size());
        state.values.forEach((key, value) -> {
            buf.writeString(key, 96);
            buf.writeVarInt(value.amount());
            buf.writeVarLong(value.expiresAt());
        });
    }

    private static MasteryRuntimeState decode(RegistryByteBuf buf) {
        int count = buf.readVarInt();
        if (count < 0 || count > MAX_VALUES) throw new IllegalArgumentException("Invalid mastery runtime count " + count);
        Map<String, Value> values = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            values.put(buf.readString(96), new Value(buf.readVarInt(), buf.readVarLong()));
        }
        return new MasteryRuntimeState(values);
    }

    public record Value(int amount, long expiresAt) {
        public static final Value EMPTY = new Value(0, 0L);
        public static final Codec<Value> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("amount").forGetter(Value::amount),
                Codec.LONG.fieldOf("expires_at").forGetter(Value::expiresAt)
        ).apply(instance, Value::new));
    }
}
