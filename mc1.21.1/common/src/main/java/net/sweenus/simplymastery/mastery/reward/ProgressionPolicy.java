package net.sweenus.simplymastery.mastery.reward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.sweenus.simplymastery.config.MasteryConfig;

import java.util.List;
import java.util.Optional;

public record ProgressionPolicy(int pointCap, Curve curve) {
    public static final Codec<ProgressionPolicy> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 64).fieldOf("point_cap").forGetter(ProgressionPolicy::pointCap),
            Curve.CODEC.fieldOf("curve").forGetter(ProgressionPolicy::curve)
    ).apply(instance, ProgressionPolicy::new));

    public ProgressionPolicy {
        if (pointCap < 1 || pointCap > 64) throw new IllegalArgumentException("point_cap must be between 1 and 64");
        if (curve == null) throw new IllegalArgumentException("Missing XP curve");
        if (!curve.costs().isEmpty() && curve.costs().size() < pointCap) {
            throw new IllegalArgumentException("Explicit XP costs must cover point_cap " + pointCap);
        }
        for (int point = 0; point < pointCap; point++) {
            if (curve.cost(point) > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("XP cost at point " + point + " exceeds " + Integer.MAX_VALUE);
            }
        }
    }

    public static ProgressionPolicy defaults() {
        return new ProgressionPolicy(Math.clamp(MasteryConfig.SERVER.maximumEarnedPoints, 1, 64),
                new Curve(Optional.of(Math.max(1, MasteryConfig.SERVER.xpBaseRequirement)),
                        Optional.of(Math.max(0, MasteryConfig.SERVER.xpRequirementGrowth)), List.of()));
    }

    public long nextCost(int earnedPoints) {
        if (earnedPoints < 0 || earnedPoints >= pointCap) return 0;
        return curve.cost(earnedPoints);
    }

    public double remainingCapacity(int earnedPoints, double xp) {
        if (!Double.isFinite(xp) || xp < 0 || earnedPoints < 0) {
            throw new IllegalArgumentException("Invalid progression balance");
        }
        long capacity = 0;
        for (int point = earnedPoints; point < pointCap; point++) capacity += nextCost(point);
        return Math.max(0, capacity - xp);
    }

    public record Curve(Optional<Integer> base, Optional<Integer> growth, List<Integer> costs) {
        public static final Codec<Curve> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(1, 1000000000).optionalFieldOf("base").forGetter(Curve::base),
                Codec.intRange(0, 1000000000).optionalFieldOf("growth").forGetter(Curve::growth),
                Codec.intRange(1, 1000000000).listOf(0, 64).optionalFieldOf("costs", List.of()).forGetter(Curve::costs)
        ).apply(instance, Curve::new));

        public Curve {
            costs = List.copyOf(costs);
            if (costs.isEmpty()) {
                if (base.isEmpty() || growth.isEmpty() || base.get() <= 0 || growth.get() < 0) {
                    throw new IllegalArgumentException("Linear curve requires positive base and nonnegative growth");
                }
            } else if (base.isPresent() || growth.isPresent() || costs.size() > 64
                    || costs.stream().anyMatch(cost -> cost <= 0)) {
                throw new IllegalArgumentException("Explicit curve requires only 1–64 positive costs");
            }
        }

        public long cost(int point) {
            if (point < 0 || point >= 64) throw new IllegalArgumentException("Invalid point index " + point);
            return costs.isEmpty() ? base.orElseThrow().longValue() + (long) point * growth.orElseThrow()
                    : costs.get(point);
        }
    }
}
