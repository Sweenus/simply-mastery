package net.sweenus.simplyswordsmastery.mastery.reward;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.sweenus.simplyswordsmastery.mastery.definition.MasteryProfileRegistry;
import net.sweenus.simplyswordsmastery.mastery.progression.ProgressionLedger;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;

public final class AdvancementEntitlements {
    private static final Map<PlayerAdvancementTracker, Integer> BASELINED = new WeakHashMap<>();
    private AdvancementEntitlements() {
    }

    public static NbtCompound playerData(ServerPlayerEntity player) {
        return ProgressionLedger.get(player.getServer()).rewardRecords().getCompound("advancements")
                .getCompound(player.getUuidAsString());
    }

    private static void save(ServerPlayerEntity player, NbtCompound data) {
        var ledger = ProgressionLedger.get(player.getServer());
        var players = ledger.rewardRecords().getCompound("advancements");
        players.put(player.getUuidAsString(), data);
        ledger.rewardRecords().put("advancements", players);
        ledger.markDirty();
    }

    public static void baseline(ServerPlayerEntity player, PlayerAdvancementTracker tracker) {
        var definitions = MasteryProfileRegistry.server().rewards();
        if (definitions == null || tracker == null || player.getServer() == null || player.getServer().getOverworld() == null) return;
        int epoch = MasteryProfileRegistry.server().epoch();
        if (Objects.equals(BASELINED.get(tracker), epoch)) return;
        BASELINED.put(tracker, epoch);
        NbtCompound data = playerData(player);
        NbtCompound seen = data.getCompound("seen");
        boolean changed = false;
        for (Identifier id : definitions.advancementIds()) {
            AdvancementEntry advancement = player.getServer().getAdvancementLoader().get(id);
            if (advancement == null) continue;
            boolean done = tracker.getProgress(advancement).isDone();
            if (!seen.contains(id.toString()) || done && !seen.getBoolean(id.toString())) {
                seen.putBoolean(id.toString(), done);
                changed = true;
            }
        }
        if (changed) {
            data.put("seen", seen);
            save(player, data);
        }
    }

    public static void completed(ServerPlayerEntity player, AdvancementEntry advancement) {
        var definitions = MasteryProfileRegistry.server().rewards();
        if (definitions == null || player.getServer() == null || player.getServer().getOverworld() == null
                || !definitions.advancementIds().contains(advancement.id())) return;
        NbtCompound data = playerData(player);
        NbtCompound seen = data.getCompound("seen");
        String id = advancement.id().toString();
        if (seen.getBoolean(id)) return;
        seen.putBoolean(id, true);
        data.put("seen", seen);
        NbtList choices = new NbtList();
        for (var weapon : definitions.weapons()) {
            var rule = definitions.advancement(advancement.id(), weapon.item(), weapon.group()).orElse(null);
            if (rule == null) continue;
            NbtCompound choice = new NbtCompound();
            choice.putString("item", weapon.item().toString());
            choice.putString("group", weapon.group().toString());
            choice.putInt("xp", rule.value().reward().xp().orElse(0));
            choice.putInt("points", rule.value().reward().points().orElse(0));
            if (!choices.contains(choice)) choices.add(choice);
        }
        if (!choices.isEmpty()) {
            NbtCompound pending = data.getCompound("pending");
            NbtCompound entitlement = new NbtCompound();
            entitlement.put("choices", choices);
            pending.put(id, entitlement);
            data.put("pending", pending);
        }
        save(player, data);
    }

    public static RewardDefinitions.Reward reward(NbtCompound entitlement, Identifier item, Identifier group) {
        NbtList choices = entitlement.getList("choices", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < choices.size(); i++) {
            NbtCompound choice = choices.getCompound(i);
            if (!choice.getString("item").equals(item.toString()) || !choice.getString("group").equals(group.toString())) continue;
            return new RewardDefinitions.Reward(choice.getInt("xp") > 0 ? Optional.of(choice.getInt("xp")) : Optional.empty(),
                    choice.getInt("points") > 0 ? Optional.of(choice.getInt("points")) : Optional.empty());
        }
        return null;
    }

    public static boolean claim(ServerPlayerEntity player, String id) {
        NbtCompound data = playerData(player);
        NbtCompound pending = data.getCompound("pending");
        if (!pending.contains(id)) return false;
        pending.remove(id);
        data.put("pending", pending);
        save(player, data);
        return true;
    }
}
