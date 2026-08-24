package net.sweenus.simplymastery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SimplyMastery {

    public static final String MOD_ID = "simplymastery";
    private static final Logger LOGGER = LoggerFactory.getLogger("Simply Mastery");

    private SimplyMastery() {
    }

    public static void init() {
        LOGGER.info("Simply Mastery initialized");
    }
}

