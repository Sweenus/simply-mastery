package net.sweenus.simplymastery.mastery.effect;

import net.minecraft.util.Identifier;
import net.sweenus.simplymastery.SimplyMastery;

public final class Phase3ContractChecks {

    private Phase3ContractChecks() {
    }

    public static void requireEffect(String path) {
        Identifier id = Identifier.of(SimplyMastery.MOD_ID, path);
        if (SkillEffectRegistry.get(id) == null) throw new IllegalStateException("Missing skill effect " + id);
    }

    public static void requireRecursionGuard() {
        boolean outer = SkillOriginGuard.run(new SkillOriginGuard.Origin(null, null, "test"),
                () -> !SkillOriginGuard.run(new SkillOriginGuard.Origin(null, null, "nested"), () -> true));
        if (!outer || SkillOriginGuard.active()) throw new IllegalStateException("Skill recursion guard leaked");
    }
}
