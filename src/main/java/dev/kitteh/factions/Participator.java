package dev.kitteh.factions;

import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.RelationUtil;
import dev.kitteh.factions.util.TL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
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
    OfflinePlayer asOfflinePlayer();

    /**
     * Sends a String.format-able message.
     *
     * @param str string
     * @param args args
     */
    void msgLegacy(String str, Object... args);

    /**
     * Sends a String.format-able message.
     *
     * @param translation translatable
     * @param args args
     */
    default void msgLegacy(TL translation, Object... args) {
        this.msgLegacy(translation.toString(), args);
    }

    /**
     * Sends a component.
     *
     * @param component component
     */
    void sendMessage(Component component);

    default void sendRichMessage(String miniMessage, TagResolver... resolvers) {
        this.sendMessage(Mini.parse(miniMessage, resolvers));
    }

    default String describeToLegacy(@Nullable Participator that) {
        return RelationUtil.describeThatToMeLegacy(this, that);
    }

    default String describeToLegacy(@Nullable Participator that, boolean uppercaseFirst) {
        return RelationUtil.describeThatToMeLegacy(this, that, uppercaseFirst);
    }

    default Relation relationTo(@Nullable Participator that) {
        return RelationUtil.getRelationTo(this, that);
    }

    default Relation relationTo(@Nullable Participator that, boolean ignorePeaceful) {
        return RelationUtil.getRelationTo(this, that, ignorePeaceful);
    }

    default TextColor textColorTo(@Nullable Participator that) {
        return RelationUtil.getTextColorOfThatToMe(this, that);
    }

    default String colorLegacyStringTo(@Nullable Participator that) {
        return RelationUtil.getLegacyColorStringOfThatToMe(this, that);
    }
}
