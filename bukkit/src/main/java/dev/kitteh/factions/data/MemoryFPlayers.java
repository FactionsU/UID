package dev.kitteh.factions.data;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;

@NullMarked
public abstract class MemoryFPlayers implements FPlayers {
    protected final Map<UUID, FPlayer> fPlayers = new ConcurrentSkipListMap<>();

    @Override
    public List<FPlayer> all() {
        return new ArrayList<>(fPlayers.values());
    }

    @Override
    public FPlayer get(UUID id) {
        return fPlayers.computeIfAbsent(id, this::constructNewFPlayer);
    }

    @Override
    public boolean has(UUID uuid) {
        return fPlayers.containsKey(uuid);
    }

    protected abstract FPlayer constructNewFPlayer(UUID id);

    public abstract int load();

    public abstract void forceSave(boolean sync);

    public abstract void removePlayer(FPlayer fPlayer);
}
