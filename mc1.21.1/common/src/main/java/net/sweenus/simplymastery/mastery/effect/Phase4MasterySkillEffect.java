package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.SimplyMastery;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplyswords.api.ability.Phase4AbilityTuning;
import net.sweenus.simplyswords.api.ability.Phase4UniqueAbilities;
import net.sweenus.simplyswords.api.ability.UniqueAbilityContext;
import net.sweenus.simplyswords.api.ability.UniqueAbilityDefinition;
import net.sweenus.simplyswords.api.ability.UniqueAbilityTuning;

import java.util.List;
import java.util.Set;

final class Phase4MasterySkillEffect implements AbilitySkillEffectType {
    private static final Identifier ID = Identifier.of(SimplyMastery.MOD_ID, "phase4_mastery");

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public void validate(MasteryProfile.Effect effect, String where, List<String> errors) {
        if (!effect.parameters().keySet().equals(Set.of("kind"))) {
            errors.add(where + "phase4_mastery requires only kind");
        }
        int kind = effect.parameters().getOrDefault("kind", -1);
        if (kind < 0 || kind >= 81) errors.add(where + "kind must be between 0 and 80");
    }

    @Override
    public void tune(UniqueAbilityContext context, UniqueAbilityDefinition definition,
                     UniqueAbilityTuning.Builder tuning, MasteryProfile.Node node) {
        int kind = parameter(node, "kind", -1);
        if (kind < 0 || kind >= 81 || !matches(kind / 27, definition)) return;
        int profile = kind / 27;
        int branch = kind % 27 / 9;
        int slot = kind % 9;
        Phase4AbilityTuning value = tuning.get(Phase4UniqueAbilities.TUNING);
        value = switch (profile) {
            case 0 -> lichblade(value, definition, branch, slot);
            case 1 -> sunfire(value, definition, branch, slot);
            case 2 -> harbinger(value, definition, branch, slot);
            default -> value;
        };
        tuning.set(Phase4UniqueAbilities.TUNING, value);
        if (definition.cooldownKey().isPresent()) {
            tuning.set(Phase4UniqueAbilities.COOLDOWN_TICKS,
                    value.integer(s("COOLDOWN_TICKS"), tuning.get(Phase4UniqueAbilities.COOLDOWN_TICKS)));
        }
    }

