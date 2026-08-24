package net.sweenus.simplymastery.mastery.definition;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

final class ProfileTestFixtures {

    private ProfileTestFixtures() {
    }

    static JsonElement stormsEdgeJson() {
        return MasteryProfile.CODEC.encodeStart(JsonOps.INSTANCE, stormsEdge()).getOrThrow();
    }

    static MasteryProfile stormsEdge() {
        return BuiltInFamilyProfiles.profile("storms_edge");
    }
}
