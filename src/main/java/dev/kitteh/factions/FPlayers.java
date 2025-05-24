package dev.kitteh.factions;

import dev.kitteh.factions.plugin.Instances;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.UUID;

@NullMarked
public interface FPlayers {
    static FPlayers fPlayers() {
        return Instances.PLAYERS;
    }

    default Collection<FPlayer> online() {
        return Bukkit.getServer().getOnlinePlayers().stream().map(this::get).toList();
    }

    Collection<FPlayer> all();

    default FPlayer get(OfflinePlayer player) {
        return this.get(player.getUniqueId());
    }

    FPlayer get(UUID uuid);

    boolean has(UUID uuid);
}
