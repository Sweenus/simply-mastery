package net.sweenus.simplymastery.mastery.combat;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.sweenus.simplymastery.config.MasteryConfig;
import net.sweenus.simplymastery.mastery.definition.MasteryProfileRegistry;
import net.sweenus.simplymastery.mastery.progression.MasteryProgression;
import net.sweenus.simplymastery.mastery.progression.MasteryProvenance;
import net.sweenus.simplymastery.mastery.progression.MasteryRewardService;
import net.sweenus.simplymastery.mastery.progression.ProgressionLedger;
import net.sweenus.simplyswords.api.combat.CombatProvenance;
import net.sweenus.simplyswords.api.combat.CombatProvenanceApi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class CombatRewards {
    private CombatRewards() {
    }

    public static void record(LivingEntity target, CombatProvenance origin, double damage, boolean lethal) {
        if (!(target.getWorld() instanceof ServerWorld world) || !Double.isFinite(damage) || damage <= 0) return;
        EncounterCarrier carrier = (EncounterCarrier) target;
        CombatEncounter encounter = carrier.simplymastery$getEncounter();
        if (encounter == null) {
            encounter = new CombatEncounter();
            encounter.maximumHealth = Math.max(1, target.getMaxHealth());
            encounter.budget = baseReward(target);
            encounter.damageFraction = Math.clamp(MasteryConfig.SERVER.damageXpBudgetPercent, 0, 100) / 100.0;
            carrier.simplymastery$setEncounter(encounter);
        }
        if (encounter.settled) return;
        long tick = world.getServer().getOverworld().getTime();
        boolean eligible = eligible(target, origin);
        if (eligible && rate(origin) > 0 && origin.weaponPercent() > 0) {
            encounter.farming.computeIfAbsent(origin.player(), player -> farm(world, origin, target, tick));
        }
        encounter.contributions.removeIf(value -> value.tick() < tick - MasteryConfig.SERVER.contributionWindowTicks);
        CombatProvenance retained = encounter.contributions.size() < 8192 ? origin : null;
        var contribution = new CombatEncounter.Contribution(tick, damage, retained, eligible && retained != null);
        if (retained == null && !encounter.contributions.isEmpty()) {
            var last = encounter.contributions.getLast();
            if (last.provenance() == null && last.tick() == tick) {
                encounter.contributions.set(encounter.contributions.size() - 1,
                        new CombatEncounter.Contribution(tick, last.damage() + damage, null, false));
            } else encounter.contributions.add(contribution);
        } else encounter.contributions.add(contribution);
        encounter.sequence++;
        if (lethal) {
            encounter.killer = origin;
            encounter.killerEligible = eligible;
        }
        if (MasteryConfig.SERVER.damageXpEnabled) {
            double health = Math.min(damage, Math.max(0, encounter.maximumHealth - encounter.consumedHealth));
            double share = Math.min(encounter.budget - encounter.consumedBudget,
                    encounter.budget * encounter.damageFraction * health / encounter.maximumHealth);
            encounter.consumedHealth += health;
            encounter.consumedBudget += Math.max(0, share);
            pay(world, target, encounter, origin, eligible, share, "damage", encounter.sequence, false);
        }
    }

    public static void settle(LivingEntity target) {
        if (!(target.getWorld() instanceof ServerWorld world)) return;
        CombatEncounter encounter = ((EncounterCarrier) target).simplymastery$getEncounter();
        if (encounter == null || encounter.settled) return;
        encounter.settled = true;
        double remaining = Math.max(0, encounter.budget - encounter.consumedBudget);
        if (!MasteryConfig.SERVER.contributionSharingEnabled) {
            pay(world, target, encounter, encounter.killer, encounter.killerEligible, remaining, "death", 1, true);
            return;
        }
        long cutoff = world.getServer().getOverworld().getTime() - MasteryConfig.SERVER.contributionWindowTicks;
        List<CombatEncounter.Contribution> recent = encounter.contributions.stream().filter(value -> value.tick() >= cutoff).toList();
        double denominator = recent.stream().mapToDouble(CombatEncounter.Contribution::damage).sum();
        if (denominator <= 0) return;
        Map<Recipient, Double> shares = new LinkedHashMap<>();
        for (var contribution : recent) {
            if (contribution.provenance() == null || !contribution.eligible()) continue;
            shares.merge(new Recipient(contribution.provenance(), contribution.eligible()), contribution.damage(), Double::sum);
        }
        long sequence = 0;
        for (var entry : shares.entrySet()) {
            sequence++;
            pay(world, target, encounter, entry.getKey().provenance(), entry.getKey().eligible(),
                    remaining * entry.getValue() / denominator, "death/" + sequence, 1, true);
        }
    }

    private static void pay(ServerWorld world, LivingEntity target, CombatEncounter encounter,
                            CombatProvenance origin, boolean eligible, double share, String channel, long sequence,
                            boolean firstKill) {
        if (origin == null || !eligible || !eligible(target, origin) || share < 0 || share == 0 && !firstKill || !MasteryConfig.SERVER.enabled
                || MasteryConfig.SERVER.excludedEntityIds.contains(EntityType.getId(target.getType()))) return;
        double multiplier = rate(origin) / 100.0 * origin.weaponPercent() / 100.0
                * encounter.farming.getOrDefault(origin.player(), 1.0);
        if (multiplier <= 0 || !profileEnabled(origin)) return;
        ProgressionLedger ledger = ProgressionLedger.get(world.getServer());
        var owner = MasteryProvenance.owner(origin);
        String key = encounter.id + "/" + channel + "/" + owner.kind() + "/" + owner.subject() + "/" + owner.group();
        if (!ledger.claimSequence(key, sequence)) return;
        MasteryRewardService.award(world.getServer(), owner, share * multiplier, 0);
        if (firstKill && MasteryConfig.SERVER.firstKillRewardsEnabled) {
            var definitions = MasteryProfileRegistry.server().rewards();
            if (definitions == null) return;
            var reward = definitions.entity(EntityType.getId(target.getType())).flatMap(value -> value.value().firstKill());
            if (reward.isPresent() && ledger.firstKill(owner, EntityType.getId(target.getType()))) {
                MasteryRewardService.award(world.getServer(), owner, reward.get().xp().orElse(0), reward.get().points().orElse(0));
            }
        }
    }

    private static boolean profileEnabled(CombatProvenance origin) {
        return MasteryProfileRegistry.server().profiles().values().stream()
                .filter(profile -> profile.progressionGroupId().equals(origin.group()))
                .anyMatch(profile -> !MasteryConfig.SERVER.disabledProfiles.contains(profile.id()));
    }

    private static boolean eligible(LivingEntity target, CombatProvenance origin) {
        if (origin == null || !profileEnabled(origin) || !MasteryConfig.SERVER.enabled
                || MasteryConfig.SERVER.excludedEntityIds.contains(EntityType.getId(target.getType()))) return false;
        ServerPlayerEntity player = target.getServer().getPlayerManager().getPlayer(origin.player());
        var cachedPlayer = target.getServer().getUserCache();
        var name = player != null ? Optional.of(player.getGameProfile().getName())
                : cachedPlayer == null ? Optional.<String>empty()
                : cachedPlayer.getByUuid(origin.player()).map(GameProfile::getName);
        var team = name.map(value -> target.getWorld().getScoreboard().getScoreHolderTeam(value)).orElse(null);
        boolean friendly = target.getUuid().equals(origin.player()) || player != null && target.isTeammate(player) || team != null && team.isEqual(target.getScoreboardTeam());
        var targetOrigin = CombatProvenanceApi.entity(target);
        boolean pet = target instanceof Tameable tame && origin.player().equals(tame.getOwnerUuid())
                || targetOrigin != null && origin.player().equals(targetOrigin.player())
                && ((targetOrigin.origins() | targetOrigin.deliveries()) & CombatProvenance.SUMMON) != 0;
        boolean pvp = target instanceof ServerPlayerEntity;
        boolean invalid = pvp && (((ServerPlayerEntity) target).isCreative() || ((ServerPlayerEntity) target).isSpectator());
        return MasteryProgression.eligibleClassification(friendly, pet, pvp, invalid,
                target instanceof HostileEntity || boss(target), MasteryConfig.SERVER);
    }

    public static double baseReward(LivingEntity target) {
        var definitions = MasteryProfileRegistry.server().rewards();
        var rule = definitions == null ? null : definitions.entity(EntityType.getId(target.getType())).map(value -> value.value()).orElse(null);
        double base = rule != null && rule.baseXp().isPresent() ? rule.baseXp().get()
                : MasteryProgression.calculateXp(target.getMaxHealth(), target instanceof HostileEntity, boss(target), 0, MasteryConfig.SERVER);
        return base * (rule == null ? 1 : rule.xpPercent().orElse(100) / 100.0);
    }

    private static boolean boss(LivingEntity target) {
        boolean vanilla = target instanceof WitherEntity || target instanceof EnderDragonEntity;
        var definitions = MasteryProfileRegistry.server().rewards();
        return definitions == null ? vanilla : definitions.entity(EntityType.getId(target.getType()))
                .flatMap(value -> value.value().boss()).orElse(vanilla);
    }

    private static int rate(CombatProvenance origin) {
        if (origin == null) return 0;
        int flags = origin.origins() | origin.deliveries();
        var config = MasteryConfig.SERVER;
        if ((flags & CombatProvenance.MELEE) != 0 && !config.meleeXpEnabled
                || (flags & CombatProvenance.ABILITY) != 0 && !config.abilityXpEnabled
                || (flags & CombatProvenance.PROJECTILE) != 0 && !config.projectileXpEnabled
                || (flags & CombatProvenance.SUMMON) != 0 && !config.summonXpEnabled
                || (flags & CombatProvenance.DAMAGE_OVER_TIME) != 0 && !config.damageOverTimeXpEnabled) return 0;
        int value = (flags & CombatProvenance.SUMMON) != 0 ? config.summonXpPercent
                : (flags & CombatProvenance.DAMAGE_OVER_TIME) != 0 ? config.damageOverTimeXpPercent
                : (flags & CombatProvenance.PROJECTILE) != 0 ? config.projectileXpPercent
                : (flags & CombatProvenance.ABILITY) != 0 ? config.abilityXpPercent : config.meleeXpPercent;
        return Math.clamp(value, 0, 10000);
    }

    private static double farm(ServerWorld world, CombatProvenance origin, LivingEntity target, long tick) {
        ProgressionLedger ledger = ProgressionLedger.get(world.getServer());
        NbtCompound all = ledger.rewardRecords().getCompound("farming");
        String key = origin.player() + "/" + EntityType.getId(target.getType());
        List<Long> recent = new ArrayList<>();
        for (long previous : all.getLongArray(key)) {
            if (previous >= tick - MasteryConfig.SERVER.antiFarmWindowTicks) recent.add(previous);
        }
        double factor = recent.size() >= MasteryConfig.SERVER.antiFarmFullValueKills
                ? MasteryConfig.SERVER.antiFarmRepeatedPercent / 100.0 : 1;
        recent.add(tick);
        int retain = Math.max(1, MasteryConfig.SERVER.antiFarmFullValueKills);
        all.putLongArray(key, recent.subList(Math.max(0, recent.size() - retain), recent.size()));
        ledger.rewardRecords().put("farming", all);
        ledger.markDirty();
        return factor;
    }

    private record Recipient(CombatProvenance provenance, boolean eligible) {
    }
}
