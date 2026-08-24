package net.sweenus.simplymastery.mastery.definition;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class MasteryProfile {

    public static final Identifier STORMS_EDGE_ID = Identifier.of("simplymastery", "storms_edge");
    public static final Identifier STORMS_EDGE_ITEM = Identifier.of("simplyswords", "storms_edge");
    public static final int DEFINITION_EPOCH = 1;
    public static final MasteryProfile STORMS_EDGE = createStormsEdge();

    private final Identifier id;
    private final int version;
    private final List<Branch> branches;
    private final List<Node> nodes;
    private final Map<String, Node> nodesById;

    private MasteryProfile(Identifier id, int version, List<Branch> branches, List<Node> nodes) {
        this.id = id;
        this.version = version;
        this.branches = List.copyOf(branches);
        this.nodes = List.copyOf(nodes);
        this.nodesById = new LinkedHashMap<>();
        for (Node node : nodes) {
            if (nodesById.put(node.id(), node) != null) {
                throw new IllegalArgumentException("Duplicate mastery node " + node.id());
            }
        }
    }

    public Identifier id() {
        return id;
    }

    public int version() {
        return version;
    }

    public List<Branch> branches() {
        return branches;
    }

    public List<Node> nodes() {
        return nodes;
    }

    public Optional<Node> node(String id) {
        return Optional.ofNullable(nodesById.get(id));
    }

    public static Optional<MasteryProfile> resolve(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return supportsItemId(Registries.ITEM.getId(stack.getItem()))
                ? Optional.of(STORMS_EDGE)
                : Optional.empty();
    }

    public static boolean supportsItemId(Identifier itemId) {
        return STORMS_EDGE_ITEM.equals(itemId);
    }

    private static MasteryProfile createStormsEdge() {
        List<Branch> branches = List.of(
                new Branch("guard", "branch.simplymastery.guard", 0xFF72D9E8),
                new Branch("tempo", "branch.simplymastery.tempo", 0xFFA984F4),
                new Branch("storm", "branch.simplymastery.storm", 0xFFE8B65A)
        );
        List<Node> nodes = List.of(
                node("guard_root", "guard", .88, .22, 1, false, "", List.of(), "none"),
                node("charged_pursuit", "guard", .56, .22, 1, false, "", List.of("guard_root"), "stormstep"),
                node("eye_of_storm", "guard", .16, .17, 2, true, "guard_capstone", List.of("charged_pursuit"), "none"),
                node("thunderhead", "guard", .16, .27, 2, true, "guard_capstone", List.of("charged_pursuit"), "none"),
                node("tempo_root", "tempo", .88, .50, 1, false, "", List.of(), "none"),
                node("afterimage", "tempo", .56, .50, 1, false, "", List.of("tempo_root"), "none"),
                node("flashpoint", "tempo", .16, .45, 2, true, "tempo_capstone", List.of("afterimage"), "none"),
                node("conduction", "tempo", .16, .55, 2, true, "tempo_capstone", List.of("afterimage"), "none"),
                node("storm_root", "storm", .88, .78, 1, false, "", List.of(), "none"),
                node("pressure_front", "storm", .56, .78, 1, false, "", List.of("storm_root"), "none"),
                node("skybreaker", "storm", .16, .73, 2, true, "storm_capstone", List.of("pressure_front"), "none"),
                node("tempest_wake", "storm", .16, .83, 2, true, "storm_capstone", List.of("pressure_front"), "none")
        );
        return new MasteryProfile(STORMS_EDGE_ID, 1, branches, nodes);
    }

    private static Node node(String id, String branch, double x, double y, int cost, boolean capstone,
                             String choiceGroup, List<String> requires, String effect) {
        return new Node(id, branch, x, y, cost, capstone, choiceGroup, requires, effect,
                "skill.simplymastery." + id, "skill.simplymastery." + id + ".description");
    }

    public record Branch(String id, String nameKey, int color) {
    }

    public record Node(String id, String branch, double x, double y, int cost, boolean capstone,
                       String choiceGroup, List<String> requires, String effect,
                       String nameKey, String descriptionKey) {
        public Node {
            requires = List.copyOf(requires);
        }
    }
}
