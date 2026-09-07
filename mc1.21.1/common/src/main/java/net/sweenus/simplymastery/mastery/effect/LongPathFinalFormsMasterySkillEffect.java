package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplymastery.mastery.definition.MasteryCohort;
import net.sweenus.simplyswords.api.ability.LongPathFinalFormsMasteryTuning;
import net.sweenus.simplyswords.api.ability.LongPathFinalFormsMasteryAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityContext;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;

import java.util.List;
import java.util.Set;

final class LongPathFinalFormsMasterySkillEffect implements AbilitySkillEffectType {
    private static final Identifier ID = MasteryCohort.LONG_PATH_FINAL_FORMS.effectId();

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
        if (!effect.parameters().keySet().equals(Set.of("kind"))) {
            errors.add(where + "cohort/long_path_final_forms requires only kind");
        }
        int kind = effect.parameters().getOrDefault("kind", -1);
        if (kind < 0 || kind >= 81) errors.add(where + "kind must be between 0 and 80");
    }

    @Override
    public void tune(UniqueAbilityContext context, UniqueAbilityDefinition definition,
                     UniqueAbilityTuning.Builder tuning, MasteryProfile.Node node) {
        int kind = parameter(node, "kind", -1);
        if (kind < 0 || kind >= 81 || !matches(kind / 27, definition)) return;
        int profile = kind / 27;
        int branch = kind % 27 / 9;
        int slot = kind % 9;
        LongPathFinalFormsMasteryTuning value = tuning.get(LongPathFinalFormsMasteryAbilities.TUNING);
        value = switch (profile) {
            case 0 -> lichblade(value, definition, branch, slot);
            case 1 -> sunfire(value, definition, branch, slot);
            case 2 -> harbinger(value, definition, branch, slot);
            default -> value;
        };
        tuning.set(LongPathFinalFormsMasteryAbilities.TUNING, value);
        if (profile == 1 && branch == 2 && slot == 7
                && definition == LongPathFinalFormsMasteryAbilities.SUNFIRE_STANDARD) {
            tuning.set(LongPathFinalFormsMasteryAbilities.COOLDOWN_TICKS,
                    tuning.get(LongPathFinalFormsMasteryAbilities.COOLDOWN_TICKS)
                            + value.integer(s("PHOENIX_COOLDOWN_TICKS"), 300));
        }
        if (definition.cooldownKey().isPresent()) {
            tuning.set(LongPathFinalFormsMasteryAbilities.COOLDOWN_TICKS,
                    value.integer(s("COOLDOWN_TICKS"), tuning.get(LongPathFinalFormsMasteryAbilities.COOLDOWN_TICKS)));
        }
    }

    private static LongPathFinalFormsMasteryTuning lichblade(LongPathFinalFormsMasteryTuning t, UniqueAbilityDefinition definition,
                                                  int branch, int slot) {
        boolean aura = definition == LongPathFinalFormsMasteryAbilities.LICHBLADE_AURA;
        if (aura && (branch > 0 || slot >= 6)) return t;
        if (branch == 0) return switch (slot) {
            case 0 -> aura ? t.with(s("AURA_INTERVAL_TICKS"), 30) : t;
            case 1 -> t.with(s("RADIUS"), 3.5);
            case 2 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 3 -> mode(t, 1).with(s("SLOW_DURATION_TICKS"), 60).with(s("REPEAT_WINDOW_TICKS"), 40);
            case 4 -> mode(t, 2).with(s("PER_TARGET_BONUS"), .03).with(s("BONUS_CAP"), .24);
            case 5 -> mode(t, 4).with(s("DEATH_BURST_DAMAGE_MULTIPLIER"), .35)
                    .with(s("DEATH_BURST_RADIUS"), 3).with(s("DEATH_BURST_TARGET_CAP"), 4);
            case 6 -> mode(t, 8).with(s("FAST_PULSE_INTERVAL_TICKS"), 4)
                    .with(s("FAST_PULSE_AFTER_TICKS"), 60);
            case 7 -> mode(t, 16).with(s("RADIUS"), 6).with(s("TARGET_CAP"), 32)
                    .multiply(s("DAMAGE_MULTIPLIER"), .65, 1).with(s("MOVEMENT_MULTIPLIER"), .6)
                    .multiply(s("COOLDOWN_MULTIPLIER"), 1.2, 1);
            case 8 -> mode(t, 32).with(s("RADIUS"), 1.5).with(s("TARGET_CAP"), 1)
                    .multiply(s("DAMAGE_MULTIPLIER"), 2, 1);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("CHANCE"), 12);
            case 1 -> t.with(s("HEAL_AMOUNT"), .75);
            case 2 -> t.with(s("ABSORPTION_CAP"), 10);
            case 3 -> mode(t, 64).with(s("CHARGE_LOCKOUT_TICKS"), 10).with(s("CHARGE_PER_HIT"), 3);
            case 4 -> mode(t, 128).with(s("RESISTANCE_CHARGE_STEP"), 4)
                    .with(s("RESISTANCE_STEP_TICKS"), 40).with(s("RESISTANCE_DURATION_CAP_TICKS"), 120);
            case 5 -> mode(t, 256).with(s("HEAL_MULTIPLIER"), .5).with(s("TEMP_ABSORPTION_CAP"), 4)
                    .with(s("OVERHEAL_ABSORPTION_TICKS"), 80);
            case 6 -> mode(t, 512).with(s("INTEREST_CHARGE_STEP"), 10)
                    .with(s("INTEREST_PER_STEP"), .1).with(s("INTEREST_CAP"), .4);
            case 7 -> mode(t, 1024).multiply(s("DAMAGE_MULTIPLIER"), .7, 1)
                    .with(s("CHANCE"), 0).with(s("BASTION_CHARGE_PER_ABSORPTION"), 2)
                    .with(s("ABSORPTION_CAP"), 16).with(s("BASTION_ABSORPTION_TICKS"), 160);
            case 8 -> mode(t, 2048).with(s("CHANCE"), 25).with(s("HEAL_AMOUNT"), 1)
                    .with(s("COOLDOWN_PER_SIPHON_TICKS"), 10).with(s("COOLDOWN_PENALTY_CAP_TICKS"), 200);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("ACQUISITION_RANGE"), 26);
            case 1 -> t.with(s("CLOUD_MOVE_INTERVAL_TICKS"), 4);
            case 2 -> t.with(s("DURATION_TICKS"), 240);
            case 3 -> mode(t, 4096).with(s("INTERRUPT_RESISTANCE_TICKS"), 20);
            case 4 -> mode(t, 8192).with(s("SPEED"), 1.5);
            case 5 -> mode(t, 16384).with(s("RETARGET_RANGE"), 8).with(s("SELECTION_RETARGET_CAP"), 1)
                    .with(s("RETARGET_WINDOW_TICKS"), 80);
            case 6 -> t.with(s("COOLDOWN_TICKS"), 600);
            case 7 -> mode(t, 32768).with(s("RETARGET_RANGE"), 8).with(s("RETARGET_CAP"), 4)
                    .with(s("RETARGET_DAMAGE_PENALTY"), .15).with(s("RETARGET_DAMAGE_FLOOR"), .4);
            case 8 -> mode(t, 65536).with(s("RECALL_DAMAGE_MULTIPLIER"), 1.25)
                    .with(s("RECALL_RADIUS"), 4).with(s("RECALL_COOLDOWN_TICKS"), 100);
            default -> t;
        };
    }

    private static LongPathFinalFormsMasteryTuning sunfire(LongPathFinalFormsMasteryTuning t, UniqueAbilityDefinition definition,
                                                int branch, int slot) {
        boolean regen = definition == LongPathFinalFormsMasteryAbilities.SUNFIRE_REGEN;
        if (branch == 0 && !regen) return switch (slot) {
            case 0 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.with(s("RADIUS"), 7);
            case 2 -> t.with(s("FIRE_TICKS"), 40);
            case 3 -> mode(t, 1).with(s("WEAKNESS_PULSE_COUNT"), 3).with(s("WEAKNESS_WINDOW_TICKS"), 40)
                    .with(s("WEAKNESS_DURATION_TICKS"), 60);
            case 4 -> t.with(s("LANDING_DAMAGE_MULTIPLIER"), 3.5).with(s("LANDING_RADIUS"), 2)
                    .with(s("LANDING_TARGET_CAP"), 12);
            case 5 -> mode(t, 2).with(s("CYCLE_PULSE_COUNT"), 4).with(s("CYCLE_DAMAGE_MULTIPLIER"), 1.4)
                    .with(s("GLOWING_DURATION_TICKS"), 60);
            case 6 -> mode(t, 4).with(s("EARLY_WINDOW_TICKS"), 100).with(s("EARLY_DAMAGE_MULTIPLIER"), 1.2);
            case 7 -> mode(t, 8).with(s("RADIUS"), 5)
                    .multiply(s("DAMAGE_MULTIPLIER"), .75, 1);
            case 8 -> mode(t, 16).with(s("RADIUS"), 9).with(s("INTERVAL_TICKS"), 15)
                    .multiply(s("DAMAGE_MULTIPLIER"), 1.6, 1).with(s("LIFETIME_MULTIPLIER"), .65);
            default -> t;
        };
        if (branch == 1 && !regen) return switch (slot) {
            case 0 -> t.multiply(s("HEAL_MULTIPLIER"), 1.12, 1);
            case 1 -> t.with(s("SUPPORT_RADIUS"), 7);
            case 2 -> t.with(s("STRENGTH_DURATION_TICKS"), 120);
            case 3 -> mode(t, 32).with(s("CLEANSE_LOCKOUT_TICKS"), 160);
            case 4 -> mode(t, 64).with(s("ALLY_REGEN_TICKS"), 60);
            case 5 -> mode(t, 128).with(s("GUARDIAN_THRESHOLD"), .35).with(s("GUARDIAN_ABSORPTION"), 4)
                    .with(s("GUARDIAN_ABSORPTION_TICKS"), 100).with(s("GUARDIAN_LOCKOUT_TICKS"), 200);
            case 6 -> mode(t, 256).with(s("RALLY_ALLY_COUNT"), 3).with(s("RALLY_REFUND_TICKS"), 40);
            case 7 -> mode(t, 512).with(s("SUPPORT_INTERVAL_TICKS"), 40)
                    .multiply(s("HEAL_MULTIPLIER"), 1.5, 1).with(s("SANCTUARY_RESISTANCE_TICKS"), 60);
            case 8 -> mode(t, 1024).with(s("SUPPORT_RADIUS"), 5).with(s("STRENGTH_DURATION_TICKS"), 100)
                    .with(s("ALLY_CHARGE_TICKS"), 60).with(s("FIRE_TICKS"), 40);
            default -> t;
        };
        if (branch != 2 || regen && (slot == 5 || slot == 7)
                || !regen && slot != 5 && slot != 7) return t;
        return switch (slot) {
            case 0 -> t.with(s("CHANCE"), 20);
            case 1 -> t.with(s("STATUS_DURATION_TICKS"), 60);
            case 2 -> mode(t, 2048).with(s("FIRE_RESISTANCE_TICKS"), 80);
            case 3 -> mode(t, 4096).with(s("RESERVE_CAP"), 4).with(s("RESERVE_THRESHOLD"), .5)
                    .with(s("RESERVE_ABSORPTION_TICKS"), 100);
            case 4 -> mode(t, 8192).with(s("REPRISAL_FIRE_TICKS"), 40);
            case 5 -> mode(t, 16384).with(s("GUARD_RANGE"), 7).with(s("DAMAGE_REDUCTION"), .15)
                    .with(s("KNOCKBACK_RESISTANCE"), .2);
            case 6 -> mode(t, 32768).with(s("REKINDLE_THRESHOLD"), .3)
                    .with(s("REKINDLE_DURATION_TICKS"), 100).with(s("REKINDLE_ABSORPTION"), 4)
                    .with(s("REKINDLE_LOCKOUT_TICKS"), 600);
            case 7 -> mode(t, 65536).with(s("PHOENIX_DURATION_TICKS"), 80)
                    .with(s("PHOENIX_COOLDOWN_TICKS"), 300).multiply(s("DAMAGE_MULTIPLIER"), .8, 1);
            case 8 -> mode(t, 131072).with(s("CHANCE"), 0).with(s("COMBO_COUNT"), 3)
                    .with(s("COMBO_WINDOW_TICKS"), 80).with(s("FLARE_LOCKOUT_TICKS"), 40)
                    .with(s("RADIUS"), 3).with(s("FLARE_DAMAGE_MULTIPLIER"), .7)
                    .with(s("TARGET_CAP"), 8).with(s("HEAL_AMOUNT"), 1);
            default -> t;
        };
    }

    private static LongPathFinalFormsMasteryTuning harbinger(LongPathFinalFormsMasteryTuning t, UniqueAbilityDefinition definition,
                                                  int branch, int slot) {
        boolean omen = definition == LongPathFinalFormsMasteryAbilities.HARBINGER_OMEN;
        if (branch == 0 && !omen) return switch (slot) {
            case 0 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.with(s("RADIUS"), 7);
            case 2 -> t.multiply(s("PULL_STRENGTH"), 1.25, .25);
            case 3 -> mode(t, 1).with(s("WEAKNESS_PULSE_COUNT"), 3).with(s("WEAKNESS_WINDOW_TICKS"), 40)
                    .with(s("WEAKNESS_DURATION_TICKS"), 80);
            case 4 -> t.with(s("LANDING_DAMAGE_MULTIPLIER"), 3.5).with(s("LANDING_RADIUS"), 2)
                    .with(s("LANDING_TARGET_CAP"), 12);
            case 5 -> mode(t, 2).with(s("CORE_RANGE"), 2).with(s("NEAR_DAMAGE_MULTIPLIER"), 1.2);
            case 6 -> mode(t, 4).with(s("CYCLE_PULSE_COUNT"), 5).with(s("CYCLE_PULL_STRENGTH"), 1.5)
                    .with(s("CYCLE_DAMAGE_MULTIPLIER"), 1.4);
            case 7 -> mode(t, 8).with(s("PURSUIT_RANGE"), 12).with(s("MOVEMENT_SPEED"), .3)
                    .with(s("RADIUS"), 4).multiply(s("DAMAGE_MULTIPLIER"), .8, 1);
            case 8 -> mode(t, 16).with(s("RADIUS"), 9).with(s("INTERVAL_TICKS"), 15)
                    .with(s("CONSTANT_PULL_STRENGTH"), 1.5).multiply(s("DAMAGE_MULTIPLIER"), 1.6, 1)
                    .with(s("LIFETIME_MULTIPLIER"), .65);
            default -> t;
        };
        if (branch == 1 && !omen) return switch (slot) {
            case 0 -> t.with(s("HASTE_DURATION_TICKS"), 120);
            case 1 -> t.with(s("SUPPORT_RADIUS"), 7);
            case 2 -> mode(t, 32).with(s("OWNER_AURA_RANGE"), 7);
            case 3 -> mode(t, 64).with(s("ALLY_SPEED_TICKS"), 80);
            case 4 -> mode(t, 128).with(s("ALLY_CHARGE_TICKS"), 80).with(s("ALLY_WEAKNESS_TICKS"), 60);
            case 5 -> mode(t, 256).with(s("RALLY_ALLY_COUNT"), 3).with(s("RALLY_REFUND_TICKS"), 40);
            case 6 -> mode(t, 512).with(s("ALLY_CHARGE_TICKS"), 80).with(s("SUPPORT_DAMAGE_BONUS"), .1)
                    .with(s("KNOCKBACK_RESISTANCE"), .15);
            case 7 -> mode(t, 1024).with(s("SUPPORT_INTERVAL_TICKS"), 40)
                    .with(s("STATUS_AMPLIFIER"), 3).with(s("OWNER_HASTE_AMPLIFIER"), 1)
                    .with(s("ALLY_SPEED_TICKS"), 60).with(s("HASTE_DURATION_TICKS"), 60)
                    .with(s("ALLY_CHARGE_TICKS"), 60).with(s("SUPPORT_DAMAGE_BONUS"), .15);
            case 8 -> mode(t, 2048).with(s("RADIUS"), 5).multiply(s("DAMAGE_MULTIPLIER"), 1.25, 1)
                    .with(s("OWNER_HASTE_AMPLIFIER"), 2);
            default -> t;
        };
        if (branch != 2 || omen && slot == 6 || !omen && slot < 6) return t;
        return switch (slot) {
            case 0 -> t.with(s("CHANCE"), 20);
            case 1 -> t.with(s("STATUS_DURATION_TICKS"), 220);
            case 2 -> mode(t, 4096).with(s("OMEN_UPGRADE_COUNT"), 3).with(s("OMEN_UPGRADE_TICKS"), 80)
                    .with(s("OMEN_WINDOW_TICKS"), 200);
            case 3 -> t.with(s("MELEE_DAMAGE_MULTIPLIER"), 1.1);
            case 4 -> mode(t, 8192).with(s("DOOM_PULL_RANGE"), 10).with(s("PULL_STRENGTH"), .75)
                    .with(s("DOOM_PULL_LOCKOUT_TICKS"), 20);
            case 5 -> mode(t, 16384).with(s("REFUND_TICKS"), 20).with(s("PROPHECY_REFUND_CAP_TICKS"), 100);
            case 6 -> mode(t, 32768).with(s("LOW_HEALTH_THRESHOLD"), .25)
                    .with(s("LOW_HEALTH_DAMAGE_MULTIPLIER"), 1.2).with(s("BOSS_DAMAGE_MULTIPLIER"), 1.1);
            case 7 -> mode(t, 65536).with(s("CHANCE"), 100).with(s("PLAGUE_DAMAGE_MULTIPLIER"), .8)
                    .with(s("PLAGUE_WEAKNESS_TICKS"), 120);
            case 8 -> mode(t, 131072).with(s("EXECUTION_DAMAGE_MULTIPLIER"), 1.4)
                    .with(s("EXECUTION_COOLDOWN_TICKS"), 80).add(s("COOLDOWN_TICKS"), 80, 700);
            default -> t;
        };
    }

    private static boolean matches(int profile, UniqueAbilityDefinition definition) {
        return switch (profile) {
            case 0 -> definition == LongPathFinalFormsMasteryAbilities.LICHBLADE_AURA
                    || definition == LongPathFinalFormsMasteryAbilities.LICHBLADE_CHANNEL;
            case 1 -> definition == LongPathFinalFormsMasteryAbilities.SUNFIRE_STANDARD
                    || definition == LongPathFinalFormsMasteryAbilities.SUNFIRE_REGEN;
            case 2 -> definition == LongPathFinalFormsMasteryAbilities.HARBINGER_STANDARD
                    || definition == LongPathFinalFormsMasteryAbilities.HARBINGER_OMEN;
            default -> false;
        };
    }

    private static LongPathFinalFormsMasteryTuning mode(LongPathFinalFormsMasteryTuning tuning, int bit) {
        return tuning.with(s("MODE"), tuning.integer(s("MODE"), 0) | bit);
    }

    private static LongPathFinalFormsMasteryTuning.Setting s(String name) {
        return LongPathFinalFormsMasteryTuning.Setting.valueOf(name);
    }

    private static int parameter(MasteryProfile.Node node, String key, int fallback) {
        return node.effect().parameters().getOrDefault(key, fallback);
    }
}
