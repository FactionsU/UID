package com.massivecraft.factions.perms;

import com.massivecraft.factions.Faction;
import net.kyori.adventure.text.Component;

import java.util.Map;

/**
 * A PermSelector tests if a given input Selectable matches or not. For
 * example, a FPlayer might match the PlayerSelector and a Role will never
 * match that selector but might match a RoleAtLeastSelector if it's at least
 * that selector's value.
 * <p>
 * Implementations must be immutable and should extend AbstractSelector (or
 * at least copy its methods to be consistent).
 */
public interface PermSelector {
    /**
     * Describes a type of PermSelector, and can create them.
     */
    interface Descriptor {
        /**
         * Creates this type of PermSelector based on the given input.
         *
         * @param input input
         * @return PermSelector
         */
        PermSelector create(String input);

        /**
         * Name, used for serialization.
         *
         * @return name
         */
        String getName();

        /**
         * Name for display in chat.
         *
         * @return chat display name
         */
        Component getDisplayName();

        /**
         * Gets the options, if there's a reasonable number of choices that
         * can be generated in advance. Key is a full, valid PermSelector
         * string, value is what should be displayed in chat.
         *
         * @param faction faction for context
         * @return map as described
         */
        default Map<String, String> getOptions(Faction faction) {
            return null;
        }

        /**
         * True if accepts empty. Only default is the AllSelector.
         *
         * @return true if this type doesn't need a value
         */
        default boolean acceptsEmpty() {
            return false;
        }

        /**
         * Gets instructions, if any exist. Given if no options are provided.
         *
         * @return instructions
         */
        default String getInstructions() {
            return null;
        }
    }

    /**
     * Gets the descriptor for this selector type.
     *
     * @return descriptor
     */
    Descriptor descriptor();

    /**
     * Tests if the given selectable matches this selector.
     *
     * @param selectable selectable
     * @param faction faction context
     * @return true if matches
     */
    boolean test(Selectable selectable, Faction faction);

    /**
     * Serializes this selector for storage. Unlikely to need to override.
     *
     * @return serialized selector
     */
    default String serialize() {
        return this.descriptor().getName() + ':' + this.serializeValue();
    }

    /**
     * Gets the name of this selector for display in chat.
     *
     * @return display name
     */
    default Component displayName() {
        return this.descriptor().getDisplayName();
    }

    /**
     * Serializes the value of this selector for storage.
     *
     * @return serialized value
     */
    String serializeValue();

    /**
     * Gets the value of this selector for display in chat. Default is plain
     * text without any decoration.
     *
     * @param context faction context
     * @return display value
     */
    default Component displayValue(Faction context) {
        return Component.text(this.serializeValue());
    }
}
