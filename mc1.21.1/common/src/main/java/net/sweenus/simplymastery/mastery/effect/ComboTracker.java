package net.sweenus.simplymastery.mastery.effect;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class ComboTracker {

    private final Map<Key, State> combos = new HashMap<>();

    boolean advance(UUID player, SkillEffectAccess.HandKey hand, String effect, long tick,
                    int windowTicks, int hits) {
        Key key = new Key(player, hand, effect);
        State old = combos.get(key);
        int count = old == null || tick - old.tick > windowTicks ? 1 : old.count + 1;
        if (count >= hits) {
            combos.remove(key);
            return true;
        }
        combos.put(key, new State(count, tick));
        return false;
    }

    void clear(UUID player) {
        combos.keySet().removeIf(key -> key.player.equals(player));
    }

    void clear(UUID player, SkillEffectAccess.HandKey hand) {
        combos.keySet().removeIf(key -> key.player.equals(player) && key.hand == hand);
    }

    int size() {
        return combos.size();
    }

    private record Key(UUID player, SkillEffectAccess.HandKey hand, String effect) {
    }

    private record State(int count, long tick) {
    }
}
