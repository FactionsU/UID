/*
 * Copyright (C) 2013 drtshock
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.kitteh.factions.util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.ApiStatus;

import java.text.SimpleDateFormat;

/**
 * An enum for requesting strings from the language file. The contents of this enum file may be subject to frequent
 * changes.
 */
@Deprecated(forRemoval = true, since = "4.5.0")
@ApiStatus.Internal
public enum TL {
    /**
     * Translation meta
     */
    _AUTHOR("misc"),
    _RESPONSIBLE("misc"),
    _LANGUAGE("English"),
    _ENCODING("UTF-8"),
    _LOCALE("en_US"),
    _REQUIRESUNICODE("false"),
    _DEFAULT("true"),
    _STATE("complete"), //incomplete, limited, partial, majority, complete

    /**
     * Localised translation meta
     */
    _LOCAL_AUTHOR("misc"),
    _LOCAL_RESPONSIBLE("misc"),
    _LOCAL_LANGUAGE("English"),
    _LOCAL_REGION("US"),
    _LOCAL_STATE("complete"), //And this is the English version. It's not ever going to be not complete.

    /**
     * Command translations
     */
    COMMAND_CHAT_MODE_PUBLIC("&ePublic chat mode."),
    COMMAND_FLY_CHANGE("&eFaction flight &d%1$s"),
    COMMAND_FLY_DAMAGE("&eFaction flight &ddisabled&e due to entering combat"),
    COMMAND_FLY_AUTO("&eFaction auto flight &d%1$s"),
    COMMAND_FLY_ENEMY_DISABLE("&cEnemy nearby, disabling fly"),
    COMMAND_GRACE_NOT_SET("&eGrace is not active"),
    COMMAND_GRACE_ACTIVE("&eGrace active! No explosions for %s"),
    COMMAND_JOIN_NEGATIVEPOWER("&c%1$s cannot join a faction with a negative power level."),
    COMMAND_KICK_ENEMYTERRITORY("&cYou cannot kick a player in enemy territory"),
    COMMAND_KICK_NEGATIVEPOWER("&cYou cannot kick that member until their power is positive."),

    COMMAND_SHOW_PEACEFUL("This faction is Peaceful"),
    COMMAND_SHOW_INVITATION("invitation is required"),
    COMMAND_SHOW_UNINVITED("no invitation is needed"),
    COMMAND_SHOW_BONUS(" (bonus: "),
    COMMAND_SHOW_PENALTY(" (penalty: "),

    COMMAND_STATUS_ONLINE("Online"),
    COMMAND_STATUS_AGOSUFFIX(" ago."),

    // Rank. Faction: Value

    COMMAND_UNCLAIM_SAFEZONE_SUCCESS("&eSafe zone was unclaimed."),
    COMMAND_UNCLAIM_SAFEZONE_NOPERM("&cThis is a safe zone. You lack permissions to unclaim."),
    COMMAND_UNCLAIM_WARZONE_SUCCESS("&eWar zone was unclaimed."),
    COMMAND_UNCLAIM_WARZONE_NOPERM("&cThis is a war zone. You lack permissions to unclaim."),
    COMMAND_UNCLAIM_UNCLAIMED("%1$s&e unclaimed some of your land."),
    COMMAND_UNCLAIM_UNCLAIMS("&eYou unclaimed this land."),
    COMMAND_UNCLAIM_NOTAMEMBER("&cYou are not a member of any faction."),
    COMMAND_UNCLAIM_WRONGFACTIONOTHER("&cAttempted to unclaim land for incorrect faction."),
    COMMAND_UNCLAIM_LOG("%1$s unclaimed land at (%2$s) from the faction: %3$s"),
    COMMAND_UNCLAIM_WRONGFACTION("&cYou don't own this land."),
    COMMAND_UNCLAIM_TOUNCLAIM("to unclaim this land"),
    COMMAND_UNCLAIM_FORUNCLAIM("for unclaiming this land"),
    COMMAND_UNCLAIM_FACTIONUNCLAIMED("%1$s&e unclaimed some land."),

