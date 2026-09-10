package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.SimplyMastery;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.SimplySwordsAPI;
import net.sweenus.simplyswords.api.SpellScalingProfile;
import net.sweenus.simplyswords.api.ability.BuiltinUniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityContext;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityEvent;
import net.sweenus.simplyswords.api.ability.UniqueAbilityPhase;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;

import java.util.List;
import java.util.Map;

final class BrimstoneAbilitySkillEffects implements AbilitySkillEffectType {
    private final Kind kind;
    private final Identifier id;

    private BrimstoneAbilitySkillEffects(Kind kind) {
        this.kind = kind;
        this.id = Identifier.of(SimplyMastery.MOD_ID, kind.path);
    }

    static List<SkillEffectType> all() {
        return java.util.Arrays.stream(Kind.values()).map(BrimstoneAbilitySkillEffects::new)
                .map(effect -> (SkillEffectType) effect).toList();
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
        Map<String, Range> ranges = ranges(kind);
        for (Map.Entry<String, Integer> entry : effect.parameters().entrySet()) {
            Range range = ranges.get(entry.getKey());
            if (range == null) errors.add(where + "unknown parameter '" + entry.getKey() + "' for " + effect.type());
            else if (!range.contains(entry.getValue())) errors.add(where + entry.getKey()
                    + " must be between " + range.minimum + " and " + range.maximum);
        }
    }

