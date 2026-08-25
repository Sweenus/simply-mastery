package net.sweenus.simplymastery.mastery.definition;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MasteryProfileCodecTest {

    @Test
    void bundledProfileRoundTripsAndValidates() {
        MasteryProfile profile = ProfileTestFixtures.stormsEdge();
        MasteryProfileValidator.validate(profile, 21);
        JsonElement encoded = MasteryProfile.CODEC.encodeStart(JsonOps.INSTANCE, profile).getOrThrow();
        MasteryProfile decoded = MasteryProfile.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(profile.id(), decoded.id());
        assertEquals(profile.nodes(), decoded.nodes());
        assertEquals(3, decoded.branches().size());
        assertEquals(6, decoded.nodes().stream().filter(MasteryProfile.Node::capstone).count());
    }

    @Test
    void malformedProfileFailsCodec() {
        JsonElement malformed = com.google.gson.JsonParser.parseString("{\"schema\":1,\"id\":\"simplymastery:bad\"}");
        assertTrue(MasteryProfile.CODEC.parse(JsonOps.INSTANCE, malformed).error().isPresent());
    }

    @Test
    void stageSelectorAndProgressionGroupRoundTrip() {
        MasteryProfile source = ProfileTestFixtures.stormsEdge();
        MasteryProfile profile = new MasteryProfile(source.schema(), source.id(), source.version(),
                List.of(new MasteryProfile.Selector(Optional.empty(),
                        Optional.of(net.minecraft.util.Identifier.of("simplyswords", "awakened_lichblade")),
                        Optional.empty(), 50)),
                Optional.of(net.minecraft.util.Identifier.of("simplymastery", "lichblade")),
                source.branches(), source.nodes(), source.migrations());
        JsonElement encoded = MasteryProfile.CODEC.encodeStart(JsonOps.INSTANCE, profile).getOrThrow();
        MasteryProfile decoded = MasteryProfile.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(profile.progressionGroup(), decoded.progressionGroup());
        assertEquals(profile.selectors(), decoded.selectors());
    }
}
