package dev.kitteh.factions.util.adapter;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dev.kitteh.factions.upgrade.UpgradeRegistry;
import dev.kitteh.factions.upgrade.UpgradeVariable;

import java.io.IOException;

public class UpgradeVariableTypeAdapter extends TypeAdapter<UpgradeVariable> {
    @Override
    public void write(JsonWriter out, UpgradeVariable value) throws IOException {
        out.value(value.name());
    }

    @Override
    public UpgradeVariable read(JsonReader in) throws IOException {
        JsonToken peek = in.peek();

        if (peek != JsonToken.STRING) {
            throw new JsonParseException("Non-string token found for UpgradeVariable! Found " + peek.name());
        }
        String string = in.nextString();

        UpgradeVariable var = UpgradeRegistry.getVariable(string);

        if (var == null) {
            throw new JsonParseException("Unknown UpgradeVariable '" + string + "'");
        }

        return var;
    }
}