    /**
     * Leaving - This is accessed through a command, and so it MAY need a COMMAND_* slug :s
     */
    LEAVE_PASSADMIN("&cYou must give the admin role to someone else first."),
    LEAVE_NEGATIVEPOWER("&cYou cannot leave until your power is positive."),
    LEAVE_TOLEAVE("to leave your faction."),
    LEAVE_FORLEAVE("for leaving your faction."),
    LEAVE_LEFT("%s&e left faction %s&e."),
    LEAVE_DISBANDED("&e%s&e was disbanded."),
    LEAVE_DISBANDEDLOG("The faction %s (%s) was disbanded due to the last player (%s) leaving."),

    /**
     * Claiming - Same as above basically. No COMMAND_* because it's not in a command class, but...
     */
    CLAIM_PROTECTED("&cThis land is protected"),
    CLAIM_DISABLED("&cSorry, this world has land claiming disabled."),
    CLAIM_CANTCLAIM("&cYou can't claim land for &d%s&c."),
    CLAIM_CANTUNCLAIM("&cYou can't unclaim land for &d%s&c."),
    CLAIM_ALREADYOWN("%s&e already own this land."),
    CLAIM_MEMBERS("Factions must have at least &d%s&c members to claim land."),
    CLAIM_SAFEZONE("&cYou can not claim a safe zone."),
    CLAIM_WARZONE("&cYou can not claim a war zone."),
    CLAIM_POWER("&cYou can't claim more land! You need more power!"),
    CLAIM_DTR_LAND("&cYou can't claim more land!"),
    CLAIM_LIMIT("&cLimit reached. You can't claim more land!"),
    CLAIM_ALLY("&cYou can't claim the land of your allies."),
    CLAIM_CONTIGIOUS("&cYou can only claim additional land which is connected to your first claim or controlled by another faction!"),
    CLAIM_FACTIONCONTIGUOUS("&cYou can only claim additional land which is connected to your first claim!"),
    CLAIM_PEACEFUL("%s&e owns this land. Your faction is peaceful, so you cannot claim land from other factions."),
    CLAIM_PEACEFULTARGET("%s&e owns this land, and is a peaceful faction. You cannot claim land from them."),
    CLAIM_THISISSPARTA("%s&e owns this land and is strong enough to keep it."),
    CLAIM_BORDER("&cYou must start claiming land at the border of the territory."),
    CLAIM_TOCLAIM("to claim this land"),
    CLAIM_FORCLAIM("for claiming this land"),
    CLAIM_TOOVERCLAIM("to overclaim this land"),
    CLAIM_FOROVERCLAIM("for over claiming this land"),
    CLAIM_CLAIMED("&d%s&e claimed land for &d%s&e from &d%s&e."),
    CLAIM_CLAIMEDLOG("%s claimed land at (%s) for the faction: %s"),
    CLAIM_OVERCLAIM_DISABLED("&eOver claiming is disabled on this server."),
    CLAIM_TOOCLOSETOOTHERFACTION("&eYour claim is too close to another Faction. Buffer required is %d"),
    CLAIM_OUTSIDEWORLDBORDER("&eYour claim is outside the border."),
    CLAIM_OUTSIDEBORDERBUFFER("&eYour claim is outside the border. %d chunks away world edge required."),
    CLAIM_YOUAREHERE("You are here"),

    DURATION_DAYS("%d days"),
    DURATION_DAY("%d day"),
    DURATION_HOURS("%d hours"),
    DURATION_HOUR("%d hour"),
    DURATION_MINUTES("%d minutes"),
    DURATION_MINUTE("%d minute"),
    DURATION_SECONDS("%d seconds"),
    DURATION_SECOND("%d second"),
    DURATION_AND("and"),

    /**
     * More generic, or less easily categorisable translations, which may apply to more than one class
     */
    GENERIC_YOU("you"),
    GENERIC_YOURFACTION("your faction"),
    GENERIC_DEFAULTDESCRIPTION("Default faction description :("),
    GENERIC_FACTIONLESS("factionless"),
    GENERIC_INFINITY("∞"),
    GENERIC_FACTIONTAG_BLACKLIST("&eThat faction tag is blacklisted."),
    GENERIC_FACTIONTAG_TOOSHORT("&eThe faction tag can't be shorter than &d%1$s&e chars."),
    GENERIC_FACTIONTAG_TOOLONG("&eThe faction tag can't be longer than &d%s&e chars."),
    GENERIC_FACTIONTAG_ALPHANUMERIC("&eFaction tag must be alphanumeric. \"&d%s&e\" is not allowed."),

