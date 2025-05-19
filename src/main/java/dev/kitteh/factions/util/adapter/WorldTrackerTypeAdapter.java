package dev.kitteh.factions.util.adapter;

import com.google.gson.FormattingStyle;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.kitteh.factions.util.WorldTracker;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.IOException;

public class WorldTrackerTypeAdapter extends TypeAdapter<Object2ObjectOpenHashMap<String, WorldTracker>> {
    @Override
    public void write(JsonWriter out, Object2ObjectOpenHashMap<String, WorldTracker> value) throws IOException {
        FormattingStyle style = out.getFormattingStyle();

        out.beginObject();

        var iterator = value.object2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            out.name(entry.getKey());
            out.beginArray();
            out.setFormattingStyle(FormattingStyle.COMPACT);
            Long2IntMap map = entry.getValue().chunkIdMapForSave();
            for (Long2IntMap.Entry e : map.long2IntEntrySet()) {
                out.value(e.getLongKey());
                out.value(e.getIntValue());
            }
            out.endArray();
            out.setFormattingStyle(style);
        }

        out.endObject();
    }

    @Override
    public Object2ObjectOpenHashMap<String, WorldTracker> read(JsonReader in) throws IOException {
        Object2ObjectOpenHashMap<String, WorldTracker> ret = new Object2ObjectOpenHashMap<>();

        String worldName;
        Long2IntMap map;

        in.beginObject();
        while (in.hasNext()) {
            map = new Long2IntOpenHashMap();
            worldName = in.nextName();
            in.beginArray();
            while (in.hasNext()) {
                long id = in.nextLong();
                int val = in.nextInt();
                map.put(id, val);
            }
            in.endArray();
            ret.put(worldName, new WorldTracker(worldName, map));

        }
        in.endObject();
        return ret;
    }
}
