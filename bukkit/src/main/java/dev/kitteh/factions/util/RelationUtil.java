package dev.kitteh.factions.util;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.Participator;
import dev.kitteh.factions.permissible.Relation;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.ApiStatus;

public class RelationUtil {
    @ApiStatus.Obsolete
    @Deprecated(forRemoval = true, since = "4.0.0")
    public static String describeThatToMeLegacy(Participator that, Participator me, boolean ucfirst) {
        String ret = "";

        Faction thatFaction = getFaction(that);
        if (thatFaction == null) {
            return "ERROR"; // ERROR
        }

        Faction myFaction = getFaction(me);
//		if (myFaction == null) return that.describeTo(null); // no relation, but can show basic name or tag

        if (that instanceof Faction) {
            if (me instanceof FPlayer && myFaction == thatFaction) {
                ret = TL.GENERIC_YOURFACTION.toString();
            } else {
                ret = thatFaction.tag();
            }
        } else if (that instanceof FPlayer fplayerthat) {
            if (that == me) {
                ret = TL.GENERIC_YOU.toString();
            } else if (thatFaction == myFaction) {
                ret = fplayerthat.nameWithTitleLegacy();
            } else {
                ret = fplayerthat.nameWithTagLegacy();
            }
        }

        if (ucfirst) {
            ret = TextUtil.upperCaseFirst(ret);
        }

        return getLegacyColorStringOfThatToMe(that, me) + ret;
    }

    @ApiStatus.Obsolete
    @Deprecated(forRemoval = true, since = "4.0.0")
    public static String describeThatToMeLegacy(Participator that, Participator me) {
        return describeThatToMeLegacy(that, me, false);
    }

    public static Relation getRelationTo(Participator me, Participator that) {
        return getRelationTo(that, me, false);
    }

    public static Relation getRelationTo(Participator me, Participator that, boolean ignorePeaceful) {
        Faction fthat = getFaction(that);
        if (fthat == null) {
            return Relation.NEUTRAL; // ERROR
        }

        Faction fme = getFaction(me);
        if (fme == null) {
            return Relation.NEUTRAL; // ERROR
        }

        if (!fthat.isNormal() || !fme.isNormal()) {
            return Relation.NEUTRAL;
        }

        if (fthat.equals(fme)) {
            return Relation.MEMBER;
        }

        if (!ignorePeaceful && (fme.isPeaceful() || fthat.isPeaceful())) {
            return Relation.NEUTRAL;
        }

        if (fme.relationWish(fthat).value >= fthat.relationWish(fme).value) {
            return fthat.relationWish(fme);
        }

        return fme.relationWish(fthat);
    }

    public static Faction getFaction(Participator rp) {
        if (rp instanceof Faction faction) {
            return faction;
        }

        if (rp instanceof FPlayer player) {
            return player.faction();
        }

        // ERROR
        return null;
    }

    @ApiStatus.Obsolete
    @Deprecated(forRemoval = true, since = "4.0.0")
    public static String getLegacyColorStringOfThatToMe(Participator that, Participator me) {
        return TextUtil.getLegacyString(getTextColorOfThatToMe(that, me));
    }

    public static TextColor getTextColorOfThatToMe(Participator that, Participator me) {
        Faction thatFaction = getFaction(that);
        if (thatFaction != null) {
            if (thatFaction.isPeaceful() && thatFaction != getFaction(me)) {
                return FactionsPlugin.instance().conf().colors().relations().getPeaceful();
            }

            if (thatFaction.isSafeZone() && thatFaction != getFaction(me)) {
                return FactionsPlugin.instance().conf().colors().factions().getSafezone();
            }

            if (thatFaction.isWarZone() && thatFaction != getFaction(me)) {
                return FactionsPlugin.instance().conf().colors().factions().getWarzone();
            }
        }

        return getRelationTo(that, me).color();
    }
}
