package net.sweenus.simplymastery.mastery.progression;

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
import net.sweenus.simplymastery.config.MasteryConfig;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplymastery.mastery.definition.MasteryProfileRegistry;
import net.sweenus.simplymastery.mastery.state.MasteryPortfolio;
import net.sweenus.simplymastery.mastery.state.MasteryState;
import net.sweenus.simplymastery.mastery.state.MasteryStateAccess;

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
                CommandManager.literal("simplymastery")
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
            source.sendError(Text.translatable("commands.simplymastery.failed.disabled"));
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
            source.sendError(Text.translatable("commands.simplymastery.failed.no_weapon",
                    player.getDisplayName()));
            return false;
        }
        MasteryProfileRegistry.ProgressionResolution resolution =
                MasteryProfileRegistry.resolveProgressionServer(stack).orElse(null);
        if (resolution == null) {
            source.sendError(Text.translatable("commands.simplymastery.failed.unsupported",
                    player.getDisplayName(), stack.getName()));
            return false;
        }
        int initialPoints = Math.min(MasteryConfig.SERVER.verticalSliceStartingPoints,
                MasteryConfig.SERVER.maximumEarnedPoints);
        MasteryProfile profile = resolution.visibleProfile().orElse(null);
        boolean changed;
        MasteryPortfolio.GroupProgress progress;
        if (profile == null) {
            changed = applyToGroup(stack, resolution.groupId(), initialPoints, mode, amount);
            progress = groupProgress(stack, resolution.groupId(), initialPoints);
        } else {
            MasteryState state = MasteryStateAccess.read(stack, profile, initialPoints);
            MasteryState updated = applyToState(state, initialPoints, mode, amount);
            changed = updated != state;
            if (changed) MasteryStateAccess.write(stack, profile, updated);
            progress = new MasteryPortfolio.GroupProgress(profile.progressionGroupId(), updated.masteryXp(),
                    updated.earnedPoints());
        }
        if (changed) {
            player.getInventory().markDirty();
            player.currentScreenHandler.sendContentUpdates();
        }
        source.sendFeedback(() -> Text.translatable(changed ? "commands.simplymastery.result"
                        : "commands.simplymastery.unchanged", player.getDisplayName(), stack.getName(),
                progress.masteryXp(), progress.earnedPoints()), true);
        return changed;
    }

    private static MasteryState applyToState(MasteryState state, int initialPoints, Mode mode, int amount) {
        int maximumPoints = MasteryConfig.SERVER.maximumEarnedPoints;
        return switch (mode) {
            case GRANT_XP -> state.awardXp(amount, maximumPoints, MasteryConfig.SERVER.xpBaseRequirement,
                    MasteryConfig.SERVER.xpRequirementGrowth);
            case GRANT_POINTS -> state.grantPoints(amount, maximumPoints);
            case SET_XP -> state.withProgress(amount, state.earnedPoints(), maximumPoints);
            case SET_POINTS -> state.withProgress(state.masteryXp(), amount, maximumPoints);
            case CLEAR -> state.respec().withProgress(0, initialPoints, maximumPoints);
        };
    }

    private static boolean applyToGroup(ItemStack stack, Identifier groupId, int initialPoints, Mode mode,
                                        int amount) {
        int maximumPoints = MasteryConfig.SERVER.maximumEarnedPoints;
        MasteryPortfolio.GroupProgress current = groupProgress(stack, groupId, initialPoints);
        return switch (mode) {
            case GRANT_XP -> MasteryStateAccess.bankXp(stack, groupId, initialPoints, amount, maximumPoints,
                    MasteryConfig.SERVER.xpBaseRequirement, MasteryConfig.SERVER.xpRequirementGrowth);
            case GRANT_POINTS -> MasteryStateAccess.grantPoints(stack, groupId, initialPoints, amount,
                    maximumPoints);
            case SET_XP -> MasteryStateAccess.setProgress(stack, groupId, initialPoints, amount,
                    current.earnedPoints(), maximumPoints);
            case SET_POINTS -> MasteryStateAccess.setProgress(stack, groupId, initialPoints,
                    current.masteryXp(), amount, maximumPoints);
            case CLEAR -> MasteryStateAccess.setProgress(stack, groupId, initialPoints, 0, initialPoints,
                    maximumPoints);
        };
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
