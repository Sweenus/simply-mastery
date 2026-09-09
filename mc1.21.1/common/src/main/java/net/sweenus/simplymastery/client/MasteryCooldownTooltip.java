package net.sweenus.simplymastery.client;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.config.MasteryConfig;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplymastery.mastery.definition.MasteryProfileRegistry;
import net.sweenus.simplymastery.mastery.effect.AbilitySkillEffectType;
import net.sweenus.simplymastery.mastery.effect.SkillEffectRegistry;
import net.sweenus.simplymastery.mastery.effect.StaticAbilitySkillEffectType;
import net.sweenus.simplymastery.mastery.state.MasteryComponents;
import net.sweenus.simplymastery.mastery.state.MasteryPortfolio;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;
import net.sweenus.simplyswords.client.util.TooltipUtils;
import net.sweenus.simplyswords.api.ability.NatureSwarmMasteryAbilities;
import net.sweenus.simplyswords.api.ability.AbyssalSpectralMasteryAbilities;
import net.sweenus.simplyswords.api.ability.StormSoulMasteryAbilities;
import net.sweenus.simplyswords.api.ability.FireForgeMasteryAbilities;
import net.sweenus.simplyswords.api.ability.StormFrostWaterMasteryAbilities;
import net.sweenus.simplyswords.api.ability.DeathShadowBloodMasteryAbilities;
import net.sweenus.simplyswords.api.ability.LongPathFinalFormsMasteryAbilities;
import net.sweenus.simplyswords.api.ability.ArcaneCosmicMasteryAbilities;
import net.sweenus.simplyswords.api.ability.MartialCommandEldritchMasteryAbilities;
import net.sweenus.simplyswords.api.ability.AbyssalSpectralMasteryTuning;
import net.sweenus.simplyswords.api.ability.ArcaneCosmicMasteryTuning;
import net.sweenus.simplyswords.api.ability.MartialCommandEldritchMasteryTuning;
import net.sweenus.simplyswords.api.ability.FireForgeMasteryTuning;

import java.util.List;
import java.util.Locale;

public final class MasteryCooldownTooltip {
    private MasteryCooldownTooltip() {
    }

    public static void init() {
        TooltipUtils.registerCooldownFormatter(Identifier.of("simplymastery", "mastery"), MasteryCooldownTooltip::format);
    }

    private static Text format(ItemStack stack, int baseTicks) {
        if (!MasteryConfig.SERVER.enabled) return null;
        MasteryProfile profile = MasteryProfileRegistry.resolveClient(stack).orElse(null);
        if (profile == null || MasteryConfig.SERVER.disabledProfiles.contains(profile.id())) return null;
        int initial = Math.min(MasteryConfig.SERVER.verticalSliceStartingPoints, MasteryConfig.SERVER.maximumEarnedPoints);
        MasteryPortfolio portfolio = stack.get(MasteryComponents.MASTERY_PORTFOLIO.get());
        if (portfolio == null) {
            portfolio = MasteryPortfolio.importLegacy(stack.get(MasteryComponents.MASTERY_STATE.get()),
                    profile.progressionGroupId(), initial);
        }
        MasteryPortfolio.ProfileLoadout loadout = portfolio.loadout(profile.id()).orElse(null);
        if (loadout == null) return null;
        List<MasteryProfile.Node> owned = profile.nodes().stream()
                .filter(node -> loadout.unlockedNodeIds().contains(node.id()) && !MasteryConfig.SERVER.disabledEffects.contains(node.effect().type()))
                .toList();
        if (owned.isEmpty()) return null;
        List<UniqueAbilityDefinition> phases = phases(profile.id());
        boolean conditional = loadout.profileVersion() != profile.version() || phases.isEmpty() || owned.stream().anyMatch(node ->
                SkillEffectRegistry.get(node.effect().type()) instanceof AbilitySkillEffectType effect
                        && !(effect instanceof StaticAbilitySkillEffectType));
        int ticks = baseTicks;
        if (!conditional) {
            try {
                for (UniqueAbilityDefinition phase : phases) ticks = compose(phase, owned, ticks);
            } catch (IllegalArgumentException exception) {
                conditional = true;
                ticks = baseTicks;
            }
        }
        int effective = TooltipUtils.getEffectiveWeaponCooldownTicks(stack, ticks);
        String seconds = effective % 20 == 0 ? Integer.toString(effective / 20)
                : String.format(Locale.ROOT, "%.2f", effective / 20.0).replaceAll("0+$", "");
        return Text.translatable(conditional ? "tooltip.simplymastery.base_cooldown"
                : "tooltip.simplymastery.mastered_cooldown", seconds);
    }

