package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.item.ItemStack;
import net.sweenus.simplymastery.mastery.state.MasteryRuntimeState;
import net.sweenus.simplymastery.mastery.state.MasteryStateAccess;

final class Phase3MasteryRuntime {
    private Phase3MasteryRuntime() {
    }

    static MasteryRuntimeState.Value value(ItemStack stack, String key, long tick) {
        return MasteryStateAccess.runtime(stack).get("phase3/" + key, tick);
    }

    static void set(ItemStack stack, String key, int amount, long expiresAt, long tick) {
        MasteryStateAccess.writeRuntime(stack, MasteryStateAccess.runtime(stack)
                .put("phase3/" + key, amount, expiresAt, tick));
    }

    static int advance(ItemStack stack, String key, long tick, int maximum, int duration) {
        int amount = Math.min(maximum, value(stack, key, tick).amount() + 1);
        set(stack, key, amount, tick + duration, tick);
        return amount;
    }

    static void clear(ItemStack stack, String key) {
        MasteryStateAccess.writeRuntime(stack, MasteryStateAccess.runtime(stack).clear("phase3/" + key));
    }
}