    private static Phase4AbilityTuning lichblade(Phase4AbilityTuning t, UniqueAbilityDefinition definition,
                                                  int branch, int slot) {
        boolean aura = definition == Phase4UniqueAbilities.LICHBLADE_AURA;
        if (aura && (branch > 0 || slot >= 6)) return t;
        if (branch == 0) return switch (slot) {
            case 0 -> aura ? t.with(s("INTERVAL_TICKS"), 30) : t;
            case 1 -> t.with(s("RADIUS"), 3.5).with(s("TARGET_CAP"), 24);
            case 2 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 3 -> mode(t, 1).with(s("STATUS_DURATION_TICKS"), 60).with(s("LOCKOUT_TICKS"), 40);
            case 4 -> mode(t, 2).with(s("PER_TARGET_BONUS"), .03).with(s("TARGET_CAP"), 8)
                    .with(s("BONUS_CAP"), .24);
            case 5 -> mode(t, 4).with(s("DEATH_BURST_DAMAGE_MULTIPLIER"), .35)
                    .with(s("DEATH_BURST_RADIUS"), 3).with(s("DEATH_BURST_TARGET_CAP"), 4)
                    .with(s("LOCKOUT_TICKS"), 20);
            case 6 -> mode(t, 8).with(s("INTERVAL_TICKS"), 4).with(s("THRESHOLD"), .3);
            case 7 -> mode(t, 16).with(s("RADIUS"), 6).with(s("TARGET_CAP"), 32)
                    .multiply(s("DAMAGE_MULTIPLIER"), .65, 1).with(s("MOVEMENT_SPEED"), .6)
                    .multiply(s("COOLDOWN_TICKS"), 1.2, 700);
            case 8 -> mode(t, 32).with(s("RADIUS"), 1.5).with(s("TARGET_CAP"), 1)
                    .multiply(s("DAMAGE_MULTIPLIER"), 2, 1);
            default -> t;
        };
        if (branch == 1) return switch (slot) {
            case 0 -> t.with(s("CHANCE"), 12);
            case 1 -> t.with(s("HEAL_AMOUNT"), .75);
            case 2 -> t.with(s("ABSORPTION_CAP"), 10);
            case 3 -> mode(t, 64).with(s("CHARGE_LOCKOUT_TICKS"), 10).with(s("COUNT"), 2);
            case 4 -> mode(t, 128).with(s("COUNT"), 4).with(s("STATUS_DURATION_TICKS"), 120);
            case 5 -> mode(t, 256).with(s("HEAL_MULTIPLIER"), .5).with(s("TEMP_ABSORPTION_CAP"), 4)
                    .with(s("DURATION_TICKS"), 80);
            case 6 -> mode(t, 512).with(s("COUNT"), 10).with(s("PER_TARGET_BONUS"), .1)
                    .with(s("BONUS_CAP"), .4);
            case 7 -> mode(t, 1024).multiply(s("DAMAGE_MULTIPLIER"), .7, 1)
                    .with(s("CHANCE"), 0).with(s("COUNT"), 2).with(s("ABSORPTION_CAP"), 16)
                    .with(s("DURATION_TICKS"), 160);
            case 8 -> mode(t, 2048).with(s("CHANCE"), 25).with(s("HEAL_AMOUNT"), 1)
                    .with(s("ABSORPTION_CAP"), 0).with(s("LOCKOUT_TICKS"), 200);
            default -> t;
        };
        return switch (slot) {
            case 0 -> t.with(s("ACQUISITION_RANGE"), 26);
            case 1 -> t.with(s("INTERVAL_TICKS"), 4);
            case 2 -> t.with(s("DURATION_TICKS"), 240);
            case 3 -> mode(t, 4096).with(s("STATUS_DURATION_TICKS"), 20);
            case 4 -> mode(t, 8192).with(s("SPEED"), 1.5);
            case 5 -> mode(t, 16384).with(s("RETARGET_RANGE"), 8).with(s("RETARGET_CAP"), 1)
                    .with(s("THRESHOLD"), .4);
            case 6 -> t.with(s("COOLDOWN_TICKS"), t.flag(16) ? 720 : 600);
            case 7 -> mode(t, 32768).with(s("RETARGET_RANGE"), 8).with(s("RETARGET_CAP"), 4)
                    .with(s("PER_TARGET_BONUS"), .15).with(s("ABSORPTION_CAP"), 0);
            case 8 -> mode(t, 65536).with(s("RECALL_DAMAGE_MULTIPLIER"), 1.25).with(s("RECALL_RADIUS"), 4);
            default -> t;
        };
    }

