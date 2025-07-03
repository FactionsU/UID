package dev.kitteh.factions.listener;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.chat.ChatTarget;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.integration.ExternalChecks;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.TextUtil;
import dev.kitteh.factions.util.WorldUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UnknownFormatConversionException;
import java.util.logging.Level;

public class FactionsLegacyChatListener implements Listener {

    public final AbstractFactionsPlugin plugin;

    public FactionsLegacyChatListener(AbstractFactionsPlugin plugin) {
        this.plugin = plugin;
    }

    public static void processFactionChat(FPlayer me, Component message) {
        ChatTarget chatTarget = me.chatTarget();

        Faction faction = me.faction();
        MainConfig.Factions.Chat.InternalChat chatConf = FactionsPlugin.instance().conf().factions().chat().internalChat();
        LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();

        TagResolver messagePlaceholder = Placeholder.component("message", message);

        if (chatTarget instanceof ChatTarget.Role(Role role)) {
            String format = role == Role.RECRUIT ? chatConf.getFactionMemberAllChatFormat() : chatConf.getFactionMemberChatFormat();
            String spyFormat = chatConf.getSpyingPrefix() + format;

            for (FPlayer fPlayer : faction.membersOnline(true)) {
                boolean qualifies = fPlayer.role().isAtLeast(role);
                if (qualifies || fPlayer.spyingChat()) {
                    fPlayer.sendRichMessage((qualifies ? format : spyFormat),
                            messagePlaceholder,
                            Placeholder.component("role", legacy.deserialize(role.nicename)),
                            FPlayerResolver.of("sender", fPlayer, me),
                            FactionResolver.of(fPlayer, faction)
                    );
                }
            }
        } else if (chatTarget instanceof ChatTarget.Relation(Relation relation)) {
            String format = chatConf.getRelationChatFormat();
            String spyFormat = chatConf.getSpyingPrefix() + format;

            for (FPlayer fPlayer : FPlayers.fPlayers().online()) {
                boolean qualifies = fPlayer.faction() == faction || (faction.relationTo(fPlayer) == relation &&
                        (
                                (relation == Relation.ALLY && !fPlayer.ignoreAllianceChat()) ||
                                        (relation == Relation.TRUCE && !fPlayer.ignoreTruceChat())
                        ));
                if (qualifies || fPlayer.spyingChat()) {
                    fPlayer.sendRichMessage((qualifies ? format : spyFormat),
                            messagePlaceholder,
                            Placeholder.component("relation", legacy.deserialize(relation.nicename)),
                            FPlayerResolver.of("sender", fPlayer, me),
                            FactionResolver.of(fPlayer, faction)
                    );
                }
            }
        }
    }

    // this is for handling faction/relation chat, set at low priority so Factions gets to them first
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerEarlyChat(AsyncPlayerChatEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer().getWorld())) {
            return;
        }

        Player player = event.getPlayer();
        FPlayer me = FPlayers.fPlayers().get(player);

        if (me.chatTarget() == ChatTarget.PUBLIC) {
            return;
        }

        event.setCancelled(true);

        FactionsLegacyChatListener.processFactionChat(me, Component.text(event.getMessage()));
    }

    // this is for handling insertion of the player's faction tag, set at highest priority to give other plugins a chance to modify chat first
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer().getWorld())) {
            return;
        }

        // Are we to insert the Faction tag into the format?
        // If we are not to insert it - we are done.
        if (this.plugin.conf().factions().chat().spigot().isTagHandledByAnotherPlugin()) {
            return;
        }

        Player talkingPlayer = event.getPlayer();
        String msg = event.getMessage();
        String eventFormat = event.getFormat();
        FPlayer me = FPlayers.fPlayers().get(talkingPlayer);
        MainConfig.Factions.Chat.Spigot chatConf = this.plugin.conf().factions().chat().spigot();
        int InsertIndex = chatConf.getTagInsertIndex();

        boolean padBefore = chatConf.isTagPadBefore();
        boolean padAfter = chatConf.isTagPadAfter();

        if (!chatConf.getTagReplaceString().isEmpty() && eventFormat.contains(chatConf.getTagReplaceString())) {
            // we're using the "replace" method of inserting the faction tags
            if (eventFormat.contains("[FACTION_TITLE]")) {
                eventFormat = eventFormat.replace("[FACTION_TITLE]", me.titleLegacy());
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

        String formatStart = eventFormat.substring(0, InsertIndex) + ((padBefore && !chatTagLegacy(me).isEmpty()) ? " " : "");
        String formatEnd = ((padAfter && !chatTagLegacy(me).isEmpty()) ? " " : "") + eventFormat.substring(InsertIndex);

        String nonColoredMsgFormat = formatStart + chatTagLegacy(me).trim() + formatEnd;

        // Relation Colored?
        if (chatConf.isTagRelationColored()) {
            for (Player listeningPlayer : event.getRecipients()) {
                if (ExternalChecks.isIgnored(listeningPlayer, talkingPlayer)) {
                    continue;
                }
                FPlayer you = FPlayers.fPlayers().get(listeningPlayer);
                String yourFormat = formatStart + chatTagLegacy(me, you).trim() + formatEnd;
                try {
                    listeningPlayer.sendMessage(String.format(yourFormat, talkingPlayer.getDisplayName(), msg));
                } catch (UnknownFormatConversionException ex) {

                    this.plugin.log(Level.SEVERE, "Critical error in chat message formatting!");
                    this.plugin.log(Level.SEVERE, "NOTE: This can be fixed right now by setting chat tagInsertIndex to 0.");
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

    private String chatTagLegacy(FPlayer me) {
        return me.hasFaction() ? String.format(FactionsPlugin.instance().conf().factions().chat().spigot().getTagFormat(), me.role().getPrefix() + (me.hasFaction() ? me.faction().tag() : "")) : TL.NOFACTION_PREFIX.toString();
    }

    private String chatTagLegacy(FPlayer me, FPlayer participator) {
        return me.hasFaction() ? TextUtil.getString(me.relationTo(participator).color()) + chatTagLegacy(me) : TL.NOFACTION_PREFIX.toString();
    }
}
