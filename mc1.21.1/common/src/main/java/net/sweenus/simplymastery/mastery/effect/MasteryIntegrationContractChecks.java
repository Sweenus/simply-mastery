package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.SimplyMastery;
import net.sweenus.simplymastery.mastery.definition.MasteryCohort;
import net.sweenus.simplyswords.api.ability.BuiltinUniqueAbilities;
import net.sweenus.simplyswords.api.ability.AbyssalSpectralMasteryAbilities;
import net.sweenus.simplyswords.api.ability.StormSoulMasteryAbilities;
import net.sweenus.simplyswords.api.ability.LongPathFinalFormsMasteryAbilities;
import net.sweenus.simplyswords.api.ability.FireForgeMasteryAbilities;
import net.sweenus.simplyswords.api.ability.FireForgeMasteryTuning;
import net.sweenus.simplyswords.api.ability.StormFrostWaterMasteryAbilities;
import net.sweenus.simplyswords.api.ability.NatureSwarmMasteryAbilities;
import net.sweenus.simplyswords.api.ability.DeathShadowBloodMasteryAbilities;
import net.sweenus.simplyswords.api.ability.ArcaneCosmicMasteryAbilities;
import net.sweenus.simplyswords.api.ability.MartialCommandEldritchMasteryAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityApi;

public final class MasteryIntegrationContractChecks {

    private MasteryIntegrationContractChecks() {
    }

    public static void requireEffect(String path) {
        Identifier id = Identifier.of(SimplyMastery.MOD_ID, path);
        if (SkillEffectRegistry.get(id) == null) throw new IllegalStateException("Missing skill effect " + id);
    }

    public static void requireRecursionGuard() {
        boolean outer = SkillOriginGuard.run(new SkillOriginGuard.Origin(null, null, "test"),
                () -> !SkillOriginGuard.run(new SkillOriginGuard.Origin(null, null, "nested"), () -> true));
        if (!outer || SkillOriginGuard.active()) throw new IllegalStateException("Skill recursion guard leaked");
    }

    public static void requireReleasedEffects() {
        for (String path : new String[]{"combo_strike", "battle_flow", "hindering_strike",
                "finishing_strike", "soul_mend", "kill_flow", "counterstrike", "guarded_recovery",
                "leeching_strike", "cleaving_echo"}) {
            requireEffect(path);
        }
    }

