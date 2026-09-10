package net.sweenus.simplyswordsmastery.mastery.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.sweenus.simplyswordsmastery.SimplySwordsMastery;
import net.sweenus.simplyswordsmastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswordsmastery.mastery.state.MasteryRuntimeState;
import net.sweenus.simplyswords.api.SimplySwordsAPI;
import net.sweenus.simplyswords.api.SpellScalingProfile;
import net.sweenus.simplyswords.api.ability.BuiltinUniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityContext;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityEvent;
import net.sweenus.simplyswords.api.ability.UniqueAbilityPhase;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import net.sweenus.simplyswords.api.render.LightningBoltStyle;

import java.util.List;
import java.util.Map;

final class StormAbilitySkillEffects implements AbilitySkillEffectType {
    private final Kind kind;
    private final Identifier id;

    private StormAbilitySkillEffects(Kind kind) {
        this.kind = kind;
        this.id = Identifier.of(SimplySwordsMastery.MOD_ID, kind.path);
    }

    static List<SkillEffectType> all() {
        return java.util.Arrays.stream(Kind.values()).map(StormAbilitySkillEffects::new)
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
            case STORMBREAK_CONDUIT -> stormbreak(definition,
                    () -> tuning.add(BuiltinUniqueAbilities.CORRIDOR_WIDTH, parameter(node, "width_tenths", 10) / 10.0));
            case SLIPSTREAM -> stormbreak(definition, () -> tuning
                    .multiply(BuiltinUniqueAbilities.DASH_DISTANCE, parameter(node, "distance_percent", 120) / 100.0)
                    .multiply(BuiltinUniqueAbilities.DASH_SPEED, parameter(node, "speed_percent", 110) / 100.0));
            case CROSSWIND -> stormbreak(definition,
                    () -> tuning.set(BuiltinUniqueAbilities.CORRIDOR_FORCE_MODE,
                            BuiltinUniqueAbilities.CORRIDOR_FORCE_INWARD));
            case CAPACITOR -> stormbreak(definition, () -> tuning
                    .set(BuiltinUniqueAbilities.THUNDERCLAP_BONUS_PER_CORRIDOR_HIT,
                            parameter(node, "per_hit_percent", 8) / 100.0)
                    .set(BuiltinUniqueAbilities.THUNDERCLAP_CORRIDOR_BONUS_CAP,
                            parameter(node, "cap_percent", 40) / 100.0));
            case STORM_CHASER -> stormbreak(definition, () -> tuning
                    .set(BuiltinUniqueAbilities.STORMBREAK_COOLDOWN_TICKS,
                            (int) Math.round(tuning.get(BuiltinUniqueAbilities.STORMBREAK_COOLDOWN_TICKS)
                                    * parameter(node, "cooldown_percent", 85) / 100.0)));
            case AFTERIMAGE -> stormbreak(definition, () -> tuning
                    .set(BuiltinUniqueAbilities.AFTERIMAGE_DURATION_TICKS, parameter(node, "duration_ticks", 40))
                    .set(BuiltinUniqueAbilities.AFTERIMAGE_DAMAGE_MULTIPLIER,
                            parameter(node, "damage_percent", 30) / 100.0));
            case EYE_OF_STORM -> stormbreak(definition, () -> tuning
                    .set(BuiltinUniqueAbilities.STORMBREAK_MODE, BuiltinUniqueAbilities.MODE_FOCUSED)
                    .multiply(BuiltinUniqueAbilities.THUNDERCLAP_RADIUS,
                            parameter(node, "radius_percent", 50) / 100.0)
                    .set(BuiltinUniqueAbilities.THUNDERCLAP_DAMAGE_MULTIPLIER,
                            parameter(node, "damage_percent", 175) / 100.0));
            case THUNDERHEAD -> stormbreak(definition, () -> tuning
                    .set(BuiltinUniqueAbilities.STORMBREAK_MODE, BuiltinUniqueAbilities.MODE_THUNDERHEAD)
                    .add(BuiltinUniqueAbilities.THUNDERCLAP_RADIUS,
                            parameter(node, "radius_tenths", 20) / 10.0));
            case STATIC_RESERVE -> {
                if (definition == BuiltinUniqueAbilities.STORMS_EDGE_REFRESH) {
                    boolean sprinting = sprintRefreshEligible(context.actor() instanceof ServerPlayerEntity,
                            context.actor().isSprinting(), StormMasteryRuntime.value(
                                    context.actor(), context.stack(), StormMasteryRuntime.SPRINT_HIT, tick).amount());
                    StormMasteryRuntime.clear(context.actor(), context.stack(), StormMasteryRuntime.SPRINT_HIT);
                    if (sprinting) {
                        tuning.set(BuiltinUniqueAbilities.REFRESH_CHANCE, parameter(node, "sprint_chance", 40));
                    }
                }
            }
            case BUILDING_VOLTAGE -> {
                if (definition == BuiltinUniqueAbilities.STORMS_EDGE_REFRESH) {
                    int stacks = StormMasteryRuntime.value(context.actor(), context.stack(), StormMasteryRuntime.VOLTAGE, tick).amount();
                    tuning.add(BuiltinUniqueAbilities.REFRESH_CHANCE,
                            stacks * parameter(node, "chance_per_stack", 5));
                }
            }
            case FEEDBACK_LOOP -> {
                if (definition == BuiltinUniqueAbilities.STORMBREAK
                        && StormMasteryRuntime.value(context.actor(), context.stack(), StormMasteryRuntime.FEEDBACK, tick).amount() > 0) {
                    double factor = parameter(node, "damage_percent", 120) / 100.0;
                    tuning.multiply(BuiltinUniqueAbilities.CORRIDOR_DAMAGE_SCALING, factor)
                            .multiply(BuiltinUniqueAbilities.CORRIDOR_SPELL_SCALING, factor)
                            .multiply(BuiltinUniqueAbilities.THUNDERCLAP_DAMAGE_SCALING, factor)
                            .multiply(BuiltinUniqueAbilities.THUNDERCLAP_SPELL_SCALING, factor);
                }
            }
            case PERPETUAL_MOTION -> {
                if (definition == BuiltinUniqueAbilities.STORMBREAK
                        && StormMasteryRuntime.value(context.actor(), context.stack(), StormMasteryRuntime.OVERDRIVE, tick).amount() > 0) {
                    tuning.set(BuiltinUniqueAbilities.STORMBREAK_COOLDOWN_TICKS,
                            (int) Math.round(tuning.get(BuiltinUniqueAbilities.STORMBREAK_COOLDOWN_TICKS)
                                    * parameter(node, "cooldown_percent", 70) / 100.0));
                }
            }
            case UPDRAFT -> stormbreak(definition, () -> tuning.multiply(BuiltinUniqueAbilities.THUNDERCLAP_KNOCK_UP,
                    parameter(node, "knock_up_percent", 150) / 100.0));
            case REVERBERATION -> stormbreak(definition, () -> tuning
                    .set(BuiltinUniqueAbilities.AFTERSHOCK_DELAY_TICKS, parameter(node, "delay_ticks", 15))
                    .set(BuiltinUniqueAbilities.AFTERSHOCK_DAMAGE_MULTIPLIER,
                            parameter(node, "damage_percent", 30) / 100.0));
            case JUDGMENT_BOLT -> stormbreak(definition, () -> tuning.set(
                    BuiltinUniqueAbilities.JUDGMENT_DAMAGE_MULTIPLIER,
                    parameter(node, "damage_percent", 100) / 100.0));
            case SUPERCELL -> stormbreak(definition, () -> tuning
                    .set(BuiltinUniqueAbilities.SUPERCELL_DURATION_TICKS, parameter(node, "duration_ticks", 80))
                    .set(BuiltinUniqueAbilities.SUPERCELL_INTERVAL_TICKS, parameter(node, "interval_ticks", 20))
                    .set(BuiltinUniqueAbilities.SUPERCELL_DAMAGE_MULTIPLIER,
                            parameter(node, "damage_percent", 20) / 100.0)
                    .set(BuiltinUniqueAbilities.SUPERCELL_PULL, parameter(node, "pull_tenths", 8) / 10.0));
            default -> {
            }
        }
    }

    @Override
    public void onAbilityEvent(UniqueAbilityEvent event, MasteryProfile.Node node) {
        if (event.phase() == UniqueAbilityPhase.START) {
            if (kind == Kind.FEEDBACK_LOOP && event.execution().definition() == BuiltinUniqueAbilities.STORMBREAK) {
                StormMasteryRuntime.clear(event.execution().context().actor(), event.execution().context().stack(), StormMasteryRuntime.FEEDBACK);
            } else if (kind == Kind.PERPETUAL_MOTION
                    && event.execution().definition() == BuiltinUniqueAbilities.STORMBREAK) {
                StormMasteryRuntime.clear(event.execution().context().actor(), event.execution().context().stack(), StormMasteryRuntime.OVERDRIVE);
            }
            return;
        }
        if (event.phase() != UniqueAbilityPhase.HIT) return;
        UniqueAbilityContext context = event.execution().context();
        long tick = context.world().getTime();
        LivingEntity target = event.target();
        switch (kind) {
            case STORMBREAK_CONDUIT -> {
                if (event.eventId().equals(BuiltinUniqueAbilities.CORRIDOR_HIT) && target != null) {
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,
                            parameter(node, "slowness_ticks", 30), parameter(node, "amplifier", 0),
                            false, true, true), context.actor());
                }
            }
            case FLASHGUARD -> {
                if (event.eventId().equals(BuiltinUniqueAbilities.DASH_END)) {
                    context.actor().addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,
                            parameter(node, "duration_ticks", 40), parameter(node, "amplifier", 0),
                            false, false, true), context.actor());
                }
            }
            case CHARGED_PURSUIT -> {
                if (event.eventId().equals(BuiltinUniqueAbilities.REFRESH_PROC)) {
                    int duration = parameter(node, "duration_ticks", 40);
                    int amplifier = parameter(node, "amplifier", 2);
                    long current = StormMasteryRuntime.value(
                            context.actor(), context.stack(), StormMasteryRuntime.PURSUIT, tick).expiresAt();
                    long deadline = StormMasteryRuntime.refreshedDeadline(current, tick, duration);
                    StormMasteryRuntime.set(context.actor(), context.stack(), StormMasteryRuntime.PURSUIT,
                            amplifier + 1, deadline, tick);
                    context.actor().addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,
                            (int) Math.max(1, deadline - tick), amplifier, false, false, true), context.actor());
                }
            }
            case BUILDING_VOLTAGE -> {
                if (event.eventId().equals(BuiltinUniqueAbilities.MELEE_HIT)) {
                    StormMasteryRuntime.advance(context.actor(), context.stack(), StormMasteryRuntime.VOLTAGE, tick,
                            parameter(node, "max_stacks", 4), parameter(node, "window_ticks", 60));
                } else if (event.eventId().equals(BuiltinUniqueAbilities.REFRESH_PROC)) {
                    StormMasteryRuntime.clear(context.actor(), context.stack(), StormMasteryRuntime.VOLTAGE);
                }
            }
            case LIVE_WIRE -> liveWire(event, node, tick);
            case FEEDBACK_LOOP -> {
                if (event.eventId().equals(BuiltinUniqueAbilities.REFRESH_PROC)) {
                    StormMasteryRuntime.set(context.actor(), context.stack(), StormMasteryRuntime.FEEDBACK, 1,
                            tick + parameter(node, "window_ticks", 100), tick);
                }
            }
            case QUICKENING_CURRENT -> quickening(event, node, tick);
            case PERPETUAL_MOTION -> {
                if (event.eventId().equals(BuiltinUniqueAbilities.REFRESH_PROC)) {
                    StormMasteryRuntime.set(context.actor(), context.stack(), StormMasteryRuntime.OVERDRIVE, 1,
                            tick + parameter(node, "window_ticks", 120), tick);
                }
            }
            case FLASHOVER -> flashover(event, node);
            case IONIZE -> {
                if (target != null && isStormbreakDamage(event.eventId())) {
                    StormMasteryRuntime.ionize(context.world(), context.actor().getUuid(), target.getUuid(),
                            tick + parameter(node, "duration_ticks", 100));
                }
            }
            case ARC_LASH -> arcLash(event, node, tick);
            case PRESSURE_DROP -> {
                if (target != null && event.eventId().equals(BuiltinUniqueAbilities.THUNDERCLAP_HIT)
                        && StormMasteryRuntime.ionized(context.world(), context.actor().getUuid(), target.getUuid())) {
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS,
                            parameter(node, "duration_ticks", 60), parameter(node, "amplifier", 0),
                            false, true, true), context.actor());
                }
            }
            case UPDRAFT -> {
                if (target != null && event.eventId().equals(BuiltinUniqueAbilities.THUNDERCLAP_HIT)) {
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING,
                            parameter(node, "slow_falling_ticks", 40), 0, false, true, true), context.actor());
                }
            }
            case FULMINATION -> fulminate(event, node);
            case STORMSHIELD -> stormshield(event, node);
            default -> {
            }
        }
    }

    @Override
    public void onDamageReceived(SkillEffectContext.DamageReceived context, MasteryProfile.Node node) {
        if (kind != Kind.UNBROKEN_PACE || !context.player().isSprinting()
                || !context.access().startCooldown(node.id(), context.tick(), parameter(node, "cooldown_ticks", 120))) {
            return;
        }
        int duration = parameter(node, "duration_ticks", 30);
        context.player().addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, duration,
                parameter(node, "speed_amplifier", 1), false, false, true));
        context.player().addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, duration,
                parameter(node, "resistance_amplifier", 0), false, false, true));
    }

    @Override
    public void onAttack(SkillEffectContext.Attack context, MasteryProfile.Node node) {
        if (kind != Kind.STATIC_RESERVE || !context.player().isSprinting()) return;
        long tick = context.player().getServerWorld().getTime();
        StormMasteryRuntime.set(context.actor(), context.stack(), StormMasteryRuntime.SPRINT_HIT, 1, tick + 2L, tick);
    }

    private static void liveWire(UniqueAbilityEvent event, MasteryProfile.Node node, long tick) {
        if (!event.eventId().equals(BuiltinUniqueAbilities.MELEE_HIT) || event.target() == null) return;
        UniqueAbilityContext context = event.execution().context();
        if (StormMasteryRuntime.value(context.actor(), context.stack(), StormMasteryRuntime.PURSUIT, tick).amount() == 0
                || StormMasteryRuntime.value(context.actor(), context.stack(), StormMasteryRuntime.LIVE_WIRE, tick).amount() > 0) return;
        StormMasteryRuntime.set(context.actor(), context.stack(), StormMasteryRuntime.LIVE_WIRE, 1,
                tick + parameter(node, "cooldown_ticks", 10), tick);
        damage(context, event.target(), scaled(event, true, parameter(node, "damage_percent", 20)));
    }

    private static void quickening(UniqueAbilityEvent event, MasteryProfile.Node node, long tick) {
        LivingEntity target = event.target();
        UniqueAbilityContext context = event.execution().context();
        if (!quickeningDamageEvent(event.eventId()) || target == null || target.isAlive()
                || StormMasteryRuntime.value(context.actor(), context.stack(), StormMasteryRuntime.PURSUIT, tick).amount() == 0) return;
        long deadline = StormMasteryRuntime.extend(context.actor(), context.stack(), StormMasteryRuntime.PURSUIT, tick,
                parameter(node, "extension_ticks", 40), parameter(node, "max_remaining_ticks", 120));
        int amplifier = Math.max(0,
                StormMasteryRuntime.value(context.actor(), context.stack(), StormMasteryRuntime.PURSUIT, tick).amount() - 1);
        context.actor().addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,
                (int) Math.max(1, deadline - tick), amplifier, false, false, true), context.actor());
    }

    static boolean quickeningDamageEvent(Identifier id) {
        return id.getNamespace().equals("simplyswords") && quickeningDamagePath(id.getPath());
    }

    static boolean quickeningDamagePath(String path) {
        return path.equals("storms_edge_melee/hit")
                || path.equals("stormbreak/corridor_hit")
                || path.equals("stormbreak/thunderclap_hit")
                || path.equals("stormbreak/afterimage_hit")
                || path.equals("stormbreak/aftershock_hit")
                || path.equals("stormbreak/judgment_hit")
                || path.equals("stormbreak/supercell_hit");
    }

    static boolean sprintRefreshEligible(boolean player, boolean sprinting, int snapshot) {
        return player ? snapshot > 0 : sprinting;
    }

    private static void flashover(UniqueAbilityEvent event, MasteryProfile.Node node) {
        if (!event.eventId().equals(BuiltinUniqueAbilities.REFRESH_PROC) || event.target() == null) return;
        UniqueAbilityContext context = event.execution().context();
        List<LivingEntity> chain = nearbyChainTargets(context, event.target(),
                parameter(node, "targets", 3), parameter(node, "range_tenths", 50) / 10.0);
        float damage = scaled(event, false, parameter(node, "damage_percent", 35));
        Vec3d previous = event.target().getPos().add(0.0, event.target().getHeight() * 0.5, 0.0);
        int hits = 0;
        for (LivingEntity target : chain) {
            if (target == event.target()) continue;
            if (damage(context, target, damage)) {
                Vec3d end = target.getPos().add(0.0, target.getHeight() * 0.5, 0.0);
                SimplySwordsAPI.spawnAbilityLightningBolt(context.world(), previous, end, bolt());
                previous = end;
                if (++hits >= parameter(node, "targets", 3)) return;
            }
        }
    }

    private static void arcLash(UniqueAbilityEvent event, MasteryProfile.Node node, long tick) {
        if (!event.eventId().equals(BuiltinUniqueAbilities.MELEE_HIT) || event.target() == null) return;
        UniqueAbilityContext context = event.execution().context();
        if (!StormMasteryRuntime.ionized(context.world(), context.actor().getUuid(), event.target().getUuid())
                || StormMasteryRuntime.value(context.actor(), context.stack(), StormMasteryRuntime.ARC_LASH, tick).amount() > 0) return;
        List<LivingEntity> chain = nearbyChainTargets(context, event.target(),
                1, parameter(node, "range_tenths", 50) / 10.0);
        LivingEntity target = chain.stream().filter(candidate -> candidate != event.target()).findFirst().orElse(null);
        if (target == null) return;
        StormMasteryRuntime.set(context.actor(), context.stack(), StormMasteryRuntime.ARC_LASH, 1,
                tick + parameter(node, "cooldown_ticks", 20), tick);
        if (damage(context, target, scaled(event, true, parameter(node, "damage_percent", 30)))) {
            SimplySwordsAPI.spawnAbilityLightningBolt(context.world(),
                    event.target().getPos().add(0.0, event.target().getHeight() * 0.5, 0.0),
                    target.getPos().add(0.0, target.getHeight() * 0.5, 0.0), bolt());
        }
    }

    private static void fulminate(UniqueAbilityEvent event, MasteryProfile.Node node) {
        LivingEntity source = event.target();
        UniqueAbilityContext context = event.execution().context();
        if (source == null || source.isAlive()
                || !StormMasteryRuntime.consumeIonized(context.world(), context.actor().getUuid(), source.getUuid())) return;
        double radius = parameter(node, "radius_tenths", 30) / 10.0;
        Box box = source.getBoundingBox().expand(radius);
        float damage = scaled(event, false, parameter(node, "damage_percent", 25));
        int hits = 0;
        for (LivingEntity target : context.world().getEntitiesByClass(LivingEntity.class, box,
                candidate -> candidate != context.actor() && candidate.isAlive()
                        && EntityPredicates.VALID_LIVING_ENTITY.test(candidate))) {
            if (damage(context, target, damage)) {
                SimplySwordsAPI.spawnAbilityLightningBolt(context.world(), source.getPos().add(0.0, 0.5, 0.0),
                        target.getPos().add(0.0, target.getHeight() * 0.5, 0.0), bolt());
                if (++hits >= parameter(node, "max_targets", 8)) return;
            }
        }
    }

    private static List<LivingEntity> nearbyChainTargets(UniqueAbilityContext context, LivingEntity origin,
                                                          int targets, double range) {
        if (origin.isAlive()) {
            return SimplySwordsAPI.findAbilityChainTargets(
                    context.world(), context.actor(), origin, targets + 1, range);
        }
        return SimplySwordsAPI.findAbilityChainTargetsFromPosition(
                context.world(), context.actor(), origin.getPos(), targets, range);
    }

    private static void stormshield(UniqueAbilityEvent event, MasteryProfile.Node node) {
        if (!event.eventId().equals(BuiltinUniqueAbilities.THUNDERCLAP_FINISH)
                || event.affectedTargets() <= 0) return;
        int amplifier = event.affectedTargets() >= parameter(node, "strong_threshold", 4) ? 1 : 0;
        UniqueAbilityContext context = event.execution().context();
        context.actor().addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION,
                parameter(node, "duration_ticks", 80), amplifier, false, false, true), context.actor());
    }

    private static float scaled(UniqueAbilityEvent event, boolean corridor, int percent) {
        UniqueAbilityTuning tuning = event.execution().tuning();
        double attack = tuning.get(corridor ? BuiltinUniqueAbilities.CORRIDOR_DAMAGE_SCALING
                : BuiltinUniqueAbilities.THUNDERCLAP_DAMAGE_SCALING);
        double spell = tuning.get(corridor ? BuiltinUniqueAbilities.CORRIDOR_SPELL_SCALING
                : BuiltinUniqueAbilities.THUNDERCLAP_SPELL_SCALING);
        double factor = percent / 100.0;
        UniqueAbilityContext context = event.execution().context();
        return SimplySwordsAPI.scaleAbilityDamage(SpellScalingProfile.LIGHTNING, context.actor(), context.stack(),
                (float) (attack * factor), (float) (spell * factor));
    }

    private static boolean damage(UniqueAbilityContext context, LivingEntity target, float damage) {
        if (context.actor() instanceof ServerPlayerEntity player) {
            return SkillOriginGuard.run(new SkillOriginGuard.Origin(player, context.stack(), "storms_edge_secondary"),
                    () -> SimplySwordsAPI.applyAbilityMagicDamageThroughIframes(context.world(), context.actor(),
                            context.stack(), target, damage, SpellScalingProfile.LIGHTNING));
        }
        return SimplySwordsAPI.applyAbilityMagicDamageThroughIframes(context.world(), context.actor(),
                context.stack(), target, damage, SpellScalingProfile.LIGHTNING);
    }

    private static boolean isStormbreakDamage(Identifier id) {
        return id.equals(BuiltinUniqueAbilities.CORRIDOR_HIT)
                || id.equals(BuiltinUniqueAbilities.THUNDERCLAP_HIT)
                || id.equals(BuiltinUniqueAbilities.AFTERIMAGE_HIT)
                || id.equals(BuiltinUniqueAbilities.AFTERSHOCK_HIT)
                || id.equals(BuiltinUniqueAbilities.JUDGMENT_HIT)
                || id.equals(BuiltinUniqueAbilities.SUPERCELL_HIT);
    }

    private static LightningBoltStyle bolt() {
        return new LightningBoltStyle(0xA6F3FF, 6, 0.05F, 3, true);
    }

    private static void stormbreak(UniqueAbilityDefinition definition, Runnable action) {
        if (definition == BuiltinUniqueAbilities.STORMBREAK) action.run();
    }

    private static int parameter(MasteryProfile.Node node, String key, int fallback) {
        return node.effect().parameters().getOrDefault(key, fallback);
    }

    private static Map<String, Range> ranges(Kind kind) {
        return switch (kind) {
            case STORMBREAK_CONDUIT -> map("width_tenths", 0, 320, "slowness_ticks", 1, 1200, "amplifier", 0, 10);
            case SLIPSTREAM -> map("distance_percent", 1, 1000, "speed_percent", 1, 1000);
            case CROSSWIND -> Map.of();
            case CAPACITOR -> map("per_hit_percent", 0, 1000, "cap_percent", 0, 1000);
            case STORM_CHASER -> map("cooldown_percent", 0, 1000);
            case FLASHGUARD -> map("duration_ticks", 1, 1200, "amplifier", 0, 10);
            case AFTERIMAGE -> map("duration_ticks", 1, 1200, "damage_percent", 0, 1000);
            case EYE_OF_STORM -> map("radius_percent", 1, 1000, "damage_percent", 0, 1000);
            case THUNDERHEAD -> map("radius_tenths", 0, 320);
            case STATIC_RESERVE -> map("sprint_chance", 0, 100);
            case CHARGED_PURSUIT -> map("duration_ticks", 1, 1200, "amplifier", 0, 10);
            case BUILDING_VOLTAGE -> map("chance_per_stack", 0, 100, "max_stacks", 1, 20, "window_ticks", 1, 1200);
            case LIVE_WIRE -> map("damage_percent", 0, 1000, "cooldown_ticks", 1, 72000);
            case FEEDBACK_LOOP -> map("window_ticks", 1, 1200, "damage_percent", 0, 1000);
            case QUICKENING_CURRENT -> map("extension_ticks", 1, 1200, "max_remaining_ticks", 1, 1200);
            case UNBROKEN_PACE -> map("duration_ticks", 1, 1200, "speed_amplifier", 0, 10,
                    "resistance_amplifier", 0, 10, "cooldown_ticks", 1, 72000);
            case PERPETUAL_MOTION -> map("window_ticks", 1, 1200, "cooldown_percent", 0, 1000);
            case FLASHOVER -> map("targets", 1, 16, "range_tenths", 5, 320, "damage_percent", 0, 1000);
            case IONIZE -> map("duration_ticks", 1, 1200);
            case ARC_LASH -> map("damage_percent", 0, 1000, "cooldown_ticks", 1, 72000, "range_tenths", 5, 320);
            case PRESSURE_DROP -> map("duration_ticks", 1, 1200, "amplifier", 0, 10);
            case UPDRAFT -> map("knock_up_percent", 0, 1000, "slow_falling_ticks", 1, 1200);
            case FULMINATION -> map("damage_percent", 0, 1000, "radius_tenths", 5, 320, "max_targets", 1, 64);
            case STORMSHIELD -> map("duration_ticks", 1, 1200, "strong_threshold", 1, 64);
            case REVERBERATION -> map("delay_ticks", 1, 1200, "damage_percent", 0, 1000);
            case JUDGMENT_BOLT -> map("damage_percent", 0, 1000);
            case SUPERCELL -> map("duration_ticks", 1, 1200, "interval_ticks", 1, 1200,
                    "damage_percent", 0, 1000, "pull_tenths", 0, 160);
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
        STORMBREAK_CONDUIT("stormbreak_conduit"), SLIPSTREAM("slipstream"), CROSSWIND("crosswind"),
        CAPACITOR("capacitor"), STORM_CHASER("storm_chaser"), FLASHGUARD("flashguard"),
        AFTERIMAGE("afterimage"), EYE_OF_STORM("eye_of_storm"), THUNDERHEAD("thunderhead"),
        STATIC_RESERVE("static_reserve"), CHARGED_PURSUIT("charged_pursuit"),
        BUILDING_VOLTAGE("building_voltage"), LIVE_WIRE("live_wire"), FEEDBACK_LOOP("feedback_loop"),
        QUICKENING_CURRENT("quickening_current"), UNBROKEN_PACE("unbroken_pace"),
        PERPETUAL_MOTION("perpetual_motion"), FLASHOVER("flashover"), IONIZE("ionize"),
        ARC_LASH("arc_lash"), PRESSURE_DROP("pressure_drop"), UPDRAFT("updraft"),
        FULMINATION("fulmination"), STORMSHIELD("stormshield"), REVERBERATION("reverberation"),
        JUDGMENT_BOLT("judgment_bolt"), SUPERCELL("supercell");

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
