package net.sweenus.simplymastery.mastery.progression;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.sweenus.simplymastery.mastery.state.MasteryPortfolio;
import net.sweenus.simplymastery.mastery.state.MasteryCooldownState;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ProgressionLedger extends PersistentState {
    private static final Type<ProgressionLedger> TYPE = new Type<>(ProgressionLedger::new,
            ProgressionLedger::read, null);
    private final Map<ProgressionOwner, Entry> records = new HashMap<>();
    private NbtCompound rewardRecords = new NbtCompound();

    public NbtCompound rewardRecords() { return rewardRecords; }

    public boolean claimSequence(String key, long sequence) {
        NbtCompound claims = rewardRecords.getCompound("sequences");
        if (claims.contains(key) && claims.getLong(key) >= sequence) return false;
        claims.putLong(key, sequence);
        rewardRecords.put("sequences", claims);
        markDirty();
        return true;
    }

    public boolean firstKill(ProgressionOwner owner, Identifier entity) {
        Entry entry = records.get(owner);
        if (entry == null || !entry.firstKills.add(entity)) return false;
        markDirty();
        return true;
    }


    public static ProgressionLedger get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, "simplymastery_progression");
    }

    public Entry entry(UUID weapon, Identifier group, int xp, int points) {
        return entry(ProgressionOwner.weapon(weapon, group), xp, points);
    }

    public Entry entry(ProgressionOwner owner, int xp, int points) {
        Entry entry = records.get(owner);
        if (entry == null) {
            entry = new Entry(Math.max(0, xp), Math.clamp(points, 0, 64));
            records.put(owner, entry);
            markDirty();
        }
        return entry;
    }

    public Entry find(UUID weapon, Identifier group) {
        return find(ProgressionOwner.weapon(weapon, group));
    }

    public Entry find(ProgressionOwner owner) {
        return records.get(owner);
    }

    public void writePersonalState(ProgressionOwner owner, MasteryPortfolio allocations,
                                   MasteryCooldownState cooldowns) {
        if (owner.kind() != ProgressionOwner.Kind.PLAYER) {
            throw new IllegalArgumentException("Personal state requires a player progression owner");
        }
        Entry entry = records.get(owner);
        if (entry == null) throw new IllegalArgumentException("Unregistered progression owner " + owner);
        if (allocations.groups().stream().anyMatch(group -> !group.id().equals(owner.group()))) {
            throw new IllegalArgumentException("Personal portfolio belongs to a different progression group");
        }
        if (!Objects.equals(entry.personalPortfolio, allocations)
                || !entry.cooldowns.equals(cooldowns)) {
            entry.personalPortfolio = allocations;
            entry.cooldowns = cooldowns;
            markDirty();
        }
    }

    private static ProgressionLedger read(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        ProgressionLedger ledger = new ProgressionLedger();
        ledger.rewardRecords = nbt.getCompound("rewards").copy();
        int schema = nbt.contains("schema_version") ? nbt.getInt("schema_version") : 1;
        if (schema < 1 || schema > 2) throw new IllegalArgumentException("Unsupported mastery ledger schema " + schema);
        NbtList savedRecords = nbt.getList(schema == 1 ? "weapons" : "records", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < savedRecords.size(); i++) {
            NbtCompound record = savedRecords.getCompound(i);
            ProgressionOwner.Kind kind = schema == 1 ? ProgressionOwner.Kind.WEAPON
                    : ProgressionOwner.Kind.valueOf(record.getString("kind"));
            UUID subject = record.getUuid(schema == 1 ? "weapon" : "subject");
            Identifier group = Identifier.of(record.getString("group"));
            ProgressionOwner owner = new ProgressionOwner(kind, subject, group);
            double xp = record.getDouble("xp");
            if (!Double.isFinite(xp) || xp < 0) throw new IllegalArgumentException("Invalid mastery ledger XP for " + owner);
            if (ledger.records.containsKey(owner)) throw new IllegalArgumentException("Duplicate mastery ledger owner " + owner);
            Entry entry = ledger.entry(owner, 0, record.getInt("points"));
            entry.xp = xp;
            entry.revision = record.getLong("reward_revision");
            NbtList kills = record.getList("first_kills", NbtElement.STRING_TYPE);
            for (int k = 0; k < kills.size(); k++) entry.firstKills.add(Identifier.of(kills.getString(k)));
            if (record.contains("personal_portfolio")) {
                MasteryPortfolio portfolio = MasteryPortfolio.CODEC.parse(NbtOps.INSTANCE,
                        record.get("personal_portfolio")).getOrThrow();
                MasteryCooldownState cooldowns = record.contains("cooldowns")
                        ? MasteryCooldownState.CODEC.parse(NbtOps.INSTANCE, record.get("cooldowns")).getOrThrow()
                        : MasteryCooldownState.EMPTY;
                ledger.writePersonalState(owner, portfolio, cooldowns);
            }
        }
        ledger.setDirty(schema == 1);
        return ledger;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putInt("schema_version", 2);
        nbt.put("rewards", rewardRecords.copy());
        NbtList records = new NbtList();
        this.records.forEach((owner, entry) -> {
            NbtCompound record = new NbtCompound();
            record.putString("kind", owner.kind().name());
            record.putUuid("subject", owner.subject());
            record.putString("group", owner.group().toString());
            record.putDouble("xp", entry.xp);
            record.putLong("reward_revision", entry.revision);
            record.putInt("points", entry.points);
            NbtList kills = new NbtList();
            entry.firstKills.stream().sorted().forEach(id -> kills.add(NbtString.of(id.toString())));
            record.put("first_kills", kills);
            if (entry.personalPortfolio != null) {
                record.put("personal_portfolio", MasteryPortfolio.CODEC.encodeStart(NbtOps.INSTANCE,
                        entry.personalPortfolio).getOrThrow());
                record.put("cooldowns", MasteryCooldownState.CODEC.encodeStart(NbtOps.INSTANCE,
                        entry.cooldowns).getOrThrow());
            }
            records.add(record);
        });
        nbt.remove("weapons");
        nbt.put("records", records);
        return nbt;
    }

    public static final class Entry {
        double xp;
        int points;
        private MasteryPortfolio personalPortfolio;
        private MasteryCooldownState cooldowns = MasteryCooldownState.EMPTY;
        final Set<Identifier> firstKills = new HashSet<>();

        private Entry(double xp, int points) {
            this.xp = xp;
            this.points = points;
        }

        public MasteryPortfolio personalPortfolio() { return personalPortfolio; }
        public MasteryCooldownState cooldowns() { return cooldowns; }
        long revision;
        public long revision() { return revision; }
        public double xp() { return xp; }
        public int points() { return points; }
    }
}
