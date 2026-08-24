package net.sweenus.simplymastery.mastery.definition;

import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.sweenus.simplyswords.api.AwakeningFormRegistry;
import net.sweenus.simplyswords.item.UniqueWeaponItem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MasteryCoverageReport {

    private MasteryCoverageReport() {
    }

    public static void init() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, environment) -> dispatcher.register(
                CommandManager.literal("simplymastery")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("coverage").executes(MasteryCoverageReport::execute))));
        LifecycleEvent.SERVER_STARTED.register(server -> {
            if (!Boolean.getBoolean("simplymastery.coverageAudit")) return;
            try {
                String configured = System.getProperty("simplymastery.projectDir");
                Path root = configured == null ? Platform.getGameFolder() : Path.of(configured);
                Result result = generate(root.resolve("build/reports/simplymastery/unique-weapon-coverage.md"));
                if (result.errors() != 0) throw new IllegalStateException("Mastery coverage has "
                        + result.errors() + " error(s); see " + result.output());
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to write Simply Mastery coverage", exception);
            }
        });
    }

    public static Result generate(Path output) throws IOException {
        MasteryProfileRegistry.Snapshot snapshot = MasteryProfileRegistry.server();
        Set<Identifier> declaredUnique = declaredUniqueIds();
        List<Row> rows = new ArrayList<>();
        Map<Identifier, Identifier> familyWinners = new HashMap<>();
        int errors = 0;

        List<Map.Entry<net.minecraft.registry.RegistryKey<Item>, Item>> items = Registries.ITEM.getEntrySet().stream()
                .filter(entry -> entry.getKey().getValue().getNamespace().equals("simplyswords"))
                .filter(entry -> entry.getValue() instanceof SwordItem || declaredUnique.contains(entry.getKey().getValue()))
                .sorted(Comparator.comparing(entry -> entry.getKey().getValue().toString())).toList();

        for (Map.Entry<net.minecraft.registry.RegistryKey<Item>, Item> entry : items) {
            Identifier itemId = entry.getKey().getValue();
            Item item = entry.getValue();
            ItemStack stack = item.getDefaultStack();
            var family = AwakeningFormRegistry.get(stack).orElse(null);
            boolean unique = item instanceof UniqueWeaponItem || family != null || declaredUnique.contains(itemId);
            List<MasteryProfileRegistry.Resolution> winners = snapshot.matches(stack);
            String error = "";
            if (unique && winners.size() != 1) {
                error = winners.isEmpty() ? "uncovered unique/form" : "ambiguous selector winners";
            } else if (!unique && !winners.isEmpty()) {
                error = "non-unique weapon resolved";
            }
            if (family != null && winners.size() == 1) {
                Identifier familyId = family.baseStage().id();
                Identifier previous = familyWinners.putIfAbsent(familyId, winners.getFirst().profile().id());
                if (previous != null && !previous.equals(winners.getFirst().profile().id())) {
                    error = "form family resolves to multiple profiles";
                }
            }
            if (!error.isEmpty()) errors++;
            rows.add(new Row(itemId, item.getClass().getName(), unique, family != null, winners, error));
        }

        Files.createDirectories(output.getParent());
        Files.writeString(output, render(snapshot, rows, errors), StandardCharsets.UTF_8);
        MasteryBalanceReport.write(output.resolveSibling("mastery-balance.md"),
                snapshot.profiles().values().stream().toList());
        return new Result(output, rows.size(), errors);
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        try {
            String configured = System.getProperty("simplymastery.projectDir");
            Path root = configured == null ? Platform.getGameFolder() : Path.of(configured);
            Result result = generate(root.resolve("build/reports/simplymastery/unique-weapon-coverage.md"));
            context.getSource().sendFeedback(() -> Text.literal("Simply Mastery coverage: " + result.items()
                    + " weapons, " + result.errors() + " errors; " + result.output()), true);
            return result.errors() == 0 ? 1 : 0;
        } catch (IOException exception) {
            context.getSource().sendError(Text.literal("Unable to write Simply Mastery coverage: "
                    + exception.getMessage()));
            return 0;
        }
    }

    private static Set<Identifier> declaredUniqueIds() {
        Set<Identifier> result = new HashSet<>();
        result.add(Identifier.of("simplyswords", "storms_edge"));
        for (BuiltInFamilyProfiles.Family family : BuiltInFamilyProfiles.familiesForCoverage()) {
            for (String item : family.items()) result.add(Identifier.of("simplyswords", item));
        }
        return result;
    }

    private static String render(MasteryProfileRegistry.Snapshot snapshot, List<Row> rows, int errors) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Simply Mastery unique-weapon coverage\n\n")
                .append("Definition epoch: ").append(snapshot.epoch()).append("  \n")
                .append("Loaded profiles: ").append(snapshot.profiles().size()).append("  \n")
                .append("Inspected weapons: ").append(rows.size()).append("  \n")
                .append("Coverage errors: ").append(errors).append("\n\n")
                .append("| Item ID | Registration / class | Profile | Priority | Version | Form family | Error |\n")
                .append("|---|---|---|---:|---:|:---:|---|\n");
        for (Row row : rows) {
            String profiles = row.winners().isEmpty() ? "—" : row.winners().stream()
                    .map(winner -> winner.profile().id().toString()).reduce((a, b) -> a + ", " + b).orElse("—");
            String priority = row.winners().size() == 1 ? Integer.toString(row.winners().getFirst().priority()) : "—";
            String version = row.winners().size() == 1 ? Integer.toString(row.winners().getFirst().profile().version()) : "—";
            markdown.append("| `").append(row.item()).append("` | ")
                    .append(row.unique() ? "unique / `" : "ordinary / `").append(row.itemClass()).append("` | `")
                    .append(profiles).append("` | ").append(priority).append(" | ").append(version).append(" | ")
                    .append(row.formMember() ? "yes" : "no").append(" | ")
                    .append(row.error().isEmpty() ? "—" : row.error()).append(" |\n");
        }
        return markdown.toString();
    }

    private record Row(Identifier item, String itemClass, boolean unique, boolean formMember,
                       List<MasteryProfileRegistry.Resolution> winners, String error) {
    }

    public record Result(Path output, int items, int errors) {
    }
}
