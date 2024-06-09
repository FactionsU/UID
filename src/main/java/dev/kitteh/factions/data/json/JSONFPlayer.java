package dev.kitteh.factions.data.json;

import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.data.MemoryFPlayer;

public class JSONFPlayer extends MemoryFPlayer {
    @Deprecated
    public JSONFPlayer(MemoryFPlayer arg0) {
        super(arg0);
    }

    public JSONFPlayer(String id) {
        super(id);
    }

    @Override
    public void remove() {
        ((JSONFPlayers) FPlayers.getInstance()).fPlayers.remove(getId());
    }
}
