package net.sweenus.simplymastery.mastery.state;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MasteryStateCodecTest {

    @Test
    void codecRoundTripPreservesState() {
        MasteryState original = new MasteryState(1, Identifier.of("simplymastery", "storms_edge"), 1, 320,
                6, List.of("guard_root", "charged_pursuit", "unknown_from_newer_version"), 7L);
        JsonElement encoded = MasteryState.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        MasteryState decoded = MasteryState.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(original, decoded);
    }

    @Test
    void packetCodecRoundTripPreservesState() {
        MasteryState original = new MasteryState(1, Identifier.of("simplymastery", "storms_edge"), 1, 45,
                6, List.of("guard_root", "charged_pursuit"), 2L);
        RegistryByteBuf buffer = new RegistryByteBuf(Unpooled.buffer(), DynamicRegistryManager.EMPTY);
        try {
            MasteryState.PACKET_CODEC.encode(buffer, original);
            assertEquals(original, MasteryState.PACKET_CODEC.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void constructorBoundsAndDeduplicatesState() {
        MasteryState state = new MasteryState(-1, Identifier.of("simplymastery", "storms_edge"),
                -2, -30, -4, List.of("guard_root", "guard_root", "", "charged_pursuit"), -8L);
        assertEquals(1, state.schemaVersion());
        assertEquals(1, state.profileVersion());
        assertEquals(0, state.masteryXp());
        assertEquals(0, state.earnedPoints());
        assertEquals(0L, state.mutationRevision());
        assertEquals(List.of("guard_root", "charged_pursuit"), state.unlockedNodeIds());
    }

    @Test
    void malformedStateIsRejected() {
        JsonElement malformed = com.google.gson.JsonParser.parseString("{\"schema_version\":1}");
        assertTrue(MasteryState.CODEC.parse(JsonOps.INSTANCE, malformed).error().isPresent());
    }

    @Test
    void grantPointsClampsAtMaximumAndZeroesBankedXp() {
        MasteryState state = new MasteryState(1, Identifier.of("simplymastery", "storms_edge"), 1, 320,
                6, List.of("guard_root"), 7L);

        MasteryState granted = state.grantPoints(3, 21);
        assertEquals(9, granted.earnedPoints());
        assertEquals(320, granted.masteryXp());
        assertEquals(8L, granted.mutationRevision());

        MasteryState capped = state.grantPoints(500, 21);
        assertEquals(21, capped.earnedPoints());
        assertEquals(0, capped.masteryXp());
    }

    @Test
    void withProgressReturnsSameInstanceWhenNothingMoves() {
        MasteryState state = new MasteryState(1, Identifier.of("simplymastery", "storms_edge"), 1, 320,
                6, List.of("guard_root"), 7L);

        assertSame(state, state.withProgress(320, 6, 21));
        MasteryState moved = state.withProgress(0, 0, 21);
        assertEquals(0, moved.masteryXp());
        assertEquals(0, moved.earnedPoints());
        assertEquals(List.of("guard_root"), moved.unlockedNodeIds());
        assertEquals(8L, moved.mutationRevision());
    }
}
