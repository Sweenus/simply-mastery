package net.sweenus.simplymastery.client.mastery.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnimTest {

    @Test
    void reducedMotionSnapsToTheTarget() {
        Anim anim = new Anim(0.0F, 7.0F);
        anim.target(1.0F);
        anim.advance(0.001F, true);
        assertEquals(1.0F, anim.value());
    }

    @Test
    void ordinaryMotionApproachesWithoutOvershooting() {
        Anim anim = new Anim(0.0F, 7.0F);
        anim.target(1.0F);
        anim.advance(0.05F, false);
        assertTrue(anim.value() > 0.0F && anim.value() < 1.0F);
    }
}
