package net.sweenus.simplymastery.mastery.combat;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.sweenus.simplyswords.api.combat.CombatProvenance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CombatEncounter {
    public UUID id = UUID.randomUUID();
    public double maximumHealth;
    public double budget;
    public double damageFraction;
    public double consumedHealth;
    public double consumedBudget;
    public long sequence;
    public boolean settled;
    public final List<Contribution> contributions = new ArrayList<>();
    public final Map<UUID, Double> farming = new HashMap<>();
    public CombatProvenance killer;
    public boolean killerEligible;

    public NbtCompound write() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("version", 1);
        nbt.putUuid("id", id);
        nbt.putDouble("health", maximumHealth);
        nbt.putDouble("budget", budget);
        nbt.putDouble("fraction", damageFraction);
        nbt.putDouble("consumed_health", consumedHealth);
        nbt.putDouble("consumed_budget", consumedBudget);
        nbt.putLong("sequence", sequence);
        nbt.putBoolean("settled", settled);
        if (killer != null) nbt.put("killer", killer.write());
        nbt.putBoolean("killer_eligible", killerEligible);
        NbtList entries = new NbtList();
        for (Contribution contribution : contributions) {
            NbtCompound entry = new NbtCompound();
            entry.putLong("tick", contribution.tick());
            entry.putDouble("damage", contribution.damage());
            entry.putBoolean("eligible", contribution.eligible());
            if (contribution.provenance() != null) entry.put("provenance", contribution.provenance().write());
            entries.add(entry);
        }
        nbt.put("contributions", entries);
        NbtCompound factors = new NbtCompound();
        farming.forEach((player, factor) -> factors.putDouble(player.toString(), factor));
        nbt.put("farming", factors);
        return nbt;
    }

    public static CombatEncounter read(NbtCompound nbt) {
        if (nbt.getInt("version") != 1) throw new IllegalArgumentException("Unknown encounter version");
        CombatEncounter result = new CombatEncounter();
        result.id = nbt.getUuid("id");
        result.maximumHealth = nonnegative(nbt.getDouble("health"));
        result.budget = nonnegative(nbt.getDouble("budget"));
        result.damageFraction = Math.min(1, nonnegative(nbt.getDouble("fraction")));
        result.consumedHealth = nonnegative(nbt.getDouble("consumed_health"));
        result.consumedBudget = nonnegative(nbt.getDouble("consumed_budget"));
        result.sequence = nbt.getLong("sequence");
        result.settled = nbt.getBoolean("settled");
        if (nbt.contains("killer")) result.killer = CombatProvenance.read(nbt.getCompound("killer"));
        result.killerEligible = nbt.getBoolean("killer_eligible");
        NbtList entries = nbt.getList("contributions", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < entries.size(); i++) {
            NbtCompound entry = entries.getCompound(i);
            result.contributions.add(new Contribution(entry.getLong("tick"), nonnegative(entry.getDouble("damage")),
                    entry.contains("provenance") ? CombatProvenance.read(entry.getCompound("provenance")) : null,
                    entry.getBoolean("eligible")));
        }
        NbtCompound factors = nbt.getCompound("farming");
        for (String key : factors.getKeys()) result.farming.put(UUID.fromString(key), nonnegative(factors.getDouble(key)));
        return result;
    }

    private static double nonnegative(double value) {
        if (!Double.isFinite(value) || value < 0) throw new IllegalArgumentException("Invalid encounter balance");
        return value;
    }

    public record Contribution(long tick, double damage, CombatProvenance provenance, boolean eligible) {
    }
}
