package net.sweenus.simplymastery.mastery.state;

import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MasteryRuntimeStateTest {
    @Test
    void valuesExpireAdvanceAndClearWithoutAffectingOtherKeys() {
        MasteryRuntimeState state = MasteryRuntimeState.EMPTY.put("voltage", 2, 80, 20)
                .put("feedback", 1, 100, 20);

        assertEquals(2, state.get("voltage", 79).amount());
        assertEquals(0, state.get("voltage", 80).amount());
        assertEquals(1, state.clear("voltage").get("feedback", 50).amount());
    }

    @Test
    void constructorBoundsRuntimeEntries() {
        java.util.Map<String, MasteryRuntimeState.Value> values = new java.util.LinkedHashMap<>();
        for (int i = 0; i < MasteryRuntimeState.MAX_VALUES + 10; i++) {
            values.put("key_" + i, new MasteryRuntimeState.Value(i, 100));
        }
        assertEquals(MasteryRuntimeState.MAX_VALUES, new MasteryRuntimeState(values).values().size());
    }

    @Test
    void codecRoundTripsAmountsAndDeadlines() {
        MasteryRuntimeState state = MasteryRuntimeState.EMPTY.put("voltage", 4, 120, 20);
        var encoded = MasteryRuntimeState.CODEC.encodeStart(JsonOps.INSTANCE, state).getOrThrow();
        MasteryRuntimeState decoded = MasteryRuntimeState.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

        assertEquals(state, decoded);
    }
}
