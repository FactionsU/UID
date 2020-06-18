package com.massivecraft.factions.listeners;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.config.file.MainConfig;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.perms.Role;
import com.massivecraft.factions.struct.ChatMode;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UnknownFormatConversionException;
import java.util.logging.Level;

public class FactionsChatListener implements Listener {

    public FactionsPlugin plugin;

    public FactionsChatListener(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    // this is for handling slashless command usage and faction/alliance chat, set at lowest priority so Factions gets to them first
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerEarlyChat(AsyncPlayerChatEvent event) {
        if (!plugin.worldUtil().isEnabled(event.getPlayer().getWorld())) {
            return;
        }

        Player talkingPlayer = event.getPlayer();
        String msg = event.getMessage();
        FPlayer me = FPlayers.getInstance().getByPlayer(talkingPlayer);
        ChatMode chat = me.getChatMode();
        MainConfig.Factions.Chat chatConf = FactionsPlugin.getInstance().conf().factions().chat();
        //Is it a MOD chat
        if (chat == ChatMode.MOD) {
            Faction myFaction = me.getFaction();

            String message = String.format(chatConf.getModChatFormat(), ChatColor.stripColor(me.getNameAndTag()), msg);

            //Send to all mods
            for (FPlayer fplayer : FPlayers.getInstance().getOnlinePlayers()) {
                if (myFaction == fplayer.getFaction() && fplayer.getRole().isAtLeast(Role.MODERATOR)) {
                    fplayer.sendMessage(message);
                } else if (fplayer.isSpyingChat() && me != fplayer) {
                    fplayer.sendMessage("[MCspy]: " + message);
                }
            }

            FactionsPlugin.getInstance().log(Level.INFO, ChatColor.stripColor("ModChat " + myFaction.getTag() + ": " + message));

            event.setCancelled(true);
        } else if (chat == ChatMode.FACTION) {
            Faction myFaction = me.getFaction();

            String message = String.format(chatConf.getFactionChatFormat(), me.describeTo(myFaction), msg);
            myFaction.sendMessage(message);

            FactionsPlugin.getInstance().log(Level.INFO, ChatColor.stripColor("FactionChat " + myFaction.getTag() + ": " + message));

            //Send to any players who are spying chat
            for (FPlayer fplayer : FPlayers.getInstance().getOnlinePlayers()) {
                if (fplayer.isSpyingChat() && fplayer.getFaction() != myFaction && me != fplayer) {
                    fplayer.sendMessage("[FCspy] " + myFaction.getTag() + ": " + message);
                }
            }

            event.setCancelled(true);
        } else if (chat == ChatMode.ALLIANCE) {
            Faction myFaction = me.getFaction();

            String message = String.format(chatConf.getAllianceChatFormat(), ChatColor.stripColor(me.getNameAndTag()), msg);

            //Send message to our own faction
            myFaction.sendMessage(message);

            //Send to all our allies
            for (FPlayer fplayer : FPlayers.getInstance().getOnlinePlayers()) {
                if (myFaction.getRelationTo(fplayer) == Relation.ALLY && !fplayer.isIgnoreAllianceChat()) {
                    fplayer.sendMessage(message);
                } else if (fplayer.isSpyingChat() && me != fplayer) {
                    fplayer.sendMessage("[ACspy]: " + message);
                }
            }

            FactionsPlugin.getInstance().log(Level.INFO, ChatColor.stripColor("AllianceChat: " + message));

            event.setCancelled(true);
        } else if (chat == ChatMode.TRUCE) {
            Faction myFaction = me.getFaction();

            String message = String.format(chatConf.getTruceChatFormat(), ChatColor.stripColor(me.getNameAndTag()), msg);

            //Send message to our own faction
            myFaction.sendMessage(message);

            //Send to all our truces
            for (FPlayer fplayer : FPlayers.getInstance().getOnlinePlayers()) {
                if (myFaction.getRelationTo(fplayer) == Relation.TRUCE) {
                    fplayer.sendMessage(message);
                } else if (fplayer.isSpyingChat() && fplayer != me) {
                    fplayer.sendMessage("[TCspy]: " + message);
                }
            }

            FactionsPlugin.getInstance().log(Level.INFO, ChatColor.stripColor("TruceChat: " + message));
            event.setCancelled(true);
        }
    }

    // this is for handling insertion of the player's faction tag, set at highest priority to give other plugins a chance to modify chat first
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!plugin.worldUtil().isEnabled(event.getPlayer().getWorld())) {
            return;
        }

