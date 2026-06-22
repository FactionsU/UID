package dev.kitteh.factions.util;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Participator;
import dev.kitteh.factions.permissible.Relation;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("4.0.0")
@Deprecated(forRemoval = true, since = "4.6.0")
public class RelationUtil {
    public static Relation getRelationTo(Participator me, Participator that) {
        return me.relationTo(that, false);
    }

    public static Relation getRelationTo(Participator me, Participator that, boolean ignorePeaceful) {
        return me.relationTo(that, ignorePeaceful);
    }

    public static Faction getFaction(Participator rp) {
       return rp.faction();
    }

    public static TextColor getTextColorOfThatToMe(Participator that, Participator me) {
        return that.textColorTo(me);
    }

    @Deprecated(forRemoval = true, since = "4.0.0")
    public static String describeThatToMeLegacy(Participator that, Participator me, boolean ucfirst) {
        return Mini.toLegacy(that.describeTo(me));
    }

    @Deprecated(forRemoval = true, since = "4.0.0")
    public static String describeThatToMeLegacy(Participator that, Participator me) {
        return Mini.toLegacy(that.describeTo(me));
    }

    @Deprecated(forRemoval = true, since = "4.0.0")
    public static String getLegacyColorStringOfThatToMe(Participator that, Participator me) {
        return LegacyCruft.getLegacyString(that.textColorTo(me));
    }
}
