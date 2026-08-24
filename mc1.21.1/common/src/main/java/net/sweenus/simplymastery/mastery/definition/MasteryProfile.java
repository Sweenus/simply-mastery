package net.sweenus.simplymastery.mastery.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class MasteryProfile {

    public static final int CURRENT_SCHEMA = 1;
    public static final int MAX_NODES = 128;
    public static final int MAX_PROFILE_JSON_BYTES = 262_144;
    public static final int MAX_CANONICAL_JSON_BYTES = 1_048_576;
    public static final Codec<MasteryProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("schema").forGetter(MasteryProfile::schema),
            Identifier.CODEC.fieldOf("id").forGetter(MasteryProfile::id),
            Codec.INT.fieldOf("profile_version").forGetter(MasteryProfile::version),
            Selector.CODEC.listOf().fieldOf("selectors").forGetter(MasteryProfile::selectors),
            Branch.CODEC.listOf().fieldOf("branches").forGetter(MasteryProfile::branches),
            Node.CODEC.listOf().fieldOf("nodes").forGetter(MasteryProfile::nodes),
            Migration.CODEC.listOf().optionalFieldOf("migrations", List.of()).forGetter(MasteryProfile::migrations)
    ).apply(instance, MasteryProfile::new));

    private final int schema;
    private final Identifier id;
    private final int version;
    private final List<Selector> selectors;
    private final List<Branch> branches;
    private final List<Node> nodes;
    private final List<Migration> migrations;
    private final Map<String, Node> nodesById;

    public MasteryProfile(int schema, Identifier id, int version, List<Selector> selectors,
                          List<Branch> branches, List<Node> nodes, List<Migration> migrations) {
        this.schema = schema;
        this.id = id;
        this.version = version;
        this.selectors = List.copyOf(selectors);
        this.branches = List.copyOf(branches);
        this.nodes = List.copyOf(nodes);
        this.migrations = List.copyOf(migrations);
        this.nodesById = new LinkedHashMap<>();
        for (Node node : nodes) {
            nodesById.putIfAbsent(node.id(), node);
        }
    }

    public int schema() {
        return schema;
    }

    public Identifier id() {
        return id;
    }

    public int version() {
        return version;
    }

    public List<Selector> selectors() {
        return selectors;
    }

    public List<Branch> branches() {
        return branches;
    }

    public List<Node> nodes() {
        return nodes;
    }

    public List<Migration> migrations() {
        return migrations;
    }

    public Optional<Node> node(String id) {
        return Optional.ofNullable(nodesById.get(id));
    }

    public record Selector(Optional<Identifier> item, Optional<Identifier> formFamily, int priority) {
        public static final Codec<Selector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.optionalFieldOf("item").forGetter(Selector::item),
                Identifier.CODEC.optionalFieldOf("form_family").forGetter(Selector::formFamily),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(Selector::priority)
        ).apply(instance, Selector::new));
    }

    public record Branch(String id, String nameKey, int color) {
        public static final Codec<Branch> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(Branch::id),
                Codec.STRING.fieldOf("name").forGetter(Branch::nameKey),
                Codec.STRING.fieldOf("color").xmap(Branch::parseColor, Branch::formatColor).forGetter(Branch::color)
        ).apply(instance, Branch::new));

        private static int parseColor(String value) {
            String hex = value.startsWith("#") ? value.substring(1) : value;
            if (hex.length() != 6) {
                throw new IllegalArgumentException("Branch color must contain six hexadecimal digits: " + value);
            }
            return 0xFF000000 | Integer.parseInt(hex, 16);
        }

        private static String formatColor(int value) {
            return "#%06X".formatted(value & 0xFFFFFF);
        }
    }

    public record Node(String id, String branch, double x, double y, int cost, boolean capstone,
                       String choiceGroup, List<String> requires, Effect effect,
                       String nameKey, String descriptionKey, Optional<Identifier> icon) {
        public static final Codec<Node> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(Node::id),
                Codec.STRING.fieldOf("branch").forGetter(Node::branch),
                Codec.DOUBLE.fieldOf("x").forGetter(Node::x),
                Codec.DOUBLE.fieldOf("y").forGetter(Node::y),
                Codec.INT.fieldOf("cost").forGetter(Node::cost),
                Codec.BOOL.optionalFieldOf("capstone", false).forGetter(Node::capstone),
                Codec.STRING.optionalFieldOf("choice_group", "").forGetter(Node::choiceGroup),
                Codec.STRING.listOf().optionalFieldOf("requires", List.of()).forGetter(Node::requires),
                Effect.CODEC.fieldOf("effect").forGetter(Node::effect),
                Codec.STRING.optionalFieldOf("name", "").forGetter(Node::nameKey),
                Codec.STRING.optionalFieldOf("description", "").forGetter(Node::descriptionKey),
                Identifier.CODEC.optionalFieldOf("icon").forGetter(Node::icon)
        ).apply(instance, Node::new));

        public Node {
            requires = List.copyOf(requires);
            if (nameKey.isEmpty()) {
                nameKey = "skill.simplymastery." + id;
            }
            if (descriptionKey.isEmpty()) {
                descriptionKey = "skill.simplymastery." + id + ".description";
            }
        }
    }

    public record Effect(Identifier type, Map<String, Integer> parameters) {
        public static final Codec<Effect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("type").forGetter(Effect::type),
                Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("parameters", Map.of())
                        .forGetter(Effect::parameters)
        ).apply(instance, Effect::new));

        public Effect {
            parameters = Map.copyOf(parameters);
        }
    }

    public record Migration(int fromVersion, int toVersion, Map<String, String> renamedNodes,
                            Map<String, Integer> removedNodeRefunds) {
        public static final Codec<Migration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("from_version").forGetter(Migration::fromVersion),
                Codec.INT.fieldOf("to_version").forGetter(Migration::toVersion),
                Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("renamed_nodes", Map.of())
                        .forGetter(Migration::renamedNodes),
                Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("removed_node_refunds", Map.of())
                        .forGetter(Migration::removedNodeRefunds)
        ).apply(instance, Migration::new));

        public Migration {
            renamedNodes = Map.copyOf(renamedNodes);
            removedNodeRefunds = Map.copyOf(removedNodeRefunds);
        }
    }
}
