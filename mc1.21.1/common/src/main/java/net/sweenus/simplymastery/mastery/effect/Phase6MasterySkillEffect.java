package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.SimplyMastery;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.Phase6AbilityTuning;
import net.sweenus.simplyswords.api.ability.Phase6UniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityContext;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;

import java.util.List;
import java.util.Set;

final class Phase6MasterySkillEffect implements AbilitySkillEffectType {
    private static final Identifier ID = Identifier.of(SimplyMastery.MOD_ID, "phase6_mastery");

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
        if (!effect.parameters().keySet().equals(Set.of("kind"))) {
            errors.add(where + "phase6_mastery requires only kind");
        }
        int kind = effect.parameters().getOrDefault("kind", -1);
        if (kind < 0 || kind >= 189) errors.add(where + "kind must be between 0 and 188");
    }

    @Override
    public void tune(UniqueAbilityContext context, UniqueAbilityDefinition definition,
                     UniqueAbilityTuning.Builder tuning, MasteryProfile.Node node) {
        int kind = node.effect().parameters().getOrDefault("kind", -1);
        if (kind < 0 || kind >= 189 || !matches(kind / 27, definition)) return;
        int profile = kind / 27;
        int branch = kind % 27 / 9;
        int slot = kind % 9;
        Phase6AbilityTuning value = mode(tuning.get(Phase6UniqueAbilities.TUNING), 1 << (branch * 9 + slot));
        value = switch (profile) {
            case 0 -> stormbringer(value, branch, slot);
            case 1 -> mjolnir(value, branch, slot);
            case 2 -> thunderbrand(value, branch, slot);
            case 3 -> tempest(value, branch, slot);
            case 4 -> frostfall(value, branch, slot);
            case 5 -> icewhisper(value, branch, slot);
            case 6 -> livyatan(value, branch, slot);
            default -> value;
        };
        tuning.set(Phase6UniqueAbilities.TUNING, value);
        if (definition.cooldownKey().isPresent()) {
            tuning.set(Phase6UniqueAbilities.COOLDOWN_TICKS,
                    value.integer(s("COOLDOWN_TICKS"), tuning.get(Phase6UniqueAbilities.COOLDOWN_TICKS)));
        }
    }

    private static Phase6AbilityTuning stormbringer(Phase6AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("INTERVAL_TICKS"), 4, 20);
            case 1 -> t.add(s("COUNT"), 1, 1);
            case 2 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1).add(s("RADIUS"), .5, 3).with(s("TARGET_CAP"), 10);
            case 3 -> t.with(s("STATUS_DURATION_TICKS"), 30);
            case 4 -> t.add(s("PULSE_COUNT"), 2, 5);
            case 5 -> t.multiply(s("KNOCKBACK"), 1.25, 1).with(s("FIRE_TICKS"), 20);
            case 6 -> t.with(s("REFUND_TICKS"), 30);
            case 7 -> t.with(s("INTERVAL_TICKS"), 8).multiply(s("DAMAGE_MULTIPLIER"), 2, 1);
            case 8 -> t.add(s("DURATION_TICKS"), 20, 20).with(s("COUNT"), 3)
                    .multiply(s("DAMAGE_MULTIPLIER"), .5, 1);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("CHARGE_CAP"), 12);
            case 1 -> t.with(s("LOCKOUT_TICKS"), 80).with(s("COUNT"), 1);
            case 2 -> t.with(s("COUNT"), 5).with(s("OUTGOING_MULTIPLIER"), 1.08);
            case 3 -> t.with(s("ABSORPTION"), 2).with(s("STATUS_DURATION_TICKS"), 60).with(s("LOCKOUT_TICKS"), 40);
            case 4 -> t.with(s("STATUS_DURATION_TICKS"), 30);
            case 5 -> t.with(s("COUNT"), 3).with(s("LOCKOUT_TICKS"), 100).with(s("STATUS_DURATION_TICKS"), 80);
            case 6 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.25, 1).with(s("COUNT"), 2);
            case 7 -> t.with(s("CHARGE_CAP"), 15).with(s("PER_STACK_MULTIPLIER"), .04).with(s("INTERVAL_TICKS"), 40);
            case 8 -> t.with(s("CHARGE_CAP"), 6).multiply(s("COOLDOWN_TICKS"), .5, 20).with(s("COUNT"), 1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.add(s("RANGE"), 1.5, 6);
            case 1 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1);
            case 2 -> t.add(s("TARGET_CAP"), 1, 4).with(s("COUNT"), 6);
            case 3 -> t.with(s("STATUS_DURATION_TICKS"), 80).with(s("OUTGOING_MULTIPLIER"), 1.15);
            case 4 -> t.with(s("RADIUS"), 2).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .25).with(s("TARGET_CAP"), 6);
            case 5 -> t.with(s("COUNT"), 2);
            case 6 -> t.with(s("REFUND_TICKS"), 4).with(s("COOLDOWN_TICKS"), 8);
            case 7 -> t.with(s("TARGET_CAP"), 1).with(s("COUNT"), 3).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .55);
            case 8 -> t.with(s("TARGET_CAP"), 10).add(s("RANGE"), 3, 6).with(s("PER_STACK_MULTIPLIER"), .82);
            default -> t;
        };
    }

    private static Phase6AbilityTuning mjolnir(Phase6AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("DURATION_TICKS"), 40, 200);
            case 1 -> t.with(s("INTERVAL_TICKS"), 9);
            case 2 -> t.add(s("RADIUS"), 2, 8).with(s("TARGET_CAP"), 20);
            case 3 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1);
            case 4 -> t.add(s("STATUS_DURATION_TICKS"), 40, 100);
            case 5 -> t.with(s("COUNT"), 4).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .6).with(s("RANGE"), 5);
            case 6 -> t.with(s("PER_STACK_MULTIPLIER"), .05).with(s("STACK_CAP"), 6);
            case 7 -> t.multiply(s("DURATION_TICKS"), 2, 200).with(s("FINAL_DAMAGE_MULTIPLIER"), 0)
                    .multiply(s("DAMAGE_MULTIPLIER"), .8, 1);
            case 8 -> t.with(s("DURATION_TICKS"), 100).with(s("INTERVAL_TICKS"), 6)
                    .multiply(s("DAMAGE_MULTIPLIER"), 1.35, 1).multiply(s("RADIUS"), .75, 8);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("STATUS_DURATION_TICKS"), 100);
            case 1 -> t.with(s("RADIUS"), 2.5).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .3).with(s("LOCKOUT_TICKS"), 40);
            case 2 -> t.with(s("RANGE"), 8).with(s("OUTGOING_MULTIPLIER"), 1.15);
            case 3 -> t.with(s("LOCKOUT_TICKS"), 60).with(s("COUNT"), 1);
            case 4 -> t.with(s("RADIUS"), 2).with(s("TARGET_CAP"), 4).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .2)
                    .with(s("LOCKOUT_TICKS"), 20);
            case 5 -> t.with(s("STATUS_DURATION_TICKS"), 40).with(s("STATUS_AMPLIFIER"), 1);
            case 6 -> t.with(s("STATUS_DURATION_TICKS"), 60).with(s("REFUND_TICKS"), 20);
            case 7 -> t.with(s("TARGET_CAP"), 1).with(s("STATUS_DURATION_TICKS"), 20);
            case 8 -> t.with(s("RADIUS"), 3).with(s("STATUS_DURATION_TICKS"), 200);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("ABSORPTION"), 4).with(s("STATUS_DURATION_TICKS"), 80);
            case 1 -> t.with(s("INCOMING_MULTIPLIER"), .88);
            case 2 -> t.with(s("SECONDARY_DAMAGE_MULTIPLIER"), .2).with(s("LOCKOUT_TICKS"), 30);
            case 3 -> t.multiply(s("KNOCKBACK"), 1.15, 1).with(s("PULL_STRENGTH"), 1.25);
            case 4 -> t.with(s("STATUS_AMPLIFIER"), 1);
            case 5 -> t.with(s("COUNT"), 4);
            case 6 -> t.add(s("RADIUS"), 1.5, 8).multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.25, 1).with(s("TARGET_CAP"), 24);
            case 7 -> t.with(s("FINAL_DAMAGE_MULTIPLIER"), 0).with(s("ABSORPTION"), 8).with(s("STATUS_DURATION_TICKS"), 120);
            case 8 -> t.multiply(s("FINAL_DAMAGE_MULTIPLIER"), 2, 1).multiply(s("KNOCKBACK"), 1.5, 1);
            default -> t;
        };
    }

    private static Phase6AbilityTuning thunderbrand(Phase6AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("CHANCE"), 5, 15);
            case 1 -> t.add(s("COOLDOWN_TICKS"), -25, 250);
            case 2 -> t.add(s("DURATION_TICKS"), -6, 40);
            case 3 -> t.add(s("COUNT"), 3, 15);
            case 4 -> t.multiply(s("SPEED"), 1.12, 4);
            case 5 -> t.add(s("RADIUS"), .5, 2).with(s("TARGET_CAP"), 8);
            case 6 -> t.with(s("RADIUS"), 3).with(s("FINAL_DAMAGE_MULTIPLIER"), .45).with(s("TARGET_CAP"), 10);
            case 7 -> t.with(s("COUNT"), 8).multiply(s("SPEED"), 1.5, 4)
                    .with(s("FINAL_DAMAGE_MULTIPLIER"), 1).with(s("PULL_STRENGTH"), 0);
            case 8 -> t.multiply(s("COUNT"), 2, 15).multiply(s("DAMAGE_MULTIPLIER"), .65, 1)
                    .multiply(s("FINAL_DAMAGE_MULTIPLIER"), .65, 1).with(s("PULL_STRENGTH"), 1.5);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("DURATION_TICKS"), 6, 40);
            case 1 -> t.with(s("LOCKOUT_TICKS"), 40);
            case 2 -> t.with(s("CHARGE_CAP"), 17);
            case 3 -> t.with(s("ABSORPTION"), 2).with(s("TARGET_CAP"), 8).with(s("STATUS_DURATION_TICKS"), 40);
            case 4 -> t.with(s("STATUS_DURATION_TICKS"), 30);
            case 5 -> t.with(s("COUNT"), 8).multiply(s("DAMAGE_MULTIPLIER"), 1.2, 1);
            case 6 -> t.with(s("REFUND_TICKS"), 40);
            case 7 -> t.with(s("INCOMING_MULTIPLIER"), .2).with(s("PER_STACK_MULTIPLIER"), .12);
            case 8 -> t.with(s("CHARGE_CAP"), 0).multiply(s("DAMAGE_MULTIPLIER"), 1.4, 1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.add(s("TARGET_CAP"), 1, 4).with(s("COUNT"), 6);
            case 1 -> t.add(s("RANGE"), 1.5, 6);
            case 2 -> t.multiply(s("SECONDARY_DAMAGE_MULTIPLIER"), 1.15, 1);
            case 3 -> t.with(s("OUTGOING_MULTIPLIER"), 1.2).with(s("STATUS_DURATION_TICKS"), 60);
            case 4 -> t.with(s("SECONDARY_DAMAGE_MULTIPLIER"), .4);
            case 5 -> t.with(s("RADIUS"), 2).with(s("FINAL_DAMAGE_MULTIPLIER"), .2).with(s("TARGET_CAP"), 4);
            case 6 -> t.with(s("RADIUS"), 3.5).with(s("FINAL_DAMAGE_MULTIPLIER"), .6).with(s("TARGET_CAP"), 12);
            case 7 -> t.with(s("TARGET_CAP"), 6).with(s("PER_STACK_MULTIPLIER"), .3);
            case 8 -> t.with(s("COUNT"), 2).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .55);
            default -> t;
        };
    }

    private static Phase6AbilityTuning tempest(Phase6AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("RADIUS"), 1, 1);
            case 1 -> t.multiply(s("PER_STACK_MULTIPLIER"), 1.1, 1).with(s("STACK_CAP"), 30);
            case 2 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 3 -> t.multiply(s("PULL_STRENGTH"), 1.2, 1);
            case 4 -> t.with(s("COUNT"), 5).with(s("ABSORPTION"), 4).with(s("STATUS_DURATION_TICKS"), 80);
            case 5 -> t.add(s("DURATION_TICKS"), 40, 1200).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .75);
            case 6 -> t.with(s("STACK_CAP"), 30).multiply(s("INTERVAL_TICKS"), .8, 10).with(s("SEARCH_CAP"), 24);
            case 7 -> t.multiply(s("STACK_CAP"), .75, 30).multiply(s("DAMAGE_MULTIPLIER"), .75, 1)
                    .with(s("STATUS_DURATION_TICKS"), 1200);
            case 8 -> t.multiply(s("STACK_CAP"), 1.35, 30).multiply(s("DAMAGE_MULTIPLIER"), 1.35, 1).with(s("RANGE"), 12);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("DURATION_TICKS"), 200, 500);
            case 1 -> t.with(s("STACK_CAP"), 12);
            case 2 -> t.with(s("COUNT"), 3);
            case 3 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1);
            case 4 -> t.with(s("OUTGOING_MULTIPLIER"), 1.05).with(s("STATUS_AMPLIFIER"), 2);
            case 5 -> t.with(s("COUNT"), 4);
            case 6 -> t.with(s("LOCKOUT_TICKS"), 100).with(s("STATUS_DURATION_TICKS"), 100);
            case 7 -> t.with(s("LOCKOUT_TICKS"), 200).multiply(s("PER_STACK_MULTIPLIER"), 1.5, 1);
            case 8 -> t.with(s("STACK_CAP"), 6).multiply(s("DAMAGE_MULTIPLIER"), 1.25, 1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.add(s("COUNT"), -1, 2);
            case 1 -> t.with(s("PER_STACK_MULTIPLIER"), .12);
            case 2 -> t.with(s("RADIUS"), 3).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .4).with(s("TARGET_CAP"), 10);
            case 3 -> t.with(s("COUNT"), 4).with(s("LOCKOUT_TICKS"), 20);
            case 4 -> t.with(s("COUNT"), 5).with(s("RADIUS"), 3).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .3)
                    .with(s("TARGET_CAP"), 8);
            case 5 -> t.with(s("STATUS_DURATION_TICKS"), 1200);
            case 6 -> t.with(s("FINAL_DAMAGE_MULTIPLIER"), .1).with(s("STACK_CAP"), 20)
                    .with(s("RADIUS"), 4).with(s("TARGET_CAP"), 16);
            case 7 -> t.with(s("RADIUS"), 4).multiply(s("PULL_STRENGTH"), 1.8, 1)
                    .multiply(s("DAMAGE_MULTIPLIER"), 1.8, 1).multiply(s("DURATION_TICKS"), .5, 1200);
            case 8 -> t.with(s("COUNT"), 2).with(s("DAMAGE_MULTIPLIER"), .75).with(s("DURATION_TICKS"), 0);
            default -> t;
        };
    }

    private static Phase6AbilityTuning frostfall(Phase6AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1);
            case 1 -> t.with(s("STATUS_DURATION_TICKS"), 40);
            case 2 -> t.with(s("COUNT"), 10).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .2);
            case 3 -> t.with(s("OUTGOING_MULTIPLIER"), 1.15);
            case 4 -> t.with(s("FREEZE_TICKS"), 30);
            case 5 -> t.with(s("COUNT"), 3).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .25).with(s("RANGE"), 4);
            case 6 -> t.with(s("RANGE"), 12).with(s("STATUS_DURATION_TICKS"), 100).with(s("OUTGOING_MULTIPLIER"), 1.2);
            case 7 -> t.with(s("OUTGOING_MULTIPLIER"), 1.8).with(s("PULSE_COUNT"), 0).with(s("RANGE"), 35);
            case 8 -> t.multiply(s("COOLDOWN_TICKS"), .5, 65).multiply(s("DAMAGE_MULTIPLIER"), .7, 1)
                    .multiply(s("SPEED"), 1.25, 1);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.multiply(s("SPEED"), 1.15, 1);
            case 1 -> t.with(s("TARGET_CAP"), 6).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .35);
            case 2 -> t.with(s("PULL_STRENGTH"), .2);
            case 3 -> t.with(s("STATUS_DURATION_TICKS"), 60);
            case 4 -> t.with(s("REFUND_TICKS"), 15);
            case 5 -> t.with(s("LOCKOUT_TICKS"), 20);
            case 6 -> t.with(s("LOCKOUT_TICKS"), 40).with(s("OUTGOING_MULTIPLIER"), 1.2).with(s("STATUS_DURATION_TICKS"), 80);
            case 7 -> t.with(s("RADIUS"), 5).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .5).add(s("DURATION_TICKS"), 20, 0);
            case 8 -> t.with(s("SECONDARY_DAMAGE_MULTIPLIER"), .8).with(s("PULSE_COUNT"), 0);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.add(s("RADIUS"), 1, 6).with(s("TARGET_CAP"), 20);
            case 1 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 2 -> t.add(s("STATUS_DURATION_TICKS"), 20, 40);
            case 3 -> t.multiply(s("PULL_STRENGTH"), 1.25, 1);
            case 4 -> t.with(s("COUNT"), 5).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .2);
            case 5 -> t.with(s("COUNT"), 5).with(s("STATUS_DURATION_TICKS"), 60).with(s("STATUS_AMPLIFIER"), 2);
            case 6 -> t.multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.4, 1);
            case 7 -> t.with(s("PULSE_COUNT"), 8).multiply(s("DAMAGE_MULTIPLIER"), .65, 1)
                    .with(s("DURATION_TICKS"), 100).with(s("PULL_STRENGTH"), 0);
            case 8 -> t.with(s("PULSE_COUNT"), 1).with(s("DURATION_TICKS"), 30)
                    .multiply(s("DAMAGE_MULTIPLIER"), 2.5, 1).multiply(s("PULL_STRENGTH"), 2, 1);
            default -> t;
        };
    }

    private static Phase6AbilityTuning icewhisper(Phase6AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.add(s("RADIUS"), 1, 5).with(s("TARGET_CAP"), 20);
            case 2 -> t.add(s("STATUS_DURATION_TICKS"), 20, 40);
            case 3 -> t.with(s("LOCKOUT_TICKS"), 60).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .3);
            case 4 -> t.with(s("FREEZE_TICKS"), 4).with(s("FREEZE_CAP_TICKS"), 100);
            case 5 -> t.with(s("COUNT"), 60).with(s("OUTGOING_MULTIPLIER"), 1.15);
            case 6 -> t.with(s("RADIUS"), 2).with(s("STATUS_AMPLIFIER"), 1);
            case 7 -> t.multiply(s("RADIUS"), .65, 5).multiply(s("DAMAGE_MULTIPLIER"), 1.8, 1).with(s("RANGE"), 2);
            case 8 -> t.multiply(s("RADIUS"), 2, 5).multiply(s("DAMAGE_MULTIPLIER"), .55, 1).with(s("INCOMING_MULTIPLIER"), .9);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("LOCKOUT_TICKS"), -4, 20);
            case 1 -> t.add(s("WIDTH"), .5, 2.5).with(s("TARGET_CAP"), 8);
            case 2 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1);
            case 3 -> t.with(s("INTERVAL_TICKS"), 12);
            case 4 -> t.with(s("COUNT"), 3);
            case 5 -> t.with(s("RANGE"), 8).with(s("TARGET_CAP"), 1);
            case 6 -> t.with(s("LOCKOUT_TICKS"), 60).with(s("PER_STACK_MULTIPLIER"), .15).with(s("STACK_CAP"), 3);
            case 7 -> t.multiply(s("COUNT"), 2, 3).multiply(s("DAMAGE_MULTIPLIER"), .55, 1)
                    .with(s("WIDTH"), 1.5).add(s("DURATION_TICKS"), 40, 200);
            case 8 -> t.with(s("COUNT"), 1).multiply(s("DAMAGE_MULTIPLIER"), 2.2, 1)
                    .with(s("WIDTH"), 4).with(s("INTERVAL_TICKS"), 24);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("ABSORPTION"), 4).with(s("STATUS_DURATION_TICKS"), 80);
            case 1 -> t.with(s("STATUS_DURATION_TICKS"), 30);
            case 2 -> t.with(s("INCOMING_MULTIPLIER"), .85);
            case 3 -> t.with(s("RADIUS"), 3).with(s("STATUS_DURATION_TICKS"), 40);
            case 4 -> t.with(s("FREEZE_TICKS"), 20).with(s("LOCKOUT_TICKS"), 30);
            case 5 -> t.with(s("OUTGOING_MULTIPLIER"), .9).with(s("STATUS_DURATION_TICKS"), 40);
            case 6 -> t.with(s("COUNT"), 35).with(s("STATUS_DURATION_TICKS"), 60).with(s("STATUS_AMPLIFIER"), 1);
            case 7 -> t.with(s("RADIUS"), 5).with(s("STATUS_AMPLIFIER"), 0);
            case 8 -> t.with(s("INCOMING_MULTIPLIER"), 1).with(s("OUTGOING_MULTIPLIER"), 1.6);
            default -> t;
        };
    }

    private static Phase6AbilityTuning livyatan(Phase6AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.add(s("WIDTH"), 1, 5).with(s("TARGET_CAP"), 8);
            case 2 -> t.add(s("LENGTH"), 2, 7);
            case 3 -> t.multiply(s("KNOCKBACK"), 1.2, .52);
            case 4 -> t.multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.25, 1);
            case 5 -> t.with(s("STATUS_DURATION_TICKS"), 30);
            case 6 -> t.with(s("COUNT"), 4).with(s("INTERVAL_TICKS"), 6).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .55);
            case 7 -> t.with(s("WIDTH"), 8).multiply(s("DAMAGE_MULTIPLIER"), .6, 1)
                    .with(s("TARGET_CAP"), 12).multiply(s("KNOCKBACK"), 1.8, .52);
            case 8 -> t.with(s("WIDTH"), 2).multiply(s("LENGTH"), 1.5, 7)
                    .multiply(s("DAMAGE_MULTIPLIER"), 1.75, 1).with(s("KNOCKBACK"), 0);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.multiply(s("SPEED"), 1.15, 1);
            case 1 -> t.multiply(s("PULL_STRENGTH"), 1.25, .42);
            case 2 -> t.add(s("RADIUS"), 1, 6).with(s("TARGET_CAP"), 16);
            case 3 -> t.add(s("CHANCE"), 10, 20);
            case 4 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.15, 1);
            case 5 -> t.with(s("RANGE"), 2).with(s("STATUS_DURATION_TICKS"), 40).with(s("STATUS_AMPLIFIER"), 1);
            case 6 -> t.with(s("COUNT"), 3).with(s("REFUND_TICKS"), 20);
            case 7 -> t.with(s("COUNT"), 2).with(s("DURATION_TICKS"), 40)
                    .with(s("SECONDARY_DAMAGE_MULTIPLIER"), .35).multiply(s("PULL_STRENGTH"), 1.5, .42).with(s("CHANCE"), 0);
            case 8 -> t.with(s("PULL_STRENGTH"), 0).with(s("CHANCE"), 100)
                    .with(s("DAMAGE_MULTIPLIER"), .8).with(s("TARGET_CAP"), 8);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1);
            case 1 -> t.with(s("RADIUS"), 2.5).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .25).with(s("TARGET_CAP"), 6);
            case 2 -> t.with(s("RANGE"), 4).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .2).with(s("COUNT"), 1);
            case 3 -> t.with(s("RANGE"), 5).with(s("PULL_STRENGTH"), 15);
            case 4 -> t.with(s("COUNT"), 3).with(s("LOCKOUT_TICKS"), 60).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .3);
            case 5 -> t.with(s("STATUS_DURATION_TICKS"), 80).with(s("OUTGOING_MULTIPLIER"), 1.15);
            case 6 -> t.with(s("LOCKOUT_TICKS"), 120).with(s("RADIUS"), 3).with(s("FINAL_DAMAGE_MULTIPLIER"), .75);
            case 7 -> t.with(s("COUNT"), 2).multiply(s("DAMAGE_MULTIPLIER"), 1.3, 1)
                    .multiply(s("COOLDOWN_TICKS"), 2, 2);
            case 8 -> t.with(s("COUNT"), 0).multiply(s("DAMAGE_MULTIPLIER"), 1.5, 1)
                    .add(s("COOLDOWN_TICKS"), 20, 65);
            default -> t;
        };
    }

    private static boolean matches(int profile, UniqueAbilityDefinition definition) {
        return switch (profile) {
            case 0 -> definition == Phase6UniqueAbilities.STORMBRINGER_GUARD
                    || definition == Phase6UniqueAbilities.STORMBRINGER_CHAIN;
            case 1 -> definition == Phase6UniqueAbilities.MJOLNIR_STORM;
            case 2 -> definition == Phase6UniqueAbilities.THUNDERBRAND_BLITZ;
            case 3 -> definition == Phase6UniqueAbilities.TEMPEST_MARK
                    || definition == Phase6UniqueAbilities.TEMPEST_VORTEX;
            case 4 -> definition == Phase6UniqueAbilities.FROSTFALL_THROW
                    || definition == Phase6UniqueAbilities.FROSTFALL_FIELD;
            case 5 -> definition == Phase6UniqueAbilities.ICEWHISPER_AURA
                    || definition == Phase6UniqueAbilities.ICEWHISPER_COMETS;
            case 6 -> definition == Phase6UniqueAbilities.LIVYATAN_THROW
                    || definition == Phase6UniqueAbilities.LIVYATAN_RETURN
                    || definition == Phase6UniqueAbilities.LIVYATAN_WAVE;
            default -> false;
        };
    }

    private static Phase6AbilityTuning mode(Phase6AbilityTuning tuning, int bit) {
        return tuning.with(s("MODE"), tuning.integer(s("MODE"), 0) | bit);
    }

    private static Phase6AbilityTuning.Setting s(String name) {
        return Phase6AbilityTuning.Setting.valueOf(name);
    }
}
