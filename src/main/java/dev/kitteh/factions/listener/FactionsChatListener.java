package dev.kitteh.factions.listener;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.chat.ChatTarget;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.integration.Essentials;
import dev.kitteh.factions.integration.IntegrationManager;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.util.WorldUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UnknownFormatConversionException;
import java.util.logging.Level;

public class FactionsChatListener implements Listener {

    public final FactionsPlugin plugin;

    public FactionsChatListener(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    // this is for handling slashless command usage and faction/alliance chat, set at lowest priority so Factions gets to them first
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerEarlyChat(AsyncPlayerChatEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer().getWorld())) {
            return;
        }

        Player talkingPlayer = event.getPlayer();
        String msg = event.getMessage();
        FPlayer me = FPlayers.fPlayers().getByPlayer(talkingPlayer);
        Faction faction = me.getFaction();
        MainConfig.Factions.Chat.InternalChat chatConf = FactionsPlugin.getInstance().conf().factions().chat().internalChat();

        ChatTarget chatTarget = me.getChatTarget();

        LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();

        if (chatTarget instanceof ChatTarget.Role(Role role)) {
            String format = role == Role.RECRUIT ? chatConf.getFactionMemberAllChatFormat() : chatConf.getFactionMemberChatFormat();
            for (FPlayer fPlayer : faction.getFPlayersWhereOnline(true)) {
                if (fPlayer.getRole().isAtLeast(role)) {
                    fPlayer.sendMessage(MiniMessage.miniMessage().deserialize(format,
                            Placeholder.unparsed("message", msg),
                            Placeholder.component("faction", legacy.deserialize(faction.getTag(fPlayer))),
                            Placeholder.unparsed("role", role.nicename),
                            Placeholder.component("sender", legacy.deserialize(me.getChatTag(fPlayer)))
                    ));
                } else if (fPlayer.isSpyingChat()) {
                    fPlayer.sendMessage(MiniMessage.miniMessage().deserialize("[MCspy] " + format,
                            Placeholder.unparsed("message", msg),
                            Placeholder.component("faction", legacy.deserialize(faction.getTag(fPlayer))),
                            Placeholder.unparsed("role", role.nicename),
                            Placeholder.component("sender", legacy.deserialize(me.getChatTag(fPlayer)))
                    ));
                }
            }
            event.setCancelled(true);
        } else if (chatTarget instanceof ChatTarget.Relation(Relation relation)) {
            String format = chatConf.getRelationChatFormat();

            for (FPlayer fPlayer : FPlayers.fPlayers().getOnlinePlayers()) {
                if (fPlayer.getFaction() == faction || (faction.getRelationTo(fPlayer) == relation &&
                        (
                                (relation == Relation.ALLY && !fPlayer.isIgnoreAllianceChat()) ||
                                        (relation == Relation.TRUCE && !fPlayer.isIgnoreTruceChat())
                        ))) {
                    fPlayer.sendMessage(MiniMessage.miniMessage().deserialize(format,
                            Placeholder.unparsed("message", msg),
                            Placeholder.component("faction", legacy.deserialize(faction.getTag(fPlayer))),
                            Placeholder.unparsed("relation", relation.nicename),
                            Placeholder.component("sender", legacy.deserialize(me.getChatTag(fPlayer)))
                    ));
                } else if (fPlayer.isSpyingChat()) {
                    fPlayer.sendMessage(MiniMessage.miniMessage().deserialize("[MCspy] " + format,
                            Placeholder.unparsed("message", msg),
                            Placeholder.component("faction", legacy.deserialize(faction.getTag(fPlayer))),
                            Placeholder.unparsed("relation", relation.nicename),
                            Placeholder.component("sender", legacy.deserialize(me.getChatTag(fPlayer)))
                    ));
                }
            }
            event.setCancelled(true);
        }
    }

    // this is for handling insertion of the player's faction tag, set at highest priority to give other plugins a chance to modify chat first
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer().getWorld())) {
            return;
        }

        // Are we to insert the Faction tag into the format?
        // If we are not to insert it - we are done.
        if (FactionsPlugin.getInstance().conf().factions().chat().isTagHandledByAnotherPlugin()) {
            return;
        }

        Player talkingPlayer = event.getPlayer();
        String msg = event.getMessage();
        String eventFormat = event.getFormat();
        FPlayer me = FPlayers.fPlayers().getByPlayer(talkingPlayer);
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
                if (FactionsPlugin.getInstance().getIntegrationManager().isEnabled(IntegrationManager.Integration.ESS) && Essentials.isIgnored(listeningPlayer, talkingPlayer)) {
                    continue;
                }
                FPlayer you = FPlayers.fPlayers().getByPlayer(listeningPlayer);
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
