package net.sweenus.simplymastery.client.mastery;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.sweenus.simplymastery.client.mastery.ui.Anim;
import net.sweenus.simplymastery.client.mastery.ui.FrameClock;
import net.sweenus.simplymastery.client.mastery.ui.GlassButtonWidget;
import net.sweenus.simplymastery.client.mastery.ui.MasteryChrome;
import net.sweenus.simplymastery.client.mastery.ui.MasteryPalette;
import net.sweenus.simplymastery.client.mastery.ui.MasteryPaletteSource;
import net.sweenus.simplymastery.client.mastery.ui.MasteryShowcase;
import net.sweenus.simplymastery.client.mastery.ui.MasteryTheme;
import net.sweenus.simplymastery.client.mastery.ui.MasteryUiPolicy;
import net.sweenus.simplymastery.client.mastery.ui.MasteryUiSounds;
import net.sweenus.simplymastery.client.mastery.ui.UiDraw;
import net.sweenus.simplymastery.config.MasteryConfig;
import net.sweenus.simplymastery.mastery.RunicForgeMasteryContext;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplymastery.mastery.definition.MasteryProfileRegistry;
import net.sweenus.simplymastery.mastery.network.MasteryNetwork;
import net.sweenus.simplymastery.mastery.network.RewardActionPacket;
import net.sweenus.simplymastery.mastery.reward.ForgeRewards;
import net.sweenus.simplymastery.mastery.state.MasteryState;
import net.sweenus.simplyswords.client.screen.RunicForgeScreen;
import net.sweenus.simplyswords.screen.RunicForgeScreenHandler;

import java.util.Locale;
import java.util.function.ToIntFunction;

public final class MasteryRewardsScreen extends Screen {

    private static final float MICRO_SCALE = MasteryChrome.MICRO_SCALE;
    private static final int XP_SEGMENTS = 16;
    private static final int STRIP_HEIGHT = 33;

    private final RunicForgeScreenHandler handler;
    private final Screen parent;
    private final FrameClock clock = new FrameClock();
    private final MasteryPaletteSource paletteSource = new MasteryPaletteSource();
    private final Anim pointsMeter = new Anim(0.0F, 13.0F);
    private final Anim xpMeter = new Anim(0.0F, 13.0F);
    private final Anim masteryMeter = new Anim(0.0F, 13.0F);

    private NbtCompound display;
    private long actionId;
    private long pendingActionId = -1L;
    private long consumedActionId = -1L;
    private int refreshTicks;
    private int page;
    private int pages = 1;

    private MasteryChrome.Frame frame;
    private MasteryPalette palette = MasteryPalette.DEFAULT;
    private MasteryProfile profile;
    private int accentRgb = MasteryPalette.DEFAULT.ACCENT;
    private GlassButtonWidget backButton;
    private GlassButtonWidget closeButton;
    private GlassButtonWidget prevButton;
    private GlassButtonWidget nextButton;

    private int rowTop;
    private int rowHeight;
    private int cardHeight;
    private boolean dense;
    private int hoveredRow = -1;
    private Anim[] rowHover = new Anim[0];
    private int itemHoverLeft;
    private int itemHoverTop;
    private int itemHoverRight;
    private int itemHoverBottom;

    private float entranceSeconds;
    private float feedbackSeconds;
    private String feedback = "";
    private String signature = "";
    private boolean opened;

    public MasteryRewardsScreen(RunicForgeScreenHandler handler, Screen parent) {
        super(Text.translatable("screen.simplymastery.rewards"));
        this.handler = handler;
        this.parent = parent;
    }

    @Override
    protected void init() {
        frame = MasteryChrome.frame(width, height);
        refreshTheme();
        measureRows();
        signature = "";
        rebuild();
        request("");
        if (!opened) {
            opened = true;
            MasteryUiSounds.openView();
        }
    }

