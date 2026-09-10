package net.sweenus.simplyswordsmastery.mixin.client;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.sweenus.simplyswordsmastery.client.mastery.MasteryScreenSwitch;
import net.sweenus.simplyswordsmastery.client.mastery.ui.GlassButtonWidget;
import net.sweenus.simplyswordsmastery.client.mastery.theme.TooltipThemeSupport;
import net.sweenus.simplyswordsmastery.client.mastery.ui.MasteryPalette;
import net.sweenus.simplyswordsmastery.client.mastery.SimplySwordsMasteryScreen;
import net.sweenus.simplyswordsmastery.config.MasteryConfig;
import net.sweenus.simplyswordsmastery.mastery.RunicForgeMasteryContext;
import net.sweenus.simplyswordsmastery.mastery.definition.MasteryProfileRegistry;
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
    private ButtonWidget simplyswordsmastery$button;

    @Unique
    private MasteryPalette simplyswordsmastery$palette = MasteryPalette.DEFAULT;

    protected RunicForgeScreenMixin(RunicForgeScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void simplyswordsmastery$addButton(CallbackInfo callback) {
        int left = (width - backgroundWidth) / 2;
        int top = (height - backgroundHeight) / 2;
        int buttonWidth = Math.min(110, Math.max(78,
                textRenderer.getWidth(Text.translatable("screen.simplyswordsmastery.open")) + 12));
        int buttonX = left + backgroundWidth + 6;
        int buttonY = top;
        if (buttonX + buttonWidth > width - 4) {
            buttonX = Math.clamp(left + backgroundWidth - buttonWidth, 4, Math.max(4, width - buttonWidth - 4));
            buttonY = Math.max(4, top - 24);
        }
        simplyswordsmastery$button = addDrawableChild(new GlassButtonWidget(
                buttonX, buttonY, buttonWidth, 20,
                Text.translatable("screen.simplyswordsmastery.open"), button -> simplyswordsmastery$open(),
                () -> simplyswordsmastery$palette, theme -> theme.ACCENT));
        simplyswordsmastery$updateButton();
    }

    @Inject(method = "handledScreenTick", at = @At("TAIL"))
    private void simplyswordsmastery$tickButton(CallbackInfo callback) {
        simplyswordsmastery$updateButton();
    }

    @Unique
    private void simplyswordsmastery$refreshPalette() {
        if (!MasteryConfig.CLIENT.themedFromTooltips) {
            simplyswordsmastery$palette = MasteryPalette.DEFAULT;
            return;
        }
        simplyswordsmastery$palette = TooltipThemeSupport.resolve(RunicForgeMasteryContext.identityStack(handler))
                .map(TooltipThemeSupport.Resolved::palette)
                .orElse(MasteryPalette.DEFAULT);
    }

    @Unique
    private void simplyswordsmastery$updateButton() {
        if (simplyswordsmastery$button == null) {
            return;
        }
        simplyswordsmastery$refreshPalette();
        ItemStack stateStack = RunicForgeMasteryContext.stateStack(handler);
        boolean supported = MasteryProfileRegistry.resolveClient(
                RunicForgeMasteryContext.identityStack(handler)).isPresent();
        boolean cursorEmpty = handler.getCursorStack().isEmpty();
        boolean profileDisabled = MasteryProfileRegistry.resolveClient(
                RunicForgeMasteryContext.identityStack(handler))
                .map(profile -> MasteryConfig.SERVER.disabledProfiles.contains(profile.id())).orElse(false);
        simplyswordsmastery$button.active = MasteryConfig.SERVER.enabled && supported && cursorEmpty;
        Text tooltip = !MasteryConfig.SERVER.enabled
                ? Text.translatable("screen.simplyswordsmastery.open.disabled")
                : profileDisabled
                ? Text.translatable("screen.simplyswordsmastery.open.profile_disabled")
                : supported
                ? cursorEmpty
                ? Text.translatable("screen.simplyswordsmastery.open.tooltip")
                : Text.translatable("screen.simplyswordsmastery.open.cursor")
                : stateStack.isEmpty()
                ? Text.translatable("screen.simplyswordsmastery.open.empty")
                : Text.translatable("screen.simplyswordsmastery.open.unsupported");
        simplyswordsmastery$button.setTooltip(Tooltip.of(tooltip));
    }

    @Unique
    private void simplyswordsmastery$open() {
        if (client == null || client.player == null || !simplyswordsmastery$button.active) {
            return;
        }
        MasteryScreenSwitch.run(handler, () -> client.setScreen(
                new SimplySwordsMasteryScreen(handler, client.player.getInventory(), title)));
    }
}
