package dev.kitteh.factions;

import dev.kitteh.factions.data.MemoryFPlayers;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.UUID;

@NullMarked
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
