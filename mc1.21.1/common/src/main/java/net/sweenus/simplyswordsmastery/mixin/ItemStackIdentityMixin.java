package net.sweenus.simplyswordsmastery.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.sweenus.simplyswordsmastery.mastery.progression.MasteryRewardService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackIdentityMixin {
    @Inject(method = "use", at = @At("HEAD"))
    private void simplyswordsmastery$beforeUse(World world, PlayerEntity user, Hand hand,
                                         CallbackInfoReturnable<TypedActionResult<ItemStack>> ci) {
        if (user instanceof ServerPlayerEntity player && player.getStackInHand(hand) == (Object) this) {
            MasteryRewardService.synchronize(player.getServer(), player.getStackInHand(hand));
        }
    }
}
