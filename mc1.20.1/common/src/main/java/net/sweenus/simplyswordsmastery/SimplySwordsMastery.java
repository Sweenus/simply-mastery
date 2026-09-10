package net.sweenus.simplyswordsmastery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SimplySwordsMastery {

    public static final String MOD_ID = "simplyswordsmastery";
    private static final Logger LOGGER = LoggerFactory.getLogger("Simply Swords: Mastery");

    private SimplySwordsMastery() {
    }

    public static void init() {
        LOGGER.info("Simply Swords: Mastery initialized");
    }
}

