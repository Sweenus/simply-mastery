package net.sweenus.simplymastery.client.mastery.ui;

public final class Anim {

    private float value;
    private float target;
    private final float rate;

    public Anim(float initial, float rate) {
        this.value = initial;
        this.target = initial;
        this.rate = rate;
    }

    public void target(float newTarget) {
        target = newTarget;
    }

    public void snap(float newValue) {
        value = newValue;
        target = newValue;
    }

    public float value() {
        return value;
    }

    public void advance(float deltaSeconds, boolean instant) {
        if (instant || deltaSeconds <= 0.0F) {
            value = target;
            return;
        }
        value += (target - value) * (1.0F - (float) Math.exp(-rate * deltaSeconds));
        if (Math.abs(target - value) < 0.0005F) {
            value = target;
        }
    }
}
