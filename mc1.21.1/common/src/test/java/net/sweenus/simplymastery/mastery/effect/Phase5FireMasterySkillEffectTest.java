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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

final class Phase5FireMasterySkillEffectTest {

    @Test
    void everyReviewedFireNodeRoutesToThePhase5Effect() {
        assertProfile("hearthflame", 0);
        assertProfile("emberblade", 27);
        assertProfile("emberlash", 54);
    }

    @Test
    void hearthflameDurationsAndDamageChannelsStayIndependent() {
        Phase5AbilityTuning tuning = tune("hearthflame", Phase5UniqueAbilities.HEARTHFLAME_CHAINS,
                List.of(0, 4, 8, 19, 22, 24, 25));

        assertEquals(1.265, tuning.get(Setting.HEARTH_ECHO_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(132, tuning.integer(Setting.HEARTH_CHAIN_DURATION_TICKS, 0));
        assertEquals(430, tuning.integer(Setting.HEARTH_BRAND_DURATION_TICKS, 0));
        assertEquals(1.6, tuning.get(Setting.HEARTH_SNAP_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(2.6, tuning.get(Setting.HEARTH_FINAL_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(60, tuning.integer(Setting.HEARTH_FORCED_SNAP_TICKS, 0));
        assertFalse(tuning.has(Setting.DURATION_TICKS));
    }

    @Test
    void emberbladeChannelFragmentsAndBuffsKeepSeparateValues() {
        Phase5AbilityTuning tuning = tune("emberblade", Phase5UniqueAbilities.EMBERBLADE_SHRAPNEL,
                List.of(7, 8, 19, 20, 25, 26));

        assertEquals(50, tuning.integer(Setting.EMBERBLADE_CHANNEL_TICKS, 0));
        assertEquals(5, tuning.integer(Setting.EMBERBLADE_FRAGMENT_COUNT, 0));
        assertEquals(.45, tuning.get(Setting.EMBERBLADE_FRAGMENT_DAMAGE_MULTIPLIER, 0), 1.0E-6);
        assertEquals(.6, tuning.get(Setting.EMBERBLADE_MIN_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(.624, tuning.get(Setting.EMBERBLADE_MAX_DAMAGE_MULTIPLIER, 1), 1.0E-6);
        assertEquals(100, tuning.integer(Setting.EMBERBLADE_INCARNATE_DURATION_TICKS, 0));
        assertEquals(40, tuning.integer(Setting.EMBERBLADE_IRE_DURATION_BONUS_TICKS, 0));
        assertEquals(120, tuning.integer(Setting.COOLDOWN_TICKS, 0));
        assertFalse(tuning.has(Setting.DURATION_TICKS));
    }

    @Test
    void emberbladePayloadAndWhiteHeatKeepSeparateFireDurations() {
        Phase5AbilityTuning tuning = tune("emberblade", Phase5UniqueAbilities.EMBERBLADE_SHRAPNEL,
                List.of(5, 6));

        assertEquals(60, tuning.integer(Setting.FIRE_TICKS, 0));
        assertEquals(80, tuning.integer(Setting.EMBERBLADE_FULL_CHARGE_FIRE_TICKS, 0));
    }

    @Test
    void emberlashSmoulderAndReprisalNeverShareCapsOrDurations() {
        Phase5AbilityTuning tuning = tune("emberlash", Phase5UniqueAbilities.EMBERLASH_SMOULDER,
                List.of(1, 2, 8, 19, 26));

        assertEquals(3, tuning.integer(Setting.EMBERLASH_SMOULDER_STACK_CAP, 0));
        assertEquals(160, tuning.integer(Setting.EMBERLASH_SMOULDER_DURATION_TICKS, 0));
        assertEquals(5, tuning.integer(Setting.EMBERLASH_REPRISAL_STACK_CAP, 0));
        assertEquals(200, tuning.integer(Setting.EMBERLASH_REPRISAL_DURATION_TICKS, 0));
        assertFalse(tuning.has(Setting.STACK_CAP));
        assertFalse(tuning.has(Setting.DURATION_TICKS));
    }

    private static void assertProfile(String profilePath, int offset) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile(profilePath);
        assertEquals(27, profile.nodes().size());
        for (int index = 0; index < 27; index++) {
            MasteryProfile.Node node = profile.nodes().get(index);
            assertInstanceOf(AbilitySkillEffectType.class, SkillEffectRegistry.get(node.effect().type()), node.id());
            assertEquals(offset + index, node.effect().parameters().get("kind"), node.id());
        }
    }

    private static Phase5AbilityTuning tune(String profilePath, UniqueAbilityDefinition definition,
                                            List<Integer> nodes) {
        MasteryProfile profile = BuiltInFamilyProfiles.profile(profilePath);
        UniqueAbilityTuning.Builder builder = UniqueAbilityTuning.builder(definition)
                .set(Phase5UniqueAbilities.TUNING, Phase5AbilityTuning.EMPTY);
        if (definition.cooldownKey().isPresent()) {
            builder.set(Phase5UniqueAbilities.COOLDOWN_TICKS, definition == Phase5UniqueAbilities.EMBERBLADE_SHRAPNEL
                    ? 60 : 0);
        }
        AbilitySkillEffectType effect = (AbilitySkillEffectType) SkillEffectRegistry.get(
                profile.nodes().getFirst().effect().type());
        for (int node : nodes) effect.tune(null, definition, builder, profile.nodes().get(node));
        return builder.get(Phase5UniqueAbilities.TUNING);
    }
}
