package net.sweenus.simplymastery.client.mastery;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.client.mastery.ui.Anim;
import net.sweenus.simplymastery.client.mastery.ui.GlassButtonWidget;
import net.sweenus.simplymastery.client.mastery.ui.MasteryLayout;
import net.sweenus.simplymastery.client.mastery.ui.MasteryNodeState;
import net.sweenus.simplymastery.client.mastery.ui.MasteryPrompt;
import net.sweenus.simplymastery.client.mastery.ui.MasteryTheme;
import net.sweenus.simplymastery.client.mastery.ui.MasteryUiPolicy;
import net.sweenus.simplymastery.client.mastery.ui.MasteryUiSounds;
import net.sweenus.simplymastery.client.mastery.ui.NodeAnimators;
import net.sweenus.simplymastery.client.mastery.ui.UiDraw;
import net.sweenus.simplymastery.config.MasteryConfig;
import net.sweenus.simplymastery.mastery.RunicForgeMasteryContext;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplymastery.mastery.definition.MasteryProfileRegistry;
import net.sweenus.simplymastery.mastery.network.RequestProfileSyncPacket;
import net.sweenus.simplymastery.mastery.network.UnlockNodePacket;
import net.sweenus.simplymastery.mastery.network.UnlockResult;
import net.sweenus.simplymastery.mastery.state.MasteryState;
import net.sweenus.simplymastery.mastery.state.MasteryStateAccess;
import net.sweenus.simplyswords.client.screen.RunicForgeScreen;
import net.sweenus.simplyswords.screen.RunicForgeScreenHandler;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public final class SimplyMasteryScreen extends HandledScreen<RunicForgeScreenHandler> {

    private static final AtomicLong ACTION_IDS = new AtomicLong();
    private static final float FEEDBACK_SECONDS = 3.2F;
    private static final int CARD_LINES = 8;
    private static final int FRAME_INSET = 2;
    private static final int GRID_SPACING = 14;
    private static final float MICRO_SCALE = 0.5F;

    private final PlayerInventory playerInventory;
    private final Anim masteryMeter = new Anim(0.0F, 13.0F);
    private final Anim cardFade = new Anim(0.0F, 30.0F);
    private final Anim promptFade = new Anim(0.0F, 28.0F);

    private MasteryLayout layout;
    private NodeAnimators animators;
    private int[] branchRgb = new int[0];
    private Identifier[] nodeIcons = new Identifier[0];
    private boolean[] ownedSnapshot;
    private int hoveredNode = -1;
    private int selectedNode = -1;
    private int cardNode = -1;
    private long pendingActionId = -1L;
    private long feedbackActionId = -1L;
    private float feedbackTimer;
    private float entranceSeconds;
    private float flash;
    private int itemHoverLeft;
    private int itemHoverTop;
    private int itemHoverRight;
    private int itemHoverBottom;
    private GlassButtonWidget backButton;
    private GlassButtonWidget respecButton;
    private GlassButtonWidget closeButton;
    private GlassButtonWidget promptConfirm;
    private GlassButtonWidget promptCancel;
    private MasteryPrompt prompt;
    private List<OrderedText> promptLines = List.of();
    private int promptX;
    private int promptY;
    private int promptWidth;
    private int promptHeight;
    private float canvasZoom = 1.0F;
    private float canvasPanX;
    private float canvasPanY;
    private long lastFrameMs = Util.getMeasuringTimeMs();
    private boolean switchingView;
    private MasteryProfile profile;
    private int definitionEpoch = -1;

    public SimplyMasteryScreen(RunicForgeScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title);
        this.playerInventory = playerInventory;
    }

    @Override
    protected void init() {
        super.init();
        MasteryProfileRegistry.Snapshot snapshot = MasteryProfileRegistry.client();
        profile = snapshot.resolve(identityStack()).map(MasteryProfileRegistry.Resolution::profile)
                .orElseThrow(() -> new IllegalStateException("Mastery screen opened without a synced profile"));
        definitionEpoch = snapshot.epoch();
        rebuildProfile(profile);
        new RequestProfileSyncPacket(handler.syncId).sendToServer();

        int buttonHeight = 18;
        int buttonY = layout.headerTop + (layout.headerBottom - layout.headerTop - buttonHeight) / 2;
        int accent = accentRgb();
        backButton = addDrawableChild(new GlassButtonWidget(layout.backButtonX, buttonY,
                layout.backButtonWidth, buttonHeight, Text.translatable("screen.simplymastery.back"),
                button -> backToForge(), accent));
        respecButton = addDrawableChild(new GlassButtonWidget(layout.respecButtonX, buttonY,
                layout.respecButtonWidth, buttonHeight, Text.translatable("screen.simplymastery.respec"),
                button -> confirmRespec(), accent));
        closeButton = addDrawableChild(new GlassButtonWidget(layout.closeButtonX, buttonY,
                layout.closeButtonWidth, buttonHeight, Text.translatable("screen.simplymastery.close"),
                button -> close(), accent, true));

        // The prompt's buttons take clicks and focus but are drawn by hand, above its panel.
        promptCancel = addSelectableChild(new GlassButtonWidget(0, 0, 60, buttonHeight,
                Text.translatable("screen.simplymastery.prompt.cancel"),
                button -> closePrompt(), accent));
        promptConfirm = addSelectableChild(new GlassButtonWidget(0, 0, 60, buttonHeight,
                Text.translatable("screen.simplymastery.prompt.confirm"),
                button -> acceptPrompt(), accent, true));
        if (prompt != null) {
            layoutPrompt();
        }
        applyPromptGating();
    }

    @Override
    protected void handledScreenTick() {
        if (client == null || client.player == null) {
            return;
        }
        if (!handler.canUse(client.player)) {
            close();
            return;
        }
        MasteryProfileRegistry.Snapshot snapshot = MasteryProfileRegistry.client();
        if (snapshot.epoch() != definitionEpoch) {
            MasteryProfile replacement = snapshot.resolve(identityStack())
                    .map(MasteryProfileRegistry.Resolution::profile).orElse(null);
            if (replacement == null) {
                backToForge();
                return;
            }
            definitionEpoch = snapshot.epoch();
            pendingActionId = -1L;
            profile = replacement;
            // The node a prompt refers to may not exist in the replacement definition.
            closePrompt();
            rebuildProfile(replacement);
        } else if (snapshot.resolve(identityStack()).isEmpty()) {
            backToForge();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MasteryState state = state(profile);
        boolean instant = MasteryConfig.CLIENT.reducedMotion;
        float step = frameSeconds();
        entranceSeconds = instant ? 1.2F : Math.min(1.2F, entranceSeconds + step);
        flash = Math.max(0.0F, flash - step / 0.28F);
        feedbackTimer = Math.max(0.0F, feedbackTimer - step);

        int previousHover = hoveredNode;
        hoveredNode = prompt == null && insideCanvas(mouseX, mouseY)
                ? layout.nodeAt(toLayoutX(mouseX), toLayoutY(mouseY)) : -1;
        if (hoveredNode != previousHover && hoveredNode >= 0) {
            MasteryUiSounds.hover();
        }

        syncOwnership(profile, state);
        consumeFeedback(profile);
        for (int i = 0; i < layout.nodeCount(); i++) {
            animators.setTargets(i, i == hoveredNode, i == selectedNode,
                    state.owns(profile.nodes().get(i).id()));
        }
        animators.advance(step, instant);
        masteryMeter.target(ownedFraction(profile, state));
        masteryMeter.advance(step, instant);
        cardFade.target(hoveredNode >= 0 || selectedNode >= 0 ? 1.0F : 0.0F);
        cardFade.advance(step, instant);
        promptFade.advance(step, instant);
        if (hoveredNode >= 0) {
            cardNode = hoveredNode;
        } else if (selectedNode >= 0) {
            cardNode = selectedNode;
        }

        itemHoverRight = itemHoverLeft;
        drawBackground(context, delta, mouseX, mouseY);
        drawCanvas(context, profile, state);
        if (layout.compact) {
            drawCompactBadge(context);
        } else {
            drawShowcase(context, profile, state);
        }
        drawHeader(context, profile, state);
        for (Element child : children()) {
            if (child instanceof Drawable drawable && child != promptConfirm && child != promptCancel) {
                drawable.render(context, mouseX, mouseY, delta);
            }
        }

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0.0F, 0.0F, 420.0F);
        if (prompt == null) {
            drawDetailCard(context, profile, state, mouseX, mouseY);
        }
        drawStatus(context);
        if (flash > 0.0F) {
            context.fill(0, 0, width, height,
                    MasteryTheme.argb(accentRgb(), 0.10F * flash * flash * motion()));
        }
        drawPrompt(context, mouseX, mouseY, delta);
        matrices.pop();
        if (prompt == null) {
            drawItemTooltip(context, mouseX, mouseY);
        }
    }

    /** The weapon on show carries its own item tooltip, as it would in any inventory. */
    private void drawItemTooltip(DrawContext context, int mouseX, int mouseY) {
        if (client == null || itemHoverRight <= itemHoverLeft
                || mouseX < itemHoverLeft || mouseX >= itemHoverRight
                || mouseY < itemHoverTop || mouseY >= itemHoverBottom) {
            return;
        }
        ItemStack stack = displayStack();
        if (stack.isEmpty()) {
            return;
        }
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0.0F, 0.0F, 500.0F);
        context.drawTooltip(textRenderer, getTooltipFromItem(client, stack), mouseX, mouseY);
        matrices.pop();
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        float appear = MasteryTheme.easeOutCubic(entranceSeconds / 0.16F);
        float dim = MasteryConfig.CLIENT.backgroundDim;
        int accent = accentRgb();
        context.fill(0, 0, width, height,
                MasteryTheme.argb(MasteryTheme.GROUND_DEEP, Math.min(0.97F, dim + 0.2F) * appear));
        context.fill(FRAME_INSET, FRAME_INSET, width - FRAME_INSET, height - FRAME_INSET,
                MasteryTheme.argb(MasteryTheme.GROUND, dim * appear));
        UiDraw.hatch45(context, FRAME_INSET, FRAME_INSET, width - FRAME_INSET, height - FRAME_INSET, 4,
                MasteryTheme.argb(0xFFFFFF, 0.014F * appear));
        UiDraw.motes(context, width, height, seconds(), 48, accent, 0.9F * motion() * appear);
        UiDraw.vignette(context, width, height, MasteryTheme.GROUND_DEEP, 0.5F * appear);

        UiDraw.boxOutline(context, FRAME_INSET - 1, FRAME_INSET - 1, width - FRAME_INSET + 1,
                height - FRAME_INSET + 1, 1, MasteryTheme.argb(MasteryTheme.FRAME_OUTER, 0.95F * appear));
        UiDraw.boxOutline(context, 0, 0, width, height, 1,
                MasteryTheme.argb(MasteryTheme.FRAME_INNER, 0.95F * appear));
        UiDraw.cornerBrackets(context, FRAME_INSET - 1, FRAME_INSET - 1, width - FRAME_INSET + 1,
                height - FRAME_INSET + 1, 6, 2, MasteryTheme.argb(accent, 0.9F * appear));

        float panels = MasteryTheme.easeOutCubic((entranceSeconds - 0.04F) / 0.18F);
        UiDraw.panel(context, layout.headerLeft, layout.headerTop, layout.headerRight, layout.headerBottom,
                MasteryTheme.PANEL, accent, panels);
        context.fill(layout.headerLeft, layout.headerBottom - 1, layout.headerRight, layout.headerBottom,
                MasteryTheme.argb(MasteryTheme.RULE, panels));
        UiDraw.panel(context, layout.canvasLeft, layout.canvasTop, layout.canvasRight, layout.canvasBottom,
                MasteryTheme.GROUND, accent, panels);
        UiDraw.gridLines(context, layout.canvasLeft + 1, layout.canvasTop + 1, layout.canvasRight - 1,
                layout.canvasBottom - 1, GRID_SPACING, MasteryTheme.argb(0xFFFFFF, 0.022F * panels));
        if (!layout.compact) {
            UiDraw.panel(context, layout.showcaseLeft, layout.showcaseTop, layout.showcaseRight,
                    layout.showcaseBottom, MasteryTheme.PANEL, accent, panels);
        }
    }

    // --- header -------------------------------------------------------------

    private void drawHeader(DrawContext context, MasteryProfile profile, MasteryState state) {
        float alpha = MasteryTheme.easeOutCubic((entranceSeconds - 0.06F) / 0.16F);
        if (alpha <= 0.01F) {
            return;
        }
        ItemStack stack = displayStack();
        int headerHeight = layout.headerBottom - layout.headerTop;
        boolean roomy = headerHeight >= 40;
        int textLeft = layout.headerContentLeft + (layout.compact ? 22 : 0);
        int ruleTop = layout.headerTop + 2;
        int ruleBottom = layout.headerBottom - 2;
        int accent = accentRgb();

        vRule(context, textLeft - 7, ruleTop, ruleBottom, alpha);

        float nameScale = roomy ? 1.35F : 1.0F;
        int titleTop = layout.headerTop + (headerHeight - 17) / 2;
        float nameY = roomy ? titleTop + 6 : layout.headerTop + (headerHeight - 8) * 0.5F;
        if (roomy) {
            microLabel(context, Text.translatable("screen.simplymastery.header.kicker"), textLeft,
                    titleTop, MasteryTheme.INK_MUTED, alpha);
        }
        UiDraw.text(context, textRenderer, stack.getName(), textLeft, nameY,
                MasteryTheme.argb(MasteryTheme.DISPLAY, alpha), nameScale, true);

        if (roomy) {
            int right = layout.respecButtonX - 6;
            int available = state.availablePoints(profile);
            int floor = textLeft + Math.round(textRenderer.getWidth(stack.getName()) * nameScale) + 10;
            right = statCell(context, right, floor, ruleTop, ruleBottom,
                    Text.translatable("screen.simplymastery.header.mastery"),
                    masteryCount(profile, state), MasteryTheme.INK, alpha, true);
            right = statCell(context, right, floor, ruleTop, ruleBottom,
                    Text.translatable("screen.simplymastery.header.spent"),
                    Text.literal(String.format("%02d", state.spentPoints(profile))),
                    MasteryTheme.INK, alpha, true);
            statCell(context, right, floor, ruleTop, ruleBottom,
                    Text.translatable("screen.simplymastery.header.available"),
                    Text.literal(String.format("%02d", available)),
                    available > 0 ? accent : MasteryTheme.INK_FAINT, alpha, false);
        }
    }

    /**
     * One right-aligned header stat cell (micro label over a numeral), returning the x its
     * left rule sits on so cells can be chained right to left. A cell that would collide with
     * the weapon name is dropped rather than overlapped.
     */
    private int statCell(DrawContext context, int right, int floor, int ruleTop, int ruleBottom, Text label,
                         Text value, int valueRgb, float alpha, boolean rule) {
        int cellWidth = Math.max(Math.round(textRenderer.getWidth(label) * MICRO_SCALE),
                textRenderer.getWidth(value)) + 12;
        int left = right - cellWidth;
        if (left < floor) {
            return right;
        }
        int cellTop = layout.headerTop + (layout.headerBottom - layout.headerTop - 14) / 2;
        microLabel(context, label, left + 6, cellTop, MasteryTheme.INK_MUTED, alpha);
        UiDraw.text(context, textRenderer, value, left + 6, cellTop + 6,
                MasteryTheme.argb(valueRgb, alpha), 1.0F, true);
        if (rule) {
            vRule(context, left, ruleTop, ruleBottom, alpha);
        }
        return left;
    }

    private void microLabel(DrawContext context, Text label, float x, float y, int rgb, float alpha) {
        UiDraw.text(context, textRenderer, label, x, y, MasteryTheme.argb(rgb, alpha), MICRO_SCALE, false);
    }

    private static void vRule(DrawContext context, int x, int top, int bottom, float alpha) {
        context.fill(x, top, x + 1, bottom, MasteryTheme.argb(MasteryTheme.RULE, 0.95F * alpha));
    }

    // --- canvas -------------------------------------------------------------

    private void drawCanvas(DrawContext context, MasteryProfile profile, MasteryState state) {
        float appear = MasteryTheme.easeOutCubic((entranceSeconds - 0.07F) / 0.18F);
        if (appear <= 0.01F) {
            return;
        }
        context.enableScissor(layout.canvasLeft + 1, layout.canvasTop + 1,
                layout.canvasRight - 1, layout.canvasBottom - 1);
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        try {
            float centerX = (layout.canvasLeft + layout.canvasRight) * 0.5F;
            float centerY = (layout.canvasTop + layout.canvasBottom) * 0.5F;
            matrices.translate(centerX + canvasPanX, centerY + canvasPanY, 0.0F);
            matrices.scale(canvasZoom, canvasZoom, 1.0F);
            matrices.translate(-centerX, -centerY, 0.0F);
            matrices.translate(0.0F, canvasEntranceOffset(), 0.0F);

            drawLanes(context, profile, state, appear);
            drawEdges(context, profile, state);
            for (int i = 0; i < layout.nodeCount(); i++) {
                if (i != hoveredNode && i != selectedNode) {
                    drawNode(context, profile, state, i);
                }
            }
            if (selectedNode >= 0 && selectedNode != hoveredNode) {
                drawNode(context, profile, state, selectedNode);
            }
            if (hoveredNode >= 0) {
                drawNode(context, profile, state, hoveredNode);
            }
        } finally {
            matrices.pop();
            context.disableScissor();
        }
    }

    private void drawLanes(DrawContext context, MasteryProfile profile, MasteryState state, float alpha) {
        for (int lane = 0; lane < layout.laneCount(); lane++) {
            drawBranchRow(context, profile, state, lane, alpha);
        }
    }

    /**
     * One branch row: the design's fixed label column, the rule dividing it from the node
     * field, and the rule closing the row off. Rows with nothing owned and nothing reachable
     * take the artboard's dimmed "sealed" treatment.
     */
    private void drawBranchRow(DrawContext context, MasteryProfile profile, MasteryState state, int lane,
                               float alpha) {
        MasteryProfile.Branch branch = profile.branches().get(lane);
        int rgb = branchRgb[lane];
        int owned = 0;
        int total = MasteryUiPolicy.achievableNodes(profile, branch.id());
        boolean reachable = false;
        for (int i = 0; i < profile.nodes().size(); i++) {
            MasteryProfile.Node node = profile.nodes().get(i);
            if (!node.branch().equals(branch.id())) {
                continue;
            }
            if (state.owns(node.id())) {
                owned++;
            } else if (nodeState(profile, state, node) == MasteryNodeState.REACHABLE) {
                reachable = true;
            }
        }
        boolean sealed = owned == 0 && !reachable;
        float rowAlpha = alpha * (sealed ? 0.72F : 1.0F);
        int nameRgb = sealed ? MasteryTheme.INK_SOFT : MasteryTheme.DISPLAY;

        int columnLeft = layout.canvasLeft + 5;
        int columnRight = layout.laneLabelRight - 5;
        int columnWidth = Math.max(12, columnRight - columnLeft);
        int top = Math.round(layout.laneBandTop(lane));
        int bottom = Math.round(layout.laneBandBottom(lane));

        // Lay the column out top-down, dropping whatever the row is too short to hold.
        int cursor = top;
        if (cursor + 4 <= bottom) {
            if (sealed) {
                UiDraw.boxOutline(context, columnLeft, cursor, columnLeft + 3, cursor + 3, 1,
                        MasteryTheme.argb(MasteryTheme.INK_MUTED, 0.9F * rowAlpha));
            } else {
                context.fill(columnLeft, cursor, columnLeft + 3, cursor + 3,
                        MasteryTheme.argb(rgb, 0.95F * rowAlpha));
            }
            microLabel(context, Text.translatable(sealed
                            ? "screen.simplymastery.branch.sealed" : "screen.simplymastery.branch.attuned"),
                    columnLeft + 5, cursor - 1, MasteryTheme.INK_MUTED, rowAlpha);
            cursor += 6;
        }
        if (cursor + 8 <= bottom) {
            Text branchName = Text.translatable(branch.nameKey());
            UiDraw.Fitted name = UiDraw.fitBlock(textRenderer, branchName, columnWidth, 2, 0.5F);
            int lineHeight = Math.max(6, Math.round(9.0F * name.scale()));
            int nameHeight = lineHeight * name.lines().size() + 2;
            if (cursor + nameHeight > bottom) {
                // Too short a row for two lines: scale one line down rather than truncate it.
                name = UiDraw.fitBlock(textRenderer, branchName, columnWidth, 1, 0.5F);
                lineHeight = Math.max(6, Math.round(9.0F * name.scale()));
                nameHeight = lineHeight + 2;
            }
            for (int line = 0; line < name.lines().size(); line++) {
                UiDraw.text(context, textRenderer, name.lines().get(line), columnLeft,
                        cursor + line * lineHeight, MasteryTheme.argb(nameRgb, rowAlpha),
                        name.scale(), true);
            }
            cursor += nameHeight;
        }
        if (cursor + 8 <= bottom) {
            String ownedText = Integer.toString(owned);
            UiDraw.text(context, textRenderer, Text.literal(ownedText), columnLeft, cursor,
                    MasteryTheme.argb(sealed ? MasteryTheme.INK_DIM : rgb, rowAlpha), 1.0F, true);
            UiDraw.text(context, textRenderer, Text.literal("/ " + total),
                    columnLeft + textRenderer.getWidth(ownedText) + 3, cursor + 1,
                    MasteryTheme.argb(MasteryTheme.INK_FAINT, rowAlpha), MICRO_SCALE, false);
            cursor += 11;
        }
        if (cursor + 3 <= bottom) {
            UiDraw.segmentStrip(context, columnLeft, cursor, columnWidth, 3, total,
                    total == 0 ? 0.0F : owned / (float) total, 1, rgb, rowAlpha);
        }

        vRule(context, layout.laneLabelRight, top, bottom, alpha);
        if (lane < layout.laneCount() - 1) {
            int ruleY = Math.round(layout.laneRuleY(lane));
            context.fill(layout.canvasLeft + 2, ruleY, layout.canvasRight - 2, ruleY + 1,
                    MasteryTheme.argb(MasteryTheme.RULE, 0.95F * alpha));
        }
    }

    private void drawEdges(DrawContext context, MasteryProfile profile, MasteryState state) {
        double time = seconds();
        List<MasteryLayout.Edge> edges = layout.edges();
        for (int index = 0; index < edges.size(); index++) {
            MasteryLayout.Edge edge = edges.get(index);
            float reveal = edgeReveal(edge);
            if (reveal <= 0.01F) {
                continue;
            }
            MasteryProfile.Node parent = profile.nodes().get(edge.parent());
            MasteryProfile.Node child = profile.nodes().get(edge.child());
            float limit = edge.length() * reveal;
            int rgb = branchRgb[layout.branch(edge.child())];
            boolean parentOwned = state.owns(parent.id());
            boolean linked = parentOwned && state.owns(child.id());
            float flow = animators.owned(edge.child());

            if (linked || flow > 0.01F) {
                float drawn = Math.min(limit, edge.length() * flow);
                // A darker run under the bar stands in for the design's inset bottom shadow.
                UiDraw.pathRange(context, edge.path(), edge.points(), 0.0F, drawn, 3.0F,
                        MasteryTheme.argb(MasteryTheme.dim(rgb, 0.55F), 0.8F * reveal));
                UiDraw.pathRange(context, edge.path(), edge.points(), 0.0F, drawn, 2.0F,
                        MasteryTheme.argb(rgb, 0.95F * reveal));
                if (linked) {
                    float cycle = edge.length() + 70.0F;
                    float head = (float) ((time * 46.0 * motion() + index * 23.0) % cycle);
                    UiDraw.pathRange(context, edge.path(), edge.points(), head - 20.0F, head, 2.0F,
                            MasteryTheme.argb(MasteryTheme.mix(rgb, 0xFFFFFF, 0.55F), 0.7F * reveal));
                }
            } else if (parentOwned && nodeState(profile, state, child) != MasteryNodeState.CONFLICT) {
                float flicker = MasteryTheme.lerp(0.9F, MasteryTheme.flicker(time + index * 0.11, 1.8),
                        motion());
                UiDraw.pathDashed(context, edge.path(), edge.points(), edge.length(), limit,
                        3.0F, 3.0F, 0.0F, 2.0F, MasteryTheme.argb(rgb, flicker * reveal));
            } else {
                UiDraw.pathDashed(context, edge.path(), edge.points(), edge.length(), limit,
                        3.0F, 3.0F, 0.0F, 1.0F,
                        MasteryTheme.argb(MasteryTheme.LOCKED_DASH, 0.9F * reveal));
            }
        }
    }

    private float edgeReveal(MasteryLayout.Edge edge) {
        return MasteryTheme.easeOutCubic(
                (entranceSeconds - 0.11F - layout.depth(edge.child()) * 0.028F) / 0.17F);
    }

    private void drawNode(DrawContext context, MasteryProfile profile, MasteryState state, int index) {
        MasteryProfile.Node node = profile.nodes().get(index);
        float reveal = MasteryTheme.easeOutBack((entranceSeconds - 0.13F
                - layout.branch(index) * 0.022F - layout.depth(index) * 0.032F) / 0.16F);
        if (reveal <= 0.01F) {
            return;
        }
        float alpha = Math.min(1.0F, reveal);
        float hover = animators.hover(index);
        float focus = animators.focus(index);
        float owned = animators.owned(index);
        float burst = animators.burst(index);
        MasteryNodeState nodeState = nodeState(profile, state, node);
        boolean capstone = node.capstone();
        int rgb = branchRgb[layout.branch(index)];
        double time = seconds();

        float lift = 2.0F * Math.max(hover, focus * 0.6F) * motion();
        float cx = layout.x(index);
        float cy = layout.y(index) - lift;
        float half = layout.radius(index) * Math.max(0.2F, reveal) * (1.0F + 0.1F * hover);
        int x0 = Math.round(cx - half);
        int y0 = Math.round(cy - half);
        int x1 = Math.round(cx + half);
        int y1 = Math.round(cy + half);

        // Ambient light: owned squares breathe, reachable ones run the design's stepped pulse.
        float ambient = switch (nodeState) {
            case OWNED -> 0.55F + 0.18F * MasteryTheme.pulse(time + index, 2.6) * motion();
            case REACHABLE -> 0.3F + 0.28F * MasteryTheme.stepPulse(time + index * 0.4, 2.2, 6) * motion();
            default -> 0.05F;
        };
        float glow = (ambient + hover * 0.75F + focus * 0.4F) * alpha;
        UiDraw.boxGlow(context, cx, cy, half * 1.15F, rgb, 0.09F * glow, 8);

        switch (nodeState) {
            case OWNED -> {
                context.fill(x0, y0, x1, y1, MasteryTheme.argb(rgb, 0.98F * alpha));
                UiDraw.bevel(context, x0, y0, x1, y1, capstone ? 2 : 1,
                        (0.8F + 0.4F * Math.max(hover, focus)) * alpha);
            }
            case REACHABLE -> {
                context.fill(x0, y0, x1, y1,
                        MasteryTheme.argb(rgb, (0.14F + 0.16F * hover) * alpha));
                UiDraw.boxOutline(context, x0, y0, x1, y1, capstone ? 2 : 1,
                        MasteryTheme.argb(rgb, (0.85F + 0.15F * Math.max(hover, focus)) * alpha));
                // mt-pulse: a square halo stepping outward and fading.
                float pulse = MasteryTheme.stepPulse(time + index * 0.4, 2.2, 6);
                UiDraw.boxRing(context, cx, cy, half + 1.0F + pulse * 3.0F, 1.0F,
                        MasteryTheme.argb(rgb, 0.5F * (1.0F - pulse) * alpha * motion()));
            }
            case LOCKED, CONFLICT -> {
                context.fill(x0, y0, x1, y1, MasteryTheme.argb(MasteryTheme.LOCKED_FILL, 0.95F * alpha));
                UiDraw.bevel(context, x0, y0, x1, y1, 1, 0.4F * alpha);
                UiDraw.boxOutline(context, x0, y0, x1, y1, 1,
                        MasteryTheme.argb(MasteryTheme.LOCKED_FRAME, 0.95F * alpha));
            }
        }
        if (capstone && nodeState != MasteryNodeState.LOCKED) {
            UiDraw.boxRing(context, cx, cy, half - 3.0F, 1.0F,
                    MasteryTheme.argb(nodeState == MasteryNodeState.OWNED
                            ? MasteryTheme.ON_ACCENT : rgb, 0.45F * alpha));
        }

        // Selection reads as the artboard's detached bracket ring; hover just brightens the rim.
        if (hover > 0.01F && focus <= 0.01F) {
            UiDraw.boxRing(context, cx, cy, half + 1.0F, 1.0F,
                    MasteryTheme.argb(0xFFFFFF, 0.4F * hover * alpha));
        }
        if (focus > 0.01F) {
            float spread = 2.0F + (1.0F - MasteryTheme.easeOutCubic(focus)) * 4.0F;
            UiDraw.marchingBrackets(context, cx, cy, half, spread, Math.max(3, Math.round(half * 0.55F)), 1,
                    MasteryTheme.argb(rgb, 0.9F * focus * alpha));
        }

        float glyphSize = half * 0.46F;
        int glyphRgb = switch (nodeState) {
            case OWNED -> MasteryTheme.ON_ACCENT;
            case REACHABLE -> rgb;
            default -> MasteryTheme.LOCKED_GLYPH;
        };
        int glyphColor = MasteryTheme.argb(glyphRgb, 0.95F * alpha);
        switch (nodeState) {
            case OWNED -> {
                if (capstone) {
                    drawNodeIcon(context, node, index, cx, cy - half * 0.18F, glyphSize, glyphColor);
                    checkGlyph(context, cx, cy + half * 0.44F, glyphSize * 0.6F, glyphColor);
                } else {
                    checkGlyph(context, cx, cy, glyphSize, glyphColor);
                }
            }
            case CONFLICT -> {
                drawNodeIcon(context, node, index, cx, cy, glyphSize,
                        MasteryTheme.argb(MasteryTheme.LOCKED_GLYPH, 0.6F * alpha));
                UiDraw.segment(context, cx - half * 0.6F, cy + half * 0.6F,
                        cx + half * 0.6F, cy - half * 0.6F, 2.0F,
                        MasteryTheme.argb(MasteryTheme.ACCENT_HOT, 0.9F * alpha));
            }
            case LOCKED -> lockGlyph(context, cx, cy, glyphSize, glyphColor);
            case REACHABLE -> drawNodeIcon(context, node, index, cx, cy, glyphSize, glyphColor);
        }

        if (burst > 0.0F) {
            float progress = 1.0F - burst;
            UiDraw.boxRing(context, cx, cy, half * (1.0F + progress * 2.2F), 2.0F,
                    MasteryTheme.argb(MasteryTheme.mix(rgb, 0xFFFFFF, 0.5F), 0.7F * burst));
            UiDraw.marchingBrackets(context, cx, cy, half, progress * 24.0F,
                    Math.max(2, Math.round(6.0F * burst)), 1, MasteryTheme.argb(rgb, 0.8F * burst));
        }
    }

    // --- showcase -----------------------------------------------------------

    private void drawShowcase(DrawContext context, MasteryProfile profile, MasteryState state) {
        float appear = MasteryTheme.easeOutCubic((entranceSeconds - 0.09F) / 0.19F);
        if (appear <= 0.01F) {
            return;
        }
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0.0F, (1.0F - appear) * 18.0F, 0.0F);

        ItemStack stack = displayStack();
        Text weaponName = stack.getName();
        Text profileLabel = profileName(profile);
        boolean showProfile = !profileLabel.getString().equals(weaponName.getString());
        double time = seconds();
        float intensity = motion();
        int accent = accentRgb();
        int centerX = (layout.showcaseLeft + layout.showcaseRight) / 2;
        int footerHeight = showProfile ? 60 : 49;
        int wellLeft = layout.showcaseLeft + 4;
        int wellRight = layout.showcaseRight - 4;
        int wellTop = layout.showcaseTop + 4;
        int wellBottom = layout.showcaseBottom - footerHeight - 2;
        int centerY = (wellTop + wellBottom) / 2;
        float aura = Math.min((wellRight - wellLeft) * 0.46F, (wellBottom - wellTop) * 0.45F);
        float progress = masteryMeter.value();

        // The design's image well: a hatched ground inside detached corner brackets.
        context.fill(wellLeft, wellTop, wellRight, wellBottom,
                MasteryTheme.argb(MasteryTheme.WELL, 0.95F * appear));
        UiDraw.hatch45(context, wellLeft, wellTop, wellRight, wellBottom, 6,
                MasteryTheme.argb(0xFFFFFF, 0.02F * appear));
        UiDraw.cornerBrackets(context, wellLeft + 3, wellTop + 3, wellRight - 3, wellBottom - 3, 5, 1,
                MasteryTheme.argb(MasteryTheme.BRACKET, 0.9F * appear));

        float breathe = 1.0F + 0.03F * (float) Math.sin(time * 0.55) * intensity;
        UiDraw.boxGlow(context, centerX, centerY, aura * 1.2F * breathe,
                accent, 0.045F * appear * (0.6F + 0.5F * progress), 14);
        UiDraw.marchingBrackets(context, centerX, centerY, aura * 0.94F,
                (float) (Math.sin(time * 0.5) * 2.0) * intensity, 6, 1,
                MasteryTheme.argb(accent, (0.12F + 0.22F * progress) * appear));
        UiDraw.marchingBrackets(context, centerX, centerY, aura * 1.04F,
                (float) (-Math.sin(time * 0.5) * 2.0) * intensity, 4, 1,
                MasteryTheme.argb(accent, (0.08F + 0.18F * progress) * appear));

        float driftX = (float) Math.sin(time * 0.37 + 1.2) * 3.0F * intensity;
        float driftY = ((float) Math.sin(time * 0.90) * 6.0F + (float) Math.sin(time * 0.27) * 2.0F) * intensity;
        float scale = Math.clamp(aura * 1.3F / 16.0F, 1.5F, 12.0F)
                * (1.0F + (float) Math.sin(time * 0.55) * 0.012F * intensity);
        float itemX = centerX - 8.0F * scale + driftX;
        float itemY = centerY - 8.0F * scale + driftY;
        UiDraw.liveItem(context, stack, itemX, itemY, scale, 140.0F);
        setItemHover(Math.round(itemX), Math.round(itemY + (1.0F - appear) * 18.0F),
                Math.round(16.0F * scale));

        int textLeft = layout.showcaseLeft + 8;
        int textRight = layout.showcaseRight - 8;
        int nameY = layout.showcaseBottom - footerHeight + 6;
        context.fill(layout.showcaseLeft + 1, nameY - 5, layout.showcaseRight - 1, nameY - 4,
                MasteryTheme.argb(MasteryTheme.RULE, appear));
        if (showProfile) {
            microLabel(context, UiDraw.fit(textRenderer, profileLabel, (textRight - textLeft) * 2),
                    textLeft, nameY, MasteryTheme.INK_MUTED, appear);
            nameY += 6;
        }
        UiDraw.text(context, textRenderer, UiDraw.fit(textRenderer, weaponName, textRight - textLeft),
                textLeft, nameY, MasteryTheme.argb(accent, appear), 1.0F, true);

        int barTop = nameY + 16;
        microLabel(context, Text.translatable("screen.simplymastery.header.mastery"), textLeft, barTop - 7,
                MasteryTheme.INK_MUTED, appear);
        UiDraw.rightText(context, textRenderer, masteryCount(profile, state), textRight, barTop - 9,
                MasteryTheme.argb(MasteryTheme.INK, appear), 1.0F, false);
        UiDraw.segmentStrip(context, textLeft, barTop, textRight - textLeft, 4,
                Math.max(1, MasteryUiPolicy.achievableNodes(profile, null)), progress, 1, accent, appear);
        matrices.pop();
    }

    private void drawCompactBadge(DrawContext context) {
        float appear = MasteryTheme.easeOutCubic((entranceSeconds - 0.09F) / 0.19F);
        if (appear <= 0.01F) {
            return;
        }
        if (layout.headerContentRight - layout.headerContentLeft < 24) {
            return;
        }
        int badgeX = layout.headerContentLeft;
        int badgeY = layout.headerTop + (layout.headerBottom - layout.headerTop) / 2 - 8;
        UiDraw.boxGlow(context, badgeX + 8, badgeY + 8, 16.0F,
                accentRgb(), 0.13F * appear, 4);
        UiDraw.liveItem(context, displayStack(), badgeX, badgeY, 1.0F, 140.0F);
        setItemHover(badgeX, badgeY, 16);
    }

    private void setItemHover(int x, int y, int size) {
        itemHoverLeft = x - 1;
        itemHoverTop = y - 1;
        itemHoverRight = x + size + 1;
        itemHoverBottom = y + size + 1;
    }

    // --- detail card --------------------------------------------------------

    private void drawDetailCard(DrawContext context, MasteryProfile profile, MasteryState state,
                                int mouseX, int mouseY) {
        float alpha = cardFade.value();
        if (alpha <= 0.01F || cardNode < 0) {
            return;
        }
        MasteryProfile.Node node = profile.nodes().get(cardNode);
        MasteryNodeState nodeState = nodeState(profile, state, node);
        int rgb = branchRgb[layout.branch(cardNode)];
        int cardWidth = layout.dockDetail
                ? layout.canvasRight - layout.canvasLeft
                : Math.clamp(width / 4, 196, 288);
        List<OrderedText> lines = textRenderer.wrapLines(Text.translatable(node.descriptionKey()), cardWidth - 22);
        int chrome = 28 + 21;
        int lineCount = Math.min(CARD_LINES, lines.size());
        if (layout.dockDetail) {
            lineCount = Math.clamp((layout.dockHeight - chrome) / 10, 1, lineCount);
        }
        int cardHeight = chrome + lineCount * 10;

        int cardX;
        int cardY;
        if (layout.dockDetail) {
            cardX = layout.canvasLeft;
            cardY = layout.canvasBottom + 6;
        } else {
            int anchorX = hoveredNode >= 0 ? mouseX : Math.round(layout.x(cardNode));
            int anchorY = hoveredNode >= 0 ? mouseY : Math.round(layout.y(cardNode));
            cardX = anchorX + 18;
            if (cardX + cardWidth > width - 8) {
                cardX = anchorX - 18 - cardWidth;
            }
            cardY = anchorY + 14;
            if (cardY + cardHeight > height - 8) {
                cardY = anchorY - 14 - cardHeight;
            }
            cardX = Math.clamp(cardX, 8, Math.max(8, width - cardWidth - 8));
            cardY = Math.clamp(cardY, layout.headerBottom + 4, Math.max(8, height - cardHeight - 8));
        }
        float slide = (1.0F - MasteryTheme.easeOutCubic(alpha)) * 6.0F;
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0.0F, slide, 0.0F);

        // Hard offset shadow, no blur, no radius.
        context.fill(cardX + 2, cardY + 3, cardX + cardWidth + 2, cardY + cardHeight + 3,
                MasteryTheme.argb(0x000000, 0.45F * alpha));
        context.fill(cardX, cardY, cardX + cardWidth, cardY + cardHeight,
                MasteryTheme.argb(MasteryTheme.CARD, 0.97F * alpha));
        UiDraw.boxOutline(context, cardX, cardY, cardX + cardWidth, cardY + cardHeight, 1,
                MasteryTheme.argb(rgb, 0.9F * alpha));

        int textX = cardX + 6;
        int textRight = cardX + cardWidth - 6;
        int stateColor = switch (nodeState) {
            case REACHABLE, OWNED -> rgb;
            case CONFLICT -> MasteryTheme.ACCENT_HOT;
            case LOCKED -> MasteryTheme.INK_MUTED;
        };
        Text stateTag = Text.translatable("screen.simplymastery.node_state."
                + nodeState.name().toLowerCase(), node.cost());

        UiDraw.text(context, textRenderer, UiDraw.fit(textRenderer, Text.translatable(node.nameKey()),
                        cardWidth - 12), textX, cardY + 6,
                MasteryTheme.argb(MasteryTheme.DISPLAY, alpha), 1.0F, true);
        Text branchName = Text.translatable(profile.branches().get(layout.branch(cardNode)).nameKey());
        microLabel(context, branchName, textX, cardY + 17, rgb, 0.9F * alpha);
        if (node.capstone()) {
            UiDraw.rightText(context, textRenderer, Text.translatable("screen.simplymastery.capstone"),
                    textRight, cardY + 16, MasteryTheme.argb(rgb, 0.85F * alpha), MICRO_SCALE, false);
        }
        context.fill(cardX + 1, cardY + 23, cardX + cardWidth - 1, cardY + 24,
                MasteryTheme.argb(MasteryTheme.RULE, alpha));
        for (int i = 0; i < lineCount; i++) {
            UiDraw.text(context, textRenderer, lines.get(i), textX, cardY + 28 + i * 10,
                    MasteryTheme.argb(MasteryTheme.BODY, 0.95F * alpha), 1.0F, false);
        }

        // Footer cells, split by the design's rules: cost on the left, state on the right.
        int footerY = cardY + cardHeight - 15;
        context.fill(cardX + 1, footerY - 4, cardX + cardWidth - 1, footerY - 3,
                MasteryTheme.argb(MasteryTheme.RULE, alpha));
        Text costLabel = Text.translatable("screen.simplymastery.header.cost");
        microLabel(context, costLabel, textX, footerY - 1, MasteryTheme.INK_MUTED, alpha);
        UiDraw.pips(context, textX + Math.round(textRenderer.getWidth(costLabel) * MICRO_SCALE) + 5,
                footerY - 1, node.cost(), node.cost(), 3, 2, rgb, alpha);
        UiDraw.rightText(context, textRenderer, UiDraw.fit(textRenderer, stateTag, cardWidth),
                textRight, footerY - 1, MasteryTheme.argb(stateColor, alpha), MICRO_SCALE, false);
        matrices.pop();
    }

    // --- status -------------------------------------------------------------

    private void drawStatus(DrawContext context) {
        float appear = MasteryTheme.easeOutCubic((entranceSeconds - 0.15F) / 0.16F);
        if (appear <= 0.01F) {
            return;
        }
        int accent = accentRgb();
        context.fill(layout.statusLeft, layout.statusTop, layout.statusRight, layout.statusTop + 1,
                MasteryTheme.argb(MasteryTheme.RULE, 0.95F * appear));

        UnlockResult result = MasteryClientFeedback.result();
        if (result != null && feedbackTimer > 0.0F) {
            float fade = Math.min(1.0F, feedbackTimer / 0.5F)
                    * Math.min(1.0F, (FEEDBACK_SECONDS - feedbackTimer) / 0.12F);
            boolean success = result == UnlockResult.SUCCESS;
            int rgb = success ? accent : MasteryTheme.ACCENT_HOT;
            float lift = (1.0F - Math.min(1.0F, (FEEDBACK_SECONDS - feedbackTimer) / 0.25F)) * 5.0F * motion();
            Text message = Text.translatable("message.simplymastery.unlock." + result.name().toLowerCase());
            float messageY = layout.statusTop + 5 - lift;
            context.fill(layout.statusLeft + 2, Math.round(messageY) + 1, layout.statusLeft + 5,
                    Math.round(messageY) + 4, MasteryTheme.argb(rgb, 0.95F * fade));
            UiDraw.text(context, textRenderer, UiDraw.fit(textRenderer, message,
                            layout.statusRight - layout.statusLeft - 10), layout.statusLeft + 8, messageY,
                    MasteryTheme.argb(rgb, fade), 1.0F, true);
            return;
        }

        MasteryState current = state(profile);
        int available = current.availablePoints(profile);
        int y = layout.statusTop + 4;
        if (!layout.compact) {
            int x = layout.statusLeft + 2;
            x = keyHint(context, x, y, "lmb", "select", appear);
            x = keyHint(context, x, y, "lmb2", "unlock", appear);
            keyHint(context, x, y, "esc", "close", appear);
        }
        Text unspent = Text.translatable("screen.simplymastery.unspent", available);
        UiDraw.rightText(context, textRenderer,
                UiDraw.fit(textRenderer, unspent, layout.statusRight - layout.statusLeft),
                layout.statusRight - 2, y + 2,
                MasteryTheme.argb(available > 0 ? accent : MasteryTheme.INK_MUTED, 0.9F * appear),
                1.0F, false);
    }

    /** A boxed key beside its action, the artboard's footer legend unit. */
    private int keyHint(DrawContext context, int x, int y, String key, String action, float appear) {
        int capRight = UiDraw.keyCap(context, textRenderer, Text.translatable("screen.simplymastery.key." + key),
                x, y, MasteryTheme.INK_DIM, MasteryTheme.BRACKET, 0.85F * appear);
        Text label = Text.translatable("screen.simplymastery.key." + action);
        UiDraw.text(context, textRenderer, label, capRight + 4, y + 4,
                MasteryTheme.argb(MasteryTheme.INK_MUTED, 0.85F * appear), MICRO_SCALE, false);
        return capRight + 11 + Math.round(textRenderer.getWidth(label) * MICRO_SCALE);
    }

    // --- prompt -------------------------------------------------------------

    /** Ask the player a question inside this screen rather than handing off to a vanilla one. */
    private void openPrompt(MasteryPrompt request) {
        prompt = request;
        promptFade.snap(0.0F);
        promptFade.target(1.0F);
        hoveredNode = -1;
        layoutPrompt();
        applyPromptGating();
        MasteryUiSounds.openView();
    }

    private void closePrompt() {
        if (prompt == null) {
            return;
        }
        prompt = null;
        promptFade.snap(0.0F);
        applyPromptGating();
        setFocused(null);
    }

    private void acceptPrompt() {
        MasteryPrompt accepted = prompt;
        closePrompt();
        if (accepted != null) {
            accepted.onConfirm().run();
        }
    }

    /** A prompt is modal: the header stops responding while it is up, and vice versa. */
    private void applyPromptGating() {
        boolean open = prompt != null;
        if (backButton != null) {
            backButton.active = !open;
            closeButton.active = !open;
            respecButton.active = !open && MasteryConfig.SERVER.respecEnabled;
        }
        if (promptConfirm != null) {
            promptConfirm.visible = open;
            promptConfirm.active = open;
            promptCancel.visible = open;
            promptCancel.active = open;
        }
    }

    /** Measure the box and place its buttons. Re-run on resize so the prompt survives one. */
    private void layoutPrompt() {
        if (prompt == null) {
            return;
        }
        promptWidth = Math.clamp(width / 3, 200, 300);
        promptLines = textRenderer.wrapLines(prompt.message(), promptWidth - 24);
        promptHeight = 62 + promptLines.size() * 10;
        promptX = (width - promptWidth) / 2;
        promptY = Math.max(4, (height - promptHeight) / 2);

        int buttonWidth = Math.clamp(promptWidth / 3, 60, 92);
        int buttonTop = promptY + promptHeight - 26;
        int buttonLeft = promptX + (promptWidth - (buttonWidth * 2 + 6)) / 2;
        promptCancel.setWidth(buttonWidth);
        promptCancel.setPosition(buttonLeft, buttonTop);
        promptConfirm.setWidth(buttonWidth);
        promptConfirm.setPosition(buttonLeft + buttonWidth + 6, buttonTop);
    }

    private void drawPrompt(DrawContext context, int mouseX, int mouseY, float delta) {
        if (prompt == null) {
            return;
        }
        float fade = promptFade.value();
        if (fade <= 0.01F) {
            return;
        }
        int accent = accentRgb();
        context.fill(0, 0, width, height, MasteryTheme.argb(MasteryTheme.GROUND_DEEP, 0.72F * fade));

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0.0F, (1.0F - MasteryTheme.easeOutCubic(fade)) * 8.0F, 0.0F);

        int x1 = promptX + promptWidth;
        int y1 = promptY + promptHeight;
        context.fill(promptX + 3, promptY + 4, x1 + 3, y1 + 4, MasteryTheme.argb(0x000000, 0.5F * fade));
        context.fill(promptX, promptY, x1, y1, MasteryTheme.argb(MasteryTheme.CARD, 0.98F * fade));
        UiDraw.boxOutline(context, promptX, promptY, x1, y1, 1, MasteryTheme.argb(accent, 0.9F * fade));
        UiDraw.cornerBrackets(context, promptX + 3, promptY + 3, x1 - 3, y1 - 3, 7, 1,
                MasteryTheme.argb(accent, 0.7F * fade));

        int textX = promptX + 12;
        UiDraw.text(context, textRenderer,
                UiDraw.fit(textRenderer, prompt.title(), promptWidth - 24), textX, promptY + 8,
                MasteryTheme.argb(MasteryTheme.DISPLAY, fade), 1.0F, true);
        context.fill(promptX + 1, promptY + 20, x1 - 1, promptY + 21,
                MasteryTheme.argb(MasteryTheme.RULE, fade));
        for (int i = 0; i < promptLines.size(); i++) {
            UiDraw.text(context, textRenderer, promptLines.get(i), textX, promptY + 25 + i * 10,
                    MasteryTheme.argb(MasteryTheme.BODY, 0.95F * fade), 1.0F, false);
        }
        int ruleY = promptY + promptHeight - 33;
        context.fill(promptX + 1, ruleY, x1 - 1, ruleY + 1, MasteryTheme.argb(MasteryTheme.RULE, fade));

        promptCancel.render(context, mouseX, mouseY, delta);
        promptConfirm.render(context, mouseX, mouseY, delta);
        matrices.pop();
    }

    // --- glyphs -------------------------------------------------------------

    private void drawNodeIcon(DrawContext context, MasteryProfile.Node node, int index, float cx, float cy,
                              float size, int argb) {
        Identifier icon = nodeIcons[index];
        if (icon != null) {
            UiDraw.textureIcon(context, icon, cx, cy, Math.max(6, Math.round(size * 2.0F)));
        } else {
            sigil(context, node.id(), cx, cy, size, argb);
        }
    }

    private static void sigil(DrawContext context, String id, float cx, float cy, float size, int argb) {
        int hash = id.hashCode() * 0x9E3779B9;
        float thickness = Math.max(1.0F, size * 0.26F);
        UiDraw.segment(context, cx, cy - size, cx, cy + size, thickness, argb);
        for (int branch = 0; branch < 2; branch++) {
            int bits = hash >>> (branch * 9);
            float stemY = ((bits & 3) - 1.5F) / 1.5F;
            float side = (bits & 4) == 0 ? -1.0F : 1.0F;
            float reach = 0.5F + ((bits >> 3) & 1) * 0.5F;
            float drop = (((bits >> 4) & 3) - 1.5F) / 1.5F;
            UiDraw.segment(context, cx, cy + stemY * size,
                    cx + side * reach * size, cy + (stemY + drop * 0.7F) * size, thickness, argb);
        }
    }

    private static void checkGlyph(DrawContext context, float cx, float cy, float size, int argb) {
        UiDraw.segment(context, cx - size * 0.75F, cy, cx - size * 0.15F, cy + size * 0.6F,
                Math.max(1.0F, size * 0.34F), argb);
        UiDraw.segment(context, cx - size * 0.15F, cy + size * 0.6F, cx + size * 0.85F, cy - size * 0.65F,
                Math.max(1.0F, size * 0.34F), argb);
    }

    private static void lockGlyph(DrawContext context, float cx, float cy, float size, int argb) {
        int bodyWidth = Math.max(3, Math.round(size * 1.25F));
        int bodyHeight = Math.max(3, Math.round(size * 1.0F));
        int top = Math.round(cy - size * 0.05F);
        context.fill(Math.round(cx - bodyWidth * 0.5F), top,
                Math.round(cx + bodyWidth * 0.5F), top + bodyHeight, argb);
        UiDraw.boxRing(context, cx, top, size * 0.55F, 1.0F, argb);
    }

    // --- input --------------------------------------------------------------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (prompt != null) {
            for (Element child : children()) {
                if (child.mouseClicked(mouseX, mouseY, button)) {
                    setFocused(child);
                    break;
                }
            }
            return true;
        }
        for (Element child : children()) {
            if (child.mouseClicked(mouseX, mouseY, button)) {
                setFocused(child);
                return true;
            }
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            int clicked = insideCanvas(mouseX, mouseY)
                    ? layout.nodeAt(toLayoutX(mouseX), toLayoutY(mouseY)) : -1;
            if (clicked >= 0) {
                if (selectedNode == clicked) {
                    requestUnlock(clicked);
                } else {
                    setFocused(null);
                    selectedNode = clicked;
                    MasteryUiSounds.select();
                    applyMousePressScrollNarratorDelay();
                }
            } else {
                selectedNode = -1;
            }
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Element child : children()) {
            child.mouseReleased(mouseX, mouseY, button);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (prompt != null) {
            return true;
        }
        if (layout.compact && canvasZoom > 1.0F
                && (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
            canvasPanX += (float) deltaX;
            canvasPanY += (float) deltaY;
            clampCanvasPan();
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (prompt != null) {
            return true;
        }
        if (layout.compact && insideCanvas(mouseX, mouseY) && verticalAmount != 0.0) {
            float oldZoom = canvasZoom;
            float worldX = (float) toLayoutX(mouseX);
            float worldY = (float) toLayoutY(mouseY);
            canvasZoom = Math.clamp(canvasZoom + (float) verticalAmount * 0.08F
                    * MasteryConfig.CLIENT.zoomSensitivity, 1.0F, 1.35F);
            if (canvasZoom != oldZoom) {
                float centerX = (layout.canvasLeft + layout.canvasRight) * 0.5F;
                float centerY = (layout.canvasTop + layout.canvasBottom) * 0.5F;
                canvasPanX = (float) mouseX - centerX - (worldX - centerX) * canvasZoom;
                canvasPanY = (float) mouseY - centerY
                        - (worldY + canvasEntranceOffset() - centerY) * canvasZoom;
                clampCanvasPan();
            }
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (prompt != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                closePrompt();
                return true;
            }
            Element focused = getFocused();
            if ((focused == promptCancel || focused == promptConfirm)
                    && focused.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) {
                acceptPrompt();
                return true;
            }
            return keyCode != GLFW.GLFW_KEY_TAB || super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        int dirX = switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT -> -1;
            case GLFW.GLFW_KEY_RIGHT -> 1;
            default -> 0;
        };
        int dirY = switch (keyCode) {
            case GLFW.GLFW_KEY_UP -> -1;
            case GLFW.GLFW_KEY_DOWN -> 1;
            default -> 0;
        };
        if (dirX != 0 || dirY != 0) {
            int next = layout.neighbour(selectedNode, dirX, dirY);
            if (next >= 0 && next != selectedNode) {
                setFocused(null);
                selectedNode = next;
                revealSelectedNode(next);
                MasteryUiSounds.select();
                applyKeyPressNarratorDelay();
            }
            return true;
        }
        for (Element child : children()) {
            if (child.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) && selectedNode >= 0) {
            requestUnlock(selectedNode);
        }
        return true;
    }

    @Override
    protected void addScreenNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, getNarratedTitle());
        if (prompt != null) {
            NarrationMessageBuilder promptMessage = builder.nextMessage();
            promptMessage.put(NarrationPart.TITLE, prompt.title());
            promptMessage.put(NarrationPart.HINT, prompt.message());
            addElementNarrations(promptMessage.nextMessage());
            return;
        }
        if (profile == null || layout == null) {
            addElementNarrations(builder.nextMessage());
            return;
        }
        int narrated = selectedNode >= 0 ? selectedNode : hoveredNode;
        if (narrated >= 0 && narrated < profile.nodes().size()) {
            MasteryProfile.Node node = profile.nodes().get(narrated);
            MasteryState current = state(profile);
            MasteryNodeState visualState = nodeState(profile, current, node);
            MasteryProfile.Branch branch = profile.branches().get(layout.branch(narrated));
            NarrationMessageBuilder nodeMessage = builder.nextMessage();
            nodeMessage.put(NarrationPart.TITLE, Text.translatable(node.nameKey()));
            nodeMessage.put(NarrationPart.POSITION, Text.translatable("screen.simplymastery.narration.position",
                    narrated + 1, profile.nodes().size(), Text.translatable(branch.nameKey())));
            nodeMessage.put(NarrationPart.HINT, Text.translatable("screen.simplymastery.narration.details",
                    nodeStatusText(profile, current, node, visualState),
                    prerequisiteText(current, node), Text.translatable(node.descriptionKey()),
                    node.capstone() ? Text.translatable("screen.simplymastery.narration.capstone") : Text.empty()));
            nodeMessage.put(NarrationPart.USAGE, visualState == MasteryNodeState.REACHABLE
                    ? Text.translatable("screen.simplymastery.narration.usage.unlock")
                    : Text.translatable("screen.simplymastery.narration.usage.navigate"));
        } else {
            builder.put(NarrationPart.HINT, Text.translatable("screen.simplymastery.narration.overview"));
        }
        addElementNarrations(builder.nextMessage());
    }

    @Override
    public void removed() {
        if (!switchingView) {
            super.removed();
        }
    }

    // --- state --------------------------------------------------------------

    private void requestUnlock(int index) {
        MasteryState state = state(profile);
        MasteryProfile.Node node = profile.nodes().get(index);
        if (nodeState(profile, state, node) != MasteryNodeState.REACHABLE) {
            MasteryUiSounds.denied();
            return;
        }
        if (MasteryUiPolicy.requiresConfirmation(node, MasteryConfig.CLIENT.confirmUnlocks)) {
            confirmUnlock(index, node);
            return;
        }
        sendUnlock(index);
    }

    private void confirmUnlock(int index, MasteryProfile.Node node) {
        openPrompt(new MasteryPrompt(
                Text.translatable("screen.simplymastery.unlock.confirm.title",
                        Text.translatable(node.nameKey())),
                node.capstone()
                        ? Text.translatable("screen.simplymastery.unlock.confirm.capstone", node.cost())
                        : Text.translatable("screen.simplymastery.unlock.confirm.message", node.cost()),
                () -> sendUnlock(index)));
    }

    private void sendUnlock(int index) {
        MasteryState state = state(profile);
        MasteryProfile.Node node = profile.nodes().get(index);
        if (nodeState(profile, state, node) != MasteryNodeState.REACHABLE) {
            MasteryUiSounds.denied();
            return;
        }
        pendingActionId = ACTION_IDS.incrementAndGet();
        new UnlockNodePacket(handler.syncId, definitionEpoch, state.mutationRevision(),
                profile.id(), node.id(), pendingActionId).sendToServer();
    }

    private void confirmRespec() {
        if (client == null) return;
        MasteryState current = state(profile);
        if (current.unlockedNodeIds().isEmpty()) {
            MasteryUiSounds.denied();
            return;
        }
        openPrompt(new MasteryPrompt(
                Text.translatable("screen.simplymastery.respec.confirm.title"),
                Text.translatable("screen.simplymastery.respec.confirm.message",
                        MasteryConfig.SERVER.respecCostCount, respecPaymentName(),
                        current.spentPoints(profile)),
                this::requestRespec));
    }

    private void requestRespec() {
        MasteryState current = state(profile);
        pendingActionId = ACTION_IDS.incrementAndGet();
        UnlockNodePacket.respec(handler.syncId, definitionEpoch, current.mutationRevision(),
                profile.id(), pendingActionId).sendToServer();
    }

    private void rebuildProfile(MasteryProfile replacement) {
        layout = new MasteryLayout(replacement, width, height);
        branchRgb = new int[replacement.branches().size()];
        for (int i = 0; i < branchRgb.length; i++) {
            branchRgb[i] = replacement.branches().get(i).color() & 0xFFFFFF;
        }
        nodeIcons = new Identifier[replacement.nodes().size()];
        if (client != null) {
            for (int i = 0; i < nodeIcons.length; i++) {
                nodeIcons[i] = MasteryUiPolicy.iconTexture(replacement.nodes().get(i))
                        .filter(texture -> client.getResourceManager().getResource(texture).isPresent())
                        .orElse(null);
            }
        }
        animators = new NodeAnimators(layout.nodeCount());
        ownedSnapshot = new boolean[layout.nodeCount()];
        MasteryState state = state(replacement);
        for (int i = 0; i < ownedSnapshot.length; i++) {
            ownedSnapshot[i] = state.owns(replacement.nodes().get(i).id());
            animators.snapOwned(i, ownedSnapshot[i]);
        }
        masteryMeter.snap(ownedFraction(replacement, state));
        hoveredNode = -1;
        selectedNode = -1;
        cardNode = -1;
        entranceSeconds = 0.0F;
        canvasZoom = 1.0F;
        canvasPanX = 0.0F;
        canvasPanY = 0.0F;
        MasteryUiSounds.openView();
    }

    private void syncOwnership(MasteryProfile profile, MasteryState state) {
        for (int i = 0; i < ownedSnapshot.length; i++) {
            boolean owned = state.owns(profile.nodes().get(i).id());
            if (owned && !ownedSnapshot[i]) {
                animators.triggerBurst(i);
                flash = 1.0F;
                if (profile.nodes().get(i).capstone()) {
                    MasteryUiSounds.capstone();
                } else {
                    MasteryUiSounds.unlock();
                }
            }
            ownedSnapshot[i] = owned;
        }
    }

    private void consumeFeedback(MasteryProfile profile) {
        UnlockResult result = MasteryClientFeedback.result();
        long actionId = MasteryClientFeedback.actionId();
        if (result == null || actionId != pendingActionId || actionId == feedbackActionId) {
            return;
        }
        feedbackActionId = actionId;
        feedbackTimer = FEEDBACK_SECONDS;
        if (result != UnlockResult.SUCCESS) {
            MasteryUiSounds.denied();
        }
    }

    private void backToForge() {
        if (client == null) {
            return;
        }
        switchingView = true;
        MasteryScreenSwitch.run(handler,
                () -> client.setScreen(new RunicForgeScreen(handler, playerInventory, title)));
        switchingView = false;
    }

    private MasteryState state(MasteryProfile profile) {
        return MasteryStateAccess.read(authoritativeStack(), profile,
                Math.min(MasteryConfig.SERVER.verticalSliceStartingPoints,
                        MasteryConfig.SERVER.maximumEarnedPoints));
    }

    private ItemStack authoritativeStack() {
        return RunicForgeMasteryContext.stateStack(handler);
    }

    private ItemStack identityStack() {
        return RunicForgeMasteryContext.identityStack(handler);
    }

    private ItemStack displayStack() {
        return identityStack();
    }

    private static Text respecPaymentName() {
        if (MasteryConfig.SERVER.respecUsesTag) {
            return Text.literal("#" + MasteryConfig.SERVER.respecCostTag.get());
        }
        return Registries.ITEM.getOrEmpty(MasteryConfig.SERVER.respecCostItem.get())
                .map(item -> (Text) item.getName())
                .orElseGet(() -> Text.literal(MasteryConfig.SERVER.respecCostItem.get().toString()));
    }

    private static Text masteryProgress(MasteryProfile profile, MasteryState state) {
        int owned = 0;
        for (MasteryProfile.Node node : profile.nodes()) {
            if (state.owns(node.id())) {
                owned++;
            }
        }
        return Text.translatable("screen.simplymastery.mastery_progress", owned,
                MasteryUiPolicy.achievableNodes(profile, null));
    }

    /** The chrome accent. Per the design this is one colour; we take it from the first branch. */
    private int accentRgb() {
        return branchRgb.length > 0 ? branchRgb[0] : MasteryTheme.ACCENT;
    }

    private static Text masteryCount(MasteryProfile profile, MasteryState state) {
        int owned = 0;
        for (MasteryProfile.Node node : profile.nodes()) {
            if (state.owns(node.id())) {
                owned++;
            }
        }
        return Text.literal(owned + " / " + MasteryUiPolicy.achievableNodes(profile, null));
    }

    private static Text profileName(MasteryProfile profile) {
        return Text.translatable("profile.simplymastery." + profile.id().getPath());
    }

    private static float ownedFraction(MasteryProfile profile, MasteryState state) {
        int owned = 0;
        for (MasteryProfile.Node node : profile.nodes()) {
            if (state.owns(node.id())) {
                owned++;
            }
        }
        int total = MasteryUiPolicy.achievableNodes(profile, null);
        return total <= 0 ? 0.0F : owned / (float) total;
    }

    private static MasteryNodeState nodeState(MasteryProfile profile, MasteryState state,
                                               MasteryProfile.Node node) {
        return MasteryUiPolicy.nodeState(profile, state, node);
    }

    private static Text nodeStatusText(MasteryProfile profile, MasteryState state, MasteryProfile.Node node,
                                       MasteryNodeState nodeState) {
        if (nodeState == MasteryNodeState.LOCKED) {
            if (!state.unlockedNodeIds().containsAll(node.requires())) {
                return Text.translatable("screen.simplymastery.node_state.prerequisites", node.cost());
            }
            return Text.translatable("screen.simplymastery.node_state.points",
                    node.cost(), state.availablePoints(profile));
        }
        return Text.translatable("screen.simplymastery.node_state." + nodeState.name().toLowerCase(), node.cost());
    }

    private static Text prerequisiteText(MasteryState state, MasteryProfile.Node node) {
        if (node.requires().isEmpty()) {
            return Text.translatable("screen.simplymastery.narration.prerequisites.none");
        }
        long met = node.requires().stream().filter(state.unlockedNodeIds()::contains).count();
        return Text.translatable("screen.simplymastery.narration.prerequisites", met, node.requires().size());
    }

    private double toLayoutX(double screenX) {
        float center = (layout.canvasLeft + layout.canvasRight) * 0.5F;
        return (screenX - center - canvasPanX) / canvasZoom + center;
    }

    private double toLayoutY(double screenY) {
        float center = (layout.canvasTop + layout.canvasBottom) * 0.5F;
        return (screenY - center - canvasPanY) / canvasZoom + center - canvasEntranceOffset();
    }

    private boolean insideCanvas(double x, double y) {
        return x >= layout.canvasLeft && x <= layout.canvasRight
                && y >= layout.canvasTop && y <= layout.canvasBottom;
    }

    private void clampCanvasPan() {
        float maxX = (layout.canvasRight - layout.canvasLeft) * (canvasZoom - 1.0F) * 0.5F;
        float maxY = (layout.canvasBottom - layout.canvasTop) * (canvasZoom - 1.0F) * 0.5F;
        canvasPanX = Math.clamp(canvasPanX, -maxX, maxX);
        canvasPanY = Math.clamp(canvasPanY, -maxY, maxY);
    }

    private void revealSelectedNode(int index) {
        if (!layout.compact || canvasZoom <= 1.0F) {
            return;
        }
        float centerX = (layout.canvasLeft + layout.canvasRight) * 0.5F;
        float centerY = (layout.canvasTop + layout.canvasBottom) * 0.5F;
        float screenX = centerX + (layout.x(index) - centerX) * canvasZoom + canvasPanX;
        float screenY = centerY + (layout.y(index) + canvasEntranceOffset() - centerY) * canvasZoom + canvasPanY;
        float pad = layout.radius(index) * canvasZoom + 4.0F;
        if (screenX - pad < layout.canvasLeft) {
            canvasPanX += layout.canvasLeft - (screenX - pad);
        } else if (screenX + pad > layout.canvasRight) {
            canvasPanX -= screenX + pad - layout.canvasRight;
        }
        if (screenY - pad < layout.canvasTop) {
            canvasPanY += layout.canvasTop - (screenY - pad);
        } else if (screenY + pad > layout.canvasBottom) {
            canvasPanY -= screenY + pad - layout.canvasBottom;
        }
        clampCanvasPan();
    }

    private float canvasEntranceOffset() {
        float appear = MasteryTheme.easeOutCubic((entranceSeconds - 0.07F) / 0.18F);
        return (1.0F - appear) * 14.0F;
    }

    private float frameSeconds() {
        long now = Util.getMeasuringTimeMs();
        float step = Math.min(0.1F, (now - lastFrameMs) / 1000.0F);
        lastFrameMs = now;
        return step;
    }

    private static double seconds() {
        return Util.getMeasuringTimeMs() / 1000.0;
    }

    private static float motion() {
        return MasteryConfig.CLIENT.reducedMotion ? 0.0F : MasteryConfig.CLIENT.motionIntensity;
    }

}
