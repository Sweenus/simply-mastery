package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.item.ItemStack;
import net.sweenus.simplymastery.mastery.state.MasteryRuntimeState;
import net.sweenus.simplymastery.mastery.state.MasteryStateAccess;

final class StormSoulMasteryRuntime {
    private StormSoulMasteryRuntime() {
    }

    static MasteryRuntimeState.Value value(ItemStack stack, String key, long tick) {
        return MasteryStateAccess.runtime(stack).get("storm_soul/" + key, tick);
    }

    static void set(ItemStack stack, String key, int amount, long expiresAt, long tick) {
        MasteryStateAccess.writeRuntime(stack, MasteryStateAccess.runtime(stack)
                .put("storm_soul/" + key, amount, expiresAt, tick));
    }

    static int advance(ItemStack stack, String key, long tick, int maximum, int duration) {
        int amount = Math.min(maximum, value(stack, key, tick).amount() + 1);
        set(stack, key, amount, tick + duration, tick);
        return amount;
    }

    static void clear(ItemStack stack, String key) {
        MasteryStateAccess.writeRuntime(stack, MasteryStateAccess.runtime(stack).clear("storm_soul/" + key));
    }
}
