package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplymastery.mastery.definition.MasteryCohort;
import net.sweenus.simplyswords.api.ability.ArcaneCosmicMasteryTuning;
import net.sweenus.simplyswords.api.ability.ArcaneCosmicMasteryAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;

import java.util.List;
import java.util.Set;

final class ArcaneCosmicMasterySkillEffect implements StaticAbilitySkillEffectType {
    private static final Identifier ID = MasteryCohort.ARCANE_COSMIC.effectId();

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
        if (!effect.parameters().keySet().equals(Set.of("kind"))) {
            errors.add(where + "cohort/arcane_cosmic requires only kind");
        }
        int kind = effect.parameters().getOrDefault("kind", -1);
        if (kind < 0 || kind >= 189) errors.add(where + "kind must be between 0 and 188");
    }

    @Override
    public void tuneStatic(UniqueAbilityDefinition definition,
                     UniqueAbilityTuning.Builder tuning, MasteryProfile.Node node) {
        int kind = node.effect().parameters().getOrDefault("kind", -1);
        if (kind < 0 || kind >= 189) return;
        int profile = kind / 27;
        int branch = kind % 27 / 9;
        int slot = kind % 9;
        if (!matches(profile, branch, definition)) return;
        ArcaneCosmicMasteryTuning value = mode(tuning.get(ArcaneCosmicMasteryAbilities.TUNING), 1 << (branch * 9 + slot));
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
        tuning.set(ArcaneCosmicMasteryAbilities.TUNING, value);
        if (definition.cooldownKey().isPresent()) tuning.set(ArcaneCosmicMasteryAbilities.COOLDOWN_TICKS,
                value.integer(s("COOLDOWN_TICKS"), tuning.get(ArcaneCosmicMasteryAbilities.COOLDOWN_TICKS)));
    }

    private static ArcaneCosmicMasteryTuning arcanethyst(ArcaneCosmicMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("CHANCE"), 7, 25);
            case 1 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 2 -> t.with(s("ARCANETHYST_LEVITATION_MELEE_DAMAGE_MULTIPLIER"), 1.12);
            case 3 -> t.with(s("DURATION_TICKS"), 60).with(s("OUTGOING_MULTIPLIER"), 1.1);
            case 4 -> t.with(s("COUNT"), 4).with(s("DELAY_TICKS"), 6).with(s("RADIUS"), 4)
                    .with(s("SECONDARY_DURATION_TICKS"), 30);
            case 5 -> t.with(s("SECONDARY_STATUS_DURATION_TICKS"), 40);
            case 6 -> t.with(s("ABSORPTION"), 4).with(s("WINDUP_TICKS"), 60)
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
            case 6 -> t.with(s("DELAY_TICKS"), 12).with(s("TERTIARY_RADIUS"), 3)
                    .with(s("TERTIARY_DAMAGE_MULTIPLIER"), .4);
            case 7 -> t.with(s("HEIGHT"), 6).multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.8, 4)
                    .add(s("COOLDOWN_TICKS"), 60, 220);
            case 8 -> t.multiply(s("FINAL_DAMAGE_MULTIPLIER"), .7, 4).with(s("RADIUS"), 5)
                    .with(s("TERTIARY_DURATION_TICKS"), 80).with(s("TERTIARY_INTERVAL_TICKS"), 20)
                    .with(s("TERTIARY_TARGET_CAP"), 8).with(s("SPELL_MULTIPLIER"), .2);
            default -> t;
        };
    }

    private static ArcaneCosmicMasteryTuning starsEdge(ArcaneCosmicMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.with(s("STATUS_DURATION_TICKS"), 60);
            case 2 -> t.with(s("PER_STACK_MULTIPLIER"), .03).with(s("STACK_CAP"), 4).with(s("LOCKOUT_TICKS"), 40);
            case 3 -> t.with(s("OUTGOING_MULTIPLIER"), 1.15);
            case 4 -> t.with(s("COUNT"), 5).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .3)
                    .with(s("SECONDARY_RADIUS"), 2).with(s("TARGET_CAP"), 5);
            case 5 -> t.with(s("STATUS_DURATION_TICKS"), 60);
            case 6 -> t.with(s("SECONDARY_DURATION_TICKS"), 40);
            case 7 -> t.with(s("COUNT"), 5).with(s("FLAT_DAMAGE"), 25).with(s("RADIUS"), 5)
                    .with(s("SECONDARY_DAMAGE_MULTIPLIER"), 1.5).multiply(s("DAMAGE_MULTIPLIER"), .5, 1);
            case 8 -> t.with(s("OUTGOING_MULTIPLIER"), .6).with(s("HEAL_MULTIPLIER"), 0);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.multiply(s("HEAL_MULTIPLIER"), 1.1, 1);
            case 1 -> t.with(s("STATUS_DURATION_TICKS"), 30);
            case 2 -> t.with(s("ABSORPTION"), 2).with(s("STATUS_DURATION_TICKS"), 40).with(s("LOCKOUT_TICKS"), 40);
            case 3 -> t.with(s("HEALTH_THRESHOLD"), .4).with(s("INCOMING_MULTIPLIER"), 1.25);
            case 4 -> t.with(s("DURATION_TICKS"), 60).with(s("PER_STACK_MULTIPLIER"), 1.08);
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
            case 1 -> t.add(s("DURATION_TICKS"), 20, 120);
            case 2 -> t.multiply(s("WIDTH"), .85, 1);
            case 3 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 4 -> t.add(s("RADIUS"), .4, 2.5);
            case 5 -> t.add(s("INTERVAL_TICKS"), -2, 5);
            case 6 -> t.with(s("STATUS_DURATION_TICKS"), 30).with(s("STATUS_AMPLIFIER"), 1);
            case 7 -> t.multiply(s("SECONDARY_DURATION_TICKS"), .5, 100).multiply(s("DAMAGE_MULTIPLIER"), 1.45, 1);
            case 8 -> t.add(s("SECONDARY_DURATION_TICKS"), 80, 100).add(s("DURATION_TICKS"), 80, 120);
            default -> t;
        };
    }

    private static ArcaneCosmicMasteryTuning magiscythe(ArcaneCosmicMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("DURATION_TICKS"), 30, 400);
            case 1 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 2 -> t.add(s("CHANCE"), 8, 5);
            case 3 -> t.with(s("PER_STACK_MULTIPLIER"), .05).with(s("STACK_CAP"), 5);
            case 4 -> t.with(s("DELAY_TICKS"), 2);
            case 5 -> t.with(s("STATUS_DURATION_TICKS"), 20);
            case 6 -> t.add(s("RADIUS"), 2, 4);
            case 7 -> t.with(s("SECONDARY_DURATION_TICKS"), 80).with(s("STACK_CAP"), 8)
                    .add(s("COOLDOWN_TICKS"), 120, 980);
            case 8 -> t.multiply(s("DURATION_TICKS"), .5, 400).with(s("INTERVAL_TICKS"), 5)
                    .multiply(s("DAMAGE_MULTIPLIER"), 1.25, 1).with(s("CHANCE"), 0);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("RADIUS"), 1.5, 4);
            case 1 -> t.with(s("COUNT"), 3).with(s("SECONDARY_RADIUS"), 3)
                    .with(s("SECONDARY_DAMAGE_MULTIPLIER"), .55);
            case 2 -> t.with(s("STATUS_DURATION_TICKS"), 30).with(s("STATUS_AMPLIFIER"), 0);
            case 3 -> t.with(s("PER_STACK_MULTIPLIER"), .05).with(s("STACK_CAP"), 4);
            case 4 -> t.with(s("HEALTH_THRESHOLD"), .7);
            case 5 -> t.with(s("SECONDARY_RADIUS"), 2).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .25)
                    .with(s("TARGET_CAP"), 4);
            case 6 -> t.with(s("DURATION_TICKS"), 60).with(s("DELAY_TICKS"), 2).with(s("LOCKOUT_TICKS"), 20);
            case 7 -> t.with(s("TARGET_CAP"), 4).with(s("DAMAGE_MULTIPLIER"), .8)
                    .with(s("SECONDARY_DAMAGE_MULTIPLIER"), .7);
            case 8 -> t.with(s("DURATION_TICKS"), 60).multiply(s("DAMAGE_MULTIPLIER"), 2, 1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.add(s("CHANCE"), 8, 25);
            case 1 -> t.with(s("REPAIR_AMOUNT"), 1);
            case 2 -> t.with(s("SEARCH_CAP"), 1);
            case 3 -> t.with(s("LOCKOUT_TICKS"), 40);
            case 4 -> t.with(s("PITY_CHANCE"), 10).with(s("STACK_CAP"), 40);
            case 5 -> t.with(s("ABSORPTION"), 4).with(s("STATUS_DURATION_TICKS"), 40);
            case 6 -> t.with(s("COUNT"), 5).with(s("SECONDARY_DAMAGE_MULTIPLIER"), 1.5);
            case 7 -> t.with(s("REPAIR_AMOUNT"), 0).multiply(s("DAMAGE_MULTIPLIER"), .65, 1);
            case 8 -> t.with(s("FINAL_DAMAGE_MULTIPLIER"), 2).with(s("CHANCE"), 25)
                    .with(s("TERTIARY_TARGET_CAP"), 2);
            default -> t;
        };
    }

    private static ArcaneCosmicMasteryTuning magiblade(ArcaneCosmicMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("CHANCE"), 8, 55);
            case 1 -> t.add(s("INTERVAL_TICKS"), -2, 8);
            case 2 -> t.add(s("RADIUS"), 1, 4);
            case 3 -> t.multiply(s("KNOCKBACK"), 1.2, 1);
            case 4 -> t.with(s("STATUS_DURATION_TICKS"), 30).with(s("STATUS_AMPLIFIER"), 1);
            case 5 -> t.with(s("DURATION_TICKS"), 60).with(s("OUTGOING_MULTIPLIER"), 1.15);
            case 6 -> t.with(s("SECONDARY_DURATION_TICKS"), 20);
            case 7 -> t.with(s("INTERVAL_TICKS"), 40).with(s("LOCKOUT_TICKS"), 40);
            case 8 -> t.multiply(s("RADIUS"), .65, 4).with(s("TARGET_CAP"), 6)
                    .with(s("DAMAGE_MULTIPLIER"), .6).multiply(s("KNOCKBACK"), 2, 1);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("WINDUP_TICKS"), -10, 10);
            case 1 -> t.add(s("DURATION_TICKS"), 50, 300);
            case 2 -> t.add(s("RADIUS"), -.35, .9).multiply(s("SPEED"), 1.2, 1);
            case 3 -> t.with(s("COUNT"), 3).with(s("DELAY_TICKS"), 8).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .55);
            case 4 -> t.add(s("RANGE"), 3, 16);
            case 5 -> t.with(s("SECONDARY_DURATION_TICKS"), 80);
            case 6 -> t.with(s("ABSORPTION"), 4).with(s("INTERVAL_TICKS"), 80)
                    .with(s("STATUS_DURATION_TICKS"), 80);
            case 7 -> t.with(s("SECONDARY_COUNT"), 2).with(s("DAMAGE_MULTIPLIER"), .65).add(s("WINDUP_TICKS"), 15, 10);
            case 8 -> t.multiply(s("DURATION_TICKS"), 2, 300);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.add(s("WINDUP_TICKS"), -5, 12);
            case 2 -> t.add(s("INTERVAL_TICKS"), -8, 100);
            case 3 -> t.add(s("WIDTH"), .2, 1);
            case 4 -> t.with(s("STATUS_DURATION_TICKS"), 50).with(s("STATUS_AMPLIFIER"), 0);
            case 5 -> t.with(s("SECONDARY_TARGET_CAP"), 3).with(s("OUTGOING_MULTIPLIER"), 1.2);
            case 6 -> t.with(s("COUNT"), 3).with(s("SECONDARY_RADIUS"), 2.5)
                    .with(s("SECONDARY_DAMAGE_MULTIPLIER"), .5).with(s("TERTIARY_TARGET_CAP"), 5);
            case 7 -> t.with(s("WIDTH"), 5).with(s("TARGET_CAP"), 10)
                    .multiply(s("DAMAGE_MULTIPLIER"), 1.35, 1).multiply(s("INTERVAL_TICKS"), 1.5, 100);
            case 8 -> t.with(s("HEALTH_THRESHOLD"), .5).multiply(s("DAMAGE_MULTIPLIER"), 1.7, 1);
            default -> t;
        };
    }

    private static ArcaneCosmicMasteryTuning magispear(ArcaneCosmicMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("CHANCE"), 8, 25);
            case 1 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1);
            case 2 -> t.with(s("ARMOR_IGNORE"), .1);
            case 3 -> t.with(s("PITY_CHANCE"), 12);
            case 4 -> t.with(s("STATUS_DURATION_TICKS"), 50);
            case 5 -> t.with(s("COUNT"), 5).with(s("DELAY_TICKS"), 5).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .55);
            case 6 -> t.with(s("SECONDARY_STATUS_DURATION_TICKS"), 25).with(s("STATUS_AMPLIFIER"), 1)
                    .with(s("LOCKOUT_TICKS"), 40);
            case 7 -> t.with(s("COUNT"), 3).multiply(s("DAMAGE_MULTIPLIER"), .7, 1);
            case 8 -> t.multiply(s("CHANCE"), .5, 25).multiply(s("DAMAGE_MULTIPLIER"), 2, 1)
                    .with(s("SECONDARY_DURATION_TICKS"), 12);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("RADIUS"), .75, 4);
            case 1 -> t.with(s("TARGET_CAP"), 8);
            case 2 -> t.add(s("WINDUP_TICKS"), -6, 30);
            case 3 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 4 -> t.add(s("PULL_STRENGTH"), .2, .35);
            case 5 -> t.add(s("STACK_CAP"), 1, 6).add(s("DURATION_TICKS"), 10, 80);
            case 6 -> t.with(s("OUTGOING_MULTIPLIER"), 1.15).with(s("COUNT"), 2);
            case 7 -> t.multiply(s("STACK_CAP"), 2, 6).with(s("TARGET_CAP"), 12)
                    .multiply(s("DAMAGE_MULTIPLIER"), .6, 1).with(s("FINAL_DAMAGE_MULTIPLIER"), 0);
            case 8 -> t.with(s("STACK_CAP"), 1).with(s("RADIUS"), 4)
                    .with(s("DAMAGE_MULTIPLIER"), 2.6);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("RANGE"), 2);
            case 1 -> t.multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.15, 1);
            case 2 -> t.add(s("SECONDARY_RADIUS"), .6, 4);
            case 3 -> t.add(s("KNOCKBACK"), .2, 1);
            case 4 -> t.with(s("TERTIARY_STATUS_DURATION_TICKS"), 8);
            case 5 -> t.with(s("DELAY_TICKS"), 10).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .35);
            case 6 -> t.with(s("SECONDARY_DURATION_TICKS"), 50).with(s("STATUS_AMPLIFIER"), 1)
                    .with(s("TARGET_CAP"), 10);
            case 7 -> t.multiply(s("HEIGHT"), 1.4, 6).multiply(s("DURATION_TICKS"), 1.4, 20)
                    .multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.8, 1).add(s("COOLDOWN_TICKS"), 80, 120);
            case 8 -> t.multiply(s("FINAL_DAMAGE_MULTIPLIER"), .55, 1).with(s("TERTIARY_RADIUS"), 5)
                    .with(s("ABSORPTION"), 8).with(s("STATUS_DURATION_TICKS"), 100);
            default -> t;
        };
    }

    private static ArcaneCosmicMasteryTuning enigma(ArcaneCosmicMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("RANGE"), 3, 16);
            case 1 -> t.multiply(s("SPEED"), 1.15, 1);
            case 2 -> t.with(s("DURATION_TICKS"), 40).with(s("OUTGOING_MULTIPLIER"), 1.1);
            case 3 -> t.with(s("HEALTH_THRESHOLD"), .5);
            case 4 -> t.with(s("SECONDARY_RADIUS"), 6).with(s("LOCKOUT_TICKS"), 20);
            case 5 -> t.with(s("TERTIARY_RADIUS"), 1.5).with(s("STATUS_DURATION_TICKS"), 30)
                    .with(s("STATUS_AMPLIFIER"), 1);
            case 6 -> t.with(s("WIDTH"), 3);
            case 7 -> t.multiply(s("SPEED"), 1.4, 1).multiply(s("DAMAGE_MULTIPLIER"), 1.3, 1)
                    .with(s("RANGE"), 24);
            case 8 -> t.multiply(s("RANGE"), 2, 16).with(s("INTERVAL_TICKS"), 30);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("RADIUS"), .75, 5.5);
            case 1 -> t.multiply(s("PULL_STRENGTH"), 1.15, 1);
            case 2 -> t.add(s("DURATION_TICKS"), 10, 35);
            case 3 -> t.add(s("KNOCKBACK"), .15, 1);
            case 4 -> t.with(s("PER_STACK_MULTIPLIER"), .04).with(s("STACK_CAP"), 6);
            case 5 -> t.with(s("COUNT"), 2).with(s("SECONDARY_TARGET_CAP"), 6)
                    .with(s("OUTGOING_MULTIPLIER"), 1.1);
            case 6 -> t.with(s("SECONDARY_RADIUS"), 2).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .4)
                    .with(s("TERTIARY_TARGET_CAP"), 4);
            case 7 -> t.multiply(s("RADIUS"), 1.6, 5.5).multiply(s("DAMAGE_MULTIPLIER"), .65, 1)
                    .with(s("TARGET_CAP"), 64).with(s("KNOCKBACK"), 0);
            case 8 -> t.multiply(s("RADIUS"), .65, 5.5).with(s("TARGET_CAP"), 1)
                    .with(s("STACK_CAP"), 12).multiply(s("FINAL_DAMAGE_MULTIPLIER"), 2, 1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("STATUS_DURATION_TICKS"), 40);
            case 1 -> t.with(s("SECONDARY_DURATION_TICKS"), 20);
            case 2 -> t.with(s("INCOMING_MULTIPLIER"), .8);
            case 3 -> t.with(s("PER_STACK_MULTIPLIER"), 1.1);
            case 4 -> t.with(s("RADIUS"), 2).with(s("STATUS_DURATION_TICKS"), 20);
            case 5 -> t.with(s("REFUND_TICKS"), 8).with(s("STACK_CAP"), 48);
            case 6 -> t.with(s("SECONDARY_RADIUS"), 4).with(s("LOCKOUT_TICKS"), 40);
            case 7 -> t.with(s("TERTIARY_DURATION_TICKS"), 60);
            case 8 -> t.with(s("TERTIARY_DAMAGE_MULTIPLIER"), .8).with(s("SPEED"), 0)
                    .with(s("STATUS_AMPLIFIER"), 0);
            default -> t;
        };
    }

    private static ArcaneCosmicMasteryTuning caelestis(ArcaneCosmicMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("CAELESTIS_EAGER_RIFT_TICKS"), 20);
            case 1 -> t.add(s("INTERVAL_TICKS"), -4, 30);
            case 2 -> t.add(s("TARGET_CAP"), 2, 12);
            case 3 -> t.multiply(s("INCOMING_MULTIPLIER"), 1.15, 1);
            case 4 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1);
            case 5 -> t.with(s("SECONDARY_TARGET_CAP"), 8).with(s("TERTIARY_DURATION_TICKS"), 100);
            case 6 -> t.with(s("STACK_CAP"), 2);
            case 7 -> t.add(s("COUNT"), 1, 1).add(s("DURATION_TICKS"), 120, 900)
                    .multiply(s("INCOMING_MULTIPLIER"), .75, 1).multiply(s("DAMAGE_MULTIPLIER"), .75, 1);
            case 8 -> t.with(s("COUNT"), 1).with(s("TARGET_CAP"), 5)
                    .multiply(s("INCOMING_MULTIPLIER"), 2, 1).multiply(s("DAMAGE_MULTIPLIER"), 1.6, 1);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("RADIUS"), 2, 20);
            case 1 -> t.add(s("DURATION_TICKS"), 80, 900);
            case 2 -> t.add(s("SECONDARY_COUNT"), 2, 12).with(s("TERTIARY_TARGET_CAP"), 16);
            case 3 -> t.add(s("SECONDARY_INTERVAL_TICKS"), -5, 30);
            case 4 -> t.add(s("STATUS_DURATION_TICKS"), 20, 20);
            case 5 -> t.with(s("PULL_STRENGTH"), .12).with(s("TERTIARY_INTERVAL_TICKS"), 10)
                    .with(s("SEARCH_CAP"), 16);
            case 6 -> t.with(s("SECONDARY_DAMAGE_MULTIPLIER"), .25).with(s("LOCKOUT_TICKS"), 20)
                    .with(s("CAELESTIS_RIM_DAMAGE_TARGET_CAP"), 12);
            case 7 -> t.add(s("SECONDARY_DURATION_TICKS"), 60, 100).multiply(s("PULL_STRENGTH"), 1.5, .12);
            case 8 -> t.add(s("DURATION_TICKS"), -120, 900).with(s("SECONDARY_COUNT"), 0);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.add(s("CHANCE"), -1, 5);
            case 1 -> t.add(s("DELAY_TICKS"), 60, 0);
            case 2 -> t.multiply(s("HEAL_MULTIPLIER"), 1.15, 1).with(s("REFUND_TICKS"), 60);
            case 3 -> t.with(s("SECONDARY_RADIUS"), 8).with(s("TERTIARY_STATUS_DURATION_TICKS"), 20);
            case 4 -> t.with(s("SECONDARY_STATUS_DURATION_TICKS"), 120)
                    .with(s("OUTGOING_MULTIPLIER"), 1.15);
            case 5 -> t.with(s("SECONDARY_REFUND_TICKS"), 120);
            case 6 -> t.with(s("RANGE"), 20).with(s("PITY_CHANCE"), 50);
            case 7 -> t.with(s("CHANCE"), 100).multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.35, 1)
                    .with(s("TERTIARY_REFUND_TICKS"), 180);
            case 8 -> t.with(s("CHANCE"), 0).multiply(s("INTERVAL_TICKS"), 1.15, 30);
            default -> t;
        };
    }

    private static boolean matches(int profile, int branch, UniqueAbilityDefinition definition) {
        return definition == switch (profile) {
            case 0 -> switch (branch) {
                case 0 -> ArcaneCosmicMasteryAbilities.ARCANETHYST_SPARK;
                case 1 -> ArcaneCosmicMasteryAbilities.ARCANETHYST_SUSPENSION;
                default -> ArcaneCosmicMasteryAbilities.ARCANETHYST_IMPACT;
            };
            case 1 -> switch (branch) {
                case 0 -> ArcaneCosmicMasteryAbilities.STARS_SOLAR;
                case 1 -> ArcaneCosmicMasteryAbilities.STARS_LUNAR;
                default -> ArcaneCosmicMasteryAbilities.STARS_CONSTELLATION;
            };
            case 2 -> switch (branch) {
                case 0 -> ArcaneCosmicMasteryAbilities.MAGISCYTHE_STORM;
                case 1 -> ArcaneCosmicMasteryAbilities.MAGISCYTHE_STRIKES;
                default -> ArcaneCosmicMasteryAbilities.MAGISCYTHE_MAGEWRIGHT;
            };
            case 3 -> switch (branch) {
                case 0 -> ArcaneCosmicMasteryAbilities.MAGIBLADE_REPULSION;
                case 1 -> ArcaneCosmicMasteryAbilities.MAGIBLADE_WARDEN;
                default -> ArcaneCosmicMasteryAbilities.MAGIBLADE_JUDGMENT;
            };
            case 4 -> switch (branch) {
                case 0 -> ArcaneCosmicMasteryAbilities.MAGISPEAR_SPELLPOINT;
                case 1 -> ArcaneCosmicMasteryAbilities.MAGISPEAR_RAIN;
                default -> ArcaneCosmicMasteryAbilities.MAGISPEAR_SLAM;
            };
            case 5 -> switch (branch) {
                case 0 -> ArcaneCosmicMasteryAbilities.ENIGMA_STORMCHASER;
                case 1 -> ArcaneCosmicMasteryAbilities.ENIGMA_VORTEX;
                default -> ArcaneCosmicMasteryAbilities.ENIGMA_TAILWIND;
            };
            case 6 -> ArcaneCosmicMasteryAbilities.CAELESTIS_HOST;
            default -> null;
        };
    }

    private static ArcaneCosmicMasteryTuning mode(ArcaneCosmicMasteryTuning tuning, int bit) {
        return tuning.with(s("MODE"), tuning.integer(s("MODE"), 0) | bit);
    }

    private static ArcaneCosmicMasteryTuning.Setting s(String name) {
        return ArcaneCosmicMasteryTuning.Setting.valueOf(name);
    }
}
