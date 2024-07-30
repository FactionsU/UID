package com.massivecraft.factions.data.json;

import com.massivecraft.factions.data.MemoryFaction;

public class JSONFaction extends MemoryFaction {
    @Deprecated
    public JSONFaction(MemoryFaction arg0) {
        super(arg0);
    }

    private JSONFaction() {
    }

    public JSONFaction(String id) {
        this(Integer.parseInt(id));
    }

    public JSONFaction(int id) {
        super(id);
    }
}
