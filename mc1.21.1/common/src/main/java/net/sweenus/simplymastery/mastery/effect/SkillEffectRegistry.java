package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.SimplyMastery;
import net.sweenus.simplymastery.config.MasteryConfig;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SkillEffectRegistry {

    private static final Map<Identifier, SkillEffectType> TYPES = new LinkedHashMap<>();

    static {
        register(simple("none", Set.of(), Map.of()));
        register(new SkillEffectType() {
            private final Identifier id = SkillEffectRegistry.id("stormstep");

            @Override
            public Identifier id() {
                return id;
            }

            @Override
            public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
                validateParameters(effect, where, errors, Set.of("duration_ticks", "amplifier", "cooldown_ticks"),
                        Map.of("duration_ticks", new Range(1, 1200), "amplifier", new Range(0, 10),
                                "cooldown_ticks", new Range(1, 72000)));
            }

            @Override
            public void onAttack(SkillEffectContext.Attack context, MasteryProfile.Node node) {
                int cooldown = parameter(node, "cooldown_ticks", 1);
                if (!context.access().startCooldown(node.id(), context.tick(), cooldown)) return;
                int duration = parameter(node, "duration_ticks", MasteryConfig.SERVER.stormstepDurationTicks);
                int amplifier = parameter(node, "amplifier", MasteryConfig.SERVER.stormstepAmplifier);
                context.player().addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,
                        duration, amplifier, false, false, true));
                context.player().getServerWorld().spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                        context.player().getX(), context.player().getBodyY(0.45), context.player().getZ(),
                        14, 0.35, 0.25, 0.35, 0.08);
                context.player().getServerWorld().playSound(null, context.player().getBlockPos(),
                        SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.PLAYERS, 0.3F, 1.7F);
            }
        });
        register(new SkillEffectType() {
            private final Identifier id = SkillEffectRegistry.id("echo_strike");

            @Override
            public Identifier id() {
                return id;
            }

            @Override
            public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
                validateParameters(effect, where, errors, Set.of("damage_tenths", "cooldown_ticks"),
                        Map.of("damage_tenths", new Range(1, 200), "cooldown_ticks", new Range(1, 72000)));
            }

            @Override
            public void onAttack(SkillEffectContext.Attack context, MasteryProfile.Node node) {
                if (context.access().startCooldown(node.id(), context.tick(), parameter(node, "cooldown_ticks", 20))) {
                    context.access().dealAdditionalDamage(context.target(), node.id(),
                            parameter(node, "damage_tenths", 20) / 10.0F);
                }
            }
        });
        register(new SkillEffectType() {
            private final Identifier id = SkillEffectRegistry.id("combo_surge");

            @Override
            public Identifier id() {
                return id;
            }

            @Override
            public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
                validateParameters(effect, where, errors,
                        Set.of("hits", "window_ticks", "duration_ticks", "amplifier", "cooldown_ticks"),
                        Map.of("hits", new Range(2, 20), "window_ticks", new Range(1, 1200),
                                "duration_ticks", new Range(1, 1200), "amplifier", new Range(0, 10),
                                "cooldown_ticks", new Range(1, 72000)));
            }

            @Override
            public void onAttack(SkillEffectContext.Attack context, MasteryProfile.Node node) {
                SkillEffectAccess.HandKey hand = context.hand() == net.minecraft.util.Hand.MAIN_HAND
                        ? SkillEffectAccess.HandKey.MAIN_HAND : SkillEffectAccess.HandKey.OFF_HAND;
                if (!context.access().advanceCombo(hand, node.id(), context.tick(),
                        parameter(node, "window_ticks", 60), parameter(node, "hits", 3))) return;
                if (!context.access().startCooldown(node.id(), context.tick(), parameter(node, "cooldown_ticks", 100))) return;
                context.player().addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE,
                        parameter(node, "duration_ticks", 60), parameter(node, "amplifier", 0),
                        false, false, true));
            }
        });
        register(new SkillEffectType() {
            private final Identifier id = SkillEffectRegistry.id("combo_strike");

            @Override
            public Identifier id() {
                return id;
            }

            @Override
            public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
                validateParameters(effect, where, errors,
                        Set.of("hits", "window_ticks", "damage_tenths", "cooldown_ticks"),
                        Map.of("hits", new Range(2, 20), "window_ticks", new Range(1, 1200),
                                "damage_tenths", new Range(1, 200), "cooldown_ticks", new Range(1, 72000)));
            }

            @Override
            public void onAttack(SkillEffectContext.Attack context, MasteryProfile.Node node) {
                SkillEffectAccess.HandKey hand = hand(context.hand());
                if (!context.access().advanceCombo(hand, node.id(), context.tick(),
                        parameter(node, "window_ticks", 60), parameter(node, "hits", 3))) return;
                if (!context.access().startCooldown(node.id(), context.tick(),
                        parameter(node, "cooldown_ticks", 60))) return;
                context.access().dealAdditionalDamage(context.target(), node.id(),
                        parameter(node, "damage_tenths", 10) / 10.0F);
            }
        });
        register(new SkillEffectType() {
            private final Identifier id = SkillEffectRegistry.id("battle_flow");

            @Override
            public Identifier id() {
                return id;
            }

            @Override
            public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
                validateParameters(effect, where, errors,
                        Set.of("hits", "window_ticks", "duration_ticks", "amplifier", "cooldown_ticks", "mode"),
                        Map.of("hits", new Range(2, 20), "window_ticks", new Range(1, 1200),
                                "duration_ticks", new Range(1, 1200), "amplifier", new Range(0, 4),
                                "cooldown_ticks", new Range(1, 72000), "mode", new Range(0, 4)));
            }

            @Override
            public void onAttack(SkillEffectContext.Attack context, MasteryProfile.Node node) {
                if (!context.access().advanceCombo(hand(context.hand()), node.id(), context.tick(),
                        parameter(node, "window_ticks", 60), parameter(node, "hits", 3))) return;
                if (!context.access().startCooldown(node.id(), context.tick(),
                        parameter(node, "cooldown_ticks", 100))) return;
                context.player().addStatusEffect(new StatusEffectInstance(
                        beneficial(parameter(node, "mode", 0)), parameter(node, "duration_ticks", 50),
                        parameter(node, "amplifier", 0), false, false, true));
            }
        });
        register(new SkillEffectType() {
            private final Identifier id = SkillEffectRegistry.id("hindering_strike");

            @Override
            public Identifier id() {
                return id;
            }

            @Override
            public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
                validateParameters(effect, where, errors,
                        Set.of("duration_ticks", "amplifier", "cooldown_ticks", "mode"),
                        Map.of("duration_ticks", new Range(1, 1200), "amplifier", new Range(0, 4),
                                "cooldown_ticks", new Range(1, 72000), "mode", new Range(0, 3)));
            }

            @Override
            public void onAttack(SkillEffectContext.Attack context, MasteryProfile.Node node) {
                if (!context.access().startCooldown(node.id(), context.tick(),
                        parameter(node, "cooldown_ticks", 100))) return;
                context.target().addStatusEffect(new StatusEffectInstance(
                        hindrance(parameter(node, "mode", 0)), parameter(node, "duration_ticks", 40),
                        parameter(node, "amplifier", 0), false, true, true), context.player());
            }
        });
        register(new SkillEffectType() {
            private final Identifier id = SkillEffectRegistry.id("finishing_strike");

            @Override
            public Identifier id() {
                return id;
            }

            @Override
            public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
                validateParameters(effect, where, errors,
                        Set.of("health_percent", "damage_tenths", "cooldown_ticks"),
                        Map.of("health_percent", new Range(5, 80), "damage_tenths", new Range(1, 200),
                                "cooldown_ticks", new Range(1, 72000)));
            }

            @Override
            public void onAttack(SkillEffectContext.Attack context, MasteryProfile.Node node) {
                if (context.target().getHealth() * 100.0F > context.target().getMaxHealth()
                        * parameter(node, "health_percent", 30)) return;
                if (context.access().startCooldown(node.id(), context.tick(),
                        parameter(node, "cooldown_ticks", 80))) {
                    context.access().dealAdditionalDamage(context.target(), node.id(),
                            parameter(node, "damage_tenths", 15) / 10.0F);
                }
            }
        });
        register(new SkillEffectType() {
            private final Identifier id = SkillEffectRegistry.id("soul_mend");

            @Override
            public Identifier id() {
                return id;
            }

            @Override
            public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
                validateParameters(effect, where, errors, Set.of("heal_tenths", "cooldown_ticks"),
                        Map.of("heal_tenths", new Range(1, 200), "cooldown_ticks", new Range(1, 72000)));
            }

            @Override
            public void onKill(SkillEffectContext.Kill context, MasteryProfile.Node node) {
                if (context.access().startCooldown(node.id(), context.tick(),
                        parameter(node, "cooldown_ticks", 100))) {
                    context.player().heal(parameter(node, "heal_tenths", 10) / 10.0F);
                }
            }
        });
        register(new SkillEffectType() {
            private final Identifier id = SkillEffectRegistry.id("kill_flow");

            @Override
            public Identifier id() {
                return id;
            }

            @Override
            public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
                validateParameters(effect, where, errors,
                        Set.of("duration_ticks", "amplifier", "cooldown_ticks", "mode"),
                        Map.of("duration_ticks", new Range(1, 1200), "amplifier", new Range(0, 4),
                                "cooldown_ticks", new Range(1, 72000), "mode", new Range(0, 4)));
            }

            @Override
            public void onKill(SkillEffectContext.Kill context, MasteryProfile.Node node) {
                if (!context.access().startCooldown(node.id(), context.tick(),
                        parameter(node, "cooldown_ticks", 100))) return;
                context.player().addStatusEffect(new StatusEffectInstance(
                        beneficial(parameter(node, "mode", 0)), parameter(node, "duration_ticks", 60),
                        parameter(node, "amplifier", 0), false, false, true));
            }
        });
        register(new SkillEffectType() {
            private final Identifier id = SkillEffectRegistry.id("counterstrike");

            @Override
            public Identifier id() {
                return id;
            }

            @Override
            public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
                validateParameters(effect, where, errors, Set.of("damage_tenths", "cooldown_ticks"),
                        Map.of("damage_tenths", new Range(1, 200), "cooldown_ticks", new Range(1, 72000)));
            }

            @Override
            public void onDamageReceived(SkillEffectContext.DamageReceived context, MasteryProfile.Node node) {
                if (context.attacker() == null || !context.attacker().isAlive()) return;
                if (context.access().startCooldown(node.id(), context.tick(),
                        parameter(node, "cooldown_ticks", 120))) {
                    context.access().dealAdditionalDamage(context.attacker(), node.id(),
                            parameter(node, "damage_tenths", 10) / 10.0F);
                }
            }
        });
        register(new SkillEffectType() {
            private final Identifier id = SkillEffectRegistry.id("guarded_recovery");

            @Override
            public Identifier id() {
                return id;
            }

            @Override
            public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
                validateParameters(effect, where, errors,
                        Set.of("duration_ticks", "amplifier", "cooldown_ticks"),
                        Map.of("duration_ticks", new Range(1, 1200), "amplifier", new Range(0, 4),
                                "cooldown_ticks", new Range(1, 72000)));
            }

            @Override
            public void onDamageReceived(SkillEffectContext.DamageReceived context, MasteryProfile.Node node) {
                if (!context.access().startCooldown(node.id(), context.tick(),
                        parameter(node, "cooldown_ticks", 160))) return;
                context.player().addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,
                        parameter(node, "duration_ticks", 40), parameter(node, "amplifier", 0),
                        false, false, true));
            }
        });
        register(new SkillEffectType() {
            private final Identifier id = SkillEffectRegistry.id("leeching_strike");

            @Override
            public Identifier id() {
                return id;
            }

            @Override
            public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
                validateParameters(effect, where, errors, Set.of("heal_tenths", "cooldown_ticks"),
                        Map.of("heal_tenths", new Range(1, 200), "cooldown_ticks", new Range(1, 72000)));
            }

            @Override
            public void onAttack(SkillEffectContext.Attack context, MasteryProfile.Node node) {
                if (context.access().startCooldown(node.id(), context.tick(),
                        parameter(node, "cooldown_ticks", 120))) {
                    context.player().heal(parameter(node, "heal_tenths", 5) / 10.0F);
                }
            }
        });
        register(new SkillEffectType() {
            private final Identifier id = SkillEffectRegistry.id("cleaving_echo");

            @Override
            public Identifier id() {
                return id;
            }

            @Override
            public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
                validateParameters(effect, where, errors,
                        Set.of("hits", "window_ticks", "damage_tenths", "radius_tenths", "cooldown_ticks"),
                        Map.of("hits", new Range(2, 20), "window_ticks", new Range(1, 1200),
                                "damage_tenths", new Range(1, 200), "radius_tenths", new Range(10, 120),
                                "cooldown_ticks", new Range(1, 72000)));
            }

            @Override
            public void onAttack(SkillEffectContext.Attack context, MasteryProfile.Node node) {
                if (!context.access().advanceCombo(hand(context.hand()), node.id(), context.tick(),
                        parameter(node, "window_ticks", 60), parameter(node, "hits", 3))) return;
                if (!context.access().startCooldown(node.id(), context.tick(),
                        parameter(node, "cooldown_ticks", 100))) return;
                double radius = parameter(node, "radius_tenths", 35) / 10.0;
                float damage = parameter(node, "damage_tenths", 10) / 10.0F;
                context.player().getServerWorld().getOtherEntities(context.player(),
                                context.target().getBoundingBox().expand(radius),
                                entity -> entity instanceof HostileEntity && entity != context.target()
                                        && !entity.isTeammate(context.player()))
                        .stream().limit(8).map(entity -> (HostileEntity) entity)
                        .forEach(target -> context.access().dealAdditionalDamage(target, node.id(), damage));
            }
        });
        StormAbilitySkillEffects.all().forEach(SkillEffectRegistry::register);
        BrimstoneAbilitySkillEffects.all().forEach(SkillEffectRegistry::register);
        register(new Phase2MasterySkillEffect());
        register(new Phase3MasterySkillEffect());
        register(new Phase4MasterySkillEffect());
        register(new Phase5MasterySkillEffect());
        register(new Phase6MasterySkillEffect());
        register(new Phase7MasterySkillEffect());
        register(new Phase8MasterySkillEffect());
        register(new Phase9MasterySkillEffect());
    }

    private SkillEffectRegistry() {
    }

    public static SkillEffectType get(Identifier id) {
        return TYPES.get(id);
    }

    public static Set<Identifier> ids() {
        return Set.copyOf(TYPES.keySet());
    }

    public static void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
        SkillEffectType type = get(effect.type());
        if (type == null) errors.add(where + "unregistered effect " + effect.type());
        else type.validate(effect, where, errors);
    }

    private static void register(SkillEffectType type) {
        if (TYPES.putIfAbsent(type.id(), type) != null) throw new IllegalStateException("Duplicate effect " + type.id());
    }

    private static SkillEffectType simple(String path, Set<String> allowed, Map<String, Range> ranges) {
        return new SkillEffectType() {
            private final Identifier id = SkillEffectRegistry.id(path);

            @Override
            public Identifier id() {
                return id;
            }

            @Override
            public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
                validateParameters(effect, where, errors, allowed, ranges);
            }
        };
    }

    private static void validateParameters(MasteryProfile.Effect effect, String where, List<String> errors,
                                           Set<String> allowed, Map<String, Range> ranges) {
        for (Map.Entry<String, Integer> entry : effect.parameters().entrySet()) {
            if (!allowed.contains(entry.getKey())) errors.add(where + "unknown parameter '" + entry.getKey()
                    + "' for " + effect.type());
            Range range = ranges.get(entry.getKey());
            if (range != null && !range.contains(entry.getValue())) errors.add(where + entry.getKey()
                    + " must be between " + range.min + " and " + range.max);
        }
    }

    private static int parameter(MasteryProfile.Node node, String key, int fallback) {
        return node.effect().parameters().getOrDefault(key, fallback);
    }

    private static SkillEffectAccess.HandKey hand(net.minecraft.util.Hand hand) {
        return hand == net.minecraft.util.Hand.MAIN_HAND
                ? SkillEffectAccess.HandKey.MAIN_HAND : SkillEffectAccess.HandKey.OFF_HAND;
    }

    private static RegistryEntry<StatusEffect> beneficial(int mode) {
        return switch (mode) {
            case 1 -> StatusEffects.HASTE;
            case 2 -> StatusEffects.RESISTANCE;
            case 3 -> StatusEffects.REGENERATION;
            case 4 -> StatusEffects.STRENGTH;
            default -> StatusEffects.SPEED;
        };
    }

    private static RegistryEntry<StatusEffect> hindrance(int mode) {
        return switch (mode) {
            case 1 -> StatusEffects.WEAKNESS;
            case 2 -> StatusEffects.GLOWING;
            case 3 -> StatusEffects.POISON;
            default -> StatusEffects.SLOWNESS;
        };
    }

    private static Identifier id(String path) {
        return Identifier.of(SimplyMastery.MOD_ID, path);
    }

    private record Range(int min, int max) {
        boolean contains(int value) {
            return value >= min && value <= max;
        }
    }
}
