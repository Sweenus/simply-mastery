package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.SimplyMastery;
import net.sweenus.simplymastery.config.MasteryConfig;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplymastery.mastery.definition.MasteryProfileRegistry;
import net.sweenus.simplymastery.mastery.state.MasteryState;
import net.sweenus.simplymastery.mastery.state.MasteryStateAccess;
import net.sweenus.simplyswords.api.ability.UniqueAbilityApi;
import net.sweenus.simplyswords.api.ability.UniqueAbilityContext;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityObserver;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;

import java.util.ArrayList;
import java.util.List;

public final class UniqueAbilityMasteryBridge {
    private static final Identifier OWNER_ID = Identifier.of(SimplyMastery.MOD_ID, "mastery_nodes");

    private UniqueAbilityMasteryBridge() {
    }

    public static void init() {
        UniqueAbilityApi.registerModifier(OWNER_ID, 0, UniqueAbilityMasteryBridge::prepare);
    }

    private static UniqueAbilityObserver prepare(UniqueAbilityContext context, UniqueAbilityDefinition definition,
                                                 UniqueAbilityTuning.Builder tuning) {
        if (!MasteryConfig.SERVER.enabled) return UniqueAbilityObserver.NONE;
        MasteryProfile profile = MasteryProfileRegistry.resolveServer(context.stack()).orElse(null);
        if (profile == null || MasteryConfig.SERVER.disabledProfiles.contains(profile.id())) {
            return UniqueAbilityObserver.NONE;
        }
        MasteryState state = MasteryStateAccess.read(context.stack(), profile,
                Math.min(MasteryConfig.SERVER.verticalSliceStartingPoints, MasteryConfig.SERVER.maximumEarnedPoints));
        List<OwnedAbilityEffect> effects = new ArrayList<>();
        for (MasteryProfile.Node node : profile.nodes()) {
            if (!state.owns(node.id()) || MasteryConfig.SERVER.disabledEffects.contains(node.effect().type())) continue;
            SkillEffectType type = SkillEffectRegistry.get(node.effect().type());
            if (!(type instanceof AbilitySkillEffectType abilityType)) continue;
            abilityType.tune(context, definition, tuning, node);
            effects.add(new OwnedAbilityEffect(abilityType, node));
        }
        if (effects.isEmpty()) return UniqueAbilityObserver.NONE;
        List<OwnedAbilityEffect> frozen = List.copyOf(effects);
        return event -> frozen.forEach(effect -> effect.type.onAbilityEvent(event, effect.node));
    }

    private record OwnedAbilityEffect(AbilitySkillEffectType type, MasteryProfile.Node node) {
    }
}
