package dev.kitteh.factions;

import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.util.RelationUtil;
import dev.kitteh.factions.util.TL;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface Participator {
    String getAccountId();

    OfflinePlayer getOfflinePlayer();

    void msg(String str, Object... args);

    default void msg(TL translation, Object... args) {
        this.msg(translation.toString(), args);
    }

    default String describeTo(@Nullable Participator that) {
        return RelationUtil.describeThatToMe(this, that);
    }

    default String describeTo(@Nullable Participator that, boolean uppercaseFirst) {
        return RelationUtil.describeThatToMe(this, that, uppercaseFirst);
    }

    default Relation getRelationTo(@Nullable Participator that) {
        return RelationUtil.getRelationTo(this, that);
    }

    default Relation getRelationTo(@Nullable Participator that, boolean ignorePeaceful) {
        return RelationUtil.getRelationTo(this, that, ignorePeaceful);
    }

    default TextColor getTextColorTo(@Nullable Participator that) {
        return RelationUtil.getTextColorOfThatToMe(this, that);
    }

    default String getColorStringTo(@Nullable Participator that) {
        return RelationUtil.getColorStringOfThatToMe(this, that);
    }
}
