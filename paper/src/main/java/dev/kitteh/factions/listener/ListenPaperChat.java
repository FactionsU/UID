package dev.kitteh.factions.listener;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.chat.ChatTarget;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.WorldUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ListenPaperChat implements Listener {
    // relation and role chats
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer())) {
            return;
        }

        Player player = event.getPlayer();
        FPlayer me = FPlayers.fPlayers().get(player);

        if (me.chatTarget() == ChatTarget.PUBLIC) {
            return;
        }

        event.setCancelled(true);

        ListenSpigotChat.processFactionChat(me, event.message());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChatLater(AsyncChatEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer())) {
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
