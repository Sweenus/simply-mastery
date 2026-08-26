package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.SimplyMastery;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.Phase9AbilityTuning;
import net.sweenus.simplyswords.api.ability.Phase9UniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityContext;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;

import java.util.List;
import java.util.Set;

final class Phase9MasterySkillEffect implements AbilitySkillEffectType {
    private static final Identifier ID = Identifier.of(SimplyMastery.MOD_ID, "phase9_mastery");

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
        if (!effect.parameters().keySet().equals(Set.of("kind"))) {
            errors.add(where + "phase9_mastery requires only kind");
        }
        int kind = effect.parameters().getOrDefault("kind", -1);
        if (kind < 0 || kind >= 189) errors.add(where + "kind must be between 0 and 188");
    }

    @Override
    public void tune(UniqueAbilityContext context, UniqueAbilityDefinition definition,
                     UniqueAbilityTuning.Builder tuning, MasteryProfile.Node node) {
        int kind = node.effect().parameters().getOrDefault("kind", -1);
        if (kind < 0 || kind >= 189) return;
        int profile = kind / 27;
        int branch = kind % 27 / 9;
        int slot = kind % 9;
        if (!matches(profile, branch, definition)) return;
        Phase9AbilityTuning value = mode(tuning.get(Phase9UniqueAbilities.TUNING), 1 << (branch * 9 + slot));
        value = switch (profile) {
            case 0 -> arcanethyst(value, branch, slot);
            case 1 -> starsEdge(value, branch, slot);
            case 2 -> magiscythe(value, branch, slot);
            case 3 -> magiblade(value, branch, slot);
            case 4 -> magispear(value, branch, slot);
            case 5 -> enigma(value, branch, slot);
            case 6 -> caelestis(value, branch, slot);
            default -> value;
        };
        tuning.set(Phase9UniqueAbilities.TUNING, value);
        if (definition.cooldownKey().isPresent()) tuning.set(Phase9UniqueAbilities.COOLDOWN_TICKS,
                value.integer(s("COOLDOWN_TICKS"), tuning.get(Phase9UniqueAbilities.COOLDOWN_TICKS)));
    }

    private static Phase9AbilityTuning arcanethyst(Phase9AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("CHANCE"), 7, 25);
            case 1 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 2 -> t.multiply(s("SPELL_MULTIPLIER"), 1.12, 1);
            case 3 -> t.with(s("DURATION_TICKS"), 60).with(s("OUTGOING_MULTIPLIER"), 1.1);
            case 4 -> t.with(s("COUNT"), 4).with(s("DELAY_TICKS"), 6).with(s("RADIUS"), 4)
                    .with(s("SECONDARY_DURATION_TICKS"), 30);
            case 5 -> t.with(s("STATUS_DURATION_TICKS"), 40).with(s("STATUS_AMPLIFIER"), 0);
            case 6 -> t.with(s("ABSORPTION"), 4).with(s("STATUS_DURATION_TICKS"), 60)
                    .with(s("LOCKOUT_TICKS"), 100);
            case 7 -> t.multiply(s("CHANCE"), 2, 25).multiply(s("DAMAGE_MULTIPLIER"), .65, 1)
                    .with(s("RADIUS"), 4).with(s("SECONDARY_DURATION_TICKS"), 36);
            case 8 -> t.with(s("CHANCE"), 15).with(s("STATUS_DURATION_TICKS"), 100)
                    .with(s("STATUS_AMPLIFIER"), 3);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("HEIGHT"), 1, 4);
            case 1 -> t.add(s("WINDUP_TICKS"), 4, 18);
            case 2 -> t.add(s("DURATION_TICKS"), 8, 14);
            case 3 -> t.add(s("RADIUS"), 1, 6);
            case 4 -> t.with(s("SPEED"), .3);
            case 5 -> t.with(s("STATUS_DURATION_TICKS"), 30).with(s("STATUS_AMPLIFIER"), 2);
            case 6 -> t.with(s("PER_STACK_MULTIPLIER"), .03).with(s("COUNT"), 8);
            case 7 -> t.with(s("DURATION_TICKS"), 50).with(s("FINAL_DAMAGE_MULTIPLIER"), 1.4);
            case 8 -> t.with(s("DURATION_TICKS"), 0).with(s("RADIUS"), 6).with(s("TARGET_CAP"), 8)
                    .with(s("PULL_STRENGTH"), .18).with(s("DELAY_TICKS"), 18)
                    .with(s("DAMAGE_MULTIPLIER"), .65);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.15, 4);
            case 1 -> t.add(s("INTERVAL_TICKS"), -2, 10);
            case 2 -> t.with(s("SECONDARY_RADIUS"), 2.5).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .35)
                    .with(s("TARGET_CAP"), 6);
            case 3 -> t.with(s("STATUS_DURATION_TICKS"), 40).with(s("STATUS_AMPLIFIER"), 0);
            case 4 -> t.with(s("PER_STACK_MULTIPLIER"), .05).with(s("COUNT"), 4);
            case 5 -> t.with(s("KNOCKBACK"), .5);
            case 6 -> t.with(s("DELAY_TICKS"), 12).with(s("SECONDARY_RADIUS"), 3)
                    .with(s("SECONDARY_DAMAGE_MULTIPLIER"), .4);
            case 7 -> t.with(s("HEIGHT"), 6).multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.8, 4)
                    .add(s("COOLDOWN_TICKS"), 60, 220);
            case 8 -> t.multiply(s("FINAL_DAMAGE_MULTIPLIER"), .7, 4).with(s("RADIUS"), 5)
                    .with(s("DURATION_TICKS"), 80).with(s("INTERVAL_TICKS"), 20)
                    .with(s("TARGET_CAP"), 8).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .2);
            default -> t;
        };
    }

    private static Phase9AbilityTuning starsEdge(Phase9AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.with(s("STATUS_DURATION_TICKS"), 60);
            case 2 -> t.with(s("PER_STACK_MULTIPLIER"), .03).with(s("STACK_CAP"), 4).with(s("LOCKOUT_TICKS"), 40);
            case 3 -> t.with(s("OUTGOING_MULTIPLIER"), 1.15);
            case 4 -> t.with(s("COUNT"), 5).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .3)
                    .with(s("SECONDARY_RADIUS"), 2).with(s("TARGET_CAP"), 5);
            case 5 -> t.with(s("STATUS_DURATION_TICKS"), 60);
            case 6 -> t.with(s("SECONDARY_DURATION_TICKS"), 40);
            case 7 -> t.with(s("COUNT"), 5).with(s("STACK_CAP"), 25).with(s("RADIUS"), 5)
                    .with(s("SECONDARY_DAMAGE_MULTIPLIER"), 1.5).multiply(s("DAMAGE_MULTIPLIER"), .5, 1);
            case 8 -> t.with(s("OUTGOING_MULTIPLIER"), .6).with(s("HEAL_MULTIPLIER"), 0);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.multiply(s("HEAL_MULTIPLIER"), 1.1, 1);
            case 1 -> t.with(s("STATUS_DURATION_TICKS"), 30);
            case 2 -> t.with(s("ABSORPTION"), 2).with(s("STATUS_DURATION_TICKS"), 40).with(s("LOCKOUT_TICKS"), 40);
            case 3 -> t.with(s("HEALTH_THRESHOLD"), .4).multiply(s("HEAL_MULTIPLIER"), 1.25, 1);
            case 4 -> t.with(s("DURATION_TICKS"), 60).with(s("OUTGOING_MULTIPLIER"), 1.08);
            case 5 -> t.with(s("REFUND_TICKS"), 10).with(s("STACK_CAP"), 60);
            case 6 -> t.with(s("ABSORPTION"), 8);
            case 7 -> t.with(s("STATUS_DURATION_TICKS"), 40).with(s("OUTGOING_MULTIPLIER"), 1.35)
                    .multiply(s("HEAL_MULTIPLIER"), .6, 1);
            case 8 -> t.with(s("COUNT"), 2).with(s("RADIUS"), 5).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .35)
                    .multiply(s("HEAL_MULTIPLIER"), .75, 1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.multiply(s("RANGE"), 1.15, 1);
            case 1 -> t.add(s("DURATION_TICKS"), 20, 60);
            case 2 -> t.multiply(s("WIDTH"), .85, 1).with(s("COUNT"), 16);
            case 3 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 4 -> t.add(s("RADIUS"), .4, 2);
            case 5 -> t.add(s("INTERVAL_TICKS"), -2, 5);
            case 6 -> t.with(s("STATUS_DURATION_TICKS"), 30).with(s("STATUS_AMPLIFIER"), 1);
            case 7 -> t.multiply(s("SECONDARY_DURATION_TICKS"), .5, 100).multiply(s("DAMAGE_MULTIPLIER"), 1.45, 1);
            case 8 -> t.add(s("SECONDARY_DURATION_TICKS"), 80, 100).with(s("COUNT"), 0);
            default -> t;
        };
    }

    private static Phase9AbilityTuning magiscythe(Phase9AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("DURATION_TICKS"), 30, 200);
            case 1 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 2 -> t.add(s("CHANCE"), 8, 25);
            case 3 -> t.with(s("PER_STACK_MULTIPLIER"), .05).with(s("STACK_CAP"), 5);
            case 4 -> t.with(s("INTERVAL_TICKS"), 2).with(s("COUNT"), 3).with(s("SECONDARY_INTERVAL_TICKS"), 6);
            case 5 -> t.with(s("STATUS_DURATION_TICKS"), 20);
            case 6 -> t.add(s("RADIUS"), 2, 6);
            case 7 -> t.with(s("DURATION_TICKS"), 80).with(s("STACK_CAP"), 8)
                    .add(s("COOLDOWN_TICKS"), 120, 400);
            case 8 -> t.multiply(s("DURATION_TICKS"), .5, 200).multiply(s("INTERVAL_TICKS"), .5, 20)
                    .multiply(s("DAMAGE_MULTIPLIER"), 1.25, 1).with(s("CHANCE"), 0);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("RADIUS"), 1.5, 6);
            case 1 -> t.with(s("COUNT"), 3).with(s("SECONDARY_RADIUS"), 3)
                    .with(s("SECONDARY_DAMAGE_MULTIPLIER"), .55);
            case 2 -> t.with(s("STATUS_DURATION_TICKS"), 30).with(s("STATUS_AMPLIFIER"), 0);
            case 3 -> t.with(s("PER_STACK_MULTIPLIER"), .05).with(s("STACK_CAP"), 4);
            case 4 -> t.with(s("HEALTH_THRESHOLD"), .7);
            case 5 -> t.with(s("SECONDARY_RADIUS"), 2).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .25)
                    .with(s("TARGET_CAP"), 4);
            case 6 -> t.with(s("DURATION_TICKS"), 60).with(s("REFUND_TICKS"), 2).with(s("LOCKOUT_TICKS"), 20);
            case 7 -> t.with(s("TARGET_CAP"), 4).with(s("DAMAGE_MULTIPLIER"), .8)
                    .with(s("SECONDARY_DAMAGE_MULTIPLIER"), .7);
            case 8 -> t.with(s("TARGET_CAP"), 1).multiply(s("DAMAGE_MULTIPLIER"), 2, 1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.add(s("CHANCE"), 8, 25);
            case 1 -> t.add(s("REPAIR_AMOUNT"), 1, 1);
            case 2 -> t.with(s("SEARCH_CAP"), 1);
            case 3 -> t.with(s("REPAIR_AMOUNT"), 2).with(s("LOCKOUT_TICKS"), 40);
            case 4 -> t.with(s("PITY_CHANCE"), 10).with(s("CHANCE"), 40);
            case 5 -> t.with(s("ABSORPTION"), 4).with(s("STATUS_DURATION_TICKS"), 40);
            case 6 -> t.with(s("COUNT"), 5).with(s("REPAIR_AMOUNT"), 150);
            case 7 -> t.with(s("REPAIR_AMOUNT"), 1).multiply(s("DAMAGE_MULTIPLIER"), .65, 1);
            case 8 -> t.multiply(s("REPAIR_AMOUNT"), 2, 1).with(s("CHANCE"), 25).with(s("COUNT"), 2);
            default -> t;
        };
    }

    private static Phase9AbilityTuning magiblade(Phase9AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("CHANCE"), 8, 25);
            case 1 -> t.with(s("INTERVAL_TICKS"), 6);
            case 2 -> t.add(s("RADIUS"), 1, 5);
            case 3 -> t.multiply(s("KNOCKBACK"), 1.2, 1);
            case 4 -> t.with(s("STATUS_DURATION_TICKS"), 30).with(s("STATUS_AMPLIFIER"), 1);
            case 5 -> t.with(s("DURATION_TICKS"), 60).with(s("OUTGOING_MULTIPLIER"), 1.15);
            case 6 -> t.with(s("SECONDARY_DURATION_TICKS"), 20);
            case 7 -> t.with(s("LOCKOUT_TICKS"), 40).with(s("DAMAGE_MULTIPLIER"), 0);
            case 8 -> t.multiply(s("RADIUS"), .65, 5).with(s("TARGET_CAP"), 6)
                    .with(s("DAMAGE_MULTIPLIER"), .6).multiply(s("KNOCKBACK"), 2, 1);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("WINDUP_TICKS"), -10, 40);
            case 1 -> t.add(s("DURATION_TICKS"), 50, 200);
            case 2 -> t.add(s("RADIUS"), -.35, 2).multiply(s("SPEED"), 1.2, 1);
            case 3 -> t.with(s("COUNT"), 3).with(s("DELAY_TICKS"), 8).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .55);
            case 4 -> t.add(s("RANGE"), 3, 12);
            case 5 -> t.add(s("DURATION_TICKS"), 80, 200);
            case 6 -> t.with(s("ABSORPTION"), 4).with(s("INTERVAL_TICKS"), 80);
            case 7 -> t.with(s("COUNT"), 2).with(s("DAMAGE_MULTIPLIER"), .65).add(s("WINDUP_TICKS"), 15, 40);
            case 8 -> t.multiply(s("DURATION_TICKS"), 2, 200).with(s("SPEED"), 0);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.add(s("WINDUP_TICKS"), -5, 20);
            case 2 -> t.add(s("INTERVAL_TICKS"), -8, 40);
            case 3 -> t.add(s("WIDTH"), .2, 1);
            case 4 -> t.with(s("STATUS_DURATION_TICKS"), 50).with(s("STATUS_AMPLIFIER"), 0);
            case 5 -> t.with(s("TARGET_CAP"), 3).with(s("OUTGOING_MULTIPLIER"), 1.2);
            case 6 -> t.with(s("COUNT"), 3).with(s("SECONDARY_RADIUS"), 2.5)
                    .with(s("SECONDARY_DAMAGE_MULTIPLIER"), .5).with(s("TARGET_CAP"), 5);
            case 7 -> t.with(s("RADIUS"), 5).with(s("TARGET_CAP"), 10)
                    .multiply(s("DAMAGE_MULTIPLIER"), 1.35, 1).multiply(s("INTERVAL_TICKS"), 1.5, 40);
            case 8 -> t.with(s("HEALTH_THRESHOLD"), .5).multiply(s("DAMAGE_MULTIPLIER"), 1.7, 1);
            default -> t;
        };
    }

    private static Phase9AbilityTuning magispear(Phase9AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("CHANCE"), 8, 25);
            case 1 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1);
            case 2 -> t.with(s("ARMOR_IGNORE"), .1);
            case 3 -> t.add(s("CHANCE"), 12, 25);
            case 4 -> t.with(s("STATUS_DURATION_TICKS"), 50);
            case 5 -> t.with(s("COUNT"), 5).with(s("DELAY_TICKS"), 5).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .55);
            case 6 -> t.with(s("STATUS_DURATION_TICKS"), 25).with(s("STATUS_AMPLIFIER"), 1)
                    .with(s("LOCKOUT_TICKS"), 40);
            case 7 -> t.with(s("COUNT"), 3).multiply(s("DAMAGE_MULTIPLIER"), .7, 1);
            case 8 -> t.multiply(s("CHANCE"), .5, 25).multiply(s("DAMAGE_MULTIPLIER"), 2, 1)
                    .with(s("SECONDARY_DURATION_TICKS"), 12);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("RADIUS"), .75, 5);
            case 1 -> t.add(s("COUNT"), 1, 4).with(s("TARGET_CAP"), 8);
            case 2 -> t.add(s("WINDUP_TICKS"), -6, 30);
            case 3 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 4 -> t.with(s("PULL_STRENGTH"), .2);
            case 5 -> t.add(s("STACK_CAP"), 1, 3).add(s("DURATION_TICKS"), 10, 80);
            case 6 -> t.with(s("OUTGOING_MULTIPLIER"), 1.15).with(s("COUNT"), 2);
            case 7 -> t.multiply(s("STACK_CAP"), 2, 3).with(s("TARGET_CAP"), 12)
                    .multiply(s("DAMAGE_MULTIPLIER"), .6, 1).with(s("FINAL_DAMAGE_MULTIPLIER"), 0);
            case 8 -> t.with(s("COUNT"), 1).with(s("WINDUP_TICKS"), 30).with(s("RADIUS"), 4)
                    .with(s("DAMAGE_MULTIPLIER"), 2.6);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("RANGE"), 2);
            case 1 -> t.multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.15, 1);
            case 2 -> t.add(s("SECONDARY_RADIUS"), .6, 4);
            case 3 -> t.add(s("KNOCKBACK"), .2, 1);
            case 4 -> t.with(s("STATUS_DURATION_TICKS"), 8).with(s("STATUS_AMPLIFIER"), 2);
            case 5 -> t.with(s("DELAY_TICKS"), 10).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .35);
            case 6 -> t.with(s("SECONDARY_DURATION_TICKS"), 50).with(s("STATUS_AMPLIFIER"), 1)
                    .with(s("TARGET_CAP"), 10);
            case 7 -> t.multiply(s("HEIGHT"), 1.4, 8).multiply(s("DURATION_TICKS"), 1.4, 20)
                    .multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.8, 1).add(s("COOLDOWN_TICKS"), 80, 300);
            case 8 -> t.with(s("DAMAGE_MULTIPLIER"), .55).with(s("RADIUS"), 5).with(s("ABSORPTION"), 8)
                    .with(s("STATUS_DURATION_TICKS"), 100);
            default -> t;
        };
    }

    private static Phase9AbilityTuning enigma(Phase9AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("RANGE"), 3, 16);
            case 1 -> t.multiply(s("SPEED"), 1.15, 1);
            case 2 -> t.with(s("DURATION_TICKS"), 40).with(s("OUTGOING_MULTIPLIER"), 1.1);
            case 3 -> t.with(s("HEALTH_THRESHOLD"), .5);
            case 4 -> t.with(s("RANGE"), 6).with(s("LOCKOUT_TICKS"), 20);
            case 5 -> t.with(s("RADIUS"), 1.5).with(s("STATUS_DURATION_TICKS"), 30)
                    .with(s("STATUS_AMPLIFIER"), 1);
            case 6 -> t.with(s("RANGE"), 3);
            case 7 -> t.multiply(s("SPEED"), 1.4, 1).multiply(s("DAMAGE_MULTIPLIER"), 1.3, 1)
                    .with(s("RANGE"), 24);
            case 8 -> t.multiply(s("RANGE"), 2, 16).with(s("INTERVAL_TICKS"), 30);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("RADIUS"), .75, 3);
            case 1 -> t.multiply(s("PULL_STRENGTH"), 1.15, 1);
            case 2 -> t.add(s("DURATION_TICKS"), 10, 40);
            case 3 -> t.add(s("KNOCKBACK"), .15, 1);
            case 4 -> t.with(s("PER_STACK_MULTIPLIER"), .04).with(s("STACK_CAP"), 6);
            case 5 -> t.with(s("COUNT"), 2).with(s("TARGET_CAP"), 6)
                    .with(s("OUTGOING_MULTIPLIER"), 1.1);
            case 6 -> t.with(s("SECONDARY_RADIUS"), 2).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .4)
                    .with(s("TARGET_CAP"), 4);
            case 7 -> t.multiply(s("RADIUS"), 1.6, 3).multiply(s("DAMAGE_MULTIPLIER"), .65, 1)
                    .with(s("TARGET_CAP"), 64).with(s("KNOCKBACK"), 0);
            case 8 -> t.multiply(s("RADIUS"), .65, 3).with(s("TARGET_CAP"), 1)
                    .with(s("STACK_CAP"), 12).multiply(s("FINAL_DAMAGE_MULTIPLIER"), 2, 1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("STATUS_DURATION_TICKS"), 40);
            case 1 -> t.with(s("SECONDARY_DURATION_TICKS"), 20);
            case 2 -> t.with(s("INCOMING_MULTIPLIER"), .8);
            case 3 -> t.with(s("SPEED"), 1.1);
            case 4 -> t.with(s("RADIUS"), 2).with(s("STATUS_DURATION_TICKS"), 20);
            case 5 -> t.with(s("REFUND_TICKS"), 8).with(s("STACK_CAP"), 48);
            case 6 -> t.with(s("RANGE"), 4).with(s("LOCKOUT_TICKS"), 40);
            case 7 -> t.with(s("DURATION_TICKS"), 60);
            case 8 -> t.multiply(s("RADIUS"), .8, 3).with(s("SPEED"), 0)
                    .with(s("STATUS_AMPLIFIER"), 0);
            default -> t;
        };
    }

    private static Phase9AbilityTuning caelestis(Phase9AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("WINDUP_TICKS"), -20, 80);
            case 1 -> t.add(s("INTERVAL_TICKS"), -4, 40);
            case 2 -> t.add(s("TARGET_CAP"), 2, 8).with(s("SEARCH_CAP"), 16);
            case 3 -> t.multiply(s("INCOMING_MULTIPLIER"), 1.15, 1);
            case 4 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1);
            case 5 -> t.with(s("TARGET_CAP"), 8).with(s("DURATION_TICKS"), 100);
            case 6 -> t.with(s("COUNT"), 2);
            case 7 -> t.add(s("COUNT"), 1, 1).add(s("DURATION_TICKS"), 120, 300)
                    .multiply(s("INCOMING_MULTIPLIER"), .75, 1).multiply(s("DAMAGE_MULTIPLIER"), .75, 1);
            case 8 -> t.with(s("COUNT"), 1).with(s("TARGET_CAP"), 5)
                    .multiply(s("INCOMING_MULTIPLIER"), 2, 1).multiply(s("DAMAGE_MULTIPLIER"), 1.6, 1);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("RADIUS"), 2, 8);
            case 1 -> t.add(s("DURATION_TICKS"), 80, 300);
            case 2 -> t.add(s("COUNT"), 2, 8).with(s("TARGET_CAP"), 16).with(s("INTERVAL_TICKS"), 20);
            case 3 -> t.add(s("SECONDARY_INTERVAL_TICKS"), -5, 40);
            case 4 -> t.add(s("STATUS_DURATION_TICKS"), 20, 40);
            case 5 -> t.with(s("PULL_STRENGTH"), .12).with(s("INTERVAL_TICKS"), 10).with(s("TARGET_CAP"), 16);
            case 6 -> t.with(s("SECONDARY_DAMAGE_MULTIPLIER"), .25).with(s("INTERVAL_TICKS"), 20)
                    .with(s("TARGET_CAP"), 12);
            case 7 -> t.add(s("SECONDARY_DURATION_TICKS"), 60, 100).multiply(s("PULL_STRENGTH"), 1.5, .12);
            case 8 -> t.add(s("DURATION_TICKS"), -120, 300).with(s("COUNT"), 0);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.add(s("CHANCE"), -1, 5);
            case 1 -> t.add(s("DELAY_TICKS"), 60, 100);
            case 2 -> t.multiply(s("INCOMING_MULTIPLIER"), 1.15, 1).with(s("REFUND_TICKS"), 60);
            case 3 -> t.with(s("RADIUS"), 8).with(s("STATUS_DURATION_TICKS"), 20);
            case 4 -> t.with(s("DURATION_TICKS"), 120).with(s("OUTGOING_MULTIPLIER"), 1.15);
            case 5 -> t.with(s("REFUND_TICKS"), 120);
            case 6 -> t.with(s("RANGE"), 20).with(s("CHANCE"), 50);
            case 7 -> t.with(s("CHANCE"), 100).multiply(s("DAMAGE_MULTIPLIER"), 1.35, 1)
                    .with(s("REFUND_TICKS"), 180);
            case 8 -> t.with(s("CHANCE"), 0).multiply(s("INTERVAL_TICKS"), 1.15, 40)
                    .with(s("REFUND_TICKS"), 0);
            default -> t;
        };
    }

    private static boolean matches(int profile, int branch, UniqueAbilityDefinition definition) {
        return definition == switch (profile) {
            case 0 -> switch (branch) {
                case 0 -> Phase9UniqueAbilities.ARCANETHYST_SPARK;
                default -> Phase9UniqueAbilities.ARCANETHYST_SUSPENSION;
            };
            case 1 -> switch (branch) {
                case 0 -> Phase9UniqueAbilities.STARS_SOLAR;
                case 1 -> Phase9UniqueAbilities.STARS_LUNAR;
                default -> Phase9UniqueAbilities.STARS_CONSTELLATION;
            };
            case 2 -> switch (branch) {
                case 0 -> Phase9UniqueAbilities.MAGISCYTHE_STORM;
                case 1 -> Phase9UniqueAbilities.MAGISCYTHE_STRIKES;
                default -> Phase9UniqueAbilities.MAGISCYTHE_MAGEWRIGHT;
            };
            case 3 -> switch (branch) {
                case 0 -> Phase9UniqueAbilities.MAGIBLADE_REPULSION;
                default -> Phase9UniqueAbilities.MAGIBLADE_WARDEN;
            };
            case 4 -> switch (branch) {
                case 0 -> Phase9UniqueAbilities.MAGISPEAR_SPELLPOINT;
                default -> Phase9UniqueAbilities.MAGISPEAR_RAIN;
            };
            case 5 -> Phase9UniqueAbilities.ENIGMA_STORMCHASER;
            case 6 -> Phase9UniqueAbilities.CAELESTIS_HOST;
            default -> null;
        };
    }

    private static Phase9AbilityTuning mode(Phase9AbilityTuning tuning, int bit) {
        return tuning.with(s("MODE"), tuning.integer(s("MODE"), 0) | bit);
    }

    private static Phase9AbilityTuning.Setting s(String name) {
        return Phase9AbilityTuning.Setting.valueOf(name);
    }
}