    private static int compose(UniqueAbilityDefinition definition, List<MasteryProfile.Node> nodes, int baseTicks) {
        var key = definition.cooldownKey().orElseThrow();
        UniqueAbilityTuning.Builder tuning = UniqueAbilityTuning.builder(definition).set(key, baseTicks);
        if (definition.supports(AbyssalSpectralMasteryAbilities.TUNING)) {
            tuning.set(AbyssalSpectralMasteryAbilities.TUNING, AbyssalSpectralMasteryTuning.EMPTY
                    .with(AbyssalSpectralMasteryTuning.Setting.COOLDOWN_TICKS, baseTicks));
        }
        if (definition.supports(ArcaneCosmicMasteryAbilities.TUNING)) {
            tuning.set(ArcaneCosmicMasteryAbilities.TUNING, ArcaneCosmicMasteryTuning.EMPTY
                    .with(ArcaneCosmicMasteryTuning.Setting.COOLDOWN_TICKS, baseTicks));
        }
        if (definition.supports(MartialCommandEldritchMasteryAbilities.TUNING)) {
            tuning.set(MartialCommandEldritchMasteryAbilities.TUNING, MartialCommandEldritchMasteryTuning.EMPTY
                    .with(MartialCommandEldritchMasteryTuning.Setting.COOLDOWN_TICKS, baseTicks));
        }
        if (definition.supports(FireForgeMasteryAbilities.TUNING)) {
            tuning.set(FireForgeMasteryAbilities.TUNING, FireForgeMasteryTuning.EMPTY
                    .with(FireForgeMasteryTuning.Setting.COOLDOWN_TICKS, baseTicks));
        }
        for (MasteryProfile.Node node : nodes) {
            if (SkillEffectRegistry.get(node.effect().type()) instanceof StaticAbilitySkillEffectType effect) {
                effect.tuneStatic(definition, tuning, node);
            }
        }
        return tuning.get(key);
    }

