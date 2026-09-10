package net.sweenus.simplyswordsmastery.mastery.effect;

import net.sweenus.simplyswordsmastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.UniqueAbilityContext;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;

public interface StaticAbilitySkillEffectType extends AbilitySkillEffectType {
    @Override
    default void tune(UniqueAbilityContext context, UniqueAbilityDefinition definition,
                      UniqueAbilityTuning.Builder tuning, MasteryProfile.Node node) {
        tuneStatic(definition, tuning, node);
    }

    void tuneStatic(UniqueAbilityDefinition definition, UniqueAbilityTuning.Builder tuning, MasteryProfile.Node node);
}
