package net.sweenus.simplyswordsmastery.mastery.progression;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.sweenus.simplyswordsmastery.config.MasteryConfig;
import net.sweenus.simplyswordsmastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswordsmastery.mastery.definition.MasteryProfileRegistry;
import net.sweenus.simplyswordsmastery.mastery.state.MasteryPortfolio;
import net.sweenus.simplyswordsmastery.mastery.state.MasteryState;
import net.sweenus.simplyswordsmastery.mastery.state.MasteryStateAccess;

import java.util.Collection;
import java.util.List;

public final class MasteryCommands {

    private static final String AMOUNT = "amount";
    private static final String TARGETS = "targets";

    private MasteryCommands() {
    }

    public static void init() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, environment) -> register(dispatcher));
    }

    static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("simplyswordsmastery")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(amountNode("grant", Mode.GRANT_XP, Mode.GRANT_POINTS, 1))
                        .then(amountNode("set", Mode.SET_XP, Mode.SET_POINTS, 0))
                        .then(CommandManager.literal("clear")
                                .executes(context -> run(context, Mode.CLEAR, 0, self(context)))
                                .then(CommandManager.argument(TARGETS, EntityArgumentType.players())
                                        .executes(context -> run(context, Mode.CLEAR, 0, targets(context))))));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> amountNode(String literal, Mode xp, Mode points,
                                                                         int minimum) {
        return CommandManager.literal(literal)
                .then(amountLeaf("xp", xp, minimum))
                .then(amountLeaf("points", points, minimum));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> amountLeaf(String literal, Mode mode, int minimum) {
        return CommandManager.literal(literal)
                .then(CommandManager.argument(AMOUNT, IntegerArgumentType.integer(minimum))
                        .executes(context -> run(context, mode, amount(context), self(context)))
                        .then(CommandManager.argument(TARGETS, EntityArgumentType.players())
                                .executes(context -> run(context, mode, amount(context), targets(context)))));
    }

    private static int run(CommandContext<ServerCommandSource> context, Mode mode, int amount,
                           Collection<ServerPlayerEntity> targets) {
        ServerCommandSource source = context.getSource();
        if (!MasteryConfig.SERVER.enabled) {
            source.sendError(Text.translatable("commands.simplyswordsmastery.failed.disabled"));
            return 0;
        }
        int changed = 0;
        for (ServerPlayerEntity player : targets) {
            if (apply(source, player, mode, amount)) changed++;
        }
        return changed;
    }

    private static boolean apply(ServerCommandSource source, ServerPlayerEntity player, Mode mode, int amount) {
        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) {
            source.sendError(Text.translatable("commands.simplyswordsmastery.failed.no_weapon",
                    player.getDisplayName()));
            return false;
        }
        MasteryProfileRegistry.ProgressionResolution resolution =
                MasteryProfileRegistry.resolveProgressionServer(stack).orElse(null);
        if (resolution == null) {
            source.sendError(Text.translatable("commands.simplyswordsmastery.failed.unsupported",
                    player.getDisplayName(), stack.getName()));
            return false;
        }
        int initialPoints = MasteryRewardService.initialPoints(resolution.groupId());
        MasteryRewardService.synchronize(player.getServer(), stack);
        Identifier group = resolution.groupId();
        MasteryPortfolio.GroupProgress current = MasteryRewardService.progress(player, stack, group);
        boolean changed = switch (mode) {
            case GRANT_XP -> MasteryRewardService.award(player, stack, group, amount, 0);
            case GRANT_POINTS -> MasteryRewardService.award(player, stack, group, 0, amount);
            case SET_XP -> MasteryRewardService.setXp(player, stack, group, amount);
            case SET_POINTS -> MasteryRewardService.set(player, stack, group, current.masteryXp(), amount);
            case CLEAR -> {
                boolean cleared = false;
                MasteryProfile profile = resolution.visibleProfile().orElse(null);
                if (profile != null) {
                    MasteryState state = MasteryStateAccess.read(player, stack, profile, initialPoints);
                    MasteryState reset = state.respec();
                    cleared = !reset.equals(state);
                    MasteryStateAccess.write(player, stack, profile, reset);
                }
                yield MasteryRewardService.set(player, stack, group, 0, initialPoints) || cleared;
            }
        };
        MasteryPortfolio.GroupProgress progress = MasteryRewardService.progress(player, stack, group);
        if (changed) {
            player.getInventory().markDirty();
            player.currentScreenHandler.sendContentUpdates();
        }
        source.sendFeedback(() -> Text.translatable(changed ? "commands.simplyswordsmastery.result"
                        : "commands.simplyswordsmastery.unchanged", player.getDisplayName(), stack.getName(),
                progress.masteryXp(), progress.earnedPoints()), true);
        return changed;
    }

    private static MasteryPortfolio.GroupProgress groupProgress(ItemStack stack, Identifier groupId,
                                                                int initialPoints) {
        MasteryPortfolio portfolio = MasteryStateAccess.portfolio(stack);
        return portfolio == null ? new MasteryPortfolio.GroupProgress(groupId, 0, initialPoints)
                : portfolio.group(groupId)
                        .orElseGet(() -> new MasteryPortfolio.GroupProgress(groupId, 0, initialPoints));
    }

    private static Collection<ServerPlayerEntity> self(CommandContext<ServerCommandSource> context)
            throws CommandSyntaxException {
        return List.of(context.getSource().getPlayerOrThrow());
    }

    private static Collection<ServerPlayerEntity> targets(CommandContext<ServerCommandSource> context)
            throws CommandSyntaxException {
        return EntityArgumentType.getPlayers(context, TARGETS);
    }

    private static int amount(CommandContext<ServerCommandSource> context) {
        return IntegerArgumentType.getInteger(context, AMOUNT);
    }

    private enum Mode {
        GRANT_XP,
        GRANT_POINTS,
        SET_XP,
        SET_POINTS,
        CLEAR
    }
}
