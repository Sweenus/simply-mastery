package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplymastery.mastery.definition.MasteryCohort;
import net.sweenus.simplyswords.api.ability.DeathShadowBloodMasteryTuning;
import net.sweenus.simplyswords.api.ability.DeathShadowBloodMasteryAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityContext;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;

import java.util.List;
import java.util.Set;

final class DeathShadowBloodMasterySkillEffect implements AbilitySkillEffectType {
    private static final Identifier ID = MasteryCohort.DEATH_SHADOW_BLOOD.effectId();

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
        if (!effect.parameters().keySet().equals(Set.of("kind"))) {
            errors.add(where + "cohort/death_shadow_blood requires only kind");
        }
        int kind = effect.parameters().getOrDefault("kind", -1);
        if (kind < 0 || kind >= 162) errors.add(where + "kind must be between 0 and 161");
    }

    @Override
    public void tune(UniqueAbilityContext context, UniqueAbilityDefinition definition,
                     UniqueAbilityTuning.Builder tuning, MasteryProfile.Node node) {
        int kind = node.effect().parameters().getOrDefault("kind", -1);
        if (kind < 0 || kind >= 162) return;
        int profile = kind / 27;
        int branch = kind % 27 / 9;
        int slot = kind % 9;
        if (!matches(profile, branch, definition)) return;
        DeathShadowBloodMasteryTuning value = mode(tuning.get(DeathShadowBloodMasteryAbilities.TUNING), 1 << (branch * 9 + slot));
        value = switch (profile) {
            case 0 -> plague(value, branch, slot);
            case 1 -> soulkeeper(value, branch, slot);
            case 2 -> soulstealer(value, branch, slot);
            case 3 -> twisted(value, branch, slot);
            case 4 -> shadowsting(value, branch, slot);
            case 5 -> bloodwake(value, branch, slot);
            default -> value;
        };
        if (definition.cooldownKey().isPresent()) {
            int base = value.integer(s("MASTERY_BASE_COOLDOWN_TICKS"),
                    tuning.get(DeathShadowBloodMasteryAbilities.COOLDOWN_TICKS));
            value = value.with(s("MASTERY_BASE_COOLDOWN_TICKS"), base);
            int cooldown = profile == 5
                    ? base + value.integer(s("BLOOD_COOLDOWN_BONUS_TICKS"), 0)
                    : value.integer(s("COOLDOWN_TICKS"), base);
            if (profile == 1 && branch == 2) {
                cooldown = (int) Math.round(cooldown
                        * value.get(s("SOUL_COOLDOWN_MULTIPLIER"), 1));
            }
            tuning.set(DeathShadowBloodMasteryAbilities.COOLDOWN_TICKS, Math.max(1, cooldown));
        }
        tuning.set(DeathShadowBloodMasteryAbilities.TUNING, value);
    }

    private static DeathShadowBloodMasteryTuning plague(DeathShadowBloodMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("PLAGUE_CONVERSION_CHANCE_BONUS"), 10);
            case 1 -> t.with(s("PLAGUE_FEVER_DURATION_BONUS_TICKS"), 40);
            case 2 -> t.add(s("PLAGUE_CONVERSION_FEVER_BONUS"), 1, 0);
            case 3 -> t.add(s("PLAGUE_CONVERSION_FEVER_BONUS"), 1, 0)
                    .add(s("PLAGUE_CARRIED_DURATION_BONUS"), .1, 0);
            case 4 -> t.with(s("PLAGUE_SYMPTOM_DURATION_TICKS"), 30);
            case 5 -> t.with(s("PLAGUE_INCUBATION_DURATION_BONUS_TICKS"), 80)
                    .with(s("PLAGUE_INCUBATION_LOCKOUT_TICKS"), 100);
            case 6 -> t.with(s("PLAGUE_CRITICAL_FEVER_THRESHOLD"), .8)
                    .with(s("PLAGUE_CRITICAL_DAMAGE_MULTIPLIER"), 1.12);
            case 7 -> t.with(s("PLAGUE_CARRIED_DURATION_BONUS"), 1)
                    .add(s("PLAGUE_APOTHEOSIS_FEVER_BONUS"), 3, 0)
                    .multiply(s("PLAGUE_TOLL_DAMAGE_MULTIPLIER"), .8, 1);
            case 8 -> t.with(s("PLAGUE_PATIENT_ZERO_LOCKOUT_TICKS"), 200);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.multiply(s("PLAGUE_TOLL_DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.with(s("PLAGUE_TOLL_RADIUS_BONUS"), .75);
            case 2 -> t.with(s("PLAGUE_FIRST_TOLL_WINDUP_TICKS"), 4);
            case 3 -> t.with(s("PLAGUE_LOW_HEALTH_THRESHOLD"), .4)
                    .with(s("PLAGUE_LOW_HEALTH_DAMAGE_MULTIPLIER"), 1.15);
            case 4 -> t.with(s("PLAGUE_WEAKNESS_DURATION_TICKS"), 40);
            case 5 -> t.with(s("PLAGUE_DAMAGE_PER_CASCADE_STEP"), .05)
                    .with(s("PLAGUE_CASCADE_STEP_CAP"), 5);
            case 6 -> t.with(s("PLAGUE_FINAL_DAMAGE_MULTIPLIER"), 1.25)
                    .with(s("PLAGUE_FINAL_RADIUS_MULTIPLIER"), 1.3);
            case 7 -> t.with(s("PLAGUE_PAIRED_DELAY_TICKS"), 6)
                    .with(s("PLAGUE_PAIRED_DAMAGE_MULTIPLIER"), .65)
                    .with(s("PLAGUE_PAIRED_RADIUS_MULTIPLIER"), .8);
            case 8 -> t.with(s("PLAGUE_SINGLE_TARGET_CAP"), 1)
                    .multiply(s("PLAGUE_TOLL_DAMAGE_MULTIPLIER"), 1.8, 1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("PLAGUE_FEVER_SPREAD_BONUS"), 1);
            case 1 -> t.with(s("PLAGUE_CASCADE_RANGE_BONUS"), 1);
            case 2 -> t.add(s("PLAGUE_CASCADE_TOLL_BONUS"), 1, 0);
            case 3 -> t.add(s("PLAGUE_CARRIED_DURATION_BONUS"), .15, 0);
            case 4 -> t.with(s("PLAGUE_WAKE_RADIUS"), 2.5)
                    .with(s("PLAGUE_WAKE_DURATION_TICKS"), 30)
                    .with(s("PLAGUE_WAKE_TARGET_CAP"), 6);
            case 5 -> t.with(s("PLAGUE_RELAPSE_DELAY_TICKS"), 40)
                    .with(s("PLAGUE_RELAPSE_FEVER"), 1);
            case 6 -> t.add(s("PLAGUE_CASCADE_TOLL_BONUS"), 2, 0)
                    .with(s("PLAGUE_CASCADE_TOLL_CAP"), 9);
            case 7 -> t.with(s("PLAGUE_REVISIT_DELAY_TICKS"), 20)
                    .with(s("PLAGUE_REVISIT_DAMAGE_MULTIPLIER"), .5)
                    .with(s("PLAGUE_REVISIT_TOLL_PENALTY"), 2);
            case 8 -> t.with(s("PLAGUE_QUARANTINE_RADIUS"), 7)
                    .with(s("PLAGUE_QUARANTINE_FEVER"), 3)
                    .with(s("PLAGUE_QUARANTINE_TARGET_CAP"), 12);
            default -> t;
        };
    }

    private static DeathShadowBloodMasteryTuning soulkeeper(DeathShadowBloodMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.multiply(s("SOUL_CONTACT_DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.with(s("SOUL_ORBIT_RADIUS_BONUS"), .35);
            case 2 -> t.with(s("SOUL_GLOW_DURATION_TICKS"), 40);
            case 3 -> t.with(s("SOUL_CROSSING_WINDOW_TICKS"), 20)
                    .with(s("SOUL_CROSSING_DAMAGE_MULTIPLIER"), 1.2);
            case 4 -> t.with(s("SOULBRAND_DAMAGE_MULTIPLIER"), 1.08)
                    .with(s("SOULBRAND_DURATION_TICKS"), 40);
            case 5 -> t.with(s("SOUL_REPEAT_DELAY_TICKS"), 12);
            case 6 -> t.with(s("SOUL_SHEAR_HIT_COUNT"), 5)
                    .with(s("SOUL_SHEAR_DAMAGE_MULTIPLIER"), 1.5);
            case 7 -> t.with(s("SOUL_ORBIT_RADIUS_MULTIPLIER"), .7)
                    .multiply(s("SOUL_CONTACT_DAMAGE_MULTIPLIER"), 1.55, 1);
            case 8 -> t.with(s("SOUL_ORBIT_RADIUS_MULTIPLIER"), 1.6)
                    .multiply(s("SOUL_CONTACT_DAMAGE_MULTIPLIER"), .75, 1)
                    .with(s("SOUL_CLEAVE_TARGET_CAP"), 2)
                    .with(s("SOUL_CLEAVE_RADIUS"), 1.5)
                    .with(s("SOUL_CLEAVE_DAMAGE_MULTIPLIER"), .6);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("SOUL_SPEED_GAIN_MULTIPLIER"), 1.25);
            case 1 -> t.with(s("SOUL_SPEED_DECAY_MULTIPLIER"), .8);
            case 2 -> t.with(s("SOUL_MAX_SPEED_BONUS"), .75);
            case 3 -> t.with(s("SOUL_FRICTION_DAMAGE_PER_SPEED"), .03)
                    .with(s("SOUL_FRICTION_DAMAGE_CAP"), .12);
            case 4 -> t.with(s("SOUL_RUSH_THRESHOLD"), 4)
                    .with(s("SOUL_RUSH_DURATION_TICKS"), 40);
            case 5 -> t.with(s("SOUL_CAPTURE_DURATION_TICKS"), 60);
            case 6 -> t.with(s("SOUL_CYCLONE_PULL_STRENGTH"), .25)
                    .with(s("SOUL_CYCLONE_TARGET_CAP"), 8)
                    .with(s("SOUL_CYCLONE_INTERVAL_TICKS"), 10);
            case 7 -> t.with(s("SOUL_CEASELESS_MIN_SPEED"), 3)
                    .with(s("SOUL_CEASELESS_MAX_SPEED"), 4);
            case 8 -> t.with(s("SOUL_DETONATION_RADIUS"), 4)
                    .with(s("SOUL_DETONATION_DAMAGE_PER_SPEED"), .18);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("SOUL_EXTRA_DURATION_BONUS_TICKS"), 60);
            case 1 -> t.with(s("SOUL_COOLDOWN_MULTIPLIER"), .9);
            case 2 -> t.with(s("SOUL_FIFTH_LANTERN_DURATION_TICKS"), 80);
            case 3 -> t.with(s("SOUL_WARD_ABSORPTION"), 4)
                    .with(s("SOUL_WARD_DURATION_TICKS"), 80);
            case 4 -> t.with(s("SOUL_INTERCEPT_RADIUS"), 1)
                    .with(s("SOUL_INTERCEPT_INTERVAL_TICKS"), 40);
            case 5 -> t.with(s("SOUL_ALLY_RADIUS"), 5)
                    .with(s("SOUL_ALLY_DURATION_TICKS"), 20)
                    .with(s("SOUL_ALLY_TARGET_CAP"), 4);
            case 6 -> t.with(s("SOUL_RECALL_WINDOW_TICKS"), 60)
                    .with(s("SOUL_RECALL_EXTENSION_TICKS"), 80);
            case 7 -> t.with(s("SOUL_GRAND_LANTERN_COUNT"), 6)
                    .with(s("SOUL_GRAND_DURATION_TICKS"), 180)
                    .with(s("SOUL_GRAND_DAMAGE_MULTIPLIER"), .7);
            case 8 -> t.with(s("SOUL_LONE_LANTERN_COUNT"), 3)
                    .with(s("SOUL_LONE_DURATION_TICKS"), 220)
                    .with(s("SOUL_LONE_ABSORPTION"), 4)
                    .with(s("SOUL_LONE_INTERVAL_TICKS"), 30);
            default -> t;
        };
    }

    private static DeathShadowBloodMasteryTuning soulstealer(DeathShadowBloodMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("SOULSTEALER_CHANCE_BONUS"), 8);
            case 1 -> t.add(s("SOULSTEALER_MAX_DEBT_BONUS"), 2, 0);
            case 2 -> t.with(s("SOULSTEALER_KILL_DEBT_BONUS"), 1);
            case 3 -> t.with(s("SOULSTEALER_COMPOUND_INTERVAL"), 3)
                    .with(s("SOULSTEALER_COMPOUND_DEBT"), 1);
            case 4 -> t.with(s("SOULSTEALER_SHIELD_MULTIPLIER"), .85)
                    .with(s("SOULSTEALER_SHIELD_LOCKOUT_TICKS"), 40);
            case 5 -> t.with(s("SOULSTEALER_MARK_HITS"), 2)
                    .with(s("SOULSTEALER_MARK_CHANCE_BONUS"), 15)
                    .with(s("SOULSTEALER_MARK_DURATION_TICKS"), 80);
            case 6 -> t.with(s("SOULSTEALER_COLLECTOR_TICKS_PER_STACK"), 40)
                    .with(s("SOULSTEALER_COLLECTOR_DURATION_CAP_TICKS"), 120);
            case 7 -> t.add(s("SOULSTEALER_MAX_DEBT_BONUS"), -2, 0)
                    .with(s("SOULSTEALER_HIT_DEBT_OVERRIDE"), 1);
            case 8 -> t.add(s("SOULSTEALER_MAX_DEBT_BONUS"), 5, 0)
                    .with(s("SOULSTEALER_KILL_DEBT_OVERRIDE"), 3)
                    .with(s("SOULSTEALER_CHANCE_MULTIPLIER"), .5);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("SOULSTEALER_TARGET_RANGE_BONUS"), 2);
            case 1 -> t.with(s("SOULSTEALER_TARGET_ANGLE_BONUS"), 20);
            case 2 -> t.with(s("SOULSTEALER_SEARCH_MULTIPLIER"), 1.5)
                    .with(s("SOULSTEALER_SEARCH_CAP"), 24);
            case 3 -> t.with(s("SOULSTEALER_VEIL_DURATION_TICKS"), 20);
            case 4 -> t.with(s("SOULSTEALER_HAMSTRING_DURATION_TICKS"), 30)
                    .with(s("SOULSTEALER_HAMSTRING_AMPLIFIER"), 2);
            case 5 -> t.with(s("SOULSTEALER_TETHER_RANGE"), 3);
            case 6 -> t.with(s("SOULSTEALER_PREDATORY_REFUND_TICKS"), 80);
            case 7 -> t.with(s("SOULSTEALER_PURSUIT_RANGE"), 8)
                    .with(s("SOULSTEALER_PURSUIT_DAMAGE_CAP"), 3);
            case 8 -> t.with(s("SOULSTEALER_RETURN_DELAY_TICKS"), 12)
                    .with(s("SOULSTEALER_RETURN_RESISTANCE_TICKS"), 40)
                    .with(s("SOULSTEALER_RETURN_RESISTANCE_AMPLIFIER"), 1)
                    .with(s("SOULSTEALER_ESCAPE_DAMAGE_MULTIPLIER"), .75);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("SOULSTEALER_DAMAGE_PER_DEBT_BONUS"), .08);
            case 1 -> t.with(s("SOULSTEALER_SPELL_MULTIPLIER"), 1.1);
            case 2 -> t.with(s("SOULSTEALER_ARMOR_IGNORE_RATIO"), .1);
            case 3 -> t.with(s("SOULSTEALER_WITHER_DURATION_TICKS"), 60)
                    .with(s("SOULSTEALER_WITHER_AMPLIFIER"), 0);
            case 4 -> t.with(s("SOULSTEALER_EXACT_PAYMENT_MULTIPLIER"), 1.25);
            case 5 -> t.with(s("SOULSTEALER_RESIDUAL_DEBT"), 1);
            case 6 -> t.with(s("SOULSTEALER_DEATH_TAX_DEBT"), 2)
                    .with(s("SOULSTEALER_DEATH_TAX_REFUND_TICKS"), 40);
            case 7 -> t.with(s("SOULSTEALER_FORECLOSURE_DAMAGE_PER_DEBT"), .2)
                    .with(s("SOULSTEALER_FORECLOSURE_STACK_CAP"), 12)
                    .with(s("SOULSTEALER_FORECLOSURE_FAILURE_SPEND"), .5);
            case 8 -> t.with(s("SOULSTEALER_INSTALLMENT_MAX_SPEND"), 3)
                    .with(s("SOULSTEALER_INSTALLMENT_DAMAGE_MULTIPLIER"), .8);
            default -> t;
        };
    }

    private static DeathShadowBloodMasteryTuning twisted(DeathShadowBloodMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("TWISTED_CHANCE_BONUS"), 8);
            case 1 -> t.with(s("TWISTED_DURATION_BONUS_TICKS"), 30);
            case 2 -> t.with(s("TWISTED_ATTACK_SPEED_PER_STACK_BONUS"), .01);
            case 3 -> t.with(s("TWISTED_GUARANTEED_HIT_INTERVAL"), 5);
            case 4 -> t.with(s("TWISTED_CADENCE_THRESHOLD"), 8)
                    .with(s("TWISTED_CADENCE_BONUS_TICKS"), 40)
                    .with(s("TWISTED_CADENCE_BONUS_CAP_TICKS"), 120);
            case 5 -> t.with(s("TWISTED_FOOTWORK_THRESHOLD"), 8);
            case 6 -> t.with(s("TWISTED_MAX_STACK_BONUS"), 3)
                    .with(s("TWISTED_OVERFLOW_THRESHOLD"), 15)
                    .with(s("TWISTED_OVERFLOW_ATTACK_SPEED_PER_STACK"), .05);
            case 7 -> t.with(s("TWISTED_ENDLESS_MAX_STACKS"), 10)
                    .with(s("TWISTED_ENDLESS_HIT_WINDOW_TICKS"), 40);
            case 8 -> t.with(s("TWISTED_FEVER_MAX_STACKS"), 20)
                    .with(s("TWISTED_FEVER_HEALTH_THRESHOLD"), .5)
                    .with(s("TWISTED_FEVER_GAIN"), 2)
                    .with(s("TWISTED_FEVER_DURATION_TICKS"), 80);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("TWISTED_CRESCENDO_DAMAGE_MULTIPLIER"), 1.1);
            case 1 -> t.with(s("TWISTED_CRESCENDO_RADIUS_BONUS"), .4);
            case 2 -> t.with(s("TWISTED_CRESCENDO_INTERVAL_BONUS"), -1);
            case 3 -> t.with(s("TWISTED_CRESCENDO_KNOCKBACK_BONUS"), .15);
            case 4 -> t.with(s("TWISTED_SYNC_INTERVAL"), 2)
                    .with(s("TWISTED_SYNC_DAMAGE_MULTIPLIER"), 1.15)
                    .with(s("TWISTED_SYNC_PULL_STRENGTH"), .2);
            case 5 -> t.with(s("TWISTED_WOUND_HITS"), 2)
                    .with(s("TWISTED_WOUND_WINDOW_TICKS"), 60)
                    .with(s("TWISTED_WOUND_DAMAGE_MULTIPLIER"), 1.1);
            case 6 -> t.with(s("TWISTED_DOUBLE_INTERVAL"), 3)
                    .with(s("TWISTED_DOUBLE_DELAY_TICKS"), 4)
                    .with(s("TWISTED_DOUBLE_DAMAGE_MULTIPLIER"), .6);
            case 7 -> t.with(s("TWISTED_SOLO_TARGET_CAP"), 1)
                    .with(s("TWISTED_SOLO_RANGE"), 5)
                    .with(s("TWISTED_SOLO_DAMAGE_MULTIPLIER"), 1.9);
            case 8 -> t.with(s("TWISTED_ORCHESTRA_RADIUS_MULTIPLIER"), 1.75)
                    .with(s("TWISTED_ORCHESTRA_TARGET_CAP"), 16)
                    .with(s("TWISTED_ORCHESTRA_DAMAGE_MULTIPLIER"), .65)
                    .with(s("TWISTED_ORCHESTRA_KNOCKBACK_MULTIPLIER"), 0);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("TWISTED_FINALE_WINDOW_BONUS_TICKS"), 30);
            case 1 -> t.with(s("TWISTED_FINALE_MIN_DAMAGE_MULTIPLIER"), 1.15);
            case 2 -> t.with(s("TWISTED_FINALE_MAX_DAMAGE_MULTIPLIER"), 1.2);
            case 3 -> t.with(s("TWISTED_FINALE_RADIUS_BONUS"), .5);
            case 4 -> t.with(s("TWISTED_FINALE_KNOCKBACK_BONUS"), .2);
            case 5 -> t.with(s("TWISTED_ENCORE_REFUND_STACKS"), 4)
                    .with(s("TWISTED_ENCORE_DELAY_TICKS"), 20);
            case 6 -> t.with(s("TWISTED_PERFECT_WINDOW_TICKS"), 20)
                    .with(s("TWISTED_PERFECT_DAMAGE_MULTIPLIER"), 1.2);
            case 7 -> t.with(s("TWISTED_SHATTER_RADIUS"), 5)
                    .with(s("TWISTED_SHATTER_TARGET_CAP"), 16)
                    .with(s("TWISTED_SHATTER_WINDOW_TICKS"), 40);
            case 8 -> t.with(s("TWISTED_SUSTAINED_HIT_COUNT"), 3)
                    .with(s("TWISTED_SUSTAINED_DAMAGE_MULTIPLIER"), .55)
                    .with(s("TWISTED_SUSTAINED_CONSUME_RATIO"), .5);
            default -> t;
        };
    }

    private static DeathShadowBloodMasteryTuning shadowsting(DeathShadowBloodMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("SHADOW_CHANCE_BONUS"), 8);
            case 1 -> t.multiply(s("SHADOW_CLONE_DAMAGE_MULTIPLIER"), 1.12, 1);
            case 2 -> t.add(s("SHADOW_CLONE_DELAY_BONUS_TICKS"), -1, 0);
            case 3 -> t.with(s("SHADOW_MARK_DURATION_TICKS"), 60)
                    .with(s("SHADOW_MARK_OUTGOING_MULTIPLIER"), 1.08);
            case 4 -> t.with(s("SHADOW_VEIL_DURATION_TICKS"), 12);
            case 5 -> t.with(s("SHADOW_TWIN_INTERVAL"), 4)
                    .with(s("SHADOW_TWIN_DELAY_TICKS"), 5)
                    .with(s("SHADOW_TWIN_DAMAGE_MULTIPLIER"), .5);
            case 6 -> t.with(s("SHADOW_KILL_REFUND_TICKS"), 20);
            case 7 -> t.with(s("SHADOW_MIRROR_COUNT"), 3)
                    .with(s("SHADOW_MIRROR_DAMAGE_MULTIPLIER"), .45)
                    .with(s("SHADOW_CHANCE_PENALTY"), 15);
            case 8 -> t.with(s("SHADOW_FLAWLESS_DAMAGE_MULTIPLIER"), 1.25)
                    .with(s("SHADOW_FLAWLESS_LOCKOUT_TICKS"), 60);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("SHADOW_DANCE_DURATION_BONUS_TICKS"), 20, 0);
            case 1 -> t.add(s("SHADOW_DANCE_INTERVAL_BONUS"), -1, 0)
                    .with(s("SHADOW_DANCE_INTERVAL_FLOOR"), 3);
            case 2 -> t.add(s("SHADOW_DANCE_RADIUS_BONUS"), 2, 0);
            case 3 -> t.with(s("SHADOW_CHAIN_DAMAGE_PER_STEP"), .05)
                    .with(s("SHADOW_CHAIN_STEP_CAP"), 5);
            case 4 -> t.with(s("SHADOW_LOW_HEALTH_THRESHOLD"), .4);
            case 5 -> t.with(s("SHADOW_GUARD_AMPLIFIER"), 0);
            case 6 -> t.with(s("SHADOW_FLOURISH_RADIUS"), 3)
                    .with(s("SHADOW_FLOURISH_DAMAGE_MULTIPLIER"), .6)
                    .with(s("SHADOW_FLOURISH_TARGET_CAP"), 8);
            case 7 -> t.add(s("SHADOW_DANCE_DURATION_BONUS_TICKS"), 60, 0)
                    .add(s("SHADOW_DANCE_INTERVAL_BONUS"), 2, 0)
                    .with(s("SHADOW_MACABRE_DAMAGE_MULTIPLIER"), .65);
            case 8 -> t.with(s("SHADOW_DANCE_DURATION_MULTIPLIER"), .5)
                    .with(s("SHADOW_WALTZ_DAMAGE_MULTIPLIER"), 1.5);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("SHADOW_ARRIVAL_DISTANCE_BONUS"), 1);
            case 1 -> t.with(s("SHADOW_SMOKE_RADIUS"), 2.5)
                    .with(s("SHADOW_SMOKE_DURATION_TICKS"), 20)
                    .with(s("SHADOW_SMOKE_TARGET_CAP"), 6);
            case 2 -> t.with(s("SHADOW_SNARE_RADIUS"), 3)
                    .with(s("SHADOW_SNARE_DURATION_TICKS"), 40)
                    .with(s("SHADOW_SNARE_AMPLIFIER"), 1)
                    .with(s("SHADOW_SNARE_TARGET_CAP"), 6);
            case 3 -> t.add(s("SHADOW_RETURN_BONUS_TICKS"), -2, 0);
            case 4 -> t.with(s("SHADOW_DISORIENT_DURATION_TICKS"), 25)
                    .with(s("SHADOW_DISORIENT_AMPLIFIER"), 1);
            case 5 -> t.with(s("SHADOW_SPEED_DURATION_TICKS"), 40)
                    .with(s("SHADOW_SPEED_AMPLIFIER"), 1);
            case 6 -> t.with(s("SHADOW_REPRIEVE_INTERVAL"), 3)
                    .with(s("SHADOW_REPRIEVE_ABSORPTION"), 4)
                    .with(s("SHADOW_REPRIEVE_CAP"), 12);
            case 7 -> t.with(s("SHADOW_NOCTURNE_DURATION_TICKS"), 60);
            case 8 -> t.with(s("SHADOW_KILLING_RESISTANCE_TICKS"), 30)
                    .with(s("SHADOW_KILLING_RESISTANCE_AMPLIFIER"), 2)
                    .with(s("SHADOW_KILLING_SKIPPED_STRIKES"), 2);
            default -> t;
        };
    }

    private static DeathShadowBloodMasteryTuning bloodwake(DeathShadowBloodMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("BLOOD_VEIN_INTERVAL"), 3).with(s("BLOOD_VEIN_BONUS_STACKS"), 1);
            case 1 -> t.add(s("BLOOD_BURST_RADIUS_BONUS"), .5, 0);
            case 2 -> t.multiply(s("BLOOD_BURST_DAMAGE_MULTIPLIER"), 1.12, 1);
            case 3 -> t.add(s("BLOOD_BLEED_DURATION_BONUS_TICKS"), 40, 0);
            case 4 -> t.with(s("BLOOD_FEAST_INTERVAL"), 2).with(s("BLOOD_FEAST_FRENZY"), 1);
            case 5 -> t.with(s("BLOOD_HEMORRHAGE_THRESHOLD"), .35)
                    .with(s("BLOOD_HEMORRHAGE_MULTIPLIER"), 1.25);
            case 6 -> t.with(s("BLOOD_CHAIN_DELAY_TICKS"), 8)
                    .with(s("BLOOD_CHAIN_DAMAGE_MULTIPLIER"), .55)
                    .with(s("BLOOD_CHAIN_CAP"), 4);
            case 7 -> t.with(s("BLOOD_CONE_RANGE"), 7)
                    .with(s("BLOOD_CONE_DOT"), .35)
                    .with(s("BLOOD_CONE_TARGET_CAP"), 12)
                    .with(s("BLOOD_SPRAY_DAMAGE_MULTIPLIER"), 1.35);
            case 8 -> t.with(s("BLOOD_SACRAMENT_RADIUS_MULTIPLIER"), .65)
                    .with(s("BLOOD_SACRAMENT_FRENZY"), 1)
                    .with(s("BLOOD_SACRAMENT_ABSORPTION"), 8)
                    .with(s("BLOOD_SACRAMENT_DAMAGE_MULTIPLIER"), .7);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("BLOOD_COOLDOWN_BONUS_TICKS"), -2, 0);
            case 1 -> t.add(s("BLOOD_WAVE_STEP_BONUS"), 3, 0);
            case 2 -> t.add(s("BLOOD_SCREAM_TARGET_BONUS"), 2, 0);
            case 3 -> t.add(s("BLOOD_BLADE_HOVER_BONUS_TICKS"), -6, 0)
                    .multiply(s("BLOOD_BLADE_DAMAGE_MULTIPLIER"), 1.1, 1);
            case 4 -> t.add(s("BLOOD_FLY_COUNT_BONUS"), 2, 0).with(s("BLOOD_FLY_COUNT_CAP"), 12);
            case 5 -> t.add(s("BLOOD_DELUGE_INTERVAL_BONUS"), -3, 0)
                    .with(s("BLOOD_DELUGE_INTERVAL_FLOOR"), 8);
            case 6 -> t.with(s("BLOOD_MEMORY_RITES"), 3)
                    .with(s("BLOOD_MEMORY_WINDOW_TICKS"), 300)
                    .with(s("BLOOD_MEMORY_FRENZY"), 1);
            case 7 -> t.multiply(s("BLOOD_RITE_DAMAGE_MULTIPLIER"), .85, 1)
                    .add(s("BLOOD_COOLDOWN_BONUS_TICKS"), 30, 0);
            case 8 -> t.multiply(s("BLOOD_RITE_DAMAGE_MULTIPLIER"), .8, 1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.add(s("BLOOD_STAIN_DURATION_BONUS_TICKS"), 100, 0);
            case 1 -> t.add(s("BLOOD_STAIN_SLOW_BONUS"), 1, 0).with(s("BLOOD_STAIN_SLOW_CAP"), 1);
            case 2 -> t.add(s("BLOOD_STAIN_HEAL_INTERVAL_BONUS"), -8, 0)
                    .with(s("BLOOD_STAIN_HEAL_INTERVAL_FLOOR"), 20);
            case 3 -> t.add(s("BLOOD_STAIN_HEAL_BONUS"), .5, 0);
            case 4 -> t.with(s("BLOOD_BLOODBOUND_RESISTANCE"), 1);
            case 5 -> t.with(s("BLOOD_FOOTING_MULTIPLIER"), 1.08);
            case 6 -> t.with(s("BLOOD_CONFLUENCE_CAP_TICKS"), 800);
            case 7 -> t.with(s("BLOOD_SEA_RADIUS_MULTIPLIER"), 1.6)
                    .add(s("BLOOD_STAIN_DURATION_BONUS_TICKS"), 200, 0);
            case 8 -> t.with(s("BLOOD_HEARTPOOL_RADIUS_MULTIPLIER"), .6)
                    .with(s("BLOOD_HEARTPOOL_HEAL_MULTIPLIER"), 2)
                    .with(s("BLOOD_HEARTPOOL_ABSORPTION"), 4)
                    .with(s("BLOOD_HEARTPOOL_ABSORPTION_INTERVAL_TICKS"), 80);
            default -> t;
        };
    }

    private static boolean matches(int profile, int branch, UniqueAbilityDefinition definition) {
        return switch (profile) {
            case 0 -> definition == switch (branch) {
                case 0 -> DeathShadowBloodMasteryAbilities.PLAGUE_PESTILENCE;
                case 1 -> DeathShadowBloodMasteryAbilities.PLAGUE_DEATH_KNELL;
                default -> DeathShadowBloodMasteryAbilities.PLAGUE_OUTBREAK;
            };
            case 1 -> definition == switch (branch) {
                case 0 -> DeathShadowBloodMasteryAbilities.SOULKEEPER_LANTERNS;
                case 1 -> DeathShadowBloodMasteryAbilities.SOULKEEPER_VELOCITY;
                default -> DeathShadowBloodMasteryAbilities.SOULKEEPER_CONCLAVE;
            };
            case 2 -> switch (branch) {
                case 0 -> definition == DeathShadowBloodMasteryAbilities.SOULSTEALER_DEBT
                        || definition == DeathShadowBloodMasteryAbilities.SOULSTEALER_REAP;
                case 1 -> definition == DeathShadowBloodMasteryAbilities.SOULSTEALER_APPROACH;
                default -> definition == DeathShadowBloodMasteryAbilities.SOULSTEALER_REAP;
            };
            case 3 -> definition == switch (branch) {
                case 0 -> DeathShadowBloodMasteryAbilities.TWISTED_FEROCITY;
                case 1 -> DeathShadowBloodMasteryAbilities.TWISTED_CRESCENDO;
                default -> DeathShadowBloodMasteryAbilities.TWISTED_FINALE;
            };
            case 4 -> branch == 0 ? definition == DeathShadowBloodMasteryAbilities.SHADOW_ECHO
                    : definition == DeathShadowBloodMasteryAbilities.SHADOW_DANCE;
            case 5 -> definition == switch (branch) {
                case 0 -> DeathShadowBloodMasteryAbilities.BLOOD_BURST;
                case 1 -> DeathShadowBloodMasteryAbilities.BLOOD_RITES;
                default -> DeathShadowBloodMasteryAbilities.BLOOD_GROUND;
            };
            default -> false;
        };
    }

    private static DeathShadowBloodMasteryTuning mode(DeathShadowBloodMasteryTuning tuning, int bit) {
        return tuning.with(s("MODE"), tuning.integer(s("MODE"), 0) | bit);
    }

    private static DeathShadowBloodMasteryTuning.Setting s(String name) {
        return DeathShadowBloodMasteryTuning.Setting.valueOf(name);
    }
}
