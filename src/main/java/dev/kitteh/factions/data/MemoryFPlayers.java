package dev.kitteh.factions.data;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;

public abstract class MemoryFPlayers implements FPlayers {
    protected Map<UUID, FPlayer> fPlayers = new ConcurrentSkipListMap<>();

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

    public abstract void forceSave(boolean sync);

    public abstract void removePlayer(FPlayer fPlayer);
}
