package net.sweenus.simplyswordsmastery.mastery.progression;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sweenus.simplyswordsmastery.config.MasteryConfig;
import net.sweenus.simplyswordsmastery.mastery.definition.MasteryProfileRegistry;
import net.sweenus.simplyswordsmastery.mastery.state.MasteryComponents;
import net.sweenus.simplyswords.api.combat.CombatProvenance;
import net.sweenus.simplyswords.api.combat.CombatProvenanceApi;

import java.util.UUID;

public final class MasteryProvenance implements CombatProvenanceApi.Provider {
    public static void init() { CombatProvenanceApi.register(new MasteryProvenance()); }

    @Override public CombatProvenance capture(LivingEntity actor, ItemStack stack, int source) {
        if (!(actor instanceof ServerPlayerEntity player) || !MasteryRewardService.accessible(player, stack)) return null;
        var resolution = MasteryProfileRegistry.resolveProgressionServer(stack).orElse(null);
        if (resolution == null) return null;
        var owner = MasteryRewardService.owner(player, stack, resolution.groupId());
        var item = Registries.ITEM.getId(stack.getItem());
        var definitions = MasteryProfileRegistry.server().rewards();
        int percent = definitions == null ? 100 : definitions.weaponXpPercent(item, resolution.groupId());
        if (!resolution.profileIds().isEmpty() && resolution.profileIds().stream()
                .allMatch(MasteryConfig.SERVER.disabledProfiles::contains)) percent = 0;
        return new CombatProvenance(UUID.randomUUID(), player.getUuid(), stack.get(MasteryComponents.WEAPON_ID.get()),
                resolution.groupId(), owner.kind().name(), owner.subject(), item, percent, source, 0);
    }

    @Override public boolean matches(ItemStack stack, CombatProvenance provenance) {
        return provenance.weapon().equals(stack.get(MasteryComponents.WEAPON_ID.get()));
    }

    public static ProgressionOwner owner(CombatProvenance provenance) {
        return new ProgressionOwner(ProgressionOwner.Kind.valueOf(provenance.destinationKind()),
                provenance.destination(), provenance.group());
    }
}
