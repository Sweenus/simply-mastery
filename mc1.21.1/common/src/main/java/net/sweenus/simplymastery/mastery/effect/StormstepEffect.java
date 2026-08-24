package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.sweenus.simplymastery.config.MasteryConfig;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplymastery.mastery.state.MasteryState;
import net.sweenus.simplymastery.mastery.state.MasteryStateAccess;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class StormstepEffect {

    private static final Map<UUID, Long> LAST_TRIGGER_TICK = new HashMap<>();

    private StormstepEffect() {
    }

    public static void onSuccessfulMeleeHit(ServerPlayerEntity player, ItemStack stack) {
        if (!MasteryConfig.SERVER.enabled) {
            return;
        }
        MasteryProfile profile = MasteryProfile.resolve(stack).orElse(null);
        if (profile == null) {
            return;
        }
        MasteryState state = MasteryStateAccess.read(stack, profile,
                Math.min(MasteryConfig.SERVER.verticalSliceStartingPoints,
                        MasteryConfig.SERVER.maximumEarnedPoints));
        if (!state.owns("charged_pursuit")) {
            return;
        }
        ServerWorld world = player.getServerWorld();
        long tick = world.getTime();
        Long previousTick = LAST_TRIGGER_TICK.put(player.getUuid(), tick);
        if (previousTick != null && previousTick == tick) {
            return;
        }
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,
                MasteryConfig.SERVER.stormstepDurationTicks,
                MasteryConfig.SERVER.stormstepAmplifier, false, false, true));
        world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                player.getX(), player.getBodyY(0.45), player.getZ(), 14, 0.35, 0.25, 0.35, 0.08);
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT,
                SoundCategory.PLAYERS, 0.3F, 1.7F);
    }
}
