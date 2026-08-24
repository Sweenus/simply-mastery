package net.sweenus.simplymastery.mastery.definition;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

final class ProfileTestFixtures {

    private ProfileTestFixtures() {
    }

    static JsonElement stormsEdgeJson() {
        return JsonParser.parseReader(new InputStreamReader(ProfileTestFixtures.class.getResourceAsStream(
                "/data/simplymastery/simplymastery/weapon_skills/storms_edge.json"), StandardCharsets.UTF_8));
    }

    static MasteryProfile stormsEdge() {
        return MasteryProfile.CODEC.parse(JsonOps.INSTANCE, stormsEdgeJson()).getOrThrow();
    }
}
