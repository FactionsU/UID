package dev.kitteh.factions.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.data.json.JSONFaction;

import java.lang.reflect.Type;

public class OldJSONFactionDeserializer implements JsonDeserializer<JSONFaction> {
    @Override
    public JSONFaction deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        jsonElement.getAsJsonObject().remove("id");
        return FactionsPlugin.getInstance().getGson().fromJson(jsonElement, JSONFaction.class);
    }
}
