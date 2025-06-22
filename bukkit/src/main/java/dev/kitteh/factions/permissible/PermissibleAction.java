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
     * Simple little record for your convenience.
     *
     * @param name             name
     * @param description      description
     * @param shortDescription shortDescription
     * @see PermissibleAction
     */
    record Simple(String name, String description, String shortDescription) implements PermissibleAction {
        @Override
        public @Nullable Upgrade prerequisite() {
            return null;
        }
    }

    /**
     * Simple little record for your convenience.
     *
     * @param name             name
     * @param description      description
     * @param shortDescription shortDescription
     * @param prerequisite     prerequisite
     * @see PermissibleAction
     */
    record WithPrerequisite(String name, String description, String shortDescription, @Nullable Upgrade prerequisite) implements PermissibleAction {
    }

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
