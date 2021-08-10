package com.massivecraft.factions.perms;

import com.massivecraft.factions.Faction;
import net.kyori.adventure.text.Component;

import java.util.Map;

/**
 * Implementations must be immutable and should extend AbstractSelector (or
 * at least copy its methods to be consistent).
 */
public interface PermSelector {
    interface Descriptor {
        PermSelector create(String input);

        String getName();

        Component getDisplayName();

        default Map<String, String> getOptions(Faction faction) {
            return null;
        }

        default boolean acceptsEmpty() {
            return false;
        }

        default String getInstructions() {
            return null;
        }
    }

    Descriptor descriptor();

    boolean test(Selectable selectable, Faction faction);

    default String serialize() {
        return this.descriptor().getName() + ':' + this.serializeValue();
    }

    default Component displayName() {
        return this.descriptor().getDisplayName();
    }

    String serializeValue();

    default Component displayValue(Faction context) {
        return Component.text(this.serializeValue());
    }
}
