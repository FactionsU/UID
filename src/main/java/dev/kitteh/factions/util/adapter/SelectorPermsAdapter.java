package dev.kitteh.factions.util.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.kitteh.factions.data.MemoryFaction;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SelectorPermsAdapter extends TypeAdapter<MemoryFaction.Permissions.SelectorPerms> {
    @Override
    public void write(JsonWriter out, MemoryFaction.Permissions.SelectorPerms value) throws IOException {
        out.beginObject();
        for (Map.Entry<String, Boolean> entry : value.getPerms().entrySet()) {
            out.name(entry.getKey());
            out.value(entry.getValue().booleanValue());
        }
        out.endObject();
    }

    @Override
    public MemoryFaction.Permissions.SelectorPerms read(JsonReader in) throws IOException {
        Map<String, Boolean> perms = new HashMap<>();

        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            perms.put(name, in.nextBoolean());
        }
        in.endObject();

        return new MemoryFaction.Permissions.SelectorPerms(perms);
    }
}
