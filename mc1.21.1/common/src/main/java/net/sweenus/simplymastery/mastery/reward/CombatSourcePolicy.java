package net.sweenus.simplymastery.mastery.reward;

import net.sweenus.simplymastery.config.MasteryServerConfig;

import java.util.EnumSet;
import java.util.Set;

public final class CombatSourcePolicy {
    private CombatSourcePolicy() {
    }

    public static int percentage(MasteryServerConfig config, Classification classification) {
        if (!config.enabled) return 0;
        Set<Source> sources = classification.sources();
        for (Source source : sources) {
            boolean enabled = switch (source) {
                case MELEE -> config.meleeXpEnabled;
                case ABILITY -> config.abilityXpEnabled;
                case PROJECTILE -> config.projectileXpEnabled;
                case DAMAGE_OVER_TIME -> config.damageOverTimeXpEnabled;
                case SUMMON -> config.summonXpEnabled;
            };
            if (!enabled) return 0;
        }
        int percentage;
        if (sources.contains(Source.SUMMON)) percentage = config.summonXpPercent;
        else if (sources.contains(Source.DAMAGE_OVER_TIME)) percentage = config.damageOverTimeXpPercent;
        else if (sources.contains(Source.PROJECTILE)) percentage = config.projectileXpPercent;
        else if (sources.contains(Source.ABILITY)) percentage = config.abilityXpPercent;
        else if (sources.contains(Source.MELEE)) percentage = config.meleeXpPercent;
        else return 0;
        return Math.clamp(percentage, 0, 10000);
    }

    public enum Source {
        MELEE, ABILITY, PROJECTILE, DAMAGE_OVER_TIME, SUMMON
    }

    public record Classification(Set<Source> origins, Set<Source> deliveries) {
        public Classification {
            origins = Set.copyOf(origins);
            deliveries = Set.copyOf(deliveries);
        }

        public Set<Source> sources() {
            var result = EnumSet.noneOf(Source.class);
            result.addAll(origins);
            result.addAll(deliveries);
            return Set.copyOf(result);
        }

        public Classification deliveredBy(Source source) {
            var result = EnumSet.noneOf(Source.class);
            result.addAll(deliveries);
            result.add(source);
            return new Classification(origins, result);
        }
    }
}
