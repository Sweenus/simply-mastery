package net.sweenus.simplymastery.mastery.progression;

import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.UUID;

public record ProgressionOwner(Kind kind, UUID subject, Identifier group) {
    public ProgressionOwner {
        Objects.requireNonNull(kind);
        Objects.requireNonNull(subject);
        Objects.requireNonNull(group);
    }

    public static ProgressionOwner weapon(UUID weapon, Identifier group) {
        return new ProgressionOwner(Kind.WEAPON, weapon, group);
    }

    public static ProgressionOwner player(UUID player, Identifier group) {
        return new ProgressionOwner(Kind.PLAYER, player, group);
    }

    public enum Kind {
        WEAPON,
        PLAYER
    }
}