    @Override
    public void tune(UniqueAbilityContext context, UniqueAbilityDefinition definition,
                     UniqueAbilityTuning.Builder tuning, MasteryProfile.Node node) {
        long tick = context.world().getTime();
        switch (kind) {
            case SULFUROUS_EDGE -> eruption(definition,
                    () -> tuning.add(BuiltinUniqueAbilities.BRIMSTONE_PROC_CHANCE,
                            parameter(node, "chance_bonus", 5)));
            case SCORCHING_BRAND -> eruption(definition, () -> tuning
                    .set(BuiltinUniqueAbilities.BRIMSTONE_ERUPTION_FIRE_TICKS,
                            parameter(node, "fire_ticks", 100))
                    .set(BuiltinUniqueAbilities.BRIMSTONE_BURNING_DAMAGE_MULTIPLIER,
                            parameter(node, "burning_damage_percent", 115) / 100.0));
            case BLAST_FURNACE -> eruption(definition, () -> tuning
                    .add(BuiltinUniqueAbilities.BRIMSTONE_ERUPTION_RADIUS,
                            parameter(node, "radius_tenths", 10) / 10.0)
                    .multiply(BuiltinUniqueAbilities.BRIMSTONE_ERUPTION_DAMAGE_MULTIPLIER,
                            parameter(node, "damage_percent", 115) / 100.0));
            case KINDLING_BLOWS -> {
                if (definition == BuiltinUniqueAbilities.BRIMSTONE_ERUPTION) {
                    int stacks = BrimstoneMasteryRuntime.value(context.actor(), context.stack(),
                            BrimstoneMasteryRuntime.KINDLING, tick).amount();
                    tuning.add(BuiltinUniqueAbilities.BRIMSTONE_PROC_CHANCE,
                            stacks * parameter(node, "chance_per_stack", 5));
                }
            }
            case FLASHPOINT -> {
                if (definition == BuiltinUniqueAbilities.BRIMSTONE_ERUPTION
                        && BrimstoneMasteryRuntime.value(context.actor(), context.stack(), BrimstoneMasteryRuntime.KINDLING, tick).amount()
                        >= parameter(node, "required_stacks", 3)) {
                    tuning.set(BuiltinUniqueAbilities.BRIMSTONE_PROC_CHANCE, 100);
                }
            }
            case CINDER_SCATTER -> eruption(definition, () -> tuning
                    .set(BuiltinUniqueAbilities.BRIMSTONE_CINDER_COUNT, parameter(node, "count", 3))
                    .set(BuiltinUniqueAbilities.BRIMSTONE_CINDER_RANGE, parameter(node, "range_tenths", 60) / 10.0)
                    .set(BuiltinUniqueAbilities.BRIMSTONE_CINDER_DAMAGE_MULTIPLIER,
                            parameter(node, "damage_percent", 20) / 100.0)
                    .set(BuiltinUniqueAbilities.BRIMSTONE_CINDER_FIRE_TICKS, parameter(node, "fire_ticks", 40)));
            case BACKDRAFT -> eruption(definition, () -> tuning
                    .set(BuiltinUniqueAbilities.BRIMSTONE_FORCE_MODE, BuiltinUniqueAbilities.BRIMSTONE_FORCE_BACKDRAFT)
                    .set(BuiltinUniqueAbilities.BRIMSTONE_BACKDRAFT_PULL, parameter(node, "pull_tenths", 8) / 10.0)
                    .set(BuiltinUniqueAbilities.BRIMSTONE_SLOWNESS_TICKS, parameter(node, "slowness_ticks", 40)));
            case CHAIN_REACTION -> eruption(definition, () -> tuning
                    .set(BuiltinUniqueAbilities.BRIMSTONE_CHAIN_DAMAGE_MULTIPLIER,
                            parameter(node, "damage_percent", 50) / 100.0)
                    .set(BuiltinUniqueAbilities.BRIMSTONE_CHAIN_RADIUS, parameter(node, "radius_tenths", 30) / 10.0)
                    .set(BuiltinUniqueAbilities.BRIMSTONE_CHAIN_MAX_DETONATIONS,
                            parameter(node, "max_detonations", 8)));
            case CRUCIBLE_STRIKE -> eruption(definition, () -> tuning
                    .set(BuiltinUniqueAbilities.BRIMSTONE_ERUPTION_MODE,
                            BuiltinUniqueAbilities.BRIMSTONE_ERUPTION_CRUCIBLE)
                    .set(BuiltinUniqueAbilities.BRIMSTONE_ERUPTION_RADIUS_MULTIPLIER,
                            parameter(node, "radius_percent", 60) / 100.0)
                    .set(BuiltinUniqueAbilities.BRIMSTONE_PRIMARY_DAMAGE_MULTIPLIER,
                            parameter(node, "primary_damage_percent", 200) / 100.0));
            case LENGTHENED_CHAIN -> rite(definition, () -> tuning.add(
                    BuiltinUniqueAbilities.BRIMSTONE_RITE_TARGET_JUMP_RANGE,
                    parameter(node, "range_tenths", 40) / 10.0));
            case FURNACE_BELLOWS -> rite(definition, () -> tuning.set(
                    BuiltinUniqueAbilities.BRIMSTONE_RITE_PULSE_INTERVAL_TICKS,
                    parameter(node, "interval_ticks", 16)));
            case STOKED_FURNACE -> rite(definition, () -> tuning
                    .add(BuiltinUniqueAbilities.BRIMSTONE_RITE_RADIUS_GROWTH_PER_HIT,
                            parameter(node, "growth_hundredths", 15) / 100.0)
                    .add(BuiltinUniqueAbilities.BRIMSTONE_RITE_MAX_RADIUS,
                            parameter(node, "max_radius_tenths", 10) / 10.0));
            case SHACKLING_HEAT -> rite(definition, () -> tuning
                    .set(BuiltinUniqueAbilities.BRIMSTONE_RITE_PULSE_PULL,
                            parameter(node, "pull_tenths", 4) / 10.0)
                    .set(BuiltinUniqueAbilities.BRIMSTONE_RITE_SLOWNESS_TICKS,
                            parameter(node, "slowness_ticks", 40)));
            case OVERPRESSURE -> rite(definition, () -> tuning
                    .set(BuiltinUniqueAbilities.BRIMSTONE_OVERPRESSURE_PER_PULSE,
                            parameter(node, "per_pulse_percent", 10) / 100.0)
                    .set(BuiltinUniqueAbilities.BRIMSTONE_OVERPRESSURE_CAP,
                            parameter(node, "cap_percent", 50) / 100.0));
            case SNAPBACK -> rite(definition, () -> tuning
                    .set(BuiltinUniqueAbilities.BRIMSTONE_SNAPBACK_DAMAGE_MULTIPLIER,
                            parameter(node, "damage_percent", 35) / 100.0)
                    .set(BuiltinUniqueAbilities.BRIMSTONE_SNAPBACK_RADIUS,
                            parameter(node, "radius_tenths", 30) / 10.0));
            case MOLTEN_WAKE -> rite(definition, () -> tuning
                    .set(BuiltinUniqueAbilities.BRIMSTONE_WAKE_DURATION_TICKS,
                            parameter(node, "duration_ticks", 40))
                    .set(BuiltinUniqueAbilities.BRIMSTONE_WAKE_INTERVAL_TICKS,
                            parameter(node, "interval_ticks", 20))
                    .set(BuiltinUniqueAbilities.BRIMSTONE_WAKE_DAMAGE_MULTIPLIER,
                            parameter(node, "damage_percent", 20) / 100.0)
                    .set(BuiltinUniqueAbilities.BRIMSTONE_WAKE_MIN_MOVE,
                            parameter(node, "min_move_tenths", 15) / 10.0));
            case EXECUTIONERS_DROP -> rite(definition, () -> tuning
                    .set(BuiltinUniqueAbilities.BRIMSTONE_RITE_DURATION_MODE,
                            BuiltinUniqueAbilities.BRIMSTONE_RITE_EXECUTIONER)
                    .set(BuiltinUniqueAbilities.BRIMSTONE_RITE_DURATION_TICKS,
                            parameter(node, "duration_ticks", 60))
                    .set(BuiltinUniqueAbilities.BRIMSTONE_RITE_FINAL_DAMAGE_MULTIPLIER,
                            parameter(node, "final_damage_percent", 225) / 100.0));
            case PERPETUAL_FURNACE -> rite(definition, () -> tuning
                    .set(BuiltinUniqueAbilities.BRIMSTONE_RITE_DURATION_MODE,
                            BuiltinUniqueAbilities.BRIMSTONE_RITE_PERPETUAL)
                    .set(BuiltinUniqueAbilities.BRIMSTONE_RITE_DURATION_TICKS,
                            parameter(node, "duration_ticks", 200))
                    .set(BuiltinUniqueAbilities.BRIMSTONE_PERPETUAL_START_MULTIPLIER,
                            parameter(node, "start_percent", 75) / 100.0)
                    .set(BuiltinUniqueAbilities.BRIMSTONE_PERPETUAL_PER_PULSE,
                            parameter(node, "per_pulse_percent", 5) / 100.0)
                    .set(BuiltinUniqueAbilities.BRIMSTONE_PERPETUAL_CAP,
                            parameter(node, "cap_percent", 150) / 100.0)
                    .set(BuiltinUniqueAbilities.BRIMSTONE_RITE_FINAL_DAMAGE_MULTIPLIER,
                            parameter(node, "final_damage_percent", 50) / 100.0));
            case WALKING_FURNACE -> rite(definition, () -> tuning
                    .set(BuiltinUniqueAbilities.BRIMSTONE_GUARD_MODE,
                            BuiltinUniqueAbilities.BRIMSTONE_GUARD_WALKING)
                    .set(BuiltinUniqueAbilities.BRIMSTONE_WALKING_RADIUS_MULTIPLIER,
                            parameter(node, "radius_percent", 85) / 100.0)
                    .set(BuiltinUniqueAbilities.BRIMSTONE_WALKING_PULL,
                            parameter(node, "pull_tenths", 5) / 10.0));
            case LAST_REPRISAL -> rite(definition, () -> tuning
                    .set(BuiltinUniqueAbilities.BRIMSTONE_GUARD_MODE,
                            BuiltinUniqueAbilities.BRIMSTONE_GUARD_LAST_REPRISAL)
                    .set(BuiltinUniqueAbilities.BRIMSTONE_EMERGENCY_HEALTH_PERCENT,
                            parameter(node, "health_percent", 35))
                    .set(BuiltinUniqueAbilities.BRIMSTONE_EMERGENCY_FINAL_MULTIPLIER,
                            parameter(node, "final_damage_percent", 175) / 100.0));
            default -> {
            }
        }
    }

