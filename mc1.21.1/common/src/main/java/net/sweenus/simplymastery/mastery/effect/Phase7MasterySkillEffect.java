package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.SimplyMastery;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.Phase7AbilityTuning;
import net.sweenus.simplyswords.api.ability.Phase7UniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityContext;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;

import java.util.List;
import java.util.Set;

final class Phase7MasterySkillEffect implements AbilitySkillEffectType {
    private static final Identifier ID = Identifier.of(SimplyMastery.MOD_ID, "phase7_mastery");

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
        if (!effect.parameters().keySet().equals(Set.of("kind"))) {
            errors.add(where + "phase7_mastery requires only kind");
        }
        int kind = effect.parameters().getOrDefault("kind", -1);
        if (kind < 0 || kind >= 108) errors.add(where + "kind must be between 0 and 107");
    }

    @Override
    public void tune(UniqueAbilityContext context, UniqueAbilityDefinition definition,
                     UniqueAbilityTuning.Builder tuning, MasteryProfile.Node node) {
        int kind = node.effect().parameters().getOrDefault("kind", -1);
        if (kind < 0 || kind >= 108) return;
        int profile = kind / 27;
        int branch = kind % 27 / 9;
        int slot = kind % 9;
        if (!matches(profile, branch, slot, definition)) return;
        Phase7AbilityTuning value = mode(tuning.get(Phase7UniqueAbilities.TUNING), 1 << (branch * 9 + slot));
        value = switch (profile) {
            case 0 -> bramblethorn(value, branch, slot);
            case 1 -> waxweaver(value, branch, slot);
            case 2 -> hiveheart(value, branch, slot);
            case 3 -> chompolotl(value, branch, slot);
            default -> value;
        };
        tuning.set(Phase7UniqueAbilities.TUNING, value);
        if (definition.cooldownKey().isPresent()) {
            tuning.set(Phase7UniqueAbilities.COOLDOWN_TICKS,
                    value.integer(s("COOLDOWN_TICKS"), tuning.get(Phase7UniqueAbilities.COOLDOWN_TICKS)));
        }
    }

    private static Phase7AbilityTuning bramblethorn(Phase7AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("RANGE"), 4, 16);
            case 1 -> t.add(s("RADIUS"), 1.5, 6).with(s("TARGET_CAP"), 6);
            case 2 -> t.add(s("INTERVAL_TICKS"), -3, 12);
            case 3 -> t.add(s("DURATION_TICKS"), 20, 60);
            case 4 -> t.multiply(s("PULL_STRENGTH"), 1.2, .22);
            case 5 -> t.with(s("SECONDARY_DAMAGE_MULTIPLIER"), .42);
            case 6 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.2, 1).multiply(s("SPEED"), 1.15, 1);
            case 7 -> t.with(s("TARGET_CAP"), 10).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .25)
                    .with(s("PULL_STRENGTH"), 0).with(s("FINAL_DAMAGE_MULTIPLIER"), 0);
            case 8 -> t.with(s("TARGET_CAP"), 1).multiply(s("SPEED"), 1.4, 1)
                    .multiply(s("DAMAGE_MULTIPLIER"), 2.25, 1);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("DURATION_TICKS"), 40, 60);
            case 1 -> t.add(s("COOLDOWN_TICKS"), -2, 8);
            case 2 -> t.add(s("RANGE"), 2, 10);
            case 3 -> t.add(s("WIDTH"), .25, .7).multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 4 -> t.add(s("STATUS_DURATION_TICKS"), 20, 30).with(s("STATUS_AMPLIFIER"), 1);
            case 5 -> t.with(s("TARGET_CAP"), 4);
            case 6 -> t.with(s("COUNT"), 3).with(s("LOCKOUT_TICKS"), 100)
                    .with(s("FINAL_DAMAGE_MULTIPLIER"), 1.35);
            case 7 -> t.with(s("COUNT"), 2).with(s("SECONDARY_DAMAGE_MULTIPLIER"), .55)
                    .with(s("STATUS_DURATION_TICKS"), 0);
            case 8 -> t.with(s("TARGET_CAP"), 1).multiply(s("DAMAGE_MULTIPLIER"), 1.8, 1)
                    .with(s("DURATION_TICKS"), 30);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("ABSORPTION"), 4).with(s("STATUS_DURATION_TICKS"), 80);
            case 1 -> t.with(s("COUNT"), 3).with(s("INCOMING_MULTIPLIER"), .88);
            case 2 -> t.with(s("PER_STACK_MULTIPLIER"), .2).with(s("LOCKOUT_TICKS"), 30);
            case 3 -> t.with(s("ABSORPTION"), 2).with(s("STACK_CAP"), 8);
            case 4 -> t.with(s("OUTGOING_MULTIPLIER"), .9);
            case 5 -> t.with(s("COUNT"), 6).with(s("STATUS_DURATION_TICKS"), 100);
            case 6 -> t.with(s("REFUND_TICKS"), 12).with(s("STACK_CAP"), 48);
            case 7 -> t.multiply(s("DURATION_TICKS"), 1.5, 60).with(s("STATUS_AMPLIFIER"), 1)
                    .multiply(s("SECONDARY_DAMAGE_MULTIPLIER"), .7, .35);
            case 8 -> t.with(s("RADIUS"), 2).with(s("FINAL_DAMAGE_MULTIPLIER"), .5);
            default -> t;
        };
    }

    private static Phase7AbilityTuning waxweaver(Phase7AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("RANGE"), 3, 12);
            case 1 -> t.add(s("DURATION_TICKS"), 20, 120);
            case 2 -> t.add(s("WIDTH"), 2, 10).with(s("TARGET_CAP"), 10);
            case 3 -> t.with(s("INTERVAL_TICKS"), 8);
            case 4 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1).add(s("FIRE_TICKS"), 20, 80);
            case 5 -> t.add(s("RADIUS"), .75, 4).with(s("TARGET_CAP"), 12);
            case 6 -> t.with(s("PER_STACK_MULTIPLIER"), .08).with(s("INTERVAL_TICKS"), 10);
            case 7 -> t.with(s("DURATION_TICKS"), 200).multiply(s("DAMAGE_MULTIPLIER"), .6, 1)
                    .with(s("OUTGOING_MULTIPLIER"), .75);
            case 8 -> t.with(s("DURATION_TICKS"), 60).multiply(s("DAMAGE_MULTIPLIER"), 1.9, 1)
                    .multiply(s("WIDTH"), .5, 10);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("DURATION_TICKS"), 20, 60);
            case 1 -> t.with(s("STACK_CAP"), 4);
            case 2 -> t.with(s("FIRE_TICKS"), 60);
            case 3 -> t.with(s("SPEED"), 1.08);
            case 4 -> t.with(s("REFUND_TICKS"), 4).with(s("STACK_CAP"), 24).with(s("LOCKOUT_TICKS"), 80);
            case 5 -> t.with(s("STATUS_DURATION_TICKS"), 30);
            case 6 -> t.with(s("FINAL_DAMAGE_MULTIPLIER"), 1.3).with(s("LOCKOUT_TICKS"), 120);
            case 7 -> t.with(s("STACK_CAP"), 6).with(s("PER_STACK_MULTIPLIER"), 1.5)
                    .with(s("LOCKOUT_TICKS"), 30);
            case 8 -> t.with(s("STACK_CAP"), 1).with(s("DURATION_TICKS"), 160).with(s("REFUND_TICKS"), 0);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.add(s("STATUS_DURATION_TICKS"), 40, 100);
            case 1 -> t.add(s("COOLDOWN_TICKS"), -100, 1200);
            case 2 -> t.with(s("ABSORPTION"), 4).with(s("STATUS_DURATION_TICKS"), 60);
            case 3 -> t.with(s("CHANCE"), 35).with(s("INTERVAL_TICKS"), 40)
                    .with(s("LOCKOUT_TICKS"), 200);
            case 4 -> t.with(s("RADIUS"), 4).with(s("FIRE_TICKS"), 80).with(s("TARGET_CAP"), 8);
            case 5 -> t.with(s("INCOMING_MULTIPLIER"), .85);
            case 6 -> t.with(s("ABSORPTION"), 6).with(s("DURATION_TICKS"), 100);
            case 7 -> t.with(s("HEALTH_THRESHOLD"), .5).with(s("STATUS_DURATION_TICKS"), 100)
                    .add(s("COOLDOWN_TICKS"), 400, 1200);
            case 8 -> t.with(s("STATUS_DURATION_TICKS"), 0).with(s("RADIUS"), 6)
                    .with(s("FINAL_DAMAGE_MULTIPLIER"), 1.25).with(s("TARGET_CAP"), 16);
            default -> t;
        };
    }

    private static Phase7AbilityTuning hiveheart(Phase7AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1);
            case 1 -> t.add(s("COOLDOWN_TICKS"), -10, 60);
            case 2 -> t.add(s("RANGE"), 3, 16);
            case 3 -> t.with(s("STATUS_DURATION_TICKS"), 40);
            case 4 -> t.with(s("OUTGOING_MULTIPLIER"), 1.15).with(s("DURATION_TICKS"), 80);
            case 5 -> t.with(s("COUNT"), 2).with(s("INTERVAL_TICKS"), 3)
                    .with(s("SECONDARY_DAMAGE_MULTIPLIER"), .6);
            case 6 -> t.with(s("REFUND_TICKS"), 20);
            case 7 -> t.with(s("HEALTH_THRESHOLD"), .35).multiply(s("DAMAGE_MULTIPLIER"), 2.2, 1)
                    .multiply(s("COOLDOWN_TICKS"), 2, 60);
            case 8 -> t.with(s("CHANCE"), 100).with(s("DAMAGE_MULTIPLIER"), .35).with(s("TARGET_CAP"), 4);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("COUNT"), 2, 8).with(s("TARGET_CAP"), 12);
            case 1 -> t.add(s("RADIUS"), 2, 8).with(s("SEARCH_CAP"), 12);
            case 2 -> t.add(s("DURATION_TICKS"), 60, 240);
            case 3 -> t.with(s("INTERVAL_TICKS"), 9);
            case 4 -> t.with(s("STACK_CAP"), 12);
            case 5 -> t.add(s("STATUS_AMPLIFIER"), 1, 3);
            case 6 -> t.with(s("COUNT"), 3).with(s("OUTGOING_MULTIPLIER"), 1.2)
                    .with(s("DURATION_TICKS"), 80);
            case 7 -> t.with(s("COUNT"), 16).with(s("DAMAGE_MULTIPLIER"), .55)
                    .with(s("RADIUS"), 12).multiply(s("DURATION_TICKS"), .6, 240);
            case 8 -> t.with(s("COUNT"), 4).with(s("DAMAGE_MULTIPLIER"), 1.5)
                    .with(s("STACK_CAP"), 24).with(s("DURATION_TICKS"), 240);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("ABSORPTION"), 4).with(s("STATUS_DURATION_TICKS"), 80);
            case 1 -> t.with(s("INCOMING_MULTIPLIER"), .75).with(s("LOCKOUT_TICKS"), 40).with(s("RANGE"), 3);
            case 2 -> t.with(s("RADIUS"), 4).with(s("STATUS_DURATION_TICKS"), 30).with(s("LOCKOUT_TICKS"), 60);
            case 3 -> t.with(s("SECONDARY_DAMAGE_MULTIPLIER"), .25).with(s("LOCKOUT_TICKS"), 30);
            case 4 -> t.with(s("COUNT"), 8).with(s("STATUS_DURATION_TICKS"), 40);
            case 5 -> t.with(s("COUNT"), 4).with(s("SPEED"), 1.1);
            case 6 -> t.with(s("HEALTH_THRESHOLD"), .3).with(s("ABSORPTION"), 2).with(s("STACK_CAP"), 10);
            case 7 -> t.with(s("RANGE"), 4).with(s("COUNT"), 6).with(s("STATUS_AMPLIFIER"), 1);
            case 8 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.5, 1).with(s("RANGE"), 12);
            default -> t;
        };
    }

    private static Phase7AbilityTuning chompolotl(Phase7AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1);
            case 1 -> t.add(s("COOLDOWN_TICKS"), -10, 60);
            case 2 -> t.add(s("DURATION_TICKS"), 80, 500);
            case 3 -> t.with(s("HEALTH_THRESHOLD"), .4).with(s("OUTGOING_MULTIPLIER"), 1.15);
            case 4 -> t.with(s("SECONDARY_DAMAGE_MULTIPLIER"), .2).with(s("RADIUS"), 2).with(s("TARGET_CAP"), 4);
            case 5 -> t.with(s("WIDTH"), 5).with(s("PER_STACK_MULTIPLIER"), .05).with(s("STACK_CAP"), 4);
            case 6 -> t.with(s("RANGE"), 8).with(s("DURATION_TICKS"), 60).with(s("COUNT"), 1);
            case 7 -> t.with(s("COUNT"), 1).multiply(s("DAMAGE_MULTIPLIER"), 2.5, 1)
                    .with(s("RADIUS"), 3).multiply(s("COOLDOWN_TICKS"), 3, 60);
            case 8 -> t.with(s("COUNT"), 4).with(s("DAMAGE_MULTIPLIER"), .45).with(s("DURATION_TICKS"), 160);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("RANGE"), 3, 16).with(s("SEARCH_CAP"), 16);
            case 1 -> t.with(s("STATUS_DURATION_TICKS"), 100).with(s("STATUS_AMPLIFIER"), 1);
            case 2 -> t.add(s("RADIUS"), 2, 16);
            case 3 -> t.with(s("ABSORPTION"), 4).with(s("DURATION_TICKS"), 80).with(s("LOCKOUT_TICKS"), 200);
            case 4 -> t.with(s("OUTGOING_MULTIPLIER"), 1.2);
            case 5 -> t.with(s("WIDTH"), 4).with(s("INTERVAL_TICKS"), 40);
            case 6 -> t.with(s("COUNT"), 3).with(s("LOCKOUT_TICKS"), 200).with(s("REFUND_TICKS"), 100);
            case 7 -> t.with(s("COUNT"), 3).with(s("RADIUS"), 3);
            case 8 -> t.with(s("COUNT"), 3).with(s("DAMAGE_MULTIPLIER"), 1.35).with(s("DURATION_TICKS"), 300);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.add(s("DURATION_TICKS"), 80, 500);
            case 1 -> t.add(s("STATUS_DURATION_TICKS"), 100, 200);
            case 2 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.25, 1);
            case 3 -> t.with(s("RANGE"), 6).with(s("INCOMING_MULTIPLIER"), .85);
            case 4 -> t.with(s("LOCKOUT_TICKS"), 400).with(s("COUNT"), 1);
            case 5 -> t.with(s("HEALTH_THRESHOLD"), .35).with(s("STATUS_DURATION_TICKS"), 60)
                    .with(s("STATUS_AMPLIFIER"), 1);
            case 6 -> t.with(s("REFUND_TICKS"), 25);
            case 7 -> t.with(s("DURATION_TICKS"), 600).with(s("RANGE"), 6).with(s("COUNT"), 0);
            case 8 -> t.multiply(s("DAMAGE_MULTIPLIER"), 2, 1).with(s("RADIUS"), 0);
            default -> t;
        };
    }

    private static boolean matches(int profile, int branch, int slot, UniqueAbilityDefinition definition) {
        return switch (profile) {
            case 0 -> definition == Phase7UniqueAbilities.BRAMBLE_GRASP ? branch != 1
                    : definition == Phase7UniqueAbilities.BRAMBLE_HUNT && branch == 1;
            case 1 -> definition == Phase7UniqueAbilities.WAXWEAVER_PRISON
                    ? branch == 0 || branch == 1 && slot == 6 || branch == 2 && (slot == 2 || slot == 5)
                    : definition == Phase7UniqueAbilities.WAXWEAVER_TEMPO ? branch == 1
                    : definition == Phase7UniqueAbilities.WAXWEAVER_REVIVAL && branch == 2;
            case 2 -> definition == Phase7UniqueAbilities.HIVEHEART_PROC ? branch == 0
                    : definition == Phase7UniqueAbilities.HIVEHEART_SWARM && branch != 0;
            case 3 -> definition == Phase7UniqueAbilities.CHOMPOLOTL_PROC ? branch == 0
                    : (definition == Phase7UniqueAbilities.CHOMPOLOTL_RALLY
                    || definition == Phase7UniqueAbilities.CHOMPOLOTL_GUARDIAN) && branch != 0;
            default -> false;
        };
    }

    private static Phase7AbilityTuning mode(Phase7AbilityTuning tuning, int bit) {
        return tuning.with(s("MODE"), tuning.integer(s("MODE"), 0) | bit);
    }

    private static Phase7AbilityTuning.Setting s(String name) {
        return Phase7AbilityTuning.Setting.valueOf(name);
    }
}
