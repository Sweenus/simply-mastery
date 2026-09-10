package net.sweenus.simplyswordsmastery;

import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.sweenus.simplyswordsmastery.client.SimplySwordsMasteryClient;
import net.sweenus.simplyswordsmastery.config.MasteryConfig;
import net.sweenus.simplyswordsmastery.mastery.network.MasteryNetwork;
import net.sweenus.simplyswordsmastery.mastery.progression.MasteryProvenance;
import net.sweenus.simplyswordsmastery.mastery.progression.MasteryRewardService;
import net.sweenus.simplyswordsmastery.mastery.progression.ProgressionOwnership;
import net.sweenus.simplyswordsmastery.mastery.state.MasteryComponents;
import net.sweenus.simplyswordsmastery.mastery.effect.SkillRuntime;
import net.sweenus.simplyswordsmastery.mastery.effect.UniqueAbilityMasteryBridge;
import net.sweenus.simplyswordsmastery.mastery.effect.StormMasteryRuntime;
import net.sweenus.simplyswordsmastery.mastery.progression.MasteryCommands;
import net.sweenus.simplyswordsmastery.mastery.progression.MasteryProgression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SimplySwordsMastery {

    public static final String MOD_ID = "simplyswordsmastery";
    private static final Logger LOGGER = LoggerFactory.getLogger("Simply Swords: Mastery");

    private SimplySwordsMastery() {
    }

    public static void init() {
        MasteryComponents.register();
        MasteryConfig.init();
        ProgressionOwnership.init();
        MasteryNetwork.init();
        SkillRuntime.init();
        StormMasteryRuntime.init();
        UniqueAbilityMasteryBridge.init();
        MasteryRewardService.init();
        MasteryProvenance.init();
        MasteryProgression.init();
        MasteryCommands.init();
        EnvExecutor.runInEnv(Env.CLIENT, () -> SimplySwordsMasteryClient::init);
        LOGGER.info("Simply Swords: Mastery initialized");
    }
}