    @Override
    public void onAbilityEvent(UniqueAbilityEvent event, MasteryProfile.Node node) {
        UniqueAbilityContext context = event.execution().context();
        long tick = context.world().getTime();
        if (event.execution().definition() == BuiltinUniqueAbilities.BRIMSTONE_RITE) {
            if (event.phase() == UniqueAbilityPhase.START) {
                int duration = event.execution().tuning().get(BuiltinUniqueAbilities.BRIMSTONE_RITE_DURATION_TICKS) + 18;
                BrimstoneMasteryRuntime.set(context.actor(), context.stack(), BrimstoneMasteryRuntime.RITE_ACTIVE,
                        1, tick + duration, tick);
            } else if (event.phase() == UniqueAbilityPhase.FINISH || event.phase() == UniqueAbilityPhase.CANCEL) {
                clearRite(context.actor(), context.stack());
            }
        }
        if (event.execution().definition() == BuiltinUniqueAbilities.BRIMSTONE_ERUPTION) {
            if (kind == Kind.KINDLING_BLOWS && event.phase() == UniqueAbilityPhase.CANCEL) {
                BrimstoneMasteryRuntime.advance(context.actor(), context.stack(), BrimstoneMasteryRuntime.KINDLING, tick,
                        parameter(node, "max_stacks", 3), parameter(node, "window_ticks", 80));
            } else if (kind == Kind.KINDLING_BLOWS && event.phase() == UniqueAbilityPhase.START) {
                BrimstoneMasteryRuntime.clear(context.actor(), context.stack(), BrimstoneMasteryRuntime.KINDLING);
            }
            return;
        }
        if (event.execution().definition() != BuiltinUniqueAbilities.BRIMSTONE_RITE) return;
        if (event.phase() == UniqueAbilityPhase.START) {
            onRiteStart(event, node);
            return;
        }
        if (event.phase() != UniqueAbilityPhase.HIT) return;
        if (event.eventId().equals(BuiltinUniqueAbilities.BRIMSTONE_PULSE_FINISH)) {
            onPulseFinish(event, node, tick);
        } else if (kind == Kind.LAST_REPRISAL
                && event.eventId().equals(BuiltinUniqueAbilities.BRIMSTONE_EMERGENCY_PLUNGE)) {
            context.actor().addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION,
                    parameter(node, "duration_ticks", 60), 1, false, false, true), context.actor());
            context.actor().addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,
                    parameter(node, "duration_ticks", 60), 1, false, false, true), context.actor());
        }
    }

    private void onRiteStart(UniqueAbilityEvent event, MasteryProfile.Node node) {
        UniqueAbilityContext context = event.execution().context();
        switch (kind) {
            case CINDER_MANTLE -> context.actor().addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION,
                    parameter(node, "duration_ticks", 80), parameter(node, "amplifier", 0),
                    false, false, true), context.actor());
            case TEMPERED_FLESH -> {
                int duration = event.execution().tuning().get(BuiltinUniqueAbilities.BRIMSTONE_RITE_DURATION_TICKS)
                        + parameter(node, "padding_ticks", 18);
                context.actor().addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE,
                        duration, 0, false, false, true), context.actor());
            }
            case WALKING_FURNACE -> {
                int duration = event.execution().tuning().get(BuiltinUniqueAbilities.BRIMSTONE_RITE_DURATION_TICKS) + 18;
                context.actor().addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,
                        duration, 0, false, false, true), context.actor());
            }
            default -> {
            }
        }
    }

    private void onPulseFinish(UniqueAbilityEvent event, MasteryProfile.Node node, long tick) {
        if (event.affectedTargets() <= 0) return;
        UniqueAbilityContext context = event.execution().context();
        switch (kind) {
            case HEAT_SINK -> {
                int stacks = BrimstoneMasteryRuntime.value(context.actor(), context.stack(),
                        BrimstoneMasteryRuntime.HEAT_SINK, tick).amount();
                if (stacks > 0) {
                    context.actor().addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION,
                            parameter(node, "duration_ticks", 80), stacks - 1,
                            false, false, true), context.actor());
                    BrimstoneMasteryRuntime.clear(context.actor(), context.stack(), BrimstoneMasteryRuntime.HEAT_SINK);
                }
            }
            case FORGED_RESOLVE -> {
                if (context.actor().getHealth() * 100.0F / context.actor().getMaxHealth()
                        <= parameter(node, "health_percent", 40)
                        && BrimstoneMasteryRuntime.value(context.actor(), context.stack(),
                        BrimstoneMasteryRuntime.FORGED_RESOLVE, tick).amount() == 0) {
                    long expiry = BrimstoneMasteryRuntime.value(context.actor(), context.stack(),
                            BrimstoneMasteryRuntime.RITE_ACTIVE, tick).expiresAt();
                    BrimstoneMasteryRuntime.set(context.actor(), context.stack(), BrimstoneMasteryRuntime.FORGED_RESOLVE,
                            1, Math.max(tick + 1, expiry), tick);
                    context.actor().addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,
                            parameter(node, "resistance_ticks", 60), 0, false, false, true), context.actor());
                    context.actor().addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION,
                            parameter(node, "absorption_ticks", 80), 1, false, false, true), context.actor());
                }
            }
            case BULWARK_PULSE -> {
                if (event.affectedTargets() >= parameter(node, "target_threshold", 3)) {
                    context.actor().addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION,
                            parameter(node, "duration_ticks", 60), 0, false, false, true), context.actor());
                }
            }
            default -> {
            }
        }
    }

    @Override
    public void onDamageReceived(SkillEffectContext.DamageReceived context, MasteryProfile.Node node) {
        long tick = context.tick();
        if (BrimstoneMasteryRuntime.value(context.actor(), context.stack(), BrimstoneMasteryRuntime.RITE_ACTIVE, tick).amount() == 0) return;
        switch (kind) {
            case HEAT_SINK -> BrimstoneMasteryRuntime.advance(context.actor(), context.stack(),
                    BrimstoneMasteryRuntime.HEAT_SINK, tick, parameter(node, "max_stacks", 2),
                    parameter(node, "window_ticks", 80));
            case FURNACE_REPRISAL -> reprisal(context, node);
            case ASHEN_STEP -> {
                if (BrimstoneMasteryRuntime.value(context.actor(), context.stack(), BrimstoneMasteryRuntime.ASHEN_STEP, tick).amount() > 0) return;
                BrimstoneMasteryRuntime.set(context.actor(), context.stack(), BrimstoneMasteryRuntime.ASHEN_STEP, 1,
                        tick + parameter(node, "cooldown_ticks", 100), tick);
                context.player().addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,
                        parameter(node, "duration_ticks", 60), 0, false, false, true));
            }
            default -> {
            }
        }
    }

    private static void reprisal(SkillEffectContext.DamageReceived context, MasteryProfile.Node node) {
        LivingEntity attacker = context.attacker();
        if (attacker == null || context.source() == null
                || !context.source().isOf(DamageTypes.PLAYER_ATTACK)
                && !context.source().isOf(DamageTypes.MOB_ATTACK)
                || !SimplySwordsAPI.isValidAbilityTarget(attacker, context.player())) return;
        long tick = context.tick();
        if (BrimstoneMasteryRuntime.value(context.actor(), context.stack(), BrimstoneMasteryRuntime.REPRISAL, tick).amount() > 0) return;
        BrimstoneMasteryRuntime.set(context.actor(), context.stack(), BrimstoneMasteryRuntime.REPRISAL, 1,
                tick + parameter(node, "cooldown_ticks", 20), tick);
        int percent = parameter(node, "damage_percent", 20);
        float pulse = BuiltinUniqueAbilities.BRIMSTONE_RITE_PULSE_DAMAGE_MULTIPLIER.defaultValue().floatValue();
        float damage = SimplySwordsAPI.scaleAbilityDamage(SpellScalingProfile.FIRE, context.player(), context.stack(),
                BuiltinUniqueAbilities.BRIMSTONE_RITE_DAMAGE_SCALING.defaultValue().floatValue() * pulse * percent / 100.0F,
                BuiltinUniqueAbilities.BRIMSTONE_RITE_SPELL_SCALING.defaultValue().floatValue() * pulse * percent / 100.0F);
        attacker.setOnFireFor(Math.max(1, (parameter(node, "fire_ticks", 40) + 19) / 20));
        damage(context.player(), context.stack(), attacker, damage);
    }

    private static boolean damage(ServerPlayerEntity actor, net.minecraft.item.ItemStack stack,
                                  LivingEntity target, float damage) {
        return SkillOriginGuard.run(new SkillOriginGuard.Origin(actor, stack, "brimstone_secondary"),
                () -> SimplySwordsAPI.applyAbilityMagicDamageThroughIframes(actor.getServerWorld(), actor,
                        stack, target, damage, SpellScalingProfile.FIRE));
    }

    private static void clearRite(LivingEntity actor, ItemStack stack) {
        BrimstoneMasteryRuntime.clear(actor, stack, BrimstoneMasteryRuntime.RITE_ACTIVE);
        BrimstoneMasteryRuntime.clear(actor, stack, BrimstoneMasteryRuntime.HEAT_SINK);
        BrimstoneMasteryRuntime.clear(actor, stack, BrimstoneMasteryRuntime.FORGED_RESOLVE);
    }

    private static void eruption(UniqueAbilityDefinition definition, Runnable action) {
        if (definition == BuiltinUniqueAbilities.BRIMSTONE_ERUPTION) action.run();
    }

    private static void rite(UniqueAbilityDefinition definition, Runnable action) {
        if (definition == BuiltinUniqueAbilities.BRIMSTONE_RITE) action.run();
    }

    private static int parameter(MasteryProfile.Node node, String key, int fallback) {
        return node.effect().parameters().getOrDefault(key, fallback);
    }

    private static Map<String, Range> ranges(Kind kind) {
        return switch (kind) {
            case SULFUROUS_EDGE -> map("chance_bonus", 0, 100);
            case SCORCHING_BRAND -> map("fire_ticks", 0, 72000, "burning_damage_percent", 0, 1000);
            case BLAST_FURNACE -> map("radius_tenths", 0, 640, "damage_percent", 0, 1000);
            case KINDLING_BLOWS -> map("chance_per_stack", 0, 100, "max_stacks", 1, 20, "window_ticks", 1, 1200);
            case FLASHPOINT -> map("required_stacks", 1, 20);
            case CINDER_SCATTER -> map("count", 1, 16, "range_tenths", 1, 640,
                    "damage_percent", 0, 1000, "fire_ticks", 0, 72000);
            case BACKDRAFT -> map("pull_tenths", 0, 160, "slowness_ticks", 0, 72000);
            case CHAIN_REACTION -> map("damage_percent", 0, 1000, "radius_tenths", 1, 640,
                    "max_detonations", 1, 64);
            case CRUCIBLE_STRIKE -> map("radius_percent", 0, 1000, "primary_damage_percent", 0, 1000);
            case LENGTHENED_CHAIN -> map("range_tenths", 0, 1280);
            case FURNACE_BELLOWS -> map("interval_ticks", 1, 72000);
            case STOKED_FURNACE -> map("growth_hundredths", 0, 6400, "max_radius_tenths", 0, 640);
            case SHACKLING_HEAT -> map("pull_tenths", 0, 160, "slowness_ticks", 0, 72000);
            case OVERPRESSURE -> map("per_pulse_percent", 0, 1000, "cap_percent", 0, 1000);
            case SNAPBACK -> map("damage_percent", 0, 1000, "radius_tenths", 1, 640);
            case MOLTEN_WAKE -> map("duration_ticks", 1, 72000, "interval_ticks", 1, 72000,
                    "damage_percent", 0, 1000, "min_move_tenths", 1, 640);
            case EXECUTIONERS_DROP -> map("duration_ticks", 1, 72000, "final_damage_percent", 0, 1000);
            case PERPETUAL_FURNACE -> map("duration_ticks", 1, 72000, "start_percent", 0, 1000,
                    "per_pulse_percent", 0, 1000, "cap_percent", 0, 1000, "final_damage_percent", 0, 1000);
            case CINDER_MANTLE -> map("duration_ticks", 1, 72000, "amplifier", 0, 10);
            case TEMPERED_FLESH -> map("padding_ticks", 0, 1200);
            case HEAT_SINK -> map("max_stacks", 1, 10, "window_ticks", 1, 1200, "duration_ticks", 1, 1200);
            case FURNACE_REPRISAL -> map("damage_percent", 0, 1000, "cooldown_ticks", 1, 72000,
                    "fire_ticks", 0, 72000);
            case FORGED_RESOLVE -> map("health_percent", 1, 100, "resistance_ticks", 1, 1200,
                    "absorption_ticks", 1, 1200);
            case ASHEN_STEP -> map("duration_ticks", 1, 1200, "cooldown_ticks", 1, 72000);
            case BULWARK_PULSE -> map("target_threshold", 1, 64, "duration_ticks", 1, 1200);
            case WALKING_FURNACE -> map("radius_percent", 0, 1000, "pull_tenths", 0, 160);
            case LAST_REPRISAL -> map("health_percent", 1, 100, "final_damage_percent", 0, 1000,
                    "duration_ticks", 1, 1200);
        };
    }

    private static Map<String, Range> map(Object... values) {
        Map<String, Range> result = new java.util.LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 3) {
            result.put((String) values[i], new Range((Integer) values[i + 1], (Integer) values[i + 2]));
        }
        return Map.copyOf(result);
    }

    private enum Kind {
        SULFUROUS_EDGE("sulfurous_edge"), SCORCHING_BRAND("scorching_brand"),
        BLAST_FURNACE("blast_furnace"), KINDLING_BLOWS("kindling_blows"), FLASHPOINT("flashpoint"),
        CINDER_SCATTER("cinder_scatter"), BACKDRAFT("backdraft"), CHAIN_REACTION("chain_reaction"),
        CRUCIBLE_STRIKE("crucible_strike"), LENGTHENED_CHAIN("lengthened_chain"),
        FURNACE_BELLOWS("furnace_bellows"), STOKED_FURNACE("stoked_furnace"),
        SHACKLING_HEAT("shackling_heat"), OVERPRESSURE("overpressure"), SNAPBACK("snapback"),
        MOLTEN_WAKE("molten_wake"), EXECUTIONERS_DROP("executioners_drop"),
        PERPETUAL_FURNACE("perpetual_furnace"), CINDER_MANTLE("cinder_mantle"),
        TEMPERED_FLESH("tempered_flesh"), HEAT_SINK("heat_sink"),
        FURNACE_REPRISAL("furnace_reprisal"), FORGED_RESOLVE("forged_resolve"),
        ASHEN_STEP("ashen_step"), BULWARK_PULSE("bulwark_pulse"),
        WALKING_FURNACE("walking_furnace"), LAST_REPRISAL("last_reprisal");

        private final String path;

        Kind(String path) {
            this.path = path;
        }
    }

    private record Range(int minimum, int maximum) {
        boolean contains(int value) {
            return value >= minimum && value <= maximum;
        }
    }
}
