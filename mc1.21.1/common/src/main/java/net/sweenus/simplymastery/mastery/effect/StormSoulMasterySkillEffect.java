package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplymastery.mastery.definition.MasteryCohort;
import net.sweenus.simplyswords.api.ability.StormSoulMasteryTuning;
import net.sweenus.simplyswords.api.ability.StormSoulMasteryAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityEvent;
import net.sweenus.simplyswords.api.ability.UniqueAbilityPhase;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;

import java.util.List;
import java.util.Set;

final class StormSoulMasterySkillEffect implements StaticAbilitySkillEffectType {
    private static final Identifier ID = MasteryCohort.STORM_SOUL.effectId();

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
        if (!effect.parameters().keySet().equals(Set.of("kind"))) {
            errors.add(where + "cohort/storm_soul requires only kind");
        }
        int kind = effect.parameters().getOrDefault("kind", -1);
        if (kind < 0 || kind >= 162) errors.add(where + "kind must be between 0 and 161");
    }

    @Override
    public void tuneStatic(UniqueAbilityDefinition definition,
                     UniqueAbilityTuning.Builder tuning, MasteryProfile.Node node) {
        int kind = parameter(node, "kind", -1);
        if (kind < 0 || kind >= 162 || !matches(kind / 27, definition)) return;
        int profile = kind / 27;
        int branch = kind % 27 / 9;
        int slot = kind % 9;
        if (!applies(profile, branch, slot, definition)) return;
        StormSoulMasteryTuning value = tuning.get(StormSoulMasteryAbilities.TUNING);
        if (definition.cooldownKey().isPresent() && !value.has(s("COOLDOWN_BASE_TICKS"))) {
            int base = tuning.get(StormSoulMasteryAbilities.COOLDOWN_TICKS);
            value = value.with(s("COOLDOWN_BASE_TICKS"), base);
            if (!value.has(s("COOLDOWN_TICKS"))) value = value.with(s("COOLDOWN_TICKS"), base);
        }
        value = switch (profile) {
            case 0 -> stormscale(value, branch, slot);
            case 1 -> ionbound(value, branch, slot);
            case 2 -> soulrender(value, branch, slot);
            case 3 -> soulstalker(value, branch, slot);
            case 4 -> whisperwind(value, branch, slot);
            case 5 -> dreadwhisper(value, branch, slot);
            default -> value;
        };
        tuning.set(StormSoulMasteryAbilities.TUNING, value);
        if (definition.cooldownKey().isPresent()) {
            int base = value.integer(s("COOLDOWN_BASE_TICKS"),
                    tuning.get(StormSoulMasteryAbilities.COOLDOWN_TICKS));
            int cooldown = value.integer(s("COOLDOWN_TICKS"), base);
            double multiplier = value.get(s("COOLDOWN_MULTIPLIER"), 1);
            tuning.set(StormSoulMasteryAbilities.COOLDOWN_TICKS, (int) Math.round(cooldown * multiplier));
        }
    }

    private static StormSoulMasteryTuning stormscale(StormSoulMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("RANGE"), 22);
            case 1 -> t.with(s("RADIUS"), 4);
            case 2 -> t.with(s("ROD_DURATION_TICKS"), 900);
            case 3 -> t.with(s("TETHER_RANGE"), 38);
            case 4 -> t.with(s("TRAVEL_TICKS"), 12);
            case 5 -> mode(t, 1).with(s("PLANT_DAMAGE_MULTIPLIER"), .7);
            case 6 -> mode(t, 2).with(s("REPOSITION_RANGE"), 14)
                    .with(s("REPOSITION_DURATION_COST_TICKS"), 80);
            case 7 -> mode(t, 4).with(s("MOVEMENT_SPEED"), .3).with(s("RADIUS"), 3)
                    .with(s("DAMAGE_MULTIPLIER"), .75).with(s("GROWTH_CAP_LIMIT"), .4);
            case 8 -> mode(t, 8).with(s("RADIUS"), 6).with(s("DAMAGE_MULTIPLIER"), 1.4)
                    .with(s("TETHER_RANGE"), 20).with(s("ROD_DURATION_TICKS"), 600)
                    .with(s("COOLDOWN_MULTIPLIER"), 1.2);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.with(s("GROWTH_PER_HIT"), .015);
            case 2 -> t.with(s("GROWTH_CAP"), 1);
            case 3 -> mode(t, 16).with(s("SECOND_CHARGE_MULTIPLIER"), .5)
                    .with(s("DOUBLE_CHARGE_LOCKOUT_TICKS"), 10);
            case 4 -> mode(t, 32).with(s("CONDUCTIVE_DURATION_TICKS"), 60).with(s("DELAY_TICKS"), 4)
                    .with(s("CONDUCTIVE_TARGET_CAP"), 12);
            case 5 -> mode(t, 64).with(s("BONUS_CAP"), .2).with(s("PER_STACK_BONUS"), .05);
            case 6 -> mode(t, 128).with(s("SURGE_INTERVAL"), 5).with(s("EXTRA_PULSE_MULTIPLIER"), .6);
            case 7 -> mode(t, 256).with(s("INSTANT_PULSE_MULTIPLIER"), .55)
                    .multiply(s("GROWTH_PER_HIT"), 2, .01).with(s("RADIUS_CAP"), 4.5);
            case 8 -> mode(t, 512).with(s("COUNT"), 10).with(s("STORED_PULSE_RADIUS"), 6)
                    .with(s("STORED_PULSE_MULTIPLIER"), .35).with(s("DURATION_TICKS"), 120)
                    .add(s("COOLDOWN_TICKS"), 80, 1000);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("PULL_STRENGTH"), .38);
            case 1 -> t.with(s("SLOW_DURATION_TICKS"), 40).with(s("STATUS_AMPLIFIER"), 0);
            case 2 -> t.with(s("DAMAGE_REDUCTION"), .15);
            case 3 -> mode(t, 1024).with(s("CHAIN_DAMAGE_MULTIPLIER"), .25).with(s("CHAIN_RANGE"), 3)
                    .with(s("CHAIN_TARGET_CAP"), 3);
            case 4 -> mode(t, 2048).with(s("CENTER_RADIUS"), 1.5)
                    .with(s("CENTER_DAMAGE_BONUS"), .2).with(s("IMPACT_LIFT"), .2);
            case 5 -> mode(t, 4096).with(s("REVERSE_STRENGTH"), 1.5)
                    .with(s("REVERSE_WEAKNESS_TICKS"), 60).with(s("REVERSE_LOCKOUT_TICKS"), 60);
            case 6 -> mode(t, 8192).with(s("WARD_TARGET_THRESHOLD"), 6).with(s("ABSORPTION"), 3)
                    .with(s("BUFF_DURATION_TICKS"), 80).with(s("REFUND_TICKS"), 20)
                    .with(s("WARD_LOCKOUT_TICKS"), 100);
            case 7 -> mode(t, 16384).with(s("ROOT_RADIUS"), 2.5).with(s("ROOT_DURATION_TICKS"), 20)
                    .multiply(s("DAMAGE_MULTIPLIER"), .8, 1).with(s("ROOT_TARGET_CAP"), 8);
            case 8 -> mode(t, 32768).with(s("PULL_STRENGTH"), -2.5)
                    .with(s("EDGE_BONUS"), .35);
            default -> t;
        };
    }

    private static StormSoulMasteryTuning ionbound(StormSoulMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("RESERVE_INTERVAL_TICKS"), 145);
            case 1 -> t.with(s("SHIELD_THRESHOLD"), .25);
            case 2 -> t.with(s("SHIELD_DURATION_TICKS"), 50);
            case 3 -> mode(t, 1).with(s("SHIELD_PUSH_STRENGTH"), 2);
            case 4 -> mode(t, 2).with(s("RESERVE_STACK_DURATION_TICKS"), 200)
                    .with(s("RESERVE_STACK_CAP"), 3).with(s("RESERVE_STACK_BONUS"), .1);
            case 5 -> mode(t, 4).with(s("SHIELD_LOW_HEALTH_THRESHOLD"), .25)
                    .with(s("SHIELD_LOCKOUT_TICKS"), 1200);
            case 6 -> mode(t, 8).with(s("RESERVE_REFUND_TICKS"), 40);
            case 7 -> mode(t, 16).with(s("SHIELD_THRESHOLD"), .15).with(s("SHIELD_DURATION_TICKS"), 80)
                    .with(s("SHIELD_CUBE_COST"), 2).with(s("ABILITY_DAMAGE_PENALTY"), .25);
            case 8 -> mode(t, 32).with(s("RESERVE_INTERVAL_TICKS"), 100)
                    .with(s("ABILITY_DAMAGE_PER_CUBE"), .15).with(s("ABILITY_DAMAGE_CUBE_CAP"), .45);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("RANGE"), 14).with(s("WIDTH"), 7).with(s("HEIGHT"), 5);
            case 1 -> t.with(s("CORRIDOR_MATERIALIZE_TICKS"), 5).with(s("CORRIDOR_HOLD_TICKS"), 7)
                    .with(s("CORRIDOR_CLOSE_TICKS"), 7);
            case 2 -> t.with(s("PULL_STRENGTH"), .4);
            case 3 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1);
            case 4 -> mode(t, 2048).with(s("STATUS_DURATION_TICKS"), 60);
            case 5 -> mode(t, 64).with(s("FOCUS_LANE_WIDTH"), 2).with(s("FOCUS_DAMAGE_BONUS"), .25);
            case 6 -> t.with(s("LOCKOUT_TICKS"), 30);
            case 7 -> mode(t, 128).with(s("RANGE"), 8).with(s("WIDTH"), 4).with(s("HEIGHT"), 4)
                    .with(s("CORRIDOR_TARGET_CAP"), 8).with(s("TRAP_MOVEMENT_SPEED"), .1)
                    .multiply(s("DAMAGE_MULTIPLIER"), 2.2, 1);
            case 8 -> mode(t, 256).with(s("BURST_STRENGTH"), 3)
                    .multiply(s("DAMAGE_MULTIPLIER"), 1.5, 1)
                    .with(s("CORRIDOR_TARGET_CAP"), 24).with(s("BEAM_WIDTH_MULTIPLIER"), .75);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("WIDTH"), 1.3);
            case 1 -> t.with(s("RANGE"), 14);
            case 2 -> t.with(s("INTERVAL_TICKS"), 4);
            case 3 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 4 -> t.with(s("STATUS_DURATION_TICKS"), 130);
            case 5 -> t.with(s("MOVEMENT_SPEED"), .3);
            case 6 -> t.with(s("BEAM_DURATION_TICKS"), 72).multiply(s("DAMAGE_MULTIPLIER"), 1.2, 1);
            case 7 -> mode(t, 512).with(s("WIDTH"), 2)
                    .multiply(s("DAMAGE_MULTIPLIER"), .7, 1).with(s("BEAM_TARGET_CAP"), 24)
                    .with(s("STATUS_DURATION_TICKS"), 60);
            case 8 -> mode(t, 1024).with(s("WIDTH"), .7).with(s("BEAM_DURATION_TICKS"), 40)
                    .multiply(s("DAMAGE_MULTIPLIER"), 2.25, 1).with(s("BEAM_TARGET_CAP"), 3)
                    .with(s("ARMOR_IGNORE"), 6).with(s("MOVEMENT_SPEED"), 0);
            default -> t;
        };
    }

    private static StormSoulMasteryTuning soulrender(StormSoulMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("CHANCE_BONUS"), 5);
            case 1 -> t.with(s("MARK_DURATION_TICKS"), 600);
            case 2 -> t.with(s("STACK_CAP"), 10);
            case 3 -> t.with(s("MELEE_BONUS_PER_STACK"), .03).with(s("MELEE_BONUS_CAP"), .18);
            case 4 -> t.with(s("FRESH_INK_STACKS"), 2).with(s("FRESH_INK_LOCKOUT_TICKS"), 80);
            case 5 -> t.with(s("ECHO_CHANCE"), 25).with(s("ECHO_RANGE"), 4)
                    .with(s("ECHO_TARGET_CAP"), 1).with(s("ECHO_LOCKOUT_TICKS"), 20)
                    .with(s("ECHO_DURATION_TICKS"), 300);
            case 6 -> t.with(s("REAP_STACK_BONUS"), .06).with(s("REAP_STACK_BONUS_CAP"), .36);
            case 7 -> mode(t, 4).with(s("REPEAT_CHANCE_PENALTY"), 20);
            case 8 -> t.with(s("STACK_CAP"), 5).with(s("REAP_STACK_BONUS"), .15)
                    .with(s("REAP_STACK_BONUS_CAP"), .75);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("RADIUS"), 12);
            case 1 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 2 -> t.with(s("REAP_SPEED_THRESHOLD"), 3).with(s("REAP_SPEED_DURATION_TICKS"), 60);
            case 3 -> t.with(s("HEAL_RATIO_BONUS"), .5).with(s("HEAL_CAP"), 9);
            case 4 -> t.with(s("REACH_BONUS_RANGE"), 4).with(s("REACH_PULL_STRENGTH"), 1.5)
                    .with(s("REACH_TARGET_CAP"), 8);
            case 5 -> t.with(s("SHARED_END_DAMAGE_MULTIPLIER"), .35).with(s("SHARED_END_RANGE"), 5)
                    .with(s("SHARED_END_TARGET_CAP"), 6);
            case 6 -> t.with(s("REAP_HASTE_THRESHOLD"), 6).with(s("REAP_HASTE_DURATION_TICKS"), 100)
                    .with(s("REAP_HASTE_AMPLIFIER"), 1);
            case 7 -> t.with(s("RADIUS"), 16).with(s("REAP_TARGET_CAP"), 32)
                    .multiply(s("DAMAGE_MULTIPLIER"), .7, 1).multiply(s("HEAL_MULTIPLIER"), .5, 1);
            case 8 -> t.with(s("RADIUS"), 10).with(s("REAP_TARGET_CAP"), 5)
                    .multiply(s("DAMAGE_MULTIPLIER"), 1.5, 1).multiply(s("HEAL_MULTIPLIER"), 1.25, 1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("SHEATH_ABSORPTION"), 2).with(s("SHEATH_DURATION_TICKS"), 80);
            case 1 -> t.with(s("PATIENCE_RANGE"), 6).with(s("KNOCKBACK_RESISTANCE_BONUS"), .1);
            case 2 -> t.with(s("BORROWED_TIME_TICKS"), 40).with(s("BORROWED_TIME_LOCKOUT_TICKS"), 40);
            case 3 -> t.with(s("COLD_GRIP_SLOW_TICKS"), 40).with(s("COLD_GRIP_LOCKOUT_TICKS"), 40)
                    .with(s("COLD_GRIP_TARGET_CAP"), 8);
            case 4 -> t.with(s("GRAVE_RESERVE_RATIO"), .1).with(s("GRAVE_RESERVE_CAP"), 6)
                    .with(s("GRAVE_RESERVE_HEALTH_THRESHOLD"), .35)
                    .with(s("GRAVE_RESERVE_DURATION_TICKS"), 100);
            case 5 -> t.with(s("QUIETUS_HEALTH_THRESHOLD"), .2).with(s("QUIETUS_ABSORPTION"), 1)
                    .with(s("QUIETUS_ABSORPTION_CAP"), 6);
            case 6 -> t.with(s("UNBROKEN_MARK_THRESHOLD"), 5).with(s("UNBROKEN_RANGE"), 10)
                    .with(s("UNBROKEN_TARGET_CAP"), 5).with(s("UNBROKEN_RESIST_TICKS"), 40)
                    .with(s("UNBROKEN_LOCKOUT_TICKS"), 1200);
            case 7 -> t.multiply(s("DAMAGE_MULTIPLIER"), .75, 1).with(s("SHELTER_ABSORPTION_RATIO"), .4)
                    .with(s("SHELTER_ABSORPTION_CAP"), 8).with(s("SHELTER_DURATION_TICKS"), 120);
            case 8 -> t.multiply(s("HEAL_MULTIPLIER"), 0, 1).with(s("TITHE_KILL_BONUS"), .1)
                    .with(s("TITHE_BONUS_CAP"), .5).with(s("TITHE_DURATION_TICKS"), 100);
            default -> t;
        };
    }

    private static StormSoulMasteryTuning soulstalker(StormSoulMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("CHANCE"), 30);
            case 1 -> t.with(s("TENDRIL_RANGE"), 10);
            case 2 -> t.with(s("TENDRIL_LOCKOUT_TICKS"), 50);
            case 3 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1).with(s("TENDRIL_SLOW_TICKS"), 40);
            case 4 -> t.with(s("GLOAM_DAMAGE_BONUS"), .25);
            case 5 -> t.with(s("SEEKING_ROOT_RANGE"), 4);
            case 6 -> t.with(s("SNARE_SLOW_TICKS"), 20).with(s("SNARE_SLOW_AMPLIFIER"), 3)
                    .with(s("SNARE_LOCKOUT_TICKS"), 80);
            case 7 -> t.with(s("TENDRIL_COUNT"), 3).multiply(s("DAMAGE_MULTIPLIER"), .65, 1)
                    .with(s("TENDRIL_LOCKOUT_TICKS"), 90);
            case 8 -> mode(t, 16).with(s("TENDRIL_RANGE"), 10)
                    .multiply(s("DAMAGE_MULTIPLIER"), 1.75, 1).with(s("TENDRIL_REFUND_TICKS"), 20)
                    .with(s("INTERVAL_TICKS"), 30);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("STRIDE_DURATION_TICKS"), 900);
            case 1 -> t.multiply(s("MOVEMENT_SPEED"), 1.1, net.sweenus.simplyswords.config.Config.uniqueEffects.soulstalker.movementSpeed)
                    .multiply(s("CLIMB_SPEED"), 1.1, net.sweenus.simplyswords.config.Config.uniqueEffects.soulstalker.climbSpeed);
            case 2 -> t.with(s("FOOTFALL_RADIUS"), 1).multiply(s("FOOTFALL_DAMAGE_MULTIPLIER"), 1.1, 1);
            case 3 -> t.with(s("TRAIL_STAIN_DURATION_TICKS"), 320);
            case 4 -> t.with(s("TRAIL_SLOW_AMPLIFIER"), 1).with(s("TRAIL_SLOW_DURATION_TICKS"), 60);
            case 5 -> t.with(s("MOMENTUM_DISTANCE"), 8).with(s("MOMENTUM_BONUS"), .15);
            case 6 -> t.with(s("COOLDOWN_TICKS"), 1020);
            case 7 -> t.add(s("STRIDE_DURATION_TICKS"), 400, 800)
                    .multiply(s("TRAIL_STAIN_WIDTH"), 1.5, net.sweenus.simplyswords.config.Config.uniqueEffects.soulstalker.stainTrailWidth)
                    .multiply(s("COOLDOWN_MULTIPLIER"), 1.2, 1)
                    .multiply(s("FOOTFALL_DAMAGE_MULTIPLIER"), .75, 1);
            case 8 -> t.with(s("RIFT_RANGE"), 12).with(s("RIFT_STAIN_RADIUS"), 2)
                    .multiply(s("STRIDE_DURATION_TICKS"), .75, 800)
                    .with(s("RIFT_LOCKOUT_TICKS"), 60);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("CLEAVE_RANGE"), 18);
            case 1 -> t.with(s("CLEAVE_FINAL_WIDTH"), 3.8);
            case 2 -> t.multiply(s("CLEAVE_DAMAGE_MULTIPLIER"), 1.12, 1);
            case 3 -> t.multiply(s("CLEAVE_COOLDOWN_MULTIPLIER"), .9, 1);
            case 4 -> t.multiply(s("LEAP_IMPACT_DAMAGE_MULTIPLIER"), 1.2, 1)
                    .with(s("LEAP_IMPACT_RADIUS"), 3);
            case 5 -> t.multiply(s("LEAP_IMPACT_KNOCKBACK"), 1.25, net.sweenus.simplyswords.config.Config.uniqueEffects.soulstalker.leapImpactKnockback)
                    .with(s("LEAP_IMPACT_LIFT"), .28);
            case 6 -> t.with(s("CLEAVE_HIT_BONUS"), .04).with(s("CLEAVE_HIT_STACK_CAP"), 5)
                    .with(s("CLEAVE_HIT_BONUS_CAP"), .2);
            case 7 -> t.with(s("CLEAVE_RANGE"), 24).with(s("CLEAVE_FINAL_WIDTH"), 5)
                    .with(s("CLEAVE_INITIAL_WIDTH"), 5)
                    .multiply(s("CLEAVE_DAMAGE_MULTIPLIER"), .8, 1).with(s("CLEAVE_TARGET_CAP"), 16)
                    .multiply(s("CLEAVE_COOLDOWN_MULTIPLIER"), 2, 1);
            case 8 -> t.with(s("LEAP_CHARGE_THRESHOLD"), .95)
                    .with(s("LEAP_CHARGED_DAMAGE_MULTIPLIER"), 2).with(s("LEAP_CHARGED_RADIUS"), 4)
                    .with(s("LEAP_STAIN_RADIUS"), 2).with(s("LEAP_STAIN_DURATION_TICKS"), 240)
                    .with(s("METEOR_CLEAVE_PENALTY"), .7);
            default -> t;
        };
    }

    private static StormSoulMasteryTuning whisperwind(StormSoulMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.multiply(s("DASH_SPEED"), 1.1, 3);
            case 1 -> t.with(s("COOLDOWN_TICKS"), 155);
            case 2 -> t.with(s("DASH_TICKS"), 18).with(s("DASH_ABSORPTION_TICKS"), 120);
            case 3 -> t.with(s("WAKE_DURATION_TICKS"), 30).with(s("WAKE_SPEED_AMPLIFIER"), 1)
                    .with(s("WAKE_KNOCKBACK_RESISTANCE"), .2);
            case 4 -> t.with(s("PASSING_CUT_MULTIPLIER"), .2).with(s("PASSING_CUT_TARGET_CAP"), 8);
            case 5 -> t.with(s("RETURN_THRESHOLD"), 3).with(s("RETURN_REFUND_TICKS"), 25);
            case 6 -> t.with(s("DASH_EXTENSION_PER_TARGET"), 1).with(s("DASH_EXTENSION_CAP"), 4);
            case 7 -> t.with(s("DASH_RANGE_MULTIPLIER"), 1.5).with(s("STRIKE_TARGET_CAP"), 16)
                    .multiply(s("STRIKE_DAMAGE_MULTIPLIER"), .75, 1)
                    .multiply(s("COOLDOWN_MULTIPLIER"), 1.2, 1);
            case 8 -> t.with(s("STRIKE_NEAREST_ONLY"), 1).with(s("STRIKE_COUNT"), 2)
                    .multiply(s("STRIKE_DAMAGE_MULTIPLIER"), .8, 1);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.multiply(s("STRIKE_DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.with(s("DELAY_TICKS"), 28).multiply(s("STRIKE_DAMAGE_MULTIPLIER"), 1.12, 1);
            case 2 -> t.with(s("BOUQUET_PER_TARGET_BONUS"), .2).with(s("BOUQUET_TARGET_CAP"), 8);
            case 3 -> t.with(s("WEAKNESS_DURATION_TICKS"), 60);
            case 4 -> t.with(s("FLOWERING_MULTIPLIER"), .35).with(s("FLOWERING_RADIUS"), 3)
                    .with(s("FLOWERING_TARGET_CAP"), 3);
            case 5 -> t.with(s("ARMOR_IGNORE_RATIO"), .15).with(s("ARMOR_IGNORE_CAP"), 4);
            case 6 -> t.with(s("SOLO_DAMAGE_BONUS"), .3).with(s("CROWD_THRESHOLD"), 5)
                    .with(s("CROWD_DAMAGE_BONUS"), .15);
            case 7 -> t.with(s("STRIKE_COUNT"), 3).multiply(s("STRIKE_DAMAGE_MULTIPLIER"), .45, 1)
                    .with(s("DELAY_TICKS"), 36).with(s("STRIKE_TARGET_CAP"), 8);
            case 8 -> t.with(s("STRIKE_NEAREST_ONLY"), 1)
                    .multiply(s("STRIKE_DAMAGE_MULTIPLIER"), 2.25, 1)
                    .with(s("DELAY_TICKS"), 16).add(s("COOLDOWN_TICKS"), 30, 175);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("CHANCE"), 20);
            case 1 -> t.with(s("DASH_ABSORPTION"), 4);
            case 2 -> t.with(s("RHYTHM_CHANCE_PER_FAILURE"), 3).with(s("RHYTHM_CHANCE_CAP"), 15)
                    .with(s("RHYTHM_WINDOW_TICKS"), 100);
            case 3 -> t.with(s("READY_SPEED_AMPLIFIER"), 0).with(s("FALL_DAMAGE_REDUCTION"), .1);
            case 4 -> t.with(s("WINDBREAK_TICKS"), 20).with(s("WINDBREAK_PROJECTILE_REDUCTION"), .3);
            case 5 -> t.with(s("REPRISE_WINDOW_TICKS"), 80);
            case 6 -> t.with(s("TEMPO_WINDOW_TICKS"), 40).with(s("TEMPO_DAMAGE_BONUS"), .2)
                    .with(s("TEMPO_HASTE_TICKS"), 60);
            case 7 -> t.with(s("REFRESH_REFUND_TICKS"), 60).with(s("REFRESH_INTERVAL_TICKS"), 20)
                    .with(s("REFRESH_SPEED_TICKS"), 40).with(s("REFRESH_SPEED_AMPLIFIER"), 1);
            case 8 -> t.with(s("STILL_WIND_THRESHOLD"), 3).with(s("STILL_WIND_WINDOW_TICKS"), 80)
                    .multiply(s("STILL_WIND_MULTIPLIER"), 1.75, 1);
            default -> t;
        };
    }

    private static StormSoulMasteryTuning dreadwhisper(StormSoulMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("REND_WIDTH"), 5);
            case 1 -> t.multiply(s("REND_DAMAGE_MULTIPLIER"), 1.1, 1);
            case 2 -> t.multiply(s("REND_SPEED"), 1.1,
                    net.sweenus.simplyswords.config.Config.uniqueEffects.dreadwhisper.dashSpeed);
            case 3 -> t.with(s("REND_RANGE"), 23);
            case 4 -> t.with(s("LEECH_RATIO"), .4).with(s("LEECH_CAP"), 18);
            case 5 -> t.with(s("COLLISION_MULTIPLIER"), .4).with(s("COLLISION_RADIUS"), 3)
                    .with(s("COLLISION_TARGET_CAP"), 8);
            case 6 -> t.with(s("MOMENTUM_SPEED_BONUS"), .04).with(s("MOMENTUM_SPEED_CAP"), .2)
                    .with(s("MOMENTUM_DAMAGE_BONUS"), .03).with(s("MOMENTUM_DAMAGE_CAP"), .15);
            case 7 -> t.multiply(s("REND_WIDTH"), 2, 4.5).with(s("REND_TARGET_CAP"), 32)
                    .multiply(s("REND_DAMAGE_MULTIPLIER"), .7, 1).with(s("LEECH_RATIO"), .2)
                    .multiply(s("COOLDOWN_MULTIPLIER"), 1.25, 1);
            case 8 -> t.with(s("REND_WIDTH"), 2).with(s("REND_STOP_ON_HIT"), 1)
                    .multiply(s("REND_DAMAGE_MULTIPLIER"), 2.2, 1).with(s("LEECH_RATIO"), .6);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("WOUND_DURATION_TICKS"), 260);
            case 1 -> t.multiply(s("WOUND_DAMAGE_MULTIPLIER"), 1.12, 1);
            case 2 -> t.with(s("WOUND_WEAKNESS_TICKS"), 260);
            case 3 -> t.with(s("WOUND_AGE_INTERVAL_TICKS"), 20).with(s("WOUND_AGE_BONUS"), .03)
                    .with(s("WOUND_AGE_CAP"), .3);
            case 4 -> t.with(s("SPLINTER_MULTIPLIER"), .35).with(s("SPLINTER_RANGE"), 3);
            case 5 -> t.with(s("REOPEN_CHANCE"), 25).with(s("REOPEN_DURATION_TICKS"), 80)
                    .with(s("REOPEN_LOCKOUT_TICKS"), 120);
            case 6 -> t.with(s("MORTAL_THRESHOLD"), .25).with(s("MORTAL_BONUS"), .3)
                    .with(s("MORTAL_BOSS_BONUS"), .15);
            case 7 -> t.with(s("SPREAD_COUNT"), 2).with(s("SPREAD_RANGE"), 4)
                    .with(s("SPREAD_DURATION_TICKS"), 120)
                    .multiply(s("WOUND_DAMAGE_MULTIPLIER"), .7, 1);
            case 8 -> t.with(s("WOUND_DURATION_TICKS"), 80)
                    .multiply(s("WOUND_DAMAGE_MULTIPLIER"), 2.5, 1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("STAIN_DURATION_TICKS"), 300);
            case 1 -> t.with(s("TRAIL_SLOW_LEVEL"), 1).with(s("TRAIL_SLOW_TICKS"), 60);
            case 2 -> t.with(s("VEIL_DURATION_TICKS"), 8);
            case 3 -> t.with(s("GLOAM_DAMAGE_RIDER"), .15);
            case 4 -> t.with(s("RECALL_RANGE"), 6).with(s("RECALL_STRENGTH"), 1.5)
                    .with(s("RECALL_TARGET_CAP"), 8);
            case 5 -> t.with(s("SHELTER_ABSORPTION_PER_HIT"), 1).with(s("SHELTER_ABSORPTION_LIMIT"), 6)
                    .with(s("SHELTER_BUFF_TICKS"), 80);
            case 6 -> t.with(s("FOOTPRINT_TICKS"), 60).with(s("FOOTPRINT_PROJECTILE_REDUCTION"), .25);
            case 7 -> t.with(s("LIVING_SHADOW_TICKS"), 100).with(s("LIVING_SHADOW_INTERVAL"), 20)
                    .with(s("LIVING_SHADOW_MULTIPLIER"), .2).with(s("LIVING_SHADOW_TARGET_CAP"), 12);
            case 8 -> mode(t, 1).with(s("REND_RANGE"), 30)
                    .with(s("VOID_COLLAPSE_MULTIPLIER"), 1.6).with(s("VOID_COLLAPSE_RADIUS"), 12)
                    .with(s("VOID_COLLAPSE_TARGET_CAP"), 12).with(s("LEECH_RATIO"), 0);
            default -> t;
        };
    }

    private static boolean matches(int profile, UniqueAbilityDefinition definition) {
        return switch (profile) {
            case 0 -> definition == StormSoulMasteryAbilities.STORMSCALE_ROD;
            case 1 -> definition == StormSoulMasteryAbilities.IONBOUND_CRUSHER
                    || definition == StormSoulMasteryAbilities.IONBOUND_BEAM
                    || definition == StormSoulMasteryAbilities.IONBOUND_SHIELD;
            case 2 -> definition == StormSoulMasteryAbilities.SOULRENDER_MARK
                    || definition == StormSoulMasteryAbilities.SOULRENDER_REAP
                    || definition == StormSoulMasteryAbilities.SOULRENDER_GRAVE;
            case 3 -> definition == StormSoulMasteryAbilities.SOULSTALKER_TENDRIL
                    || definition == StormSoulMasteryAbilities.SOULSTALKER_STRIDE;
            case 4 -> definition == StormSoulMasteryAbilities.WHISPERWIND_DASH
                    || definition == StormSoulMasteryAbilities.WHISPERWIND_RESET
                    || definition == StormSoulMasteryAbilities.WHISPERWIND_STILL_WIND;
            case 5 -> definition == StormSoulMasteryAbilities.DREADWHISPER_REAVE
                    || definition == StormSoulMasteryAbilities.DREADWHISPER_WOUND;
            default -> false;
        };
    }

    private static boolean applies(int profile, int branch, int slot, UniqueAbilityDefinition definition) {
        return switch (profile) {
            case 0 -> true;
            case 1 -> branch == 0
                    || branch == 1 && definition == StormSoulMasteryAbilities.IONBOUND_CRUSHER
                    || branch == 2 && definition == StormSoulMasteryAbilities.IONBOUND_BEAM;
            case 2 -> switch (branch) {
                case 0 -> definition == StormSoulMasteryAbilities.SOULRENDER_MARK
                        || definition == StormSoulMasteryAbilities.SOULRENDER_REAP && reapReaching(slot);
                case 1 -> definition == StormSoulMasteryAbilities.SOULRENDER_REAP;
                default -> definition == StormSoulMasteryAbilities.SOULRENDER_REAP
                        || definition == StormSoulMasteryAbilities.SOULRENDER_GRAVE;
            };
            case 3 -> definition == StormSoulMasteryAbilities.SOULSTALKER_TENDRIL ? branch == 0 : branch > 0;
            case 4 -> definition == StormSoulMasteryAbilities.WHISPERWIND_DASH
                    || definition == StormSoulMasteryAbilities.WHISPERWIND_RESET && branch == 2
                    || definition == StormSoulMasteryAbilities.WHISPERWIND_STILL_WIND
                    && (branch == 1 || branch == 2 || branch == 0 && slot >= 7);
            case 5 -> definition == StormSoulMasteryAbilities.DREADWHISPER_REAVE
                    || branch == 1 || branch == 2 && slot == 3;
            default -> false;
        };
    }

    // Rendmarks nodes whose bonus is spent by the reaping rather than the mark.
    private static boolean reapReaching(int slot) {
        return slot == 6 || slot == 8;
    }

    @Override
    public void onAbilityEvent(UniqueAbilityEvent event, MasteryProfile.Node node) {
        String key = "execution/" + event.execution().definition().id().getPath();
        long tick = event.execution().context().world().getTime();
        if (event.phase() == UniqueAbilityPhase.START) {
            StormSoulMasteryRuntime.set(event.execution().context().actor(), event.execution().context().stack(), key, 1, tick + 1200, tick);
        } else if (event.phase() == UniqueAbilityPhase.FINISH || event.phase() == UniqueAbilityPhase.CANCEL) {
            StormSoulMasteryRuntime.clear(event.execution().context().actor(), event.execution().context().stack(), key);
        }
    }

    private static StormSoulMasteryTuning mode(StormSoulMasteryTuning tuning, int bit) {
        return tuning.with(s("MODE"), tuning.integer(s("MODE"), 0) | bit);
    }

    private static StormSoulMasteryTuning.Setting s(String name) {
        return StormSoulMasteryTuning.Setting.valueOf(name);
    }

    private static int parameter(MasteryProfile.Node node, String key, int fallback) {
        return node.effect().parameters().getOrDefault(key, fallback);
    }
}
