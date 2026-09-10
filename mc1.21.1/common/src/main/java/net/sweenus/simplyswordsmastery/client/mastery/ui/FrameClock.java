package net.sweenus.simplyswordsmastery.client.mastery.ui;

import net.minecraft.util.Util;

public final class FrameClock {

    private long lastFrameMs = Util.getMeasuringTimeMs();

    public float advance() {
        long now = Util.getMeasuringTimeMs();
        float step = Math.min(0.1F, (now - lastFrameMs) / 1000.0F);
        lastFrameMs = now;
        return step;
    }
}
