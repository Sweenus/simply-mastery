package net.sweenus.simplyswordsmastery.config;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;

public final class MasteryConfig {

    public static final MasteryServerConfig SERVER =
            ConfigApiJava.registerAndLoadConfig(MasteryServerConfig::new);
    public static final MasteryClientConfig CLIENT =
            ConfigApiJava.registerAndLoadConfig(MasteryClientConfig::new, RegisterType.CLIENT);

    private MasteryConfig() {
    }

    public static void init() {
    }
}
