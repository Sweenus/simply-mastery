package net.sweenus.simplyswordsmastery.mastery.reward;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagManagerLoader;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.util.Identifier;
import net.sweenus.simplyswordsmastery.mastery.definition.MasteryProfileRegistry;
import net.sweenus.simplyswords.api.AwakeningFormFamily;
import net.sweenus.simplyswords.api.AwakeningFormRegistry;
import net.sweenus.simplyswords.api.AwakeningFormStage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RewardCatalogFactory {
    private RewardCatalogFactory() {
    }

    public static ResolvedRewardDefinitions.Catalog create(MasteryProfileRegistry.Snapshot profiles,
                                                           TagManagerLoader tags,
                                                           ServerAdvancementLoader advancements) {
        Set<Identifier> groups = new HashSet<>();
        profiles.profiles().values().forEach(profile -> groups.add(profile.progressionGroupId()));
        List<ResolvedRewardDefinitions.Weapon> weapons = new ArrayList<>();
        Set<AwakeningFormFamily> families = new HashSet<>();
        for (var item : Registries.ITEM) {
            ItemStack stack = item.getDefaultStack();
            profiles.resolveProgression(stack).ifPresent(resolution -> {
                Identifier itemId = Registries.ITEM.getId(item);
                groups.add(resolution.groupId());
                weapons.add(new ResolvedRewardDefinitions.Weapon(
                        Identifier.of(itemId.getNamespace(), "item/" + itemId.getPath()), itemId, resolution.groupId()));
            });
            AwakeningFormRegistry.get(stack).ifPresent(families::add);
        }
        for (AwakeningFormFamily family : families) {
            Map<Identifier, AwakeningFormStage> stages = new LinkedHashMap<>();
            stages.put(family.baseStage().id(), family.baseStage());
            family.routes().values().forEach(route -> route.stages().forEach(stage -> stages.put(stage.id(), stage)));
            for (AwakeningFormStage stage : stages.values()) {
                Identifier itemId = Registries.ITEM.getId(stage.item());
                var resolution = profiles.resolveIds(itemId, stage.id(), family.baseStage().id());
                Identifier group = resolution.map(value -> value.profile().progressionGroupId())
                        .orElseGet(() -> profiles.resolveProgression(stage.item().getDefaultStack())
                                .map(MasteryProfileRegistry.ProgressionResolution::groupId).orElse(null));
                if (group == null) continue;
                groups.add(group);
                weapons.add(new ResolvedRewardDefinitions.Weapon(
                        Identifier.of(stage.id().getNamespace(), "stage/" + stage.id().getPath()), itemId, group));
            }
        }
        Map<Identifier, Set<Identifier>> itemTags = Map.of();
        Map<Identifier, Set<Identifier>> entityTags = Map.of();
        for (var registry : tags.getRegistryTags()) {
            if (registry.key().equals(RegistryKeys.ITEM)) itemTags = copyTags(registry);
            if (registry.key().equals(RegistryKeys.ENTITY_TYPE)) entityTags = copyTags(registry);
        }
        Set<Identifier> advancementIds = new HashSet<>();
        advancements.getAdvancements().forEach(entry -> advancementIds.add(entry.id()));
        return new ResolvedRewardDefinitions.Catalog(Registries.ITEM.getIds(), Registries.ENTITY_TYPE.getIds(),
                advancementIds, itemTags, entityTags, groups, weapons);
    }

    private static Map<Identifier, Set<Identifier>> copyTags(TagManagerLoader.RegistryTags<?> registry) {
        Map<Identifier, Set<Identifier>> result = new LinkedHashMap<>();
        registry.tags().forEach((id, entries) -> {
            Set<Identifier> members = new HashSet<>();
            entries.forEach(entry -> members.add(entry.getKey().orElseThrow().getValue()));
            result.put(id, Set.copyOf(members));
        });
        return Map.copyOf(result);
    }
}
