package net.sweenus.simplymastery.mastery.definition;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MasteryProfileValidatorTest {

    @Test
    void rejectsDanglingPrerequisiteWithProfileAndNode() {
        MasteryProfile source = ProfileTestFixtures.stormsEdge();
        List<MasteryProfile.Node> nodes = new ArrayList<>(source.nodes());
        MasteryProfile.Node node = nodes.get(1);
        nodes.set(1, new MasteryProfile.Node(node.id(), node.branch(), node.x(), node.y(), node.cost(),
                node.capstone(), node.choiceGroup(), List.of("missing"), node.effect(), node.nameKey(),
                node.descriptionKey(), node.icon()));
        ProfileValidationException exception = assertThrows(ProfileValidationException.class,
                () -> MasteryProfileValidator.validate(copy(source, nodes), 21));
        assertTrue(exception.getMessage().contains("node " + node.id() + ": dangling prerequisite missing"));
    }

    @Test
    void rejectsCyclesOverlappingCapstonesAndInvalidEffectParameters() {
        MasteryProfile source = ProfileTestFixtures.stormsEdge();
        List<MasteryProfile.Node> nodes = new ArrayList<>(source.nodes());
        MasteryProfile.Node root = nodes.getFirst();
        MasteryProfile.Node child = nodes.get(1);
        nodes.set(0, new MasteryProfile.Node(root.id(), root.branch(), root.x(), root.y(), root.cost(),
                root.capstone(), root.choiceGroup(), List.of(child.id()), root.effect(), root.nameKey(),
                root.descriptionKey(), root.icon()));
        MasteryProfile.Node cap = nodes.get(7);
        MasteryProfile.Node other = nodes.get(8);
        nodes.set(7, new MasteryProfile.Node(cap.id(), cap.branch(), other.x(), other.y(), cap.cost(), cap.capstone(),
                cap.choiceGroup(), cap.requires(), new MasteryProfile.Effect(Identifier.of("simplymastery", "stormstep"),
                java.util.Map.of("duration_ticks", 5000)), cap.nameKey(), cap.descriptionKey(), cap.icon()));
        ProfileValidationException exception = assertThrows(ProfileValidationException.class,
                () -> MasteryProfileValidator.validate(copy(source, nodes), 21));
        assertTrue(exception.getMessage().contains("prerequisite cycle"));
        assertTrue(exception.getMessage().contains("capstone hit boxes overlap"));
        assertTrue(exception.getMessage().contains("duration_ticks"));
    }

    @Test
    void rejectsEqualPrioritySelectorAmbiguity() {
        MasteryProfile source = ProfileTestFixtures.stormsEdge();
        MasteryProfile second = new MasteryProfile(source.schema(), Identifier.of("simplymastery", "other"),
                source.version(), source.selectors(), source.branches(), source.nodes(), source.migrations());
        ProfileValidationException exception = assertThrows(ProfileValidationException.class,
                () -> MasteryProfileValidator.validateRegistry(List.of(source, second), 21));
        assertTrue(exception.getMessage().contains("ambiguous selector item:simplyswords:storms_edge"));
    }

    private static MasteryProfile copy(MasteryProfile source, List<MasteryProfile.Node> nodes) {
        return new MasteryProfile(source.schema(), source.id(), source.version(), source.selectors(),
                source.branches(), nodes, source.migrations());
    }
}
