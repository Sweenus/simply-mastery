package net.sweenus.simplyswordsmastery.mastery.reward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

import java.util.Optional;

public final class RewardDefinitions {
    private RewardDefinitions() {
    }

    public record Selector(Optional<Identifier> id, Optional<Identifier> tag, Optional<Identifier> group) {
        public static final Codec<Selector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.optionalFieldOf("id").forGetter(Selector::id),
                Identifier.CODEC.optionalFieldOf("tag").forGetter(Selector::tag),
                Identifier.CODEC.optionalFieldOf("group").forGetter(Selector::group)
        ).apply(instance, Selector::new));

        public Selector {
            if ((id.isPresent() ? 1 : 0) + (tag.isPresent() ? 1 : 0) + (group.isPresent() ? 1 : 0) != 1) {
                throw new IllegalArgumentException("Selector requires exactly one of id, tag, group");
            }
        }

        public int specificity() { return id.isPresent() ? 2 : tag.isPresent() ? 1 : 0; }

        public void requireItemOrEntity() {
            if (group.isPresent()) throw new IllegalArgumentException("Ingredient/entity selector cannot use group");
        }
    }

    public record Reward(Optional<Integer> xp, Optional<Integer> points) {
        public static final Codec<Reward> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("xp").forGetter(Reward::xp),
                Codec.intRange(1, 64).optionalFieldOf("points").forGetter(Reward::points)
        ).apply(instance, Reward::new));

        public Reward {
            if (xp.isPresent() == points.isPresent() || xp.orElse(1) <= 0
                    || points.orElse(1) < 1 || points.orElse(1) > 64) {
                throw new IllegalArgumentException("Reward requires positive xp OR 1–64 points");
            }
        }
    }

    public record ProgressionRule(int version, int priority, Selector selector, int xpPercent,
                                  Optional<Integer> pointCap, Optional<ProgressionPolicy.Curve> curve) {
        public static final Codec<ProgressionRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(1, 1).fieldOf("version").forGetter(ProgressionRule::version),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(ProgressionRule::priority),
                Selector.CODEC.fieldOf("selector").forGetter(ProgressionRule::selector),
                Codec.intRange(0, 10000).optionalFieldOf("xp_percent", 100).forGetter(ProgressionRule::xpPercent),
                Codec.intRange(1, 64).optionalFieldOf("point_cap").forGetter(ProgressionRule::pointCap),
                ProgressionPolicy.Curve.CODEC.optionalFieldOf("curve").forGetter(ProgressionRule::curve)
        ).apply(instance, ProgressionRule::new));

        public ProgressionRule {
            requireVersion(version);
            if (pointCap.isPresent() && curve.isPresent()) new ProgressionPolicy(pointCap.get(), curve.get());
        }

        public ProgressionPolicy policy(ProgressionPolicy fallback) {
            return new ProgressionPolicy(pointCap.orElse(fallback.pointCap()), curve.orElse(fallback.curve()));
        }
    }

    public record EntityReward(int version, int priority, Selector selector, Optional<Integer> baseXp,
                               Optional<Integer> xpPercent, Optional<Boolean> boss, Optional<Reward> firstKill) {
        public static final Codec<EntityReward> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(1, 1).fieldOf("version").forGetter(EntityReward::version),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(EntityReward::priority),
                Selector.CODEC.fieldOf("selector").forGetter(EntityReward::selector),
                Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("base_xp").forGetter(EntityReward::baseXp),
                Codec.intRange(0, 10000).optionalFieldOf("xp_percent").forGetter(EntityReward::xpPercent),
                Codec.BOOL.optionalFieldOf("boss").forGetter(EntityReward::boss),
                Reward.CODEC.optionalFieldOf("first_kill").forGetter(EntityReward::firstKill)
        ).apply(instance, EntityReward::new));

        public EntityReward {
            requireVersion(version);
            selector.requireItemOrEntity();
            if (baseXp.isPresent() && xpPercent.isPresent()) {
                throw new IllegalArgumentException("Entity reward cannot combine base_xp and xp_percent");
            }
        }
    }

    public record Consumable(int version, int priority, Selector ingredient, int quantity,
                             Selector weapon, Reward reward) {
        public static final Codec<Consumable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(1, 1).fieldOf("version").forGetter(Consumable::version),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(Consumable::priority),
                Selector.CODEC.fieldOf("ingredient").forGetter(Consumable::ingredient),
                Codec.intRange(1, 2304).fieldOf("quantity").forGetter(Consumable::quantity),
                Selector.CODEC.fieldOf("weapon").forGetter(Consumable::weapon),
                Reward.CODEC.fieldOf("reward").forGetter(Consumable::reward)
        ).apply(instance, Consumable::new));

        public Consumable {
            requireVersion(version);
            ingredient.requireItemOrEntity();
        }
    }

    public record AdvancementReward(int version, int priority, Identifier advancement, Selector weapon, Reward reward) {
        public static final Codec<AdvancementReward> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(1, 1).fieldOf("version").forGetter(AdvancementReward::version),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(AdvancementReward::priority),
                Identifier.CODEC.fieldOf("advancement").forGetter(AdvancementReward::advancement),
                Selector.CODEC.fieldOf("weapon").forGetter(AdvancementReward::weapon),
                Reward.CODEC.fieldOf("reward").forGetter(AdvancementReward::reward)
        ).apply(instance, AdvancementReward::new));

        public AdvancementReward { requireVersion(version); }
    }

    private static void requireVersion(int version) {
        if (version != 1) throw new IllegalArgumentException("Unsupported reward definition version " + version);
    }
}
