package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.sweenus.simplymastery.mastery.state.MasteryRuntimeState;
import net.sweenus.simplymastery.mastery.state.MasteryStateAccess;

final class StormSoulMasteryRuntime {
    private StormSoulMasteryRuntime() {
    }

    static MasteryRuntimeState.Value value(LivingEntity actor, ItemStack stack, String key, long tick) {
        return PersonalRuntime.read(actor, stack).get("storm_soul/" + key, tick);
    }

    static void set(LivingEntity actor, ItemStack stack, String key, int amount, long expiresAt, long tick) {
        PersonalRuntime.write(actor, stack, PersonalRuntime.read(actor, stack)
                .put("storm_soul/" + key, amount, expiresAt, tick));
    }

    static int advance(LivingEntity actor, ItemStack stack, String key, long tick, int maximum, int duration) {
        int amount = Math.min(maximum, value(actor, stack, key, tick).amount() + 1);
        set(actor, stack, key, amount, tick + duration, tick);
        return amount;
    }

    static void clear(LivingEntity actor, ItemStack stack, String key) {
        PersonalRuntime.write(actor, stack, PersonalRuntime.read(actor, stack).clear("storm_soul/" + key));
    }
}
