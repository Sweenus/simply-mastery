package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.SimplyMastery;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.Phase8AbilityTuning;
import net.sweenus.simplyswords.api.ability.Phase8UniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityContext;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;

import java.util.List;
import java.util.Set;

final class Phase8MasterySkillEffect implements AbilitySkillEffectType {
    private static final Identifier ID = Identifier.of(SimplyMastery.MOD_ID, "phase8_mastery");

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
        if (!effect.parameters().keySet().equals(Set.of("kind"))) {
            errors.add(where + "phase8_mastery requires only kind");
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
        Phase8AbilityTuning value = mode(tuning.get(Phase8UniqueAbilities.TUNING), 1 << (branch * 9 + slot));
        value = switch (profile) {
            case 0 -> plague(value, branch, slot);
            case 1 -> soulkeeper(value, branch, slot);
            case 2 -> soulstealer(value, branch, slot);
            case 3 -> twisted(value, branch, slot);
            case 4 -> shadowsting(value, branch, slot);
            case 5 -> bloodwake(value, branch, slot);
            default -> value;
        };
        if (profile == 5 && branch == 1 && value.flag(1 << 17) && context.actor().isSneaking()) {
            value = value.with(s("COOLDOWN_TICKS"), 0);
        }
        tuning.set(Phase8UniqueAbilities.TUNING, value);
        if (definition.cooldownKey().isPresent()) {
            tuning.set(Phase8UniqueAbilities.COOLDOWN_TICKS,
                    value.integer(s("COOLDOWN_TICKS"), tuning.get(Phase8UniqueAbilities.COOLDOWN_TICKS)));
        }
    }