        // Are we to insert the Faction tag into the format?
        // If we are not to insert it - we are done.
        if (FactionsPlugin.getInstance().isAnotherPluginHandlingChat()) {
            return;
        }

        Player talkingPlayer = event.getPlayer();
        String msg = event.getMessage();
        String eventFormat = event.getFormat();
        FPlayer me = FPlayers.getInstance().getByPlayer(talkingPlayer);
        MainConfig.Factions.Chat chatConf = FactionsPlugin.getInstance().conf().factions().chat();
        int InsertIndex = chatConf.getTagInsertIndex();

        boolean padBefore = chatConf.isTagPadBefore();
        boolean padAfter = chatConf.isTagPadAfter();

        if (!chatConf.getTagReplaceString().isEmpty() && eventFormat.contains(chatConf.getTagReplaceString())) {
            // we're using the "replace" method of inserting the faction tags
            if (eventFormat.contains("[FACTION_TITLE]")) {
                eventFormat = eventFormat.replace("[FACTION_TITLE]", me.getTitle());
            }
            InsertIndex = eventFormat.indexOf(chatConf.getTagReplaceString());
            eventFormat = eventFormat.replace(chatConf.getTagReplaceString(), "");
            padBefore = false;
            padAfter = false;
        } else if (!chatConf.getTagInsertAfterString().isEmpty() && eventFormat.contains(chatConf.getTagInsertAfterString())) {
            // we're using the "insert after string" method
            InsertIndex = eventFormat.indexOf(chatConf.getTagInsertAfterString()) + chatConf.getTagInsertAfterString().length();
        } else if (!chatConf.getTagInsertBeforeString().isEmpty() && eventFormat.contains(chatConf.getTagInsertBeforeString())) {
            // we're using the "insert before string" method
            InsertIndex = eventFormat.indexOf(chatConf.getTagInsertBeforeString());
        } else if (!chatConf.isAlwaysShowChatTag()) {
            return;
        }

        String formatStart = eventFormat.substring(0, InsertIndex) + ((padBefore && !me.getChatTag().isEmpty()) ? " " : "");
        String formatEnd = ((padAfter && !me.getChatTag().isEmpty()) ? " " : "") + eventFormat.substring(InsertIndex);

        String nonColoredMsgFormat = formatStart + me.getChatTag().trim() + formatEnd;

        // Relation Colored?
        if (chatConf.isTagRelationColored()) {
            for (Player listeningPlayer : event.getRecipients()) {
                FPlayer you = FPlayers.getInstance().getByPlayer(listeningPlayer);
                String yourFormat = formatStart + me.getChatTag(you).trim() + formatEnd;
                try {
                    listeningPlayer.sendMessage(String.format(yourFormat, talkingPlayer.getDisplayName(), msg));
                } catch (UnknownFormatConversionException ex) {

                    FactionsPlugin.getInstance().log(Level.SEVERE, "Critical error in chat message formatting!");
                    FactionsPlugin.getInstance().log(Level.SEVERE, "NOTE: This can be fixed right now by setting chat tagInsertIndex to 0.");
                    return;
                }
            }

            // Messages are sent to players individually
            // This still leaves a chance for other plugins to pick it up
            event.getRecipients().clear();
        }
        // Message with no relation color.
        event.setFormat(nonColoredMsgFormat);
    }

}
