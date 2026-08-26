package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.SimplyMastery;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.Phase2AbilityTuning;
import net.sweenus.simplyswords.api.ability.Phase2UniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityContext;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityEvent;
import net.sweenus.simplyswords.api.ability.UniqueAbilityPhase;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;

import java.util.List;

final class Phase2MasterySkillEffect implements AbilitySkillEffectType {
    private static final Identifier ID = Identifier.of(SimplyMastery.MOD_ID, "phase2_mastery");

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
        if (!effect.parameters().keySet().equals(java.util.Set.of("kind"))) {
            errors.add(where + "phase2_mastery requires only kind");
        }
        int kind = effect.parameters().getOrDefault("kind", -1);
        if (kind < 0 || kind >= 162) errors.add(where + "kind must be between 0 and 161");
    }

    @Override
    public void tune(UniqueAbilityContext context, UniqueAbilityDefinition definition,
                     UniqueAbilityTuning.Builder tuning, MasteryProfile.Node node) {
        int kind = parameter(node, "kind", -1);
        if (kind < 0 || kind >= 162 || !matches(kind / 27, definition)) return;
        int branch = kind % 27 / 9;
        int slot = kind % 9;
        Phase2AbilityTuning value = tuning.get(Phase2UniqueAbilities.TUNING);
        value = switch (kind / 27) {
            case 0 -> watcher(value, branch, slot);
            case 1 -> devourer(value, branch, slot);
            case 2 -> wickpiercer(value, branch, slot);
            case 3 -> gloampiercer(value, branch, slot);
            case 4 -> wraithfang(value, branch, slot);
            case 5 -> wraithmaw(value, branch, slot);
            default -> value;
        };
        tuning.set(Phase2UniqueAbilities.TUNING, value);
        if (definition.cooldownKey().isPresent()) {
            int cooldown = value.integer(Phase2AbilityTuning.Setting.COOLDOWN_TICKS,
                    tuning.get(Phase2UniqueAbilities.COOLDOWN_TICKS));
            tuning.set(Phase2UniqueAbilities.COOLDOWN_TICKS, cooldown);
        }
    }

    private static Phase2AbilityTuning watcher(Phase2AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("STACK_DURATION_TICKS"), 260);
            case 1 -> t.with(s("STACK_CAP"), 6);
            case 2 -> t.with(s("MARKED_TARGET_CAP"), 7);
            case 3 -> t.with(s("MELEE_BONUS_PER_STACK"), .03).with(s("MELEE_BONUS_CAP"), .18);
            case 4 -> mode(t, 1).with(s("THRESHOLD"), 4).with(s("RANGE"), 4).with(s("LOCKOUT_TICKS"), 40);
            case 5 -> mode(t, 2).with(s("THRESHOLD"), 30).with(s("LOCKOUT_TICKS"), 80);
            case 6 -> mode(t, 4).with(s("COOLDOWN_REFUND_TICKS"), 30);
            case 7 -> mode(t, 8).with(s("STACK_CAP"), 3).with(s("MARKED_TARGET_CAP"), 12)
                    .with(s("SECONDARY_TARGET_CAP"), 3).multiply(s("COOLDOWN_TICKS"), 1.25, 180);
            case 8 -> mode(t, 16).with(s("STACK_CAP"), 8).with(s("MARKED_TARGET_CAP"), 1)
                    .with(s("FINAL_PER_STACK_MULTIPLIER"), .2);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("DURATION_TICKS"), 54);
            case 1 -> t.with(s("SWOOPS_PER_STACK"), 1).with(s("TARGET_CAP"), 17);
            case 2 -> t.multiply(s("SWOOP_DAMAGE_MULTIPLIER"), 1.12, 1);
            case 3 -> t.with(s("STATUS_DURATION_TICKS"), 90);
            case 4 -> t.with(s("BONUS_PER_TRIGGER"), .03).with(s("BONUS_CAP"), .36);
            case 5 -> mode(t, 32).with(s("RANGE"), 12).with(s("PULL_STRENGTH"), 2);
            case 6 -> t.multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.15, 1)
                    .with(s("MISSING_HEALTH_BONUS_CAP"), .9);
            case 7 -> mode(t, 64).with(s("DURATION_TICKS"), 100).with(s("SPEAR_COUNT"), 20)
                    .with(s("SWOOP_DAMAGE_MULTIPLIER"), .65).multiply(s("FINAL_DAMAGE_MULTIPLIER"), .65, 1)
                    .add(s("COOLDOWN_TICKS"), 60, 180);
            case 8 -> mode(t, 128).with(s("DURATION_TICKS"), 20).with(s("SPEAR_COUNT"), 0)
                    .multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.9, 1).with(s("MISSING_HEALTH_BONUS_CAP"), .35)
                    .with(s("STATUS_DURATION_TICKS"), 0);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("EXECUTE_THRESHOLD"), .28);
            case 1 -> t.multiply(s("ABSORPTION_MULTIPLIER"), 1.15, 1);
            case 2 -> mode(t, 256).with(s("REVIVE_ABSORPTION"), 4).with(s("STATUS_DURATION_TICKS"), 100);
            case 3 -> mode(t, 512).with(s("STATUS_DURATION_TICKS"), 60);
            case 4 -> mode(t, 1024).with(s("THRESHOLD"), 4).with(s("DAMAGE_REDUCTION"), .15);
            case 5 -> mode(t, 2048).with(s("THRESHOLD"), 30).with(s("RANGE"), 12).with(s("LOCKOUT_TICKS"), 200);
            case 6 -> mode(t, 4096).with(s("BONUS_PER_TRIGGER"), .05).with(s("BONUS_CAP"), .4)
                    .with(s("DURATION_TICKS"), 120);
            case 7 -> mode(t, 8192).with(s("EXECUTE_THRESHOLD"), 0).with(s("ABSORPTION_MULTIPLIER"), 1.5)
                    .with(s("STATUS_DURATION_TICKS"), 60);
            case 8 -> mode(t, 16384).with(s("EXECUTE_THRESHOLD"), .35).with(s("THRESHOLD"), 6)
                    .with(s("ABSORPTION_MULTIPLIER"), 0).with(s("DAMAGE_MULTIPLIER"), .7)
                    .add(s("COOLDOWN_TICKS"), 100, 180);
            default -> t;
        };
    }

    private static Phase2AbilityTuning devourer(Phase2AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("RADIUS"), 4.3).with(s("SCAN_RADIUS"), 8);
            case 1 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 2 -> t.with(s("PULL_STRENGTH"), .34);
            case 3 -> t.with(s("DURATION_TICKS"), 900);
            case 4 -> t.with(s("TARGET_CAP"), 8).with(s("CANDIDATE_CAP"), 32);
            case 5 -> t.with(s("ACCELERATE_THRESHOLD_TICKS"), 100).with(s("PULSE_INTERVAL_TICKS"), 16);
            case 6 -> mode(t, 1).with(s("IMPACT_DAMAGE_MULTIPLIER"), 1.25).with(s("IMPACT_RADIUS"), 4.5)
                    .with(s("IMPACT_TARGET_CAP"), 12);
            case 7 -> mode(t, 2).with(s("RANGE"), 10).with(s("MOVEMENT_SPEED"), .25)
                    .with(s("DURATION_TICKS"), 600).with(s("RADIUS"), 3)
                    .multiply(s("DAMAGE_MULTIPLIER"), .8, 1);
            case 8 -> mode(t, 4).with(s("RADIUS"), 6).with(s("TARGET_CAP"), 12)
                    .with(s("PULL_STRENGTH"), .42).with(s("PULSE_INTERVAL_TICKS"), 30)
                    .multiply(s("COOLDOWN_TICKS"), 1.25, 1200);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("LOOSE_TARGET_CAP"), 96).with(s("LAUNCH_SPEED"), .42);
            case 1 -> t.with(s("TENDRIL_CAP"), 5);
            case 2 -> t.with(s("STAIN_TARGET_CAP"), 16).with(s("STAIN_RADIUS"), 1.6);
            case 3 -> t.with(s("STAIN_DURATION_TICKS"), 80).with(s("STAIN_AMPLIFIER"), 1)
                    .with(s("STATUS_DURATION_TICKS"), 40);
            case 4 -> t.with(s("BONUS_PER_TRIGGER"), .03).with(s("BONUS_CAP"), .24);
            case 5 -> mode(t, 8).with(s("DURATION_BONUS_TICKS"), 20).with(s("EXTRA_DURATION_CAP"), 100);
            case 6 -> mode(t, 16).with(s("RUPTURE_THRESHOLD_TICKS"), 160)
                    .with(s("SECONDARY_DAMAGE_MULTIPLIER"), .75).with(s("SECONDARY_TARGET_CAP"), 8);
            case 7 -> mode(t, 32).with(s("LOOSE_TARGET_CAP"), 160).with(s("TARGET_CAP"), 3)
                    .with(s("LAUNCH_SPEED"), .84)
                    .multiply(s("DAMAGE_MULTIPLIER"), .7, 1);
            case 8 -> mode(t, 64).multiply(s("DAMAGE_MULTIPLIER"), 1.75, 1)
                    .with(s("COOLDOWN_REFUND_TICKS"), 20).with(s("COOLDOWN_REFUND_CAP_TICKS"), 160);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("STATUS_DURATION_TICKS"), 40).with(s("STATUS_AMPLIFIER"), 0);
            case 1 -> t.with(s("REPRISAL_RADIUS"), 4);
            case 2 -> t.with(s("REPRISAL_PULL"), .38);
            case 3 -> t.multiply(s("REPRISAL_DAMAGE_MULTIPLIER"), 1.12, 1);
            case 4 -> mode(t, 128).with(s("BONUS_PER_TRIGGER"), .25);
            case 5 -> mode(t, 256).with(s("REVIVE_ABSORPTION"), 3).with(s("STATUS_DURATION_TICKS"), 60);
            case 6 -> t.with(s("REPRISAL_TARGET_CAP"), 9).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .7);
            case 7 -> mode(t, 512).with(s("REPRISAL_DAMAGE_MULTIPLIER"), .5)
                    .with(s("DAMAGE_REDUCTION"), .5).with(s("REPRISAL_TARGET_CAP"), 9)
                    .with(s("PULL_STRENGTH"), 2).with(s("LOCKOUT_TICKS"), 80);
            case 8 -> mode(t, 1024).with(s("REPRISAL_DAMAGE_MULTIPLIER"), 2.25)
                    .with(s("REPRISAL_TARGET_CAP"), 1).with(s("RANGE"), 30).with(s("LOCKOUT_TICKS"), 40);
            default -> t;
        };
    }

    private static Phase2AbilityTuning wickpiercer(Phase2AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.multiply(s("PROJECTILE_DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.with(s("PROJECTILE_SPEED"), 1.8);
            case 2 -> t.with(s("PROJECTILE_LIFETIME"), 100);
            case 3 -> t.with(s("FIRE_TICKS"), 60);
            case 4 -> t.with(s("COOLDOWN_TICKS"), 26);
            case 5 -> t.with(s("PIERCE_COUNT"), 1).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .7);
            case 6 -> t.with(s("IMPACT_DAMAGE_MULTIPLIER"), .35).with(s("IMPACT_RADIUS"), 2.5)
                    .with(s("IMPACT_TARGET_CAP"), 5).with(s("LOCKOUT_TICKS"), 20);
            case 7 -> mode(t, 1).with(s("PROJECTILE_SPEED"), 2.2).with(s("PROJECTILE_DAMAGE_MULTIPLIER"), 1.8)
                    .with(s("LOYALTY"), 0).with(s("DURATION_TICKS"), 60);
            case 8 -> mode(t, 2).with(s("DURATION_TICKS"), 60).with(s("INTERVAL_TICKS"), 20)
                    .with(s("SECONDARY_DAMAGE_MULTIPLIER"), .45).with(s("PROJECTILE_DAMAGE_MULTIPLIER"), .6)
                    .with(s("COOLDOWN_TICKS"), 100);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("STACK_DURATION_TICKS"), 110);
            case 1 -> t.with(s("STACK_CAP"), 5);
            case 2 -> t.multiply(s("MELEE_BONUS_PER_STACK"), 1.1, 1);
            case 3 -> mode(t, 4).with(s("LOCKOUT_TICKS"), 20);
            case 4 -> mode(t, 8).with(s("FIRE_TICKS"), 40).with(s("BONUS_PER_TRIGGER"), .2);
            case 5 -> mode(t, 16).with(s("DURATION_TICKS"), 40).with(s("BONUS_PER_TRIGGER"), .05)
                    .with(s("BONUS_CAP"), .2);
            case 6 -> mode(t, 32).with(s("THRESHOLD"), 2).with(s("LOCKOUT_TICKS"), 40);
            case 7 -> mode(t, 64).with(s("IMPACT_DAMAGE_MULTIPLIER"), .45).with(s("IMPACT_RADIUS"), 3)
                    .with(s("IMPACT_TARGET_CAP"), 6);
            case 8 -> mode(t, 128).with(s("STACK_CAP"), 3).with(s("MELEE_BONUS_PER_STACK"), 1.75)
                    .with(s("THRESHOLD"), 4);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("DAMAGE_REDUCTION"), .1);
            case 1 -> t.with(s("REVIVE_HEALTH_MULTIPLIER"), 1).with(s("REVIVE_ABSORPTION"), 4)
                    .with(s("STATUS_DURATION_TICKS"), 100);
            case 2 -> t.with(s("STATUS_DURATION_TICKS"), 140);
            case 3 -> mode(t, 256).with(s("THRESHOLD"), 30).with(s("STATUS_DURATION_TICKS"), 60)
                    .with(s("LOCKOUT_TICKS"), 300);
            case 4 -> mode(t, 512).with(s("RADIUS"), 6).with(s("TARGET_CAP"), 8)
                    .with(s("STATUS_DURATION_TICKS"), 80);
            case 5 -> mode(t, 1024).with(s("DURATION_TICKS"), 100).with(s("DAMAGE_MULTIPLIER"), .6)
                    .with(s("FIRE_TICKS"), 100);
            case 6 -> t.with(s("REVIVE_COOLDOWN_MULTIPLIER"), .85).with(s("COOLDOWN_TICKS"), 600);
            case 7 -> mode(t, 2048).with(s("REVIVE_HEALTH_MULTIPLIER"), .5)
                    .with(s("REVIVE_COOLDOWN_MULTIPLIER"), .6).with(s("STATUS_AMPLIFIER"), 1)
                    .with(s("STATUS_DURATION_TICKS"), 80);
            case 8 -> mode(t, 4096).with(s("REVIVE_HEALTH_MULTIPLIER"), 0).with(s("IMPACT_RADIUS"), 5)
                    .with(s("IMPACT_DAMAGE_MULTIPLIER"), 2).with(s("IMPACT_TARGET_CAP"), 12)
                    .with(s("COOLDOWN_TICKS"), 1200);
            default -> t;
        };
    }

    private static Phase2AbilityTuning gloampiercer(Phase2AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("PASSIVE_COOLDOWN_TICKS"), 10);
            case 1 -> t.with(s("FIRE_DELAY_TICKS"), 7);
            case 2 -> t.with(s("CONE_DEGREES"), 125).with(s("RANGE"), 14);
            case 3 -> t.multiply(s("PROJECTILE_DAMAGE_MULTIPLIER"), 1.1, 1);
            case 4 -> mode(t, 1).with(s("THRESHOLD"), 3).with(s("CLONE_COUNT"), 2)
                    .with(s("SECONDARY_DAMAGE_MULTIPLIER"), .7);
            case 5 -> mode(t, 2).with(s("DURATION_TICKS"), 80);
            case 6 -> t.with(s("HOMING_TURN_DEGREES"), 8).with(s("PROJECTILE_LIFETIME"), 100);
            case 7 -> mode(t, 4).with(s("CLONE_COUNT"), 3).with(s("PROJECTILE_DAMAGE_MULTIPLIER"), .55)
                    .with(s("PASSIVE_COOLDOWN_TICKS"), 24);
            case 8 -> mode(t, 8).with(s("CLONE_COUNT"), 1).with(s("PROJECTILE_DAMAGE_MULTIPLIER"), 2.2)
                    .with(s("HOMING_TURN_DEGREES"), 10).with(s("CONE_DEGREES"), 70).with(s("RANGE"), 10);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("SPEAR_COUNT"), 20);
            case 1 -> t.with(s("CLONE_COUNT"), 6);
            case 2 -> t.with(s("FIRE_DELAY_TICKS"), 10).with(s("THRESHOLD"), 10);
            case 3 -> t.with(s("PROJECTILE_SPEED"), 1.7).multiply(s("PROJECTILE_DAMAGE_MULTIPLIER"), 1.08, 1);
            case 4 -> t.with(s("EXPLOSION_RADIUS"), 3).with(s("SECONDARY_TARGET_CAP"), 8);
            case 5 -> t.with(s("INTERVAL_TICKS"), 2);
            case 6 -> t.with(s("CHANNEL_DURATION_TICKS"), 52).with(s("COOLDOWN_TICKS"), 420);
            case 7 -> mode(t, 16).with(s("SPEAR_COUNT"), 30).with(s("RADIUS"), 10)
                    .with(s("PROJECTILE_DAMAGE_MULTIPLIER"), .65).with(s("CHANNEL_DURATION_TICKS"), 70)
                    .with(s("MOVEMENT_RETENTION"), 0);
            case 8 -> mode(t, 32).with(s("SPEAR_COUNT"), 6).with(s("PROJECTILE_DAMAGE_MULTIPLIER"), 2.4)
                    .with(s("EXPLOSION_RADIUS"), 1.5).with(s("HOMING_TURN_DEGREES"), 10)
                    .add(s("COOLDOWN_TICKS"), 90, 450);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("STAIN_RADIUS"), 1.8);
            case 1 -> t.with(s("STAIN_DURATION_TICKS"), 300);
            case 2 -> t.with(s("STAIN_AMPLIFIER"), 1).with(s("STATUS_DURATION_TICKS"), 60);
            case 3 -> t.with(s("BONUS_PER_TRIGGER"), .15);
            case 4 -> t.with(s("TRIGGER_RADIUS"), 1.5).with(s("EMBEDDED_DURATION_TICKS"), 140);
            case 5 -> mode(t, 64).with(s("RANGE"), 5).with(s("FIRE_DELAY_TICKS"), 4)
                    .with(s("SECONDARY_TARGET_CAP"), 6);
            case 6 -> mode(t, 128).with(s("TARGET_CAP"), 6).with(s("PULL_STRENGTH"), 1);
            case 7 -> mode(t, 256).with(s("RANGE"), 6).with(s("MOVEMENT_SPEED"), .15)
                    .with(s("STAIN_DURATION_TICKS"), 200).multiply(s("DAMAGE_MULTIPLIER"), .75, 1);
            case 8 -> mode(t, 512).with(s("STAIN_AMPLIFIER"), 0).with(s("STAIN_DURATION_TICKS"), 100)
                    .with(s("IMPACT_DAMAGE_MULTIPLIER"), .8).with(s("IMPACT_RADIUS"), 2.5)
                    .with(s("IMPACT_TARGET_CAP"), 6);
            default -> t;
        };
    }

    private static Phase2AbilityTuning wraithfang(Phase2AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.multiply(s("PROJECTILE_DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.with(s("PROJECTILE_SPEED"), 1.85);
            case 2 -> t.with(s("LOYALTY"), 2);
            case 3 -> t.with(s("STATUS_DURATION_TICKS"), 60).with(s("STATUS_AMPLIFIER"), 0);
            case 4 -> t.with(s("BONUS_PER_TRIGGER"), .65).with(s("THRESHOLD"), 40);
            case 5 -> t.with(s("PIERCE_COUNT"), 1).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .65);
            case 6 -> t.with(s("IMPACT_DAMAGE_MULTIPLIER"), .35).with(s("IMPACT_RADIUS"), 2.5)
                    .with(s("IMPACT_TARGET_CAP"), 5).with(s("LOCKOUT_TICKS"), 20);
            case 7 -> mode(t, 1).with(s("PROJECTILE_SPEED"), 2.3).with(s("LOYALTY"), 0)
                    .with(s("PIERCE_COUNT"), 4).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .75)
                    .with(s("DURATION_TICKS"), 80).with(s("COOLDOWN_TICKS"), 60);
            case 8 -> mode(t, 2).with(s("PROJECTILE_DAMAGE_MULTIPLIER"), 2.25).with(s("PIERCE_COUNT"), 0)
                    .with(s("STATUS_AMPLIFIER"), 1).with(s("STATUS_DURATION_TICKS"), 80)
                    .with(s("LOYALTY"), 1).with(s("DURATION_TICKS"), 30);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("DASH_DURATION_TICKS"), 14);
            case 1 -> t.with(s("DASH_SPEED"), 1.55);
            case 2 -> t.multiply(s("RANGE"), 1.25, 14.4);
            case 3 -> mode(t, 4).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .25).with(s("SECONDARY_TARGET_CAP"), 6);
            case 4 -> mode(t, 8).with(s("HOMING_TURN_DEGREES"), 8).with(s("DURATION_TICKS"), 10);
            case 5 -> mode(t, 16).with(s("RANGE"), 2).with(s("IMPACT_DAMAGE_MULTIPLIER"), .7);
            case 6 -> t.with(s("COOLDOWN_TICKS"), 14);
            case 7 -> mode(t, 32).with(s("SECONDARY_TARGET_CAP"), 10).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .5)
                    .with(s("STATUS_DURATION_TICKS"), 20).with(s("COOLDOWN_TICKS"), 40);
            case 8 -> mode(t, 64).with(s("RANGE"), 12).with(s("IMPACT_DAMAGE_MULTIPLIER"), 1.6)
                    .with(s("COOLDOWN_TICKS"), 60).multiply(s("PROJECTILE_SPEED"), .75, 1.65);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("HASTE_DURATION_TICKS"), 110);
            case 1 -> t.with(s("MELEE_BONUS_PER_STACK"), .08);
            case 2 -> mode(t, 128).with(s("HASTE_DURATION_TICKS"), 60).with(s("HASTE_AMPLIFIER"), 0);
            case 3 -> mode(t, 256).with(s("DAMAGE_REDUCTION"), .2).with(s("DURATION_TICKS"), 40);
            case 4 -> mode(t, 512).with(s("HASTE_DURATION_TICKS"), 100).with(s("HASTE_AMPLIFIER"), 2)
                    .with(s("LOCKOUT_TICKS"), 20);
            case 5 -> mode(t, 1024).with(s("DURATION_TICKS"), 60).with(s("BONUS_PER_TRIGGER"), .05)
                    .with(s("BONUS_CAP"), .2);
            case 6 -> mode(t, 2048).with(s("COOLDOWN_REFUND_TICKS"), 10)
                    .with(s("COOLDOWN_REFUND_CAP_TICKS"), 40);
            case 7 -> mode(t, 4096).with(s("HASTE_DURATION_TICKS"), 80).with(s("HASTE_AMPLIFIER"), 3)
                    .multiply(s("COOLDOWN_TICKS"), .5, 20).multiply(s("DAMAGE_MULTIPLIER"), .8, 1);
            case 8 -> mode(t, 8192).with(s("HASTE_DURATION_TICKS"), 0).with(s("DURATION_TICKS"), 80)
                    .with(s("MELEE_BONUS_CAP"), 1).with(s("COOLDOWN_TICKS"), 40);
            default -> t;
        };
    }

    private static Phase2AbilityTuning wraithmaw(Phase2AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("SPEAR_COUNT"), 18);
            case 1 -> t.with(s("MATERIALIZE_TICKS"), 10);
            case 2 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 3 -> t.with(s("FALL_SPEED"), 1.45);
            case 4 -> t.with(s("RADIUS"), 7);
            case 5 -> t.with(s("INTERVAL_TICKS"), 1).with(s("THRESHOLD"), 10);
            case 6 -> t.with(s("IMPACT_DAMAGE_MULTIPLIER"), .3).with(s("IMPACT_RADIUS"), 2)
                    .with(s("IMPACT_TARGET_CAP"), 4);
            case 7 -> mode(t, 1).with(s("SPEAR_COUNT"), 28).with(s("RADIUS"), 9)
                    .with(s("DAMAGE_MULTIPLIER"), .65).add(s("COOLDOWN_TICKS"), 20, 600);
            case 8 -> mode(t, 2).with(s("SPEAR_COUNT"), 6).with(s("RADIUS"), 2)
                    .with(s("DAMAGE_MULTIPLIER"), 2.5).with(s("IMPACT_DAMAGE_MULTIPLIER"), 0)
                    .with(s("IMPACT_TARGET_CAP"), 0).add(s("COOLDOWN_TICKS"), 100, 600);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("ORBIT_CAP"), 7);
            case 1 -> t.with(s("RECOVERY_RADIUS"), 2);
            case 2 -> t.with(s("ORBIT_DURATION_TICKS"), 1100);
            case 3 -> t.with(s("LAUNCH_SPEED"), 1.55);
            case 4 -> t.with(s("LAUNCH_RANGE"), 28).with(s("PROJECTILE_LIFETIME"), 100);
            case 5 -> t.with(s("HOMING_RANGE"), 5).with(s("HOMING_TURN_DEGREES"), 9);
            case 6 -> t.with(s("BONUS_PER_TRIGGER"), .04).with(s("BONUS_CAP"), .24);
            case 7 -> mode(t, 4).with(s("ORBIT_CAP"), 12).with(s("THRESHOLD"), 2)
                    .with(s("PROJECTILE_DAMAGE_MULTIPLIER"), .65).with(s("ORBIT_DURATION_TICKS"), 600);
            case 8 -> mode(t, 8).with(s("ORBIT_CAP"), 1).with(s("PROJECTILE_DAMAGE_MULTIPLIER"), 3)
                    .with(s("HOMING_TURN_DEGREES"), 12).with(s("LOCKOUT_TICKS"), 80);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("STAIN_RADIUS"), 1.6);
            case 1 -> t.with(s("STAIN_DURATION_TICKS"), 320).with(s("EMBEDDED_DURATION_TICKS"), 600);
            case 2 -> t.with(s("STAIN_AMPLIFIER"), 1).with(s("STATUS_DURATION_TICKS"), 60);
            case 3 -> t.with(s("BONUS_PER_TRIGGER"), .15);
            case 4 -> mode(t, 16).with(s("RANGE"), 3).with(s("INTERVAL_TICKS"), 40)
                    .with(s("SECONDARY_DAMAGE_MULTIPLIER"), .25).with(s("TARGET_CAP"), 8);
            case 5 -> mode(t, 32).with(s("RANGE"), 5).with(s("STAIN_EXTENSION_TICKS"), 40)
                    .with(s("EXTRA_DURATION_CAP"), 80);
            case 6 -> mode(t, 64).with(s("RANGE"), 4).with(s("TARGET_CAP"), 5).with(s("PULL_STRENGTH"), 1);
            case 7 -> mode(t, 128).with(s("RANGE"), 6).with(s("MOVEMENT_SPEED"), .18)
                    .with(s("SECONDARY_DAMAGE_MULTIPLIER"), .6);
            case 8 -> mode(t, 256).with(s("STAIN_AMPLIFIER"), 0).with(s("DURATION_TICKS"), 200)
                    .with(s("RANGE"), 12).with(s("TARGET_CAP"), 8).with(s("DAMAGE_MULTIPLIER"), .9)
                    .add(s("COOLDOWN_TICKS"), 120, 600);
            default -> t;
        };
    }

    @Override
    public void onAbilityEvent(UniqueAbilityEvent event, MasteryProfile.Node node) {
        String executionKey = "execution/" + event.execution().definition().id().getPath();
        long tick = event.execution().context().world().getTime();
        if (event.phase() == UniqueAbilityPhase.START) {
            Phase2MasteryRuntime.set(event.execution().context().stack(), executionKey, 1, tick + 1200, tick);
        } else if (event.phase() == UniqueAbilityPhase.FINISH || event.phase() == UniqueAbilityPhase.CANCEL) {
            Phase2MasteryRuntime.clear(event.execution().context().stack(), executionKey);
            return;
        }
    }

    private static boolean matches(int profile, UniqueAbilityDefinition definition) {
        return switch (profile) {
            case 0 -> definition == Phase2UniqueAbilities.WATCHER_DREAD
                    || definition == Phase2UniqueAbilities.WATCHER_OMEN;
            case 1 -> definition == Phase2UniqueAbilities.DEVOURER_MASS
                    || definition == Phase2UniqueAbilities.DEVOURER_REPRISAL;
            case 2 -> definition == Phase2UniqueAbilities.WICKPIERCER_THROW
                    || definition == Phase2UniqueAbilities.WICKPIERCER_REVIVE;
            case 3 -> definition == Phase2UniqueAbilities.GLOAMPIERCER_AMBUSH
                    || definition == Phase2UniqueAbilities.GLOAMPIERCER_BARRAGE;
            case 4 -> definition == Phase2UniqueAbilities.WRAITHFANG_THROW;
            case 5 -> definition == Phase2UniqueAbilities.WRAITHMAW_MUSTER;
            default -> false;
        };
    }

    private static Phase2AbilityTuning mode(Phase2AbilityTuning tuning, int bit) {
        int current = tuning.integer(Phase2AbilityTuning.Setting.MODE, 0);
        return tuning.with(Phase2AbilityTuning.Setting.MODE, current | bit);
    }

    private static Phase2AbilityTuning.Setting s(String name) {
        return Phase2AbilityTuning.Setting.valueOf(name);
    }

    private static int parameter(MasteryProfile.Node node, String key, int fallback) {
        return node.effect().parameters().getOrDefault(key, fallback);
    }
}
