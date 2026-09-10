package net.sweenus.simplymastery.mastery.reward;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.config.MasteryConfig;
import net.sweenus.simplymastery.mastery.RunicForgeMasteryContext;
import net.sweenus.simplymastery.mastery.definition.MasteryProfileRegistry;
import net.sweenus.simplymastery.mastery.network.MasteryNetwork;
import net.sweenus.simplymastery.mastery.network.RewardDisplayPacket;
import net.sweenus.simplymastery.mastery.progression.MasteryRewardService;
import net.sweenus.simplymastery.mastery.progression.ProgressionLedger;
import net.sweenus.simplymastery.mastery.progression.ProgressionOwnership;
import net.sweenus.simplymastery.mastery.state.MasteryComponents;
import net.sweenus.simplymastery.mastery.state.MasteryStateAccess;
import net.sweenus.simplymastery.mixin.ForgeRewardsAccessor;
import net.sweenus.simplyswords.screen.RunicForgeScreenHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public final class ForgeRewards {
    public static final int PAGE_SIZE = 6;
    private static final Map<UUID, Long> LAST_REQUEST = new HashMap<>();
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private ForgeRewards() {
    }

    public static void init() {
        PlayerEvent.PLAYER_QUIT.register(player -> { SESSIONS.remove(player.getUuid()); LAST_REQUEST.remove(player.getUuid()); });
        LifecycleEvent.SERVER_STOPPED.register(server -> { SESSIONS.clear(); LAST_REQUEST.clear(); });
    }

    public static void handle(ServerPlayerEntity player, NbtCompound request) {
        if (request.getInt("protocol") != MasteryNetwork.PROTOCOL_VERSION
                || !(player.currentScreenHandler instanceof RunicForgeScreenHandler forge)
                || forge.syncId != request.getInt("screen") || !forge.canUse(player)) return;
        long tick = player.getServerWorld().getTime();
        if (LAST_REQUEST.getOrDefault(player.getUuid(), Long.MIN_VALUE) == tick) return;
        LAST_REQUEST.put(player.getUuid(), tick);
        ItemStack stack = RunicForgeMasteryContext.stateStack(forge);
        var resolution = MasteryProfileRegistry.resolveProgressionServer(stack).orElse(null);
        if (resolution == null || stack.isEmpty()) return;
        MasteryRewardService.synchronize(player.getServer(), stack);
        UUID weapon = stack.get(MasteryComponents.WEAPON_ID.get());
        Session session = SESSIONS.get(player.getUuid());
        if (session == null || session.handler() != forge || !session.weapon().equals(weapon)
                || !request.containsUuid("session") && request.getString("action").isEmpty()) {
            session = new Session(forge, weapon, UUID.randomUUID());
            SESSIONS.put(player.getUuid(), session);
        }
        var owner = MasteryRewardService.owner(player, stack, resolution.groupId());
        var entry = ProgressionLedger.get(player.getServer()).find(owner);
        var profile = MasteryProfileRegistry.resolveServer(RunicForgeMasteryContext.identityStack(forge)).orElse(null);
        long allocationRevision = profile == null ? 0 : MasteryStateAccess.read(player, stack, profile,
                MasteryRewardService.initialPoints(resolution.groupId())).mutationRevision();
        List<Row> rows = rows(player, stack, resolution.groupId(), entry);
        String message = "";
        String action = request.getString("action");
        if (!action.isEmpty()) {
            boolean envelope = request.containsUuid("session") && request.getUuid("session").equals(session.id())
                    && request.containsUuid("weapon") && request.getUuid("weapon").equals(weapon)
                    && request.getString("group").equals(resolution.groupId().toString())
                    && request.getString("mode").equals(owner.kind().name())
                    && request.containsUuid("subject") && request.getUuid("subject").equals(owner.subject())
                    && request.getInt("epoch") == MasteryProfileRegistry.server().epoch()
                    && request.getLong("revision") == entry.revision()
                    && request.getLong("allocations") == allocationRevision
                    && request.getLong("action_id") >= 0 && action.length() <= 512;
            Row row = rows.stream().filter(value -> value.id().equals(action)).findFirst().orElse(null);
            if (!envelope) message = "stale";
            else if (row == null) message = "unavailable";
            else if (!row.reason().isEmpty()) message = row.reason();
            else if (!ProgressionLedger.get(player.getServer()).claimSequence("forge/" + session.id(), request.getLong("action_id"))) message = "duplicate";
            else {
                if (row.advancement()) {
                    AdvancementEntitlements.claim(player, row.source());
                } else {
                    int remaining = row.quantity();
                    for (int slot = 0; slot < player.getInventory().size() && remaining > 0; slot++) {
                        ItemStack payment = player.getInventory().getStack(slot);
                        if (!matchesPayment(payment, stack, resolution.groupId(), row.source())) continue;
                        int amount = Math.min(remaining, payment.getCount());
                        payment.decrement(amount);
                        remaining -= amount;
                    }
                }
                MasteryRewardService.award(player, stack, resolution.groupId(), row.reward().xp().orElse(0), row.reward().points().orElse(0));
                ((ForgeRewardsAccessor) forge).simplymastery$refreshRewardsPreview();
                forge.getForgeInventory().markDirty();
                player.getInventory().markDirty();
                forge.sendContentUpdates();
                message = "success";
                rows = rows(player, stack, resolution.groupId(), entry);
                allocationRevision = profile == null ? 0 : MasteryStateAccess.read(player, stack, profile,
                        MasteryRewardService.initialPoints(resolution.groupId())).mutationRevision();
            }
        }
        int pages = Math.max(1, (rows.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = Math.clamp(request.getInt("page"), 0, pages - 1);
        NbtCompound display = new NbtCompound();
        display.putInt("protocol", MasteryNetwork.PROTOCOL_VERSION);
        display.putInt("screen", forge.syncId);
        display.putUuid("session", session.id());
        display.putUuid("weapon", weapon);
        display.putString("group", resolution.groupId().toString());
        display.putString("mode", owner.kind().name());
        display.putUuid("subject", owner.subject());
        display.putInt("epoch", MasteryProfileRegistry.server().epoch());
        display.putLong("revision", entry.revision());
        display.putLong("allocations", allocationRevision);
        display.putInt("page", page);
        display.putInt("pages", pages);
        display.putString("message", message);
        var policy = MasteryProfileRegistry.server().policy(resolution.groupId());
        display.putDouble("xp", entry.xp());
        display.putInt("points", Math.min(entry.points(), policy.pointCap()));
        display.putInt("cap", policy.pointCap());
        display.putLong("next", policy.nextCost(entry.points()));
        NbtList visible = new NbtList();
        for (Row row : rows.subList(page * PAGE_SIZE, Math.min(rows.size(), (page + 1) * PAGE_SIZE))) {
            NbtCompound value = new NbtCompound();
            value.putString("id", row.id());
            value.putString("source", row.source());
            String label = row.source();
            if (row.advancement()) {
                var advancement = player.getServer().getAdvancementLoader().get(Identifier.of(row.source()));
                if (advancement != null) label = advancement.value().display().map(info -> info.getTitle().getString()).orElse(label);
            } else {
                for (int slot = 0; slot < player.getInventory().size(); slot++) {
                    var payment = player.getInventory().getStack(slot);
                    if (matchesPayment(payment, stack, resolution.groupId(), row.source())) {
                        label = payment.getName().getString();
                        break;
                    }
                }
            }
            value.putString("label", label.substring(0, Math.min(256, label.length())));
            value.putString("cost", row.cost());
            value.putInt("quantity", row.quantity());
            value.putInt("xp", row.reward().xp().orElse(0));
            value.putInt("points", row.reward().points().orElse(0));
            value.putString("reason", row.reason());
            visible.add(value);
        }
        display.put("rows", visible);
        new RewardDisplayPacket(display).sendTo(player);
    }

    private static List<Row> rows(ServerPlayerEntity player, ItemStack weapon, Identifier group, ProgressionLedger.Entry entry) {
        List<Row> result = new ArrayList<>();
        var definitions = MasteryProfileRegistry.server().rewards();
        if (definitions == null) return result;
        Map<Identifier, ResolvedRewardDefinitions.Named<RewardDefinitions.Consumable>> rules = new TreeMap<>();
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack item = player.getInventory().getStack(slot);
            if (item.isEmpty()) continue;
            definitions.consumable(Registries.ITEM.getId(item.getItem()), Registries.ITEM.getId(weapon.getItem()), group)
                    .ifPresent(rule -> rules.put(rule.id(), rule));
        }
        for (var rule : rules.values()) {
            var definition = rule.value();
            int available = 0;
            for (int slot = 0; slot < player.getInventory().size(); slot++) {
                var item = player.getInventory().getStack(slot);
                if (matchesPayment(item, weapon, group, rule.id().toString())) available += item.getCount();
            }
            String reason = reason(weapon, group, entry, definition.reward(), MasteryConfig.SERVER.masteryConsumablesEnabled);
            if (reason.isEmpty() && available < definition.quantity()) reason = "payment";
            String ingredient = definition.ingredient().id().map(id -> Registries.ITEM.get(id).getName().getString())
                    .orElseGet(() -> "#" + definition.ingredient().tag().orElseThrow());
            result.add(new Row("consumable/" + rule.id(), rule.id().toString(), false, definition.quantity(),
                    definition.quantity() + " × " + ingredient + " (" + available + ")", definition.reward(), reason));
        }
        var pending = AdvancementEntitlements.playerData(player).getCompound("pending");
        for (String id : pending.getKeys().stream().sorted().toList()) {
            var reward = AdvancementEntitlements.reward(pending.getCompound(id), Registries.ITEM.getId(weapon.getItem()), group);
            if (reward == null) continue;
            result.add(new Row("advancement/" + id, id, true, 0, "", reward,
                    reason(weapon, group, entry, reward, MasteryConfig.SERVER.advancementRewardsEnabled)));
        }
        return result;
    }

    private static String reason(ItemStack weapon, Identifier group, ProgressionLedger.Entry entry, RewardDefinitions.Reward reward, boolean feature) {
        if (!MasteryConfig.SERVER.enabled || !feature) return "disabled";
        var resolution = MasteryProfileRegistry.resolveProgressionServer(weapon).orElse(null);
        if (resolution == null || !resolution.profileIds().isEmpty() && resolution.profileIds().stream()
                .allMatch(MasteryConfig.SERVER.disabledProfiles::contains)) return "disabled";
        var policy = MasteryProfileRegistry.server().policy(group);
        if (reward.points().orElse(0) > policy.pointCap() - entry.points()
                || reward.xp().orElse(0) > policy.remainingCapacity(entry.points(), entry.xp())) return "capacity";
        return "";
    }

    private static boolean matchesPayment(ItemStack payment, ItemStack weapon, Identifier group, String ruleId) {
        if (payment.isEmpty()) return false;
        var definitions = MasteryProfileRegistry.server().rewards();
        return definitions != null && definitions.consumable(Registries.ITEM.getId(payment.getItem()),
                Registries.ITEM.getId(weapon.getItem()), group).map(rule -> rule.id().toString().equals(ruleId)).orElse(false);
    }

    private record Session(RunicForgeScreenHandler handler, UUID weapon, UUID id) {
    }
    private record Row(String id, String source, boolean advancement, int quantity, String cost,
                       RewardDefinitions.Reward reward, String reason) {
    }
}
