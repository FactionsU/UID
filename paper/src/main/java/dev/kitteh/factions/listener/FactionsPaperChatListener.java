package dev.kitteh.factions.listener;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.chat.ChatTarget;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.WorldUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class FactionsPaperChatListener implements Listener {
    // relation and role chats
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer().getWorld())) {
            return;
        }

        Player player = event.getPlayer();
        FPlayer me = FPlayers.fPlayers().get(player);
        ChatTarget chatTarget = me.chatTarget();

        if (chatTarget == ChatTarget.PUBLIC) {
            return;
        }

        event.setCancelled(true);

        Faction faction = me.faction();
        MainConfig.Factions.Chat.InternalChat chatConf = FactionsPlugin.instance().conf().factions().chat().internalChat();
        LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();

        TagResolver messagePlaceholder = Placeholder.component("message", event.message());

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
            event.setCancelled(true);
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
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChatLater(AsyncChatEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer().getWorld())) {
            return;
        }

        MainConfig.Factions.Chat.Paper chatConf = FactionsPlugin.instance().conf().factions().chat().paper();

        if (chatConf.isEnabled()) {
            FPlayer fPlayer = FPlayers.fPlayers().get(event.getPlayer());
            String format = fPlayer.hasFaction() ? chatConf.getFormatHasFaction() : chatConf.getFormatNoFaction();
            event.renderer((source, sourceDisplayName, message, viewer) -> {
                Player observer = viewer instanceof Player player ? player : null;
                return Mini.parse(format,
                        Placeholder.component("message", message),
                        FPlayerResolver.of("sender", observer, fPlayer)
                );
            });
        }
    }
}
