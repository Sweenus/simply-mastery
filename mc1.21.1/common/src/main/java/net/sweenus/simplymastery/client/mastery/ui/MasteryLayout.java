package net.sweenus.simplymastery.client.mastery.ui;

import net.sweenus.simplymastery.mastery.definition.MasteryProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MasteryLayout {

    public record Edge(int parent, int child, float[] path, int points, float length) {
    }

    public final MasteryProfile profile;
    public final MasteryChrome.Frame frame;
    public final int screenWidth;
    public final int screenHeight;
    public final boolean compact;
    public final boolean dockDetail;

    public final int headerLeft;
    public final int headerTop;
    public final int headerRight;
    public final int headerBottom;
    public final int canvasLeft;
    public final int canvasTop;
    public final int canvasRight;
    public final int canvasBottom;
    public final int showcaseLeft;
    public final int showcaseTop;
    public final int showcaseRight;
    public final int showcaseBottom;
    public final int statusLeft;
    public final int statusTop;
    public final int statusRight;
    public final int statusBottom;
    public final int dockHeight;

    public final int backButtonX;
    public final int backButtonWidth;
    public final int rewardsButtonX;
    public final int rewardsButtonWidth;
    public final int respecButtonX;
    public final int respecButtonWidth;
    public final int closeButtonX;
    public final int closeButtonWidth;
    public final int buttonY;
    public final int headerContentLeft;
    public final int headerContentRight;
    public final int laneLabelRight;

    private final float[] nodeX;
    private final float[] nodeY;
    private final float[] nodeRadius;
    private final float[] nodeHit;
    private final int[] nodeBranch;
    private final int[] nodeDepth;
    private final float[] laneBandTop;
    private final float[] laneBandBottom;
    private final float[] laneRuleY;
    private final List<Edge> edges = new ArrayList<>();
    private final Map<String, Integer> indexById = new HashMap<>();

    public MasteryLayout(MasteryProfile profile, int screenWidth, int screenHeight) {
        this.profile = profile;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        List<MasteryProfile.Node> nodes = profile.nodes();
        List<MasteryProfile.Branch> branches = profile.branches();
        for (int i = 0; i < nodes.size(); i++) {
            indexById.put(nodes.get(i).id(), i);
        }

        this.frame = MasteryChrome.frame(screenWidth, screenHeight);
        headerLeft = frame.headerLeft();
        headerTop = frame.headerTop();
        headerRight = frame.headerRight();
        headerBottom = frame.headerBottom();

        backButtonWidth = frame.backButtonWidth();
        closeButtonWidth = frame.closeButtonWidth();
        respecButtonWidth = frame.respecButtonWidth();
        rewardsButtonWidth = frame.rewardsButtonWidth();
        backButtonX = frame.backButtonX();
        closeButtonX = frame.closeButtonX();
        respecButtonX = frame.respecButtonX();
        rewardsButtonX = frame.rewardsButtonX();
        headerContentLeft = frame.headerContentLeft();
        headerContentRight = frame.headerContentRight();
        buttonY = frame.buttonY();

        compact = frame.compact();
        dockDetail = frame.dockDetail();
        dockHeight = frame.dockHeight();

        statusTop = frame.statusTop();
        statusBottom = frame.statusBottom();
        statusLeft = frame.statusLeft();
        statusRight = frame.statusRight();

        canvasLeft = frame.canvasLeft();
        canvasTop = frame.canvasTop();
        canvasRight = frame.canvasRight();
        canvasBottom = frame.canvasBottom();
        showcaseLeft = frame.showcaseLeft();
        showcaseTop = frame.showcaseTop();
        showcaseRight = frame.showcaseRight();
        showcaseBottom = frame.showcaseBottom();

        laneLabelRight = canvasLeft + Math.clamp(Math.round((canvasRight - canvasLeft) * 0.2F), 46, 84);

        int laneCount = Math.max(1, branches.size());
        float lanePad = 5.0F;
        float laneHeight = (canvasBottom - canvasTop - lanePad * 2.0F) / laneCount;
        float bandPad = Math.min(5.0F, laneHeight * 0.1F);
        float bandHeight = laneHeight - bandPad * 2.0F;
        int capstoneRadius = Math.round(Math.min(Math.clamp(laneHeight * 0.26F, 11.0F, 22.0F),
                Math.max(6.0F, (bandHeight - 6.0F) / 4.0F)));
        float smallRadius = Math.clamp(capstoneRadius * 0.58F, 5.0F, 13.0F);
        float laneSpread = Math.max(0.0F, Math.min(bandHeight * 0.5F - capstoneRadius - 1.0F,
                capstoneRadius * 2.6F));
        float branchSpread = Math.max(laneSpread, bandHeight * 0.5F - smallRadius - 1.0F);

        laneBandTop = new float[laneCount];
        laneBandBottom = new float[laneCount];
        laneRuleY = new float[laneCount];
        for (int lane = 0; lane < laneCount; lane++) {
            float top = canvasTop + lanePad + lane * laneHeight;
            laneBandTop[lane] = top + bandPad;
            laneBandBottom[lane] = top + laneHeight - bandPad;
            laneRuleY[lane] = top + laneHeight;
        }

        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        for (MasteryProfile.Node node : nodes) {
            minX = Math.min(minX, (float) node.x());
            maxX = Math.max(maxX, (float) node.x());
        }
        float spanX = Math.max(0.0001F, maxX - minX);
        float innerLeft = laneLabelRight + capstoneRadius + 7;
        float innerRight = canvasRight - smallRadius - 12;

        int count = nodes.size();
        nodeX = new float[count];
        nodeY = new float[count];
        nodeRadius = new float[count];
        nodeHit = new float[count];
        nodeBranch = new int[count];
        nodeDepth = new int[count];

        for (int i = 0; i < count; i++) {
            MasteryProfile.Node node = nodes.get(i);
            int lane = branchIndexOf(branches, node.branch());
            nodeBranch[i] = lane;
            nodeRadius[i] = node.capstone() ? capstoneRadius : smallRadius;
            nodeHit[i] = Math.max(nodeRadius[i] + 5.0F, node.capstone() ? 16.0F : 11.0F);
            nodeX[i] = innerLeft + ((float) node.x() - minX) / spanX * (innerRight - innerLeft);

            float bandTop = laneBandTop[lane];
            float bandBottom = laneBandBottom[lane];
            float bandCenter = (bandTop + bandBottom) * 0.5F;
            float laneMin = Float.MAX_VALUE;
            float laneMax = -Float.MAX_VALUE;
            for (MasteryProfile.Node other : nodes) {
                if (other.branch().equals(node.branch())) {
                    laneMin = Math.min(laneMin, (float) other.y());
                    laneMax = Math.max(laneMax, (float) other.y());
                }
            }
            float laneSpan = laneMax - laneMin;
            float offset = laneSpan <= 0.0001F
                    ? 0.0F
                    : ((float) node.y() - (laneMin + laneMax) * 0.5F) / (laneSpan * 0.5F);
            nodeY[i] = node.capstone()
                    ? bandCenter + offset * laneSpread
                    : bandCenter + splay(offset) * branchSpread;
        }

        for (int i = 0; i < count; i++) {
            nodeDepth[i] = depthOf(nodes, i, 0);
        }

        for (int child = 0; child < count; child++) {
            for (String requiredId : nodes.get(child).requires()) {
                Integer parent = indexById.get(requiredId);
                if (parent != null) {
                    edges.add(buildEdge(parent, child));
                }
            }
        }
    }

    private static float splay(float offset) {
        float magnitude = Math.abs(offset);
        return magnitude <= 0.0001F ? 0.0F : Math.signum(offset) * (float) Math.sqrt(magnitude);
    }

    private static int branchIndexOf(List<MasteryProfile.Branch> branches, String branchId) {
        for (int i = 0; i < branches.size(); i++) {
            if (branches.get(i).id().equals(branchId)) {
                return i;
            }
        }
        return 0;
    }

    private int depthOf(List<MasteryProfile.Node> nodes, int index, int guard) {
        if (guard > 16) {
            return guard;
        }
        List<String> requires = nodes.get(index).requires();
        if (requires.isEmpty()) {
            return 0;
        }
        int deepest = 0;
        for (String id : requires) {
            Integer parent = indexById.get(id);
            if (parent != null) {
                deepest = Math.max(deepest, depthOf(nodes, parent, guard + 1) + 1);
            }
        }
        return deepest;
    }

    private Edge buildEdge(int parent, int child) {
        float startX = nodeX[parent] - nodeRadius[parent] - 2.0F;
        float startY = nodeY[parent];
        float endX = nodeX[child] + nodeRadius[child] + 2.0F;
        float endY = nodeY[child];
        float[] path;
        int points;
        if (Math.abs(startY - endY) < 1.0F) {
            path = new float[]{startX, startY, endX, endY};
            points = 2;
        } else {
            float midX = Math.round((startX + endX) * 0.5F);
            path = new float[]{
                    startX, startY,
                    midX, startY,
                    midX, endY,
                    endX, endY
            };
            points = 4;
        }
        return new Edge(parent, child, path, points, UiDraw.pathLength(path, points));
    }

    public int nodeCount() {
        return nodeX.length;
    }

    public float x(int index) {
        return nodeX[index];
    }

    public float y(int index) {
        return nodeY[index];
    }

    public float radius(int index) {
        return nodeRadius[index];
    }

    public int branch(int index) {
        return nodeBranch[index];
    }

    public int depth(int index) {
        return nodeDepth[index];
    }

    public float laneBandTop(int lane) {
        return laneBandTop[lane];
    }

    public float laneBandBottom(int lane) {
        return laneBandBottom[lane];
    }

    public float laneRuleY(int lane) {
        return laneRuleY[lane];
    }

    public int laneCount() {
        return laneBandTop.length;
    }

    public List<Edge> edges() {
        return edges;
    }

    public int indexOf(String nodeId) {
        return indexById.getOrDefault(nodeId, -1);
    }

    public int nodeAt(double mouseX, double mouseY) {
        int nearest = -1;
        double nearestDistance = Double.MAX_VALUE;
        for (int i = 0; i < nodeX.length; i++) {
            double dx = Math.abs(mouseX - nodeX[i]);
            double dy = Math.abs(mouseY - nodeY[i]);
            if (dx > nodeHit[i] || dy > nodeHit[i]) {
                continue;
            }
            double distance = Math.max(dx, dy);
            if (distance < nearestDistance) {
                nearest = i;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    public int neighbour(int from, int dirX, int dirY) {
        if (from < 0) {
            return nodeX.length == 0 ? -1 : 0;
        }
        int best = -1;
        double bestScore = Double.MAX_VALUE;
        for (int i = 0; i < nodeX.length; i++) {
            if (i == from) {
                continue;
            }
            double dx = nodeX[i] - nodeX[from];
            double dy = nodeY[i] - nodeY[from];
            double along = dx * dirX + dy * dirY;
            if (along <= 1.0) {
                continue;
            }
            double across = Math.abs(dx * dirY - dy * dirX);
            double score = along + across * 2.5;
            if (score < bestScore) {
                bestScore = score;
                best = i;
            }
        }
        return best;
    }
}
