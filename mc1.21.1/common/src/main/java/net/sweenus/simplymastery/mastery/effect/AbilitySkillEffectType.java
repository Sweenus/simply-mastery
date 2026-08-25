package net.sweenus.simplymastery.mastery.effect;

import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.UniqueAbilityContext;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityEvent;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;

public interface AbilitySkillEffectType extends SkillEffectType {
    default void tune(UniqueAbilityContext context, UniqueAbilityDefinition definition,
                      UniqueAbilityTuning.Builder tuning, MasteryProfile.Node node) {
    }

    default void onAbilityEvent(UniqueAbilityEvent event, MasteryProfile.Node node) {
    }
}
