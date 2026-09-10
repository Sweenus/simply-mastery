package net.sweenus.simplymastery.mastery.reward;

import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.ToIntFunction;

public final class RewardDefinitionLoader {
    private static final int MAX_RESOURCE_BYTES = 65536;
    private static final int MAX_DEFINITIONS = 4096;

    private RewardDefinitionLoader() {
    }

    public static Prepared prepare(ResourceManager resources) {
        List<String> errors = new ArrayList<>();
        var progression = read(resources, "progression_rules", RewardDefinitions.ProgressionRule.CODEC, errors);
        var entities = read(resources, "entity_rewards", RewardDefinitions.EntityReward.CODEC, errors);
        var consumables = read(resources, "mastery_consumables", RewardDefinitions.Consumable.CODEC, errors);
        var advancements = read(resources, "advancement_rewards", RewardDefinitions.AdvancementReward.CODEC, errors);
        if (!errors.isEmpty()) return new Prepared(Optional.empty(), errors);
        return new Prepared(Optional.of(new Definitions(progression, entities, consumables, advancements)), List.of());
    }

    private static <T> Map<Identifier, T> read(ResourceManager resources, String registry, Codec<T> codec,
                                              List<String> errors) {
        String prefix = "simplymastery/" + registry + "/";
        var files = resources.findResources("simplymastery/" + registry, id -> id.getPath().endsWith(".json"));
        if (files.size() > MAX_DEFINITIONS) {
            errors.add(registry + ": exceeds " + MAX_DEFINITIONS + " definitions");
            return Map.of();
        }
        Map<Identifier, T> result = new LinkedHashMap<>();
        files.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(file -> {
            try (InputStream input = file.getValue().getInputStream()) {
                byte[] data = input.readNBytes(MAX_RESOURCE_BYTES + 1);
                if (data.length > MAX_RESOURCE_BYTES) throw new IllegalArgumentException("resource exceeds " + MAX_RESOURCE_BYTES + " bytes");
                Identifier id = Identifier.of(file.getKey().getNamespace(), file.getKey().getPath()
                        .substring(prefix.length(), file.getKey().getPath().length() - 5));
                if (id.toString().length() > 256) throw new IllegalArgumentException("definition ID exceeds 256 characters");
                JsonElement document = JsonParser.parseString(new String(data, StandardCharsets.UTF_8));
                validateFields(registry, document);
                T value = codec.parse(JsonOps.INSTANCE, document).getOrThrow();
                if (result.putIfAbsent(id, value) != null) throw new IllegalArgumentException("duplicate definition " + id);
            } catch (Exception exception) {
                errors.add(file.getKey() + ": " + exception.getMessage());
            }
        });
        return Map.copyOf(result);
    }

    private static void validateFields(String registry, JsonElement document) {
        Set<String> fields = switch (registry) {
            case "progression_rules" -> Set.of("version", "priority", "selector", "xp_percent", "point_cap", "curve");
            case "entity_rewards" -> Set.of("version", "priority", "selector", "base_xp", "xp_percent", "boss", "first_kill");
            case "mastery_consumables" -> Set.of("version", "priority", "ingredient", "quantity", "weapon", "reward");
            case "advancement_rewards" -> Set.of("version", "priority", "advancement", "weapon", "reward");
            default -> throw new IllegalArgumentException("Unknown registry " + registry);
        };
        JsonObject object = requireFields(document, fields, registry);
        for (String selector : List.of("selector", "ingredient", "weapon")) {
            if (object.has(selector)) requireFields(object.get(selector), Set.of("id", "tag", "group"), selector);
        }
        for (String reward : List.of("reward", "first_kill")) {
            if (object.has(reward)) requireFields(object.get(reward), Set.of("xp", "points"), reward);
        }
        if (object.has("curve")) {
            requireFields(object.get("curve"), Set.of("base", "growth", "costs", "unchanged_points", "final_multiplier"), "curve");
        }
    }

    private static JsonObject requireFields(JsonElement element, Set<String> allowed, String path) {
        if (!element.isJsonObject()) throw new IllegalArgumentException(path + " must be an object");
        JsonObject object = element.getAsJsonObject();
        for (String key : object.keySet()) {
            if (!allowed.contains(key)) throw new IllegalArgumentException(path + ": unknown field '" + key + "'");
            if (object.get(key).isJsonPrimitive() && object.get(key).getAsJsonPrimitive().isString()
                    && object.get(key).getAsString().length() > 256) throw new IllegalArgumentException(path + "." + key + " exceeds 256 characters");
            if (object.get(key).isJsonNull()) throw new IllegalArgumentException(path + "." + key + " cannot be null");
        }
        return object;
    }

    public static <T> Comparator<Map.Entry<Identifier, T>> precedence(ToIntFunction<T> priority,
                                                                    ToIntFunction<T> specificity) {
        return Comparator.<Map.Entry<Identifier, T>>comparingInt(entry -> priority.applyAsInt(entry.getValue()))
                .reversed().thenComparing(Comparator.<Map.Entry<Identifier, T>>comparingInt(
                        entry -> specificity.applyAsInt(entry.getValue())).reversed())
                .thenComparing(entry -> entry.getKey().toString());
    }

    public record Definitions(Map<Identifier, RewardDefinitions.ProgressionRule> progression,
                              Map<Identifier, RewardDefinitions.EntityReward> entities,
                              Map<Identifier, RewardDefinitions.Consumable> consumables,
                              Map<Identifier, RewardDefinitions.AdvancementReward> advancements) {
        public Definitions {
            progression = Map.copyOf(progression);
            entities = Map.copyOf(entities);
            consumables = Map.copyOf(consumables);
            advancements = Map.copyOf(advancements);
        }
    }

    public record Prepared(Optional<Definitions> candidate, List<String> errors) {
        public Prepared { errors = List.copyOf(errors); }
    }
}
