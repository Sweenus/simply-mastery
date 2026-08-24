package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;

import java.util.List;

public interface SkillEffectType {

    Identifier id();

    void validate(MasteryProfile.Effect effect, String where, List<String> errors);

    default void onAttack(SkillEffectContext.Attack context, MasteryProfile.Node node) {
    }

    default void onKill(SkillEffectContext.Kill context, MasteryProfile.Node node) {
    }

    default void onHeldTick(SkillEffectContext.HeldTick context, MasteryProfile.Node node) {
    }

    default void onDamageReceived(SkillEffectContext.DamageReceived context, MasteryProfile.Node node) {
    }

    default void onProjectile(SkillEffectContext.Projectile context, MasteryProfile.Node node) {
    }

    default void onAbility(SkillEffectContext.Ability context, MasteryProfile.Node node) {
    }

    default boolean needsHeldTick() {
        return false;
    }
}
