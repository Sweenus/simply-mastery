package net.sweenus.simplymastery.mastery.progression;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.ServerCommandSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MasteryCommandsTest {

    @Test
    void registrationExposesEveryGrantSetAndClearForm() {
        CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher<>();
        MasteryCommands.register(dispatcher);

        assertExecutable(dispatcher, "simplymastery", "coverage");
        for (String literal : List.of("grant", "set")) {
            for (String resource : List.of("xp", "points")) {
                assertExecutable(dispatcher, "simplymastery", literal, resource, "amount");
                assertExecutable(dispatcher, "simplymastery", literal, resource, "amount", "targets");
            }
        }
        assertExecutable(dispatcher, "simplymastery", "clear");
        assertExecutable(dispatcher, "simplymastery", "clear", "targets");
    }

    private static void assertExecutable(CommandDispatcher<ServerCommandSource> dispatcher, String... path) {
        CommandNode<ServerCommandSource> node = dispatcher.findNode(List.of(path));
        String joined = String.join(" ", path);
        assertNotNull(node, joined + " is not registered");
        assertTrue(node.getCommand() != null, joined + " has no executor");
    }
}
