package dev.kitteh.factions;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collection;

public interface FPlayers {
    void clean();

    static FPlayers getInstance() {
        return Instances.PLAYERS;
    }

    Collection<FPlayer> getOnlinePlayers();

    FPlayer getByPlayer(Player player);

    Collection<FPlayer> getAllFPlayers();

    void forceSave();

    void forceSave(boolean sync);

    FPlayer getByOfflinePlayer(OfflinePlayer player);

    FPlayer getById(String string);

    int load();
}
