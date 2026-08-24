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
    public final int respecButtonX;
    public final int respecButtonWidth;
    public final int closeButtonX;
    public final int closeButtonWidth;
    public final int headerContentLeft;
    public final int headerContentRight;

    private final float[] nodeX;
    private final float[] nodeY;
    private final float[] nodeRadius;
    private final float[] nodeHit;
    private final int[] nodeBranch;
    private final int[] nodeDepth;
    private final float[] laneLabelY;
    private final float[] laneBandTop;
    private final float[] laneBandBottom;
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

        int marginX = Math.max(10, Math.round(screenWidth * 0.025F));
        int marginY = Math.max(8, Math.round(screenHeight * 0.025F));
        int headerHeight = Math.clamp(Math.round(screenHeight * 0.095F), 32, 48);
        headerLeft = marginX;
        headerTop = marginY;
        headerRight = screenWidth - marginX;
        headerBottom = headerTop + headerHeight;

        backButtonWidth = Math.min(104, Math.max(64, screenWidth / 7));
        closeButtonWidth = Math.min(64, Math.max(46, screenWidth / 12));
        respecButtonWidth = Math.min(78, Math.max(58, screenWidth / 10));
        backButtonX = headerLeft + 7;
        closeButtonX = headerRight - 7 - closeButtonWidth;
        respecButtonX = closeButtonX - 6 - respecButtonWidth;
        headerContentLeft = backButtonX + backButtonWidth + 13;
        headerContentRight = respecButtonX - 12;

        int bodyTop = headerBottom + Math.max(5, Math.round(screenHeight * 0.02F));
        int bodyBottom = screenHeight - marginY;
        int desiredShowcaseLeft = Math.round(screenWidth * 0.715F);
        compact = screenWidth - marginX - desiredShowcaseLeft < 104 || screenWidth < 420;
        dockDetail = compact;

        statusBottom = bodyBottom;
        statusTop = bodyBottom - 21;
        dockHeight = dockDetail ? Math.clamp(Math.round(screenHeight * 0.24F), 58, 104) : 0;

        canvasTop = bodyTop;
        canvasLeft = marginX;
        if (compact) {
            canvasRight = screenWidth - marginX;
            showcaseLeft = 0;
            showcaseRight = 0;
            showcaseTop = 0;
            showcaseBottom = 0;
            statusLeft = canvasLeft;
            statusRight = canvasRight;
            canvasBottom = Math.max(canvasTop + 60, statusTop - 6 - dockHeight - 6);
        } else {
            canvasRight = Math.round(screenWidth * 0.695F);
            showcaseLeft = desiredShowcaseLeft;
            showcaseRight = screenWidth - marginX;
            showcaseTop = bodyTop;
            showcaseBottom = statusTop - 6;
            statusLeft = canvasLeft;
            statusRight = showcaseRight;
            canvasBottom = statusTop - 6;
        }

        int laneCount = Math.max(1, branches.size());
        float lanePad = 5.0F;
        float laneHeight = (canvasBottom - canvasTop - lanePad * 2.0F) / laneCount;
        float labelRow = Math.min(14.0F, laneHeight * 0.24F);
        float bandHeight = laneHeight - labelRow;
        int capstoneRadius = Math.round(Math.min(Math.clamp(laneHeight * 0.26F, 11.0F, 22.0F),
                Math.max(6.0F, (bandHeight - 6.0F) / 4.0F)));
        float smallRadius = Math.clamp(capstoneRadius * 0.58F, 5.0F, 13.0F);
        float laneSpread = Math.max(0.0F, Math.min(bandHeight * 0.5F - capstoneRadius - 1.0F,
                capstoneRadius * 2.6F));

        laneLabelY = new float[laneCount];
        laneBandTop = new float[laneCount];
        laneBandBottom = new float[laneCount];
        for (int lane = 0; lane < laneCount; lane++) {
            float top = canvasTop + lanePad + lane * laneHeight;
            laneLabelY[lane] = top + 1.0F;
            laneBandTop[lane] = top + labelRow;
            laneBandBottom[lane] = top + laneHeight;
        }

        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        for (MasteryProfile.Node node : nodes) {
            minX = Math.min(minX, (float) node.x());
            maxX = Math.max(maxX, (float) node.x());
        }
        float spanX = Math.max(0.0001F, maxX - minX);
        float innerLeft = canvasLeft + capstoneRadius + 9;
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
            nodeY[i] = bandCenter + offset * laneSpread;
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
            float midX = (startX + endX) * 0.5F;
            float chamfer = Math.min(6.0F, Math.min(Math.abs(startY - endY) * 0.5F, Math.abs(startX - endX) * 0.25F));
            float direction = Math.signum(endY - startY);
            path = new float[]{
                    startX, startY,
                    midX + chamfer, startY,
                    midX, startY + direction * chamfer,
                    midX, endY - direction * chamfer,
                    midX - chamfer, endY,
                    endX, endY
            };
            points = 6;
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

    public float laneLabelY(int lane) {
        return laneLabelY[lane];
    }

    public float laneBandTop(int lane) {
        return laneBandTop[lane];
    }

    public float laneBandBottom(int lane) {
        return laneBandBottom[lane];
    }

    public int laneCount() {
        return laneLabelY.length;
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
            double dx = mouseX - nodeX[i];
            double dy = mouseY - nodeY[i];
            double distance = dx * dx + dy * dy;
            if (distance <= nodeHit[i] * nodeHit[i] && distance < nearestDistance) {
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
