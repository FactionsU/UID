package dev.kitteh.factions.data.json;

import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.data.MemoryFPlayer;

import java.util.UUID;

public class JSONFPlayer extends MemoryFPlayer {
    public JSONFPlayer(UUID id) {
        super(id);
    }

    @Override
    public void remove() {
        ((JSONFPlayers) FPlayers.getInstance()).fPlayers.remove(getUniqueId());
    }
}
