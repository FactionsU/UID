package dev.kitteh.factions.permissible;

import dev.kitteh.factions.util.TextUtil;
import net.kyori.adventure.text.format.TextColor;
import org.jspecify.annotations.NullMarked;

/**
 * Parent interface of roles and relations, who can be granted faction access.
 */
@NullMarked
public interface Permissible extends Selectable {
    String name();

    @Deprecated
    default String chatColor() {
        return TextUtil.getString(this.color());
    }

    TextColor color();

    String translation();
}
