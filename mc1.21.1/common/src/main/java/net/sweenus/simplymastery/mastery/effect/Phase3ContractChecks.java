package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.SimplyMastery;
import net.sweenus.simplyswords.api.ability.BuiltinUniqueAbilities;
import net.sweenus.simplyswords.api.ability.Phase2UniqueAbilities;
import net.sweenus.simplyswords.api.ability.Phase3UniqueAbilities;
import net.sweenus.simplyswords.api.ability.Phase4UniqueAbilities;
import net.sweenus.simplyswords.api.ability.Phase5UniqueAbilities;
import net.sweenus.simplyswords.api.ability.Phase5AbilityTuning;
import net.sweenus.simplyswords.api.ability.Phase6UniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityApi;

public final class Phase3ContractChecks {

    private Phase3ContractChecks() {
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
        requireEffect("phase2_mastery");
        requireEffect("phase3_mastery");
        requireEffect("phase4_mastery");
        requireEffect("phase5_mastery");
        requireEffect("phase6_mastery");
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
                || !UniqueAbilityApi.isDefinitionRegistered(Phase2UniqueAbilities.WATCHER_DREAD_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase2UniqueAbilities.WATCHER_OMEN_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase2UniqueAbilities.DEVOURER_MASS_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase2UniqueAbilities.DEVOURER_REPRISAL_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase2UniqueAbilities.WICKPIERCER_THROW_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase2UniqueAbilities.WICKPIERCER_REVIVE_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase2UniqueAbilities.GLOAMPIERCER_AMBUSH_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase2UniqueAbilities.GLOAMPIERCER_BARRAGE_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase2UniqueAbilities.WRAITHFANG_THROW_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase2UniqueAbilities.WRAITHMAW_MUSTER_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase3UniqueAbilities.STORMSCALE_ROD_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase3UniqueAbilities.IONBOUND_CRUSHER_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase3UniqueAbilities.IONBOUND_BEAM_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase3UniqueAbilities.IONBOUND_SHIELD_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase3UniqueAbilities.SOULRENDER_MARK_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase3UniqueAbilities.SOULRENDER_REAP_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase3UniqueAbilities.SOULRENDER_GRAVE_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase3UniqueAbilities.SOULSTALKER_TENDRIL_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase3UniqueAbilities.SOULSTALKER_STRIDE_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase3UniqueAbilities.WHISPERWIND_DASH_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase3UniqueAbilities.WHISPERWIND_RESET_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase3UniqueAbilities.DREADWHISPER_REAVE_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase3UniqueAbilities.DREADWHISPER_WOUND_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase4UniqueAbilities.LICHBLADE_AURA_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase4UniqueAbilities.LICHBLADE_CHANNEL_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase4UniqueAbilities.SUNFIRE_STANDARD_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase4UniqueAbilities.SUNFIRE_REGEN_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase4UniqueAbilities.HARBINGER_STANDARD_ID)
                || !UniqueAbilityApi.isDefinitionRegistered(Phase4UniqueAbilities.HARBINGER_OMEN_ID)) {
            throw new IllegalStateException("Missing unique ability definitions");
        }
        for (var definition : Phase5UniqueAbilities.definitions()) {
            if (!UniqueAbilityApi.isDefinitionRegistered(definition.id())) {
                throw new IllegalStateException("Missing unique ability definition " + definition.id());
            }
        }
        for (var definition : Phase6UniqueAbilities.definitions()) {
            if (!UniqueAbilityApi.isDefinitionRegistered(definition.id())) {
                throw new IllegalStateException("Missing unique ability definition " + definition.id());
            }
        }
        Phase5AbilityTuning backburn = Phase5MasterySkillEffect.flamewind(
                Phase5AbilityTuning.EMPTY, 2, 3);
        Phase5AbilityTuning controlled = Phase5MasterySkillEffect.flamewind(backburn, 2, 7);
        Phase5AbilityTuning wildfire = Phase5MasterySkillEffect.flamewind(backburn, 2, 8);
        if (backburn.integer(Phase5AbilityTuning.Setting.COOLDOWN_TICKS, 350) != 100
                || controlled.integer(Phase5AbilityTuning.Setting.COOLDOWN_TICKS, 350) != 140
                || wildfire.integer(Phase5AbilityTuning.Setting.COOLDOWN_TICKS, 350) != 160) {
            throw new IllegalStateException("Invalid Flamewind release cooldown tuning");
        }
    }
}
