package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SkillEffectRegistryTest {

    @Test
    void primitiveFamiliesAreRegistered() {
        assertTrue(SkillEffectRegistry.ids().containsAll(List.of(
                Identifier.of("simplymastery", "none"),
                Identifier.of("simplymastery", "stormstep"),
                Identifier.of("simplymastery", "echo_strike"),
                Identifier.of("simplymastery", "combo_surge"),
                Identifier.of("simplymastery", "combo_strike"),
                Identifier.of("simplymastery", "battle_flow"),
                Identifier.of("simplymastery", "hindering_strike"),
                Identifier.of("simplymastery", "finishing_strike"),
                Identifier.of("simplymastery", "soul_mend"),
                Identifier.of("simplymastery", "kill_flow"),
                Identifier.of("simplymastery", "counterstrike"),
                Identifier.of("simplymastery", "guarded_recovery"),
                Identifier.of("simplymastery", "leeching_strike"),
                Identifier.of("simplymastery", "cleaving_echo"),
                Identifier.of("simplymastery", "stormbreak_conduit"),
                Identifier.of("simplymastery", "eye_of_storm"),
                Identifier.of("simplymastery", "thunderhead"),
                Identifier.of("simplymastery", "static_reserve"),
                Identifier.of("simplymastery", "charged_pursuit"),
                Identifier.of("simplymastery", "building_voltage"),
                Identifier.of("simplymastery", "supercell"),
                Identifier.of("simplymastery", "sulfurous_edge"),
                Identifier.of("simplymastery", "chain_reaction"),
                Identifier.of("simplymastery", "perpetual_furnace"),
                Identifier.of("simplymastery", "last_reprisal"))));
    }

    @Test
    void parametersAreTypedAndBounded() {
        List<String> errors = new ArrayList<>();
        SkillEffectRegistry.validate(new MasteryProfile.Effect(Identifier.of("simplymastery", "echo_strike"),
                Map.of("damage_tenths", 0, "unknown", 1)), "node: ", errors);
        assertEquals(2, errors.size());
    }

    @Test
    void recursionGuardRejectsNestedOriginsAndAlwaysClears() {
        assertFalse(SkillOriginGuard.active());
        assertTrue(SkillOriginGuard.run(new SkillOriginGuard.Origin(null, null, "outer"),
                () -> !SkillOriginGuard.run(new SkillOriginGuard.Origin(null, null, "inner"), () -> true)));
        assertFalse(SkillOriginGuard.active());
    }
}
