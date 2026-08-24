package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sweenus.simplymastery.mastery.state.MasteryCooldownState;
import net.sweenus.simplymastery.mastery.state.MasteryStateAccess;

public final class SkillEffectAccess {

    private final ServerPlayerEntity player;
    private final ItemStack stack;
    private final String profileKey;

    SkillEffectAccess(ServerPlayerEntity player, ItemStack stack, String profileKey) {
        this.player = player;
        this.stack = stack;
        this.profileKey = profileKey;
    }

    public boolean startCooldown(String nodeId, long tick, int durationTicks) {
        MasteryCooldownState cooldowns = MasteryStateAccess.cooldowns(stack);
        String key = profileKey + "/" + nodeId;
        if (!cooldowns.ready(key, tick)) return false;
        MasteryStateAccess.writeCooldowns(stack, cooldowns.start(key, tick + Math.max(1, durationTicks)));
        player.getInventory().markDirty();
        return true;
    }

    public boolean advanceCombo(HandKey hand, String nodeId, long tick, int windowTicks, int hits) {
        return SkillRuntime.advanceCombo(player, hand, profileKey + "/" + nodeId, tick, windowTicks, hits);
    }

    public boolean dealAdditionalDamage(LivingEntity target, String nodeId, float amount) {
        if (amount <= 0.0F || !target.isAlive()) return false;
        return SkillOriginGuard.run(new SkillOriginGuard.Origin(player, stack, nodeId),
                () -> target.damage(player.getServerWorld().getDamageSources().indirectMagic(player, player), amount));
    }

    public enum HandKey {
        MAIN_HAND,
        OFF_HAND
    }
}
