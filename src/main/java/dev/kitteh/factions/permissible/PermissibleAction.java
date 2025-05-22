package dev.kitteh.factions.permissible;

import dev.kitteh.factions.upgrade.Upgrade;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Permissible actions, such as the ability to open container blocks.
 */
@NullMarked
public interface PermissibleAction {
    /**
     * Name of this permissible action.
     *
     * @return name
     */
    String name();

    /**
     * Long description, such as "Opening any block that can store items".
     *
     * @return description
     */
    String description();

    /**
     * Short description text, such as "open containers".
     *
     * @return short description
     */
    String shortDescription();

    /**
     * Prerequisite for this permissible action to appear to users, if present.
     *
     * @return prerequisite or null if not present
     */
    @Nullable
    Upgrade prerequisite();
}
