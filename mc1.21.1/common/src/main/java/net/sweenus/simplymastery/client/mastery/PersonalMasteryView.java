package net.sweenus.simplymastery.client.mastery;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.mastery.definition.MasteryProfile;
import net.sweenus.simplymastery.mastery.definition.MasteryProfileRegistry;
import net.sweenus.simplymastery.mastery.progression.ProgressionOwner;
import net.sweenus.simplymastery.mastery.state.MasteryState;
import net.sweenus.simplymastery.mastery.state.MasteryStateAccess;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PersonalMasteryView {
    private static ProgressionOwner.Kind mode = ProgressionOwner.Kind.WEAPON;
    private static final Map<Identifier, View> STATES = new HashMap<>();
    private static UUID subject;

    private PersonalMasteryView() {
    }

    public static void install(UUID player, ProgressionOwner.Kind ownership, int epoch, MasteryState state) {
        if (state == null || !player.equals(subject) || mode != ownership) STATES.clear();
        subject = player;
        mode = ownership;
        if (state != null) STATES.put(state.profileId(), new View(epoch, state));
    }

    public static ProgressionOwner.Kind mode() { return mode; }

    public static boolean personal() { return mode == ProgressionOwner.Kind.PLAYER; }

    public static MasteryState read(ItemStack stack, MasteryProfile profile, int initialPoints) {
        if (!personal()) return MasteryStateAccess.read(stack, profile, initialPoints,
                MasteryProfileRegistry.client().policy(profile.progressionGroupId()).pointCap());
        var player = MinecraftClient.getInstance().player;
        View view = STATES.get(profile.id());
        if (player == null || !player.getUuid().equals(subject) || view == null
                || view.epoch != MasteryProfileRegistry.client().epoch()
                || view.state.profileVersion() != profile.version()) return MasteryState.initial(profile, 0);
        return view.state;
    }

    public static void clear() {
        mode = ProgressionOwner.Kind.WEAPON;
        subject = null;
        STATES.clear();
    }

    private record View(int epoch, MasteryState state) {
    }
}
