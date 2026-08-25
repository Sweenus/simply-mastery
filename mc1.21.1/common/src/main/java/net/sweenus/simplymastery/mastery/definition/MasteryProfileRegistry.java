package net.sweenus.simplymastery.mastery.definition;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.config.MasteryConfig;
import net.sweenus.simplyswords.api.AwakeningFormFamily;
import net.sweenus.simplyswords.api.AwakeningFormRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class MasteryProfileRegistry {

    public static final String RESOURCE_PATH = "simplymastery/weapon_skills";
    private static final Logger LOGGER = LoggerFactory.getLogger("Simply Mastery/Profiles");
    private static volatile Snapshot server = Snapshot.empty();
    private static volatile Snapshot client = Snapshot.empty();
    private static Map<Identifier, Identifier> sourceProfiles = Map.of();

    private MasteryProfileRegistry() {
    }

    public static synchronized ReloadResult reload(ResourceManager resources) {
        Map<Identifier, JsonElement> documents = new LinkedHashMap<>();
        List<String> readErrors = new ArrayList<>();
        Set<Identifier> failedSources = new java.util.LinkedHashSet<>();
        resources.findResources(RESOURCE_PATH, id -> id.getPath().endsWith(".json"))
                .entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                    try (InputStream input = entry.getValue().getInputStream()) {
                        byte[] data = input.readNBytes(MasteryProfile.MAX_PROFILE_JSON_BYTES + 1);
                        if (data.length > MasteryProfile.MAX_PROFILE_JSON_BYTES) {
                            throw new IllegalArgumentException("resource exceeds "
                                    + MasteryProfile.MAX_PROFILE_JSON_BYTES + " bytes");
                        }
                        documents.put(entry.getKey(), JsonParser.parseString(new String(data, StandardCharsets.UTF_8)));
                    } catch (Exception exception) {
                        readErrors.add(entry.getKey() + ": " + exception.getMessage());
                        failedSources.add(entry.getKey());
                    }
                });
        return reload(documents, readErrors, failedSources, MasteryConfig.SERVER.maximumEarnedPoints);
    }

    public static synchronized ReloadResult reload(Map<Identifier, JsonElement> documents) {
        return reload(documents, List.of(), Set.of(), 21);
    }

    private static ReloadResult reload(Map<Identifier, JsonElement> documents, List<String> initialErrors,
                                       Set<Identifier> failedSources, int pointBudget) {
        List<String> errors = new ArrayList<>(initialErrors);
        Map<Identifier, MasteryProfile> accepted = new LinkedHashMap<>();
        Map<Identifier, Identifier> nextSources = new LinkedHashMap<>();

        documents.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            Identifier source = entry.getKey();
            MasteryProfile parsed;
            try {
                parsed = MasteryProfile.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                        .resultOrPartial(message -> errors.add(source + ": codec: " + message)).orElse(null);
            } catch (RuntimeException exception) {
                errors.add(source + ": codec: " + exception.getMessage());
                parsed = null;
            }
            if (parsed == null) {
                retainSource(source, accepted, nextSources);
                return;
            }
            try {
                MasteryProfileValidator.validate(parsed, pointBudget);
            } catch (ProfileValidationException exception) {
                exception.errors().forEach(error -> errors.add(source + ": " + error));
                retainSource(source, accepted, nextSources);
                return;
            }
            MasteryProfile duplicate = accepted.putIfAbsent(parsed.id(), parsed);
            if (duplicate != null) {
                errors.add(source + ": duplicate profile id " + parsed.id());
                retainSource(source, accepted, nextSources);
                return;
            }
            nextSources.put(source, parsed.id());
        });

        for (Identifier source : failedSources) retainSource(source, accepted, nextSources);

        BuiltInFamilyProfiles.addMissing(accepted);

        List<MasteryProfile> candidate = sorted(accepted.values());
        try {
            MasteryProfileValidator.validateRegistry(candidate, pointBudget);
        } catch (ProfileValidationException exception) {
            errors.addAll(exception.errors());
            logErrors(errors, "Rejected mastery profile reload; retaining epoch " + server.epoch());
            return new ReloadResult(false, server, errors);
        }

        Snapshot previous = server;
        Snapshot next = new Snapshot(previous.epoch() + 1, candidate);
        server = next;
        sourceProfiles = Map.copyOf(nextSources);
        logErrors(errors, "Loaded " + candidate.size() + " mastery profiles at epoch " + next.epoch());
        return new ReloadResult(true, next, errors);
    }

    private static void retainSource(Identifier source, Map<Identifier, MasteryProfile> accepted,
                                     Map<Identifier, Identifier> nextSources) {
        Identifier previousId = sourceProfiles.get(source);
        if (previousId == null) return;
        MasteryProfile previous = server.profiles().get(previousId);
        if (previous != null) {
            accepted.putIfAbsent(previousId, previous);
            nextSources.put(source, previousId);
        }
    }

    private static void logErrors(List<String> errors, String success) {
        for (String error : errors) LOGGER.error("Mastery definition error: {}", error);
        if (errors.isEmpty()) LOGGER.info(success);
        else LOGGER.warn("{} with {} retained/disabled invalid resource(s)", success, errors.size());
    }

    public static Snapshot server() {
        return server;
    }

    public static Snapshot client() {
        return client;
    }

    public static synchronized boolean installClient(int epoch, List<MasteryProfile> profiles) {
        if (epoch < client.epoch()) return false;
        try {
            MasteryProfileValidator.validateRegistry(profiles, 21);
        } catch (ProfileValidationException exception) {
            exception.errors().forEach(error -> LOGGER.error("Rejected canonical profile sync: {}", error));
            return false;
        }
        client = new Snapshot(epoch, profiles);
        return true;
    }

    public static void clearClient() {
        client = Snapshot.empty();
    }

    public static Optional<MasteryProfile> resolveServer(ItemStack stack) {
        return server.resolve(stack).map(Resolution::profile);
    }

    public static Optional<MasteryProfile> resolveClient(ItemStack stack) {
        return client.resolve(stack).map(Resolution::profile);
    }

    public static Optional<ProgressionResolution> resolveProgressionServer(ItemStack stack) {
        return server.resolveProgression(stack);
    }

    private static List<MasteryProfile> sorted(Collection<MasteryProfile> profiles) {
        return profiles.stream().sorted(Comparator.comparing(profile -> profile.id().toString())).toList();
    }

    public record Snapshot(int epoch, Map<Identifier, MasteryProfile> profiles) {
        public Snapshot(int epoch, Collection<MasteryProfile> profiles) {
            this(epoch, index(profiles));
        }

        public Snapshot {
            profiles = Map.copyOf(profiles);
        }

        public static Snapshot empty() {
            return new Snapshot(0, Map.of());
        }

        public List<MasteryProfile> orderedProfiles() {
            return sorted(profiles.values());
        }

        public Optional<Resolution> resolve(ItemStack stack) {
            if (stack == null || stack.isEmpty()) return Optional.empty();
            Identifier itemId = Registries.ITEM.getId(stack.getItem());
            Identifier stageId = AwakeningFormRegistry.getStage(stack)
                    .filter(stage -> stack.isOf(stage.item())).map(stage -> stage.id()).orElse(null);
            Identifier familyId = AwakeningFormRegistry.get(stack).map(family -> family.baseStage().id()).orElse(null);
            return resolveIds(itemId, stageId, familyId);
        }

        public Optional<Resolution> resolveIds(Identifier itemId, Identifier familyId) {
            return resolveIds(itemId, null, familyId);
        }

        public Optional<Resolution> resolveIds(Identifier itemId, Identifier stageId, Identifier familyId) {
            Optional<Resolution> stage = stageId == null ? Optional.empty()
                    : select(stageId, SelectorKind.EXACT_FORM_STAGE);
            if (stage.isPresent()) return stage;
            Optional<Resolution> item = select(itemId, SelectorKind.EXACT_ITEM);
            return item.isPresent() || familyId == null ? item : select(familyId, SelectorKind.FORM_FAMILY);
        }

        public List<Resolution> matches(ItemStack stack) {
            if (stack == null || stack.isEmpty()) return List.of();
            Identifier itemId = Registries.ITEM.getId(stack.getItem());
            List<Resolution> stage = AwakeningFormRegistry.getStage(stack).filter(value -> stack.isOf(value.item()))
                    .map(value -> matches(value.id(), SelectorKind.EXACT_FORM_STAGE)).orElse(List.of());
            if (!stage.isEmpty()) return winners(stage);
            List<Resolution> item = matches(itemId, SelectorKind.EXACT_ITEM);
            if (!item.isEmpty()) return winners(item);
            return AwakeningFormRegistry.get(stack)
                    .map(family -> winners(matches(family.baseStage().id(), SelectorKind.FORM_FAMILY)))
                    .orElse(List.of());
        }

        public Optional<ProgressionResolution> resolveProgression(ItemStack stack) {
            Optional<Resolution> visible = resolve(stack);
            if (visible.isPresent()) {
                MasteryProfile profile = visible.get().profile();
                return Optional.of(new ProgressionResolution(profile.progressionGroupId(),
                        Optional.of(profile), List.of(profile.id())));
            }
            Optional<AwakeningFormFamily> family = AwakeningFormRegistry.get(stack);
            if (family.isPresent()) {
                Set<Identifier> stages = new java.util.LinkedHashSet<>();
                stages.add(family.get().baseStage().id());
                family.get().routes().values().forEach(route -> route.stages()
                        .forEach(stage -> stages.add(stage.id())));
                Map<Identifier, List<Identifier>> groups = new LinkedHashMap<>();
                for (MasteryProfile profile : profiles.values()) {
                    boolean member = profile.selectors().stream()
                            .flatMap(selector -> selector.formStage().stream()).anyMatch(stages::contains);
                    if (member) groups.computeIfAbsent(profile.progressionGroupId(), ignored -> new ArrayList<>())
                            .add(profile.id());
                }
                if (groups.size() == 1) {
                    Map.Entry<Identifier, List<Identifier>> entry = groups.entrySet().iterator().next();
                    return Optional.of(new ProgressionResolution(entry.getKey(), Optional.empty(), entry.getValue()));
                }
            }
            Identifier itemId = stack == null || stack.isEmpty() ? null : Registries.ITEM.getId(stack.getItem());
            return BuiltInFamilyProfiles.bankingGroup(itemId).map(group -> new ProgressionResolution(group,
                    Optional.empty(), BuiltInFamilyProfiles.bankingProfiles(group)));
        }

        private Optional<Resolution> select(Identifier selectorId, SelectorKind kind) {
            List<Resolution> winners = winners(matches(selectorId, kind));
            return winners.size() == 1 ? Optional.of(winners.getFirst()) : Optional.empty();
        }

        private List<Resolution> matches(Identifier selectorId, SelectorKind kind) {
            List<Resolution> result = new ArrayList<>();
            for (MasteryProfile profile : profiles.values()) {
                for (MasteryProfile.Selector selector : profile.selectors()) {
                    Optional<Identifier> target = switch (kind) {
                        case EXACT_FORM_STAGE -> selector.formStage();
                        case EXACT_ITEM -> selector.item();
                        case FORM_FAMILY -> selector.formFamily();
                    };
                    if (target.filter(selectorId::equals).isPresent()) {
                        result.add(new Resolution(profile, selector.priority(), kind));
                    }
                }
            }
            return result;
        }

        private static List<Resolution> winners(List<Resolution> matches) {
            int best = matches.stream().mapToInt(Resolution::priority).max().orElse(Integer.MIN_VALUE);
            return matches.stream().filter(match -> match.priority() == best)
                    .sorted(Comparator.comparing(match -> match.profile().id().toString())).toList();
        }

        private static Map<Identifier, MasteryProfile> index(Collection<MasteryProfile> profiles) {
            Map<Identifier, MasteryProfile> result = new LinkedHashMap<>();
            for (MasteryProfile profile : sorted(profiles)) result.put(profile.id(), profile);
            return result;
        }
    }

    public record Resolution(MasteryProfile profile, int priority, SelectorKind kind) {
    }

    public enum SelectorKind {
        EXACT_FORM_STAGE,
        EXACT_ITEM,
        FORM_FAMILY
    }

    public record ProgressionResolution(Identifier groupId, Optional<MasteryProfile> visibleProfile,
                                        List<Identifier> profileIds) {
        public ProgressionResolution {
            profileIds = List.copyOf(profileIds);
        }
    }

    public record ReloadResult(boolean installed, Snapshot snapshot, List<String> errors) {
        public ReloadResult {
            errors = List.copyOf(errors);
        }
    }
}