    private static Phase8AbilityTuning plague(Phase8AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("CHANCE"), 10, 55);
            case 1 -> t.add(s("DURATION_TICKS"), 40, 120);
            case 2 -> t.add(s("FEVER"), 1, 2);
            case 3 -> t.add(s("FEVER"), 1, 2).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .6);
            case 4 -> t.with(s("STATUS_DURATION_TICKS"), 30).with(s("STATUS_AMPLIFIER"), 0);
            case 5 -> t.with(s("STATUS_DURATION_TICKS"), 80).with(s("LOCKOUT_TICKS"), 100);
            case 6 -> t.with(s("HEALTH_THRESHOLD"), .8).with(s("OUTGOING_MULTIPLIER"), 1.12);
            case 7 -> t.with(s("SECONDARY_DAMAGE_MULTIPLIER"), 1).add(s("FEVER"), 3, 2)
                    .multiply(s("DAMAGE_MULTIPLIER"), .8, 1);
            case 8 -> t.with(s("FEVER"), 64).with(s("LOCKOUT_TICKS"), 200);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("FLAT_DAMAGE"), 10, 0);
            case 1 -> t.add(s("RADIUS"), .75, 5);
            case 2 -> t.with(s("WINDUP_TICKS"), 4);
            case 3 -> t.with(s("HEALTH_THRESHOLD"), .4).with(s("OUTGOING_MULTIPLIER"), 1.15);
            case 4 -> t.with(s("STATUS_DURATION_TICKS"), 40).with(s("STATUS_AMPLIFIER"), 0);
            case 5 -> t.with(s("PER_STACK_MULTIPLIER"), .05).with(s("STACK_CAP"), 5);
            case 6 -> t.with(s("FINAL_DAMAGE_MULTIPLIER"), 1.3).with(s("SECONDARY_RADIUS"), 1.25);
            case 7 -> t.with(s("COUNT"), 2).with(s("DELAY_TICKS"), 6)
                    .with(s("DAMAGE_MULTIPLIER"), .65).multiply(s("RADIUS"), .8, 5);
            case 8 -> t.with(s("TARGET_CAP"), 1).with(s("DAMAGE_MULTIPLIER"), 1.8);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.add(s("FEVER"), 1, 2);
            case 1 -> t.add(s("RANGE"), 1, 5);
            case 2 -> t.add(s("COUNT"), 1, 6);
            case 3 -> t.with(s("SECONDARY_DAMAGE_MULTIPLIER"), .65);
            case 4 -> t.with(s("RADIUS"), 2.5).with(s("STATUS_DURATION_TICKS"), 30).with(s("TARGET_CAP"), 6);
            case 5 -> t.with(s("DELAY_TICKS"), 40).with(s("FEVER"), 1).with(s("COUNT"), 1);
            case 6 -> t.add(s("COUNT"), 2, 6).with(s("TARGET_CAP"), 9);
            case 7 -> t.with(s("DELAY_TICKS"), 20).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .5)
                    .add(s("COUNT"), -2, 6);
            case 8 -> t.with(s("RADIUS"), 7).with(s("FEVER"), 3).with(s("TARGET_CAP"), 12)
                    .with(s("COUNT"), 0);
            default -> t;
        };
    }

    private static Phase8AbilityTuning soulkeeper(Phase8AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("FLAT_DAMAGE"), 10, 0);
            case 1 -> t.add(s("RADIUS"), .35, 2.65);
            case 2 -> t.with(s("STATUS_DURATION_TICKS"), 40);
            case 3 -> t.with(s("LOCKOUT_TICKS"), 20).with(s("SECONDARY_DAMAGE_MULTIPLIER"), 1.2);
            case 4 -> t.with(s("OUTGOING_MULTIPLIER"), 1.08).with(s("DURATION_TICKS"), 40);
            case 5 -> t.with(s("DELAY_TICKS"), 12).with(s("COUNT"), 2);
            case 6 -> t.with(s("COUNT"), 5).with(s("PER_STACK_MULTIPLIER"), .5);
            case 7 -> t.multiply(s("RADIUS"), .7, 2.65).with(s("DAMAGE_MULTIPLIER"), 1.55);
            case 8 -> t.multiply(s("RADIUS"), 1.6, 2.65).with(s("TARGET_CAP"), 3)
                    .with(s("DAMAGE_MULTIPLIER"), .75).with(s("SECONDARY_RADIUS"), 1.5)
                    .with(s("SECONDARY_DAMAGE_MULTIPLIER"), .6);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.multiply(s("SPEED"), 1.25, .2);
            case 1 -> t.multiply(s("PER_STACK_MULTIPLIER"), .8, .15);
            case 2 -> t.add(s("STACK_CAP"), .75, 6);
            case 3 -> t.with(s("DAMAGE_MULTIPLIER"), 1.03).with(s("FINAL_DAMAGE_MULTIPLIER"), 1.12);
            case 4 -> t.with(s("STATUS_DURATION_TICKS"), 40);
            case 5 -> t.with(s("DURATION_TICKS"), 60);
            case 6 -> t.with(s("PULL_STRENGTH"), .25).with(s("TARGET_CAP"), 8).with(s("INTERVAL_TICKS"), 10);
            case 7 -> t.with(s("SPEED"), 3).with(s("STACK_CAP"), 4);
            case 8 -> t.with(s("RADIUS"), 4).with(s("PER_STACK_MULTIPLIER"), .18);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.add(s("DURATION_TICKS"), 60, 300);
            case 1 -> t.multiply(s("COOLDOWN_TICKS"), .9, 800);
            case 2 -> t.add(s("COUNT"), 1, 4).with(s("STATUS_DURATION_TICKS"), 80);
            case 3 -> t.with(s("ABSORPTION"), 4).with(s("STATUS_DURATION_TICKS"), 80);
            case 4 -> t.with(s("RANGE"), 1).with(s("INTERVAL_TICKS"), 40);
            case 5 -> t.with(s("RADIUS"), 5).with(s("STATUS_DURATION_TICKS"), 20).with(s("TARGET_CAP"), 4);
            case 6 -> t.with(s("LOCKOUT_TICKS"), 60).with(s("SECONDARY_DURATION_TICKS"), 80);
            case 7 -> t.with(s("COUNT"), 6).with(s("DURATION_TICKS"), 180).with(s("DAMAGE_MULTIPLIER"), .7);
            case 8 -> t.with(s("COUNT"), 3).with(s("DURATION_TICKS"), 220).with(s("ABSORPTION"), 4)
                    .with(s("INTERVAL_TICKS"), 30);
            default -> t;
        };
    }

    private static Phase8AbilityTuning soulstealer(Phase8AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("CHANCE"), 8, 25);
            case 1 -> t.add(s("STACK_CAP"), 2, 5);
            case 2 -> t.add(s("COUNT"), 1, 2);
            case 3 -> t.with(s("INTERVAL_TICKS"), 3).with(s("FEVER"), 1);
            case 4 -> t.with(s("INCOMING_MULTIPLIER"), .85).with(s("LOCKOUT_TICKS"), 40);
            case 5 -> t.with(s("DURATION_TICKS"), 80).with(s("COUNT"), 2);
            case 6 -> t.with(s("ABSORPTION"), 4).with(s("STATUS_DURATION_TICKS"), 40).with(s("DURATION_TICKS"), 120);
            case 7 -> t.add(s("STACK_CAP"), -2, 5).with(s("CHANCE"), 100);
            case 8 -> t.add(s("STACK_CAP"), 5, 5).with(s("COUNT"), 3).multiply(s("CHANCE"), .5, 25);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("RANGE"), 2, 20);
            case 1 -> t.add(s("ANGLE"), 20, 0);
            case 2 -> t.multiply(s("SEARCH_CAP"), 1.5, 4).with(s("TARGET_CAP"), 24);
            case 3 -> t.with(s("STATUS_DURATION_TICKS"), 20);
            case 4 -> t.with(s("DURATION_TICKS"), 30).with(s("STATUS_AMPLIFIER"), 2);
            case 5 -> t.with(s("RANGE"), 3);
            case 6 -> t.with(s("REFUND_TICKS"), 80);
            case 7 -> t.with(s("RANGE"), 8).with(s("DAMAGE_MULTIPLIER"), 3);
            case 8 -> t.with(s("DELAY_TICKS"), 12).with(s("STATUS_DURATION_TICKS"), 40)
                    .with(s("STATUS_AMPLIFIER"), 1).multiply(s("DAMAGE_MULTIPLIER"), .75, 1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.add(s("PER_STACK_MULTIPLIER"), .08, 1);
            case 1 -> t.multiply(s("SPELL_MULTIPLIER"), 1.1, 1);
            case 2 -> t.with(s("ARMOR_IGNORE"), .1);
            case 3 -> t.with(s("STATUS_DURATION_TICKS"), 60).with(s("STATUS_AMPLIFIER"), 0);
            case 4 -> t.with(s("DAMAGE_MULTIPLIER"), 1.25);
            case 5 -> t.with(s("REFUND_TICKS"), 1);
            case 6 -> t.with(s("COUNT"), 2).with(s("REFUND_TICKS"), 40);
            case 7 -> t.with(s("PER_STACK_MULTIPLIER"), .2).with(s("STACK_CAP"), 12)
                    .with(s("INCOMING_MULTIPLIER"), .5);
            case 8 -> t.with(s("COUNT"), 3).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .8);
            default -> t;
        };
    }

    private static Phase8AbilityTuning twisted(Phase8AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("CHANCE"), 8, 25);
            case 1 -> t.add(s("DURATION_TICKS"), 30, 80);
            case 2 -> t.with(s("PER_STACK_MULTIPLIER"), .11);
            case 3 -> t.with(s("COUNT"), 5);
            case 4 -> t.with(s("STACK_CAP"), 8).with(s("DURATION_TICKS"), 40)
                    .with(s("DURATION_CAP_TICKS"), 120);
            case 5 -> t.with(s("STATUS_DURATION_TICKS"), 20);
            case 6 -> t.add(s("STACK_CAP"), 3, 15).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .05);
            case 7 -> t.with(s("STACK_CAP"), 10).with(s("LOCKOUT_TICKS"), 40);
            case 8 -> t.with(s("STACK_CAP"), 20).with(s("HEALTH_THRESHOLD"), .5)
                    .with(s("DAMAGE_MULTIPLIER"), 2).with(s("DURATION_TICKS"), 80);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.add(s("RADIUS"), .4, 3);
            case 2 -> t.add(s("INTERVAL_TICKS"), -1, 4);
            case 3 -> t.add(s("KNOCKBACK"), .15, .5);
            case 4 -> t.with(s("DAMAGE_MULTIPLIER"), 1.15).with(s("PULL_STRENGTH"), .2).with(s("COUNT"), 2);
            case 5 -> t.with(s("LOCKOUT_TICKS"), 60).with(s("OUTGOING_MULTIPLIER"), 1.1);
            case 6 -> t.with(s("COUNT"), 3).with(s("DELAY_TICKS"), 4).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .6);
            case 7 -> t.with(s("TARGET_CAP"), 1).with(s("RANGE"), 5).with(s("DAMAGE_MULTIPLIER"), 1.9);
            case 8 -> t.multiply(s("RADIUS"), 1.75, 3).with(s("TARGET_CAP"), 16)
                    .multiply(s("DAMAGE_MULTIPLIER"), .65, 1).with(s("KNOCKBACK"), 0);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.add(s("DURATION_TICKS"), 30, 80);
            case 1 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.15, 1);
            case 2 -> t.multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.2, 1);
            case 3 -> t.add(s("RADIUS"), .5, 3);
            case 4 -> t.add(s("KNOCKBACK"), .2, .5);
            case 5 -> t.with(s("COUNT"), 4).with(s("DELAY_TICKS"), 20);
            case 6 -> t.with(s("LOCKOUT_TICKS"), 20).with(s("DAMAGE_MULTIPLIER"), 1.2);
            case 7 -> t.with(s("RADIUS"), 5).with(s("TARGET_CAP"), 16).with(s("DURATION_TICKS"), 40);
            case 8 -> t.with(s("COUNT"), 3).with(s("DAMAGE_MULTIPLIER"), .55).with(s("RADIUS"), 0);
            default -> t;
        };
    }

    private static Phase8AbilityTuning shadowsting(Phase8AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("CHANCE"), 8, 25);
            case 1 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1);
            case 2 -> t.with(s("DELAY_TICKS"), 2);
            case 3 -> t.with(s("DURATION_TICKS"), 60).with(s("OUTGOING_MULTIPLIER"), 1.08);
            case 4 -> t.with(s("STATUS_DURATION_TICKS"), 12);
            case 5 -> t.with(s("COUNT"), 4).with(s("DELAY_TICKS"), 5).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .5);
            case 6 -> t.with(s("REFUND_TICKS"), 20);
            case 7 -> t.with(s("COUNT"), 3).with(s("DAMAGE_MULTIPLIER"), .45).add(s("CHANCE"), -15, 25);
            case 8 -> t.with(s("DAMAGE_MULTIPLIER"), 1.25).with(s("LOCKOUT_TICKS"), 60);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("DURATION_TICKS"), 20, 100);
            case 1 -> t.add(s("INTERVAL_TICKS"), -1, 8);
            case 2 -> t.add(s("RADIUS"), 2, 10);
            case 3 -> t.with(s("PER_STACK_MULTIPLIER"), .05).with(s("FINAL_DAMAGE_MULTIPLIER"), 1.25);
            case 4 -> t.with(s("HEALTH_THRESHOLD"), .4);
            case 5 -> t.with(s("STATUS_DURATION_TICKS"), 20);
            case 6 -> t.with(s("RADIUS"), 3).with(s("DAMAGE_MULTIPLIER"), .6).with(s("TARGET_CAP"), 8);
            case 7 -> t.add(s("DURATION_TICKS"), 60, 100).add(s("INTERVAL_TICKS"), 2, 8)
                    .with(s("COUNT"), 2).with(s("DAMAGE_MULTIPLIER"), .65);
            case 8 -> t.with(s("DURATION_TICKS"), 50).with(s("TARGET_CAP"), 1)
                    .with(s("DAMAGE_MULTIPLIER"), 1.5);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.add(s("RANGE"), 1, 3);
            case 1 -> t.with(s("SECONDARY_RADIUS"), 2.5).with(s("STATUS_DURATION_TICKS"), 20).with(s("SEARCH_CAP"), 6);
            case 2 -> t.with(s("SECONDARY_RADIUS"), 3).with(s("LOCKOUT_TICKS"), 40).with(s("SEARCH_CAP"), 6);
            case 3 -> t.with(s("SECONDARY_DURATION_TICKS"), 6);
            case 4 -> t.with(s("STATUS_DURATION_TICKS"), 25).with(s("STATUS_AMPLIFIER"), 1);
            case 5 -> t.with(s("SECONDARY_DURATION_TICKS"), 40).with(s("STATUS_AMPLIFIER"), 1);
            case 6 -> t.with(s("COUNT"), 3).with(s("ABSORPTION"), 4).with(s("SEARCH_CAP"), 12);
            case 7 -> t.with(s("SECONDARY_DURATION_TICKS"), 60).with(s("SECONDARY_RADIUS"), 0);
            case 8 -> t.with(s("SECONDARY_DURATION_TICKS"), 0).with(s("STATUS_DURATION_TICKS"), 30)
                    .with(s("STATUS_AMPLIFIER"), 2).with(s("COUNT"), 2);
            default -> t;
        };
    }

    private static Phase8AbilityTuning bloodwake(Phase8AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("COUNT"), 3).with(s("FEVER"), 1);
            case 1 -> t.add(s("RADIUS"), .5, 4);
            case 2 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1);
            case 3 -> t.add(s("DURATION_TICKS"), 40, 100);
            case 4 -> t.with(s("COUNT"), 2).with(s("FEVER"), 1);
            case 5 -> t.with(s("HEALTH_THRESHOLD"), .35).with(s("DAMAGE_MULTIPLIER"), 1.25);
            case 6 -> t.with(s("DELAY_TICKS"), 8).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .55).with(s("TARGET_CAP"), 4);
            case 7 -> t.with(s("RANGE"), 7).with(s("ANGLE"), 0).with(s("TARGET_CAP"), 12)
                    .with(s("DAMAGE_MULTIPLIER"), 1.35).with(s("FEVER"), 0);
            case 8 -> t.multiply(s("RADIUS"), .65, 4).with(s("COUNT"), 2).with(s("ABSORPTION"), 8)
                    .multiply(s("DAMAGE_MULTIPLIER"), .7, 1);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("COOLDOWN_TICKS"), -2, 20);
            case 1 -> t.add(s("RANGE"), 3, 10);
            case 2 -> t.add(s("TARGET_CAP"), 2, 6);
            case 3 -> t.add(s("WINDUP_TICKS"), -6, 24).multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 4 -> t.add(s("COUNT"), 2, 8).with(s("TARGET_CAP"), 12);
            case 5 -> t.add(s("INTERVAL_TICKS"), -3, 14);
            case 6 -> t.with(s("COUNT"), 3).with(s("DURATION_TICKS"), 300).with(s("FEVER"), 1);
            case 7 -> t.with(s("FEVER"), 1).multiply(s("DAMAGE_MULTIPLIER"), .85, 1).add(s("COOLDOWN_TICKS"), 30, 20);
            case 8 -> t.with(s("FEVER"), 1).multiply(s("DAMAGE_MULTIPLIER"), .8, 1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.add(s("DURATION_TICKS"), 100, 600);
            case 1 -> t.add(s("STATUS_AMPLIFIER"), 1, 0);
            case 2 -> t.add(s("INTERVAL_TICKS"), -8, 40);
            case 3 -> t.add(s("COUNT"), 1, 2);
            case 4 -> t.with(s("OUTGOING_MULTIPLIER"), 1);
            case 5 -> t.with(s("DAMAGE_MULTIPLIER"), 1.08);
            case 6 -> t.with(s("TARGET_CAP"), 1).with(s("DURATION_TICKS"), 800);
            case 7 -> t.multiply(s("RADIUS"), 1.6, 4).add(s("DURATION_TICKS"), 200, 600).with(s("COUNT"), 0);
            case 8 -> t.multiply(s("RADIUS"), .6, 4).multiply(s("COUNT"), 2, 2).with(s("ABSORPTION"), 4)
                    .with(s("INTERVAL_TICKS"), 80).with(s("STATUS_AMPLIFIER"), 0);
            default -> t;
        };
    }

    private static boolean matches(int profile, int branch, UniqueAbilityDefinition definition) {
        return switch (profile) {
            case 0 -> definition == switch (branch) {
                case 0 -> Phase8UniqueAbilities.PLAGUE_PESTILENCE;
                case 1 -> Phase8UniqueAbilities.PLAGUE_DEATH_KNELL;
                default -> Phase8UniqueAbilities.PLAGUE_OUTBREAK;
            };
            case 1 -> definition == switch (branch) {
                case 0 -> Phase8UniqueAbilities.SOULKEEPER_LANTERNS;
                case 1 -> Phase8UniqueAbilities.SOULKEEPER_VELOCITY;
                default -> Phase8UniqueAbilities.SOULKEEPER_CONCLAVE;
            };
            case 2 -> branch == 0 ? definition == Phase8UniqueAbilities.SOULSTEALER_DEBT
                    : definition == Phase8UniqueAbilities.SOULSTEALER_REAP;
            case 3 -> definition == switch (branch) {
                case 0 -> Phase8UniqueAbilities.TWISTED_FEROCITY;
                case 1 -> Phase8UniqueAbilities.TWISTED_CRESCENDO;
                default -> Phase8UniqueAbilities.TWISTED_FINALE;
            };
            case 4 -> branch == 0 ? definition == Phase8UniqueAbilities.SHADOW_ECHO
                    : definition == Phase8UniqueAbilities.SHADOW_DANCE;
            case 5 -> definition == switch (branch) {
                case 0 -> Phase8UniqueAbilities.BLOOD_BURST;
                case 1 -> Phase8UniqueAbilities.BLOOD_RITES;
                default -> Phase8UniqueAbilities.BLOOD_GROUND;
            };
            default -> false;
        };
    }

    private static Phase8AbilityTuning mode(Phase8AbilityTuning tuning, int bit) {
        return tuning.with(s("MODE"), tuning.integer(s("MODE"), 0) | bit);
    }

    private static Phase8AbilityTuning.Setting s(String name) {
        return Phase8AbilityTuning.Setting.valueOf(name);
    }
}
