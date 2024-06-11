package dev.kitteh.factions;

import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.util.RelationUtil;
import dev.kitteh.factions.util.TL;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

public interface Participator {
    String getAccountId();

    OfflinePlayer getOfflinePlayer();

    void msg(String str, Object... args);

    void msg(TL translation, Object... args);

    String describeTo(Participator that);

    String describeTo(Participator that, boolean uppercaseFirst);

    Relation getRelationTo(Participator that);

    Relation getRelationTo(Participator that, boolean ignorePeaceful);

    default TextColor getTextColorTo(Participator to) {
        return RelationUtil.getTextColorOfThatToMe(this, to);
    }

    String getColorStringTo(Participator to);
}
