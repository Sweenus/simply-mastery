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
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EmberlashMasterySkillEffectTest {

    private static final List<Integer> ALL = IntStream.range(0, 27).boxed().toList();

    @Test
    void endlessSmoulderCarriesATunableEcho() {
        Phase5AbilityTuning endless = smoulder(List.of(7));

        assertEquals(.5, endless.get(Setting.EMBERLASH_ECHO_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(60, endless.integer(Setting.LOCKOUT_TICKS, 0));
        assertEquals(.75, endless.get(Setting.EMBERLASH_SMOULDER_DAMAGE_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void readyCauteryReachesOnlyTheActiveDefinition() {
        assertEquals(68, cautery(List.of(10)).integer(Setting.COOLDOWN_TICKS, 80));
        assertEquals(68, smoulder(List.of(10)).integer(Setting.COOLDOWN_TICKS, 80));
    }

    @Test
    void theSmoulderDamageMultipliersCompose() {
        assertEquals(1.1, smoulder(List.of(0)).get(Setting.EMBERLASH_SMOULDER_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.825, smoulder(List.of(0, 7)).get(Setting.EMBERLASH_SMOULDER_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(1.595, smoulder(List.of(0, 8)).get(Setting.EMBERLASH_SMOULDER_DAMAGE_MULTIPLIER, 1), 1.0E-6);
    }

    @Test
    void smoulderAndReprisalNeverShareCapsOrDurations() {
        Phase5AbilityTuning all = smoulder(List.of(1, 2, 19, 26));

        assertEquals(6, all.integer(Setting.EMBERLASH_SMOULDER_STACK_CAP, 0));
        assertEquals(160, all.integer(Setting.EMBERLASH_SMOULDER_DURATION_TICKS, 0));
        assertEquals(5, all.integer(Setting.EMBERLASH_REPRISAL_STACK_CAP, 0));
        assertEquals(200, all.integer(Setting.EMBERLASH_REPRISAL_DURATION_TICKS, 0));
        assertFalse(all.has(Setting.STACK_CAP));
        assertFalse(all.has(Setting.DURATION_TICKS));
    }

    @Test
    void ashenBrandAndDeepScorchResolveInNodeOrder() {
        assertEquals(6, smoulder(List.of(1)).integer(Setting.EMBERLASH_SMOULDER_STACK_CAP, 0));
        assertEquals(3, smoulder(List.of(1, 8)).integer(Setting.EMBERLASH_SMOULDER_STACK_CAP, 0));
    }

    @Test
    void theCauteryRidersKeepTheirOwnKeys() {
        Phase5AbilityTuning all = cautery(List.of(11, 12, 13, 14, 15));

        assertEquals(4, all.get(Setting.EMBERLASH_CAUTERY_ABSORPTION, 0), 1.0E-6);
        assertEquals(60, all.integer(Setting.EMBERLASH_CAUTERY_ABSORPTION_TICKS, 0));
        assertEquals(30, all.integer(Setting.EMBERLASH_BLIND_DURATION_TICKS, 0));
        assertEquals(2, all.integer(Setting.EMBERLASH_BACKLASH_STACKS, 0));
        assertEquals(80, all.integer(Setting.EMBERLASH_BACKLASH_DURATION_TICKS, 0));
        assertEquals(40, all.integer(Setting.EMBERLASH_BURNING_PACE_DURATION_TICKS, 0));
        assertFalse(all.has(Setting.FIRE_TICKS));
    }

    @Test
    void everyNodeContributesItsOwnOwnershipBit() {
        int mode = smoulder(ALL).integer(Setting.MODE, 0);

        for (int slot = 0; slot < 27; slot++) {
            assertTrue((mode & (1 << slot)) != 0, "ownership bit " + slot);
        }
    }

    private static Phase5AbilityTuning smoulder(List<Integer> nodes) {
        return tune(Phase5UniqueAbilities.EMBERLASH_SMOULDER, nodes);
    }

    private static Phase5AbilityTuning cautery(List<Integer> nodes) {
        return tune(Phase5UniqueAbilities.EMBERLASH_CAUTERY, nodes);
    }

    private static Phase5AbilityTuning tune(UniqueAbilityDefinition definition, List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile("emberlash");
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition);
        if (definition.cooldownKey().isPresent()) {
            builder.set(Phase5UniqueAbilities.COOLDOWN_TICKS, 80);
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
