package com.massivecraft.factions.integration.permcontext;

import org.bukkit.entity.Player;

import java.util.Set;

/**
 * A context that can be registered to the {@link ContextManager}.
 *
 * @see ContextManager#registerContext(Context)
 */
public interface Context {
    /**
     * Gets the context name, without any namespacing. Format should be
     * lowercase words separated by hyphens ({@code -}).
     *
     * @return name
     */
    String getName();

    /**
     * Gets the namespace for this context. Namespaces should be the plugin
     * name, lowercased. Other plugins using this class should <i>not</i> use
     * {@code factionsuuid} as their namespace.
     *
     * @return namespace
     */
    String getNamespace();

    /**
     * Gets the context name with namespace. The format is {@code namespace:name}.
     *
     * @return namespaced name
     */
    default String getNamespacedName() {
        return this.getNamespace() + ':' + this.getName();
    }

    /**
     * Gets known possible values for the context.
     *
     * @return immutable set of possible values
     */
    Set<String> getPossibleValues();

    /**
     * Gets the values for a given player at this moment in time.
     *
     * @param player player
     * @return immutable set of current values
     */
    Set<String> getValues(Player player);
}
