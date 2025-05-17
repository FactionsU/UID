package dev.kitteh.factions.permissible;

import dev.kitteh.factions.util.TextUtil;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Permissible extends Selectable {
    String name();

    @Deprecated
    default ChatColor chatColor() {
        return TextUtil.getClosest(this.color());
    }

    TextColor color();

    String translation();
}
