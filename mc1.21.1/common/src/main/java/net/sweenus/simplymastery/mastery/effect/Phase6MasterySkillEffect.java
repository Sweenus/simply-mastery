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
        int branch = kind % 27 / 9;
        int slot = kind % 9;
        if (kind < 0 || kind >= 189 || !matches(kind / 27, branch, slot, definition)) return;
        int profile = kind / 27;
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
            int cooldown = tuning.get(Phase6UniqueAbilities.COOLDOWN_TICKS);
            if (profile == 2) {
                if (branch == 0 && slot == 1) {
                    cooldown = Math.max(0, cooldown
                            - value.integer(s("THUNDERBRAND_COOLDOWN_REDUCTION_TICKS"), 0));
                }
            } else if (profile == 4) {
                if (branch == 0 && slot == 8) {
                    cooldown = Math.max(0, (int) Math.round(cooldown
                            * value.get(s("FROSTFALL_COOLDOWN_MULTIPLIER"), 1)));
                }
            } else if (profile == 6) {
                if (branch == 2 && slot == 8) {
                    cooldown = Math.max(0, cooldown
                            + value.integer(s("LIVYATAN_ACTIVE_COOLDOWN_BONUS_TICKS"), 0));
                }
            } else {
                cooldown = value.integer(s("COOLDOWN_TICKS"), cooldown);
            }
            tuning.set(Phase6UniqueAbilities.COOLDOWN_TICKS, cooldown);
        }
    }

    private static Phase6AbilityTuning stormbringer(Phase6AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("STORMBRINGER_PARRY_WINDOW_TICKS"), 24);
            case 1 -> t.with(s("STORMBRINGER_NORMAL_CHARGE_GAIN"), 2);
            case 2 -> t.multiply(s("STORMBRINGER_COUNTER_DAMAGE_MULTIPLIER"), 1.12, 1)
                    .with(s("STORMBRINGER_COUNTER_RADIUS"), 3.5)
                    .with(s("STORMBRINGER_COUNTER_TARGET_CAP"), 10);
            case 3 -> t.with(s("STORMBRINGER_BLOCK_RESISTANCE_TICKS"), 30);
            case 4 -> t.with(s("STORMBRINGER_PARRY_CHARGE_GAIN"), 7);
            case 5 -> t.with(s("STORMBRINGER_COUNTER_KNOCKBACK_MULTIPLIER"), 1.25)
                    .with(s("STORMBRINGER_COUNTER_FIRE_TICKS"), 20);
            case 6 -> t.with(s("STORMBRINGER_ACTIVE_REFUND_TICKS"), 30);
            case 7 -> t.with(s("STORMBRINGER_PARRY_WINDOW_TICKS"), 8)
                    .multiply(s("STORMBRINGER_COUNTER_DAMAGE_MULTIPLIER"), 2, 1)
                    .with(s("STORMBRINGER_NORMAL_CHARGE_GAIN"), 0);
            case 8 -> t.with(s("STORMBRINGER_BLOCK_DURATION_TICKS"), 70)
                    .with(s("STORMBRINGER_NORMAL_CHARGE_GAIN"), 3)
                    .multiply(s("STORMBRINGER_COUNTER_DAMAGE_MULTIPLIER"), .5, 1);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("STORMBRINGER_CHARGE_CAP"), 12);
            case 1 -> t.with(s("STORMBRINGER_FREE_CHAIN_LOCKOUT_TICKS"), 80);
            case 2 -> t.with(s("STORMBRINGER_MELEE_CHARGE_THRESHOLD"), 5)
                    .with(s("STORMBRINGER_MELEE_DAMAGE_MULTIPLIER"), 1.08);
            case 3 -> t.with(s("STORMBRINGER_OVERFLOW_ABSORPTION"), 2)
                    .with(s("STORMBRINGER_OVERFLOW_DURATION_TICKS"), 60)
                    .with(s("STORMBRINGER_OVERFLOW_LOCKOUT_TICKS"), 40);
            case 4 -> t.with(s("STORMBRINGER_RESIDUAL_SLOW_TICKS"), 30);
            case 5 -> t.with(s("STORMBRINGER_RHYTHM_SPEND_COUNT"), 3)
                    .with(s("STORMBRINGER_RHYTHM_WINDOW_TICKS"), 100)
                    .with(s("STORMBRINGER_RHYTHM_HASTE_TICKS"), 80);
            case 6 -> t.with(s("STORMBRINGER_FULL_DAMAGE_MULTIPLIER"), 1.25)
                    .with(s("STORMBRINGER_FULL_CHARGE_COST"), 2);
            case 7 -> t.with(s("STORMBRINGER_CHARGE_CAP"), 15)
                    .with(s("STORMBRINGER_SUPERCELL_DAMAGE_PER_CHARGE"), .04)
                    .with(s("STORMBRINGER_SUPERCELL_DECAY_TICKS"), 40);
            case 8 -> t.with(s("STORMBRINGER_CHARGE_CAP"), 6)
                    .with(s("STORMBRINGER_CHAIN_COOLDOWN_MULTIPLIER"), .5)
                    .with(s("STORMBRINGER_CHARGE_GAIN_MULTIPLIER"), .5);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("STORMBRINGER_CHAIN_RANGE"), 7.5);
            case 1 -> t.multiply(s("STORMBRINGER_CHAIN_DAMAGE_MULTIPLIER"), 1.12, 1);
            case 2 -> t.with(s("STORMBRINGER_FORK_EXTRA_TARGETS"), 1);
            case 3 -> t.with(s("STORMBRINGER_MARK_DURATION_TICKS"), 80)
                    .with(s("STORMBRINGER_MARK_DAMAGE_MULTIPLIER"), 1.15);
            case 4 -> t.with(s("STORMBRINGER_BURST_RADIUS"), 2)
                    .with(s("STORMBRINGER_BURST_DAMAGE_MULTIPLIER"), .25)
                    .with(s("STORMBRINGER_BURST_TARGET_CAP"), 6);
            case 5 -> t.with(s("STORMBRINGER_KILL_REFUND_CAP"), 2);
            case 6 -> t.with(s("STORMBRINGER_CADENCE_REFUND_PER_TARGET"), 4)
                    .with(s("STORMBRINGER_CADENCE_MIN_COOLDOWN_TICKS"), 8);
            case 7 -> t.with(s("STORMBRINGER_FOCUSED_HIT_COUNT"), 3)
                    .with(s("STORMBRINGER_FOCUSED_HIT_DAMAGE_MULTIPLIER"), .55);
            case 8 -> t.with(s("STORMBRINGER_ROLLING_TARGET_CAP"), 10)
                    .with(s("STORMBRINGER_ROLLING_RANGE_BONUS"), 3)
                    .with(s("STORMBRINGER_ROLLING_JUMP_MULTIPLIER"), .82);
            default -> t;
        };
    }

    private static Phase6AbilityTuning mjolnir(Phase6AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("MJOLNIR_DURATION_BONUS_TICKS"), 40);
            case 1 -> t.with(s("MJOLNIR_PULSE_INTERVAL_TICKS"), 9);
            case 2 -> t.with(s("MJOLNIR_STORM_RADIUS_BONUS"), 2).with(s("MJOLNIR_STORM_TARGET_CAP"), 20);
            case 3 -> t.multiply(s("MJOLNIR_BOLT_DAMAGE_MULTIPLIER"), 1.12, 1);
            case 4 -> t.with(s("MJOLNIR_CONDUCTIVE_BONUS_TICKS"), 40);
            case 5 -> t.with(s("MJOLNIR_FORK_COUNT"), 4).with(s("MJOLNIR_FORK_DAMAGE_MULTIPLIER"), .6)
                    .with(s("MJOLNIR_FORK_RANGE"), 5);
            case 6 -> t.with(s("MJOLNIR_FINALE_PER_TARGET_MULTIPLIER"), .05)
                    .with(s("MJOLNIR_FINALE_TARGET_CAP"), 6);
            case 7 -> t.with(s("MJOLNIR_DURATION_MULTIPLIER"), 2)
                    .multiply(s("MJOLNIR_BOLT_DAMAGE_MULTIPLIER"), .8, 1);
            case 8 -> t.with(s("MJOLNIR_DURATION_TICKS"), 100).with(s("MJOLNIR_PULSE_INTERVAL_TICKS"), 6)
                    .multiply(s("MJOLNIR_BOLT_DAMAGE_MULTIPLIER"), 1.35, 1)
                    .with(s("MJOLNIR_STORM_RADIUS_MULTIPLIER"), .75);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("MJOLNIR_SPEED_DURATION_TICKS"), 100).with(s("MJOLNIR_SPEED_AMPLIFIER"), 0);
            case 1 -> t.with(s("MJOLNIR_ENTRY_RADIUS"), 2.5).with(s("MJOLNIR_ENTRY_DAMAGE_MULTIPLIER"), .3)
                    .with(s("MJOLNIR_ENTRY_LOCKOUT_TICKS"), 40);
            case 2 -> t.with(s("MJOLNIR_ADVANCE_DISTANCE"), 8).with(s("MJOLNIR_ADVANCE_DAMAGE_MULTIPLIER"), 1.15);
            case 3 -> t.with(s("MJOLNIR_RECALL_ARM_TICKS"), 60);
            case 4 -> t.with(s("MJOLNIR_WAKE_RADIUS"), 2).with(s("MJOLNIR_WAKE_TARGET_CAP"), 4)
                    .with(s("MJOLNIR_WAKE_DAMAGE_MULTIPLIER"), .2).with(s("MJOLNIR_WAKE_LOCKOUT_TICKS"), 20);
            case 5 -> t.with(s("MJOLNIR_SKYBOUND_DURATION_TICKS"), 40).with(s("MJOLNIR_SKYBOUND_AMPLIFIER"), 1);
            case 6 -> t.with(s("MJOLNIR_FLASH_SPEED_TICKS"), 60).with(s("MJOLNIR_FLASH_SPEED_AMPLIFIER"), 1)
                    .with(s("MJOLNIR_FLASH_REFUND_TICKS"), 20);
            case 7 -> t.with(s("MJOLNIR_RIDE_RESISTANCE_TICKS"), 20);
            case 8 -> t.with(s("MJOLNIR_ANCHOR_RADIUS"), 3);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("MJOLNIR_SHELL_ABSORPTION"), 4).with(s("MJOLNIR_SHELL_DURATION_TICKS"), 80);
            case 1 -> t.with(s("MJOLNIR_CONDUCTIVE_INCOMING_MULTIPLIER"), .88);
            case 2 -> t.with(s("MJOLNIR_REBUKE_DAMAGE_MULTIPLIER"), .2)
                    .with(s("MJOLNIR_REBUKE_LOCKOUT_TICKS"), 30);
            case 3 -> t.multiply(s("MJOLNIR_BURST_KNOCKBACK_MULTIPLIER"), 1.15, 1)
                    .multiply(s("MJOLNIR_BURST_KNOCKUP_MULTIPLIER"), 1.25, 1);
            case 4 -> t.with(s("MJOLNIR_SHELTER_AMPLIFIER"), 1);
            case 5 -> t.with(s("MJOLNIR_FINAL_BOLT_COUNT"), 4);
            case 6 -> t.with(s("MJOLNIR_FINAL_RADIUS_BONUS"), 1.5)
                    .multiply(s("MJOLNIR_FINAL_DAMAGE_MULTIPLIER"), 1.25, 1)
                    .with(s("MJOLNIR_FINAL_TARGET_CAP"), 24);
            case 7 -> t.with(s("MJOLNIR_AEGIS_ABSORPTION"), 8).with(s("MJOLNIR_AEGIS_DURATION_TICKS"), 120)
                    .with(s("MJOLNIR_AEGIS_AMPLIFIER"), 1);
            case 8 -> t.multiply(s("MJOLNIR_FINAL_DAMAGE_MULTIPLIER"), 2, 1)
                    .multiply(s("MJOLNIR_FINAL_KNOCKBACK_MULTIPLIER"), 1.5, 1);
            default -> t;
        };
    }

    private static Phase6AbilityTuning thunderbrand(Phase6AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("THUNDERBRAND_REFRESH_CHANCE_BONUS"), 5);
            case 1 -> t.with(s("THUNDERBRAND_COOLDOWN_REDUCTION_TICKS"), 25);
            case 2 -> t.with(s("THUNDERBRAND_CHARGE_REDUCTION_TICKS"), 6);
            case 3 -> t.with(s("THUNDERBRAND_DASH_BONUS_TICKS"), 3);
            case 4 -> t.multiply(s("THUNDERBRAND_DASH_SPEED_MULTIPLIER"), 1.12, 1);
            case 5 -> t.with(s("THUNDERBRAND_COLLISION_RADIUS_BONUS"), .5);
            case 6 -> t.with(s("THUNDERBRAND_BLITZ_BURST_RADIUS"), 3)
                    .with(s("THUNDERBRAND_BLITZ_BURST_DAMAGE_MULTIPLIER"), .45)
                    .with(s("THUNDERBRAND_BLITZ_BURST_TARGET_CAP"), 10);
            case 7 -> t.with(s("THUNDERBRAND_DASH_DURATION_TICKS"), 8)
                    .multiply(s("THUNDERBRAND_DASH_SPEED_MULTIPLIER"), 1.5, 1)
                    .with(s("THUNDERBRAND_BLITZ_BURST_DAMAGE_MULTIPLIER"), 1)
                    .with(s("THUNDERBRAND_STEERING_MULTIPLIER"), 0);
            case 8 -> t.with(s("THUNDERBRAND_DASH_DURATION_MULTIPLIER"), 2)
                    .multiply(s("THUNDERBRAND_COLLISION_DAMAGE_MULTIPLIER"), .65, 1)
                    .multiply(s("THUNDERBRAND_BURST_DAMAGE_MULTIPLIER"), .65, 1)
                    .with(s("THUNDERBRAND_STEERING_MULTIPLIER"), 1.5);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("THUNDERBRAND_DEFENSE_PADDING_TICKS"), 6);
            case 1 -> t.with(s("THUNDERBRAND_MEMORY_TICKS"), 40);
            case 2 -> t.with(s("THUNDERBRAND_STORED_HIT_CAP"), 17);
            case 3 -> t.with(s("THUNDERBRAND_ABSORPTION_PER_HIT"), 2)
                    .with(s("THUNDERBRAND_ABSORPTION_CAP"), 8)
                    .with(s("THUNDERBRAND_ABSORPTION_DURATION_TICKS"), 40);
            case 4 -> t.with(s("THUNDERBRAND_RETALIATORY_SLOW_TICKS"), 30);
            case 5 -> t.with(s("THUNDERBRAND_OVERLOAD_THRESHOLD"), 8)
                    .with(s("THUNDERBRAND_OVERLOAD_DAMAGE_MULTIPLIER"), 1.2);
            case 6 -> t.with(s("THUNDERBRAND_CAP_REFUND_TICKS"), 40);
            case 7 -> t.with(s("THUNDERBRAND_GROUNDED_DAMAGE_PER_HIT"), .12);
            case 8 -> t.with(s("THUNDERBRAND_INSULATOR_DAMAGE_MULTIPLIER"), 1.4);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("THUNDERBRAND_CHAIN_EXTRA_TARGETS"), 1)
                    .with(s("THUNDERBRAND_CHAIN_TARGET_CAP"), 6);
            case 1 -> t.with(s("THUNDERBRAND_CHAIN_RANGE_BONUS"), 1.5);
            case 2 -> t.multiply(s("THUNDERBRAND_CHAIN_DAMAGE_MULTIPLIER"), 1.15, 1);
            case 3 -> t.with(s("THUNDERBRAND_CONDUCTOR_DAMAGE_MULTIPLIER"), 1.2)
                    .with(s("THUNDERBRAND_CONDUCTOR_DURATION_TICKS"), 60);
            case 4 -> t.with(s("THUNDERBRAND_CROSSCURRENT_DAMAGE_MULTIPLIER"), .4);
            case 5 -> t.with(s("THUNDERBRAND_ARC_WAKE_RADIUS"), 2)
                    .with(s("THUNDERBRAND_ARC_WAKE_DAMAGE_MULTIPLIER"), .2)
                    .with(s("THUNDERBRAND_ARC_WAKE_TARGET_CAP"), 4);
            case 6 -> t.with(s("THUNDERBRAND_FINAL_RADIUS"), 3.5)
                    .with(s("THUNDERBRAND_FINAL_DAMAGE_MULTIPLIER"), .6)
                    .with(s("THUNDERBRAND_FINAL_TARGET_CAP"), 12);
            case 7 -> t.with(s("THUNDERBRAND_RAIL_HIT_CAP"), 6)
                    .with(s("THUNDERBRAND_RAIL_DAMAGE_PER_HIT"), .3);
            case 8 -> t.with(s("THUNDERBRAND_WEB_CHAIN_COUNT"), 2)
                    .with(s("THUNDERBRAND_WEB_DAMAGE_MULTIPLIER"), .55);
            default -> t;
        };
    }

    private static Phase6AbilityTuning tempest(Phase6AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.with(s("TEMPEST_START_RADIUS_BONUS"), 1);
            case 1 -> t.multiply(s("TEMPEST_RADIUS_PER_STACK_MULTIPLIER"), 1.1, 1);
            case 2 -> t.multiply(s("TEMPEST_VORTEX_DAMAGE_MULTIPLIER"), 1.1, 1);
            case 3 -> t.with(s("TEMPEST_VORTEX_PULL_STRENGTH"), 1.2);
            case 4 -> t.with(s("TEMPEST_SHELL_STACK_THRESHOLD"), 5)
                    .with(s("TEMPEST_SHELL_ABSORPTION"), 4)
                    .with(s("TEMPEST_SHELL_DURATION_TICKS"), 80);
            case 5 -> t.with(s("TEMPEST_DURATION_BONUS_TICKS"), 40)
                    .with(s("TEMPEST_SHRINK_INTERVAL_MULTIPLIER"), 1.25);
            case 6 -> t.with(s("TEMPEST_MAX_CADENCE_INTERVAL_TICKS"), 8)
                    .with(s("TEMPEST_PULSE_TARGET_CAP"), 24);
            case 7 -> t.multiply(s("TEMPEST_MAX_SIZE_MULTIPLIER"), .75, 1)
                    .multiply(s("TEMPEST_VORTEX_DAMAGE_MULTIPLIER"), .75, 1)
                    .with(s("TEMPEST_SPEED_AMPLIFIER"), 0);
            case 8 -> t.multiply(s("TEMPEST_MAX_SIZE_MULTIPLIER"), 1.35, 1)
                    .multiply(s("TEMPEST_VORTEX_DAMAGE_MULTIPLIER"), 1.35, 1)
                    .with(s("TEMPEST_FIXED_RANGE"), 12);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("TEMPEST_MARK_DURATION_BONUS_TICKS"), 200);
            case 1 -> t.with(s("TEMPEST_MARK_STACK_CAP"), 12);
            case 2 -> t.with(s("TEMPEST_SHIFTING_ATTACK_COUNT"), 3);
            case 3 -> t.multiply(s("TEMPEST_FIRE_DAMAGE_MULTIPLIER"), 1.12, 1);
            case 4 -> t.with(s("TEMPEST_FROST_SLOW_BONUS"), .05)
                    .with(s("TEMPEST_FROST_SLOW_CAP"), .45);
            case 5 -> t.with(s("TEMPEST_BALANCED_ATTACK_COUNT"), 4);
            case 6 -> t.with(s("TEMPEST_SEQUENCE_WINDOW_TICKS"), 100)
                    .with(s("TEMPEST_SEQUENCE_HASTE_TICKS"), 100)
                    .with(s("TEMPEST_SEQUENCE_HASTE_AMPLIFIER"), 0);
            case 7 -> t.with(s("TEMPEST_SPECIALIST_LOCK_TICKS"), 200)
                    .with(s("TEMPEST_SPECIALIST_POTENCY_MULTIPLIER"), 1.5);
            case 8 -> t.with(s("TEMPEST_PRISMATIC_STACK_CAP"), 6)
                    .multiply(s("TEMPEST_PRISMATIC_DAMAGE_MULTIPLIER"), 1.25, 1);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("TEMPEST_RECALL_RETAIN_COUNT"), 1);
            case 1 -> t.with(s("TEMPEST_CONSUMED_DAMAGE_PER_STACK"), .12);
            case 2 -> t.with(s("TEMPEST_THERMAL_RADIUS"), 3)
                    .with(s("TEMPEST_THERMAL_DAMAGE_MULTIPLIER"), .4)
                    .with(s("TEMPEST_THERMAL_TARGET_CAP"), 10);
            case 3 -> t.with(s("TEMPEST_RIME_STACK_THRESHOLD"), 4)
                    .with(s("TEMPEST_RIME_HIT_COUNT"), 3)
                    .with(s("TEMPEST_RIME_ROOT_TICKS"), 20);
            case 4 -> t.with(s("TEMPEST_STEAM_PULSE_COUNT"), 5)
                    .with(s("TEMPEST_STEAM_RADIUS"), 3)
                    .with(s("TEMPEST_STEAM_DAMAGE_MULTIPLIER"), .3)
                    .with(s("TEMPEST_STEAM_TARGET_CAP"), 8);
            case 5 -> t.with(s("TEMPEST_RESISTANCE_AMPLIFIER"), 0);
            case 6 -> t.with(s("TEMPEST_FINAL_DAMAGE_PER_STACK"), .1)
                    .with(s("TEMPEST_FINAL_STACK_CAP"), 20)
                    .with(s("TEMPEST_FINAL_RADIUS"), 4)
                    .with(s("TEMPEST_FINAL_TARGET_CAP"), 16);
            case 7 -> t.with(s("TEMPEST_SINGULARITY_RADIUS"), 4)
                    .with(s("TEMPEST_SINGULARITY_PULL_MULTIPLIER"), 1.8)
                    .with(s("TEMPEST_SINGULARITY_DAMAGE_MULTIPLIER"), 1.8)
                    .with(s("TEMPEST_SINGULARITY_DURATION_MULTIPLIER"), .5);
            case 8 -> t.with(s("TEMPEST_WAVE_COUNT"), 2)
                    .with(s("TEMPEST_WAVE_DAMAGE_MULTIPLIER"), .75)
                    .with(s("TEMPEST_WAVE_WIDTH"), 3)
                    .with(s("TEMPEST_WAVE_TARGET_CAP"), 16);
            default -> t;
        };
    }

    private static Phase6AbilityTuning frostfall(Phase6AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.multiply(s("FROSTFALL_DIRECT_DAMAGE_MULTIPLIER"), 1.12, 1);
            case 1 -> t.with(s("FROSTFALL_DIRECT_SLOW_TICKS"), 40);
            case 2 -> t.with(s("FROSTFALL_HIGH_ARC_FLIGHT_TICKS"), 10)
                    .with(s("FROSTFALL_HIGH_ARC_DAMAGE_MULTIPLIER"), .2);
            case 3 -> t.with(s("FROSTFALL_SLOWED_DAMAGE_MULTIPLIER"), 1.15);
            case 4 -> t.with(s("FROSTFALL_DIRECT_FREEZE_TICKS"), 30);
            case 5 -> t.with(s("FROSTFALL_SPLINTER_COUNT"), 3)
                    .with(s("FROSTFALL_SPLINTER_DAMAGE_MULTIPLIER"), .25)
                    .with(s("FROSTFALL_SPLINTER_RANGE"), 4);
            case 6 -> t.with(s("FROSTFALL_MARK_RANGE"), 12)
                    .with(s("FROSTFALL_MARK_DURATION_TICKS"), 100)
                    .with(s("FROSTFALL_MARK_PULSE_DAMAGE_MULTIPLIER"), 1.2);
            case 7 -> t.with(s("FROSTFALL_DEADFALL_ANGLE_DEGREES"), 35)
                    .with(s("FROSTFALL_DEADFALL_DAMAGE_MULTIPLIER"), 1.8)
                    .with(s("FROSTFALL_DEADFALL_PULSE_DAMAGE_MULTIPLIER"), .5);
            case 8 -> t.with(s("FROSTFALL_COOLDOWN_MULTIPLIER"), .5)
                    .with(s("FROSTFALL_SKIRMISHER_DAMAGE_MULTIPLIER"), .7)
                    .multiply(s("FROSTFALL_RETURN_SPEED_MULTIPLIER"), 1.25, 1);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.multiply(s("FROSTFALL_RETURN_SPEED_MULTIPLIER"), 1.15, 1);
            case 1 -> t.with(s("FROSTFALL_RETURN_TARGET_CAP"), 6)
                    .with(s("FROSTFALL_RETURN_DAMAGE_MULTIPLIER"), .35);
            case 2 -> t.with(s("FROSTFALL_RETURN_PULL_STRENGTH"), .2);
            case 3 -> t.with(s("FROSTFALL_CATCH_SPEED_TICKS"), 60)
                    .with(s("FROSTFALL_CATCH_SPEED_AMPLIFIER"), 0);
            case 4 -> t.with(s("FROSTFALL_CATCH_REFUND_TICKS"), 15);
            case 5 -> t.with(s("FROSTFALL_RETURN_ROOT_TICKS"), 20);
            case 6 -> t.with(s("FROSTFALL_PERFECT_RETURN_WINDOW_TICKS"), 40)
                    .with(s("FROSTFALL_PERFECT_RETURN_DAMAGE_MULTIPLIER"), 1.2)
                    .with(s("FROSTFALL_PERFECT_RETURN_DURATION_TICKS"), 80);
            case 7 -> t.with(s("FROSTFALL_ORBIT_RADIUS"), 5)
                    .with(s("FROSTFALL_ORBIT_DAMAGE_MULTIPLIER"), .5)
                    .with(s("FROSTFALL_ORBIT_DURATION_TICKS"), 20);
            case 8 -> t.with(s("FROSTFALL_RECALL_DAMAGE_MULTIPLIER"), .8);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("FROSTFALL_PULSE_RADIUS_BONUS"), 1)
                    .with(s("FROSTFALL_PULSE_TARGET_CAP"), 20);
            case 1 -> t.multiply(s("FROSTFALL_PULSE_DAMAGE_MULTIPLIER"), 1.1, 1);
            case 2 -> t.with(s("FROSTFALL_PULSE_SLOW_BONUS_TICKS"), 20);
            case 3 -> t.multiply(s("FROSTFALL_PULSE_PULL_MULTIPLIER"), 1.25, 1);
            case 4 -> t.with(s("FROSTFALL_SPIKE_PULSE_INDEX"), 3)
                    .with(s("FROSTFALL_SPIKE_COUNT"), 5)
                    .with(s("FROSTFALL_SPIKE_DAMAGE_MULTIPLIER"), .2);
            case 5 -> t.with(s("FROSTFALL_PERMAFROST_DURATION_TICKS"), 60)
                    .with(s("FROSTFALL_PERMAFROST_AMPLIFIER"), 2);
            case 6 -> t.with(s("FROSTFALL_SHATTER_DAMAGE_MULTIPLIER"), 1.4);
            case 7 -> t.with(s("FROSTFALL_FIELD_PULSE_COUNT"), 8)
                    .with(s("FROSTFALL_FIELD_DURATION_TICKS"), 100)
                    .with(s("FROSTFALL_AVALANCHE_DAMAGE_MULTIPLIER"), .65)
                    .with(s("FROSTFALL_AVALANCHE_PULL_MULTIPLIER"), 0);
            case 8 -> t.with(s("FROSTFALL_FIELD_PULSE_COUNT"), 1)
                    .with(s("FROSTFALL_GLACIER_DELAY_TICKS"), 30)
                    .with(s("FROSTFALL_GLACIER_DAMAGE_MULTIPLIER"), 2.5)
                    .with(s("FROSTFALL_GLACIER_PULL_MULTIPLIER"), 2);
            default -> t;
        };
    }

    private static Phase6AbilityTuning icewhisper(Phase6AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.multiply(s("ICEWHISPER_AURA_DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.add(s("ICEWHISPER_AURA_RADIUS_BONUS"), 1, 0)
                    .with(s("ICEWHISPER_AURA_TARGET_CAP"), 20);
            case 2 -> t.add(s("ICEWHISPER_AURA_SLOW_BONUS_TICKS"), 20, 0);
            case 3 -> t.with(s("ICEWHISPER_DWELL_TICKS"), 60)
                    .with(s("ICEWHISPER_DWELL_DAMAGE_MULTIPLIER"), .3)
                    .with(s("ICEWHISPER_DWELL_LOCKOUT_TICKS"), 60);
            case 4 -> t.with(s("ICEWHISPER_FREEZE_PER_PULSE_TICKS"), 20)
                    .with(s("ICEWHISPER_FREEZE_CAP_TICKS"), 100);
            case 5 -> t.with(s("ICEWHISPER_FROZEN_THRESHOLD_TICKS"), 60)
                    .multiply(s("ICEWHISPER_FROZEN_DAMAGE_MULTIPLIER"), 1.15, 1);
            case 6 -> t.with(s("ICEWHISPER_INNER_RADIUS"), 2).with(s("ICEWHISPER_INNER_AMPLIFIER"), 4);
            case 7 -> t.multiply(s("ICEWHISPER_AURA_RADIUS_MULTIPLIER"), .65, 1)
                    .multiply(s("ICEWHISPER_AURA_DAMAGE_MULTIPLIER"), 1.8, 1)
                    .with(s("ICEWHISPER_SLOW_RADIUS"), 2);
            case 8 -> t.multiply(s("ICEWHISPER_AURA_RADIUS_MULTIPLIER"), 2, 1)
                    .multiply(s("ICEWHISPER_AURA_DAMAGE_MULTIPLIER"), .55, 1)
                    .with(s("ICEWHISPER_AURA_INCOMING_MULTIPLIER"), .9);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.add(s("ICEWHISPER_FALL_REDUCTION_TICKS"), 4, 0);
            case 1 -> t.add(s("ICEWHISPER_SPLASH_RADIUS_BONUS"), .5, 0)
                    .with(s("ICEWHISPER_SPLASH_TARGET_CAP"), 8);
            case 2 -> t.multiply(s("ICEWHISPER_COMET_DAMAGE_MULTIPLIER"), 1.12, 1);
            case 3 -> t.with(s("ICEWHISPER_WAVE_INTERVAL_TICKS"), 12);
            case 4 -> t.with(s("ICEWHISPER_EXTRA_COMET_WAVE_INTERVAL"), 3)
                    .with(s("ICEWHISPER_EXTRA_COMET_COUNT"), 1);
            case 5 -> t.with(s("ICEWHISPER_HUNTER_RANGE"), 8).with(s("ICEWHISPER_HUNTER_COMET_COUNT"), 1);
            case 6 -> t.with(s("ICEWHISPER_FRACTURE_WINDOW_TICKS"), 60)
                    .with(s("ICEWHISPER_FRACTURE_PER_STACK_MULTIPLIER"), .15)
                    .with(s("ICEWHISPER_FRACTURE_STACK_CAP"), 3);
            case 7 -> t.multiply(s("ICEWHISPER_COMET_COUNT_MULTIPLIER"), 2, 1)
                    .multiply(s("ICEWHISPER_COMET_DAMAGE_MULTIPLIER"), .55, 1)
                    .with(s("ICEWHISPER_SPLASH_RADIUS"), 1.5)
                    .add(s("ICEWHISPER_STORM_DURATION_BONUS_TICKS"), 40, 0);
            case 8 -> t.with(s("ICEWHISPER_COMET_COUNT"), 1)
                    .multiply(s("ICEWHISPER_COMET_DAMAGE_MULTIPLIER"), 2.2, 1)
                    .with(s("ICEWHISPER_SPLASH_RADIUS"), 4)
                    .with(s("ICEWHISPER_WAVE_INTERVAL_TICKS"), 24);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("ICEWHISPER_WARD_ABSORPTION"), 4)
                    .with(s("ICEWHISPER_WARD_DURATION_TICKS"), 80);
            case 1 -> t.with(s("ICEWHISPER_BLIND_TICKS"), 30);
            case 2 -> t.with(s("ICEWHISPER_PROJECTILE_INCOMING_MULTIPLIER"), .85);
            case 3 -> t.with(s("ICEWHISPER_STEP_RADIUS"), 3)
                    .with(s("ICEWHISPER_STEP_SPEED_TICKS"), 40)
                    .with(s("ICEWHISPER_STEP_SPEED_AMPLIFIER"), 0);
            case 4 -> t.with(s("ICEWHISPER_REBUKE_FREEZE_TICKS"), 20)
                    .with(s("ICEWHISPER_REBUKE_LOCKOUT_TICKS"), 30);
            case 5 -> t.with(s("ICEWHISPER_SLOW_ATTACK_MULTIPLIER"), .9)
                    .with(s("ICEWHISPER_SLOW_ATTACK_TICKS"), 40);
            case 6 -> t.with(s("ICEWHISPER_LAST_SNOW_HEALTH_PERCENT"), 35)
                    .with(s("ICEWHISPER_LAST_SNOW_RESISTANCE_TICKS"), 60)
                    .with(s("ICEWHISPER_LAST_SNOW_AMPLIFIER"), 1);
            case 7 -> t.with(s("ICEWHISPER_GLOBE_AMPLIFIER"), 0);
            case 8 -> t.with(s("ICEWHISPER_BLACK_ICE_MULTIPLIER"), 1.6);
            default -> t;
        };
    }

    private static Phase6AbilityTuning livyatan(Phase6AbilityTuning t, int branch, int slot) {
        if (branch == 0) return switch (slot) {
            case 0 -> t.multiply(s("LIVYATAN_WAVE_DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.with(s("LIVYATAN_WAVE_WIDTH_BONUS"), 1);
            case 2 -> t.with(s("LIVYATAN_WAVE_LENGTH_BONUS_STEPS"), 2);
            case 3 -> t.with(s("LIVYATAN_WAVE_KNOCKBACK_MULTIPLIER"), 1.2);
            case 4 -> t.with(s("LIVYATAN_WAVE_FINAL_DAMAGE_MULTIPLIER"), 1.25);
            case 5 -> t.with(s("LIVYATAN_WAVE_SLOW_TICKS"), 30);
            case 6 -> t.with(s("LIVYATAN_DOUBLE_SWING_COUNT"), 4)
                    .with(s("LIVYATAN_DOUBLE_DELAY_TICKS"), 6)
                    .with(s("LIVYATAN_DOUBLE_DAMAGE_MULTIPLIER"), .55);
            case 7 -> t.with(s("LIVYATAN_WALL_WIDTH_BONUS"), 2)
                    .with(s("LIVYATAN_WALL_TARGET_CAP"), 12)
                    .with(s("LIVYATAN_WALL_DAMAGE_MULTIPLIER"), .6)
                    .with(s("LIVYATAN_WALL_KNOCKBACK_MULTIPLIER"), 1.8);
            case 8 -> t.with(s("LIVYATAN_LANCE_WIDTH"), 2)
                    .with(s("LIVYATAN_LANCE_LENGTH_MULTIPLIER"), 1.5)
                    .with(s("LIVYATAN_LANCE_DAMAGE_MULTIPLIER"), 1.75)
                    .with(s("LIVYATAN_LANCE_KNOCKBACK_MULTIPLIER"), 0);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("LIVYATAN_RETURN_SPEED_MULTIPLIER"), 1.15);
            case 1 -> t.with(s("LIVYATAN_RETURN_PULL_MULTIPLIER"), 1.25);
            case 2 -> t.with(s("LIVYATAN_RETURN_RADIUS_BONUS"), 1);
            case 3 -> t.with(s("LIVYATAN_RETURN_LIGHTNING_CHANCE_BONUS"), 10);
            case 4 -> t.multiply(s("LIVYATAN_RETURN_LIGHTNING_DAMAGE_MULTIPLIER"), 1.15, 1);
            case 5 -> t.with(s("LIVYATAN_RETURN_ROOT_DISTANCE"), 2)
                    .with(s("LIVYATAN_RETURN_ROOT_TICKS"), 20);
            case 6 -> t.with(s("LIVYATAN_CATCH_HIT_THRESHOLD"), 3)
                    .with(s("LIVYATAN_CATCH_REFUND_TICKS"), 20);
            case 7 -> t.with(s("LIVYATAN_MAELSTROM_ROTATIONS"), 2)
                    .with(s("LIVYATAN_MAELSTROM_DURATION_TICKS"), 40)
                    .with(s("LIVYATAN_MAELSTROM_DAMAGE_MULTIPLIER"), .35)
                    .with(s("LIVYATAN_MAELSTROM_PULL_MULTIPLIER"), 1.5);
            case 8 -> t.with(s("LIVYATAN_THUNDERHEAD_LIGHTNING_MULTIPLIER"), .8)
                    .with(s("LIVYATAN_THUNDERHEAD_TARGET_CAP"), 8);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.multiply(s("LIVYATAN_THROW_DAMAGE_MULTIPLIER"), 1.12, 1);
            case 1 -> t.with(s("LIVYATAN_SPLASH_RADIUS"), 2.5)
                    .with(s("LIVYATAN_SPLASH_DAMAGE_MULTIPLIER"), .25)
                    .with(s("LIVYATAN_SPLASH_TARGET_CAP"), 6);
            case 2 -> t.with(s("LIVYATAN_CONDUCTION_RANGE"), 4)
                    .with(s("LIVYATAN_CONDUCTION_DAMAGE_MULTIPLIER"), .2)
                    .with(s("LIVYATAN_CONDUCTION_TARGET_COUNT"), 1);
            case 3 -> t.with(s("LIVYATAN_STEERING_RANGE"), 5)
                    .with(s("LIVYATAN_STEERING_DEGREES"), 15);
            case 4 -> t.with(s("LIVYATAN_SURGE_HIT_COUNT"), 3)
                    .with(s("LIVYATAN_SURGE_WINDOW_TICKS"), 60)
                    .with(s("LIVYATAN_SURGE_DAMAGE_MULTIPLIER"), .3);
            case 5 -> t.with(s("LIVYATAN_MARK_DURATION_TICKS"), 80)
                    .with(s("LIVYATAN_MARK_WAVE_DAMAGE_MULTIPLIER"), 1.15);
            case 6 -> t.with(s("LIVYATAN_PERFECT_STORM_WINDOW_TICKS"), 120)
                    .with(s("LIVYATAN_PERFECT_STORM_RADIUS"), 3)
                    .with(s("LIVYATAN_PERFECT_STORM_DAMAGE_MULTIPLIER"), .75)
                    .with(s("LIVYATAN_PERFECT_STORM_TARGET_CAP"), 16);
            case 7 -> t.with(s("LIVYATAN_UNBOUND_WAVE_DAMAGE_MULTIPLIER"), 1.3)
                    .with(s("LIVYATAN_UNBOUND_COOLDOWN_MULTIPLIER"), 2);
            case 8 -> t.with(s("LIVYATAN_CALM_DAMAGE_MULTIPLIER"), 1.5)
                    .with(s("LIVYATAN_ACTIVE_COOLDOWN_BONUS_TICKS"), 20)
                    .with(s("LIVYATAN_SUPPRESS_WAVES"), 1);
            default -> t;
        };
    }

    private static boolean matches(int profile, int branch, int slot, UniqueAbilityDefinition definition) {
        return switch (profile) {
            case 0 -> branch == 0 ? definition == Phase6UniqueAbilities.STORMBRINGER_GUARD
                    : branch == 2 ? definition == Phase6UniqueAbilities.STORMBRINGER_CHAIN
                    : switch (slot) {
                        case 0, 3, 7, 8 -> definition == Phase6UniqueAbilities.STORMBRINGER_GUARD
                                || definition == Phase6UniqueAbilities.STORMBRINGER_CHAIN;
                        default -> definition == Phase6UniqueAbilities.STORMBRINGER_CHAIN;
                    };
            case 1 -> definition == Phase6UniqueAbilities.MJOLNIR_STORM;
            case 2 -> branch == 0 && slot == 0
                    ? definition == Phase6UniqueAbilities.THUNDERBRAND_REFRESH
                    : definition == Phase6UniqueAbilities.THUNDERBRAND_BLITZ;
            case 3 -> branch == 0 || branch == 2
                    ? definition == Phase6UniqueAbilities.TEMPEST_VORTEX
                    : slot == 8
                    ? definition == Phase6UniqueAbilities.TEMPEST_MARK
                            || definition == Phase6UniqueAbilities.TEMPEST_VORTEX
                    : definition == Phase6UniqueAbilities.TEMPEST_MARK;
            case 4 -> branch == 2 ? definition == Phase6UniqueAbilities.FROSTFALL_FIELD
                    : definition == Phase6UniqueAbilities.FROSTFALL_THROW;
            case 5 -> branch == 0 ? definition == Phase6UniqueAbilities.ICEWHISPER_AURA
                    : branch == 1 ? definition == Phase6UniqueAbilities.ICEWHISPER_COMETS
                    : switch (slot) {
                        case 2, 5 -> definition == Phase6UniqueAbilities.ICEWHISPER_AURA;
                        default -> definition == Phase6UniqueAbilities.ICEWHISPER_COMETS;
                    };
            case 6 -> branch == 0 ? definition == Phase6UniqueAbilities.LIVYATAN_WAVE
                    : branch == 1 ? definition == Phase6UniqueAbilities.LIVYATAN_RETURN
                            || (slot == 4 || slot == 8) && definition == Phase6UniqueAbilities.LIVYATAN_WAVE
                    : switch (slot) {
                        case 0, 1 -> definition == Phase6UniqueAbilities.LIVYATAN_THROW;
                        case 2 -> definition == Phase6UniqueAbilities.LIVYATAN_RETURN
                                || definition == Phase6UniqueAbilities.LIVYATAN_WAVE;
                        case 3, 4, 7 -> definition == Phase6UniqueAbilities.LIVYATAN_WAVE;
                        case 5 -> definition == Phase6UniqueAbilities.LIVYATAN_RETURN
                                || definition == Phase6UniqueAbilities.LIVYATAN_WAVE;
                        case 6, 8 -> definition == Phase6UniqueAbilities.LIVYATAN_THROW
                                || definition == Phase6UniqueAbilities.LIVYATAN_RETURN
                                || definition == Phase6UniqueAbilities.LIVYATAN_WAVE;
                        default -> false;
                    };
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
