package net.sweenus.simplymastery.client.mastery;

import net.minecraft.screen.ScreenHandler;

public final class MasteryScreenSwitch {

    private static ScreenHandler switchingHandler;

    private MasteryScreenSwitch() {
    }

    public static void run(ScreenHandler handler, Runnable switchAction) {
        switchingHandler = handler;
        try {
            switchAction.run();
        } finally {
            switchingHandler = null;
        }
    }

    public static boolean isSwitching(ScreenHandler handler) {
        return switchingHandler == handler;
    }
}
