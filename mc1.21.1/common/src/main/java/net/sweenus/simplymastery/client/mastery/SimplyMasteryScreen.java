package net.sweenus.simplymastery.client.mastery;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.sweenus.simplymastery.client.mastery.ui.Anim;
import net.sweenus.simplymastery.client.mastery.ui.GlassButtonWidget;
import net.sweenus.simplymastery.client.mastery.ui.MasteryLayout;
import net.sweenus.simplymastery.client.mastery.ui.MasteryTheme;
import net.sweenus.simplymastery.client.mastery.ui.MasteryUiSounds;
import net.sweenus.simplymastery.client.mastery.ui.NodeAnimators;
import net.sweenus.simplymastery.client.mastery.ui.UiDraw;
import net.sweenus.simplymastery.config.MasteryConfig;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
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
    private static final int CARD_LINES = 5;

    private final PlayerInventory playerInventory;
    private final Anim masteryMeter = new Anim(0.0F, 7.0F);
    private final Anim cardFade = new Anim(0.0F, 20.0F);

    private MasteryLayout layout;
    private NodeAnimators animators;
    private int[] branchRgb = new int[0];
    private boolean[] ownedSnapshot;
    private int hoveredNode = -1;
    private int selectedNode = -1;
    private int cardNode = -1;
    private long pendingActionId = -1L;
    private long feedbackActionId = -1L;
    private float feedbackTimer;
    private float entranceSeconds;
    private float flash;
    private long lastFrameMs = Util.getMeasuringTimeMs();
    private boolean switchingView;

    public SimplyMasteryScreen(RunicForgeScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title);
        this.playerInventory = playerInventory;
    }

    @Override
    protected void init() {
        super.init();
        MasteryProfile profile = MasteryProfile.STORMS_EDGE;
        layout = new MasteryLayout(profile, width, height);

        branchRgb = new int[profile.branches().size()];
        for (int i = 0; i < branchRgb.length; i++) {
            branchRgb[i] = profile.branches().get(i).color() & 0xFFFFFF;
        }
        if (animators == null) {
            animators = new NodeAnimators(layout.nodeCount());
            entranceSeconds = 0.0F;
            MasteryUiSounds.openView();
        }
        if (ownedSnapshot == null) {
            MasteryState state = state(profile);
            ownedSnapshot = new boolean[layout.nodeCount()];
            for (int i = 0; i < ownedSnapshot.length; i++) {
                ownedSnapshot[i] = state.owns(profile.nodes().get(i).id());
                animators.snapOwned(i, ownedSnapshot[i]);
            }
            masteryMeter.snap(ownedFraction(profile, state));
        }

        int buttonHeight = 18;
        int buttonY = layout.headerTop + (layout.headerBottom - layout.headerTop - buttonHeight) / 2;
        int backWidth = backButtonWidth();
        int closeWidth = closeButtonWidth();
        addDrawableChild(new GlassButtonWidget(layout.headerLeft + 7, buttonY, backWidth, buttonHeight,
                Text.translatable("screen.simplymastery.back"), button -> backToForge(), MasteryTheme.ACCENT));
        addDrawableChild(new GlassButtonWidget(layout.headerRight - 7 - closeWidth, buttonY, closeWidth, buttonHeight,
                Text.translatable("screen.simplymastery.close"), button -> close(), MasteryTheme.DANGER));
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
        if (MasteryProfile.resolve(authoritativeStack()).isEmpty()) {
            backToForge();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MasteryProfile profile = MasteryProfile.STORMS_EDGE;
        MasteryState state = state(profile);
        boolean instant = MasteryConfig.CLIENT.reducedMotion;
        float step = frameSeconds();
        entranceSeconds = instant ? 2.0F : Math.min(2.0F, entranceSeconds + step);
        flash = Math.max(0.0F, flash - step / 0.45F);
        feedbackTimer = Math.max(0.0F, feedbackTimer - step);

        int previousHover = hoveredNode;
        hoveredNode = layout.nodeAt(mouseX, mouseY);
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
        if (hoveredNode >= 0) {
            cardNode = hoveredNode;
        } else if (selectedNode >= 0) {
            cardNode = selectedNode;
        }

        drawBackground(context, delta, mouseX, mouseY);
        drawCanvas(context, profile, state);
        if (layout.compact) {
            drawCompactBadge(context);
        } else {
            drawShowcase(context, profile, state);
        }
        drawHeader(context, profile, state);
        for (Element child : children()) {
            if (child instanceof Drawable drawable) {
                drawable.render(context, mouseX, mouseY, delta);
            }
        }

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0.0F, 0.0F, 420.0F);
        drawDetailCard(context, profile, state, mouseX, mouseY);
        drawStatus(context);
        if (flash > 0.0F) {
            context.fill(0, 0, width, height,
                    MasteryTheme.argb(MasteryTheme.ACCENT, 0.10F * flash * flash * motion()));
        }
        matrices.pop();
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        float appear = MasteryTheme.easeOutCubic(entranceSeconds / 0.28F);
        float dim = MasteryConfig.CLIENT.backgroundDim;
        context.fillGradient(0, 0, width, height,
                MasteryTheme.argb(MasteryTheme.BACKDROP_TOP, dim * appear),
                MasteryTheme.argb(MasteryTheme.BACKDROP_BOTTOM, Math.min(0.97F, dim + 0.2F) * appear));
        UiDraw.motes(context, width, height, seconds(), 48, MasteryTheme.ACCENT, 0.9F * motion() * appear);
        for (int y = 0; y < height; y += 3) {
            context.fill(0, y, width, y + 1, MasteryTheme.argb(0x000000, 0.05F * appear));
        }
        UiDraw.vignette(context, width, height, 0x02040A, 0.5F * appear);

        float panels = MasteryTheme.easeOutCubic((entranceSeconds - 0.08F) / 0.32F);
        UiDraw.glassPanel(context, layout.headerLeft, layout.headerTop, layout.headerRight, layout.headerBottom,
                4, MasteryTheme.ACCENT, panels);
        UiDraw.glassPanel(context, layout.canvasLeft, layout.canvasTop, layout.canvasRight, layout.canvasBottom,
                6, MasteryTheme.ACCENT, panels);
        if (!layout.compact) {
            UiDraw.glassPanel(context, layout.showcaseLeft, layout.showcaseTop, layout.showcaseRight,
                    layout.showcaseBottom, 6, MasteryTheme.GOLD, panels);
        }
    }

    // --- header -------------------------------------------------------------

    private void drawHeader(DrawContext context, MasteryProfile profile, MasteryState state) {
        float alpha = MasteryTheme.easeOutCubic((entranceSeconds - 0.12F) / 0.3F);
        if (alpha <= 0.01F) {
            return;
        }
        ItemStack stack = displayStack();
        int headerHeight = layout.headerBottom - layout.headerTop;
        boolean roomy = headerHeight >= 40;
        int textLeft = contentLeft() + (layout.compact ? 22 : 0);

        float nameScale = roomy ? 1.35F : 1.0F;
        float nameY = roomy ? layout.headerTop + 8 : layout.headerTop + (headerHeight - 8) * 0.5F;
        UiDraw.text(context, textRenderer, stack.getName(), textLeft, nameY,
                MasteryTheme.argb(MasteryTheme.INK, alpha), nameScale, true);
        if (roomy) {
            int nameWidth = Math.round(textRenderer.getWidth(stack.getName()) * nameScale);
            UiDraw.hGradient(context, textLeft, Math.round(nameY) + 12, textLeft + nameWidth,
                    Math.round(nameY) + 13, MasteryTheme.argb(MasteryTheme.ACCENT, 0.7F * alpha),
                    MasteryTheme.argb(MasteryTheme.ACCENT, 0.0F));
            UiDraw.text(context, textRenderer, masteryProgress(profile, state), textLeft, nameY + 16,
                    MasteryTheme.argb(MasteryTheme.INK_MUTED, alpha), 1.0F, false);
        }

        int available = state.availablePoints(profile);
        int earned = state.earnedPoints();
        Text points = Text.translatable("screen.simplymastery.points", available, earned);
        int right = layout.headerRight - 7 - closeButtonWidth() - 12;
        int pointsColor = available > 0 ? MasteryTheme.GOLD : MasteryTheme.INK_MUTED;
        if (roomy) {
            int pipSize = 6;
            int pipGap = 2;
            int shown = Math.min(earned, 12);
            int pipsWidth = shown * (pipSize + pipGap) - pipGap;
            UiDraw.pips(context, right - pipsWidth, layout.headerTop + 9, shown, Math.min(available, shown),
                    pipSize, pipGap, MasteryTheme.GOLD, alpha);
            UiDraw.rightText(context, textRenderer, points, right, layout.headerTop + 24,
                    MasteryTheme.argb(pointsColor, alpha), 1.0F, false);
        } else {
            UiDraw.rightText(context, textRenderer, points, right, layout.headerTop + (headerHeight - 8) * 0.5F,
                    MasteryTheme.argb(pointsColor, alpha), 1.0F, false);
        }
    }

    // --- canvas -------------------------------------------------------------

    private void drawCanvas(DrawContext context, MasteryProfile profile, MasteryState state) {
        float appear = MasteryTheme.easeOutCubic((entranceSeconds - 0.14F) / 0.34F);
        if (appear <= 0.01F) {
            return;
        }
        context.enableScissor(layout.canvasLeft + 1, layout.canvasTop + 1,
                layout.canvasRight - 1, layout.canvasBottom - 1);
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0.0F, (1.0F - appear) * 14.0F, 0.0F);

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

        matrices.pop();
        context.disableScissor();
    }

    private void drawLanes(DrawContext context, MasteryProfile profile, MasteryState state, float alpha) {
        for (int lane = 0; lane < layout.laneCount(); lane++) {
            int rgb = branchRgb[lane];
            int top = Math.round(layout.laneBandTop(lane));
            int bottom = Math.round(layout.laneBandBottom(lane));
            context.fillGradient(layout.canvasLeft + 2, top, layout.canvasRight - 2, bottom,
                    MasteryTheme.argb(rgb, 0.045F * alpha), MasteryTheme.argb(rgb, 0.0F));
            UiDraw.hGradient(context, layout.canvasLeft + 2, top, layout.canvasRight - 2, top + 1,
                    MasteryTheme.argb(rgb, 0.22F * alpha), MasteryTheme.argb(rgb, 0.0F));

            MasteryProfile.Branch branch = profile.branches().get(lane);
            int owned = 0;
            int total = 0;
            for (MasteryProfile.Node node : profile.nodes()) {
                if (node.branch().equals(branch.id())) {
                    total++;
                    if (state.owns(node.id())) {
                        owned++;
                    }
                }
            }
            float labelY = layout.laneLabelY(lane);
            float pipX = layout.canvasLeft + 9;
            UiDraw.shape(context, UiDraw.SHAPE_DIAMOND, pipX, labelY + 4.0F, 3.0F,
                    MasteryTheme.argb(rgb, (owned > 0 ? 0.95F : 0.4F) * alpha));
            Text name = Text.translatable(branch.nameKey());
            UiDraw.text(context, textRenderer, name, pipX + 7.0F, labelY,
                    MasteryTheme.argb(rgb, 0.92F * alpha), 1.0F, true);
            UiDraw.text(context, textRenderer,
                    Text.translatable("screen.simplymastery.branch_progress", owned, total),
                    pipX + 12.0F + textRenderer.getWidth(name), labelY,
                    MasteryTheme.argb(MasteryTheme.INK_MUTED, 0.9F * alpha), 1.0F, false);
        }
    }

    private void drawEdges(DrawContext context, MasteryProfile profile, MasteryState state) {
        double time = seconds();
        List<MasteryLayout.Edge> edges = layout.edges();
        for (MasteryLayout.Edge edge : edges) {
            float reveal = edgeReveal(edge);
            if (reveal > 0.01F) {
                UiDraw.pathDashed(context, edge.path(), edge.points(), edge.length(),
                        edge.length() * reveal, 3.0F, 5.0F, 0.0F, 2.0F,
                        MasteryTheme.argb(MasteryTheme.SLATE, 0.55F * reveal));
            }
        }
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
                UiDraw.pathRange(context, edge.path(), edge.points(), 0.0F, drawn, 3.0F,
                        MasteryTheme.argb(rgb, 0.8F * reveal));
                UiDraw.pathRange(context, edge.path(), edge.points(), 0.0F, drawn, 1.0F,
                        MasteryTheme.argb(MasteryTheme.mix(rgb, 0xFFFFFF, 0.6F), 0.45F * reveal));
                if (linked) {
                    float cycle = edge.length() + 70.0F;
                    float head = (float) ((time * 46.0 * motion() + index * 23.0) % cycle);
                    UiDraw.pathRange(context, edge.path(), edge.points(), head - 20.0F, head, 3.0F,
                            MasteryTheme.argb(MasteryTheme.mix(rgb, 0xFFFFFF, 0.55F), 0.55F * reveal));
                }
            } else if (parentOwned && nodeState(profile, state, child) != NodeState.CONFLICT) {
                float shimmer = 0.24F + 0.2F * MasteryTheme.pulse(time, 1.6);
                UiDraw.pathDashed(context, edge.path(), edge.points(), edge.length(), limit,
                        5.0F, 4.0F, (float) (-time * 14.0 * motion()), 2.0F,
                        MasteryTheme.argb(rgb, shimmer * reveal));
            }
        }
    }

    private float edgeReveal(MasteryLayout.Edge edge) {
        return MasteryTheme.easeOutCubic(
                (entranceSeconds - 0.22F - layout.depth(edge.child()) * 0.06F) / 0.32F);
    }

    private void drawNode(DrawContext context, MasteryProfile profile, MasteryState state, int index) {
        MasteryProfile.Node node = profile.nodes().get(index);
        float reveal = MasteryTheme.easeOutBack((entranceSeconds - 0.26F
                - layout.branch(index) * 0.045F - layout.depth(index) * 0.07F) / 0.3F);
        if (reveal <= 0.01F) {
            return;
        }
        float alpha = Math.min(1.0F, reveal);
        float hover = animators.hover(index);
        float focus = animators.focus(index);
        float owned = animators.owned(index);
        float burst = animators.burst(index);
        NodeState nodeState = nodeState(profile, state, node);
        boolean capstone = node.capstone();
        int kind = capstone ? UiDraw.SHAPE_DIAMOND : UiDraw.SHAPE_HEX;
        int rgb = branchRgb[layout.branch(index)];
        double time = seconds();

        float lift = 2.0F * Math.max(hover, focus * 0.6F) * motion();
        float cx = layout.x(index);
        float cy = layout.y(index) - lift;
        float radius = layout.radius(index) * Math.max(0.2F, reveal) * (1.0F + 0.1F * hover);

        int baseFill = switch (nodeState) {
            case OWNED, REACHABLE -> MasteryTheme.mix(0x101823, rgb, 0.16F);
            case CONFLICT -> 0x1B1219;
            case LOCKED -> 0x0F1620;
        };
        int ownedFill = MasteryTheme.mix(rgb, 0x0A111A, 0.42F);
        int fill = MasteryTheme.mix(baseFill, ownedFill, owned);
        int rim = switch (nodeState) {
            case OWNED -> rgb;
            case REACHABLE -> MasteryTheme.mix(rgb, 0xFFFFFF, 0.15F);
            case CONFLICT -> MasteryTheme.DANGER;
            case LOCKED -> MasteryTheme.SLATE;
        };

        float ambient = switch (nodeState) {
            case OWNED -> 0.55F + 0.18F * MasteryTheme.pulse(time + index, 2.6) * motion();
            case REACHABLE -> 0.3F + 0.28F * MasteryTheme.pulse(time + index * 0.4, 1.5) * motion();
            default -> 0.05F;
        };
        float glow = (ambient + hover * 0.75F + focus * 0.4F) * alpha;
        UiDraw.shapeGlow(context, kind, cx, cy, radius * 2.3F, rgb, 0.05F * glow, 5);

        UiDraw.shape(context, kind, cx, cy, radius, MasteryTheme.argb(fill, 0.94F * alpha));
        UiDraw.shape(context, kind, cx, cy - radius * 0.3F, radius * 0.66F,
                MasteryTheme.argb(MasteryTheme.mix(fill, 0xFFFFFF, 0.35F), 0.16F * alpha));
        UiDraw.shapeOutline(context, kind, cx, cy, radius, capstone ? 2.0F : 1.5F,
                MasteryTheme.argb(rim, (0.7F + 0.3F * Math.max(hover, focus)) * alpha));
        if (capstone) {
            UiDraw.shapeOutline(context, kind, cx, cy, radius - 4.5F, 1.0F,
                    MasteryTheme.argb(rim, 0.45F * alpha));
            float ticks = Math.max(hover, focus);
            if (ticks > 0.01F) {
                UiDraw.arcTicks(context, cx, cy, radius + 4.0F, 8, (float) (time * 18.0 * motion()), 3.0F, 1.0F,
                        MasteryTheme.argb(rim, 0.5F * ticks * alpha));
            }
        }
        if (hover > 0.01F || focus > 0.01F) {
            UiDraw.shapeOutline(context, kind, cx, cy, radius + 2.0F, 1.0F,
                    MasteryTheme.argb(0xFFFFFF, 0.5F * Math.max(hover, focus) * alpha));
        }
        if (focus > 0.01F) {
            UiDraw.arcTicks(context, cx, cy, radius + 5.0F, 4, (float) (45.0 + time * 34.0 * motion()),
                    4.0F, 1.5F, MasteryTheme.argb(0xFFFFFF, 0.75F * focus * alpha));
        }

        float glyphSize = radius * 0.46F;
        int glyphColor = MasteryTheme.argb(nodeState == NodeState.LOCKED
                ? MasteryTheme.INK_MUTED : MasteryTheme.mix(rim, 0xFFFFFF, 0.4F), 0.9F * alpha);
        switch (nodeState) {
            case OWNED -> {
                if (capstone) {
                    sigil(context, node.id(), cx, cy - radius * 0.18F, glyphSize, glyphColor);
                    checkGlyph(context, cx, cy + radius * 0.44F, glyphSize * 0.6F, glyphColor);
                } else {
                    checkGlyph(context, cx, cy, glyphSize, glyphColor);
                }
            }
            case CONFLICT -> {
                sigil(context, node.id(), cx, cy, glyphSize, MasteryTheme.argb(MasteryTheme.SLATE, 0.6F * alpha));
                UiDraw.segment(context, cx - radius * 0.6F, cy + radius * 0.6F,
                        cx + radius * 0.6F, cy - radius * 0.6F, 2.0F,
                        MasteryTheme.argb(MasteryTheme.DANGER, 0.9F * alpha));
            }
            case LOCKED -> lockGlyph(context, cx, cy, glyphSize, glyphColor);
            case REACHABLE -> sigil(context, node.id(), cx, cy, glyphSize, glyphColor);
        }

        if (burst > 0.0F) {
            float progress = 1.0F - burst;
            UiDraw.shapeOutline(context, kind, cx, cy, radius * (1.0F + progress * 2.2F), 2.0F,
                    MasteryTheme.argb(MasteryTheme.mix(rgb, 0xFFFFFF, 0.5F), 0.7F * burst));
            UiDraw.arcTicks(context, cx, cy, radius + progress * 24.0F, 8, progress * 50.0F,
                    6.0F * burst, 1.5F, MasteryTheme.argb(rgb, 0.8F * burst));
        }
    }

    // --- showcase -----------------------------------------------------------

    private void drawShowcase(DrawContext context, MasteryProfile profile, MasteryState state) {
        float appear = MasteryTheme.easeOutCubic((entranceSeconds - 0.18F) / 0.36F);
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
        int centerX = (layout.showcaseLeft + layout.showcaseRight) / 2;
        int footerHeight = showProfile ? 60 : 49;
        int contentTop = layout.showcaseTop + 8;
        int contentBottom = layout.showcaseBottom - footerHeight;
        int centerY = (contentTop + contentBottom) / 2;
        float aura = Math.min((layout.showcaseRight - layout.showcaseLeft) * 0.46F,
                (contentBottom - contentTop) * 0.45F);
        float progress = masteryMeter.value();

        float breathe = 1.0F + 0.03F * (float) Math.sin(time * 0.55) * intensity;
        UiDraw.shapeGlow(context, UiDraw.SHAPE_DISC, centerX, centerY, aura * 1.2F * breathe,
                MasteryTheme.ACCENT, 0.018F * appear * (0.6F + 0.5F * progress), 14);
        UiDraw.ring(context, centerX, centerY, aura * 0.94F, 1.0F,
                MasteryTheme.argb(MasteryTheme.ACCENT, 0.14F * appear));
        UiDraw.arcTicks(context, centerX, centerY, aura * 0.84F, 18, (float) (time * 9.0 * intensity),
                4.0F, 1.0F, MasteryTheme.argb(MasteryTheme.ACCENT, (0.12F + 0.22F * progress) * appear));
        UiDraw.arcTicks(context, centerX, centerY, aura * 1.04F, 12, (float) (-time * 6.0 * intensity),
                6.0F, 1.0F, MasteryTheme.argb(MasteryTheme.GOLD, (0.08F + 0.18F * progress) * appear));
        UiDraw.softFloor(context, centerX, centerY + aura * 0.72F, aura * 0.8F, aura * 0.26F,
                MasteryTheme.ACCENT, 0.16F * appear);

        float driftX = (float) Math.sin(time * 0.37 + 1.2) * 3.0F * intensity;
        float driftY = ((float) Math.sin(time * 0.90) * 6.0F + (float) Math.sin(time * 0.27) * 2.0F) * intensity;
        float scale = Math.clamp(aura * 1.3F / 16.0F, 1.5F, 12.0F)
                * (1.0F + (float) Math.sin(time * 0.55) * 0.012F * intensity);
        matrices.push();
        matrices.translate(centerX - 8.0F * scale + driftX, centerY - 8.0F * scale + driftY, 140.0F);
        matrices.scale(scale, scale, 1.0F);
        context.drawItem(stack, 0, 0);
        matrices.pop();

        int nameY = layout.showcaseBottom - footerHeight + 6;
        UiDraw.centeredText(context, textRenderer, weaponName, centerX, nameY,
                MasteryTheme.argb(MasteryTheme.INK, appear), 1.0F, true);
        if (showProfile) {
            UiDraw.centeredText(context, textRenderer, profileLabel, centerX, nameY + 11,
                    MasteryTheme.argb(MasteryTheme.ACCENT, 0.85F * appear), 1.0F, false);
        }

        int barLeft = layout.showcaseLeft + 12;
        int barRight = layout.showcaseRight - 12;
        int barTop = nameY + (showProfile ? 26 : 15);
        UiDraw.bar(context, barLeft, barTop, barRight, barTop + 6, progress, MasteryTheme.ACCENT, appear);
        UiDraw.centeredText(context, textRenderer, masteryProgress(profile, state), centerX, barTop + 10,
                MasteryTheme.argb(MasteryTheme.INK_MUTED, 0.9F * appear), 1.0F, false);
        matrices.pop();
    }

    private void drawCompactBadge(DrawContext context) {
        float appear = MasteryTheme.easeOutCubic((entranceSeconds - 0.18F) / 0.36F);
        if (appear <= 0.01F) {
            return;
        }
        int badgeX = contentLeft();
        int badgeY = layout.headerTop + (layout.headerBottom - layout.headerTop) / 2 - 8;
        UiDraw.shapeGlow(context, UiDraw.SHAPE_DISC, badgeX + 8, badgeY + 8, 16.0F,
                MasteryTheme.ACCENT, 0.05F * appear, 4);
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0.0F, 0.0F, 140.0F);
        context.drawItem(displayStack(), badgeX, badgeY);
        matrices.pop();
    }

    // --- detail card --------------------------------------------------------

    private void drawDetailCard(DrawContext context, MasteryProfile profile, MasteryState state,
                                int mouseX, int mouseY) {
        float alpha = cardFade.value();
        if (alpha <= 0.01F || cardNode < 0) {
            return;
        }
        MasteryProfile.Node node = profile.nodes().get(cardNode);
        NodeState nodeState = nodeState(profile, state, node);
        int rgb = branchRgb[layout.branch(cardNode)];
        int cardWidth = layout.dockDetail
                ? layout.canvasRight - layout.canvasLeft
                : Math.clamp(width / 4, 196, 288);
        List<OrderedText> lines = textRenderer.wrapLines(Text.translatable(node.descriptionKey()), cardWidth - 22);
        int chrome = 10 + 11 + 11 + 9 + 12;
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

        UiDraw.chamferFill(context, cardX + 2, cardY + 3, cardX + cardWidth + 2, cardY + cardHeight + 3, 5,
                MasteryTheme.argb(0x000000, 0.4F * alpha));
        UiDraw.chamferGradient(context, cardX, cardY, cardX + cardWidth, cardY + cardHeight, 5,
                MasteryTheme.argb(MasteryTheme.mix(MasteryTheme.PANEL_TOP, rgb, 0.08F), 0.95F * alpha),
                MasteryTheme.argb(0x05080E, 0.97F * alpha));
        UiDraw.chamferOutline(context, cardX, cardY, cardX + cardWidth, cardY + cardHeight, 5,
                MasteryTheme.argb(rgb, 0.75F * alpha));
        UiDraw.hGradient(context, cardX + 5, cardY + 1, cardX + cardWidth - 5, cardY + 2,
                MasteryTheme.argb(rgb, 0.6F * alpha), MasteryTheme.argb(rgb, 0.0F));

        int textX = cardX + 10;
        UiDraw.text(context, textRenderer, Text.translatable(node.nameKey()), textX, cardY + 8,
                MasteryTheme.argb(MasteryTheme.INK, alpha), 1.0F, true);
        Text branchName = Text.translatable(profile.branches().get(layout.branch(cardNode)).nameKey());
        UiDraw.text(context, textRenderer, branchName, textX, cardY + 19,
                MasteryTheme.argb(rgb, 0.85F * alpha), 1.0F, false);
        if (node.capstone()) {
            UiDraw.rightText(context, textRenderer, Text.translatable("screen.simplymastery.capstone"),
                    cardX + cardWidth - 10, cardY + 19, MasteryTheme.argb(MasteryTheme.GOLD, 0.85F * alpha),
                    1.0F, false);
        }
        for (int i = 0; i < lineCount; i++) {
            UiDraw.text(context, textRenderer, lines.get(i), textX, cardY + 32 + i * 10,
                    MasteryTheme.argb(MasteryTheme.INK_DIM, 0.95F * alpha), 1.0F, false);
        }
        int footerY = cardY + cardHeight - 15;
        UiDraw.hGradient(context, textX, footerY - 5, cardX + cardWidth - 10, footerY - 4,
                MasteryTheme.argb(rgb, 0.35F * alpha), MasteryTheme.argb(rgb, 0.0F));
        UiDraw.pips(context, textX, footerY + 1, node.cost(), node.cost(), 6, 2, MasteryTheme.GOLD, alpha);
        int stateColor = switch (nodeState) {
            case REACHABLE -> MasteryTheme.GOLD;
            case OWNED -> MasteryTheme.SUCCESS;
            case CONFLICT -> MasteryTheme.DANGER;
            case LOCKED -> MasteryTheme.INK_MUTED;
        };
        UiDraw.rightText(context, textRenderer,
                Text.translatable("screen.simplymastery.node_state." + nodeState.name().toLowerCase(), node.cost()),
                cardX + cardWidth - 10, footerY, MasteryTheme.argb(stateColor, alpha), 1.0F, false);
        matrices.pop();
    }

    // --- status -------------------------------------------------------------

    private void drawStatus(DrawContext context) {
        float appear = MasteryTheme.easeOutCubic((entranceSeconds - 0.3F) / 0.3F);
        if (appear <= 0.01F) {
            return;
        }
        UnlockResult result = MasteryClientFeedback.result();
        if (result != null && feedbackTimer > 0.0F) {
            float fade = Math.min(1.0F, feedbackTimer / 0.5F)
                    * Math.min(1.0F, (FEEDBACK_SECONDS - feedbackTimer) / 0.12F);
            boolean success = result == UnlockResult.SUCCESS;
            int rgb = success ? MasteryTheme.SUCCESS : MasteryTheme.DANGER;
            float lift = (1.0F - Math.min(1.0F, (FEEDBACK_SECONDS - feedbackTimer) / 0.25F)) * 5.0F * motion();
            Text message = Text.translatable("message.simplymastery.unlock." + result.name().toLowerCase());
            float messageY = layout.statusTop + 1 - lift;
            UiDraw.shape(context, UiDraw.SHAPE_DIAMOND, layout.statusLeft + 4.0F, messageY + 4.0F, 3.0F,
                    MasteryTheme.argb(rgb, 0.95F * fade));
            UiDraw.text(context, textRenderer, message, layout.statusLeft + 11.0F, messageY,
                    MasteryTheme.argb(rgb, fade), 1.0F, true);
        }
        UiDraw.rightText(context, textRenderer, Text.translatable("screen.simplymastery.hint"),
                layout.statusRight, layout.statusBottom - 9,
                MasteryTheme.argb(MasteryTheme.INK_MUTED, 0.75F * appear), 1.0F, false);
    }

    // --- glyphs -------------------------------------------------------------

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
        UiDraw.ring(context, cx, top, size * 0.55F, 1.0F, argb);
    }

    // --- input --------------------------------------------------------------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Element child : children()) {
            if (child.mouseClicked(mouseX, mouseY, button)) {
                setFocused(child);
                return true;
            }
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            int clicked = layout.nodeAt(mouseX, mouseY);
            if (clicked >= 0) {
                if (selectedNode == clicked) {
                    requestUnlock(clicked);
                } else {
                    selectedNode = clicked;
                    MasteryUiSounds.select();
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
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
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
                selectedNode = next;
                MasteryUiSounds.select();
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
    public void removed() {
        if (!switchingView) {
            super.removed();
        }
    }

    // --- state --------------------------------------------------------------

    private void requestUnlock(int index) {
        MasteryProfile profile = MasteryProfile.STORMS_EDGE;
        MasteryState state = state(profile);
        MasteryProfile.Node node = profile.nodes().get(index);
        if (nodeState(profile, state, node) != NodeState.REACHABLE) {
            MasteryUiSounds.denied();
            return;
        }
        pendingActionId = ACTION_IDS.incrementAndGet();
        new UnlockNodePacket(handler.syncId, MasteryProfile.DEFINITION_EPOCH, state.mutationRevision(),
                profile.id(), node.id(), pendingActionId).sendToServer();
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
        return handler.getForgeInventory().getStack(RunicForgeScreenHandler.WEAPON_SLOT);
    }

    private ItemStack displayStack() {
        ItemStack preview = handler.getPreviewStack();
        return preview.isEmpty() ? authoritativeStack() : preview;
    }

    private int backButtonWidth() {
        return Math.min(104, Math.max(64, width / 7));
    }

    private int closeButtonWidth() {
        return Math.min(64, Math.max(46, width / 12));
    }

    private int contentLeft() {
        return layout.headerLeft + 7 + backButtonWidth() + 13;
    }

    private static Text masteryProgress(MasteryProfile profile, MasteryState state) {
        int owned = 0;
        for (MasteryProfile.Node node : profile.nodes()) {
            if (state.owns(node.id())) {
                owned++;
            }
        }
        return Text.translatable("screen.simplymastery.mastery_progress", owned, profile.nodes().size());
    }

    private static Text profileName(MasteryProfile profile) {
        return Text.translatable("profile." + profile.id().getNamespace() + "." + profile.id().getPath());
    }

    private static float ownedFraction(MasteryProfile profile, MasteryState state) {
        int owned = 0;
        for (MasteryProfile.Node node : profile.nodes()) {
            if (state.owns(node.id())) {
                owned++;
            }
        }
        return profile.nodes().isEmpty() ? 0.0F : owned / (float) profile.nodes().size();
    }

    private NodeState nodeState(MasteryProfile profile, MasteryState state, MasteryProfile.Node node) {
        if (state.owns(node.id())) {
            return NodeState.OWNED;
        }
        if (!node.choiceGroup().isEmpty() && profile.nodes().stream()
                .filter(other -> node.choiceGroup().equals(other.choiceGroup()))
                .anyMatch(other -> state.owns(other.id()))) {
            return NodeState.CONFLICT;
        }
        return state.unlockedNodeIds().containsAll(node.requires()) && state.availablePoints(profile) >= node.cost()
                ? NodeState.REACHABLE
                : NodeState.LOCKED;
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

    private enum NodeState {
        LOCKED,
        REACHABLE,
        OWNED,
        CONFLICT
    }
}