    public void install(NbtCompound value) {
        if (value.getInt("protocol") != MasteryNetwork.PROTOCOL_VERSION
                || value.getInt("screen") != handler.syncId) {
            return;
        }
        display = value.copy();
        pages = Math.max(1, value.getInt("pages"));
        page = Math.clamp(value.getInt("page"), 0, pages - 1);
        String message = value.getString("message");
        if (!message.isEmpty() && pendingActionId != consumedActionId) {
            consumedActionId = pendingActionId;
            feedback = message;
            feedbackSeconds = MasteryChrome.FEEDBACK_SECONDS;
            if (message.equals("success")) {
                MasteryUiSounds.unlock();
            } else {
                MasteryUiSounds.denied();
            }
        }
        if (frame != null) {
            rebuild();
        }
    }

    private void measureRows() {
        rowTop = frame.canvasTop() + 5 + STRIP_HEIGHT;
        int available = frame.bodyBottom() - 6 - rowTop;
        rowHeight = Math.clamp(available / ForgeRewards.PAGE_SIZE, 16, 40);
        cardHeight = rowHeight - 3;
        dense = cardHeight < 26;
    }

    private int visibleRows(int rows) {
        int fits = Math.max(0, (frame.bodyBottom() - 6 - rowTop) / Math.max(1, rowHeight));
        return Math.min(Math.min(rows, ForgeRewards.PAGE_SIZE), fits);
    }

    private String signature(NbtList rows) {
        StringBuilder builder = new StringBuilder().append(page).append('/').append(pages)
                .append('@').append(rowHeight).append('#').append(frame.canvasRight())
                .append('^').append(frame.bodyBottom());
        for (int i = 0; i < rows.size(); i++) {
            NbtCompound row = rows.getCompound(i);
            builder.append('|').append(row.getString("id")).append(':').append(row.getString("reason"));
        }
        return builder.toString();
    }

    private void rebuild() {
        NbtList rows = display == null ? new NbtList() : display.getList("rows", NbtElement.COMPOUND_TYPE);
        String current = signature(rows);
        if (current.equals(signature) && backButton != null) {
            return;
        }
        signature = current;
        clearChildren();

        int buttonHeight = MasteryChrome.BUTTON_HEIGHT;
        int buttonY = frame.buttonY();
        ToIntFunction<MasteryPalette> accent = theme -> accentRgb();

        backButton = addDrawableChild(new GlassButtonWidget(frame.backButtonX(), buttonY,
                frame.backButtonWidth(), buttonHeight, Text.translatable("screen.simplymastery.back"),
                button -> close(), this::palette, accent));
        prevButton = addDrawableChild(new GlassButtonWidget(frame.rewardsButtonX(), buttonY, 18, buttonHeight,
                Text.literal("<"), button -> turnTo(page - 1), this::palette, accent));
        nextButton = addDrawableChild(new GlassButtonWidget(
                frame.respecButtonX() + frame.respecButtonWidth() - 18, buttonY, 18, buttonHeight,
                Text.literal(">"), button -> turnTo(page + 1), this::palette, accent));
        closeButton = addDrawableChild(new GlassButtonWidget(frame.closeButtonX(), buttonY,
                frame.closeButtonWidth(), buttonHeight, Text.translatable("screen.simplymastery.close"),
                button -> closeAll(), this::palette, accent, true));
        prevButton.setTooltip(Tooltip.of(Text.translatable("screen.simplymastery.rewards.prev")));
        nextButton.setTooltip(Tooltip.of(Text.translatable("screen.simplymastery.rewards.next")));
        applyPagerState();

        int visible = visibleRows(rows.size());
        rowHover = new Anim[visible];
        int claimWidth = Math.clamp((frame.canvasRight() - frame.canvasLeft()) / 5, 40, 66);
        int claimHeight = Math.min(16, Math.max(10, cardHeight - 6));
        for (int i = 0; i < visible; i++) {
            NbtCompound row = rows.getCompound(i);
            rowHover[i] = new Anim(0.0F, 24.0F);
            boolean claimable = row.getString("reason").isEmpty();
            String id = row.getString("id");
            int cardY = rowTop + i * rowHeight;
            GlassButtonWidget claim = addDrawableChild(new GlassButtonWidget(
                    frame.canvasRight() - 8 - claimWidth, cardY + (cardHeight - claimHeight) / 2,
                    claimWidth, claimHeight, Text.translatable("screen.simplymastery.rewards.claim"),
                    button -> claim(id), this::palette, accent, claimable));
            claim.active = claimable;
        }
    }

