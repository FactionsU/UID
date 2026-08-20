package dev.kitteh.factions;

import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.util.LegacyCruft;
import dev.kitteh.factions.util.Mini;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// Describes an entity participating in Factions, i.e. a player or faction.
@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public sealed interface Participator permits FPlayer, Faction {
    /// Gets an OfflinePlayer for the given participator.
    ///
    /// @return offline player representation
    OfflinePlayer asOfflinePlayer();

    /// Sends a String.format-able message.
    ///
    /// @param str string
    /// @param args args
    @Deprecated(forRemoval = true, since = "4.0.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    void msgLegacy(String str, Object... args);

    /// Sends a component.
    ///
    /// @param component component
    void sendMessage(Component component);

    /// Sends a MiniMessage message.
    ///
    /// @param miniMessage MiniMessage string
    /// @param resolvers   tag resolvers
    void sendRichMessage(String miniMessage, TagResolver... resolvers);

    @Deprecated(forRemoval = true, since = "4.0.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    default String describeToLegacy(@Nullable Participator that) {
        return Mini.toLegacy(this.describeTo(that));
    }

    @Deprecated(forRemoval = true, since = "4.0.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    default String describeToLegacy(@Nullable Participator that, boolean uppercaseFirst) {
        return Mini.toLegacy(this.describeTo(that));
    }

    /// Gets a colored component describing this to that.
    ///
    /// @see Faction#describeTo(Participator)
    /// @see FPlayer#describeTo(Participator)
    @ApiStatus.AvailableSince("4.6.0")
    Component describeTo(@Nullable Participator that);

    /// Gets the relation between two participators.
    ///
    /// @param that second participator - if null, relationship is neutral
    /// @return relationship
    default Relation relationTo(@Nullable Participator that) {
        return this.relationTo(that, false);
    }

    /// Gets the relation between two participators.
    ///
    /// @param that second participator - if null, relationship is neutral
    /// @param ignorePeaceful if false, return neutral if either faction is peaceful
    /// @return relationship
    default Relation relationTo(@Nullable Participator that, boolean ignorePeaceful) {
        if (that == null) {
            return Relation.NEUTRAL;
        }

        Faction thisFaction = this.faction();
        Faction thatFaction = that.faction();

        if (!thatFaction.isNormal() || !thisFaction.isNormal()) {
            return Relation.NEUTRAL;
        }

        if (thatFaction == thisFaction) {
            return Relation.MEMBER;
        }

        if (!ignorePeaceful && (thisFaction.isPeaceful() || thatFaction.isPeaceful())) {
            return Relation.NEUTRAL;
        }

        if (thisFaction.relationWish(thatFaction).value() >= thatFaction.relationWish(thisFaction).value()) {
            return thatFaction.relationWish(thisFaction);
        }

        return thisFaction.relationWish(thatFaction);
    }

    /// Gets the text color for the faction from the perspective of the observer.
    ///
    /// @param that observing participator
    /// @return text color
    default TextColor textColorTo(@Nullable Participator that) {
        Faction thisFaction = this.faction();
        Faction thatFaction = that == null ? null : that.faction();

        if (thisFaction != thatFaction) {
            if (thisFaction.isPeaceful()) {
                return Confs.tl().colors().relations().getPeaceful();
            }

            if (thisFaction.isSafeZone()) {
                return Confs.tl().colors().factions().getSafezone();
            }

            if (thisFaction.isWarZone()) {
                return Confs.tl().colors().factions().getWarzone();
            }

            if (thisFaction.isWilderness()) {
                return Confs.tl().colors().factions().getWilderness();
            }
        }

        return this.relationTo(that).color();
    }

    @Deprecated(forRemoval = true, since = "4.0.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    default String colorLegacyStringTo(@Nullable Participator that) {
        return LegacyCruft.getLegacyString(this.textColorTo(that));
    }

    /// Gets the faction of the participator, returning self if it is a faction.
    ///
    /// @return faction
    Faction faction();
}
