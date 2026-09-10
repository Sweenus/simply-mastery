package net.sweenus.simplyswordsmastery.client.mastery.ui;

public final class NodeAnimators {

    private final Anim[] hover;
    private final Anim[] focus;
    private final Anim[] owned;
    private final float[] burst;

    public NodeAnimators(int count) {
        hover = new Anim[count];
        focus = new Anim[count];
        owned = new Anim[count];
        burst = new float[count];
        for (int i = 0; i < count; i++) {
            hover[i] = new Anim(0.0F, 26.0F);
            focus[i] = new Anim(0.0F, 24.0F);
            owned[i] = new Anim(0.0F, 12.0F);
        }
    }

    public void setTargets(int index, boolean isHovered, boolean isFocused, boolean isOwned) {
        hover[index].target(isHovered ? 1.0F : 0.0F);
        focus[index].target(isFocused ? 1.0F : 0.0F);
        owned[index].target(isOwned ? 1.0F : 0.0F);
    }

    public void snapOwned(int index, boolean isOwned) {
        owned[index].snap(isOwned ? 1.0F : 0.0F);
    }

    public void triggerBurst(int index) {
        burst[index] = 1.0F;
    }

    public void advance(float deltaSeconds, boolean instant) {
        for (int i = 0; i < hover.length; i++) {
            hover[i].advance(deltaSeconds, instant);
            focus[i].advance(deltaSeconds, instant);
            owned[i].advance(deltaSeconds, instant);
            if (burst[i] > 0.0F) {
                burst[i] = instant ? 0.0F : Math.max(0.0F, burst[i] - deltaSeconds / 0.42F);
            }
        }
    }

    public float hover(int index) {
        return hover[index].value();
    }

    public float focus(int index) {
        return focus[index].value();
    }

    public float owned(int index) {
        return owned[index].value();
    }

    public float burst(int index) {
        return burst[index];
    }
}
