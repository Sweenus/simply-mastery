package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplymastery.mastery.definition.MasteryCohort;
import net.sweenus.simplyswords.api.ability.FireForgeMasteryTuning;
import net.sweenus.simplyswords.api.ability.FireForgeMasteryAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;

import java.util.List;
import java.util.Set;

final class FireForgeMasterySkillEffect implements StaticAbilitySkillEffectType {
    private static final Identifier ID = MasteryCohort.FIRE_FORGE.effectId();

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
        if (!effect.parameters().keySet().equals(Set.of("kind"))) {
            errors.add(where + "cohort/fire_forge requires only kind");
        }
        int kind = effect.parameters().getOrDefault("kind", -1);
        if (kind < 0 || kind >= 162) errors.add(where + "kind must be between 0 and 161");
    }

    @Override
    public void tuneStatic(UniqueAbilityDefinition definition,
                     UniqueAbilityTuning.Builder tuning, MasteryProfile.Node node) {
        int kind = node.effect().parameters().getOrDefault("kind", -1);
        if (kind < 0 || kind >= 162) return;
        int profile = kind / 27;
        int branch = kind % 27 / 9;
        int slot = kind % 9;
        if (!matches(profile, branch, definition)) return;
        FireForgeMasteryTuning value = tuning.get(FireForgeMasteryAbilities.TUNING);
        if (definition.cooldownKey().isPresent() && !value.has(s("COOLDOWN_TICKS"))) {
            value = value.with(s("COOLDOWN_TICKS"), tuning.get(FireForgeMasteryAbilities.COOLDOWN_TICKS));
        }
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
        tuning.set(FireForgeMasteryAbilities.TUNING, value);
        if (definition.cooldownKey().isPresent()) {
            tuning.set(FireForgeMasteryAbilities.COOLDOWN_TICKS,
                    value.integer(s("COOLDOWN_TICKS"), tuning.get(FireForgeMasteryAbilities.COOLDOWN_TICKS)));
        }
    }

    private static FireForgeMasteryTuning hearthflame(FireForgeMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.multiply(s("HEARTH_ECHO_DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.add(s("HEARTH_BIND_RANGE"), 2, 10);
            case 2 -> t.multiply(s("PULL_STRENGTH"), 1.2, .16)
                    .add(s("HEARTH_MIN_LENGTH"), -.5, 2.5);
            case 3 -> t.with(s("HEARTH_ECHO_FIRE_TICKS"), 120);
            case 4 -> t.multiply(s("HEARTH_ECHO_DAMAGE_MULTIPLIER"), 1.15, 1);
            case 5 -> t.multiply(s("HEARTH_SNAP_PRESSURE_MULTIPLIER"), 1.2, 1)
                    .add(s("HEARTH_SNAP_RADIUS"), .5, 2.5);
            case 6 -> t.with(s("HEARTH_FINAL_CHAIN_REQUIREMENT"), 6)
                    .with(s("HEARTH_FINAL_CHAIN_MULTIPLIER"), 1.25);
            case 7 -> t.with(s("TARGET_CAP"), 8).multiply(s("HEARTH_ECHO_DAMAGE_MULTIPLIER"), 1.2, 1)
                    .multiply(s("PULL_STRENGTH"), .7, .16);
            case 8 -> t.with(s("TARGET_CAP"), 1).multiply(s("HEARTH_CHAIN_DURATION_TICKS"), .6, 220)
                    .multiply(s("HEARTH_FINAL_DAMAGE_MULTIPLIER"), 2, 1);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("HEARTH_CAST_ABSORPTION"), 4)
                    .with(s("HEARTH_CAST_ABSORPTION_DURATION_TICKS"), 80);
            case 1 -> t.with(s("HEARTH_FIRE_DAMAGE_REDUCTION"), .3);
            case 2 -> t.with(s("HEARTH_SNAP_RESISTANCE_DURATION_TICKS"), 30)
                    .with(s("HEARTH_SNAP_RESISTANCE_MAX_TICKS"), 90);
            case 3 -> t.with(s("HEARTH_BOUND_DAMAGE_REDUCTION"), .15)
                    .with(s("HEARTH_BOUND_DAMAGE_REDUCTION_RANGE"), 6);
            case 4 -> t.with(s("HEARTH_BRAND_ABSORPTION"), 2)
                    .with(s("HEARTH_BRAND_ABSORPTION_LOCKOUT_TICKS"), 40)
                    .with(s("HEARTH_BRAND_ABSORPTION_DURATION_TICKS"), 200)
                    .with(s("HEARTH_BRAND_ABSORPTION_CAP"), 8);
            case 5 -> t.with(s("HEARTH_CHAIN_PRESERVE_TICKS"), 20);
            case 6 -> t.with(s("HEARTH_COMPLETION_ABSORPTION"), 8)
                    .with(s("HEARTH_COMPLETION_ABSORPTION_DURATION_TICKS"), 60)
                    .with(s("HEARTH_COMPLETION_MIN_CHAINS"), 3);
            case 7 -> t.with(s("HEARTH_ANCHOR_SPEED_MULTIPLIER"), .75);
            case 8 -> t.add(s("HEARTH_BREAK_RANGE"), 6, 24)
                    .multiply(s("HEARTH_BIND_RANGE"), .8, 10)
                    .multiply(s("HEARTH_FINAL_DAMAGE_MULTIPLIER"), .8, 1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.add(s("CHANCE"), 10, 50);
            case 1 -> t.add(s("HEARTH_BRAND_DURATION_TICKS"), 80, 350);
            case 2 -> t.with(s("HEARTH_BRAND_HIT_DAMAGE_MULTIPLIER"), 1.12)
                    .with(s("HEARTH_BRAND_HIT_FIRE_TICKS"), 120);
            case 3 -> t.with(s("HEARTH_REACTIVE_BRAND_DURATION_TICKS"), 120)
                    .with(s("HEARTH_REACTIVE_BRAND_LOCKOUT_TICKS"), 60);
            case 4 -> t.multiply(s("HEARTH_SNAP_DAMAGE_MULTIPLIER"), 1.2, 1)
                    .add(s("HEARTH_SNAP_RADIUS"), .5, 2.5);
            case 5 -> t.with(s("HEARTH_BRAND_SPREAD_COUNT"), 2)
                    .with(s("HEARTH_BRAND_SPREAD_RANGE"), 4)
                    .with(s("HEARTH_BRAND_SPREAD_DURATION_TICKS"), 100);
            case 6 -> t.multiply(s("HEARTH_FINAL_DAMAGE_MULTIPLIER"), 1.3, 1)
                    .with(s("HEARTH_FINAL_KNOCKBACK_MULTIPLIER"), 1.25);
            case 7 -> t.with(s("HEARTH_FORCED_SNAP_TICKS"), 60)
                    .multiply(s("HEARTH_SNAP_DAMAGE_MULTIPLIER"), 1.6, 1)
                    .with(s("PULL_STRENGTH"), 0);
            case 8 -> t.with(s("HEARTH_REBIND_COUNT"), 3).with(s("HEARTH_REBIND_RANGE"), 5)
                    .with(s("HEARTH_REBIND_DAMAGE_MULTIPLIER"), .75);
            default -> t;
        };
    }

    private static FireForgeMasteryTuning emberblade(FireForgeMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.multiply(s("EMBERBLADE_MIN_DAMAGE_MULTIPLIER"), 1.12, 1);
            case 1 -> t.multiply(s("EMBERBLADE_MAX_DAMAGE_MULTIPLIER"), 1.15, 1);
            case 2 -> t.with(s("EMBERBLADE_PIERCE_COUNT"), 1)
                    .with(s("EMBERBLADE_PIERCE_DAMAGE_MULTIPLIER"), .7)
                    .with(s("EMBERBLADE_PIERCE_RANGE"), 12);
            case 3 -> t.with(s("EMBERBLADE_AIM_DAMAGE_MULTIPLIER"), 1.2);
            case 4 -> t.with(s("EMBERBLADE_BANK_DURATION_TICKS"), 60)
                    .with(s("EMBERBLADE_BANK_MULTIPLIER"), .5);
            case 5 -> t.with(s("FIRE_TICKS"), 60).with(s("EMBERBLADE_SPLASH_DAMAGE_MULTIPLIER"), .25)
                    .with(s("EMBERBLADE_SPLASH_RADIUS"), 2).with(s("EMBERBLADE_SPLASH_TARGET_CAP"), 6);
            case 6 -> t.with(s("EMBERBLADE_FULL_CHARGE_WINDOW_TICKS"), 10)
                    .with(s("EMBERBLADE_FULL_CHARGE_MULTIPLIER"), 1.25)
                    .with(s("EMBERBLADE_FULL_CHARGE_FIRE_TICKS"), 80);
            case 7 -> t.add(s("EMBERBLADE_CHANNEL_TICKS"), 20, 80)
                    .multiply(s("EMBERBLADE_MAX_DAMAGE_MULTIPLIER"), 1.6, 1);
            case 8 -> t.multiply(s("EMBERBLADE_CHANNEL_TICKS"), .5, 80)
                    .add(s("COOLDOWN_TICKS"), -20, 60)
                    .multiply(s("EMBERBLADE_MAX_DAMAGE_MULTIPLIER"), .65, 1);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("EMBERBLADE_QUICKDRAW_DURATION_TICKS"), 60);
            case 1 -> t.with(s("EMBERBLADE_HASTE_DURATION_TICKS"), 80);
            case 2 -> t.with(s("EMBERBLADE_RECOIL_DISTANCE"), 1.5)
                    .with(s("EMBERBLADE_FALL_PROTECTION_TICKS"), 20);
            case 3 -> t.with(s("EMBERBLADE_MOVE_DISTANCE"), 6)
                    .with(s("EMBERBLADE_NEXT_HIT_MULTIPLIER"), 1.15)
                    .with(s("EMBERBLADE_NEXT_HIT_DURATION_TICKS"), 80);
            case 4 -> t.with(s("EMBERBLADE_LATE_DAMAGE_REDUCTION"), .25)
                    .with(s("EMBERBLADE_LATE_GUARD_TICKS"), 8);
            case 5 -> t.with(s("EMBERBLADE_IRE_RUSH_DURATION_TICKS"), 60)
                    .with(s("EMBERBLADE_FULL_BUFF_LOCKOUT_TICKS"), 100);
            case 6 -> t.with(s("EMBERBLADE_PURSUIT_SPEED_TICKS"), 40);
            case 7 -> t;
            case 8 -> t.with(s("EMBERBLADE_RECOIL_DISTANCE"), 4)
                    .with(s("EMBERBLADE_BLASTBACK_RESISTANCE_TICKS"), 40)
                    .multiply(s("EMBERBLADE_MIN_DAMAGE_MULTIPLIER"), .8, 1)
                    .multiply(s("EMBERBLADE_MAX_DAMAGE_MULTIPLIER"), .8, 1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("CHANCE"), 10);
            case 1 -> t.with(s("EMBERBLADE_IRE_DURATION_BONUS_TICKS"), 40);
            case 2 -> t.with(s("EMBERBLADE_FRAGMENT_COUNT"), 2)
                    .with(s("EMBERBLADE_FRAGMENT_DAMAGE_MULTIPLIER"), .35)
                    .with(s("EMBERBLADE_FRAGMENT_RANGE"), 5);
            case 3 -> t.with(s("EMBERBLADE_FRAGMENT_SEEK_RANGE"), 10)
                    .with(s("EMBERBLADE_FRAGMENT_SEEK_MULTIPLIER"), 1.2);
            case 4 -> t.with(s("EMBERBLADE_FLASHOVER_COUNT"), 3)
                    .with(s("EMBERBLADE_FLASHOVER_WINDOW_TICKS"), 100)
                    .with(s("EMBERBLADE_FLASHOVER_RADIUS"), 3)
                    .with(s("EMBERBLADE_FLASHOVER_DAMAGE_MULTIPLIER"), .4)
                    .with(s("EMBERBLADE_FLASHOVER_TARGET_CAP"), 8);
            case 5 -> t.with(s("EMBERBLADE_BANK_GAIN"), .05).with(s("EMBERBLADE_BANK_CAP"), .25)
                    .with(s("EMBERBLADE_FLAME_BANK_DURATION_TICKS"), 100);
            case 6 -> t.with(s("EMBERBLADE_FRAGMENT_JUMP_RANGE"), 6)
                    .with(s("EMBERBLADE_FRAGMENT_JUMP_MULTIPLIER"), .5);
            case 7 -> t.with(s("EMBERBLADE_FRAGMENT_COUNT"), 5)
                    .with(s("EMBERBLADE_FRAGMENT_DAMAGE_MULTIPLIER"), .45)
                    .multiply(s("EMBERBLADE_MIN_DAMAGE_MULTIPLIER"), .6, 1)
                    .multiply(s("EMBERBLADE_MAX_DAMAGE_MULTIPLIER"), .6, 1);
            case 8 -> t.with(s("STATUS_AMPLIFIER"), 1)
                    .with(s("EMBERBLADE_INCARNATE_DURATION_TICKS"), 100)
                    .add(s("COOLDOWN_TICKS"), 80, 60);
            default -> t;
        };
    }

    private static FireForgeMasteryTuning emberlash(FireForgeMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.multiply(s("EMBERLASH_SMOULDER_DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.with(s("EMBERLASH_SMOULDER_STACK_CAP"), 6);
            case 2 -> t.add(s("EMBERLASH_SMOULDER_DURATION_TICKS"), 60, 100);
            case 3 -> t.with(s("COUNT"), 3).with(s("FIRE_TICKS"), 60);
            case 4 -> t.with(s("EMBERLASH_SWEEP_RADIUS"), 3).with(s("EMBERLASH_SWEEP_TARGET_CAP"), 4);
            case 5 -> t.with(s("EMBERLASH_COMBO_HITS"), 3).with(s("EMBERLASH_COMBO_WINDOW_TICKS"), 40);
            case 6 -> t.with(s("EMBERLASH_MAX_DAMAGE_REDUCTION"), .12)
                    .with(s("EMBERLASH_MAX_REDUCTION_DURATION_TICKS"), 60);
            case 7 -> t.multiply(s("EMBERLASH_SMOULDER_DAMAGE_MULTIPLIER"), .75, 1)
                    .with(s("LOCKOUT_TICKS"), 60)
                    .with(s("EMBERLASH_ECHO_DAMAGE_MULTIPLIER"), .5);
            case 8 -> t.with(s("EMBERLASH_SMOULDER_STACK_CAP"), 3)
                    .multiply(s("EMBERLASH_SMOULDER_DAMAGE_MULTIPLIER"), 1.45, 1);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("EMBERLASH_EVADE_DISTANCE_MULTIPLIER"), 1.2);
            case 1 -> t.add(s("COOLDOWN_TICKS"), -12, 80);
            case 2 -> t.with(s("EMBERLASH_CAUTERY_ABSORPTION"), 4)
                    .with(s("EMBERLASH_CAUTERY_ABSORPTION_TICKS"), 60);
            case 3 -> t.with(s("EMBERLASH_BLIND_RADIUS"), 3)
                    .with(s("EMBERLASH_BLIND_TARGET_CAP"), 6)
                    .with(s("EMBERLASH_BLIND_DURATION_TICKS"), 30);
            case 4 -> t.with(s("EMBERLASH_BACKLASH_STACKS"), 2)
                    .with(s("EMBERLASH_BACKLASH_DURATION_TICKS"), 80);
            case 5 -> t.with(s("EMBERLASH_BURNING_PACE_DURATION_TICKS"), 40)
                    .with(s("EMBERLASH_BURNING_PACE_AMPLIFIER"), 1);
            case 6 -> t.with(s("EMBERLASH_EMERGENCY_LOCKOUT_TICKS"), 160)
                    .with(s("EMBERLASH_EMERGENCY_RESISTANCE_TICKS"), 60);
            case 7 -> t.with(s("EMBERLASH_PHOENIX_DAMAGE_MULTIPLIER"), .35)
                    .with(s("EMBERLASH_PHOENIX_TARGET_CAP"), 6)
                    .with(s("EMBERLASH_PHOENIX_FIRE_TICKS"), 60);
            case 8 -> t.multiply(s("HEAL_MULTIPLIER"), 1.5, 1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("EMBERLASH_HOT_BLOOD_LOCKOUT_TICKS"), 30);
            case 1 -> t.with(s("EMBERLASH_REPRISAL_DURATION_TICKS"), 100)
                    .with(s("EMBERLASH_REPRISAL_STACK_CAP"), 3);
            case 2 -> t.with(s("EMBERLASH_REPRISAL_TRIGGER_COUNT"), 3)
                    .with(s("EMBERLASH_REPRISAL_DAMAGE_MULTIPLIER"), .25);
            case 3 -> t.with(s("EMBERLASH_ASH_RADIUS"), 2.5)
                    .with(s("EMBERLASH_ASH_DAMAGE_PER_STACK"), .08)
                    .with(s("EMBERLASH_ASH_TARGET_CAP"), 8);
            case 4 -> t.with(s("EMBERLASH_ASH_APPLIED_STACKS"), 1);
            case 5 -> t.with(s("EMBERLASH_LASHBACK_DURATION_TICKS"), 40)
                    .with(s("EMBERLASH_LASHBACK_AMPLIFIER"), 1);
            case 6 -> t.with(s("EMBERLASH_KILL_REFUND_TICKS"), 20);
            case 7 -> t.with(s("EMBERLASH_DETONATION_DAMAGE_PER_STACK"), .3);
            case 8 -> t.with(s("EMBERLASH_REPRISAL_DURATION_TICKS"), 200)
                    .with(s("EMBERLASH_REPRISAL_STACK_CAP"), 5)
                    .with(s("EMBERLASH_INCOMING_PER_CHARGE_MULTIPLIER"), 1.04);
            default -> t;
        };
    }

    static FireForgeMasteryTuning flamewind(FireForgeMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.multiply(s("PERIODIC_DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.add(s("RADIUS"), .75, 5);
            case 2 -> t.with(s("FIRE_TICKS"), 60);
            case 3 -> t.with(s("FLAMEWIND_HASTE_DURATION_TICKS"), 140)
                    .with(s("FLAMEWIND_HASTE_AMPLIFIER"), 1);
            case 4 -> t.with(s("FLAMEWIND_DEATH_DAMAGE_MULTIPLIER"), 1.25);
            case 5 -> t.with(s("FLAMEWIND_DRAFT_PER_SEED"), .03).with(s("FLAMEWIND_DRAFT_CAP"), .15)
                    .with(s("FLAMEWIND_DRAFT_RANGE"), 8);
            case 6 -> t.with(s("PULL_STRENGTH"), .2).with(s("FLAMEWIND_KNOCKBACK_MULTIPLIER"), 1.2);
            case 7 -> t.with(s("INTERVAL_TICKS"), 20)
                    .with(s("FLAMEWIND_PERIODIC_DETONATION_MULTIPLIER"), .3)
                    .multiply(s("FINAL_DAMAGE_MULTIPLIER"), .6, 1);
            case 8 -> t.with(s("PERIODIC_DAMAGE_MULTIPLIER"), 0).with(s("RADIUS"), 5)
                    .multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.5, 1).with(s("TARGET_CAP"), 12);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("FLAMEWIND_SEED_RANGE"), 4, 10);
            case 1 -> t.add(s("DURATION_TICKS"), -20, 101);
            case 2 -> t.add(s("DURATION_TICKS"), 40, 101).multiply(s("PERIODIC_DAMAGE_MULTIPLIER"), 1.08, 1);
            case 3 -> t.with(s("FLAMEWIND_RETARGET_RANGE"), 6);
            case 4 -> t.add(s("SPREAD_CAP"), 1, 6);
            case 5 -> t.add(s("FLAMEWIND_SPREAD_RANGE"), 2, 5).with(s("FLAMEWIND_SEARCH_CAP"), 16);
            case 6 -> t.with(s("FLAMEWIND_SPREAD_DURATION_FRACTION"), .8);
            case 7 -> t.with(s("FLAMEWIND_GENERATION_CAP"), 3)
                    .with(s("FLAMEWIND_GENERATION_MULTIPLIER"), 1.25);
            case 8 -> t.with(s("SPREAD_CAP"), 10).add(s("FLAMEWIND_SPREAD_RANGE"), 3, 5)
                    .with(s("FLAMEWIND_GENERATION_MULTIPLIER"), .8);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("FLAMEWIND_HARVEST_REFUND_TICKS"), 6)
                    .with(s("FLAMEWIND_HARVEST_REFUND_CAP_TICKS"), 30);
            case 1 -> t.with(s("FLAMEWIND_CHAIN_WINDOW_TICKS"), 10)
                    .with(s("FLAMEWIND_CHAIN_DAMAGE_MULTIPLIER"), 1.15);
            case 2 -> t.with(s("FLAMEWIND_FLASH_COUNT"), 3).with(s("FLAMEWIND_FLASH_HASTE_TICKS"), 80)
                    .with(s("FLAMEWIND_FLASH_HASTE_AMPLIFIER"), 1)
                    .with(s("FLAMEWIND_FLASH_WINDOW_TICKS"), 20);
            case 3 -> t.multiply(s("FINAL_DAMAGE_MULTIPLIER"), .7, 1)
                    .with(s("FLAMEWIND_RECAST_LOCKOUT_TICKS"), 100).with(s("COOLDOWN_TICKS"), 100);
            case 4 -> t.with(s("PULL_STRENGTH"), .25);
            case 5 -> t.with(s("FLAMEWIND_RESERVE_ABSORPTION"), 2)
                    .with(s("FLAMEWIND_RESERVE_ABSORPTION_CAP"), 8)
                    .with(s("FLAMEWIND_RESERVE_ABSORPTION_TICKS"), 80);
            case 6 -> t.with(s("FLAMEWIND_RESET_KILL_COUNT"), 3)
                    .with(s("FLAMEWIND_RESET_REFUND_FRACTION"), .25);
            case 7 -> t.multiply(s("FINAL_DAMAGE_MULTIPLIER"), 1.4, 1).add(s("COOLDOWN_TICKS"), 40, 350);
            case 8 -> t.with(s("SPREAD_CAP"), 0)
                    .with(s("FLAMEWIND_RECAST_LOCKOUT_TICKS"), 160).with(s("COOLDOWN_TICKS"), 160);
            default -> t;
        };
    }

    private static FireForgeMasteryTuning moltenEdge(FireForgeMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("MOLTEN_MELEE_HEAT_GAIN"), 6);
            case 1 -> t.with(s("MOLTEN_INCOMING_HEAT_GAIN"), 7);
            case 2 -> t.with(s("MOLTEN_INCOMING_PENALTY_MULTIPLIER"), .85);
            case 3 -> t.with(s("MOLTEN_REDLINE_THRESHOLD"), 75)
                    .with(s("MOLTEN_REDLINE_DAMAGE_MULTIPLIER"), 1.1);
            case 4 -> t.with(s("MOLTEN_HEAT_SINK_ABSORPTION"), 2)
                    .with(s("MOLTEN_HEAT_SINK_STEP"), 20)
                    .with(s("MOLTEN_HEAT_SINK_DURATION_TICKS"), 60)
                    .with(s("MOLTEN_HEAT_SINK_CAP"), 6);
            case 5 -> t.with(s("MOLTEN_UNWIELDED_DECAY_INTERVAL_TICKS"), 2);
            case 6 -> t.with(s("MOLTEN_WHITE_HOT_FIRE_TICKS"), 60)
                    .with(s("MOLTEN_WHITE_HOT_DAMAGE_MULTIPLIER"), 1.12);
            case 7 -> t.with(s("MOLTEN_OVERCLOCK_HEAT_FLOOR"), 75)
                    .with(s("MOLTEN_OVERCLOCK_OUTGOING_MULTIPLIER"), 1.35)
                    .with(s("MOLTEN_OVERCLOCK_INCOMING_AMPLIFICATION"), 1);
            case 8 -> t.with(s("MOLTEN_HEAT_MAX"), 75)
                    .with(s("MOLTEN_INCOMING_AMPLIFICATION_CAP"), .5)
                    .with(s("MOLTEN_OUTGOING_BONUS_CAP"), .35);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("MOLTEN_VENT_DRAIN"), 2);
            case 1 -> t.with(s("MOLTEN_VENT_SPEED_MULTIPLIER"), 1.15);
            case 2 -> t.with(s("MOLTEN_SHOCKWAVE_RADIUS"), 6)
                    .with(s("MOLTEN_SHOCKWAVE_TARGET_CAP"), 16);
            case 3 -> t.with(s("MOLTEN_SHOCKWAVE_KNOCKBACK_MULTIPLIER"), 1.25)
                    .with(s("MOLTEN_BLAST_SPEED_TICKS"), 60);
            case 4 -> t.with(s("MOLTEN_RUPTURE_REFUND_TICKS"), 1)
                    .with(s("MOLTEN_RUPTURE_MIN_COOLDOWN_TICKS"), 3);
            case 5 -> t.with(s("MOLTEN_RUPTURE_SEEK_RANGE"), 5)
                    .with(s("MOLTEN_RUPTURE_TURN_DEGREES"), 20);
            case 6 -> t.with(s("MOLTEN_RECLAIM_TARGET_COUNT"), 3)
                    .with(s("MOLTEN_RECLAIM_HEAT"), 15);
            case 7 -> t.with(s("MOLTEN_AUTO_RUPTURE_INTERVAL_TICKS"), 8)
                    .multiply(s("MOLTEN_SHOCKWAVE_DAMAGE_MULTIPLIER"), .65, 1);
            case 8 -> t.with(s("MOLTEN_ROOT_SPEED_MULTIPLIER"), 0)
                    .with(s("MOLTEN_RESISTANCE_AMPLIFIER"), 1)
                    .multiply(s("MOLTEN_SHOCKWAVE_DAMAGE_MULTIPLIER"), 1.8, 1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("MOLTEN_RUPTURE_LENGTH"), 9);
            case 1 -> t.with(s("MOLTEN_RUPTURE_WIDTH"), 2.75)
                    .with(s("MOLTEN_RUPTURE_SEGMENT_TARGET_CAP"), 8);
            case 2 -> t.multiply(s("MOLTEN_RUPTURE_DAMAGE_MULTIPLIER"), 1.12, 1);
            case 3 -> t.with(s("MOLTEN_RUPTURE_KNOCK_UP"), .35);
            case 4 -> t.with(s("MOLTEN_RUPTURE_FIRE_TICKS"), 80);
            case 5 -> t.with(s("MOLTEN_FORK_SWING_INTERVAL"), 3)
                    .with(s("MOLTEN_FORK_COUNT"), 2)
                    .with(s("MOLTEN_FORK_ANGLE_DEGREES"), 18)
                    .with(s("MOLTEN_FORK_DAMAGE_MULTIPLIER"), .55);
            case 6 -> t.with(s("MOLTEN_SEQUENCE_WINDOW_TICKS"), 40)
                    .with(s("MOLTEN_SEQUENCE_PER_STACK_MULTIPLIER"), .1)
                    .with(s("MOLTEN_SEQUENCE_STACK_CAP"), 3);
            case 7 -> t.with(s("MOLTEN_RUPTURE_LENGTH"), 12)
                    .with(s("MOLTEN_RUPTURE_WIDTH"), 3.5)
                    .multiply(s("MOLTEN_RUPTURE_DAMAGE_MULTIPLIER"), 1.7, 1)
                    .with(s("MOLTEN_RUPTURE_COOLDOWN_MULTIPLIER"), 2);
            case 8 -> t.with(s("MOLTEN_SHATTER_LANE_COUNT"), 5)
                    .with(s("MOLTEN_SHATTER_LANE_LENGTH"), 5)
                    .with(s("MOLTEN_SHATTER_LANE_ANGLE_DEGREES"), 18)
                    .with(s("MOLTEN_SHATTER_DAMAGE_MULTIPLIER"), .45);
            default -> t;
        };
    }

    private static FireForgeMasteryTuning soulPyre(FireForgeMasteryTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.add(s("SOULPYRE_TETHER_DURATION_TICKS"), 100, 600);
            case 1 -> t.add(s("SOULPYRE_START_RADIUS"), 1, 3)
                    .with(s("SOULPYRE_PULSE_TARGET_CAP"), 32);
            case 2 -> t.with(s("SOULPYRE_RADIUS_GROWTH"), 1.25)
                    .with(s("SOULPYRE_MAX_RADIUS"), 12);
            case 3 -> t.with(s("SOULPYRE_PULSE_SOUL_MULTIPLIER"), .06);
            case 4 -> t.with(s("SOULPYRE_BINDING_HIT_COUNT"), 3)
                    .with(s("SOULPYRE_BINDING_WINDOW_TICKS"), 80)
                    .with(s("SOULPYRE_BINDING_SLOW_TICKS"), 40)
                    .with(s("SOULPYRE_BINDING_SLOW_AMPLIFIER"), 1);
            case 5 -> t.with(s("SOULPYRE_PULL_INTERVAL"), 5)
                    .with(s("SOULPYRE_PULL_STRENGTH"), .3);
            case 6 -> t.with(s("SOULPYRE_FEAST_KILL_COUNT"), 3)
                    .with(s("SOULPYRE_FEAST_ABSORPTION"), 2)
                    .with(s("SOULPYRE_FEAST_ABSORPTION_TICKS"), 80)
                    .with(s("SOULPYRE_FEAST_ABSORPTION_CAP"), 6);
            case 7 -> t.with(s("SOULPYRE_RADIUS_GROWTH"), 2)
                    .with(s("SOULPYRE_DEVOURING_EXTRA_PULSES"), 1)
                    .with(s("SOULPYRE_DEVOURING_PULSE_DAMAGE_MULTIPLIER"), .75);
            case 8 -> t.with(s("SOULPYRE_CLOSED_RADIUS"), 4)
                    .with(s("SOULPYRE_CLOSED_PULSE_DAMAGE_MULTIPLIER"), 1.6)
                    .with(s("SOULPYRE_CLOSED_INTERVAL_MULTIPLIER"), .8);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("SOULPYRE_WISP_DAMAGE_MULTIPLIER"), 1.15);
            case 1 -> t.with(s("SOULPYRE_WISP_VOLLEY_SIZE"), 6);
            case 2 -> t.with(s("SOULPYRE_WISP_RANGE_BONUS"), 4)
                    .with(s("SOULPYRE_WISP_SEARCH_CAP"), 24);
            case 3 -> t.with(s("SOULPYRE_WISP_RETARGET_RANGE"), 5)
                    .with(s("SOULPYRE_WISP_RETARGET_COUNT"), 1);
            case 4 -> t.with(s("SOULPYRE_WISP_FIRE_TICKS"), 60)
                    .with(s("SOULPYRE_WISP_MARK_DURATION_TICKS"), 80);
            case 5 -> t.with(s("SOULPYRE_WISP_MARK_DAMAGE_MULTIPLIER"), 1.2);
            case 6 -> t.with(s("SOULPYRE_WAILING_FULL_VOLLEY_COUNT"), 6)
                    .with(s("SOULPYRE_WAILING_RADIUS"), 2.5)
                    .with(s("SOULPYRE_WAILING_DAMAGE_MULTIPLIER"), .4)
                    .with(s("SOULPYRE_WAILING_TARGET_CAP"), 8);
            case 7 -> t.with(s("SOULPYRE_LEGION_VOLLEY_MULTIPLIER"), 2)
                    .with(s("SOULPYRE_LEGION_DAMAGE_MULTIPLIER"), .55)
                    .with(s("SOULPYRE_LEGION_PER_TARGET_CAP"), 3);
            case 8 -> t.with(s("SOULPYRE_LANCE_DAMAGE_PER_SOUL"), .25)
                    .with(s("SOULPYRE_LANCE_SOUL_CAP"), 10)
                    .with(s("SOULPYRE_LANCE_TARGET_CAP"), 6);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("SOULPYRE_DAMAGE_REDUCTION"), .55);
            case 1 -> t.with(s("SOULPYRE_KNOCKBACK_LINGER_TICKS"), 60);
            case 2 -> t.with(s("SOULPYRE_FIRE_RESISTANCE_LINGER_TICKS"), 40);
            case 3 -> t.with(s("SOULPYRE_MANTLE_SOUL_STEP"), 5)
                    .with(s("SOULPYRE_MANTLE_ABSORPTION"), 2)
                    .with(s("SOULPYRE_MANTLE_ABSORPTION_CAP"), 8);
            case 4 -> t.with(s("SOULPYRE_COLLAPSE_DURATION_TICKS"), 80)
                    .with(s("SOULPYRE_COLLAPSE_INTERVAL_TICKS"), 20)
                    .with(s("SOULPYRE_COLLAPSE_DAMAGE_MULTIPLIER"), .2);
            case 5 -> t.with(s("SOULPYRE_REQUIEM_BONUS_PER_SOUL"), .12)
                    .with(s("SOULPYRE_REQUIEM_SOUL_CAP"), 10);
            case 6 -> t.with(s("SOULPYRE_LAST_RITES_SOUL_COUNT"), 5)
                    .with(s("SOULPYRE_LAST_RITES_DURATION_TICKS"), 80);
            case 7 -> t.with(s("SOULPYRE_UNDYING_SOUL_COUNT"), 10)
                    .with(s("SOULPYRE_UNDYING_RESISTANCE_AMPLIFIER"), 2)
                    .with(s("SOULPYRE_UNDYING_RESISTANCE_TICKS"), 60);
            case 8 -> t.with(s("SOULPYRE_FUNERAL_DAMAGE_REDUCTION"), 0)
                    .with(s("SOULPYRE_FUNERAL_RADIUS"), 8)
                    .with(s("SOULPYRE_FUNERAL_DAMAGE_MULTIPLIER"), 1.75)
                    .with(s("SOULPYRE_FUNERAL_TARGET_CAP"), 24);
            default -> t;
        };
    }

    private static boolean matches(int profile, int branch, UniqueAbilityDefinition definition) {
        return switch (profile) {
            case 0 -> definition == FireForgeMasteryAbilities.HEARTHFLAME_CHAINS
                    || definition == FireForgeMasteryAbilities.HEARTHFLAME_BRAND;
            case 1 -> definition == FireForgeMasteryAbilities.EMBERBLADE_SHRAPNEL;
            case 2 -> definition == FireForgeMasteryAbilities.EMBERLASH_SMOULDER
                    || definition == FireForgeMasteryAbilities.EMBERLASH_CAUTERY;
            case 3 -> definition == FireForgeMasteryAbilities.FLAMEWIND_SEED;
            case 4 -> branch == 0 && definition == FireForgeMasteryAbilities.MOLTEN_EDGE_HEAT
                    || branch == 1 && definition == FireForgeMasteryAbilities.MOLTEN_EDGE_VENT
                    || branch == 2 && definition == FireForgeMasteryAbilities.MOLTEN_EDGE_RUPTURE;
            case 5 -> branch == 1 && definition == FireForgeMasteryAbilities.SOUL_PYRE_WISP
                    || branch != 1 && definition == FireForgeMasteryAbilities.SOUL_PYRE_TETHER;
            default -> false;
        };
    }

    private static FireForgeMasteryTuning mode(FireForgeMasteryTuning tuning, int bit) {
        return tuning.with(s("MODE"), tuning.integer(s("MODE"), 0) | bit);
    }

    private static FireForgeMasteryTuning.Setting s(String name) {
        return FireForgeMasteryTuning.Setting.valueOf(name);
    }
}
