package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.SimplyMastery;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.Phase3AbilityTuning;
import net.sweenus.simplyswords.api.ability.Phase3UniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityContext;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityEvent;
import net.sweenus.simplyswords.api.ability.UniqueAbilityPhase;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;

import java.util.List;
import java.util.Set;

final class Phase3MasterySkillEffect implements AbilitySkillEffectType {
    private static final Identifier ID = Identifier.of(SimplyMastery.MOD_ID, "phase3_mastery");

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
        if (!effect.parameters().keySet().equals(Set.of("kind"))) {
            errors.add(where + "phase3_mastery requires only kind");
        }
        int kind = effect.parameters().getOrDefault("kind", -1);
        if (kind < 0 || kind >= 162) errors.add(where + "kind must be between 0 and 161");
    }

    @Override
    public void tune(UniqueAbilityContext context, UniqueAbilityDefinition definition,
                     UniqueAbilityTuning.Builder tuning, MasteryProfile.Node node) {
        int kind = parameter(node, "kind", -1);
        if (kind < 0 || kind >= 162 || !matches(kind / 27, definition)) return;
        int profile = kind / 27;
        int branch = kind % 27 / 9;
        int slot = kind % 9;
        if (!applies(profile, branch, definition)) return;
        Phase3AbilityTuning value = tuning.get(Phase3UniqueAbilities.TUNING);
        value = switch (profile) {
            case 0 -> stormscale(value, branch, slot);
            case 1 -> ionbound(value, branch, slot);
            case 2 -> soulrender(value, branch, slot);
            case 3 -> soulstalker(value, branch, slot);
            case 4 -> whisperwind(value, branch, slot);
            case 5 -> dreadwhisper(value, branch, slot);
            default -> value;
        };
        tuning.set(Phase3UniqueAbilities.TUNING, value);
        if (definition.cooldownKey().isPresent()) {
            int cooldown = value.integer(s("COOLDOWN_TICKS"), tuning.get(Phase3UniqueAbilities.COOLDOWN_TICKS));
            double multiplier = value.get(s("COOLDOWN_MULTIPLIER"), 1);
            tuning.set(Phase3UniqueAbilities.COOLDOWN_TICKS, (int) Math.round(cooldown * multiplier));
        }
    }

    private static Phase3AbilityTuning stormscale(Phase3AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("RANGE"), 22);
            case 1 -> t.with(s("RADIUS"), 4).with(s("TARGET_CAP"), 24);
            case 2 -> t.with(s("ROD_DURATION_TICKS"), 900);
            case 3 -> t.with(s("TETHER_RANGE"), 38);
            case 4 -> t.with(s("TRAVEL_TICKS"), 12);
            case 5 -> mode(t, 1).with(s("IMPACT_DAMAGE_MULTIPLIER"), .7);
            case 6 -> mode(t, 2).with(s("REPOSITION_RANGE"), 14).with(s("LOCKOUT_TICKS"), 80);
            case 7 -> mode(t, 4).with(s("MOVEMENT_SPEED"), .3).with(s("RADIUS"), 3)
                    .with(s("DAMAGE_MULTIPLIER"), .75).with(s("GROWTH_CAP"), .4);
            case 8 -> mode(t, 8).with(s("RADIUS"), 6).with(s("DAMAGE_MULTIPLIER"), 1.4)
                    .with(s("TETHER_RANGE"), 20).with(s("ROD_DURATION_TICKS"), 600)
                    .with(s("COOLDOWN_MULTIPLIER"), 1.2);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.with(s("GROWTH_PER_HIT"), .015).with(s("GROWTH_CAP"), .8);
            case 2 -> t.with(s("GROWTH_CAP"), 1);
            case 3 -> mode(t, 16).with(s("IMPACT_DAMAGE_MULTIPLIER"), .5).with(s("LOCKOUT_TICKS"), 10);
            case 4 -> mode(t, 32).with(s("STATUS_DURATION_TICKS"), 60).with(s("DELAY_TICKS"), 4)
                    .with(s("TARGET_CAP"), 12);
            case 5 -> mode(t, 64).with(s("BONUS_CAP"), .2).with(s("PER_STACK_BONUS"), .05);
            case 6 -> mode(t, 128).with(s("THRESHOLD"), 5).with(s("IMPACT_DAMAGE_MULTIPLIER"), .6);
            case 7 -> mode(t, 256).with(s("IMPACT_DAMAGE_MULTIPLIER"), .55)
                    .with(s("GROWTH_PER_HIT"), .02).with(s("RADIUS"), 4.5);
            case 8 -> mode(t, 512).with(s("COUNT"), 10).with(s("RADIUS"), 6)
                    .with(s("IMPACT_DAMAGE_MULTIPLIER"), .35).with(s("DURATION_TICKS"), 120)
                    .add(s("COOLDOWN_TICKS"), 80, 1000);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("PULL_STRENGTH"), .38);
            case 1 -> t.with(s("SLOW_DURATION_TICKS"), 40).with(s("STATUS_AMPLIFIER"), 0);
            case 2 -> t.with(s("DAMAGE_REDUCTION"), .15);
            case 3 -> mode(t, 1024).with(s("IMPACT_DAMAGE_MULTIPLIER"), .25).with(s("CHAIN_RANGE"), 3)
                    .with(s("CHAIN_TARGET_CAP"), 3);
            case 4 -> mode(t, 2048).with(s("CENTER_RADIUS"), 1.5)
                    .with(s("IMPACT_LIFT"), .2);
            case 5 -> mode(t, 4096).with(s("REVERSE_STRENGTH"), 1.5)
                    .with(s("STATUS_DURATION_TICKS"), 60).with(s("LOCKOUT_TICKS"), 60);
            case 6 -> mode(t, 8192).with(s("THRESHOLD"), 6).with(s("ABSORPTION"), 3)
                    .with(s("BUFF_DURATION_TICKS"), 80).with(s("REFUND_TICKS"), 20)
                    .with(s("LOCKOUT_TICKS"), 100);
            case 7 -> mode(t, 16384).with(s("ROOT_RADIUS"), 2.5).with(s("STATUS_DURATION_TICKS"), 20)
                    .multiply(s("DAMAGE_MULTIPLIER"), .8, 1).with(s("ROOT_TARGET_CAP"), 8);
            case 8 -> mode(t, 32768).with(s("PULL_STRENGTH"), -2.5)
                    .with(s("EDGE_BONUS"), .35);
            default -> t;
        };
    }

    private static Phase3AbilityTuning ionbound(Phase3AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("INTERVAL_TICKS"), 145);
            case 1 -> t.with(s("THRESHOLD"), .25);
            case 2 -> t.with(s("SHIELD_DURATION_TICKS"), 50);
            case 3 -> mode(t, 1).with(s("PULL_STRENGTH"), -2);
            case 4 -> mode(t, 2).with(s("STACK_DURATION_TICKS"), 200).with(s("STACK_CAP"), 3)
                    .with(s("PER_STACK_BONUS"), .1);
            case 5 -> mode(t, 4).with(s("THRESHOLD"), .25).with(s("LOCKOUT_TICKS"), 1200);
            case 6 -> mode(t, 8).with(s("REFUND_TICKS"), 40);
            case 7 -> mode(t, 16).with(s("THRESHOLD"), .15).with(s("SHIELD_DURATION_TICKS"), 80)
                    .with(s("COUNT"), 2).with(s("DAMAGE_REDUCTION"), .25);
            case 8 -> mode(t, 32).with(s("INTERVAL_TICKS"), 100).with(s("PER_STACK_BONUS"), .15)
                    .with(s("BONUS_CAP"), .45);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("RANGE"), 14).with(s("WIDTH"), 7).with(s("HEIGHT"), 5)
                    .with(s("TARGET_CAP"), 24);
            case 1 -> t.with(s("CORRIDOR_MATERIALIZE_TICKS"), 5).with(s("CORRIDOR_HOLD_TICKS"), 7)
                    .with(s("CORRIDOR_CLOSE_TICKS"), 7);
            case 2 -> t.with(s("PULL_STRENGTH"), .4);
            case 3 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1);
            case 4 -> t.with(s("STATUS_DURATION_TICKS"), 60).with(s("TARGET_CAP"), 16);
            case 5 -> mode(t, 64).with(s("WIDTH"), 2).with(s("PER_STACK_BONUS"), .25);
            case 6 -> t.with(s("LOCKOUT_TICKS"), 30);
            case 7 -> mode(t, 128).with(s("RANGE"), 8).with(s("WIDTH"), 4).with(s("HEIGHT"), 4)
                    .with(s("TARGET_CAP"), 8).with(s("MOVEMENT_SPEED"), .1)
                    .with(s("DAMAGE_MULTIPLIER"), 2.2);
            case 8 -> mode(t, 256).with(s("PULL_STRENGTH"), -3).with(s("DAMAGE_MULTIPLIER"), 1.5)
                    .with(s("TARGET_CAP"), 24).multiply(s("FINAL_WIDTH"), .75, 1.1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("WIDTH"), 1.3).with(s("TARGET_CAP"), 16);
            case 1 -> t.with(s("RANGE"), 14);
            case 2 -> t.with(s("INTERVAL_TICKS"), 4);
            case 3 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 4 -> t.with(s("STATUS_DURATION_TICKS"), 130);
            case 5 -> t.with(s("MOVEMENT_SPEED"), .3);
            case 6 -> t.with(s("BEAM_DURATION_TICKS"), 72).with(s("DAMAGE_MULTIPLIER"), 1.2).with(s("COUNT"), 1);
            case 7 -> mode(t, 512).with(s("SPEED"), 5).with(s("WIDTH"), 2)
                    .with(s("DAMAGE_MULTIPLIER"), .7).with(s("TARGET_CAP"), 24)
                    .with(s("STATUS_DURATION_TICKS"), 60);
            case 8 -> mode(t, 1024).with(s("WIDTH"), .7).with(s("BEAM_DURATION_TICKS"), 40)
                    .with(s("DAMAGE_MULTIPLIER"), 2.25).with(s("TARGET_CAP"), 3)
                    .with(s("THRESHOLD"), 6).with(s("MOVEMENT_SPEED"), 0);
            default -> t;
        };
    }

    private static Phase3AbilityTuning soulrender(Phase3AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("CHANCE"), 5, 85);
            case 1 -> t.with(s("MARK_DURATION_TICKS"), 600);
            case 2 -> t.with(s("STACK_CAP"), 10);
            case 3 -> t.with(s("MELEE_BONUS_PER_STACK"), .03).with(s("MELEE_BONUS_CAP"), .18);
            case 4 -> mode(t, 1).with(s("COUNT"), 2).with(s("LOCKOUT_TICKS"), 80);
            case 5 -> mode(t, 2).with(s("CHANCE"), 25).with(s("RANGE"), 4)
                    .with(s("TARGET_CAP"), 1).with(s("LOCKOUT_TICKS"), 20);
            case 6 -> t.with(s("PER_STACK_BONUS"), .06).with(s("BONUS_CAP"), .36);
            case 7 -> mode(t, 4).with(s("CHANCE"), 100).with(s("LOCKOUT_TICKS"), 60)
                    .with(s("BONUS_CAP"), .2);
            case 8 -> mode(t, 8).with(s("STACK_CAP"), 5).with(s("PER_STACK_BONUS"), .15)
                    .with(s("BONUS_CAP"), .75);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("RADIUS"), 12).with(s("TARGET_CAP"), 32);
            case 1 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 2 -> mode(t, 16).with(s("THRESHOLD"), 3).with(s("STATUS_DURATION_TICKS"), 60);
            case 3 -> t.add(s("HEAL_RATIO"), .5, .5).with(s("HEAL_CAP"), 9);
            case 4 -> mode(t, 32).with(s("RANGE"), 4).with(s("PULL_STRENGTH"), 1.5)
                    .with(s("TARGET_CAP"), 8);
            case 5 -> mode(t, 64).with(s("IMPACT_DAMAGE_MULTIPLIER"), .35).with(s("RADIUS"), 5)
                    .with(s("TARGET_CAP"), 6);
            case 6 -> mode(t, 128).with(s("THRESHOLD"), 6).with(s("STATUS_DURATION_TICKS"), 100)
                    .with(s("STATUS_AMPLIFIER"), 1);
            case 7 -> mode(t, 256).with(s("RADIUS"), 16).with(s("TARGET_CAP"), 32)
                    .with(s("DAMAGE_MULTIPLIER"), .7).with(s("HEAL_RATIO"), .5);
            case 8 -> mode(t, 512).with(s("RADIUS"), 10).with(s("TARGET_CAP"), 5)
                    .with(s("DAMAGE_MULTIPLIER"), 1.5).with(s("HEAL_RATIO"), 1.25);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("ABSORPTION"), 2).with(s("BUFF_DURATION_TICKS"), 80);
            case 1 -> mode(t, 1024).with(s("RADIUS"), 6).with(s("DAMAGE_REDUCTION"), .1);
            case 2 -> mode(t, 2048).with(s("STATUS_DURATION_TICKS"), 40).with(s("LOCKOUT_TICKS"), 40);
            case 3 -> mode(t, 4096).with(s("STATUS_DURATION_TICKS"), 40).with(s("LOCKOUT_TICKS"), 40)
                    .with(s("TARGET_CAP"), 8);
            case 4 -> mode(t, 8192).with(s("PER_STACK_BONUS"), .1).with(s("BONUS_CAP"), 6)
                    .with(s("THRESHOLD"), .35);
            case 5 -> mode(t, 16384).with(s("THRESHOLD"), .2).with(s("ABSORPTION"), 1)
                    .with(s("BONUS_CAP"), 6);
            case 6 -> mode(t, 32768).with(s("THRESHOLD"), 5).with(s("RANGE"), 10)
                    .with(s("TARGET_CAP"), 5).with(s("STATUS_DURATION_TICKS"), 40)
                    .with(s("LOCKOUT_TICKS"), 1200);
            case 7 -> mode(t, 65536).multiply(s("DAMAGE_MULTIPLIER"), .75, 1)
                    .with(s("HEAL_RATIO"), .4).with(s("ABSORPTION"), 8).with(s("BUFF_DURATION_TICKS"), 120);
            case 8 -> mode(t, 131072).with(s("HEAL_RATIO"), 0).with(s("PER_STACK_BONUS"), .1)
                    .with(s("BONUS_CAP"), .5).with(s("BUFF_DURATION_TICKS"), 100);
            default -> t;
        };
    }

    private static Phase3AbilityTuning soulstalker(Phase3AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("CHANCE"), 30).with(s("INTERVAL_TICKS"), 20);
            case 1 -> t.with(s("RANGE"), 10);
            case 2 -> t.with(s("LOCKOUT_TICKS"), 50);
            case 3 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1).with(s("STATUS_DURATION_TICKS"), 40);
            case 4 -> mode(t, 1).with(s("PER_STACK_BONUS"), .25);
            case 5 -> mode(t, 2).with(s("COUNT"), 1).with(s("RANGE"), 4);
            case 6 -> mode(t, 4).with(s("CHANCE"), 60).with(s("STATUS_DURATION_TICKS"), 20)
                    .with(s("LOCKOUT_TICKS"), 80);
            case 7 -> mode(t, 8).with(s("COUNT"), 3).with(s("RANGE"), 8)
                    .with(s("DAMAGE_MULTIPLIER"), .65).with(s("LOCKOUT_TICKS"), 90);
            case 8 -> mode(t, 16).with(s("RANGE"), 10).with(s("DAMAGE_MULTIPLIER"), 1.75)
                    .with(s("REFUND_TICKS"), 20).with(s("LOCKOUT_TICKS"), 30);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("STRIDE_DURATION_TICKS"), 900);
            case 1 -> t.multiply(s("MOVEMENT_SPEED"), 1.1, .215).multiply(s("CLIMB_SPEED"), 1.1, .3);
            case 2 -> t.with(s("RADIUS"), 1).multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 3 -> t.with(s("STAIN_DURATION_TICKS"), 320);
            case 4 -> t.with(s("STATUS_DURATION_TICKS"), 60).with(s("STATUS_AMPLIFIER"), 1)
                    .with(s("TARGET_CAP"), 16).with(s("INTERVAL_TICKS"), 10);
            case 5 -> mode(t, 32).with(s("THRESHOLD"), 8).with(s("PER_STACK_BONUS"), .15);
            case 6 -> t.with(s("COOLDOWN_TICKS"), 1020);
            case 7 -> mode(t, 64).add(s("STRIDE_DURATION_TICKS"), 400, 800).multiply(s("WIDTH"), 1.5, 1.25)
                    .with(s("COOLDOWN_MULTIPLIER"), 1.2).multiply(s("DAMAGE_MULTIPLIER"), .75, 1);
            case 8 -> mode(t, 128).with(s("RANGE"), 12).with(s("STAIN_RADIUS"), 2)
                    .multiply(s("STRIDE_DURATION_TICKS"), .75, 800).with(s("LOCKOUT_TICKS"), 60);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("RANGE"), 18);
            case 1 -> t.with(s("FINAL_WIDTH"), 3.8);
            case 2 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1);
            case 3 -> t.with(s("LOCKOUT_TICKS"), 1);
            case 4 -> t.multiply(s("IMPACT_DAMAGE_MULTIPLIER"), 1.2, 1).with(s("IMPACT_RADIUS"), 2.5)
                    .with(s("TARGET_CAP"), 24);
            case 5 -> t.multiply(s("IMPACT_KNOCKBACK"), 1.25, .7).with(s("IMPACT_LIFT"), .28);
            case 6 -> mode(t, 256).with(s("PER_STACK_BONUS"), .04).with(s("STACK_CAP"), 5)
                    .with(s("BONUS_CAP"), .2);
            case 7 -> mode(t, 512).with(s("RANGE"), 24).with(s("FINAL_WIDTH"), 5)
                    .with(s("DAMAGE_MULTIPLIER"), .8).with(s("TARGET_CAP"), 16)
                    .with(s("COOLDOWN_MULTIPLIER"), 2);
            case 8 -> mode(t, 1024).with(s("IMPACT_DAMAGE_MULTIPLIER"), 2).with(s("IMPACT_RADIUS"), 4)
                    .with(s("STAIN_RADIUS"), 2).with(s("STAIN_DURATION_TICKS"), 240)
                    .multiply(s("DAMAGE_MULTIPLIER"), .7, 1);
            default -> t;
        };
    }

    private static Phase3AbilityTuning whisperwind(Phase3AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("SPEED"), 3.3);
            case 1 -> t.with(s("COOLDOWN_TICKS"), 155);
            case 2 -> t.with(s("FLICKER_DURATION_TICKS"), 18).with(s("ABSORPTION_DURATION_TICKS"), 120);
            case 3 -> mode(t, 1).with(s("STATUS_DURATION_TICKS"), 30).with(s("DAMAGE_REDUCTION"), .2);
            case 4 -> mode(t, 2).with(s("IMPACT_DAMAGE_MULTIPLIER"), .2).with(s("TARGET_CAP"), 8);
            case 5 -> mode(t, 4).with(s("THRESHOLD"), 3).with(s("REFUND_TICKS"), 25);
            case 6 -> mode(t, 8).with(s("RANGE"), 3).with(s("TARGET_CAP"), 4);
            case 7 -> mode(t, 16).multiply(s("RANGE"), 1.5, 8).with(s("TARGET_CAP"), 16)
                    .multiply(s("DAMAGE_MULTIPLIER"), .75, 1).with(s("COOLDOWN_MULTIPLIER"), 1.2);
            case 8 -> mode(t, 32).with(s("TARGET_CAP"), 1).with(s("IMPACT_DAMAGE_MULTIPLIER"), .8)
                    .with(s("DAMAGE_MULTIPLIER"), .8);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.with(s("DELAY_TICKS"), 28).multiply(s("DAMAGE_MULTIPLIER"), 1.12, 1);
            case 2 -> t.with(s("PER_STACK_BONUS"), .2).with(s("STACK_CAP"), 8);
            case 3 -> t.with(s("WEAKNESS_DURATION_TICKS"), 60);
            case 4 -> mode(t, 64).with(s("IMPACT_DAMAGE_MULTIPLIER"), .35).with(s("RADIUS"), 3)
                    .with(s("TARGET_CAP"), 3);
            case 5 -> t.with(s("THRESHOLD"), 4).with(s("BONUS_CAP"), .15);
            case 6 -> mode(t, 128).with(s("COUNT"), 1).with(s("IMPACT_DAMAGE_MULTIPLIER"), .3)
                    .with(s("PER_STACK_BONUS"), .15);
            case 7 -> mode(t, 256).with(s("COUNT"), 3).with(s("DAMAGE_MULTIPLIER"), .45)
                    .with(s("INTERVAL_TICKS"), 4).with(s("DELAY_TICKS"), 36).with(s("TARGET_CAP"), 8);
            case 8 -> mode(t, 512).with(s("TARGET_CAP"), 1).with(s("DAMAGE_MULTIPLIER"), 2.25)
                    .with(s("DELAY_TICKS"), 16).add(s("COOLDOWN_TICKS"), 30, 175);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("CHANCE"), 20);
            case 1 -> t.with(s("ABSORPTION"), 4).with(s("ABSORPTION_DURATION_TICKS"), 100);
            case 2 -> mode(t, 1024).with(s("CHANCE_PER_FAILURE"), 3).with(s("CHANCE_BONUS_CAP"), 15)
                    .with(s("STACK_DURATION_TICKS"), 100);
            case 3 -> mode(t, 2048).with(s("DAMAGE_REDUCTION"), .1);
            case 4 -> mode(t, 4096).with(s("STATUS_DURATION_TICKS"), 20).with(s("DAMAGE_REDUCTION"), .3);
            case 5 -> mode(t, 8192).with(s("DURATION_TICKS"), 80).with(s("CHANCE"), 100);
            case 6 -> mode(t, 16384).with(s("DURATION_TICKS"), 40).with(s("PER_STACK_BONUS"), .2)
                    .with(s("STATUS_DURATION_TICKS"), 60);
            case 7 -> mode(t, 32768).with(s("REFUND_TICKS"), 60).with(s("INTERVAL_TICKS"), 20)
                    .with(s("STATUS_DURATION_TICKS"), 40);
            case 8 -> mode(t, 65536).with(s("THRESHOLD"), 3).with(s("STACK_DURATION_TICKS"), 80)
                    .with(s("DAMAGE_MULTIPLIER"), 1.75).with(s("TARGET_CAP"), 1);
            default -> t;
        };
    }

    private static Phase3AbilityTuning dreadwhisper(Phase3AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("WIDTH"), 5).with(s("TARGET_CAP"), 24);
            case 1 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 2 -> t.with(s("SPEED"), 1.76);
            case 3 -> t.with(s("RANGE"), 23);
            case 4 -> t.with(s("HEAL_RATIO"), .4).with(s("HEAL_CAP"), 6);
            case 5 -> mode(t, 1).with(s("IMPACT_DAMAGE_MULTIPLIER"), .4).with(s("RADIUS"), 3)
                    .with(s("TARGET_CAP"), 8);
            case 6 -> mode(t, 2).with(s("PER_STACK_BONUS"), .03).with(s("BONUS_CAP"), .15)
                    .with(s("STACK_CAP"), 5).with(s("STACK_DURATION_TICKS"), 20);
            case 7 -> mode(t, 4).multiply(s("WIDTH"), 2, 4.5).with(s("TARGET_CAP"), 32)
                    .with(s("DAMAGE_MULTIPLIER"), .7).with(s("HEAL_RATIO"), .2)
                    .with(s("COOLDOWN_MULTIPLIER"), 1.25);
            case 8 -> mode(t, 8).with(s("WIDTH"), 2).with(s("TARGET_CAP"), 1)
                    .with(s("DAMAGE_MULTIPLIER"), 2.2).with(s("HEAL_RATIO"), .6);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("WOUND_DURATION_TICKS"), 260);
            case 1 -> t.multiply(s("WOUND_DAMAGE_MULTIPLIER"), 1.12, 1);
            case 2 -> t.with(s("STATUS_DURATION_TICKS"), 260);
            case 3 -> mode(t, 16).with(s("INTERVAL_TICKS"), 20).with(s("PER_STACK_BONUS"), .03)
                    .with(s("BONUS_CAP"), .3).with(s("STACK_DURATION_TICKS"), 200);
            case 4 -> mode(t, 32).with(s("IMPACT_DAMAGE_MULTIPLIER"), .35).with(s("RANGE"), 3);
            case 5 -> mode(t, 64).with(s("CHANCE"), 25).with(s("WOUND_DURATION_TICKS"), 80)
                    .with(s("LOCKOUT_TICKS"), 120);
            case 6 -> mode(t, 128).with(s("THRESHOLD"), .25).with(s("PER_STACK_BONUS"), .3);
            case 7 -> mode(t, 256).with(s("COUNT"), 2).with(s("RANGE"), 4)
                    .with(s("WOUND_DURATION_TICKS"), 120).multiply(s("WOUND_DAMAGE_MULTIPLIER"), .7, 1);
            case 8 -> mode(t, 512).with(s("WOUND_DURATION_TICKS"), 80).with(s("WOUND_DAMAGE_MULTIPLIER"), 2.5)
                    .with(s("COUNT"), 0).with(s("LOCKOUT_TICKS"), 40);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("STAIN_DURATION_TICKS"), 300);
            case 1 -> t.with(s("STATUS_DURATION_TICKS"), 60).with(s("STATUS_AMPLIFIER"), 1);
            case 2 -> t.with(s("BUFF_DURATION_TICKS"), 8);
            case 3 -> mode(t, 1024).with(s("PER_STACK_BONUS"), .15);
            case 4 -> mode(t, 2048).with(s("RANGE"), 6).with(s("PULL_STRENGTH"), 1.5)
                    .with(s("TARGET_CAP"), 8);
            case 5 -> mode(t, 4096).with(s("ABSORPTION"), 1).with(s("BONUS_CAP"), 6)
                    .with(s("BUFF_DURATION_TICKS"), 80);
            case 6 -> mode(t, 8192).with(s("STATUS_DURATION_TICKS"), 60).with(s("DAMAGE_REDUCTION"), .25);
            case 7 -> mode(t, 16384).with(s("DURATION_TICKS"), 100).with(s("INTERVAL_TICKS"), 20)
                    .with(s("DAMAGE_MULTIPLIER"), .2).with(s("TARGET_CAP"), 12);
            case 8 -> mode(t, 32768).with(s("DAMAGE_MULTIPLIER"), 0).with(s("RANGE"), 30)
                    .with(s("RADIUS"), 12).with(s("IMPACT_DAMAGE_MULTIPLIER"), 1.6)
                    .with(s("HEAL_RATIO"), 0).with(s("TARGET_CAP"), 12);
            default -> t;
        };
    }

    private static boolean matches(int profile, UniqueAbilityDefinition definition) {
        return switch (profile) {
            case 0 -> definition == Phase3UniqueAbilities.STORMSCALE_ROD;
            case 1 -> definition == Phase3UniqueAbilities.IONBOUND_CRUSHER
                    || definition == Phase3UniqueAbilities.IONBOUND_BEAM
                    || definition == Phase3UniqueAbilities.IONBOUND_SHIELD;
            case 2 -> definition == Phase3UniqueAbilities.SOULRENDER_MARK
                    || definition == Phase3UniqueAbilities.SOULRENDER_REAP;
            case 3 -> definition == Phase3UniqueAbilities.SOULSTALKER_TENDRIL
                    || definition == Phase3UniqueAbilities.SOULSTALKER_STRIDE;
            case 4 -> definition == Phase3UniqueAbilities.WHISPERWIND_DASH
                    || definition == Phase3UniqueAbilities.WHISPERWIND_RESET;
            case 5 -> definition == Phase3UniqueAbilities.DREADWHISPER_REAVE
                    || definition == Phase3UniqueAbilities.DREADWHISPER_WOUND;
            default -> false;
        };
    }

    private static boolean applies(int profile, int branch, UniqueAbilityDefinition definition) {
        return switch (profile) {
            case 0 -> true;
            case 1 -> branch == 0
                    || branch == 1 && definition == Phase3UniqueAbilities.IONBOUND_CRUSHER
                    || branch == 2 && definition == Phase3UniqueAbilities.IONBOUND_BEAM;
            case 2 -> definition == Phase3UniqueAbilities.SOULRENDER_REAP || branch == 0;
            case 3 -> definition == Phase3UniqueAbilities.SOULSTALKER_TENDRIL ? branch == 0 : branch > 0;
            case 4 -> definition == Phase3UniqueAbilities.WHISPERWIND_DASH || branch == 2;
            case 5 -> definition == Phase3UniqueAbilities.DREADWHISPER_REAVE || branch == 1;
            default -> false;
        };
    }

    @Override
    public void onAbilityEvent(UniqueAbilityEvent event, MasteryProfile.Node node) {
        String key = "execution/" + event.execution().definition().id().getPath();
        long tick = event.execution().context().world().getTime();
        if (event.phase() == UniqueAbilityPhase.START) {
            Phase3MasteryRuntime.set(event.execution().context().stack(), key, 1, tick + 1200, tick);
        } else if (event.phase() == UniqueAbilityPhase.FINISH || event.phase() == UniqueAbilityPhase.CANCEL) {
            Phase3MasteryRuntime.clear(event.execution().context().stack(), key);
        }
    }

    private static Phase3AbilityTuning mode(Phase3AbilityTuning tuning, int bit) {
        return tuning.with(s("MODE"), tuning.integer(s("MODE"), 0) | bit);
    }

    private static Phase3AbilityTuning.Setting s(String name) {
        return Phase3AbilityTuning.Setting.valueOf(name);
    }

    private static int parameter(MasteryProfile.Node node, String key, int fallback) {
        return node.effect().parameters().getOrDefault(key, fallback);
    }
}