    private void applyPagerState() {
        if (prevButton == null) {
            return;
        }
        prevButton.visible = pages > 1;
        nextButton.visible = pages > 1;
        prevButton.active = page > 0;
        nextButton.active = page + 1 < pages;
    }

    private void turnTo(int target) {
        int bounded = Math.clamp(target, 0, pages - 1);
        if (bounded == page) {
            return;
        }
        page = bounded;
        applyPagerState();
        MasteryUiSounds.select();
        request("");
    }

    private void claim(String id) {
        MasteryUiSounds.select();
        request(id);
    }

    private void request(String action) {
        NbtCompound request = display == null ? new NbtCompound() : display.copy();
        request.remove("rows");
        request.putInt("protocol", MasteryNetwork.PROTOCOL_VERSION);
        request.putInt("screen", handler.syncId);
        request.putInt("page", page);
        request.putString("action", action);
        if (!action.isEmpty()) {
            pendingActionId = ++actionId;
            request.putLong("action_id", pendingActionId);
        }
        new RewardActionPacket(request).sendToServer();
    }

    @Override
    public void tick() {
        if (client == null || client.player == null) {
            return;
        }
        if (client.player.currentScreenHandler != handler || !handler.canUse(client.player)) {
            client.player.closeHandledScreen();
            return;
        }
        refreshTheme();
        if (++refreshTicks >= 20) {
            refreshTicks = 0;
            request("");
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean instant = MasteryConfig.CLIENT.reducedMotion;
        float step = clock.advance();
        entranceSeconds = instant ? 1.2F : Math.min(1.2F, entranceSeconds + step);
        feedbackSeconds = Math.max(0.0F, feedbackSeconds - step);
        if (feedbackSeconds <= 0.0F) {
            feedback = "";
        }

        float appear = MasteryTheme.easeOutCubic(entranceSeconds / 0.16F);
        float panels = MasteryTheme.easeOutCubic((entranceSeconds - 0.04F) / 0.18F);
        int accent = accentRgb();
        itemHoverRight = itemHoverLeft;

        MasteryChrome.background(context, palette, width, height, accent, seconds(), motion(), appear);
        MasteryChrome.headerPanel(context, palette, frame, accent, panels);
        MasteryChrome.bodyPanel(context, palette, frame.canvasLeft(), frame.canvasTop(), frame.canvasRight(),
                frame.bodyBottom(), accent, panels, true);
        MasteryChrome.showcasePanel(context, palette, frame, accent, panels);

        drawHeader(context, accent);
        drawBody(context, accent, mouseX, mouseY, step, instant);
        drawShowcase(context, accent, step, instant);
        super.render(context, mouseX, mouseY, delta);
        drawStatus(context, accent);
        drawItemTooltip(context, mouseX, mouseY);
    }

    private void drawHeader(DrawContext context, int accent) {
        float alpha = MasteryTheme.easeOutCubic((entranceSeconds - 0.06F) / 0.16F);
        if (alpha <= 0.01F) {
            return;
        }
        ItemStack stack = identityStack();
        int textLeft = frame.headerContentLeft();
        int ruleTop = frame.headerTop() + 2;
        int ruleBottom = frame.headerBottom() - 2;
        MasteryChrome.vRule(context, palette, textLeft - 7, ruleTop, ruleBottom, alpha);

        boolean roomy = frame.roomy();
        float nameScale = roomy ? 1.35F : 1.0F;
        int titleTop = frame.headerTop() + (frame.headerHeight() - 17) / 2;
        float nameY = roomy ? titleTop + 6 : frame.headerTop() + (frame.headerHeight() - 8) * 0.5F;
        if (roomy) {
            MasteryChrome.microLabel(context, textRenderer,
                    Text.translatable("screen.simplymastery.rewards.kicker"), textLeft, titleTop,
                    palette.INK_MUTED, alpha);
        }
        Text name = UiDraw.fit(textRenderer, stack.getName(),
                Math.round((frame.headerContentRight() - textLeft) / nameScale));
        UiDraw.text(context, textRenderer, name, textLeft, nameY,
                MasteryTheme.argb(palette.DISPLAY, alpha), nameScale, true);

        if (pages > 1) {
            int cellLeft = frame.rewardsButtonX() + 18;
            int cellRight = frame.respecButtonX() + frame.respecButtonWidth() - 18;
            UiDraw.centeredText(context, textRenderer,
                    Text.translatable("screen.simplymastery.rewards.page", page + 1, pages),
                    (cellLeft + cellRight) * 0.5F, frame.headerTop() + (frame.headerHeight() - 8) * 0.5F,
                    MasteryTheme.argb(palette.INK, alpha), 1.0F, false);
        }
    }

    private void drawBody(DrawContext context, int accent, int mouseX, int mouseY, float step, boolean instant) {
        float alpha = MasteryTheme.easeOutCubic((entranceSeconds - 0.07F) / 0.18F);
        if (alpha <= 0.01F || display == null) {
            return;
        }
        int contentLeft = frame.canvasLeft() + 8;
        int contentRight = frame.canvasRight() - 8;
        int points = display.getInt("points");
        int cap = Math.max(1, display.getInt("cap"));
        double xp = display.getDouble("xp");
        long next = display.getLong("next");
        boolean capped = points >= display.getInt("cap") || next <= 0;

        pointsMeter.target(Math.min(1.0F, points / (float) cap));
        xpMeter.target(capped ? 1.0F : Math.min(1.0F, (float) (xp / next)));
        pointsMeter.advance(step, instant);
        xpMeter.advance(step, instant);

        int stripY = frame.canvasTop() + 5;
        MasteryChrome.microLabel(context, textRenderer,
                Text.translatable("screen.simplymastery.rewards.points"), contentLeft, stripY,
                palette.INK_MUTED, alpha);
        UiDraw.text(context, textRenderer, Text.literal(points + " / " + display.getInt("cap")),
                contentLeft + Math.round(textRenderer.getWidth(
                        Text.translatable("screen.simplymastery.rewards.points")) * MICRO_SCALE) + 6,
                stripY - 1, MasteryTheme.argb(palette.INK, alpha), 1.0F, false);
        UiDraw.rightText(context, textRenderer,
                Text.translatable("screen.simplymastery.ownership."
                        + (display.getString("mode").equals("PLAYER") ? "player" : "weapon")),
                contentRight, stripY, MasteryTheme.argb(palette.INK_MUTED, alpha), MICRO_SCALE, false);
        UiDraw.segmentStrip(context, palette, contentLeft, stripY + 10, contentRight - contentLeft, 3,
                cap, pointsMeter.value(), 1, accent, alpha);

        int xpY = stripY + 17;
        MasteryChrome.microLabel(context, textRenderer,
                Text.translatable("screen.simplymastery.rewards.xp"), contentLeft, xpY,
                palette.INK_MUTED, alpha);
        Text xpValue = capped
                ? Text.translatable("screen.simplymastery.progression.capped")
                : Text.literal(String.format(Locale.ROOT, "%.2f", xp) + " / " + next);
        UiDraw.rightText(context, textRenderer, xpValue, contentRight, xpY,
                MasteryTheme.argb(capped ? accent : palette.INK_MUTED, alpha), MICRO_SCALE, false);
        UiDraw.segmentStrip(context, palette, contentLeft, xpY + 8, contentRight - contentLeft, 3,
                XP_SEGMENTS, xpMeter.value(), 1, capped ? accent : palette.INK_DIM, alpha);
        context.fill(frame.canvasLeft() + 1, xpY + 15, frame.canvasRight() - 1, xpY + 16,
                MasteryTheme.argb(palette.RULE, alpha));

        NbtList rows = display.getList("rows", NbtElement.COMPOUND_TYPE);
        if (rows.isEmpty()) {
            drawEmptyState(context, alpha);
            return;
        }

        int visible = Math.min(rowHover.length, rows.size());
        int previousHover = hoveredRow;
        hoveredRow = -1;
        for (int i = 0; i < visible; i++) {
            int cardY = rowTop + i * rowHeight;
            if (mouseX >= frame.canvasLeft() + 4 && mouseX < frame.canvasRight() - 4
                    && mouseY >= cardY && mouseY < cardY + cardHeight) {
                hoveredRow = i;
            }
        }
        if (hoveredRow != previousHover && hoveredRow >= 0) {
            MasteryUiSounds.hover();
        }

        for (int i = 0; i < visible; i++) {
            rowHover[i].target(i == hoveredRow ? 1.0F : 0.0F);
            rowHover[i].advance(step, instant);
            drawRow(context, accent, rows.getCompound(i), i, alpha);
        }
    }

    private void drawRow(DrawContext context, int accent, NbtCompound row, int index, float bodyAlpha) {
        float stagger = MasteryTheme.easeOutCubic((entranceSeconds - 0.08F - index * 0.02F) / 0.18F);
        float alpha = Math.min(bodyAlpha, stagger);
        if (alpha <= 0.01F) {
            return;
        }
        boolean claimable = row.getString("reason").isEmpty();
        int rgb = claimable ? accent : palette.INK_FAINT;
        int x0 = frame.canvasLeft() + 4;
        int x1 = frame.canvasRight() - 4;
        int y0 = rowTop + index * rowHeight;
        int y1 = y0 + cardHeight;
        float lit = rowHover[index].value();

        MasteryChrome.card(context, palette, x0, y0, x1, y1, rgb, alpha);
        if (lit > 0.01F) {
            context.fill(x0 + 1, y0 + 1, x1 - 1, y1 - 1,
                    MasteryTheme.argb(palette.HOVER_FILL, 0.5F * lit * alpha));
        }

        int textX = x0 + 6;
        int claimWidth = Math.clamp((frame.canvasRight() - frame.canvasLeft()) / 5, 40, 66);
        int textRight = x1 - 8 - claimWidth;
        int labelWidth = Math.max(20, textRight - textX - 6);
        Text amount = row.getInt("points") > 0
                ? Text.translatable("screen.simplymastery.rewards.amount.points", row.getInt("points"))
                : Text.translatable("screen.simplymastery.rewards.amount.xp", row.getInt("xp"));
        Text detail = claimable
                ? Text.literal(row.getString("cost"))
                : Text.translatable("screen.simplymastery.rewards." + row.getString("reason"));
        int detailRgb = claimable ? palette.INK_MUTED : palette.ACCENT_HOT;

        if (dense) {
            int detailWidth = Math.max(16, Math.round(labelWidth * 0.4F));
            UiDraw.text(context, textRenderer,
                    UiDraw.fit(textRenderer, Text.literal(row.getString("label")),
                            labelWidth - detailWidth - 6),
                    textX, y0 + (cardHeight - 8) * 0.5F, MasteryTheme.argb(palette.DISPLAY, alpha), 1.0F, true);
            UiDraw.rightText(context, textRenderer,
                    UiDraw.fit(textRenderer, detail, Math.round(detailWidth / MICRO_SCALE)),
                    textRight, y0 + (cardHeight - 5) * 0.5F, MasteryTheme.argb(detailRgb, alpha),
                    MICRO_SCALE, false);
            return;
        }

        UiDraw.text(context, textRenderer,
                UiDraw.fit(textRenderer, Text.literal(row.getString("label")), labelWidth),
                textX, y0 + 5, MasteryTheme.argb(palette.DISPLAY, alpha), 1.0F, true);
        int amountRight = textX;
        MasteryChrome.microLabel(context, textRenderer, amount, amountRight, y0 + 17, rgb, alpha);
        amountRight += Math.round(textRenderer.getWidth(amount) * MICRO_SCALE) + 5;
        if (row.getInt("points") > 0) {
            UiDraw.pips(context, palette, amountRight, y0 + 17, row.getInt("points"),
                    row.getInt("points"), 3, 2, rgb, alpha);
            amountRight += row.getInt("points") * 5 + 5;
        }
        UiDraw.text(context, textRenderer,
                UiDraw.fit(textRenderer, detail, Math.max(10, textRight - amountRight)),
                amountRight, y0 + 17, MasteryTheme.argb(detailRgb, alpha), MICRO_SCALE, false);
    }

    private void drawEmptyState(DrawContext context, float alpha) {
        int wellLeft = frame.canvasLeft() + 12;
        int wellRight = frame.canvasRight() - 12;
        int wellTop = rowTop + 6;
        int wellBottom = frame.bodyBottom() - 12;
        if (wellBottom - wellTop < 24) {
            return;
        }
        context.fill(wellLeft, wellTop, wellRight, wellBottom,
                MasteryTheme.argb(palette.WELL, 0.95F * alpha));
        UiDraw.hatch45(context, wellLeft, wellTop, wellRight, wellBottom, 6,
                MasteryTheme.argb(palette.TEXTURE_INK, 0.02F * alpha));
        UiDraw.cornerBrackets(context, wellLeft + 3, wellTop + 3, wellRight - 3, wellBottom - 3, 5, 1,
                MasteryTheme.argb(palette.BRACKET, 0.9F * alpha));
        float centerX = (wellLeft + wellRight) * 0.5F;
        float centerY = (wellTop + wellBottom) * 0.5F;
        UiDraw.centeredText(context, textRenderer,
                UiDraw.fit(textRenderer, Text.translatable("screen.simplymastery.rewards.empty"),
                        wellRight - wellLeft - 16),
                centerX, centerY - 8, MasteryTheme.argb(palette.INK_MUTED, alpha), 1.0F, false);
        UiDraw.Fitted hint = UiDraw.fitBlock(textRenderer,
                Text.translatable("screen.simplymastery.rewards.empty.hint"),
                Math.round((wellRight - wellLeft - 16) / MICRO_SCALE), 2, MICRO_SCALE);
        for (int i = 0; i < hint.lines().size(); i++) {
            UiDraw.text(context, textRenderer, hint.lines().get(i),
                    centerX - textRenderer.getWidth(hint.lines().get(i)) * MICRO_SCALE * 0.5F,
                    centerY + 4 + i * 6, MasteryTheme.argb(palette.INK_FAINT, alpha), MICRO_SCALE, false);
        }
    }

    private void drawShowcase(DrawContext context, int accent, float step, boolean instant) {
        if (frame.compact() || profile == null) {
            return;
        }
        float appear = MasteryTheme.easeOutCubic((entranceSeconds - 0.09F) / 0.19F);
        if (appear <= 0.01F) {
            return;
        }
        MasteryState state = state();
        if (state == null) {
            return;
        }
        int total = MasteryUiPolicy.maxInvestablePoints(profile,
                MasteryProfileRegistry.client().policy(profile.progressionGroupId()).pointCap());
        masteryMeter.target(total <= 0 ? 0.0F : Math.min(1.0F, state.spentPoints(profile) / (float) total));
        masteryMeter.advance(step, instant);
        MasteryShowcase.Hover hover = MasteryShowcase.draw(context, textRenderer, palette, accent,
                frame.showcaseLeft(), frame.showcaseTop(), frame.showcaseRight(), frame.showcaseBottom(),
                identityStack(), Text.translatable("profile.simplymastery." + profile.id().getPath()),
                Text.literal(state.spentPoints(profile) + " / " + total),
                Text.translatable("screen.simplymastery.header.mastery"),
                total, masteryMeter.value(), seconds(), motion(), appear);
        itemHoverLeft = hover.left();
        itemHoverTop = hover.top();
        itemHoverRight = hover.right();
        itemHoverBottom = hover.bottom();
    }

    private void drawStatus(DrawContext context, int accent) {
        float appear = MasteryTheme.easeOutCubic((entranceSeconds - 0.15F) / 0.16F);
        if (appear <= 0.01F) {
            return;
        }
        MasteryChrome.statusRule(context, palette, frame, appear);
        if (!feedback.isEmpty() && MasteryChrome.feedback(context, textRenderer, palette, frame,
                Text.translatable("screen.simplymastery.rewards." + feedback),
                feedback.equals("success") ? accent : palette.ACCENT_HOT, feedbackSeconds, motion())) {
            return;
        }

        int y = frame.statusTop() + 4;
        int x = frame.statusLeft() + 2;
        if (!frame.compact()) {
            x = MasteryChrome.keyHint(context, textRenderer, palette, x, y, "lmb", "claim", appear);
        }
        MasteryChrome.keyHint(context, textRenderer, palette, x, y, "esc", "close", appear);

        int available = availablePoints();
        Text unspent = Text.translatable("screen.simplymastery.unspent", available);
        UiDraw.rightText(context, textRenderer,
                UiDraw.fit(textRenderer, unspent, frame.statusRight() - frame.statusLeft()),
                frame.statusRight() - 2, y + 2,
                MasteryTheme.argb(available > 0 ? accent : palette.INK_MUTED, 0.9F * appear), 1.0F, false);
    }

    private void drawItemTooltip(DrawContext context, int mouseX, int mouseY) {
        if (client == null || itemHoverRight <= itemHoverLeft
                || mouseX < itemHoverLeft || mouseX >= itemHoverRight
                || mouseY < itemHoverTop || mouseY >= itemHoverBottom) {
            return;
        }
        ItemStack stack = identityStack();
        if (stack.isEmpty()) {
            return;
        }
        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0.0F, 0.0F, 500.0F);
        context.drawTooltip(textRenderer, getTooltipFromItem(client, stack), mouseX, mouseY);
        matrices.pop();
    }

