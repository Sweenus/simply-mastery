package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.BuiltInFamilyProfiles;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.Phase5AbilityTuning;
import net.sweenus.simplyswords.api.ability.Phase5AbilityTuning.Setting;
import net.sweenus.simplyswords.api.ability.Phase5UniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HearthflameMasterySkillEffectTest {

    private static final List<Integer> ALL = IntStream.range(0, 27).boxed().toList();

    @Test
    void everyHearthflameNodeRoutesToThePhase5Abilities() {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("hearthflame");

        assertEquals(27, profile.nodes().size());
        for (int index = 0; index < profile.nodes().size(); index++) {
            MasteryProfile.Node node = profile.nodes().get(index);
            assertInstanceOf(AbilitySkillEffectType.class, SkillEffectRegistry.get(node.effect().type()), node.id());
            assertEquals(index, node.effect().parameters().get("kind"), node.id());
        }
    }

    @Test
    void judgmentPyreComposesWithViolentSnap() {
        assertEquals(1.2, chains(List.of(22)).get(Setting.HEARTH_SNAP_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.6, chains(List.of(25)).get(Setting.HEARTH_SNAP_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.92, chains(List.of(22, 25)).get(Setting.HEARTH_SNAP_DAMAGE_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void sixfoldSentenceCarriesItsGateRatherThanAPreAppliedMultiplier() {
        Phase5AbilityTuning sixfold = chains(List.of(6));

        assertEquals(6, sixfold.integer(Setting.HEARTH_FINAL_CHAIN_REQUIREMENT, 0));
        assertEquals(1.25, sixfold.get(Setting.HEARTH_FINAL_CHAIN_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1, sixfold.get(Setting.HEARTH_FINAL_DAMAGE_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void theOtherFinalDamageNodesStillCompose() {
        assertEquals(2, chains(List.of(8)).get(Setting.HEARTH_FINAL_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.3, chains(List.of(24)).get(Setting.HEARTH_FINAL_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(2.6, chains(List.of(8, 24)).get(Setting.HEARTH_FINAL_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(2.08, chains(List.of(8, 17, 24)).get(Setting.HEARTH_FINAL_DAMAGE_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void bothFireNodesNowOutlastTheWeaponIgnite() {
        assertEquals(120, chains(List.of(3)).integer(Setting.HEARTH_ECHO_FIRE_TICKS, 0));
        assertEquals(120, chains(List.of(20)).integer(Setting.HEARTH_BRAND_HIT_FIRE_TICKS, 0));
    }

    @Test
    void brandShelterIsBoundedAndExpires() {
        Phase5AbilityTuning shelter = chains(List.of(13));

        assertEquals(2, shelter.get(Setting.HEARTH_BRAND_ABSORPTION, 0), 1.0E-6);
        assertEquals(200, shelter.integer(Setting.HEARTH_BRAND_ABSORPTION_DURATION_TICKS, 0));
        assertEquals(8, shelter.get(Setting.HEARTH_BRAND_ABSORPTION_CAP, 0), 1.0E-6);
        assertEquals(40, shelter.integer(Setting.HEARTH_BRAND_ABSORPTION_LOCKOUT_TICKS, 0));
    }

    @Test
    void safeAtHomeGrantsTheAbsorptionItsTextPromises() {
        assertEquals(8, chains(List.of(15)).get(Setting.HEARTH_COMPLETION_ABSORPTION, 0), 1.0E-6);
        assertEquals(4, chains(List.of(9)).get(Setting.HEARTH_CAST_ABSORPTION, 0), 1.0E-6);
    }

    @Test
    void echoAndPullNodesStillCompose() {
        assertEquals(1.1, chains(List.of(0)).get(Setting.HEARTH_ECHO_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.265, chains(List.of(0, 4)).get(Setting.HEARTH_ECHO_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.518, chains(List.of(0, 4, 7)).get(Setting.HEARTH_ECHO_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.16 * 1.2 * .7, chains(List.of(2, 7)).get(Setting.PULL_STRENGTH, .16), 1.0E-6);
        assertEquals(0, chains(List.of(2, 7, 25)).get(Setting.PULL_STRENGTH, .16), 1.0E-6);
    }

    @Test
    void everyNodeContributesItsOwnOwnershipBit() {
        int mode = chains(ALL).integer(Setting.MODE, 0);

        for (int slot = 0; slot < 27; slot++) {
            assertTrue((mode & (1 << slot)) != 0, "ownership bit " + slot);
        }
        assertFalse((chains(List.of(6)).integer(Setting.MODE, 0) & (1 << 16)) != 0);
    }

    private static Phase5AbilityTuning chains(List<Integer> nodes) {
        return tune(Phase5UniqueAbilities.HEARTHFLAME_CHAINS, nodes);
    }

    private static Phase5AbilityTuning tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("hearthflame");
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition);
        if (definition.cooldownKey().isPresent()) {
            builder.set(Phase5UniqueAbilities.COOLDOWN_TICKS, 450);
        }
        builder.set(Phase5UniqueAbilities.TUNING, Phase5AbilityTuning.EMPTY);
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) {
            effect.tune(null, definition, builder, profile.nodes().get(node));
        }
        return builder.get(Phase5UniqueAbilities.TUNING);
    }
}