    private static Phase4AbilityTuning sunfire(Phase4AbilityTuning t, UniqueAbilityDefinition definition,
                                                int branch, int slot) {
        boolean regen = definition == Phase4UniqueAbilities.SUNFIRE_REGEN;
        if (branch == 0 && !regen) return switch (slot) {
            case 0 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.with(s("RADIUS"), 7).with(s("TARGET_CAP"), 32);
            case 2 -> t.with(s("FIRE_TICKS"), 40);
            case 3 -> mode(t, 1).with(s("COUNT"), 3).with(s("LOCKOUT_TICKS"), 40)
                    .with(s("STATUS_DURATION_TICKS"), 60);
            case 4 -> t.with(s("LANDING_DAMAGE_MULTIPLIER"), 3.5).with(s("LANDING_RADIUS"), 2)
                    .with(s("LANDING_TARGET_CAP"), 12);
            case 5 -> mode(t, 2).with(s("COUNT"), 4).with(s("CYCLE_DAMAGE_MULTIPLIER"), 1.4)
                    .with(s("STATUS_DURATION_TICKS"), 60);
            case 6 -> mode(t, 4).with(s("DURATION_TICKS"), 100).with(s("EARLY_DAMAGE_MULTIPLIER"), 1.2);
            case 7 -> mode(t, 8).with(s("MOVEMENT_SPEED"), .35).with(s("RADIUS"), 5)
                    .multiply(s("DAMAGE_MULTIPLIER"), .75, 1);
            case 8 -> mode(t, 16).with(s("RADIUS"), 9).with(s("INTERVAL_TICKS"), 15)
                    .with(s("DAMAGE_MULTIPLIER"), 1.6).with(s("LIFETIME_MULTIPLIER"), .65);
            default -> t;
        };
        if (branch == 1 && !regen) return switch (slot) {
            case 0 -> t.multiply(s("HEAL_MULTIPLIER"), 1.12, 1);
            case 1 -> t.with(s("SUPPORT_RADIUS"), 7).with(s("SUPPORT_TARGET_CAP"), 16);
            case 2 -> t.with(s("STATUS_DURATION_TICKS"), 120);
            case 3 -> mode(t, 32).with(s("LOCKOUT_TICKS"), 160);
            case 4 -> mode(t, 64).with(s("DURATION_TICKS"), 60);
            case 5 -> mode(t, 128).with(s("THRESHOLD"), .35).with(s("ABSORPTION"), 4)
                    .with(s("DURATION_TICKS"), 100).with(s("LOCKOUT_TICKS"), 200);
            case 6 -> mode(t, 256).with(s("COUNT"), 3).with(s("REFUND_TICKS"), 40);
            case 7 -> mode(t, 512).with(s("SUPPORT_INTERVAL_TICKS"), 40)
                    .with(s("HEAL_MULTIPLIER"), 1.5).with(s("DURATION_TICKS"), 60);
            case 8 -> mode(t, 1024).with(s("SUPPORT_RADIUS"), 5).with(s("STATUS_DURATION_TICKS"), 100)
                    .with(s("DURATION_TICKS"), 60);
            default -> t;
        };
        if (branch != 2 || regen && (slot == 5 || slot == 7)
                || !regen && slot != 5 && slot != 7) return t;
        return switch (slot) {
            case 0 -> t.with(s("CHANCE"), 20);
            case 1 -> t.with(s("STATUS_DURATION_TICKS"), 60);
            case 2 -> mode(t, 2048).with(s("DURATION_TICKS"), 80);
            case 3 -> mode(t, 4096).with(s("ABSORPTION_CAP"), 4).with(s("THRESHOLD"), .5)
                    .with(s("DURATION_TICKS"), 100);
            case 4 -> mode(t, 8192).with(s("FIRE_TICKS"), 40).with(s("LOCKOUT_TICKS"), 40)
                    .with(s("TARGET_CAP"), 8);
            case 5 -> mode(t, 16384).with(s("RANGE"), 7).with(s("DAMAGE_REDUCTION"), .15);
            case 6 -> mode(t, 32768).with(s("THRESHOLD"), .3).with(s("DURATION_TICKS"), 100)
                    .with(s("ABSORPTION"), 4).with(s("LOCKOUT_TICKS"), 600);
            case 7 -> mode(t, 65536).with(s("DURATION_TICKS"), 80).with(s("LOCKOUT_TICKS"), 300)
                    .multiply(s("DAMAGE_MULTIPLIER"), .8, 1);
            case 8 -> mode(t, 131072).with(s("CHANCE"), 0).with(s("COUNT"), 3)
                    .with(s("LOCKOUT_TICKS"), 40).with(s("RADIUS"), 3)
                    .with(s("FLARE_DAMAGE_MULTIPLIER"), .7).with(s("TARGET_CAP"), 8).with(s("HEAL_AMOUNT"), 1);
            default -> t;
        };
    }

