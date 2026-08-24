package net.sweenus.simplymastery.mastery.definition;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.client.mastery.ui.MasteryUiPolicy;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MasteryUiPolicyTest {

    @Test
    void capstonesAlwaysRequireConfirmation() {
        MasteryProfile profile = ProfileTestFixtures.stormsEdge();
        assertFalse(MasteryUiPolicy.requiresConfirmation(profile.nodes().get(0), false));
        assertTrue(MasteryUiPolicy.requiresConfirmation(profile.nodes().get(0), true));
        assertTrue(MasteryUiPolicy.requiresConfirmation(profile.nodes().get(7), false));
    }

    @Test
    void authoredIconsResolveToTexturePathsAndMissingIconsRemainEmpty() {
        MasteryProfile source = ProfileTestFixtures.stormsEdge();
        MasteryProfile.Node plain = source.nodes().get(0);
        MasteryProfile.Node missing = new MasteryProfile.Node(plain.id(), plain.branch(), plain.x(), plain.y(),
                plain.cost(), plain.capstone(), plain.choiceGroup(), plain.requires(), plain.effect(), plain.nameKey(),
                plain.descriptionKey(), Optional.empty());
        assertTrue(MasteryUiPolicy.iconTexture(missing).isEmpty());
        MasteryProfile.Node authored = new MasteryProfile.Node(plain.id(), plain.branch(), plain.x(), plain.y(),
                plain.cost(), plain.capstone(), plain.choiceGroup(), plain.requires(), plain.effect(), plain.nameKey(),
                plain.descriptionKey(), Optional.of(Identifier.of("example", "skills/wind")));
        assertEquals(Identifier.of("example", "textures/skills/wind.png"),
                MasteryUiPolicy.iconTexture(authored).orElseThrow());
    }
}
