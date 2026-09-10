package net.sweenus.simplyswordsmastery.mastery.effect;

import net.minecraft.util.Identifier;
import net.sweenus.simplyswordsmastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswordsmastery.mastery.definition.MasteryCohort;
import net.sweenus.simplyswords.api.ability.*;

import java.util.List;
import java.util.Set;

final class MartialCommandEldritchMasterySkillEffect implements StaticAbilitySkillEffectType {
    private static final Identifier ID = MasteryCohort.MARTIAL_COMMAND_ELDRITCH.effectId();

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
        if (!effect.parameters().keySet().equals(Set.of("kind")))
            errors.add(where + "cohort/martial_command_eldritch requires only kind");
        int kind = effect.parameters().getOrDefault("kind", -1);
        if (kind < 0 || kind >= 135) errors.add(where + "kind must be between 0 and 134");
    }

    @Override
    public void tuneStatic(UniqueAbilityDefinition definition,
                     UniqueAbilityTuning.Builder tuning, MasteryProfile.Node node) {
        int kind = node.effect().parameters().getOrDefault("kind", -1);
        if (kind < 0 || kind >= 135) return;
        int profile = kind / 27;
        int branch = kind % 27 / 9;
        int slot = kind % 9;
        if (!matches(profile, branch, slot, definition)) return;
        MartialCommandEldritchMasteryTuning value = mode(tuning.get(MartialCommandEldritchMasteryAbilities.TUNING), 1 << (branch * 9 + slot));
        value = switch (profile) {
            case 0 -> warglaive(value, branch, slot);
            case 1 -> ribbon(value, branch, slot);
            case 2 -> riftmane(value, branch, slot);
            case 3 -> dawnquiver(value, branch, slot);
            case 4 -> dreadtide(value, branch, slot);
            default -> value;
        };
        tuning.set(MartialCommandEldritchMasteryAbilities.TUNING, value);
        if (definition.cooldownKey().isPresent()) tuning.set(MartialCommandEldritchMasteryAbilities.COOLDOWN_TICKS,
                value.integer(s("COOLDOWN_TICKS"), tuning.get(MartialCommandEldritchMasteryAbilities.COOLDOWN_TICKS)));
    }

    private static MartialCommandEldritchMasteryTuning warglaive(MartialCommandEldritchMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("DURATION_TICKS"), 40, 160);
            case 1 -> t.add(s("STACK_CAP"), 1, 5);
            case 2 -> t.with(s("COUNT"), 3);
            case 3 -> t.with(s("RADIUS"), 3).with(s("LOCKOUT_TICKS"), 20);
            case 4 -> t.with(s("SEARCH_RANGE"), 16).with(s("SECONDARY_DURATION_TICKS"), 20);
            case 5 -> t.with(s("SECONDARY_RADIUS"), 6);
            case 6 -> t.with(s("STATUS_DURATION_TICKS"), 40).with(s("STATUS_AMPLIFIER"), 0);
            case 7 -> t.with(s("TARGET_CAP"), 1).multiply(s("DAMAGE_MULTIPLIER"), 1.5, 1);
            case 8 -> t.with(s("TARGET_CAP"), 8).with(s("STACK_CAP"), 3).with(s("RADIUS"), 5);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.multiply(s("SPEED"), 1.12, 1);
            case 1 -> t.add(s("RADIUS"), 1.5, 8);
            case 2 -> t.add(s("RANGE"), 2, 16);
            case 3 -> t.with(s("SECONDARY_RADIUS"), 8).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .6);
            case 4 -> t.with(s("STATUS_DURATION_TICKS"), 20).with(s("STATUS_AMPLIFIER"), 1);
            case 5 -> t.with(s("PER_STACK_MULTIPLIER"), .05).with(s("STACK_CAP"), 5);
            case 6 -> t.with(s("FINAL_DAMAGE_MULTIPLIER"), .4);
            case 7 -> t.with(s("COUNT"), 2).with(s("DAMAGE_MULTIPLIER"), .55).multiply(s("SPEED"), .8, 1);
            case 8 -> t.with(s("TARGET_CAP"), 8).multiply(s("DAMAGE_MULTIPLIER"), 1.45, 1)
                    .multiply(s("SPEED"), 1.45, 1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.multiply(s("HEAL_MULTIPLIER"), 1.1, 1);
            case 2 -> t.add(s("HEALTH_THRESHOLD"), .05, .4);
            case 3 -> t.with(s("PER_STACK_MULTIPLIER"), .04).with(s("STACK_CAP"), 7);
            case 4 -> t.with(s("ABSORPTION"), 8);
            case 5 -> t.with(s("COUNT"), 3).with(s("SECONDARY_DURATION_TICKS"), 60);
            case 6 -> t.with(s("REFUND_TICKS"), 15).with(s("LOCKOUT_TICKS"), 60);
            case 7 -> t.multiply(s("HEAL_MULTIPLIER"), 2, 1).multiply(s("DAMAGE_MULTIPLIER"), .8, 1)
                    .with(s("HEALTH_THRESHOLD"), .6);
            case 8 -> t.with(s("HEAL_MULTIPLIER"), 0).multiply(s("DAMAGE_MULTIPLIER"), 1.65, 1)
                    .with(s("STATUS_DURATION_TICKS"), 50).with(s("STATUS_AMPLIFIER"), 1);
            default -> t;
        };
    }

    private static MartialCommandEldritchMasteryTuning ribbon(MartialCommandEldritchMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("SPEED"), .97);
            case 1 -> t.with(s("INCOMING_MULTIPLIER"), .82);
            case 2 -> t.with(s("KNOCKBACK"), .15);
            case 3 -> t.with(s("DURATION_TICKS"), 40).with(s("STATUS_DURATION_TICKS"), 20);
            case 4 -> t.with(s("OUTGOING_MULTIPLIER"), 1.1).with(s("LOCKOUT_TICKS"), 40);
            case 5 -> t.with(s("HEALTH_THRESHOLD"), .35).with(s("PER_STACK_MULTIPLIER"), 1);
            case 6 -> t.with(s("COUNT"), 3).with(s("ABSORPTION"), 4).with(s("SECONDARY_DURATION_TICKS"), 60);
            case 7 -> t.with(s("SPEED"), .88).with(s("INCOMING_MULTIPLIER"), .7);
            case 8 -> t.with(s("SPEED"), 1).with(s("INCOMING_MULTIPLIER"), 1)
                    .with(s("SECONDARY_DAMAGE_MULTIPLIER"), .88);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.multiply(s("SPEED"), 1.1, 1.7);
            case 1 -> t.add(s("COOLDOWN_TICKS"), -6, 40);
            case 2 -> t.with(s("DURATION_TICKS"), 4).with(s("ANGLE"), 12);
            case 3 -> t.with(s("DAMAGE_MULTIPLIER"), .3).with(s("KNOCKBACK"), .5)
                    .with(s("TARGET_CAP"), 1);
            case 4 -> t.with(s("TARGET_CAP"), 4).with(s("STATUS_DURATION_TICKS"), 30);
            case 5 -> t.with(s("SECONDARY_DURATION_TICKS"), 10);
            case 6 -> t.with(s("REFUND_TICKS"), 8);
            case 7 -> t.multiply(s("RANGE"), 2, 8).with(s("TARGET_CAP"), 8)
                    .with(s("DAMAGE_MULTIPLIER"), .45).with(s("ANGLE"), 0);
            case 8 -> t.with(s("SECONDARY_RADIUS"), 5).add(s("COOLDOWN_TICKS"), 20, 40);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("DURATION_TICKS"), 80);
            case 1 -> t.add(s("DAMAGE_MULTIPLIER"), .15, .95);
            case 2 -> t.with(s("TARGET_CAP"), 3).with(s("RADIUS"), 2.5).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .4);
            case 3 -> t.with(s("ARMOR_IGNORE"), .12);
            case 4 -> t.with(s("STATUS_DURATION_TICKS"), 40).with(s("STATUS_AMPLIFIER"), 1);
            case 5 -> t.with(s("WINDUP_TICKS"), 40).with(s("PER_STACK_MULTIPLIER"), .2);
            case 6 -> t.with(s("HEALTH_THRESHOLD"), .3).with(s("OUTGOING_MULTIPLIER"), 1.25);
            case 7 -> t.add(s("DAMAGE_MULTIPLIER"), 1, .95).with(s("TARGET_CAP"), 1);
            case 8 -> t.with(s("RADIUS"), 5).with(s("TARGET_CAP"), 10)
                    .with(s("SECONDARY_DAMAGE_MULTIPLIER"), .7);
            default -> t;
        };
    }

    private static MartialCommandEldritchMasteryTuning riftmane(MartialCommandEldritchMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("CHANCE"), 8, 20);
            case 1 -> t.add(s("LOCKOUT_TICKS"), -10, 60);
            case 2 -> t.add(s("SEARCH_RANGE"), 2, 16);
            case 3 -> t.add(s("SECONDARY_RADIUS"), -1, 3);
            case 4 -> t.add(s("ANGLE"), 15, 45);
            case 5 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.15, 1);
            case 6 -> t.with(s("REFUND_TICKS"), 30).with(s("SEARCH_RADIUS"), 6)
                    .with(s("SECONDARY_DAMAGE_MULTIPLIER"), .5);
            case 7 -> t.with(s("COUNT"), 3).multiply(s("DAMAGE_MULTIPLIER"), .45, 1)
                    .add(s("CHANCE"), -20, 20);
            case 8 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.8, 1)
                    .add(s("LOCKOUT_TICKS"), 30, 60);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("COUNT"), 1, 5);
            case 1 -> t.add(s("WIDTH"), 1, 6);
            case 2 -> t.add(s("RANGE"), 3, 16);
            case 3 -> t.multiply(s("SPEED"), 1.1, 1);
            case 4 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 5 -> t.add(s("KNOCKBACK"), .25, 1);
            case 6 -> t.with(s("DELAY_TICKS"), 10).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .55).with(s("TARGET_CAP"), 3);
            case 7 -> t.with(s("COUNT"), 10).multiply(s("WIDTH"), 1.5, 6)
                    .multiply(s("DAMAGE_MULTIPLIER"), .65, 1).multiply(s("RANGE"), .75, 16);
            case 8 -> t.with(s("COUNT"), 1).multiply(s("DAMAGE_MULTIPLIER"), 2.4, 1)
                    .multiply(s("RADIUS"), 2, 1.1).with(s("KNOCKBACK"), 0);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.add(s("WINDUP_TICKS"), -4, 12);
            case 1 -> t.add(s("SECONDARY_RADIUS"), .35, 1);
            case 2 -> t.add(s("HEIGHT"), .4, 1);
            case 3 -> t.multiply(s("SPEED"), 1.2, 1);
            case 4 -> t.with(s("STATUS_DURATION_TICKS"), 20).with(s("STATUS_AMPLIFIER"), 1);
            case 5 -> t.with(s("OUTGOING_MULTIPLIER"), 1.25);
            case 6 -> t.with(s("TARGET_CAP"), 6).with(s("SECONDARY_DAMAGE_MULTIPLIER"), 1.25);
            case 7 -> t.multiply(s("TRAVEL_DISTANCE_MULTIPLIER"), 3, 1);
            case 8 -> t.multiply(s("SPEED"), .75, 1).multiply(s("DAMAGE_MULTIPLIER"), 1.75, 1)
                    .multiply(s("KNOCKBACK"), 2, 1).with(s("STATUS_AMPLIFIER"), 2);
            default -> t;
        };
    }

    private static MartialCommandEldritchMasteryTuning dawnquiver(MartialCommandEldritchMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("INTERVAL_TICKS"), -15, 80);
            case 1 -> t.add(s("CHANCE"), 8, 20);
            case 2 -> t.add(s("RANGE"), 3, 20);
            case 3 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1);
            case 4 -> t.add(s("LOCKOUT_TICKS"), -12, 60);
            case 5 -> t.with(s("STATUS_DURATION_TICKS"), 60).with(s("OUTGOING_MULTIPLIER"), 1.15);
            case 6 -> t.with(s("SEARCH_CAP"), 4).with(s("DELAY_TICKS"), 6)
                    .with(s("SECONDARY_DAMAGE_MULTIPLIER"), .55);
            case 7 -> t.with(s("COUNT"), 3).with(s("DAMAGE_MULTIPLIER"), .45);
            case 8 -> t.with(s("STACK_CAP"), 3).multiply(s("DAMAGE_MULTIPLIER"), 1.85, 1)
                    .add(s("INTERVAL_TICKS"), 30, 80);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("STACK_CAP"), 1, 3);
            case 1 -> t.add(s("CHANCE"), 10, 20);
            case 2 -> t.add(s("PITY_CHANCE"), 15, 0);
            case 3 -> t.with(s("REFUND_TICKS"), 1).with(s("HEALTH_THRESHOLD"), .2);
            case 4 -> t.with(s("ABSORPTION"), 4).with(s("STATUS_DURATION_TICKS"), 40).with(s("LOCKOUT_TICKS"), 40);
            case 5 -> t.with(s("PER_STACK_MULTIPLIER"), .03);
            case 6 -> t.with(s("INCOMING_MULTIPLIER"), .8);
            case 7 -> t.with(s("STACK_CAP"), 3).multiply(s("DAMAGE_MULTIPLIER"), .75, 1);
            case 8 -> t.with(s("STACK_CAP"), 6).multiply(s("FINAL_DAMAGE_MULTIPLIER"), 2, 1)
                    .with(s("SECONDARY_DURATION_TICKS"), 200);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.add(s("WINDUP_TICKS"), -10, 40);
            case 1 -> t.add(s("HEALTH_THRESHOLD"), -.05, .25);
            case 2 -> t.multiply(s("SECONDARY_DAMAGE_MULTIPLIER"), 1.15, 1);
            case 3 -> t.add(s("TARGET_CAP"), 1, 3).add(s("OUTGOING_MULTIPLIER"), .05, .75);
            case 4 -> t.add(s("DELAY_TICKS"), -2, 8).add(s("INTERVAL_TICKS"), -1, 3);
            case 5 -> t.add(s("RADIUS"), .5, 2.5);
            case 6 -> t.multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.15, 1);
            case 7 -> t.with(s("SPELL_MULTIPLIER"), 2.5)
                    .with(s("HEALTH_THRESHOLD"), .8);
            case 8 -> t.with(s("COUNT"), 3).with(s("ANGLE"), 12).with(s("DAMAGE_MULTIPLIER"), .55)
                    .with(s("INCOMING_MULTIPLIER"), 1.25);
            default -> t;
        };
    }

    private static MartialCommandEldritchMasteryTuning dreadtide(MartialCommandEldritchMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("FLAT_DAMAGE"), 18);
            case 1 -> t.add(s("STACK_CAP"), 1, 5);
            case 2 -> t.with(s("LOCKOUT_TICKS"), 15);
            case 3 -> t.with(s("SPEED"), .03);
            case 4 -> t.with(s("INCOMING_MULTIPLIER"), .89);
            case 5 -> t.with(s("OUTGOING_MULTIPLIER"), 1.12).with(s("DURATION_TICKS"), 60);
            case 6 -> t.with(s("HEALTH_THRESHOLD"), .3).with(s("DELAY_TICKS"), 200);
            case 7 -> t.with(s("STACK_CAP"), 3).with(s("INCOMING_MULTIPLIER"), .8).with(s("SPEED"), 0);
            case 8 -> t.with(s("STACK_CAP"), 7).with(s("INCOMING_MULTIPLIER"), .95)
                    .with(s("SPEED"), 0).with(s("PER_STACK_MULTIPLIER"), .08);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("DURATION_TICKS"), 30, 250);
            case 1 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 2 -> t.add(s("INTERVAL_TICKS"), -1, 12);
            case 3 -> t.with(s("PER_STACK_MULTIPLIER"), .06);
            case 4 -> t.with(s("STATUS_DURATION_TICKS"), 30).with(s("STATUS_AMPLIFIER"), 1);
            case 5 -> t.with(s("RANGE"), 5).with(s("COUNT"), 2);
            case 6 -> t.with(s("FINAL_DAMAGE_MULTIPLIER"), 1.8).with(s("RADIUS"), 2.5).with(s("TARGET_CAP"), 6);
            case 7 -> t.multiply(s("DURATION_TICKS"), 2, 250).multiply(s("INTERVAL_TICKS"), .5, 12)
                    .with(s("DAMAGE_MULTIPLIER"), .55);
            case 8 -> t.with(s("DAMAGE_MULTIPLIER"), 1.6).with(s("INTERVAL_TICKS"), 8);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("INTERVAL_TICKS"), 70);
            case 1 -> t.with(s("SECONDARY_DURATION_TICKS"), 1100);
            case 2 -> t.with(s("CORRUPTION"), 40).with(s("OUTGOING_MULTIPLIER"), 1.1);
            case 3 -> t.with(s("SECONDARY_CORRUPTION"), 60).with(s("INCOMING_MULTIPLIER"), .92);
            case 4 -> t.with(s("COUNT"), 5);
            case 5 -> t.with(s("ABSORPTION"), 4).with(s("SEARCH_RADIUS"), 12)
                    .with(s("FLAT_DAMAGE"), 20).with(s("STATUS_DURATION_TICKS"), 200);
            case 6 -> t.with(s("FINAL_CORRUPTION"), 80).with(s("SPEED"), .7)
                    .with(s("HEAL_MULTIPLIER"), 1.08);
            case 7 -> t.with(s("PITY_CHANCE"), 75).with(s("DAMAGE_MULTIPLIER"), 1.25)
                    .with(s("HEIGHT"), 8);
            case 8 -> t.with(s("LOCKOUT_TICKS"), 200);
            default -> t;
        };
    }

    private static boolean matches(int profile, int branch, int slot, UniqueAbilityDefinition definition) {
        if (profile == 0 && branch == 1 && slot == 2 && definition == MartialCommandEldritchMasteryAbilities.WARG_MARK) return true;
        return definition == switch (profile) {
            case 0 -> branch == 0 ? MartialCommandEldritchMasteryAbilities.WARG_MARK
                    : branch == 1 ? MartialCommandEldritchMasteryAbilities.WARG_HUNT : MartialCommandEldritchMasteryAbilities.WARG_SANGUINE;
            case 1 -> branch == 0 ? MartialCommandEldritchMasteryAbilities.RIBBON_HEAVY
                    : branch == 1 ? MartialCommandEldritchMasteryAbilities.RIBBON_RUSH : MartialCommandEldritchMasteryAbilities.RIBBON_PROMISE;
            case 2 -> branch == 0 ? MartialCommandEldritchMasteryAbilities.RIFTMANE_HARRIER
                    : branch == 1 ? MartialCommandEldritchMasteryAbilities.RIFTMANE_RANK : MartialCommandEldritchMasteryAbilities.RIFTMANE_RIDER;
            case 3 -> branch == 0 ? MartialCommandEldritchMasteryAbilities.DAWN_LESSER
                    : branch == 1 ? MartialCommandEldritchMasteryAbilities.DAWN_CHORUS : MartialCommandEldritchMasteryAbilities.DAWN_DRAW;
            case 4 -> branch == 0 ? MartialCommandEldritchMasteryAbilities.DREAD_CLOAK
                    : branch == 1 ? MartialCommandEldritchMasteryAbilities.DREAD_ASSAULT : MartialCommandEldritchMasteryAbilities.DREAD_PACT;
            default -> null;
        };
    }

    private static MartialCommandEldritchMasteryTuning mode(MartialCommandEldritchMasteryTuning tuning, int bit) {
        return tuning.with(s("MODE"), tuning.integer(s("MODE"), 0) | bit);
    }

    private static MartialCommandEldritchMasteryTuning.Setting s(String name) {
        return MartialCommandEldritchMasteryTuning.Setting.valueOf(name);
    }
}
