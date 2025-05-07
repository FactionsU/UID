package dev.kitteh.factions.data.json;

import dev.kitteh.factions.data.MemoryFaction;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class JSONFaction extends MemoryFaction {
    public JSONFaction(int id, String tag) {
        super(id, tag);
    }
}
