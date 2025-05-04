package dev.kitteh.factions.util.adapter;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dev.kitteh.factions.upgrade.Upgrade;
import dev.kitteh.factions.upgrade.UpgradeRegistry;

import java.io.IOException;

public class UpgradeTypeAdapter extends TypeAdapter<Upgrade> {
    @Override
    public void write(JsonWriter out, Upgrade value) throws IOException {
        out.value(value.name());
    }

    @Override
    public Upgrade read(JsonReader in) throws IOException {
        JsonToken peek = in.peek();

        if (peek != JsonToken.STRING) {
            throw new JsonParseException("Non-string token found for Upgrade! Found " + peek.name());
        }
        String string = in.nextString();

        Upgrade upgrade = UpgradeRegistry.getUpgrade(string);

        if (upgrade == null) {
            throw new JsonParseException("Unknown Upgrade '" + string + "'");
        }

        return upgrade;
    }
}
