package net.sweenus.simplymastery.mixin.client;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.sweenus.simplymastery.client.mastery.MasteryScreenSwitch;
import net.sweenus.simplymastery.client.mastery.ui.GlassButtonWidget;
import net.sweenus.simplymastery.client.mastery.ui.MasteryTheme;
import net.sweenus.simplymastery.client.mastery.SimplyMasteryScreen;
import net.sweenus.simplymastery.config.MasteryConfig;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplymastery.mastery.definition.MasteryProfileRegistry;
import net.sweenus.simplyswords.client.screen.RunicForgeScreen;
import net.sweenus.simplyswords.screen.RunicForgeScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RunicForgeScreen.class)
public abstract class RunicForgeScreenMixin extends HandledScreen<RunicForgeScreenHandler> {

    @Unique
    private ButtonWidget simplymastery$button;

    protected RunicForgeScreenMixin(RunicForgeScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void simplymastery$addButton(CallbackInfo callback) {
        int left = (width - backgroundWidth) / 2;
        int top = (height - backgroundHeight) / 2;
        simplymastery$button = addDrawableChild(new GlassButtonWidget(
                left + backgroundWidth + 6, top, 78, 20,
                Text.translatable("screen.simplymastery.open"), button -> simplymastery$open(),
                MasteryTheme.ACCENT));
        simplymastery$updateButton();
    }

    @Inject(method = "handledScreenTick", at = @At("TAIL"))
    private void simplymastery$tickButton(CallbackInfo callback) {
        simplymastery$updateButton();
    }

    @Unique
    private void simplymastery$updateButton() {
        if (simplymastery$button == null) {
            return;
        }
        ItemStack stack = handler.getForgeInventory().getStack(RunicForgeScreenHandler.WEAPON_SLOT);
        boolean supported = MasteryProfileRegistry.resolveClient(stack).isPresent();
        boolean cursorEmpty = handler.getCursorStack().isEmpty();
        simplymastery$button.active = MasteryConfig.SERVER.enabled && supported && cursorEmpty;
        Text tooltip = !MasteryConfig.SERVER.enabled
                ? Text.translatable("screen.simplymastery.open.disabled")
                : supported
                ? cursorEmpty
                ? Text.translatable("screen.simplymastery.open.tooltip")
                : Text.translatable("screen.simplymastery.open.cursor")
                : stack.isEmpty()
                ? Text.translatable("screen.simplymastery.open.empty")
                : Text.translatable("screen.simplymastery.open.unsupported");
        simplymastery$button.setTooltip(Tooltip.of(tooltip));
    }

    @Unique
    private void simplymastery$open() {
        if (client == null || client.player == null || !simplymastery$button.active) {
            return;
        }
        MasteryScreenSwitch.run(handler, () -> client.setScreen(
                new SimplyMasteryScreen(handler, client.player.getInventory(), title)));
    }
}
