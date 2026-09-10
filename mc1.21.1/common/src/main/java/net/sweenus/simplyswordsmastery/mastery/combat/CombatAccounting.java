package net.sweenus.simplyswordsmastery.mastery.combat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.sweenus.simplyswords.api.combat.CombatProvenance;
import net.sweenus.simplyswords.api.combat.CombatProvenanceApi;

import java.util.ArrayDeque;

public final class CombatAccounting {
    private static final ThreadLocal<ArrayDeque<Frame>> FRAMES = ThreadLocal.withInitial(ArrayDeque::new);
    private CombatAccounting() {
    }

    public static Frame begin(LivingEntity target, DamageSource source) {
        Frame frame = new Frame(target, CombatProvenanceApi.current());
        FRAMES.get().push(frame);
        return frame;
    }

    public static void healthChanged(LivingEntity target, float before, float after) {
        double lost = Math.max(0, before - after);
        if (lost == 0) return;
        for (Frame frame : FRAMES.get()) {
            if (frame.target == target) {
                if (frame.damage == 0) frame.provenance = CombatProvenanceApi.current();
                frame.damage += lost;
                if (after <= 0) frame.lethal = true;
                return;
            }
        }
    }

    public static void finish(Frame frame) {
        ArrayDeque<Frame> frames = FRAMES.get();
        if (frames.pop() != frame) throw new IllegalStateException("Unbalanced mastery damage frame");
        boolean outer = frames.stream().anyMatch(value -> value.target == frame.target);
        if (frames.isEmpty()) FRAMES.remove();
        if (frame.damage > 0) CombatRewards.record(frame.target, frame.provenance, frame.damage, frame.lethal);
        if (!outer && !frame.target.isAlive()) CombatRewards.settle(frame.target);
    }

    public static final class Frame {
        private final LivingEntity target;
        private CombatProvenance provenance;
        private double damage;
        private boolean lethal;
        private Frame(LivingEntity target, CombatProvenance provenance) {
            this.target = target;
            this.provenance = provenance;
        }
    }
}
