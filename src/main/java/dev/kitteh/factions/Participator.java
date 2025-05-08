package dev.kitteh.factions;

import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.util.RelationUtil;
import dev.kitteh.factions.util.TL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Describes an entity participating in Factions, i.e. a player or faction.
 */
@NullMarked
public interface Participator {
    /**
     * Gets an OfflinePlayer for the given participator.
     *
     * @return offline player representation
     */
    OfflinePlayer getOfflinePlayer();

    /**
     * Sends a String.format-able message.
     *
     * @param str string
     * @param args args
     */
    void msg(String str, Object... args);

    /**
     * Sends a String.format-able message.
     *
     * @param translation translatable
     * @param args args
     */
    default void msg(TL translation, Object... args) {
        this.msg(translation.toString(), args);
    }

    /**
     * Sends a component.
     *
     * @param component component
     */
    void sendMessage(Component component);

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
