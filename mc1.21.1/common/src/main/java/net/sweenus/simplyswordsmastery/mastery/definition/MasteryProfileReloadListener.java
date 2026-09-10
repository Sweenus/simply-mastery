package net.sweenus.simplyswordsmastery.mastery.definition;

import net.minecraft.registry.tag.TagManagerLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.server.ServerAdvancementLoader;
import net.sweenus.simplyswordsmastery.mastery.network.MasteryNetwork;
import net.sweenus.simplyswordsmastery.mastery.network.ProfileSyncPacket;
import net.sweenus.simplyswordsmastery.mastery.reward.ProgressionPolicy;
import net.sweenus.simplyswordsmastery.mastery.reward.ResolvedRewardDefinitions;
import net.sweenus.simplyswordsmastery.mastery.reward.RewardCatalogFactory;
import net.sweenus.simplyswordsmastery.mastery.reward.RewardDefinitionLoader;
import org.slf4j.LoggerFactory;

public final class MasteryProfileReloadListener implements SynchronousResourceReloader {
    private final TagManagerLoader tags;
    private final ServerAdvancementLoader advancements;
    private MasteryProfileRegistry.Snapshot candidate;

    public MasteryProfileReloadListener(TagManagerLoader tags, ServerAdvancementLoader advancements) {
        this.tags = tags;
        this.advancements = advancements;
    }

    @Override
    public void reload(ResourceManager manager) {
        candidate = null;
        var profiles = MasteryProfileRegistry.prepare(manager);
        var definitions = RewardDefinitionLoader.prepare(manager);
        definitions.errors().forEach(MasteryProfileReloadListener::log);
        if (profiles.candidate().isEmpty() || definitions.candidate().isEmpty()) return;
        try {
            var snapshot = profiles.candidate().orElseThrow();
            var fallback = ProgressionPolicy.defaults();
            var resolved = ResolvedRewardDefinitions.prepare(definitions.candidate().orElseThrow(),
                    RewardCatalogFactory.create(snapshot, tags, advancements), fallback);
            resolved.errors().forEach(MasteryProfileReloadListener::log);
            if (resolved.candidate().isEmpty()) return;
            var rewards = resolved.candidate().orElseThrow();
            candidate = new MasteryProfileRegistry.Snapshot(snapshot.epoch(), snapshot.profiles(),
                    rewards.policies(), fallback, rewards);
            ProfileSyncPacket.encode(candidate);
        } catch (RuntimeException exception) {
            candidate = null;
            log("Catalog validation failed: " + exception.getMessage());
        }
    }

    public void publish() {
        if (candidate == null) return;
        MasteryProfileRegistry.installServer(candidate);
        candidate = null;
        MasteryNetwork.syncAllPlayers();
    }

    private static void log(String error) {
        LoggerFactory.getLogger("Simply Swords: Mastery/Rewards")
                .error("Rejected mastery reload; retaining installed definitions: {}", error);
    }
}
