package net.sweenus.simplyswordsmastery.client.mastery;

import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.sweenus.simplyswordsmastery.mastery.state.MasteryComponents;

import java.util.Objects;

public final class MasteryItemComparison {
    private MasteryItemComparison() {
    }

    public static ItemStack refreshBookkeeping(ItemStack previous, ItemStack current) {
        if (previous == current || previous == null || current == null
                || previous.isEmpty() || current.isEmpty()
                || !previous.isOf(current.getItem()) || previous.getCount() != current.getCount()
                || !Objects.equals(previous.get(MasteryComponents.WEAPON_ID.get()),
                        current.get(MasteryComponents.WEAPON_ID.get()))) {
            return previous;
        }
        ItemStack result = previous;
        result = refresh(result, previous, current, MasteryComponents.MASTERY_STATE.get());
        result = refresh(result, previous, current, MasteryComponents.MASTERY_PORTFOLIO.get());
        result = refresh(result, previous, current, MasteryComponents.MASTERY_COOLDOWNS.get());
        return refresh(result, previous, current, MasteryComponents.MASTERY_RUNTIME.get());
    }

    public static boolean equivalent(ItemStack previous, ItemStack current) {
        if (previous == current) return true;
        if (previous == null || current == null) return false;
        return ItemStack.areEqual(refreshBookkeeping(previous, current), current);
    }

    private static <T> ItemStack refresh(ItemStack result, ItemStack previous, ItemStack current,
                                         ComponentType<T> type) {
        T value = current.get(type);
        if (Objects.equals(previous.get(type), value)) return result;
        if (result == previous) result = previous.copy();
        if (value == null) result.remove(type);
        else result.set(type, value);
        return result;
    }
}
