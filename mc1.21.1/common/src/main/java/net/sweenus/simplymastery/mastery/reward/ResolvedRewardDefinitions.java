package net.sweenus.simplymastery.mastery.reward;

import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public final class ResolvedRewardDefinitions {
    private final Catalog catalog;
    private final ProgressionPolicy fallback;
    private final Map<Identifier, ProgressionPolicy> policies;
    private final List<Named<RewardDefinitions.ProgressionRule>> progression;
    private final List<Named<RewardDefinitions.EntityReward>> entities;
    private final List<Named<RewardDefinitions.Consumable>> consumables;
    private final List<Named<RewardDefinitions.AdvancementReward>> advancements;

    private ResolvedRewardDefinitions(RewardDefinitionLoader.Definitions definitions, Catalog catalog,
                                      ProgressionPolicy fallback, Map<Identifier, ProgressionPolicy> policies) {
        this.catalog = catalog;
        this.fallback = fallback;
        this.policies = Map.copyOf(policies);
        progression = ordered(definitions.progression(), RewardDefinitions.ProgressionRule::priority,
                rule -> rule.selector().specificity());
        entities = ordered(definitions.entities(), RewardDefinitions.EntityReward::priority,
                rule -> rule.selector().specificity());
        consumables = ordered(definitions.consumables(), RewardDefinitions.Consumable::priority,
                rule -> rule.ingredient().specificity());
        advancements = ordered(definitions.advancements(), RewardDefinitions.AdvancementReward::priority,
                rule -> rule.weapon().specificity());
    }

    public static Prepared prepare(RewardDefinitionLoader.Definitions definitions, Catalog catalog,
                                    ProgressionPolicy fallback) {
        List<String> errors = new ArrayList<>();
        definitions.progression().forEach((id, rule) -> validate("progression_rules/" + id, errors, () -> {
            validateSelector(rule.selector(), catalog.items(), catalog.itemTags(), catalog.groups());
            rule.policy(fallback);
        }));
        definitions.entities().forEach((id, rule) -> validate("entity_rewards/" + id, errors,
                () -> validateSelector(rule.selector(), catalog.entities(), catalog.entityTags(), Set.of())));
        definitions.consumables().forEach((id, rule) -> validate("mastery_consumables/" + id, errors, () -> {
            validateSelector(rule.ingredient(), catalog.items(), catalog.itemTags(), Set.of());
            validateSelector(rule.weapon(), catalog.items(), catalog.itemTags(), catalog.groups());
        }));
        definitions.advancements().forEach((id, rule) -> validate("advancement_rewards/" + id, errors, () -> {
            if (!catalog.advancements().contains(rule.advancement())) {
                throw new IllegalArgumentException("Unknown advancement " + rule.advancement());
            }
            validateSelector(rule.weapon(), catalog.items(), catalog.itemTags(), catalog.groups());
        }));
        if (!errors.isEmpty()) return new Prepared(Optional.empty(), errors);
        ResolvedRewardDefinitions candidate = new ResolvedRewardDefinitions(definitions, catalog, fallback, Map.of());
        Map<Identifier, ProgressionPolicy> policies = new HashMap<>();
        Map<Identifier, String> origins = new HashMap<>();
        catalog.weapons().stream().sorted(Comparator.comparing(weapon -> weapon.form().toString()))
                .forEach(weapon -> {
                    Optional<Named<RewardDefinitions.ProgressionRule>> winner = candidate.progression(weapon.item(), weapon.group());
                    ProgressionPolicy policy = winner.map(rule -> rule.value().policy(fallback)).orElse(fallback);
                    String origin = weapon.form() + " (" + winner.map(rule -> "progression_rules/" + rule.id())
                            .orElse("server defaults") + ")";
                    ProgressionPolicy previous = policies.putIfAbsent(weapon.group(), policy);
                    if (previous != null && !equivalent(previous, policy)) {
                        errors.add("Conflicting cap/curve for group " + weapon.group() + ": "
                                + origins.get(weapon.group()) + " and " + origin);
                    } else {
                        origins.putIfAbsent(weapon.group(), origin);
                    }
                });
        for (Identifier group : catalog.groups()) {
            if (policies.containsKey(group)) continue;
            Optional<Named<RewardDefinitions.ProgressionRule>> winner = first(candidate.progression,
                    rule -> rule.selector().group().filter(group::equals).isPresent());
            policies.put(group, winner.map(rule -> rule.value().policy(fallback)).orElse(fallback));
        }
        return errors.isEmpty()
                ? new Prepared(Optional.of(new ResolvedRewardDefinitions(definitions, catalog, fallback, policies)), List.of())
                : new Prepared(Optional.empty(), errors);
    }

    public Set<Identifier> advancementIds() {
        return advancements.stream().map(value -> value.value().advancement()).collect(Collectors.toUnmodifiableSet());
    }

    public List<Weapon> weapons() { return catalog.weapons(); }

    public ProgressionPolicy policy(Identifier group) {
        return policies.getOrDefault(group, fallback);
    }

    public Map<Identifier, ProgressionPolicy> policies() {
        return policies;
    }

    public int weaponXpPercent(Identifier item, Identifier group) {
        return progression(item, group).map(rule -> rule.value().xpPercent()).orElse(100);
    }

    public Optional<Named<RewardDefinitions.ProgressionRule>> progression(Identifier item, Identifier group) {
        return first(progression, rule -> matches(rule.selector(), item, group, catalog.itemTags()));
    }

    public Optional<Named<RewardDefinitions.EntityReward>> entity(Identifier entity) {
        return first(entities, rule -> matches(rule.selector(), entity, null, catalog.entityTags()));
    }

    public Optional<Named<RewardDefinitions.Consumable>> consumable(Identifier paymentItem, Identifier weapon,
                                                                  Identifier group) {
        return first(consumables, rule -> matches(rule.ingredient(), paymentItem, null, catalog.itemTags())
                && matches(rule.weapon(), weapon, group, catalog.itemTags()));
    }

    public Optional<Named<RewardDefinitions.AdvancementReward>> advancement(Identifier advancement,
                                                                           Identifier weapon, Identifier group) {
        return first(advancements, rule -> rule.advancement().equals(advancement)
                && matches(rule.weapon(), weapon, group, catalog.itemTags()));
    }

    public Set<Weapon> eligibleWeapons(RewardDefinitions.Selector selector) {
        return catalog.weapons().stream()
                .filter(weapon -> matches(selector, weapon.item(), weapon.group(), catalog.itemTags()))
                .collect(Collectors.toUnmodifiableSet());
    }

    private static boolean equivalent(ProgressionPolicy left, ProgressionPolicy right) {
        if (left.pointCap() != right.pointCap()) return false;
        for (int point = 0; point < left.pointCap(); point++) {
            if (left.nextCost(point) != right.nextCost(point)) return false;
        }
        return true;
    }

    private static boolean matches(RewardDefinitions.Selector selector, Identifier id, Identifier group,
                                   Map<Identifier, Set<Identifier>> tags) {
        return selector.id().filter(value -> value.equals(id)).isPresent()
                || selector.group().filter(value -> value.equals(group)).isPresent()
                || selector.tag().map(tag -> tags.getOrDefault(tag, Set.of()).contains(id)).orElse(false);
    }

    private static void validateSelector(RewardDefinitions.Selector selector, Set<Identifier> ids,
                                         Map<Identifier, Set<Identifier>> tags, Set<Identifier> groups) {
        selector.id().ifPresent(id -> {
            if (!ids.contains(id)) throw new IllegalArgumentException("Unknown ID " + id);
        });
        selector.tag().ifPresent(tag -> {
            if (!tags.containsKey(tag)) throw new IllegalArgumentException("Unknown tag #" + tag);
        });
        selector.group().ifPresent(group -> {
            if (!groups.contains(group)) throw new IllegalArgumentException("Unknown progression group " + group);
        });
    }

    private static void validate(String source, List<String> errors, Runnable validation) {
        try {
            validation.run();
        } catch (IllegalArgumentException exception) {
            errors.add(source + ": " + exception.getMessage());
        }
    }

    private static <T> List<Named<T>> ordered(Map<Identifier, T> entries, ToIntFunction<T> priority,
                                             ToIntFunction<T> specificity) {
        return entries.entrySet().stream().sorted(RewardDefinitionLoader.precedence(priority, specificity))
                .map(entry -> new Named<>(entry.getKey(), entry.getValue())).toList();
    }

    private static <T> Optional<Named<T>> first(List<Named<T>> entries, Predicate<T> predicate) {
        return entries.stream().filter(entry -> predicate.test(entry.value())).findFirst();
    }

    public record Named<T>(Identifier id, T value) {
    }

    public record Weapon(Identifier form, Identifier item, Identifier group) {
        public Weapon {
            Objects.requireNonNull(form);
            Objects.requireNonNull(item);
            Objects.requireNonNull(group);
        }
    }

    public record Catalog(Set<Identifier> items, Set<Identifier> entities, Set<Identifier> advancements,
                          Map<Identifier, Set<Identifier>> itemTags, Map<Identifier, Set<Identifier>> entityTags,
                          Set<Identifier> groups, List<Weapon> weapons) {
        public Catalog {
            items = Set.copyOf(items);
            entities = Set.copyOf(entities);
            advancements = Set.copyOf(advancements);
            itemTags = copyTags(itemTags, items);
            entityTags = copyTags(entityTags, entities);
            groups = Set.copyOf(groups);
            weapons = List.copyOf(weapons);
            Set<Identifier> forms = new HashSet<>();
            for (Weapon weapon : weapons) {
                if (!items.contains(weapon.item()) || !groups.contains(weapon.group())) {
                    throw new IllegalArgumentException("Unknown item/group for form " + weapon.form());
                }
                if (!forms.add(weapon.form())) throw new IllegalArgumentException("Duplicate form " + weapon.form());
            }
        }

        private static Map<Identifier, Set<Identifier>> copyTags(Map<Identifier, Set<Identifier>> tags,
                                                                Set<Identifier> ids) {
            Map<Identifier, Set<Identifier>> copy = new LinkedHashMap<>();
            tags.forEach((tag, members) -> {
                if (!ids.containsAll(members)) throw new IllegalArgumentException("Unknown members in tag #" + tag);
                copy.put(tag, Set.copyOf(members));
            });
            return Map.copyOf(copy);
        }
    }

    public record Prepared(Optional<ResolvedRewardDefinitions> candidate, List<String> errors) {
        public Prepared { errors = List.copyOf(errors); }
    }
}
