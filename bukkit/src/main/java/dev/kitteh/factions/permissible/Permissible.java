package dev.kitteh.factions.permissible;

import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/**
 * Parent interface of roles and relations, who can be granted faction access.
 */
@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public interface Permissible extends Selectable {
    String name();

    TextColor color();

    String translation();
}
