package dev.kitteh.factions;

import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.RelationUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Describes an entity participating in Factions, i.e. a player or faction.
 */
@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public sealed interface Participator permits FPlayer, Faction {
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
    @Deprecated(forRemoval = true, since = "4.0.0")
    void msgLegacy(String str, Object... args);

    /**
     * Sends a component.
     *
     * @param component component
     */
    void sendMessage(Component component);

    default void sendRichMessage(String miniMessage, TagResolver... resolvers) {
        // TODO is changing this from default an ABI break? Shouldn't even end up here now, but keeping until 5.0.
        if (this instanceof FPlayer fp) {
            fp.sendRichMessage(miniMessage, resolvers);
        } else if (this instanceof Faction f) {
            f.sendRichMessage(miniMessage, resolvers);
        } else { // How did I get here?
            this.sendMessage(Mini.parse(miniMessage, resolvers));
        }
    }

    @Deprecated(forRemoval = true, since = "4.0.0")
    default String describeToLegacy(@Nullable Participator that) {
        return RelationUtil.describeThatToMeLegacy(this, that);
    }

    @Deprecated(forRemoval = true, since = "4.0.0")
    default String describeToLegacy(@Nullable Participator that, boolean uppercaseFirst) {
        return RelationUtil.describeThatToMeLegacy(this, that, uppercaseFirst);
    }

    Component describeTo(@Nullable Participator that);

    default Relation relationTo(@Nullable Participator that) {
        return RelationUtil.getRelationTo(this, that);
    }

    default Relation relationTo(@Nullable Participator that, boolean ignorePeaceful) {
        return RelationUtil.getRelationTo(this, that, ignorePeaceful);
    }

    default TextColor textColorTo(@Nullable Participator that) {
        return RelationUtil.getTextColorOfThatToMe(this, that);
    }

    @Deprecated(forRemoval = true, since = "4.0.0")
    default String colorLegacyStringTo(@Nullable Participator that) {
        return RelationUtil.getLegacyColorStringOfThatToMe(this, that);
    }
}
