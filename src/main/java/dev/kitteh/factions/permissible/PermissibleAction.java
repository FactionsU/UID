package dev.kitteh.factions.permissible;

import dev.kitteh.factions.upgrade.Upgrade;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface PermissibleAction {
    String name();

    String description();

    String shortDescription();

    @Nullable
    Upgrade prerequisite();
}
