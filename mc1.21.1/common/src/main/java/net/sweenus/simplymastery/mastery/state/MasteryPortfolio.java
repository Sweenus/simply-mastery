package net.sweenus.simplymastery.mastery.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record MasteryPortfolio(int schemaVersion, List<GroupProgress> groups,
                               List<ProfileLoadout> loadouts, long mutationRevision) {

    public static final int CURRENT_SCHEMA = 1;
    public static final int MAX_GROUPS = 16;
    public static final int MAX_LOADOUTS = 64;
    public static final Codec<MasteryPortfolio> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("schema_version").forGetter(MasteryPortfolio::schemaVersion),
            GroupProgress.CODEC.listOf(0, MAX_GROUPS).fieldOf("groups").forGetter(MasteryPortfolio::groups),
            ProfileLoadout.CODEC.listOf(0, MAX_LOADOUTS).fieldOf("loadouts").forGetter(MasteryPortfolio::loadouts),
            Codec.LONG.fieldOf("mutation_revision").forGetter(MasteryPortfolio::mutationRevision)
    ).apply(instance, MasteryPortfolio::new));
    public static final PacketCodec<RegistryByteBuf, MasteryPortfolio> PACKET_CODEC = PacketCodec.ofStatic(
            MasteryPortfolio::encode, MasteryPortfolio::decode);

    public MasteryPortfolio {
        schemaVersion = Math.max(1, schemaVersion);
        groups = uniqueGroups(groups);
        loadouts = uniqueLoadouts(loadouts);
        mutationRevision = Math.max(0L, mutationRevision);
    }

    public static MasteryPortfolio initial(Identifier groupId, int initialPoints) {
        return new MasteryPortfolio(CURRENT_SCHEMA,
                List.of(new GroupProgress(groupId, 0, initialPoints)), List.of(), 0L);
    }

    public static MasteryPortfolio importLegacy(MasteryState state, Identifier groupId, int initialPoints) {
        if (state == null) return initial(groupId, initialPoints);
        return new MasteryPortfolio(CURRENT_SCHEMA,
                List.of(new GroupProgress(groupId, state.masteryXp(), state.earnedPoints())),
                List.of(new ProfileLoadout(state.profileId(), state.profileVersion(), state.unlockedNodeIds())),
                state.mutationRevision());
    }

    public Optional<GroupProgress> group(Identifier id) {
        return groups.stream().filter(group -> group.id().equals(id)).findFirst();
    }

    public Optional<ProfileLoadout> loadout(Identifier id) {
        return loadouts.stream().filter(loadout -> loadout.profileId().equals(id)).findFirst();
    }

    public MasteryPortfolio reconcile(MasteryProfile profile, int initialPoints) {
        MasteryPortfolio current = ensureGroup(profile.progressionGroupId(), initialPoints);
        GroupProgress group = current.group(profile.progressionGroupId()).orElseThrow();
        ProfileLoadout loadout = current.loadout(profile.id())
                .orElse(new ProfileLoadout(profile.id(), profile.version(), List.of()));
        MasteryState migrated = MasteryStateMigrator.migrate(new MasteryState(
                MasteryState.CURRENT_SCHEMA, profile.id(), loadout.profileVersion(), group.masteryXp(),
                group.earnedPoints(), loadout.unlockedNodeIds(), current.mutationRevision), profile);
        ProfileLoadout resolved = new ProfileLoadout(profile.id(), migrated.profileVersion(),
                migrated.unlockedNodeIds());
        if (current.loadout(profile.id()).filter(resolved::equals).isPresent()) return current;
        return current.putLoadout(resolved);
    }

    public MasteryState activeView(MasteryProfile profile, int initialPoints) {
        MasteryPortfolio current = reconcile(profile, initialPoints);
        GroupProgress group = current.group(profile.progressionGroupId()).orElseThrow();
        ProfileLoadout loadout = current.loadout(profile.id()).orElseThrow();
        return new MasteryState(MasteryState.CURRENT_SCHEMA, profile.id(), loadout.profileVersion(),
                group.masteryXp(), group.earnedPoints(), loadout.unlockedNodeIds(), current.mutationRevision);
    }

    public MasteryPortfolio withState(MasteryProfile profile, MasteryState state, int initialPoints) {
        MasteryPortfolio current = ensureGroup(profile.progressionGroupId(), initialPoints);
        current = current.putGroup(new GroupProgress(profile.progressionGroupId(),
                state.masteryXp(), state.earnedPoints()));
        current = current.putLoadout(new ProfileLoadout(profile.id(), state.profileVersion(),
                state.unlockedNodeIds()));
        return new MasteryPortfolio(CURRENT_SCHEMA, current.groups, current.loadouts,
                Math.max(current.mutationRevision, state.mutationRevision()));
    }

    public MasteryPortfolio awardXp(Identifier groupId, int initialPoints, int amount, int maximumPoints,
                                    int baseRequirement, int requirementGrowth) {
        MasteryPortfolio current = ensureGroup(groupId, initialPoints);
        GroupProgress group = current.group(groupId).orElseThrow();
        int points = Math.min(maximumPoints, group.earnedPoints());
        long xp = (long) group.masteryXp() + Math.max(0, amount);
        while (points < maximumPoints) {
            int requirement = Math.max(1, baseRequirement + points * requirementGrowth);
            if (xp < requirement) break;
            xp -= requirement;
            points++;
        }
        if (points >= maximumPoints) xp = 0;
        GroupProgress updated = new GroupProgress(groupId, (int) Math.min(Integer.MAX_VALUE, xp), points);
        if (updated.equals(group)) return current;
        current = current.putGroup(updated);
        return new MasteryPortfolio(CURRENT_SCHEMA, current.groups, current.loadouts,
                current.mutationRevision + 1L);
    }

    public MasteryPortfolio withProgress(Identifier groupId, int initialPoints, int masteryXp,
                                        int earnedPoints, int maximumPoints) {
        MasteryPortfolio current = ensureGroup(groupId, initialPoints);
        GroupProgress group = current.group(groupId).orElseThrow();
        int points = Math.max(0, Math.min(maximumPoints, earnedPoints));
        GroupProgress updated = new GroupProgress(groupId, points >= maximumPoints ? 0 : Math.max(0, masteryXp),
                points);
        if (updated.equals(group)) return current;
        current = current.putGroup(updated);
        return new MasteryPortfolio(CURRENT_SCHEMA, current.groups, current.loadouts,
                current.mutationRevision + 1L);
    }

    public MasteryPortfolio grantPoints(Identifier groupId, int initialPoints, int amount, int maximumPoints) {
        MasteryPortfolio current = ensureGroup(groupId, initialPoints);
        GroupProgress group = current.group(groupId).orElseThrow();
        return current.withProgress(groupId, initialPoints, group.masteryXp(),
                group.earnedPoints() + Math.max(0, amount), maximumPoints);
    }

    private MasteryPortfolio ensureGroup(Identifier id, int initialPoints) {
        return group(id).isPresent() ? this : putGroup(new GroupProgress(id, 0, initialPoints));
    }

    private MasteryPortfolio putGroup(GroupProgress value) {
        List<GroupProgress> updated = new ArrayList<>(groups);
        updated.removeIf(group -> group.id().equals(value.id()));
        if (updated.size() >= MAX_GROUPS) updated.removeFirst();
        updated.add(value);
        return new MasteryPortfolio(schemaVersion, updated, loadouts, mutationRevision);
    }

    private MasteryPortfolio putLoadout(ProfileLoadout value) {
        List<ProfileLoadout> updated = new ArrayList<>(loadouts);
        updated.removeIf(loadout -> loadout.profileId().equals(value.profileId()));
        if (updated.size() >= MAX_LOADOUTS) updated.removeFirst();
        updated.add(value);
        return new MasteryPortfolio(schemaVersion, groups, updated, mutationRevision);
    }

    private static List<GroupProgress> uniqueGroups(List<GroupProgress> values) {
        Map<Identifier, GroupProgress> unique = new LinkedHashMap<>();
        for (GroupProgress value : values) {
            if (value != null && unique.size() < MAX_GROUPS) unique.putIfAbsent(value.id(), value);
        }
        return List.copyOf(unique.values());
    }

    private static List<ProfileLoadout> uniqueLoadouts(List<ProfileLoadout> values) {
        Map<Identifier, ProfileLoadout> unique = new LinkedHashMap<>();
        for (ProfileLoadout value : values) {
            if (value != null && unique.size() < MAX_LOADOUTS) unique.putIfAbsent(value.profileId(), value);
        }
        return List.copyOf(unique.values());
    }

    private static void encode(RegistryByteBuf buf, MasteryPortfolio portfolio) {
        buf.writeVarInt(portfolio.schemaVersion);
        buf.writeVarInt(portfolio.groups.size());
        portfolio.groups.forEach(group -> group.encode(buf));
        buf.writeVarInt(portfolio.loadouts.size());
        portfolio.loadouts.forEach(loadout -> loadout.encode(buf));
        buf.writeVarLong(portfolio.mutationRevision);
    }

    private static MasteryPortfolio decode(RegistryByteBuf buf) {
        int schema = buf.readVarInt();
        int groupCount = boundedCount(buf.readVarInt(), MAX_GROUPS, "progression groups");
        List<GroupProgress> groups = new ArrayList<>(groupCount);
        for (int i = 0; i < groupCount; i++) groups.add(GroupProgress.decode(buf));
        int loadoutCount = boundedCount(buf.readVarInt(), MAX_LOADOUTS, "profile loadouts");
        List<ProfileLoadout> loadouts = new ArrayList<>(loadoutCount);
        for (int i = 0; i < loadoutCount; i++) loadouts.add(ProfileLoadout.decode(buf));
        return new MasteryPortfolio(schema, groups, loadouts, buf.readVarLong());
    }

    private static int boundedCount(int count, int maximum, String label) {
        if (count < 0 || count > maximum) throw new IllegalArgumentException("Invalid " + label + " count " + count);
        return count;
    }

    public record GroupProgress(Identifier id, int masteryXp, int earnedPoints) {
        public static final Codec<GroupProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("id").forGetter(GroupProgress::id),
                Codec.INT.fieldOf("mastery_xp").forGetter(GroupProgress::masteryXp),
                Codec.INT.fieldOf("earned_points").forGetter(GroupProgress::earnedPoints)
        ).apply(instance, GroupProgress::new));

        public GroupProgress {
            masteryXp = Math.max(0, masteryXp);
            earnedPoints = Math.max(0, earnedPoints);
        }

        private void encode(RegistryByteBuf buf) {
            buf.writeIdentifier(id);
            buf.writeVarInt(masteryXp);
            buf.writeVarInt(earnedPoints);
        }

        private static GroupProgress decode(RegistryByteBuf buf) {
            return new GroupProgress(buf.readIdentifier(), buf.readVarInt(), buf.readVarInt());
        }
    }

    public record ProfileLoadout(Identifier profileId, int profileVersion, List<String> unlockedNodeIds) {
        public static final Codec<ProfileLoadout> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("profile_id").forGetter(ProfileLoadout::profileId),
                Codec.INT.fieldOf("profile_version").forGetter(ProfileLoadout::profileVersion),
                Codec.STRING.listOf(0, MasteryState.MAX_UNLOCKED_IDS).fieldOf("unlocked_nodes")
                        .forGetter(ProfileLoadout::unlockedNodeIds)
        ).apply(instance, ProfileLoadout::new));

        public ProfileLoadout {
            profileVersion = Math.max(1, profileVersion);
            LinkedHashSet<String> unique = new LinkedHashSet<>();
            for (String id : unlockedNodeIds) {
                if (id != null && !id.isBlank() && id.length() <= 64
                        && unique.size() < MasteryState.MAX_UNLOCKED_IDS) unique.add(id);
            }
            unlockedNodeIds = List.copyOf(unique);
        }

        private void encode(RegistryByteBuf buf) {
            buf.writeIdentifier(profileId);
            buf.writeVarInt(profileVersion);
            buf.writeVarInt(unlockedNodeIds.size());
            unlockedNodeIds.forEach(id -> buf.writeString(id, 64));
        }

        private static ProfileLoadout decode(RegistryByteBuf buf) {
            Identifier profile = buf.readIdentifier();
            int version = buf.readVarInt();
            int count = boundedCount(buf.readVarInt(), MasteryState.MAX_UNLOCKED_IDS, "unlocked nodes");
            List<String> nodes = new ArrayList<>(count);
            for (int i = 0; i < count; i++) nodes.add(buf.readString(64));
            return new ProfileLoadout(profile, version, nodes);
        }
    }
}
