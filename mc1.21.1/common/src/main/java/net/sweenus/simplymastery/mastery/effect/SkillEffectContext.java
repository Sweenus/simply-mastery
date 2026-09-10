package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplymastery.mastery.state.MasteryState;

public sealed interface SkillEffectContext {

    ServerPlayerEntity player();
    default LivingEntity actor() { return player(); }
    ItemStack stack();
    Hand hand();
    MasteryProfile profile();
    MasteryState state();
    long tick();
    SkillEffectAccess access();

    record Attack(ServerPlayerEntity player, LivingEntity target, ItemStack stack, Hand hand,
                  MasteryProfile profile, MasteryState state, long tick,
                  SkillEffectAccess access) implements SkillEffectContext {
    }

    record Kill(ServerPlayerEntity player, LivingEntity target, ItemStack stack, Hand hand,
                MasteryProfile profile, MasteryState state, long tick,
                SkillEffectAccess access) implements SkillEffectContext {
    }

    record HeldTick(ServerPlayerEntity player, ItemStack stack, Hand hand,
                    MasteryProfile profile, MasteryState state, long tick,
                    SkillEffectAccess access) implements SkillEffectContext {
    }

    record DamageReceived(ServerPlayerEntity player, LivingEntity attacker, float amount, DamageSource source,
                          ItemStack stack, Hand hand, MasteryProfile profile, MasteryState state,
                          long tick, SkillEffectAccess access) implements SkillEffectContext {
        public DamageReceived(ServerPlayerEntity player, LivingEntity attacker, float amount,
                              ItemStack stack, Hand hand, MasteryProfile profile, MasteryState state,
                              long tick, SkillEffectAccess access) {
            this(player, attacker, amount, null, stack, hand, profile, state, tick, access);
        }
    }

    record Projectile(ServerPlayerEntity player, ProjectileEntity projectile, ItemStack stack, Hand hand,
                      MasteryProfile profile, MasteryState state, long tick,
                      SkillEffectAccess access) implements SkillEffectContext {
    }

    record Ability(ServerPlayerEntity player, Identifier abilityId, LivingEntity target,
                   ItemStack stack, Hand hand, MasteryProfile profile, MasteryState state,
                   long tick, SkillEffectAccess access) implements SkillEffectContext {
    }
}