    private static List<UniqueAbilityDefinition> phases(Identifier profile) {
        if (!profile.getNamespace().equals("simplymastery")) return List.of();
        return switch (profile.getPath()) {
            case "bramblethorn" -> List.of(NatureSwarmMasteryAbilities.BRAMBLE_GRASP);
            case "waxweaver" -> List.of(NatureSwarmMasteryAbilities.WAXWEAVER_PRISON);
            case "hiveheart" -> List.of(NatureSwarmMasteryAbilities.HIVEHEART_SWARM);
            case "chompolotl" -> List.of(NatureSwarmMasteryAbilities.CHOMPOLOTL_RALLY);
            case "watcher_claymore" -> List.of(AbyssalSpectralMasteryAbilities.WATCHER_OMEN);
            case "the_devourer" -> List.of(AbyssalSpectralMasteryAbilities.DEVOURER_MASS);
            case "wickpiercer" -> List.of(AbyssalSpectralMasteryAbilities.WICKPIERCER_THROW);
            case "gloampiercer" -> List.of(AbyssalSpectralMasteryAbilities.GLOAMPIERCER_BARRAGE);
            case "wraithfang" -> List.of(AbyssalSpectralMasteryAbilities.WRAITHFANG_THROW);
            case "wraithmaw" -> List.of(AbyssalSpectralMasteryAbilities.WRAITHMAW_MUSTER);
            case "stormscale" -> List.of(StormSoulMasteryAbilities.STORMSCALE_ROD);
            case "soulrender" -> List.of(StormSoulMasteryAbilities.SOULRENDER_REAP);
            case "soulstalker" -> List.of(StormSoulMasteryAbilities.SOULSTALKER_STRIDE);
            case "whisperwind" -> List.of(StormSoulMasteryAbilities.WHISPERWIND_DASH);
            case "dreadwhisper" -> List.of(StormSoulMasteryAbilities.DREADWHISPER_REAVE);
            case "hearthflame" -> List.of(FireForgeMasteryAbilities.HEARTHFLAME_CHAINS);
            case "emberblade" -> List.of(FireForgeMasteryAbilities.EMBERBLADE_SHRAPNEL);
            case "emberlash" -> List.of(FireForgeMasteryAbilities.EMBERLASH_CAUTERY);
            case "flamewind" -> List.of(FireForgeMasteryAbilities.FLAMEWIND_SEED);
            case "molten_edge" -> List.of(FireForgeMasteryAbilities.MOLTEN_EDGE_VENT);
            case "soulpyre" -> List.of(FireForgeMasteryAbilities.SOUL_PYRE_TETHER);
            case "stormbringer" -> List.of(StormFrostWaterMasteryAbilities.STORMBRINGER_GUARD);
            case "mjolnir" -> List.of(StormFrostWaterMasteryAbilities.MJOLNIR_STORM);
            case "thunderbrand" -> List.of(StormFrostWaterMasteryAbilities.THUNDERBRAND_BLITZ);
            case "tempest" -> List.of(StormFrostWaterMasteryAbilities.TEMPEST_VORTEX);
            case "frostfall" -> List.of(StormFrostWaterMasteryAbilities.FROSTFALL_THROW);
            case "icewhisper" -> List.of(StormFrostWaterMasteryAbilities.ICEWHISPER_COMETS);
            case "soulkeeper" -> List.of(DeathShadowBloodMasteryAbilities.SOULKEEPER_CONCLAVE);
            case "soulstealer" -> List.of(DeathShadowBloodMasteryAbilities.SOULSTEALER_REAP);
            case "twisted_blade" -> List.of(DeathShadowBloodMasteryAbilities.TWISTED_FINALE);
            case "bloodwake" -> List.of(DeathShadowBloodMasteryAbilities.BLOOD_RITES);
            case "sunfire" -> List.of(LongPathFinalFormsMasteryAbilities.SUNFIRE_STANDARD);
            case "harbinger" -> List.of(LongPathFinalFormsMasteryAbilities.HARBINGER_STANDARD);
            case "stars_edge" -> List.of(ArcaneCosmicMasteryAbilities.STARS_CONSTELLATION);
            case "magiscythe" -> List.of(ArcaneCosmicMasteryAbilities.MAGISCYTHE_STORM);
            case "ribboncleaver" -> List.of(MartialCommandEldritchMasteryAbilities.RIBBON_RUSH);
            case "dawnquiver" -> List.of(MartialCommandEldritchMasteryAbilities.DAWN_DRAW);
            case "dreadtide" -> List.of(MartialCommandEldritchMasteryAbilities.DREAD_ASSAULT);
            case "arcanethyst" -> List.of(ArcaneCosmicMasteryAbilities.ARCANETHYST_IMPACT,
                    ArcaneCosmicMasteryAbilities.ARCANETHYST_SUSPENSION);
            case "magispear" -> List.of(ArcaneCosmicMasteryAbilities.MAGISPEAR_SLAM,
                    ArcaneCosmicMasteryAbilities.MAGISPEAR_RAIN);
            default -> List.of();
        };
    }
}
