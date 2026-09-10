package net.sweenus.simplyswordsmastery.mastery.effect;

import net.minecraft.util.Identifier;
import net.sweenus.simplyswordsmastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswordsmastery.mastery.definition.MasteryCohort;
import net.sweenus.simplyswords.api.ability.NatureSwarmMasteryTuning;
import net.sweenus.simplyswords.api.ability.NatureSwarmMasteryAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;

import java.util.List;
import java.util.Set;

final class NatureSwarmMasterySkillEffect implements StaticAbilitySkillEffectType {
    private static final Identifier ID = MasteryCohort.NATURE_SWARM.effectId();

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
        if (!effect.parameters().keySet().equals(Set.of("kind"))) {
            errors.add(where + "cohort/nature_swarm requires only kind");
        }
        int kind = effect.parameters().getOrDefault("kind", -1);
        if (kind < 0 || kind >= 108) errors.add(where + "kind must be between 0 and 107");
    }

    @Override
    public void tuneStatic(UniqueAbilityDefinition definition,
                     UniqueAbilityTuning.Builder tuning, MasteryProfile.Node node) {
        int kind = node.effect().parameters().getOrDefault("kind", -1);
        if (kind < 0 || kind >= 108) return;
        int profile = kind / 27;
        int branch = kind % 27 / 9;
        int slot = kind % 9;
        if (!matches(profile, branch, slot, definition)) return;
        NatureSwarmMasteryTuning value = tuning.get(NatureSwarmMasteryAbilities.TUNING);
        if (profile != 0) value = mode(value, 1 << (branch * 9 + slot));
        value = switch (profile) {
            case 0 -> bramblethorn(value, branch, slot);
            case 1 -> waxweaver(value, branch, slot);
            case 2 -> hiveheart(value, branch, slot);
            case 3 -> chompolotl(value, branch, slot);
            default -> value;
        };
        tuning.set(NatureSwarmMasteryAbilities.TUNING, value);
        if (definition.cooldownKey().isPresent()) {
            tuning.set(NatureSwarmMasteryAbilities.COOLDOWN_TICKS,
                    value.integer(s("COOLDOWN_TICKS"), tuning.get(NatureSwarmMasteryAbilities.COOLDOWN_TICKS)));
        }
    }

    private static NatureSwarmMasteryTuning bramblethorn(NatureSwarmMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("BRAMBLE_GRASP_RANGE_BONUS"), 4);
            case 1 -> t.with(s("BRAMBLE_GRASP_RADIUS_BONUS"), 1.5);
            case 2 -> t.with(s("BRAMBLE_GRASP_TRAVEL_TICK_BONUS"), -3);
            case 3 -> t.with(s("BRAMBLE_BIND_DURATION_BONUS_TICKS"), 20);
            case 4 -> t.with(s("BRAMBLE_PULL_MULTIPLIER"), 1.2);
            case 5 -> t.with(s("BRAMBLE_SHARED_DAMAGE_RATIO"), .42);
            case 6 -> t.with(s("BRAMBLE_SLAM_DAMAGE_MULTIPLIER"), 1.2)
                    .with(s("BRAMBLE_LIFT_FORCE_MULTIPLIER"), 1.15);
            case 7 -> t.with(s("BRAMBLE_TANGLED_TARGET_CAP"), 10)
                    .with(s("BRAMBLE_TANGLED_SHARED_DAMAGE_RATIO"), .25)
                    .with(s("BRAMBLE_TANGLED_DISABLE_PULL"), 1)
                    .with(s("BRAMBLE_TANGLED_DISABLE_SLAM"), 1);
            case 8 -> t.with(s("BRAMBLE_HANGMAN_TARGET_CAP"), 1)
                    .with(s("BRAMBLE_HANGMAN_LIFT_FORCE_MULTIPLIER"), 1.4)
                    .with(s("BRAMBLE_HANGMAN_SLAM_DAMAGE_MULTIPLIER"), 2.25);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("BRAMBLE_HUNT_MEMORY_BONUS_TICKS"), 40);
            case 1 -> t.with(s("BRAMBLE_HUNT_COOLDOWN_BONUS_TICKS"), -2);
            case 2 -> t.with(s("BRAMBLE_HUNT_RANGE_BONUS"), 2);
            case 3 -> t.with(s("BRAMBLE_HUNT_WIDTH_BONUS"), .25)
                    .with(s("BRAMBLE_HUNT_DAMAGE_MULTIPLIER"), 1.1);
            case 4 -> t.with(s("BRAMBLE_HUNT_SLOW_DURATION_BONUS_TICKS"), 20)
                    .with(s("BRAMBLE_HUNT_MIN_SLOW_AMPLIFIER"), 1);
            case 5 -> t.with(s("BRAMBLE_HUNT_PATH_TARGET_CAP"), 4);
            case 6 -> t.with(s("BRAMBLE_CROSSCUT_TARGET_COUNT"), 3)
                    .with(s("BRAMBLE_CROSSCUT_WINDOW_TICKS"), 100)
                    .with(s("BRAMBLE_CROSSCUT_DAMAGE_MULTIPLIER"), 1.35);
            case 7 -> t.with(s("BRAMBLE_WALTZ_PROJECTILE_COUNT"), 2)
                    .with(s("BRAMBLE_WALTZ_DAMAGE_MULTIPLIER"), .55)
                    .with(s("BRAMBLE_WALTZ_DISABLE_SLOW"), 1);
            case 8 -> t.with(s("BRAMBLE_PREDATOR_TARGET_ONLY"), 1)
                    .with(s("BRAMBLE_PREDATOR_DAMAGE_MULTIPLIER"), 1.8)
                    .with(s("BRAMBLE_PREDATOR_MEMORY_TICKS"), 30);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("BRAMBLE_BARKSKIN_ABSORPTION"), 4)
                    .with(s("BRAMBLE_BARKSKIN_DURATION_TICKS"), 80);
            case 1 -> t.with(s("BRAMBLE_ROOTED_GUARD_TARGET_COUNT"), 3)
                    .with(s("BRAMBLE_ROOTED_GUARD_INCOMING_MULTIPLIER"), .88);
            case 2 -> t.with(s("BRAMBLE_RETORT_DAMAGE_MULTIPLIER"), .2)
                    .with(s("BRAMBLE_RETORT_LOCKOUT_TICKS"), 30);
            case 3 -> t.with(s("BRAMBLE_SAP_RISE_ABSORPTION"), 2)
                    .with(s("BRAMBLE_SAP_RISE_CAP"), 8);
            case 4 -> t.with(s("BRAMBLE_SHELTERWOOD_OUTGOING_MULTIPLIER"), .9);
            case 5 -> t.with(s("BRAMBLE_THORNWARD_TARGET_COUNT"), 6)
                    .with(s("BRAMBLE_THORNWARD_DURATION_TICKS"), 100)
                    .with(s("BRAMBLE_THORNWARD_AMPLIFIER"), 0);
            case 6 -> t.with(s("BRAMBLE_RECLAIMED_REFUND_TICKS"), 12)
                    .with(s("BRAMBLE_RECLAIMED_REFUND_CAP_TICKS"), 48);
            case 7 -> t.with(s("BRAMBLE_ANCIENT_BIND_DURATION_MULTIPLIER"), 1.5)
                    .with(s("BRAMBLE_ANCIENT_RESISTANCE_AMPLIFIER"), 1)
                    .with(s("BRAMBLE_ANCIENT_SHARED_DAMAGE_MULTIPLIER"), .7);
            case 8 -> t.with(s("BRAMBLE_BRIAR_RADIUS"), 2)
                    .with(s("BRAMBLE_BRIAR_DAMAGE_MULTIPLIER"), .5)
                    .with(s("BRAMBLE_BRIAR_TARGET_CAP"), 16);
            default -> t;
        };
    }

    private static NatureSwarmMasteryTuning waxweaver(NatureSwarmMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("WAX_PRISON_RANGE_BONUS"), 3);
            case 1 -> t.with(s("WAX_PRISON_DURATION_BONUS_TICKS"), 20);
            case 2 -> t.with(s("WAX_TAUNT_RADIUS_BONUS"), 2);
            case 3 -> t.with(s("WAX_TAUNT_INTERVAL_BONUS_TICKS"), -2);
            case 4 -> t.with(s("WAX_EXPLOSION_DAMAGE_MULTIPLIER"), 1.12)
                    .with(s("WAX_EXPLOSION_FIRE_BONUS_TICKS"), 20);
            case 5 -> t.with(s("WAX_EXPLOSION_RADIUS_BONUS"), .75);
            case 6 -> t.with(s("WAX_BRITTLE_DAMAGE_PER_STEP"), .08)
                    .with(s("WAX_BRITTLE_DURATION_REDUCTION_TICKS"), 10);
            case 7 -> t.with(s("WAX_IRON_DURATION_TICKS"), 200)
                    .with(s("WAX_IRON_DAMAGE_MULTIPLIER"), .6)
                    .with(s("WAX_IRON_OUTGOING_MULTIPLIER"), .75);
            case 8 -> t.with(s("WAX_VOLATILE_DURATION_TICKS"), 60)
                    .with(s("WAX_VOLATILE_DAMAGE_MULTIPLIER"), 1.9)
                    .with(s("WAX_VOLATILE_TAUNT_RADIUS_MULTIPLIER"), .5);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("WAX_TEMPO_DURATION_BONUS_TICKS"), 20);
            case 1 -> t.with(s("WAX_TEMPO_STACK_CAP"), 4);
            case 2 -> t.with(s("WAX_ENCASED_FIRE_TICKS"), 60);
            case 3 -> t.with(s("WAX_RHYTHM_SPEED_BONUS"), .08);
            case 4 -> t.with(s("WAX_REFUND_PER_HIT_TICKS"), 4)
                    .with(s("WAX_REFUND_CAP_TICKS"), 24)
                    .with(s("WAX_REFUND_WINDOW_TICKS"), 80);
            case 5 -> t.with(s("WAX_CANDLE_STEP_TICKS"), 30);
            case 6 -> t.with(s("WAX_FLASH_MULTIPLIER"), 1.3)
                    .with(s("WAX_FLASH_WINDOW_TICKS"), 120);
            case 7 -> t.with(s("WAX_TEMPO_STACK_CAP"), 6)
                    .with(s("WAX_FRENZY_BONUS_MULTIPLIER"), 1.5)
                    .with(s("WAX_FRENZY_DURATION_TICKS"), 30);
            case 8 -> t.with(s("WAX_TEMPO_STACK_CAP"), 1)
                    .with(s("WAX_PATIENT_DURATION_TICKS"), 160);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("WAX_REVIVE_RESISTANCE_BONUS_TICKS"), 40);
            case 1 -> t.with(s("WAX_REVIVE_COOLDOWN_BONUS_TICKS"), -100);
            case 2 -> t.with(s("WAX_FIRST_LAYER_ABSORPTION"), 4)
                    .with(s("WAX_FIRST_LAYER_DURATION_TICKS"), 60);
            case 3 -> t.with(s("WAX_REACTIVE_HEALTH_THRESHOLD"), .35)
                    .with(s("WAX_REACTIVE_DURATION_TICKS"), 40)
                    .with(s("WAX_REACTIVE_COOLDOWN_TICKS"), 200);
            case 4 -> t.with(s("WAX_MOLTEN_RADIUS"), 4)
                    .with(s("WAX_MOLTEN_FIRE_TICKS"), 80)
                    .with(s("WAX_MOLTEN_TARGET_CAP"), 8);
            case 5 -> t.with(s("WAX_REFUGE_INCOMING_MULTIPLIER"), .85);
            case 6 -> t.with(s("WAX_SECOND_SKIN_ABSORPTION"), 6)
                    .with(s("WAX_SECOND_SKIN_DURATION_TICKS"), 100);
            case 7 -> t.with(s("WAX_QUEEN_HEALTH_THRESHOLD"), .5)
                    .with(s("WAX_QUEEN_RESISTANCE_AMPLIFIER"), 3)
                    .with(s("WAX_QUEEN_RESISTANCE_TICKS"), 100)
                    .with(s("WAX_QUEEN_COOLDOWN_BONUS_TICKS"), 400);
            case 8 -> t.with(s("WAX_EMERGENCE_RADIUS"), 6)
                    .with(s("WAX_EMERGENCE_DAMAGE_MULTIPLIER"), 1.25)
                    .with(s("WAX_EMERGENCE_TARGET_CAP"), 16);
            default -> t;
        };
    }

    private static NatureSwarmMasteryTuning hiveheart(NatureSwarmMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("HIVE_PROC_DAMAGE_MULTIPLIER"), 1.12);
            case 1 -> t.with(s("HIVE_PROC_COOLDOWN_BONUS_TICKS"), -10);
            case 2 -> t.with(s("HIVE_PROC_RANGE_BONUS"), 3);
            case 3 -> t.with(s("HIVE_PROC_POISON_TICKS"), 40);
            case 4 -> t.with(s("HIVE_SHARED_TARGET_MULTIPLIER"), 1.15)
                    .with(s("HIVE_SHARED_TARGET_TICKS"), 80);
            case 5 -> t.with(s("HIVE_TWIN_COUNT"), 2)
                    .with(s("HIVE_TWIN_DAMAGE_MULTIPLIER"), .6);
            case 6 -> t.with(s("HIVE_KILL_REFUND_TICKS"), 20);
            case 7 -> t.with(s("HIVE_EXECUTION_HEALTH_THRESHOLD"), .35)
                    .with(s("HIVE_EXECUTION_DAMAGE_MULTIPLIER"), 2.2)
                    .with(s("HIVE_EXECUTION_COOLDOWN_MULTIPLIER"), 2);
            case 8 -> t.with(s("HIVE_BUSY_DAMAGE_MULTIPLIER"), .35)
                    .with(s("HIVE_BUSY_BEE_CAP"), 4);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("HIVE_SWARM_COUNT_BONUS"), 2)
                    .with(s("HIVE_SWARM_COUNT_CAP"), 12);
            case 1 -> t.with(s("HIVE_SWARM_RADIUS_BONUS"), 2)
                    .with(s("HIVE_SWARM_SEARCH_CAP"), 12);
            case 2 -> t.with(s("HIVE_SWARM_DURATION_BONUS_TICKS"), 60);
            case 3 -> t.with(s("HIVE_SWARM_STING_INTERVAL_TICKS"), 9);
            case 4 -> t.with(s("HIVE_SWARM_STING_COUNT"), 12);
            case 5 -> t.with(s("HIVE_SWARM_SLOW_AMPLIFIER_BONUS"), 1);
            case 6 -> t.with(s("HIVE_FOCUS_REQUIRED_STINGS"), 3)
                    .with(s("HIVE_FOCUS_DAMAGE_MULTIPLIER"), 1.2)
                    .with(s("HIVE_FOCUS_DURATION_TICKS"), 80);
            case 7 -> t.with(s("HIVE_CLOUD_COUNT"), 16)
                    .with(s("HIVE_CLOUD_DAMAGE_MULTIPLIER"), .55)
                    .with(s("HIVE_CLOUD_RADIUS"), 12)
                    .with(s("HIVE_CLOUD_DURATION_MULTIPLIER"), .6);
            case 8 -> t.with(s("HIVE_HUNT_COUNT"), 4)
                    .with(s("HIVE_HUNT_DAMAGE_MULTIPLIER"), 1.5)
                    .with(s("HIVE_HUNT_STING_COUNT"), 24)
                    .with(s("HIVE_HUNT_RADIUS"), 12)
                    .with(s("HIVE_HUNT_DURATION_TICKS"), 240);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("HIVE_WARD_ABSORPTION"), 4)
                    .with(s("HIVE_WARD_DURATION_TICKS"), 80);
            case 1 -> t.with(s("HIVE_GUARD_RANGE"), 3)
                    .with(s("HIVE_GUARD_INCOMING_MULTIPLIER"), .75)
                    .with(s("HIVE_GUARD_LOCKOUT_TICKS"), 40);
            case 2 -> t.with(s("HIVE_WARNING_RADIUS"), 4)
                    .with(s("HIVE_WARNING_DURATION_TICKS"), 30)
                    .with(s("HIVE_WARNING_LOCKOUT_TICKS"), 60);
            case 3 -> t.with(s("HIVE_RETORT_DAMAGE_MULTIPLIER"), .25)
                    .with(s("HIVE_RETORT_LOCKOUT_TICKS"), 30);
            case 4 -> t.with(s("HIVE_RALLY_STING_COUNT"), 8)
                    .with(s("HIVE_RALLY_RESISTANCE_TICKS"), 40);
            case 5 -> t.with(s("HIVE_ESCORT_BEE_COUNT"), 4)
                    .with(s("HIVE_ESCORT_SPEED_MULTIPLIER"), 1.1);
            case 6 -> t.with(s("HIVE_SAVE_HEALTH_THRESHOLD"), .3)
                    .with(s("HIVE_SAVE_ABSORPTION_PER_BEE"), 2)
                    .with(s("HIVE_SAVE_ABSORPTION_CAP"), 10);
            case 7 -> t.with(s("HIVE_PHALANX_RANGE"), 4)
                    .with(s("HIVE_PHALANX_BEE_COUNT"), 6)
                    .with(s("HIVE_PHALANX_RESISTANCE_AMPLIFIER"), 1);
            case 8 -> t.with(s("HIVE_VENGEFUL_DAMAGE_MULTIPLIER"), 1.5)
                    .with(s("HIVE_VENGEFUL_RANGE"), 12);
            default -> t;
        };
    }

    private static NatureSwarmMasteryTuning chompolotl(NatureSwarmMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("CHOMP_PROC_DAMAGE_MULTIPLIER"), 1.12);
            case 1 -> t.with(s("CHOMP_PROC_COOLDOWN_BONUS_TICKS"), -10);
            case 2 -> t.with(s("CHOMP_LIFESPAN_BONUS_TICKS"), 80);
            case 3 -> t.with(s("CHOMP_LOW_HEALTH_THRESHOLD"), .4)
                    .with(s("CHOMP_LOW_HEALTH_DAMAGE_MULTIPLIER"), 1.15);
            case 4 -> t.with(s("CHOMP_SPLASH_DAMAGE_MULTIPLIER"), .2)
                    .with(s("CHOMP_SPLASH_RADIUS"), 2)
                    .with(s("CHOMP_SPLASH_TARGET_CAP"), 4);
            case 5 -> t.with(s("CHOMP_PACK_RANGE"), 5)
                    .with(s("CHOMP_PACK_DAMAGE_PER_ALLY"), .05)
                    .with(s("CHOMP_PACK_ALLY_CAP"), 4);
            case 6 -> t.with(s("CHOMP_CHAIN_RANGE"), 8)
                    .with(s("CHOMP_CHAIN_EXTENSION_TICKS"), 60);
            case 7 -> t.with(s("CHOMP_COLOSSAL_DAMAGE_MULTIPLIER"), 2.5)
                    .with(s("CHOMP_COLOSSAL_SPLASH_MULTIPLIER"), 1)
                    .with(s("CHOMP_COLOSSAL_SPLASH_RADIUS"), 3)
                    .with(s("CHOMP_COLOSSAL_COOLDOWN_MULTIPLIER"), 3).with(s("CHOMP_COLOSSAL_MINIMUM_TICKS"), 600);
            case 8 -> t.with(s("CHOMP_RELEASE_COUNT"), 4)
                    .with(s("CHOMP_RELEASE_DAMAGE_MULTIPLIER"), .45)
                    .with(s("CHOMP_RELEASE_LIFESPAN_TICKS"), 160);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("CHOMP_TARGET_RANGE_BONUS"), 3)
                    .with(s("CHOMP_TARGET_SEARCH_CAP"), 16);
            case 1 -> t.with(s("CHOMP_RALLY_SPEED_DURATION_TICKS"), 100)
                    .with(s("CHOMP_RALLY_SPEED_AMPLIFIER"), 1);
            case 2 -> t.with(s("CHOMP_SHOULDER_AURA_BONUS"), 2);
            case 3 -> t.with(s("CHOMP_HELPFUL_ABSORPTION"), 4)
                    .with(s("CHOMP_HELPFUL_DURATION_TICKS"), 80)
                    .with(s("CHOMP_HELPFUL_LOCKOUT_TICKS"), 200);
            case 4 -> t.with(s("CHOMP_COORDINATED_DAMAGE_MULTIPLIER"), 1.2);
            case 5 -> t.with(s("CHOMP_POUNCE_RANGE"), 4)
                    .with(s("CHOMP_POUNCE_INTERVAL_TICKS"), 40);
            case 6 -> t.with(s("CHOMP_VICTORY_KILL_COUNT"), 3)
                    .with(s("CHOMP_VICTORY_WINDOW_TICKS"), 200)
                    .with(s("CHOMP_VICTORY_REFUND_TICKS"), 100);
            case 7 -> t.with(s("CHOMP_BRIGADE_COUNT"), 3)
                    .with(s("CHOMP_BRIGADE_AURA_MULTIPLIER"), 3);
            case 8 -> t.with(s("CHOMP_HUNTER_COUNT"), 3)
                    .with(s("CHOMP_HUNTER_DAMAGE_MULTIPLIER"), 1.35)
                    .with(s("CHOMP_HUNTER_LIFESPAN_TICKS"), 300);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("CHOMP_BLUE_LIFESPAN_BONUS_TICKS"), 80);
            case 1 -> t.with(s("CHOMP_GRACE_DURATION_BONUS_TICKS"), 100);
            case 2 -> t.with(s("CHOMP_BLUE_DAMAGE_MULTIPLIER"), 1.25);
            case 3 -> t.with(s("CHOMP_GUARD_RANGE"), 6)
                    .with(s("CHOMP_GUARD_INCOMING_MULTIPLIER"), .85);
            case 4 -> t.with(s("CHOMP_CLEANSE_LOCKOUT_TICKS"), 400)
                    .with(s("CHOMP_CLEANSE_EFFECT_COUNT"), 1);
            case 5 -> t.with(s("CHOMP_RESCUE_HEALTH_THRESHOLD"), .35)
                    .with(s("CHOMP_RESCUE_DURATION_TICKS"), 60)
                    .with(s("CHOMP_RESCUE_RESISTANCE_AMPLIFIER"), 1);
            case 6 -> t.with(s("CHOMP_FIRST_BITE_REFUND_PERCENT"), 25);
            case 7 -> t.with(s("CHOMP_ETERNAL_LIFESPAN_TICKS"), 600)
                    .with(s("CHOMP_ETERNAL_AURA_RADIUS"), 6).with(s("CHOMP_ETERNAL_HEALTH"), 70)
                    .with(s("CHOMP_ETERNAL_INTERCEPTION"), .5);
            case 8 -> t.with(s("CHOMP_RAVAGER_DAMAGE_MULTIPLIER"), 1.25)
                    .with(s("CHOMP_RAVAGER_SCALE"), 2).with(s("CHOMP_WAVE_CHARGE_TICKS"), 10)
                    .with(s("CHOMP_RAVAGER_LIFESPAN_TICKS"), 1800)
                    .with(s("COOLDOWN_TICKS"), 3600);
            default -> t;
        };
    }

    private static boolean matches(int profile, int branch, int slot, UniqueAbilityDefinition definition) {
        return switch (profile) {
            case 0 -> definition == NatureSwarmMasteryAbilities.BRAMBLE_GRASP ? branch != 1
                    : definition == NatureSwarmMasteryAbilities.BRAMBLE_HUNT && branch == 1;
            case 1 -> definition == NatureSwarmMasteryAbilities.WAXWEAVER_PRISON
                    ? branch == 0 || branch == 2 && (slot == 2 || slot == 5)
                    : definition == NatureSwarmMasteryAbilities.WAXWEAVER_TEMPO ? branch == 1
                    : definition == NatureSwarmMasteryAbilities.WAXWEAVER_REVIVAL && branch == 2;
            case 2 -> definition == NatureSwarmMasteryAbilities.HIVEHEART_PROC ? branch == 0
                    : definition == NatureSwarmMasteryAbilities.HIVEHEART_SWARM && branch != 0;
            case 3 -> definition == NatureSwarmMasteryAbilities.CHOMPOLOTL_PROC
                    ? branch == 0 || branch == 1 && slot < 7
                    : (definition == NatureSwarmMasteryAbilities.CHOMPOLOTL_RALLY
                    || definition == NatureSwarmMasteryAbilities.CHOMPOLOTL_GUARDIAN)
                    && (branch != 0 || slot != 1 && slot < 7);
            default -> false;
        };
    }

    private static NatureSwarmMasteryTuning mode(NatureSwarmMasteryTuning tuning, int bit) {
        return tuning.with(s("MODE"), tuning.integer(s("MODE"), 0) | bit);
    }

    private static NatureSwarmMasteryTuning.Setting s(String name) {
        return NatureSwarmMasteryTuning.Setting.valueOf(name);
    }
}
