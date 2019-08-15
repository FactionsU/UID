package com.massivecraft.factions;

import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.util.Set;

public interface FactionsAPI {
    /**
     * Gets the current API version.
     *
     * @return current API version
     * @since API 5
     */
    default int getAPIVersion() {
        return 5;
    }

    boolean isAnotherPluginHandlingChat();

    void setHandlingChat(Plugin plugin, boolean handling);

    boolean shouldLetFactionsHandleThisChat(AsyncPlayerChatEvent event);

    boolean isPlayerFactionChatting(Player player);

    String getPlayerFactionTag(Player player);

    String getPlayerFactionTagRelation(Player speaker, Player listener);

    String getPlayerTitle(Player player);

    Set<String> getFactionTags();

    Set<String> getPlayersInFaction(String factionTag);

    Set<String> getOnlinePlayersInFaction(String factionTag);
}