    /**
     * Clip placeholder stuff
     */
    PLACEHOLDER_ROLE_NAME("None"),

    /**
     * Economy stuff
     */

    ECON_OFF("no %s"), // no balance, no value, no refund, etc
    ECON_FORMAT("###,###.###"),
    ECON_DISABLED("Faction econ is disabled."),
    ECON_OVER_BAL_CAP("&4The amount &e%s &4is over Essentials' balance cap."),
    ECON_BALANCE("&6%s's&e balance is &d%s&e."),
    ECON_NOPERM("&d%s&e lacks permission to control &d%s's&e money."),
    ECON_CANTAFFORD_TRANSFER("&d%s&c can't afford to transfer &d%s&c to %s&c."),
    ECON_CANTAFFORD_AMOUNT("&d%s&e can't afford &d%s&e %s."),
    ECON_TRANSFER_UNABLE("Unable to transfer %s&c to &d%s&c from &d%s&c."),
    ECON_TRANSFER_NOINVOKER("&d%s&e was transferred from &d%s&e to &d%s&e."),
    ECON_TRANSFER_GAVE("&d%s&e &dgave %s&e to &d%s&e."),
    ECON_TRANSFER_TOOK("&d%s&e &dtook %s&e from &d%s&e."),
    ECON_TRANSFER_TRANSFER("&d%s&e transferred &d%s&e from &d%s&e to &d%s&e."),

    /**
     * Relations
     */
    RELATION_MEMBER_SINGULAR("member"),
    RELATION_ALLY_SINGULAR("ally"),
    RELATION_TRUCE_SINGULAR("truce"),
    RELATION_NEUTRAL_SINGULAR("neutral"),
    RELATION_ENEMY_SINGULAR("enemy"),

    /**
     * Roles
     */
    ROLE_ADMIN("admin"),
    ROLE_COLEADER("coleader"),
    ROLE_MODERATOR("moderator"),
    ROLE_NORMAL("normal member"),
    ROLE_RECRUIT("recruit"),

    /**
     * In the player and entity listeners
     */
    PLAYER_COMMAND_WARZONE("&cYou can't use the command '%s' in war zone."),
    PLAYER_COMMAND_NEUTRAL("&cYou can't use the command '%s' in neutral territory."),
    PLAYER_COMMAND_ENEMY("&cYou can't use the command '%s' in enemy territory."),
    PLAYER_COMMAND_PERMANENT("&cYou can't use the command '%s' because you are in a permanent faction."),
    PLAYER_COMMAND_ALLY("&cYou can't use the command '%s' in ally territory."),
    PLAYER_COMMAND_TRUCE("&cYou can't use the command '%s' in truce territory."),
    PLAYER_COMMAND_WILDERNESS("&cYou can't use the command '%s' in the wilderness."),

    PLAYER_PORTAL_NOTALLOWED("&cDestination portal can't be created there."),

    PLAYER_POWER_NOLOSS_PEACEFUL("&eYou didn't lose any power since you are in a peaceful faction."),
    PLAYER_POWER_NOLOSS_WORLD("&eYou didn't lose any power due to the world you died in."),
    PLAYER_POWER_NOLOSS_REGION("&eYou didn't lose any power due to the region you were in."),
    PLAYER_POWER_NOLOSS_WILDERNESS("&eYou didn't lose any power since you were in the wilderness."),
    PLAYER_POWER_NOLOSS_WARZONE("&eYou didn't lose any power since you were in a war zone."),
    PLAYER_POWER_LOSS_WARZONE("&cThe world you are in has power loss normally disabled, but you still lost power since you were in a war zone.\n&eYour power is now &d%d / %d"),
    PLAYER_POWER_NOW("&eYour power is now &d%d / %d"),
    PLAYER_POWER_VAMPIRISM_GAIN("&eStole &d%.2f&e power from %s&e. Your power is now &d%d / %d"),