    public static void requireUniqueAbilityIntegration() {
        for (MasteryCohort cohort : MasteryCohort.values()) requireEffect("cohort/" + cohort.id());
        for (String path : new String[]{"stormbreak_conduit", "slipstream", "crosswind", "capacitor",
                "storm_chaser", "flashguard", "afterimage", "eye_of_storm", "thunderhead",
                "static_reserve", "charged_pursuit", "building_voltage", "live_wire", "feedback_loop",
                "quickening_current", "unbroken_pace", "perpetual_motion", "flashover", "ionize",
                "arc_lash", "pressure_drop", "updraft", "fulmination", "stormshield", "reverberation",
                "judgment_bolt", "supercell", "sulfurous_edge", "scorching_brand", "blast_furnace",
                "kindling_blows", "flashpoint", "cinder_scatter", "backdraft", "chain_reaction",
                "crucible_strike", "lengthened_chain", "furnace_bellows", "stoked_furnace",
                "shackling_heat", "overpressure", "snapback", "molten_wake", "executioners_drop",
                "perpetual_furnace", "cinder_mantle", "tempered_flesh", "heat_sink",
                "furnace_reprisal", "forged_resolve", "ashen_step", "bulwark_pulse", "walking_furnace",
                "last_reprisal"}) {
            requireEffect(path);
        }
        if (!UniqueAbilityApi.isDefinitionRegistered(BuiltinUniqueAbilities.STORMBREAK_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(BuiltinUniqueAbilities.STORMS_EDGE_REFRESH_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(BuiltinUniqueAbilities.STORMS_EDGE_MELEE_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(BuiltinUniqueAbilities.BRIMSTONE_ERUPTION_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(BuiltinUniqueAbilities.BRIMSTONE_RITE_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(AbyssalSpectralMasteryAbilities.WATCHER_DREAD_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(AbyssalSpectralMasteryAbilities.WATCHER_OMEN_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(AbyssalSpectralMasteryAbilities.DEVOURER_MASS_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(AbyssalSpectralMasteryAbilities.DEVOURER_REPRISAL_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(AbyssalSpectralMasteryAbilities.WICKPIERCER_THROW_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(AbyssalSpectralMasteryAbilities.WICKPIERCER_REVIVE_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(AbyssalSpectralMasteryAbilities.GLOAMPIERCER_AMBUSH_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(AbyssalSpectralMasteryAbilities.GLOAMPIERCER_BARRAGE_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(AbyssalSpectralMasteryAbilities.WRAITHFANG_THROW_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(AbyssalSpectralMasteryAbilities.WRAITHMAW_MUSTER_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(StormSoulMasteryAbilities.STORMSCALE_ROD_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(StormSoulMasteryAbilities.IONBOUND_CRUSHER_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(StormSoulMasteryAbilities.IONBOUND_BEAM_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(StormSoulMasteryAbilities.IONBOUND_SHIELD_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(StormSoulMasteryAbilities.SOULRENDER_MARK_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(StormSoulMasteryAbilities.SOULRENDER_REAP_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(StormSoulMasteryAbilities.SOULRENDER_GRAVE_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(StormSoulMasteryAbilities.SOULSTALKER_TENDRIL_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(StormSoulMasteryAbilities.SOULSTALKER_STRIDE_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(StormSoulMasteryAbilities.WHISPERWIND_DASH_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(StormSoulMasteryAbilities.WHISPERWIND_RESET_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(StormSoulMasteryAbilities.DREADWHISPER_REAVE_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(StormSoulMasteryAbilities.DREADWHISPER_WOUND_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(LongPathFinalFormsMasteryAbilities.LICHBLADE_AURA_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(LongPathFinalFormsMasteryAbilities.LICHBLADE_CHANNEL_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(LongPathFinalFormsMasteryAbilities.SUNFIRE_STANDARD_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(LongPathFinalFormsMasteryAbilities.SUNFIRE_REGEN_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(LongPathFinalFormsMasteryAbilities.HARBINGER_STANDARD_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(LongPathFinalFormsMasteryAbilities.HARBINGER_OMEN_ID)) {
            throw new IllegalStateException("Missing unique ability definitions");
        }
        for (var definition : FireForgeMasteryAbilities.definitions()) {
            if (!UniqueAbilityApi.isDefinitionRegistered(definition.id())) {
                throw new IllegalStateException("Missing unique ability definition " + definition.id());
            }
        }
        for (var definition : StormFrostWaterMasteryAbilities.definitions()) {
            if (!UniqueAbilityApi.isDefinitionRegistered(definition.id())) {
                throw new IllegalStateException("Missing unique ability definition " + definition.id());
            }
        }
        for (var definitions : java.util.List.of(
                NatureSwarmMasteryAbilities.definitions(),
                DeathShadowBloodMasteryAbilities.definitions(),
                ArcaneCosmicMasteryAbilities.definitions(),
                MartialCommandEldritchMasteryAbilities.definitions())) {
            for (var definition : definitions) {
                if (!UniqueAbilityApi.isDefinitionRegistered(definition.id())) {
                    throw new IllegalStateException("Missing unique ability definition " + definition.id());
                }
            }
        }
        FireForgeMasteryTuning backburn = FireForgeMasterySkillEffect.flamewind(
                FireForgeMasteryTuning.EMPTY, 2, 3);
        FireForgeMasteryTuning controlled = FireForgeMasterySkillEffect.flamewind(backburn, 2, 7);
        FireForgeMasteryTuning wildfire = FireForgeMasterySkillEffect.flamewind(backburn, 2, 8);
        if (backburn.integer(FireForgeMasteryTuning.Setting.COOLDOWN_TICKS, 350) != 100
                || controlled.integer(FireForgeMasteryTuning.Setting.COOLDOWN_TICKS, 350) != 140
                || wildfire.integer(FireForgeMasteryTuning.Setting.COOLDOWN_TICKS, 350) != 160) {
            throw new IllegalStateException("Invalid Flamewind release cooldown tuning");
        }
    }
}
