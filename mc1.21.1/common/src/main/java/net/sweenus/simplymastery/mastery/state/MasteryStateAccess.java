package net.sweenus.simplymastery.mastery.state;

import net.minecraft.item.ItemStack;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;

public final class MasteryStateAccess {

    private MasteryStateAccess() {
    }

    public static MasteryState read(ItemStack stack, MasteryProfile profile, int initialPoints) {
        MasteryState state = stack.get(MasteryComponents.MASTERY_STATE.get());
        if (state == null || !state.profileId().equals(profile.id())) {
            return MasteryState.initial(profile, initialPoints);
        }
        return state;
    }

    public static void write(ItemStack stack, MasteryState state) {
        stack.set(MasteryComponents.MASTERY_STATE.get(), state);
    }
}
