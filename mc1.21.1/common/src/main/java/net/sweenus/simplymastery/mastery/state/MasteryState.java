package net.sweenus.simplymastery.mastery.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record MasteryState(int schemaVersion, Identifier profileId, int profileVersion, int masteryXp,
                           int earnedPoints, List<String> unlockedNodeIds, long mutationRevision) {

    public static final int CURRENT_SCHEMA = 1;
    public static final int MAX_UNLOCKED_IDS = 128;
    public static final Codec<MasteryState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("schema_version").forGetter(MasteryState::schemaVersion),
            Identifier.CODEC.fieldOf("profile_id").forGetter(MasteryState::profileId),
            Codec.INT.fieldOf("profile_version").forGetter(MasteryState::profileVersion),
            Codec.INT.fieldOf("mastery_xp").forGetter(MasteryState::masteryXp),
            Codec.INT.fieldOf("earned_points").forGetter(MasteryState::earnedPoints),
            Codec.STRING.listOf(0, MAX_UNLOCKED_IDS).fieldOf("unlocked_nodes").forGetter(MasteryState::unlockedNodeIds),
            Codec.LONG.fieldOf("mutation_revision").forGetter(MasteryState::mutationRevision)
    ).apply(instance, MasteryState::new));
    public static final PacketCodec<RegistryByteBuf, MasteryState> PACKET_CODEC = PacketCodec.ofStatic(
            MasteryState::encode,
            MasteryState::decode
    );

    public MasteryState {
        schemaVersion = Math.max(1, schemaVersion);
        profileVersion = Math.max(1, profileVersion);
        masteryXp = Math.max(0, masteryXp);
        earnedPoints = Math.max(0, earnedPoints);
        mutationRevision = Math.max(0L, mutationRevision);
        Set<String> unique = new LinkedHashSet<>();
        for (String id : unlockedNodeIds) {
            if (id != null && !id.isBlank() && id.length() <= 64 && unique.size() < MAX_UNLOCKED_IDS) {
                unique.add(id);
            }
        }
        unlockedNodeIds = List.copyOf(unique);
    }

    public static MasteryState initial(MasteryProfile profile, int earnedPoints) {
        return new MasteryState(CURRENT_SCHEMA, profile.id(), profile.version(), 0,
                earnedPoints, List.of(), 0L);
    }

    public boolean owns(String nodeId) {
        return unlockedNodeIds.contains(nodeId);
    }

    public int spentPoints(MasteryProfile profile) {
        return unlockedNodeIds.stream()
                .map(profile::node)
                .flatMap(java.util.Optional::stream)
                .mapToInt(MasteryProfile.Node::cost)
                .sum();
    }

    public int availablePoints(MasteryProfile profile) {
        return Math.max(0, earnedPoints - spentPoints(profile));
    }

    public MasteryState unlock(String nodeId) {
        if (owns(nodeId)) {
            return this;
        }
        List<String> unlocked = new ArrayList<>(unlockedNodeIds);
        unlocked.add(nodeId);
        return new MasteryState(schemaVersion, profileId, profileVersion, masteryXp,
                earnedPoints, unlocked, mutationRevision + 1L);
    }

    public MasteryState awardXp(int amount, int maximumPoints, int baseRequirement, int requirementGrowth) {
        int points = Math.min(maximumPoints, earnedPoints);
        long xp = (long) masteryXp + Math.max(0, amount);
        while (points < maximumPoints) {
            int requirement = Math.max(1, baseRequirement + points * requirementGrowth);
            if (xp < requirement) break;
            xp -= requirement;
            points++;
        }
        if (points >= maximumPoints) xp = 0;
        int boundedXp = (int) Math.min(Integer.MAX_VALUE, xp);
        if (boundedXp == masteryXp && points == earnedPoints) return this;
        return new MasteryState(schemaVersion, profileId, profileVersion, boundedXp,
                points, unlockedNodeIds, mutationRevision + 1L);
    }

    public MasteryState withProgress(int masteryXp, int earnedPoints, int maximumPoints) {
        int points = Math.max(0, Math.min(maximumPoints, earnedPoints));
        int xp = points >= maximumPoints ? 0 : Math.max(0, masteryXp);
        if (xp == this.masteryXp && points == this.earnedPoints) return this;
        return new MasteryState(schemaVersion, profileId, profileVersion, xp,
                points, unlockedNodeIds, mutationRevision + 1L);
    }

    public MasteryState grantPoints(int amount, int maximumPoints) {
        return withProgress(masteryXp, (int) Math.min(Integer.MAX_VALUE,
                (long) earnedPoints + Math.max(0, amount)), maximumPoints);
    }

    public MasteryState respec() {
        if (unlockedNodeIds.isEmpty()) return this;
        return new MasteryState(schemaVersion, profileId, profileVersion, masteryXp,
                earnedPoints, List.of(), mutationRevision + 1L);
    }

    private static void encode(RegistryByteBuf buf, MasteryState state) {
        buf.writeVarInt(state.schemaVersion);
        buf.writeIdentifier(state.profileId);
        buf.writeVarInt(state.profileVersion);
        buf.writeVarInt(state.masteryXp);
        buf.writeVarInt(state.earnedPoints);
        buf.writeVarInt(state.unlockedNodeIds.size());
        for (String id : state.unlockedNodeIds) {
            buf.writeString(id, 64);
        }
        buf.writeVarLong(state.mutationRevision);
    }

    private static MasteryState decode(RegistryByteBuf buf) {
        int schema = buf.readVarInt();
        Identifier profile = buf.readIdentifier();
        int profileVersion = buf.readVarInt();
        int xp = buf.readVarInt();
        int points = buf.readVarInt();
        int count = buf.readVarInt();
        if (count < 0 || count > MAX_UNLOCKED_IDS) {
            throw new IllegalArgumentException("Invalid unlocked mastery node count " + count);
        }
        List<String> unlocked = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            unlocked.add(buf.readString(64));
        }
        return new MasteryState(schema, profile, profileVersion, xp, points, unlocked, buf.readVarLong());
    }
}