    PLAYER_TELEPORTEDONJOIN("&eYou were teleported out of %s territory"),

    /**
     * The ones here before I started messing around with this
     */
    WILDERNESS("wilderness", "&2Wilderness"),
    WILDERNESS_DESCRIPTION("wilderness-description", ""),
    WARZONE("warzone", "&4Warzone"),
    WARZONE_DESCRIPTION("warzone-description", "Not the safest place to be."),
    SAFEZONE("safezone", "&6Safezone"),
    SAFEZONE_DESCRIPTION("safezone-description", "Free from pvp and monsters."),
    FACTIONS_ANNOUNCEMENT_TOP("faction-announcement-top", "&d--Unread Faction Announcements--"),
    FACTIONS_ANNOUNCEMENT_BOTTOM("faction-announcement-bottom", "&d--Unread Faction Announcements--"),
    FACTION_LOGIN("faction-login", "&e%1$s &9logged in."),
    FACTION_LOGOUT("faction-logout", "&e%1$s &9logged out.."),
    NOFACTION_PREFIX("nofactions-prefix", "&6[&ano-faction&6]&r"),
    DATE_FORMAT("date-format", "MM/d/yy h:ma"), // 3/31/15 07:49AM

    /**
     * Raidable is used in multiple places. Allow more than just true/false.
     */
    RAIDABLE_TRUE("raidable-true", "true"),
    RAIDABLE_FALSE("raidable-false", "false"),
    WARMUPS_NOTIFY_CANCELLED("&cYou have cancelled your pending action!"),
    /**
     * DTR
     */
    DTR_CANNOT_FROZEN("&cAction denied due to frozen DTR"),
    DTR_KICK_PENALTY("&cPenalty DTR lost due to kicking with frozen DTR"),
    DTR_FROZEN_STATUS_MESSAGE("%s"),
    DTR_FROZEN_STATUS_TRUE("Frozen"),
    DTR_FROZEN_STATUS_FALSE("Not frozen"),
    DTR_FROZEN_TIME_MESSAGE("%s"),
    DTR_FROZEN_TIME_NOTFROZEN(""),
    DTR_VAMPIRISM_GAIN("&eStole &d%.2f&e DTR from %s&e. Your DTR is now &d%d"),

    RAIDABLE_NOWRAIDABLE("%s &cis now raidable!"),
    RAIDABLE_NOLONGERRAIDABLE("%s &cis no longer raidable!"),

    FACTION_HOME_UNSET("&cYour faction home has been un-set since it is no longer in your territory."),
    FACTION_NEWLEADER("&eFaction admin &d%s&e has been removed. %s&e has been promoted as the new faction admin."),
    ;

    private String path;
    private final String def;
    private static YamlConfiguration LANG;
    public static SimpleDateFormat sdf;

    /**
     * Lang enum constructor.
     *
     * @param path  The string path.
     * @param start The default string.
     */
    TL(String path, String start) {
        this.path = path;
        this.def = start;
    }

    /**
     * Lang enum constructor. Use this when your desired path simply exchanges '_' for '.'
     *
     * @param start The default string.
     */
    TL(String start) {
        this.path = this.name().replace('_', '.');
        if (this.path.startsWith(".")) {
            path = "root" + path;
        }
        this.def = start;
    }

    /**
     * Set the {@code YamlConfiguration} to use.
     *
     * @param config The config to set.
     */
    public static void setFile(YamlConfiguration config) {
        LANG = config;
        sdf = new SimpleDateFormat(DATE_FORMAT.toString());
    }

    @Override
    public String toString() {
        return ChatColor.translateAlternateColorCodes('&', LANG.getString(this.path, def));
    }

    public String format(Object... args) {
        return String.format(toString(), args);
    }

    /**
     * Get the default value of the path.
     *
     * @return The default value of the path.
     */
    public String getDefault() {
        return this.def;
    }

    /**
     * Get the path to the string.
     *
     * @return The path to the string.
     */
    public String getPath() {
        return this.path;
    }
}
