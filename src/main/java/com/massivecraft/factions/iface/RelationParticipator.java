package com.massivecraft.factions.iface;

import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.util.RelationUtil;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;

public interface RelationParticipator {

    String describeTo(RelationParticipator that);

    String describeTo(RelationParticipator that, boolean ucfirst);

    Relation getRelationTo(RelationParticipator that);

    Relation getRelationTo(RelationParticipator that, boolean ignorePeaceful);

    @Deprecated
    ChatColor getColorTo(RelationParticipator to);

    default TextColor getTextColorTo(RelationParticipator to) {
        return RelationUtil.getTextColorOfThatToMe(this, to);
    }

    String getColorStringTo(RelationParticipator to);
}