    private void refreshTheme() {
        ItemStack stack = identityStack();
        paletteSource.refresh(stack);
        palette = paletteSource.palette();
        profile = MasteryProfileRegistry.resolveClient(stack).orElse(null);
        accentRgb = MasteryChrome.accent(palette, profile);
    }

    private MasteryPalette palette() {
        return palette;
    }

    private int accentRgb() {
        return accentRgb;
    }

    private MasteryState state() {
        if (profile == null) {
            return null;
        }
        return PersonalMasteryView.read(RunicForgeMasteryContext.stateStack(handler), profile,
                Math.min(MasteryConfig.SERVER.verticalSliceStartingPoints,
                        MasteryProfileRegistry.client().policy(profile.progressionGroupId()).pointCap()));
    }

    private int availablePoints() {
        MasteryState state = state();
        if (state != null) {
            return state.availablePoints(profile);
        }
        return display == null ? 0 : display.getInt("points");
    }

    private ItemStack identityStack() {
        return RunicForgeMasteryContext.identityStack(handler);
    }

    private static double seconds() {
        return Util.getMeasuringTimeMs() / 1000.0;
    }

    private static float motion() {
        return MasteryConfig.CLIENT.reducedMotion ? 0.0F : MasteryConfig.CLIENT.motionIntensity;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void closeAll() {
        if (client != null && client.player != null) {
            client.player.closeHandledScreen();
        }
    }

    @Override
    public void close() {
        if (client == null || client.player == null) {
            return;
        }
        MasteryScreenSwitch.run(handler, () -> {
            if (MasteryProfileRegistry.resolveClient(identityStack()).isPresent()) {
                client.setScreen(parent);
            } else {
                client.setScreen(new RunicForgeScreen(handler,
                        client.player.getInventory(), Text.translatable("screen.simplymastery.back")));
            }
        });
    }
}
