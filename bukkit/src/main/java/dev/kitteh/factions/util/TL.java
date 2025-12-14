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
@ApiStatus.Obsolete
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
    COMMAND_ADMIN_NOTMEMBER("%1$s&e is not a member in your faction."),
    COMMAND_ADMIN_NOTADMIN("&cYou are not the faction admin."),
    COMMAND_ADMIN_TARGETSELF("&cThe target player mustn't be yourself."),
    COMMAND_ADMIN_PROMOTED("%1$s&e gave %2$s&e the leadership of %3$s&e."),

    COMMAND_AHOME_DESCRIPTION("Send a player to their f home no matter what."),
    COMMAND_AHOME_NOHOME("%1$s doesn't have an f home."),
    COMMAND_AHOME_SUCCESS("%1$s was sent to their f home."),
    COMMAND_AHOME_OFFLINE("%1$s is offline."),
    COMMAND_AHOME_TARGET("You were sent to your f home."),

    COMMAND_ANNOUNCE_DESCRIPTION("Announce a message to players in faction."),

    COMMAND_AUTOCLAIM_ENABLED("&eNow auto-claiming land for &d%1$s&e."),
    COMMAND_AUTOCLAIM_DISABLED("&eAuto-claiming of land disabled."),
    COMMAND_AUTOCLAIM_OTHERFACTION("&cYou can't claim land for &d%1$s&c."),

    COMMAND_AUTOUNCLAIM_ENABLED("&eNow auto-unclaiming land for &d%1$s&e."),
    COMMAND_AUTOUNCLAIM_DISABLED("&eAuto-unclaiming of land disabled."),
    COMMAND_AUTOUNCLAIM_OTHERFACTION("&cYou can't unclaim land for &d%1$s&c."),

    COMMAND_BAN_DESCRIPTION("Ban players from joining your Faction."),
    COMMAND_BAN_TARGET("&cYou were banned from &7%1$s"), // banned player perspective
    COMMAND_BAN_BANNED("&e%1$s &cbanned &7%2$s"),
    COMMAND_BAN_ALREADYBANNED("&c%1$s is already banned"),
    COMMAND_BAN_SELF("&cYou may not ban yourself"),
    COMMAND_BAN_INSUFFICIENTRANK("&cYour rank is too low to ban &7%1$s"),

    COMMAND_BANLIST_DESCRIPTION("View a Faction's ban list"),
    COMMAND_BANLIST_HEADER("&6There are &c%d&6 bans for %s"),
    COMMAND_BANLIST_ENTRY("&7%d. &c%s &r- &a%s &r- &e%s"),
    COMMAND_BANLIST_NOFACTION("&4You are not in a Faction."),

    COMMAND_BOOM_PEACEFULONLY("&cThis command is only usable by factions which are specifically designated as peaceful."),
    COMMAND_BOOM_TOTOGGLE("to toggle explosions"),
    COMMAND_BOOM_FORTOGGLE("for toggling explosions"),
    COMMAND_BOOM_ENABLED("%1$s&e has %2$s explosions in your faction's territory."),
    COMMAND_BOOM_DESCRIPTION("Toggle explosions (peaceful factions only)"),

    COMMAND_BYPASS_ENABLE("&eYou have enabled admin bypass mode. You will be able to build or destroy anywhere."),
    COMMAND_BYPASS_ENABLELOG(" has ENABLED admin bypass mode."),
    COMMAND_BYPASS_DISABLE("&eYou have disabled admin bypass mode."),
    COMMAND_BYPASS_DISABLELOG(" has DISABLED admin bypass mode."),
    COMMAND_BYPASS_DESCRIPTION("Enable admin bypass mode"),

    COMMAND_CHAT_DESCRIPTION("Change chat mode"),

    COMMAND_CHAT_MODE_PUBLIC("&ePublic chat mode."),
    COMMAND_CHAT_MODE_ALLIANCE("&eAlliance only chat mode."),
    COMMAND_CHAT_MODE_TRUCE("&eTruce only chat mode."),
    COMMAND_CHAT_MODE_FACTION("&eFaction only chat mode."),
    COMMAND_CHAT_MODE_COLEADER("&eColeader only chat mode."),
    COMMAND_CHAT_MODE_MOD("&eMod only chat mode."),
    COMMAND_CHAT_MODE_NORMAL("&eNormal member only chat mode."),

    COMMAND_CHATSPY_ENABLE("&eYou have enabled chat spying mode."),
    COMMAND_CHATSPY_ENABLELOG(" has ENABLED chat spying mode."),
    COMMAND_CHATSPY_DISABLE("&eYou have disabled chat spying mode."),
    COMMAND_CHATSPY_DISABLELOG(" has DISABLED chat spying mode."),
    COMMAND_CHATSPY_DESCRIPTION("Enable admin chat spy mode"),

    COMMAND_CLAIM_DENIED("&cYou do not have permission to claim in a radius."),
    COMMAND_CLAIM_DESCRIPTION("Claim land from where you are standing"),

    COMMAND_CLAIMFILL_ABOVEMAX("&cThe maximum limit for claim fill is %s."),
    COMMAND_CLAIMFILL_ALREADYCLAIMED("&cCannot claim fill using already claimed land!"),
    COMMAND_CLAIMFILL_TOOFAR("&cThis fill would exceed the maximum distance of %.2f"),
    COMMAND_CLAIMFILL_PASTLIMIT("&cThis claim would exceed the limit!"),
    COMMAND_CLAIMFILL_NOTENOUGHLANDLEFT("%s &cdoes not have enough land left to make %d claims"),
    COMMAND_CLAIMFILL_TOOMUCHFAIL("&cAborting claim fill after %d failures"),

    COMMAND_CLEAR_DESCRIPTION_BANS("Unban all from faction"),
    COMMAND_CLEAR_DESCRIPTION_CLAIMS("Unclaim all territory"),
    COMMAND_CLEAR_DESCRIPTION_INVITES("Revoke all invites"),
    COMMAND_CLEAR_DESCRIPTION_WARPS("Delete all warps"),

    COMMAND_COLEADER_ALREADY_COLEADER("The faction already has a coleader. There can only be 1."),

    COMMAND_COORDS_MESSAGE("&e%s's location: &6%d&e, &6%d&e, &6%d&e in &6%s&e"),
    COMMAND_COORDS_DESCRIPTION("Broadcast your current position to your faction"),

    COMMAND_CREATE_MUSTLEAVE("&cYou must leave your current faction first."),
    COMMAND_CREATE_INUSE("&cThat tag is already in use."),
    COMMAND_CREATE_TOCREATE("to create a new faction"),
    COMMAND_CREATE_FORCREATE("for creating a new faction"),
    COMMAND_CREATE_CREATED("%1$s&e created a new faction %2$s"),
    COMMAND_CREATE_CREATEDLOG(" created a new faction: "),
    COMMAND_CREATE_DESCRIPTION("Create a new faction"),

    COMMAND_DEINVITE_REVOKED("%1$s&e revoked your invitation to &d%2$s&e."),
    COMMAND_DEINVITE_REVOKES("%1$s&e revoked %2$s's&e invitation."),

    COMMAND_DELFWARP_DELETED("&eDeleted warp &6%1$s"),
    COMMAND_DELFWARP_INVALID("&eCouldn't find warp &6%1$s"),
    COMMAND_DELFWARP_CLEAR_CONFIRM("&eAre you sure you want to clear all warps? If so, run /%s"),
    COMMAND_DELFWARP_CLEAR_SUCCESS("&eDeleted all warps"),
    COMMAND_DELFWARP_TODELETE("to delete warp"),
    COMMAND_DELFWARP_FORDELETE("for deleting warp"),

    COMMAND_DELHOME_DEL("%1$s&e unset the home for your faction."),
    COMMAND_DELHOME_TOSET("to unset the faction home"),
    COMMAND_DELHOME_FORSET("for unsetting the faction home"),

    COMMAND_DESCRIPTION_CHANGES("You have changed the description for &d%1$s&e to:"),
    COMMAND_DESCRIPTION_CHANGED("&eThe faction %1$s&e changed their description to:"),
    COMMAND_DESCRIPTION_TOOLONG("&cDescription too long! Limit is %s characters."),
    COMMAND_DESCRIPTION_TOCHANGE("to change faction description"),
    COMMAND_DESCRIPTION_FORCHANGE("for changing faction description"),
    COMMAND_DESCRIPTION_DESCRIPTION("Change the faction description"),

    COMMAND_DISBAND_IMMUTABLE("&eYou cannot disband the Wilderness, safe zone, or war zone."),
    COMMAND_DISBAND_MARKEDPERMANENT("&eThis faction is designated as permanent, so you cannot disband it."),
    COMMAND_DISBAND_BROADCAST_YOURS("&d%1$s&e disbanded your faction."),
    COMMAND_DISBAND_BROADCAST_NOTYOURS("&d%1$s&e disbanded the faction %2$s."),
    COMMAND_DISBAND_HOLDINGS("&eYou have been given the disbanded faction's bank, totaling %1$s."),
    COMMAND_DISBAND_CONFIRM("&eAre you sure you want to disband %s? If so, run /%s"),
    COMMAND_DISBAND_DESCRIPTION("Disband a faction"),

    COMMAND_DTR_TOSHOW("to show faction DTR info"),
    COMMAND_DTR_FORSHOW("for showing faction DTR info"),
    COMMAND_DTR_DTR("%1$s&6 - DTR / Max DTR: &e%2$s / %3$s"),
    COMMAND_DTR_DESCRIPTION("Show faction DTR info"),
    COMMAND_DTR_MODIFY_DESCRIPTION("Modify faction DTR"),
    COMMAND_DTR_MODIFY_DONE("&eSet DTR for %s&e to %s"),

    COMMAND_FLY_DESCRIPTION("Enter or leave Faction flight mode"),
    COMMAND_FLY_CHANGE("&eFaction flight &d%1$s"),
    COMMAND_FLY_DAMAGE("&eFaction flight &ddisabled&e due to entering combat"),
    COMMAND_FLY_AUTO("&eFaction auto flight &d%1$s"),
    COMMAND_FLY_NO_ACCESS("&cCannot fly in territory of %1$s"),
    COMMAND_FLY_ENEMY_NEARBY("&cCannot enable fly, enemy nearby"),
    COMMAND_FLY_ENEMY_DISABLE("&cEnemy nearby, disabling fly"),

    COMMAND_FLYTRAILS_PARTICLE_INVALID("&cInvalid particle effect"),
    COMMAND_FLYTRAILS_PARTICLE_PERMS("&cInsufficient permission to use &d%1s"),
    COMMAND_FLYTRAILS_PARTICLE_CHANGE("&eFaction flight trail effect set to &d%1s"),
    COMMAND_FLYTRAILS_CHANGE("&eFaction flight trail &d%1s"),

    COMMAND_GRACE_DESCRIPTION("View the current grace status"),
    COMMAND_GRACE_NOT_SET("&eGrace is not active"),
    COMMAND_GRACE_ACTIVE("&eGrace active! No explosions for %s"),

    COMMAND_SET_GRACE_DESCRIPTION("Set grace status"),
    COMMAND_SET_GRACE_OFF("&eGrace disabled!"),
    COMMAND_SET_GRACE_ACTIVE("&eGrace active! No explosions for %s"),

    COMMAND_HOME_DENIED("&cSorry, you cannot teleport to the home of %s"),
    COMMAND_HOME_NOHOME("&cYour faction does not have a home. "),
    COMMAND_HOME_INENEMY("&cYou cannot teleport to your faction home while in the territory of an enemy faction."),
    COMMAND_HOME_WRONGWORLD("&cYou cannot teleport to your faction home while in a different world."),
    COMMAND_HOME_ENEMYNEAR("&cYou cannot teleport to your faction home while an enemy is within %s blocks of you."),
    COMMAND_HOME_TOTELEPORT("to teleport to your faction home"),
    COMMAND_HOME_FORTELEPORT("for teleporting to your faction home"),
    COMMAND_HOME_WARPSREMAIN("&cCannot delete home while the faction has warps!"),
    COMMAND_HOME_DESCRIPTION("Teleport to the faction home"),

    COMMAND_INVITE_TOINVITE("to invite someone"),
    COMMAND_INVITE_FORINVITE("for inviting someone"),
    COMMAND_INVITE_CLICKTOJOIN("Click to join!"),
    COMMAND_INVITE_INVITEDYOU(" has invited you to join "),
    COMMAND_INVITE_INVITED("%1$s&e invited %2$s&e to your faction."),
    COMMAND_INVITE_ALREADYMEMBER("%1$s&e is already a member of %2$s"),
    COMMAND_INVITE_DESCRIPTION("Invite a player to your faction"),
    COMMAND_INVITE_BANNED("&7%1$s &cis banned from your Faction. Not sending an invite."),
    COMMAND_INVITE_CLEAR_CONFIRM("&eAre you sure you want to clear all invites? If so, run /%s"),
    COMMAND_INVITE_CLEAR_SUCCESS("&eDeleted all invites."),

    COMMAND_JOIN_SYSTEMFACTION("&cPlayers may only join normal factions. This is a system faction."),
    COMMAND_JOIN_ALREADYMEMBERFIXED("&You are already a member of %s"),
    COMMAND_JOIN_ATLIMIT(" &c!&f The faction %1$s is at the limit of %2$d members, so %3$s cannot currently join."),
    COMMAND_JOIN_INOTHERFACTIONFIXED("&cYou must leave your current faction first."),
    COMMAND_JOIN_NEGATIVEPOWER("&c%1$s cannot join a faction with a negative power level."),
    COMMAND_JOIN_REQUIRESINVITATION("&eThis faction requires invitation."),
    COMMAND_JOIN_ATTEMPTEDJOIN("%1$s&e tried to join your faction."),
    COMMAND_JOIN_TOJOIN("to join a faction"),
    COMMAND_JOIN_FORJOIN("for joining a faction"),
    COMMAND_JOIN_SUCCESS("&e%1$s successfully joined %2$s."),
    COMMAND_JOIN_JOINED("&e%1$s joined your faction."),
    COMMAND_JOIN_JOINEDLOG("%1$s joined the faction %2$s."),
    COMMAND_JOIN_DESCRIPTION("Join a faction"),
    COMMAND_JOIN_BANNED("&cYou are banned from %1$s &c:("),
    COMMAND_FORCE_JOIN_INOTHERFACTION("&cUser already in a faction."),
    COMMAND_FORCE_JOIN_SUCCESS("&e%1$s joined %2$s."),

    COMMAND_KICK_CANDIDATES("Players you can kick: "),
    COMMAND_KICK_CLICKTOKICK("Click to kick "),
    COMMAND_KICK_SELF("&cYou cannot kick yourself."),
    COMMAND_KICK_ENEMYTERRITORY("&cYou cannot kick a player in enemy territory"),
    COMMAND_KICK_NONE("That player is not in a faction."),
    COMMAND_KICK_NOTMEMBER("%1$s&c is not a member of %2$s"),
    COMMAND_KICK_INSUFFICIENTRANK("&cYour rank is too low to kick this player."),
    COMMAND_KICK_NEGATIVEPOWER("&cYou cannot kick that member until their power is positive."),
    COMMAND_KICK_TOKICK("to kick someone from the faction"),
    COMMAND_KICK_FORKICK("for kicking someone from the faction"),
    COMMAND_KICK_FACTION("%1$s&e kicked %2$s&e from the faction! :O"), //message given to faction members
    COMMAND_KICK_KICKED("%1$s&e kicked you from %2$s&e! :O"), //kicked player perspective
    COMMAND_KICK_DESCRIPTION("Kick a player from the faction"),

    COMMAND_LINK_CHANGED("%s&e changed their link to:"),
    COMMAND_LINK_INVALIDURL("&cInvalid URL!"),
    COMMAND_LINK_SHOW("%sFaction link: &e%s"),
    COMMAND_LINK_DESCRIPTION("Change the faction link"),

    COMMAND_LIST_TOLIST("to list the factions"),
    COMMAND_LIST_FORLIST("for listing the factions"),
    COMMAND_LIST_DESCRIPTION("See a list of the factions"),

    COMMAND_LISTCLAIMS_MESSAGE("&eClaims by %s&e in %s:"),
    COMMAND_LISTCLAIMS_INVALIDWORLD("&cInvalid world name %s"),
    COMMAND_LISTCLAIMS_NOCLAIMS("&cNo claims by %s&e in world %s"),
    COMMAND_LISTCLAIMS_DESCRIPTION("List your faction's claims"),

    COMMAND_SETAUTOSAVE_DISABLED("&eFactions autosave disabled"),
    COMMAND_SETAUTOSAVE_ENABLED("&eFactions autosave enabled"),
    COMMAND_SETAUTOSAVE_DESCRIPTION("Control autosaving."),

    COMMAND_LOGINS_TOGGLE("&eSet login / logout notifications for Faction members to: &6%s"),
    COMMAND_LOGINS_DESCRIPTION("Toggle(?) login / logout notifications for Faction members"),

    COMMAND_MAP_TOSHOW("to show the map"),
    COMMAND_MAP_FORSHOW("for showing the map"),
    COMMAND_MAP_UPDATE_ENABLED("&eMap auto update &2ENABLED."),
    COMMAND_MAP_UPDATE_DISABLED("&eMap auto update &4DISABLED."),
    COMMAND_MAP_DESCRIPTION("Show the territory map, and set optional auto update"),

    COMMAND_MAPHEIGHT_SET("&eSet /f map lines to &a%1$d"),

    COMMAND_MODIFYPOWER_ADDED("&eAdded &6%1$f &epower to &6%2$s. &eNew total rounded power: &6%3$d"),
    COMMAND_MODIFYPOWER_SET("&eSet &6%1$f &epower to &6%2$s."),
    COMMAND_MODIFYPOWER_DESCRIPTION("Modify the power of a faction/player"),

    COMMAND_MONEYDEPOSIT_DEPOSITED("%1$s deposited %2$s in the faction bank: %3$s"),
    COMMAND_MONEYMODIFY_DESCRIPTION("Modify faction bank money."),
    COMMAND_MONEYMODIFY_MODIFIED("&eModified %1$s bank by %2$s"),
    COMMAND_MONEYMODIFY_NOTIFY("&eModified %1$s bank by %2$s"),
    COMMAND_MONEYMODIFY_SET("&eSet %1$s bank to %2$s"),
    COMMAND_MONEYMODIFY_SETNOTIFY("&eModified %1$s bank by %2$s"),
    COMMAND_MONEYMODIFY_FAIL("&cFailed to modify!"),

    COMMAND_MONEYTRANSFERFF_TRANSFER("%1$s transferred %2$s from the faction \"%3$s\" to the faction \"%4$s\""),

    COMMAND_MONEYTRANSFERFP_TRANSFER("%1$s transferred %2$s from the faction \"%3$s\" to the player \"%4$s\""),

    COMMAND_MONEYWITHDRAW_WITHDRAW("%1$s withdrew %2$s from the faction bank: %3$s"),

    COMMAND_NEAR_DESCRIPTION("Show nearby faction members"),
    COMMAND_NEAR_PLAYER("&a&l{role-prefix}&8{name} (&d{distance}m&8)"),
    COMMAND_NEAR_PLAYERLIST("&eNear: {players-nearby}"),
    COMMAND_NEAR_NONE("&8None"),

    COMMAND_OPEN_TOOPEN("to open or close the faction"),
    COMMAND_OPEN_FOROPEN("for opening or closing the faction"),
    COMMAND_OPEN_OPEN("open"),
    COMMAND_OPEN_CLOSED("closed"),
    COMMAND_OPEN_CHANGES("%1$s&e changed the faction to &d%2$s&e."),
    COMMAND_OPEN_CHANGED("&eThe faction %1$s&e is now %2$s"),
    COMMAND_OPEN_DESCRIPTION("Switch if invitation is required to join"),

    COMMAND_PEACEFUL_DESCRIPTION("Set a faction to peaceful"),
    COMMAND_PEACEFUL_YOURS("%1$s has %2$s your faction"),
    COMMAND_PEACEFUL_OTHER("%s&e has %s the faction '%s&e'."),
    COMMAND_PEACEFUL_GRANT("granted peaceful status to"),
    COMMAND_PEACEFUL_REVOKE("removed peaceful status from"),

    COMMAND_PERMS_DESCRIPTION("&6Edit or list your Faction's permissions."),

    COMMAND_PERMANENT_DESCRIPTION("Toggles a faction's permanence"),
    COMMAND_PERMANENT_GRANT("added permanent status to"),
    COMMAND_PERMANENT_REVOKE("removed permanent status from"),
    COMMAND_PERMANENT_YOURS("%1$s has %2$s your faction"),
    COMMAND_PERMANENT_OTHER("%s&e has %s the faction '%s&e'."),

    COMMAND_PERMANENTPOWER_DESCRIPTION("Toggle faction power permanence"),
    COMMAND_PERMANENTPOWER_GRANT("added permanentpower status to"),
    COMMAND_PERMANENTPOWER_REVOKE("removed permanentpower status from"),
    COMMAND_PERMANENTPOWER_SUCCESS("&eYou %s &d%s&e."),
    COMMAND_PERMANENTPOWER_FACTION("%s&e %s your faction"),

    COMMAND_ROLE_DESCRIPTION("Modify a faction member's role."),
    COMMAND_ROLE_NOT_ALLOWED("&cYou can't change that player's role."),
    COMMAND_ROLE_WRONGFACTION("&cThat player is not part of your faction."),
    COMMAND_ROLE_UPDATED("&aMember %s&a role updated to %s."),

    COMMAND_POWER_TOSHOW("to show player power info"),
    COMMAND_POWER_FORSHOW("for showing player power info"),
    COMMAND_POWER_POWER("%1$s&6 - Power / Maxpower: &e%2$d / %3$d %4$s"),
    COMMAND_POWER_BONUS(" (bonus: "),
    COMMAND_POWER_PENALTY(" (penalty: "),
    COMMAND_POWER_DESCRIPTION("Show player power info"),

    COMMAND_POWERBOOST_BOOST("&e%1$s now has a power bonus/penalty of %2$d to min and max power levels."),
    COMMAND_POWERBOOST_DESCRIPTION("Apply permanent power bonus/penalty to specified player or faction"),

    COMMAND_RELATIONS_ALLTHENOPE("&cNope! You can't."),
    COMMAND_RELATIONS_MORENOPE("&cNope! You can't declare a relation to yourself :)"),
    COMMAND_RELATIONS_ALREADYINRELATIONSHIP("&cYou already have that relation wish set with %1$s."),
    COMMAND_RELATIONS_TOMARRY("to change a relation wish"),
    COMMAND_RELATIONS_FORMARRY("for changing a relation wish"),
    COMMAND_RELATIONS_MUTUAL("&eYour faction is now %1$s&e to %2$s"),
    COMMAND_RELATIONS_PEACEFUL("&eThis will have no effect while your faction is peaceful."),
    COMMAND_RELATIONS_PEACEFULOTHER("&eThis will have no effect while their faction is peaceful."),
    COMMAND_RELATIONS_DESCRIPTION("Set relation wish to another faction"),
    COMMAND_RELATIONS_EXCEEDS_ME("&eFailed to set relation wish. You can only have %1$s %2$s."),
    COMMAND_RELATIONS_EXCEEDS_THEY("&eFailed to set relation wish. They can only have %1$s %2$s."),

    COMMAND_RELATIONS_PROPOSAL_1("%1$s&e wishes to be your %2$s"),
    COMMAND_RELATIONS_PROPOSAL_2("&eType &b/%1$s %2$s %3$s&e to accept."),
    COMMAND_RELATIONS_PROPOSAL_SENT("%1$s&e were informed that you wish to be %2$s"),

    COMMAND_RELOAD_TIME("&eReloaded &dall configuration files &efrom disk, took &d%1$d ms&e."),
    COMMAND_RELOAD_DESCRIPTION("Reload data file(s) from disk"),

    COMMAND_SAVEALL_SUCCESS("&eFactions saved to disk!"),
    COMMAND_SAVEALL_DESCRIPTION("Save all data to disk"),

    COMMAND_SCOREBOARD_DESCRIPTION("Scoreboardy things"),

    COMMAND_SETDEFAULTROLE_DESCRIPTION("/f defaultrole <role> - set your Faction's default role."),
    COMMAND_SETDEFAULTROLE_NOTTHATROLE("You cannot set the default to admin."),
    COMMAND_SETDEFAULTROLE_SUCCESS("Set default role of your faction to %1$s"),
    COMMAND_SETDEFAULTROLE_INVALIDROLE("Couldn't find matching role for %1$s"),

    COMMAND_SETFWARP_NOTCLAIMED("&eYou can only set warps in your faction territory."),
    COMMAND_SETFWARP_LIMIT("&eYour Faction already has the max amount of warps set &6(%1$d)."),
    COMMAND_SETFWARP_SET("&eSet warp &6%1$s&e and password &b'%2$s' &eto your location."),
    COMMAND_SETFWARP_TOSET("to set warp"),
    COMMAND_SETFWARP_FORSET("for setting warp"),
    COMMAND_SETFWARP_HOMEREQUIRED("&cCannot create warps until a home is set!"),
    COMMAND_SETFWARP_DESCRIPTION("Set a faction warp"),

    COMMAND_SETFWARPPROPERTY_DESCRIPTION("Set a faction warp property"),
    COMMAND_SETFWARPPROPERTY_NOWARP("&cNo warp found with name %s"),
    COMMAND_SETFWARPPROPERTY_REMOVEPASSWORD("&aPassword removed for warp %s"),
    COMMAND_SETFWARPPROPERTY_SETPASSWORD("&aPassword set for warp %s"),

    COMMAND_SETHOME_NOTCLAIMED("&cSorry, your faction home can only be set inside your own claimed territory."),
    COMMAND_SETHOME_TOSET("to set the faction home"),
    COMMAND_SETHOME_FORSET("for setting the faction home"),
    COMMAND_SETHOME_SET("%1$s&e set the home for your faction. You can now use:"),
    COMMAND_SETHOME_DESCRIPTION("Set the faction home"),

    COMMAND_SETMAXVAULTS_DESCRIPTION("Set max vaults for a Faction."),
    COMMAND_SETMAXVAULTS_SUCCESS("&aSet max vaults for &e%s &ato &b%d"),

    COMMAND_SHIELD_DESCRIPTION("View and manage shields."),
    COMMAND_SHIELD_NOT_SET("&eShield is not active"),
    COMMAND_SHIELD_AVAILABLE("&eShield available: %s"),
    COMMAND_SHIELD_COOLDOWN("&cShield on cooldown for %s"),
    COMMAND_SHIELD_ACTIVATED("&eShield activated by %s&e! No explosions for %s"),
    COMMAND_SHIELD_ACTIVE("&eShield active! No explosions for %s"),

    COMMAND_TNT_TERRITORYONLY("&cCommand can only be run from your faction's territory!"),
    COMMAND_TNT_DEPOSIT_DESCRIPTION("Add to your faction's TNT bank"),
    COMMAND_TNT_DEPOSIT_FAIL_FULL("&cFaction bank already at maximum!"),
    COMMAND_TNT_DEPOSIT_FAIL_POSITIVE("&cMust deposit at least one!"),
    COMMAND_TNT_DEPOSIT_SUCCESS("&eYour faction now has %d TNT"),
    COMMAND_TNT_FILL_DESCRIPTION("Fill TNT into nearby dispensers"),
    COMMAND_TNT_FILL_MESSAGE("&eFilled %d TNT into %d dispensers. %d left in the faction bank."),
    COMMAND_TNT_FILL_FAIL_MAXRADIUS("&c%d is bigger than the maximum radius of %d"),
    COMMAND_TNT_FILL_FAIL_NOTENOUGH("&cThe faction bank does not have %d TNT!"),
    COMMAND_TNT_FILL_FAIL_POSITIVE("&cPositive values only!"),
    COMMAND_TNT_INFO_DESCRIPTION("View your faction's TNT bank"),
    COMMAND_TNT_INFO_MESSAGE("&eYour faction has %d TNT"),
    COMMAND_TNT_SIPHON_DESCRIPTION("Take TNT from nearby dispensers"),
    COMMAND_TNT_SIPHON_MESSAGE("&eAcquired %d TNT, for a total of %d in the faction bank."),
    COMMAND_TNT_SIPHON_FAIL_POSITIVE("&cPositive values only!"),
    COMMAND_TNT_SIPHON_FAIL_FULL("&cFaction bank already at maximum!"),
    COMMAND_TNT_SIPHON_FAIL_MAXRADIUS("&c%d is bigger than the maximum radius of %d"),
    COMMAND_TNT_WITHDRAW_DESCRIPTION("Withdraw TNT from the faction bank"),
    COMMAND_TNT_WITHDRAW_MESSAGE("&eWithdrew %d TNT. %d left in the faction bank."),
    COMMAND_TNT_WITHDRAW_FAIL_NOTENOUGH("&cThe faction bank does not have %d TNT!"),
    COMMAND_TNT_WITHDRAW_FAIL_POSITIVE("&cPositive values only!"),
    COMMAND_TNT_MODIFY_SUCCESS("&e%s now has %d TNT"),

    COMMAND_VAULT_DESCRIPTION("/f vault <number> to open one of your Faction's vaults."),
    COMMAND_VAULT_TOOHIGH("&cYou tried to open vault %d but your Faction only has %d vaults."),

    COMMAND_SEECHUNK_DESCRIPTION("Show chunk boundaries"),
    COMMAND_SEECHUNK_TOGGLE("&eSeechunk &d%1$s"),

    COMMAND_SHOW_NOFACTION_OTHER("That's not a faction"),
    COMMAND_SHOW_TOSHOW("to show faction information"),
    COMMAND_SHOW_FORSHOW("for showing faction information"),
    COMMAND_SHOW_PEACEFUL("This faction is Peaceful"),
    COMMAND_SHOW_INVITATION("invitation is required"),
    COMMAND_SHOW_UNINVITED("no invitation is needed"),
    COMMAND_SHOW_NOHOME("n/a"),
    COMMAND_SHOW_BONUS(" (bonus: "),
    COMMAND_SHOW_PENALTY(" (penalty: "),
    COMMAND_SHOW_COMMANDDESCRIPTION("Show faction information"),
    COMMAND_SHOW_DEATHS_TIL_RAIDABLE("&eDTR: %1$d"),
    COMMAND_SHOW_EXEMPT("&cThis faction cannot be seen."),

    COMMAND_SHOWINVITES_PENDING("Players with pending invites: "),
    COMMAND_SHOWINVITES_CLICKTOREVOKE("Click to revoke invite for %1$s"),
    COMMAND_SHOWINVITES_DESCRIPTION("Show pending faction invites"),

    COMMAND_STATUS_FORMAT("%1$s Power: %2$s Last Seen: %3$s"),
    COMMAND_STATUS_ONLINE("Online"),
    COMMAND_STATUS_AGOSUFFIX(" ago."),
    COMMAND_STATUS_DESCRIPTION("Show the status of a player"),

    COMMAND_STUCK_OUTSIDE("&6Teleport cancelled because you left &e%1$d &6block radius"),
    COMMAND_STUCK_ALREADYEXISTS("&6You are already teleporting, you must wait!"),
    COMMAND_STUCK_TELEPORT("&6Teleported safely to %1$d, %2$d, %3$d."),
    COMMAND_STUCK_FAILED("&cFailed to find a safe place to get you out."),
    COMMAND_STUCK_TOSTUCK2("to safely teleport out"),
    COMMAND_STUCK_FORSTUCK2("for initiating a safe teleport out"),
    COMMAND_STUCK_DESCRIPTION("Safely teleports you out of enemy faction"),

    COMMAND_TAG_TAKEN("&cThat tag is already taken"),
    COMMAND_TAG_TOCHANGE("to change the faction tag"),
    COMMAND_TAG_FORCHANGE("for changing the faction tag"),
    COMMAND_TAG_FACTION("%1$s&e changed your faction tag to %2$s"),
    COMMAND_TAG_CHANGED("&eThe faction %1$s&e changed their name to %2$s."),
    COMMAND_TAG_DESCRIPTION("Change the faction tag"),

    COMMAND_TICKETINFO_DESCRIPTION("Create requested ticket info"),

    COMMAND_TITLE_CANNOTPLAYER("&cCannot change this player's title"),
    COMMAND_TITLE_TOCHANGE("to change a players title"),
    COMMAND_TITLE_FORCHANGE("for changing a players title"),
    COMMAND_TITLE_CHANGED("%1$s&e changed a title: %2$s"),
    COMMAND_TITLE_DESCRIPTION("Set or remove a players title"),

    COMMAND_TOGGLEALLIANCECHAT_DESCRIPTION("Toggles whether or not you will see alliance chat"),
    COMMAND_TOGGLEALLIANCECHAT_IGNORE("Alliance chat is now ignored"),
    COMMAND_TOGGLEALLIANCECHAT_UNIGNORE("Alliance chat is no longer ignored"),
    COMMAND_TOGGLETRUCECHAT_DESCRIPTION("Toggles whether or not you will see truce chat"),
    COMMAND_TOGGLETRUCECHAT_IGNORE("Truce chat is now ignored"),
    COMMAND_TOGGLETRUCECHAT_UNIGNORE("Truce chat is no longer ignored"),

    COMMAND_TOGGLESB_DISABLED("You can't toggle scoreboards while they are disabled."),

    COMMAND_TOP_DESCRIPTION("Sort Factions to see the top of some criteria."),
    COMMAND_TOP_TOP("Top Factions by %s. Page %d/%d"),
    COMMAND_TOP_LINE("%d. &6%s: &c%s"), // Rank. Faction: Value
    COMMAND_TOP_INVALID("Could not sort by %s. Try balance, online, members, power or land."),

    COMMAND_UNBAN_DESCRIPTION("Unban someone from your Faction"),
    COMMAND_UNBAN_NOTBANNED("&7%s &cisn't banned. Not doing anything."),
    COMMAND_UNBAN_UNBANNED("&e%1$s &cunbanned &7%2$s"),
    COMMAND_UNBAN_TARGET("&aYou were unbanned from &r%s"),
    COMMAND_UNBAN_CLEAR_CONFIRM("&eAre you sure you want to clear all bans? If so, run /%s"),
    COMMAND_UNBAN_CLEAR_SUCCESS("&eAll bans removed."),

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
    COMMAND_UNCLAIM_DESCRIPTION("Unclaim the land where you are standing"),

    COMMAND_UNCLAIMALL_TOUNCLAIM("to unclaim all faction land"),
    COMMAND_UNCLAIMALL_FORUNCLAIM("for unclaiming all faction land"),
    COMMAND_UNCLAIMALL_UNCLAIMED("%1$s&e unclaimed ALL of your faction's land."),
    COMMAND_UNCLAIMALL_LOG("%1$s unclaimed everything for the faction: %2$s"),
    COMMAND_UNCLAIMALL_CONFIRM("&eAre you sure you want to unclaim ALL %s territory? If so, run /%s"),

    COMMAND_UNCLAIMFILL_ABOVEMAX("&cThe maximum limit for unclaim fill is %s."),
    COMMAND_UNCLAIMFILL_NOTCLAIMED("&cCannot unclaim fill using non-claimed land!"),
    COMMAND_UNCLAIMFILL_TOOFAR("&cThis unclaim would exceed the maximum distance of %.2f"),
    COMMAND_UNCLAIMFILL_PASTLIMIT("&cThis unclaim would exceed the limit!"),
    COMMAND_UNCLAIMFILL_TOOMUCHFAIL("&cAborting unclaim fill after %d failures"),
    COMMAND_UNCLAIMFILL_UNCLAIMED("%s&e unclaimed %d claims of your faction's land around %s."),
    COMMAND_UNCLAIMFILL_BYPASSCOMPLETE("&eUnclaimed %d claims."),

    COMMAND_VERSION_DESCRIPTION("Show plugin and translation version information"),

    COMMAND_UPGRADES_DESCRIPTION("Show faction upgrades"),
    COMMAND_UPGRADES_TOUPGRADE("to buy an upgrade"),
    COMMAND_UPGRADES_FORUPGRADE("for buying an upgrade"),

    COMMAND_ZONE_DESCRIPTION("Manage zones"),

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
    LEAVE_DESCRIPTION("Leave your faction"),

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
    GENERIC_NOPERMISSION("&cYou don't have permission to %1$s."),
    GENERIC_DEFAULTDESCRIPTION("Default faction description :("),
    GENERIC_FACTIONLESS("factionless"),
    GENERIC_SERVERADMIN("A server admin"),
    GENERIC_DISABLED("disabled"),
    GENERIC_ENABLED("enabled"),
    GENERIC_INFINITY("∞"),
    GENERIC_MEMBERONLY("&cYou are not member of any faction."),
    GENERIC_YOUMUSTBE("&cYou must be &d%s&c."),
    GENERIC_FACTIONTAG_BLACKLIST("&eThat faction tag is blacklisted."),
    GENERIC_FACTIONTAG_TOOSHORT("&eThe faction tag can't be shorter than &d%1$s&e chars."),
    GENERIC_FACTIONTAG_TOOLONG("&eThe faction tag can't be longer than &d%s&e chars."),
    GENERIC_FACTIONTAG_ALPHANUMERIC("&eFaction tag must be alphanumeric. \"&d%s&e\" is not allowed."),

    /**
     * Clip placeholder stuff
     */
    PLACEHOLDER_ROLE_NAME("None"),

    /**
     * ASCII compass (for chat map)
     */
    COMPASS_SHORT_NORTH("N"),
    COMPASS_SHORT_EAST("E"),
    COMPASS_SHORT_SOUTH("S"),
    COMPASS_SHORT_WEST("W"),

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
    ECON_GAIN_SUCCESS("&d%s&e gained &d%s&e %s."),
    ECON_GAIN_FAILURE("&d%s&e would have gained &d%s&e %s, but the deposit failed."),
    ECON_LOST_SUCCESS("&d%s&e lost &d%s&e %s."),
    ECON_LOST_FAILURE("&d%s&e can't afford &d%s&e %s."),

    /**
     * Relations
     */
    RELATION_MEMBER_SINGULAR("member"),
    RELATION_MEMBER_PLURAL("members"),
    RELATION_ALLY_SINGULAR("ally"),
    RELATION_ALLY_PLURAL("allies"),
    RELATION_TRUCE_SINGULAR("truce"),
    RELATION_TRUCE_PLURAL("truces"),
    RELATION_NEUTRAL_SINGULAR("neutral"),
    RELATION_NEUTRAL_PLURAL("neutrals"),
    RELATION_ENEMY_SINGULAR("enemy"),
    RELATION_ENEMY_PLURAL("enemies"),

    /**
     * Roles
     */
    ROLE_ADMIN("admin"),
    ROLE_COLEADER("coleader"),
    ROLE_MODERATOR("moderator"),
    ROLE_NORMAL("normal member"),
    ROLE_RECRUIT("recruit"),

    /**
     * Region types.
     */
    REGION_SAFEZONE("safezone"),

    REGION_PEACEFUL("peaceful territory"),
    /**
     * In the player and entity listeners
     */
    PLAYER_CANTHURT("&eYou may not harm other players in %s"),
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

    PLAYER_PVP_LOGIN("&eYou can't hurt other players for %d seconds after logging in."),
    PLAYER_PVP_REQUIREFACTION("&eYou can't hurt other players until you join a faction."),
    PLAYER_PVP_FACTIONLESS("&eYou can't hurt players who are not currently in a faction."),
    PLAYER_PVP_PEACEFUL("&ePeaceful players cannot participate in combat."),
    PLAYER_PVP_NEUTRAL("&eYou can't hurt neutral factions. Declare them as an enemy."),
    PLAYER_PVP_CANTHURT("&eYou can't hurt %s&e."),

    PLAYER_PVP_NEUTRALFAIL("&eYou can't hurt %s&e in their own territory unless you declare them as an enemy."),
    PLAYER_PVP_TRIED("%s&e tried to hurt you."),

    PLAYER_TELEPORTEDONJOIN("&eYou were teleported out of %s territory"),

    PERM_DENIED_WILDERNESS("&cYou can't %s in the wilderness"),
    PERM_DENIED_SAFEZONE("&c>You can't %s in a safe zone"),
    PERM_DENIED_WARZONE("&cYou can't %s in a war zone"),
    PERM_DENIED_TERRITORY("&cYou can't %s in the territory of %s"),
    PERM_DENIED_PAINTERRITORY("&cIt is painful to %s in the territory of %s"),

    TAG_LEADER_OWNERLESS("Server"),

    /**
     * The ones here before I started messing around with this
     */
    WILDERNESS("wilderness", "&2Wilderness"),
    WILDERNESS_DESCRIPTION("wilderness-description", ""),
    WARZONE("warzone", "&4Warzone"),
    WARZONE_DESCRIPTION("warzone-description", "Not the safest place to be."),
    SAFEZONE("safezone", "&6Safezone"),
    SAFEZONE_DESCRIPTION("safezone-description", "Free from pvp and monsters."),
    TOGGLE_SB("toggle-sb", "You now have scoreboards set to {value}"),
    FACTION_LEAVE("faction-leave", "&6Leaving %1$s, &6Entering %2$s"),
    FACTIONS_ANNOUNCEMENT_TOP("faction-announcement-top", "&d--Unread Faction Announcements--"),
    FACTIONS_ANNOUNCEMENT_BOTTOM("faction-announcement-bottom", "&d--Unread Faction Announcements--"),
    DEFAULT_PREFIX("default-prefix", "{relationcolor}[{faction}] &r"),
    FACTION_LOGIN("faction-login", "&e%1$s &9logged in."),
    FACTION_LOGOUT("faction-logout", "&e%1$s &9logged out.."),
    NOFACTION_PREFIX("nofactions-prefix", "&6[&ano-faction&6]&r"),
    DATE_FORMAT("date-format", "MM/d/yy h:ma"), // 3/31/15 07:49AM

    /**
     * Raidable is used in multiple places. Allow more than just true/false.
     */
    RAIDABLE_TRUE("raidable-true", "true"),
    RAIDABLE_FALSE("raidable-false", "false"),
    /**
     * Warmups
     */
    WARMUPS_NOTIFY_FLY("&eFlight will enable in &d%d &eseconds."),
    WARMUPS_NOTIFY_HOME("&eYou will teleport home in &d%d &eseconds."),
    WARMUPS_NOTIFY_STUCK("&eYou will find a safe place to become unstuck in &d%d &eseconds."),
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
