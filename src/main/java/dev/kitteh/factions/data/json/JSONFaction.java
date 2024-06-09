package dev.kitteh.factions.data.json;

import dev.kitteh.factions.data.MemoryFaction;

public class JSONFaction extends MemoryFaction {
    public JSONFaction(String id) {
        this(Integer.parseInt(id));
    }

    public JSONFaction(int id) {
        super(id);
    }
}
