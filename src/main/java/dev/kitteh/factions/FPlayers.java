package dev.kitteh.factions;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Collection;
import java.util.UUID;

public interface FPlayers {
    static FPlayers getInstance() {
        return Instances.PLAYERS;
    }

    default Collection<FPlayer> getOnlinePlayers() {
        return Bukkit.getServer().getOnlinePlayers().stream().map(this::getByPlayer).toList();
    }

    Collection<FPlayer> getAllFPlayers();

    default FPlayer getByPlayer(OfflinePlayer player) {
        return this.getById(player.getUniqueId());
    }

    FPlayer getById(UUID uuid);
}
