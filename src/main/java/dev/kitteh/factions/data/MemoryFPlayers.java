package dev.kitteh.factions.data;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;

public abstract class MemoryFPlayers implements FPlayers {
    public Map<UUID, FPlayer> fPlayers = new ConcurrentSkipListMap<>();

    @Override
    public List<FPlayer> getAllFPlayers() {
        return new ArrayList<>(fPlayers.values());
    }

    @Override
    public FPlayer getById(UUID id) {
        return fPlayers.computeIfAbsent(id, this::constructNewFPlayer);
    }

    protected abstract FPlayer constructNewFPlayer(UUID id);

    public abstract int load();

    public abstract void forceSave();

    public abstract void forceSave(boolean sync);
}
