package dev.kitteh.factions.permissible;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface PermissibleAction {
    String getName();

    String getDescription();

    String getShortDescription();
}