    private static Phase4AbilityTuning harbinger(Phase4AbilityTuning t, UniqueAbilityDefinition definition,
                                                  int branch, int slot) {
        boolean omen = definition == Phase4UniqueAbilities.HARBINGER_OMEN;
        if (branch == 0 && !omen) return switch (slot) {
            case 0 -> t.multiply(s("DAMAGE_MULTIPLIER"), 1.1, 1);
            case 1 -> t.with(s("RADIUS"), 7).with(s("TARGET_CAP"), 32);
            case 2 -> t.multiply(s("PULL_STRENGTH"), 1.25, .25);
            case 3 -> mode(t, 1).with(s("COUNT"), 3).with(s("LOCKOUT_TICKS"), 40)
                    .with(s("STATUS_DURATION_TICKS"), 80);
            case 4 -> t.with(s("LANDING_DAMAGE_MULTIPLIER"), 3.5).with(s("LANDING_RADIUS"), 2)
                    .with(s("LANDING_TARGET_CAP"), 12);
            case 5 -> mode(t, 2).with(s("RANGE"), 2).with(s("NEAR_DAMAGE_MULTIPLIER"), 1.2);
            case 6 -> mode(t, 4).with(s("COUNT"), 5).with(s("PULL_STRENGTH"), 1.5)
                    .with(s("CYCLE_DAMAGE_MULTIPLIER"), 1.4).with(s("TARGET_CAP"), 24);
            case 7 -> mode(t, 8).with(s("RANGE"), 12).with(s("MOVEMENT_SPEED"), .3)
                    .with(s("RADIUS"), 4).multiply(s("DAMAGE_MULTIPLIER"), .8, 1);
            case 8 -> mode(t, 16).with(s("RADIUS"), 9).with(s("INTERVAL_TICKS"), 15)
                    .with(s("PULL_STRENGTH"), 1.5).with(s("DAMAGE_MULTIPLIER"), 1.6)
                    .with(s("LIFETIME_MULTIPLIER"), .65);
            default -> t;
        };
        if (branch == 1 && !omen) return switch (slot) {
            case 0 -> t.with(s("STATUS_DURATION_TICKS"), 120);
            case 1 -> t.with(s("SUPPORT_RADIUS"), 7).with(s("SUPPORT_TARGET_CAP"), 16);
            case 2 -> mode(t, 32).with(s("RANGE"), 7);
            case 3 -> mode(t, 64).with(s("DURATION_TICKS"), 80);
            case 4 -> mode(t, 128).with(s("DURATION_TICKS"), 80);
            case 5 -> mode(t, 256).with(s("COUNT"), 3).with(s("REFUND_TICKS"), 40);
            case 6 -> mode(t, 512).with(s("DURATION_TICKS"), 80).with(s("SUPPORT_DAMAGE_BONUS"), .1);
            case 7 -> mode(t, 1024).with(s("SUPPORT_INTERVAL_TICKS"), 40)
                    .with(s("STATUS_AMPLIFIER"), 3).with(s("DURATION_TICKS"), 60)
                    .with(s("SUPPORT_DAMAGE_BONUS"), .15);
            case 8 -> mode(t, 2048).with(s("SUPPORT_RADIUS"), 0).with(s("RADIUS"), 5)
                    .with(s("DAMAGE_MULTIPLIER"), 1.25).with(s("STATUS_DURATION_TICKS"), 60);
            default -> t;
        };
        if (branch != 2 || omen && slot == 6 || !omen && slot < 6) return t;
        return switch (slot) {
            case 0 -> t.with(s("CHANCE"), 20);
            case 1 -> t.with(s("STATUS_DURATION_TICKS"), 220);
            case 2 -> mode(t, 4096).with(s("COUNT"), 3).with(s("DURATION_TICKS"), 80)
                    .with(s("LOCKOUT_TICKS"), 200);
            case 3 -> t.with(s("MELEE_DAMAGE_MULTIPLIER"), 1.1);
            case 4 -> mode(t, 8192).with(s("RANGE"), 10).with(s("PULL_STRENGTH"), .75)
                    .with(s("LOCKOUT_TICKS"), 20);
            case 5 -> mode(t, 16384).with(s("REFUND_TICKS"), 20).with(s("LOCKOUT_TICKS"), 100);
            case 6 -> mode(t, 32768).with(s("THRESHOLD"), .25).with(s("LOW_HEALTH_DAMAGE_MULTIPLIER"), 1.2);
            case 7 -> mode(t, 65536).with(s("CHANCE"), 100).with(s("WEAKENED_DAMAGE_MULTIPLIER"), .8)
                    .with(s("STATUS_DURATION_TICKS"), 120);
            case 8 -> mode(t, 131072).with(s("TARGET_CAP"), 1).with(s("WEAKENED_DAMAGE_MULTIPLIER"), 1.4)
                    .add(s("COOLDOWN_TICKS"), 80, 700);
            default -> t;
        };
    }

    private static boolean matches(int profile, UniqueAbilityDefinition definition) {
        return switch (profile) {
            case 0 -> definition == Phase4UniqueAbilities.LICHBLADE_AURA
                    || definition == Phase4UniqueAbilities.LICHBLADE_CHANNEL;
            case 1 -> definition == Phase4UniqueAbilities.SUNFIRE_STANDARD
                    || definition == Phase4UniqueAbilities.SUNFIRE_REGEN;
            case 2 -> definition == Phase4UniqueAbilities.HARBINGER_STANDARD
                    || definition == Phase4UniqueAbilities.HARBINGER_OMEN;
            default -> false;
        };
    }

    private static Phase4AbilityTuning mode(Phase4AbilityTuning tuning, int bit) {
        return tuning.with(s("MODE"), tuning.integer(s("MODE"), 0) | bit);
    }

    private static Phase4AbilityTuning.Setting s(String name) {
        return Phase4AbilityTuning.Setting.valueOf(name);
    }

    private static int parameter(MasteryProfile.Node node, String key, int fallback) {
        return node.effect().parameters().getOrDefault(key, fallback);
    }
}
