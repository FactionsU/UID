package dev.kitteh.factions.util.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.data.json.JSONFaction;
import dev.kitteh.factions.permissible.Relation;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class OldJSONFactionDeserializer implements JsonDeserializer<JSONFaction> {
    @Override
    public JSONFaction deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonElement idElement = jsonElement.getAsJsonObject().remove("id");
        String id = idElement.getAsString();
        try {
            Integer.parseInt(id);
        } catch (NumberFormatException e) {
            FactionsPlugin.getInstance().getLogger().warning("Invalid faction id found: " + id);
        }
        JsonElement relationWish = jsonElement.getAsJsonObject().get("relationWish").getAsJsonObject();

        Iterator<String> iterator = relationWish.getAsJsonObject().keySet().iterator();
        while(iterator.hasNext()) {
            String key = iterator.next();
            try {
                Integer.parseInt(key);
            } catch (NumberFormatException e) {
                iterator.remove();
            }
        }

        return FactionsPlugin.getInstance().getGson().fromJson(jsonElement, JSONFaction.class);
    }
}
