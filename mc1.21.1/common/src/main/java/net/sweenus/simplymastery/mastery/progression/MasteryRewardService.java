package net.sweenus.simplymastery.mastery.progression;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.config.MasteryConfig;
import net.sweenus.simplymastery.mastery.RunicForgeMasteryContext;
import net.sweenus.simplymastery.mastery.definition.MasteryProfileRegistry;
import net.sweenus.simplymastery.mastery.state.MasteryComponents;
import net.sweenus.simplymastery.mastery.state.MasteryPortfolio;
import net.sweenus.simplymastery.mastery.state.MasteryStateAccess;
import net.sweenus.simplyswords.screen.RunicForgeScreenHandler;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class MasteryRewardService {
    private MasteryRewardService() {
    }

    public static void init() {
        TickEvent.SERVER_POST.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                boolean changed = false;
                for (int slot = 0; slot < player.getInventory().size(); slot++) {
                    changed |= synchronize(server, player.getInventory().getStack(slot));
                }
                if (changed) player.getInventory().markDirty();
                changed |= synchronize(server, player.currentScreenHandler.getCursorStack());
                Set<Inventory> dirty =
                        Collections.newSetFromMap(new IdentityHashMap<>());
                for (var slot : player.currentScreenHandler.slots) {
                    if (slot.inventory == player.getInventory()) continue;
                    boolean storage = storageSlot(player, slot);
                    if (storage && synchronize(server, slot.getStack())) dirty.add(slot.inventory);
                }
                dirty.forEach(Inventory::markDirty);
                changed |= !dirty.isEmpty();
                if (player.currentScreenHandler instanceof RunicForgeScreenHandler forge
                        && synchronize(server, RunicForgeMasteryContext.stateStack(forge))) {
                    forge.getForgeInventory().markDirty();
                    changed = true;
                }
                if (changed) player.currentScreenHandler.sendContentUpdates();
            }
        });
    }

    public static boolean accessible(ServerPlayerEntity player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            if (stack == player.getInventory().getStack(slot)) return true;
        }
        if (stack == player.currentScreenHandler.getCursorStack()) return true;
        if (player.currentScreenHandler instanceof RunicForgeScreenHandler forge
                && stack == RunicForgeMasteryContext.stateStack(forge)) return true;
        return player.currentScreenHandler.slots.stream().anyMatch(slot -> storageSlot(player, slot)
                && slot.getStack() == stack);
    }

    private static boolean storageSlot(ServerPlayerEntity player, Slot slot) {
        return slot.inventory instanceof BlockEntity
                || slot.inventory instanceof DoubleInventory
                || slot.inventory instanceof EnderChestInventory
                || player.currentScreenHandler instanceof GenericContainerScreenHandler
                || player.currentScreenHandler instanceof ShulkerBoxScreenHandler
                || player.currentScreenHandler instanceof Generic3x3ContainerScreenHandler
                || player.currentScreenHandler instanceof HopperScreenHandler
                || player.currentScreenHandler instanceof HorseScreenHandler;
    }

    public static UUID identify(MinecraftServer server, ItemStack stack, Identifier group) {
        UUID id = stack.get(MasteryComponents.WEAPON_ID.get());
        if (id == null) {
            id = UUID.randomUUID();
            stack.set(MasteryComponents.WEAPON_ID.get(), id);
        }
        if (ProgressionOwnership.personal(server)) return id;
        MasteryPortfolio portfolio = MasteryStateAccess.portfolio(stack);
        if (portfolio == null) {
            portfolio = MasteryPortfolio.importLegacy(stack.get(MasteryComponents.MASTERY_STATE.get()),
                    group, initialPoints(group));
        }
        ProgressionLedger ledger = ProgressionLedger.get(server);
        for (MasteryPortfolio.GroupProgress progress : portfolio.groups()) {
            ledger.entry(id, progress.id(), progress.masteryXp(), progress.earnedPoints());
        }
        ledger.entry(id, group, 0, initialPoints(group));
        return id;
    }

    public static boolean synchronize(MinecraftServer server, ItemStack stack) {
        if (stack.isEmpty()) return false;
        var resolution = MasteryProfileRegistry.resolveProgressionServer(stack).orElse(null);
        if (resolution == null) return false;
        boolean newIdentity = !stack.contains(MasteryComponents.WEAPON_ID.get());
        UUID id = identify(server, stack, resolution.groupId());
        if (ProgressionOwnership.personal(server)) return newIdentity;
        ProgressionLedger.Entry entry = ProgressionLedger.get(server).find(id, resolution.groupId());
        MasteryPortfolio previous = MasteryStateAccess.portfolio(stack);
        MasteryStateAccess.setProgress(stack, resolution.groupId(), initialPoints(resolution.groupId()),
                (int) Math.min(Integer.MAX_VALUE, entry.xp()), entry.points(), cap(resolution.groupId()));
        resolution.visibleProfile().ifPresent(profile -> MasteryStateAccess.read(stack, profile, initialPoints(resolution.groupId())));
        return newIdentity || !Objects.equals(previous, MasteryStateAccess.portfolio(stack));
    }

    public static ProgressionOwner owner(ServerPlayerEntity player, ItemStack stack, Identifier group) {
        UUID weapon = identify(player.getServer(), stack, group);
        ProgressionOwner owner = ProgressionOwnership.personal(player.getServer())
                ? ProgressionOwner.player(player.getUuid(), group) : ProgressionOwner.weapon(weapon, group);
        ProgressionLedger.get(player.getServer()).entry(owner, 0, initialPoints(group));
        return owner;
    }

    public static boolean award(ServerPlayerEntity player, ItemStack stack, Identifier group, double xp, int points) {
        boolean changed = award(player.getServer(), owner(player, stack, group), xp, points);
        synchronize(player.getServer(), stack);
        return changed;
    }

    public static MasteryPortfolio.GroupProgress progress(ServerPlayerEntity player, ItemStack stack, Identifier group) {
        ProgressionLedger.Entry entry = ProgressionLedger.get(player.getServer()).find(owner(player, stack, group));
        return new MasteryPortfolio.GroupProgress(group, (int) Math.min(Integer.MAX_VALUE, entry.xp()),
                Math.min(cap(group), entry.points()));
    }

    public static boolean setXp(ServerPlayerEntity player, ItemStack stack, Identifier group, int xp) {
        ProgressionLedger ledger = ProgressionLedger.get(player.getServer());
        ProgressionLedger.Entry entry = ledger.find(owner(player, stack, group));
        double updated = entry.points >= cap(group) ? entry.xp : Math.max(0, xp);
        if (entry.xp == updated) return false;
        entry.xp = updated;
        entry.revision++;
        ledger.markDirty();
        synchronize(player.getServer(), stack);
        return true;
    }

    public static boolean set(ServerPlayerEntity player, ItemStack stack, Identifier group, int xp, int points) {
        ProgressionLedger ledger = ProgressionLedger.get(player.getServer());
        ProgressionLedger.Entry entry = ledger.find(owner(player, stack, group));
        int boundedPoints = Math.clamp(points, 0, cap(group));
        double boundedXp = boundedPoints >= cap(group) ? 0 : Math.max(0, xp);
        boolean changed = entry.points != boundedPoints || entry.xp != boundedXp;
        entry.points = boundedPoints;
        entry.xp = boundedXp;
        if (changed) { entry.revision++; ledger.markDirty(); }
        synchronize(player.getServer(), stack);
        return changed;
    }

    public static boolean award(MinecraftServer server, ItemStack stack, Identifier group, double xp, int points) {
        UUID id = identify(server, stack, group);
        boolean changed = award(server, id, group, xp, points);
        synchronize(server, stack);
        return changed;
    }

    public static boolean award(MinecraftServer server, UUID weapon, Identifier group, double xp, int points) {
        return award(server, ProgressionOwner.weapon(weapon, group), xp, points);
    }

    public static boolean award(MinecraftServer server, ProgressionOwner owner, double xp, int points) {
        if (!MasteryConfig.SERVER.enabled || !Double.isFinite(xp) || xp < 0 || points < 0) return false;
        ProgressionLedger ledger = ProgressionLedger.get(server);
        ProgressionLedger.Entry entry = ledger.find(owner);
        if (entry == null || entry.points >= cap(owner.group())) return false;
        double beforeXp = entry.xp;
        int beforePoints = entry.points;
        entry.points = (int) Math.min(cap(owner.group()), (long) entry.points + points);
        entry.xp = Math.min(Double.MAX_VALUE, entry.xp + xp);
        var policy = MasteryProfileRegistry.server().policy(owner.group());
        while (entry.points < policy.pointCap()) {
            long cost = policy.nextCost(entry.points);
            if (entry.xp < cost) break;
            entry.xp -= cost;
            entry.points++;
        }
        if (entry.points >= cap(owner.group())) entry.xp = 0;
        boolean changed = beforeXp != entry.xp || beforePoints != entry.points;
        if (changed) { entry.revision++; ledger.markDirty(); }
        return changed;
    }

    public static boolean set(MinecraftServer server, ItemStack stack, Identifier group, int xp, int points) {
        UUID id = identify(server, stack, group);
        ProgressionLedger ledger = ProgressionLedger.get(server);
        ProgressionLedger.Entry entry = ledger.find(id, group);
        int boundedPoints = Math.clamp(points, 0, cap(group));
        double boundedXp = boundedPoints >= cap(group) ? 0 : Math.max(0, xp);
        boolean changed = entry.points != boundedPoints || entry.xp != boundedXp;
        entry.points = boundedPoints;
        entry.xp = boundedXp;
        if (changed) { entry.revision++; ledger.markDirty(); }
        synchronize(server, stack);
        return changed;
    }

    public static int cap(Identifier group) { return MasteryProfileRegistry.server().policy(group).pointCap(); }
    public static int initialPoints(Identifier group) {
        return Math.clamp(MasteryConfig.SERVER.verticalSliceStartingPoints, 0, cap(group));
    }

}
