package net.sweenus.simplymastery.mastery;

import net.minecraft.item.ItemStack;
import net.sweenus.simplyswords.screen.RunicForgeScreenHandler;

public final class RunicForgeMasteryContext {

    private RunicForgeMasteryContext() {
    }

    public static ItemStack identityStack(RunicForgeScreenHandler handler) {
        return identityStack(stateStack(handler), handler.getPreviewStack());
    }

    public static ItemStack identityStack(ItemStack stateStack, ItemStack previewStack) {
        return previewStack.isEmpty() ? stateStack : previewStack;
    }

    public static ItemStack stateStack(RunicForgeScreenHandler handler) {
        return handler.getForgeInventory().getStack(RunicForgeScreenHandler.WEAPON_SLOT);
    }
}
