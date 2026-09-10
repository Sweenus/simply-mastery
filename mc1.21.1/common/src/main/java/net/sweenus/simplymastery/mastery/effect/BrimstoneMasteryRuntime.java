package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.sweenus.simplymastery.mastery.state.MasteryRuntimeState;
import net.sweenus.simplymastery.mastery.state.MasteryStateAccess;

final class BrimstoneMasteryRuntime {
    static final String KINDLING = "brimstone/kindling";
    static final String RITE_ACTIVE = "brimstone/rite_active";
    static final String HEAT_SINK = "brimstone/heat_sink";
    static final String REPRISAL = "brimstone/reprisal";
    static final String FORGED_RESOLVE = "brimstone/forged_resolve";
    static final String ASHEN_STEP = "brimstone/ashen_step";

    private BrimstoneMasteryRuntime() {
    }

    static MasteryRuntimeState.Value value(LivingEntity actor, ItemStack stack, String key, long tick) {
        return PersonalRuntime.read(actor, stack).get(key, tick);
    }

    static void set(LivingEntity actor, ItemStack stack, String key, int amount, long expiresAt, long tick) {
        PersonalRuntime.write(actor, stack,
                PersonalRuntime.read(actor, stack).put(key, amount, expiresAt, tick));
    }

    static void clear(LivingEntity actor, ItemStack stack, String key) {
        PersonalRuntime.write(actor, stack, PersonalRuntime.read(actor, stack).clear(key));
    }

    static int advance(LivingEntity actor, ItemStack stack, String key, long tick, int maximum, int windowTicks) {
        int amount = Math.min(maximum, value(actor, stack, key, tick).amount() + 1);
        set(actor, stack, key, amount, tick + windowTicks, tick);
        return amount;
    }
}
