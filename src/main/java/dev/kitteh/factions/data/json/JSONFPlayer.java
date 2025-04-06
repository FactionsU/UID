package dev.kitteh.factions.data.json;

import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.data.MemoryFPlayer;
import dev.kitteh.factions.data.MemoryFPlayers;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public final class JSONFPlayer extends MemoryFPlayer {
    public JSONFPlayer(UUID id) {
        super(id);
    }

    @Override
    public void remove() {
        ((MemoryFPlayers) FPlayers.getInstance()).removePlayer(this);
    }
}
