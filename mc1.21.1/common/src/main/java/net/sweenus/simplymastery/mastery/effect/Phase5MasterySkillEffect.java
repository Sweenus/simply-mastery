package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.SimplyMastery;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.Phase5AbilityTuning;
import net.sweenus.simplyswords.api.ability.Phase5UniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityContext;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;

import java.util.List;
import java.util.Set;

final class Phase5MasterySkillEffect implements AbilitySkillEffectType {
    private static final Identifier ID = Identifier.of(SimplyMastery.MOD_ID, "phase5_mastery");

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
        if (!effect.parameters().keySet().equals(Set.of("kind"))) {
            errors.add(where + "phase5_mastery requires only kind");
        }
        int kind = effect.parameters().getOrDefault("kind", -1);
        if (kind < 0 || kind >= 162) errors.add(where + "kind must be between 0 and 161");
    }

    @Override
    public void tune(UniqueAbilityContext context, UniqueAbilityDefinition definition,
                     UniqueAbilityTuning.Builder tuning, MasteryProfile.Node node) {
        int kind = node.effect().parameters().getOrDefault("kind", -1);
        if (kind < 0 || kind >= 162 || !matches(kind / 27, definition)) return;
        int profile = kind / 27;
        int branch = kind % 27 / 9;
        int slot = kind % 9;
        Phase5AbilityTuning value = tuning.get(Phase5UniqueAbilities.TUNING);
        value = mode(value, 1 << (branch * 9 + slot));
        value = switch (profile) {
            case 0 -> hearthflame(value, branch, slot);
            case 1 -> emberblade(value, branch, slot);
            case 2 -> emberlash(value, branch, slot);
            case 3 -> flamewind(value, branch, slot);
            case 4 -> moltenEdge(value, branch, slot);
            case 5 -> soulPyre(value, branch, slot);
            default -> value;
        };
        tuning.set(Phase5UniqueAbilities.TUNING, value);
        if (definition.cooldownKey().isPresent()) {
            tuning.set(Phase5UniqueAbilities.COOLDOWN_TICKS,
                    value.integer(s("COOLDOWN_TICKS"), tuning.get(Phase5UniqueAbilities.COOLDOWN_TICKS)));
        }
    }

    private static Phase5AbilityTuning hearthflame(Phase5AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.add(s("RANGE"), 2, 10);
            case 2 -> t.multiply(s("PULL_STRENGTH"), 1.2, .16).add(s("MIN_LENGTH"), -.5, 2.5);
            case 3 -> t.with(s("FIRE_TICKS"), 40);
            case 4 -> t.with(s("SECONDARY_DAMAGE_MULTIPLIER"), 1.15);
            case 5 -> t.multiply(s("MAX_PRESSURE"), 1.2, 300).with(s("RADIUS"), 2.5);
            case 6 -> t.multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.25, 1);
            case 7 -> t.with(s("TARGET_CAP"), 8).multiply(s("DAMAGE_MULTIPLIER"), 1.2, 1)
                    .multiply(s("PULL_STRENGTH"), .7, .16);
            case 8 -> t.with(s("TARGET_CAP"), 1).multiply(s("DURATION_TICKS"), .6, 220)
                    .multiply(s("FINAL_DAMAGE_MULTIPLIER"), 2, 1);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("ABSORPTION"), 4).with(s("STATUS_DURATION_TICKS"), 80);
            case 1 -> t.with(s("DAMAGE_REDUCTION"), .3);
            case 2 -> t.with(s("STATUS_DURATION_TICKS"), 30);
            case 3 -> t.with(s("DAMAGE_REDUCTION"), .15).with(s("RANGE"), 6);
            case 4 -> t.with(s("ABSORPTION"), 2).with(s("LOCKOUT_TICKS"), 40);
            case 5 -> t.with(s("LOCKOUT_TICKS"), 20);
            case 6 -> t.with(s("ABSORPTION"), 4).with(s("STATUS_DURATION_TICKS"), 60).with(s("COUNT"), 3);
            case 7 -> t.with(s("STATUS_AMPLIFIER"), 1).with(s("SPEED"), .75);
            case 8 -> t.with(s("STATUS_AMPLIFIER"), 0).with(s("SPEED"), 1.2)
                    .multiply(s("RANGE"), .8, 10).multiply(s("FINAL_DAMAGE_MULTIPLIER"), .8, 1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.add(s("CHANCE"), 10, 50);
            case 1 -> t.add(s("DURATION_TICKS"), 80, 350);
            case 2 -> t.with(s("OUTGOING_MULTIPLIER"), 1.12).with(s("FIRE_TICKS"), 20);
            case 3 -> t.with(s("DURATION_TICKS"), 120).with(s("LOCKOUT_TICKS"), 60);
            case 4 -> t.multiply(s("SECONDARY_DAMAGE_MULTIPLIER"), 1.2, 1).add(s("RADIUS"), .5, 2.5);
            case 5 -> t.with(s("COUNT"), 2).with(s("RANGE"), 4).with(s("DURATION_TICKS"), 100);
            case 6 -> t.multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.3, 1).with(s("PULL_STRENGTH"), 1.25);
            case 7 -> t.with(s("DURATION_TICKS"), 60).with(s("SECONDARY_DAMAGE_MULTIPLIER"), 1.6)
                    .with(s("PULL_STRENGTH"), 0);
            case 8 -> t.with(s("COUNT"), 3).with(s("RANGE"), 5).with(s("GENERATION_MULTIPLIER"), .75);
            default -> t;
        };
    }

    private static Phase5AbilityTuning emberblade(Phase5AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1);
            case 1 -> t.multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.15, 1);
            case 2 -> t.with(s("COUNT"), 1).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .7).with(s("RANGE"), 12);
            case 3 -> t.with(s("OUTGOING_MULTIPLIER"), 1.2);
            case 4 -> t.with(s("DURATION_TICKS"), 60).with(s("PERIODIC_DAMAGE_MULTIPLIER"), .5);
            case 5 -> t.with(s("FIRE_TICKS"), 60).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .25)
                    .with(s("RADIUS"), 2).with(s("TARGET_CAP"), 6);
            case 6 -> t.multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.25, 1).with(s("FIRE_TICKS"), 80);
            case 7 -> t.add(s("DURATION_TICKS"), 20, 80).multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.6, 1);
            case 8 -> t.multiply(s("DURATION_TICKS"), .5, 80).add(s("COOLDOWN_TICKS"), -20, 60)
                    .multiply(s("FINAL_DAMAGE_MULTIPLIER"), .65, 1);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("STATUS_DURATION_TICKS"), 60);
            case 1 -> t.with(s("STATUS_DURATION_TICKS"), 80);
            case 2 -> t.with(s("SPEED"), 1.5).with(s("DURATION_TICKS"), 20);
            case 3 -> t.with(s("RANGE"), 6).with(s("OUTGOING_MULTIPLIER"), 1.15).with(s("LOCKOUT_TICKS"), 80);
            case 4 -> t.with(s("DAMAGE_REDUCTION"), .25).with(s("LOCKOUT_TICKS"), 8);
            case 5 -> t.with(s("STATUS_DURATION_TICKS"), 60).with(s("LOCKOUT_TICKS"), 100);
            case 6 -> t.with(s("STATUS_DURATION_TICKS"), 40);
            case 7 -> t.with(s("COUNT"), 0).with(s("RADIUS"), 0);
            case 8 -> t.with(s("SPEED"), 4).with(s("STATUS_DURATION_TICKS"), 40)
                    .multiply(s("DAMAGE_MULTIPLIER"), .8, 1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.add(s("CHANCE"), 10, 30);
            case 1 -> t.add(s("STATUS_DURATION_TICKS"), 40, 150);
            case 2 -> t.with(s("COUNT"), 2).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .35).with(s("RANGE"), 5);
            case 3 -> t.with(s("RANGE"), 10).with(s("OUTGOING_MULTIPLIER"), 1.2);
            case 4 -> t.with(s("COUNT"), 3).with(s("LOCKOUT_TICKS"), 100)
                    .with(s("RADIUS"), 3).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .4).with(s("TARGET_CAP"), 8);
            case 5 -> t.with(s("PERIODIC_DAMAGE_MULTIPLIER"), .05).with(s("FINAL_DAMAGE_MULTIPLIER"), .25)
                    .with(s("LOCKOUT_TICKS"), 100);
            case 6 -> t.with(s("RETARGET_RANGE"), 6).with(s("GENERATION_MULTIPLIER"), .5);
            case 7 -> t.with(s("COUNT"), 5).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .45)
                    .multiply(s("DAMAGE_MULTIPLIER"), .6, 1);
            case 8 -> t.with(s("STATUS_AMPLIFIER"), 1).with(s("STATUS_DURATION_TICKS"), 100)
                    .add(s("COOLDOWN_TICKS"), 80, 60);
            default -> t;
        };
    }

    private static Phase5AbilityTuning emberlash(Phase5AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.multiply(s("PER_STACK_MULTIPLIER"), 1.1, 1);
            case 1 -> t.with(s("STACK_CAP"), 6);
            case 2 -> t.add(s("DURATION_TICKS"), 60, 100);
            case 3 -> t.with(s("COUNT"), 3).with(s("FIRE_TICKS"), 60);
            case 4 -> t.with(s("RADIUS"), 3).with(s("TARGET_CAP"), 4);
            case 5 -> t.with(s("COUNT"), 3).with(s("LOCKOUT_TICKS"), 40);
            case 6 -> t.with(s("DAMAGE_REDUCTION"), .12).with(s("STATUS_DURATION_TICKS"), 60);
            case 7 -> t.multiply(s("PER_STACK_MULTIPLIER"), .75, 1).with(s("LOCKOUT_TICKS"), 60);
            case 8 -> t.with(s("STACK_CAP"), 3).multiply(s("PER_STACK_MULTIPLIER"), 1.45, 1).with(s("RADIUS"), 0);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("SPEED"), 1.2);
            case 1 -> t.add(s("COOLDOWN_TICKS"), -12, 80);
            case 2 -> t.with(s("ABSORPTION"), 4).with(s("STATUS_DURATION_TICKS"), 60);
            case 3 -> t.with(s("RADIUS"), 3).with(s("TARGET_CAP"), 6).with(s("STATUS_DURATION_TICKS"), 30);
            case 4 -> t.with(s("COUNT"), 2).with(s("LOCKOUT_TICKS"), 80);
            case 5 -> t.with(s("STATUS_DURATION_TICKS"), 40).with(s("STATUS_AMPLIFIER"), 1);
            case 6 -> t.with(s("LOCKOUT_TICKS"), 160).with(s("STATUS_DURATION_TICKS"), 60);
            case 7 -> t.with(s("SECONDARY_DAMAGE_MULTIPLIER"), .35).with(s("TARGET_CAP"), 6).with(s("FIRE_TICKS"), 60);
            case 8 -> t.multiply(s("HEAL_MULTIPLIER"), 1.5, 1).with(s("SPEED"), 0);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("LOCKOUT_TICKS"), 30);
            case 1 -> t.with(s("DURATION_TICKS"), 100).with(s("STACK_CAP"), 3);
            case 2 -> t.with(s("COUNT"), 3).with(s("OUTGOING_MULTIPLIER"), 1.25);
            case 3 -> t.with(s("RADIUS"), 2.5).with(s("PER_STACK_MULTIPLIER"), .08).with(s("TARGET_CAP"), 8);
            case 4 -> t.with(s("COUNT"), 1);
            case 5 -> t.with(s("STATUS_DURATION_TICKS"), 40).with(s("STATUS_AMPLIFIER"), 1);
            case 6 -> t.with(s("REFUND_TICKS"), 20);
            case 7 -> t.with(s("PER_STACK_MULTIPLIER"), .3);
            case 8 -> t.with(s("DURATION_TICKS"), 200).with(s("STACK_CAP"), 5).with(s("INCOMING_MULTIPLIER"), 1.04);
            default -> t;
        };
    }

    static Phase5AbilityTuning flamewind(Phase5AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.multiply(s("PERIODIC_DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.add(s("RADIUS"), .75, 5).with(s("TARGET_CAP"), 10);
            case 2 -> t.with(s("FIRE_TICKS"), 60);
            case 3 -> t.add(s("STATUS_DURATION_TICKS"), 20, 120).with(s("STATUS_AMPLIFIER"), 1);
            case 4 -> t.multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.25, 1);
            case 5 -> t.with(s("OUTGOING_MULTIPLIER"), .03).with(s("COUNT"), 5).with(s("RANGE"), 8);
            case 6 -> t.with(s("PULL_STRENGTH"), .2);
            case 7 -> t.with(s("INTERVAL_TICKS"), 20).with(s("PERIODIC_DAMAGE_MULTIPLIER"), .3)
                    .multiply(s("FINAL_DAMAGE_MULTIPLIER"), .6, 1);
            case 8 -> t.with(s("PERIODIC_DAMAGE_MULTIPLIER"), 0).with(s("RADIUS"), 5)
                    .multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.5, 1).with(s("TARGET_CAP"), 12);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("RANGE"), 4, 10);
            case 1 -> t.add(s("DURATION_TICKS"), -20, 101);
            case 2 -> t.add(s("DURATION_TICKS"), 40, 101).multiply(s("PERIODIC_DAMAGE_MULTIPLIER"), 1.08, 1);
            case 3 -> t.with(s("RETARGET_RANGE"), 6);
            case 4 -> t.add(s("SPREAD_CAP"), 1, 6);
            case 5 -> t.add(s("RANGE"), 2, 5).with(s("SEARCH_CAP"), 16);
            case 6 -> t.with(s("PERIODIC_DAMAGE_MULTIPLIER"), .8);
            case 7 -> t.with(s("GENERATION_CAP"), 3).with(s("GENERATION_MULTIPLIER"), 1.25);
            case 8 -> t.with(s("SPREAD_CAP"), 10).add(s("RANGE"), 3, 5).with(s("GENERATION_MULTIPLIER"), .8);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("REFUND_TICKS"), 6).with(s("COUNT"), 5);
            case 1 -> t.with(s("LOCKOUT_TICKS"), 10).with(s("SECONDARY_DAMAGE_MULTIPLIER"), 1.15);
            case 2 -> t.with(s("COUNT"), 3).with(s("STATUS_DURATION_TICKS"), 80).with(s("STATUS_AMPLIFIER"), 1);
            case 3 -> t.with(s("FINAL_DAMAGE_MULTIPLIER"), .7).with(s("LOCKOUT_TICKS"), 100)
                    .with(s("COOLDOWN_TICKS"), 100);
            case 4 -> t.with(s("PULL_STRENGTH"), .25);
            case 5 -> t.with(s("ABSORPTION"), 2).with(s("TARGET_CAP"), 8).with(s("STATUS_DURATION_TICKS"), 80);
            case 6 -> t.with(s("COUNT"), 3).with(s("REFUND_TICKS"), 25);
            case 7 -> t.with(s("COUNT"), 3).with(s("FINAL_DAMAGE_MULTIPLIER"), 1.4).add(s("COOLDOWN_TICKS"), 40, 350);
            case 8 -> t.with(s("FINAL_DAMAGE_MULTIPLIER"), 1).with(s("SPREAD_CAP"), 0)
                    .with(s("LOCKOUT_TICKS"), 160).with(s("COOLDOWN_TICKS"), 160);
            default -> t;
        };
    }

    private static Phase5AbilityTuning moltenEdge(Phase5AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("HEAT_GAIN"), 6);
            case 1 -> t.with(s("COUNT"), 7);
            case 2 -> t.with(s("INCOMING_MULTIPLIER"), .85);
            case 3 -> t.with(s("OUTGOING_MULTIPLIER"), 1.1);
            case 4 -> t.with(s("ABSORPTION"), 2).with(s("COUNT"), 20).with(s("STATUS_DURATION_TICKS"), 60);
            case 5 -> t.with(s("PERIODIC_DAMAGE_MULTIPLIER"), .6);
            case 6 -> t.with(s("FIRE_TICKS"), 60).with(s("OUTGOING_MULTIPLIER"), 1.12);
            case 7 -> t.with(s("HEAT_MAX"), 100).with(s("COUNT"), 75).with(s("OUTGOING_MULTIPLIER"), 1.35);
            case 8 -> t.with(s("HEAT_MAX"), 75).with(s("INCOMING_MULTIPLIER"), .5).with(s("OUTGOING_MULTIPLIER"), .35);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("VENT_DRAIN"), 1, 1);
            case 1 -> t.with(s("SPEED"), 1.15);
            case 2 -> t.add(s("RADIUS"), 1, 5).with(s("TARGET_CAP"), 16);
            case 3 -> t.with(s("PULL_STRENGTH"), 1.25).with(s("STATUS_DURATION_TICKS"), 60);
            case 4 -> t.with(s("REFUND_TICKS"), 1).with(s("COUNT"), 3);
            case 5 -> t.with(s("RANGE"), 5).with(s("PULL_STRENGTH"), 20);
            case 6 -> t.with(s("COUNT"), 3).with(s("HEAT_GAIN"), 15);
            case 7 -> t.with(s("INTERVAL_TICKS"), 8).multiply(s("DAMAGE_MULTIPLIER"), .65, 1);
            case 8 -> t.with(s("SPEED"), 0).with(s("STATUS_AMPLIFIER"), 1).multiply(s("DAMAGE_MULTIPLIER"), 1.8, 1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.add(s("RUPTURE_LENGTH"), 2, 7);
            case 1 -> t.add(s("RUPTURE_WIDTH"), .75, 2).with(s("TARGET_CAP"), 8);
            case 2 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1);
            case 3 -> t.multiply(s("RUPTURE_KNOCK_UP"), 1.4, .25);
            case 4 -> t.add(s("FIRE_TICKS"), 40, 40);
            case 5 -> t.with(s("COUNT"), 2).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .55);
            case 6 -> t.with(s("LOCKOUT_TICKS"), 40).with(s("PER_STACK_MULTIPLIER"), .1).with(s("STACK_CAP"), 3);
            case 7 -> t.with(s("RUPTURE_LENGTH"), 12).with(s("RUPTURE_WIDTH"), 3.5)
                    .multiply(s("DAMAGE_MULTIPLIER"), 1.7, 1).with(s("COOLDOWN_TICKS"), 2);
            case 8 -> t.with(s("COUNT"), 5).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .45);
            default -> t;
        };
    }

    private static Phase5AbilityTuning soulPyre(Phase5AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("DURATION_TICKS"), 100, 600);
            case 1 -> t.add(s("RADIUS"), 1, 3).with(s("TARGET_CAP"), 32);
            case 2 -> t.with(s("RADIUS_GROWTH"), 1.25);
            case 3 -> t.multiply(s("PER_STACK_MULTIPLIER"), 1.2, .05);
            case 4 -> t.with(s("COUNT"), 3).with(s("LOCKOUT_TICKS"), 80).with(s("STATUS_DURATION_TICKS"), 40);
            case 5 -> t.with(s("COUNT"), 5).with(s("PULL_STRENGTH"), .3);
            case 6 -> t.with(s("COUNT"), 3).with(s("ABSORPTION"), 2).with(s("STATUS_DURATION_TICKS"), 80);
            case 7 -> t.with(s("RADIUS_GROWTH"), 2).add(s("PULSE_COUNT"), 1, 10)
                    .multiply(s("DAMAGE_MULTIPLIER"), .75, 1);
            case 8 -> t.with(s("RADIUS"), 4).multiply(s("DAMAGE_MULTIPLIER"), 1.6, 1)
                    .multiply(s("INTERVAL_TICKS"), .8, 60);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.15, 1);
            case 1 -> t.with(s("WISP_COUNT"), 6);
            case 2 -> t.add(s("RANGE"), 4, 12).with(s("SEARCH_CAP"), 24);
            case 3 -> t.with(s("RETARGET_RANGE"), 5).with(s("COUNT"), 1);
            case 4 -> t.with(s("FIRE_TICKS"), 60).with(s("STATUS_DURATION_TICKS"), 80);
            case 5 -> t.with(s("OUTGOING_MULTIPLIER"), 1.2);
            case 6 -> t.with(s("WISP_COUNT"), 6).with(s("RADIUS"), 2.5)
                    .with(s("SECONDARY_DAMAGE_MULTIPLIER"), .4).with(s("TARGET_CAP"), 8);
            case 7 -> t.multiply(s("WISP_COUNT"), 2, 5).multiply(s("DAMAGE_MULTIPLIER"), .55, 1).with(s("TARGET_CAP"), 3);
            case 8 -> t.with(s("COUNT"), 1).with(s("PER_STACK_MULTIPLIER"), .25)
                    .with(s("STACK_CAP"), 10).with(s("TARGET_CAP"), 6);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("DAMAGE_REDUCTION"), .55);
            case 1 -> t.with(s("STATUS_DURATION_TICKS"), 60);
            case 2 -> t.add(s("STATUS_DURATION_TICKS"), 40, 600);
            case 3 -> t.with(s("COUNT"), 5).with(s("ABSORPTION"), 2).with(s("TARGET_CAP"), 8);
            case 4 -> t.add(s("COLLAPSE_DURATION_TICKS"), 20, 60).with(s("INTERVAL_TICKS"), 20)
                    .with(s("PERIODIC_DAMAGE_MULTIPLIER"), .2);
            case 5 -> t.with(s("PER_STACK_MULTIPLIER"), .12).with(s("STACK_CAP"), 10);
            case 6 -> t.with(s("COUNT"), 5).with(s("STATUS_DURATION_TICKS"), 80);
            case 7 -> t.with(s("COUNT"), 10).with(s("STATUS_AMPLIFIER"), 2).with(s("STATUS_DURATION_TICKS"), 60);
            case 8 -> t.with(s("DAMAGE_REDUCTION"), 0).with(s("RADIUS"), 8)
                    .multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.75, 1).with(s("TARGET_CAP"), 24);
            default -> t;
        };
    }

    private static boolean matches(int profile, UniqueAbilityDefinition definition) {
        return switch (profile) {
            case 0 -> definition == Phase5UniqueAbilities.HEARTHFLAME_CHAINS
                    || definition == Phase5UniqueAbilities.HEARTHFLAME_BRAND;
            case 1 -> definition == Phase5UniqueAbilities.EMBERBLADE_SHRAPNEL;
            case 2 -> definition == Phase5UniqueAbilities.EMBERLASH_SMOULDER
                    || definition == Phase5UniqueAbilities.EMBERLASH_CAUTERY;
            case 3 -> definition == Phase5UniqueAbilities.FLAMEWIND_SEED;
            case 4 -> definition == Phase5UniqueAbilities.MOLTEN_EDGE_HEAT
                    || definition == Phase5UniqueAbilities.MOLTEN_EDGE_VENT
                    || definition == Phase5UniqueAbilities.MOLTEN_EDGE_RUPTURE;
            case 5 -> definition == Phase5UniqueAbilities.SOUL_PYRE_TETHER
                    || definition == Phase5UniqueAbilities.SOUL_PYRE_WISP;
            default -> false;
        };
    }

    private static Phase5AbilityTuning mode(Phase5AbilityTuning tuning, int bit) {
        return tuning.with(s("MODE"), tuning.integer(s("MODE"), 0) | bit);
    }

    private static Phase5AbilityTuning.Setting s(String name) {
        return Phase5AbilityTuning.Setting.valueOf(name);
    }
}
