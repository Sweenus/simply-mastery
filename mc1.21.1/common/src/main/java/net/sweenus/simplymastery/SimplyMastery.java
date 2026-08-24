package net.sweenus.simplymastery;

import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.sweenus.simplymastery.client.SimplyMasteryClient;
import net.sweenus.simplymastery.config.MasteryConfig;
import net.sweenus.simplymastery.mastery.network.MasteryNetwork;
import net.sweenus.simplymastery.mastery.state.MasteryComponents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SimplyMastery {

    public static final String MOD_ID = "simplymastery";
    private static final Logger LOGGER = LoggerFactory.getLogger("Simply Mastery");

    private SimplyMastery() {
    }

    public static void init() {
        MasteryComponents.register();
        MasteryConfig.init();
        MasteryNetwork.init();
        EnvExecutor.runInEnv(Env.CLIENT, () -> SimplyMasteryClient::init);
        LOGGER.info("Simply Mastery initialized");
    }
}
