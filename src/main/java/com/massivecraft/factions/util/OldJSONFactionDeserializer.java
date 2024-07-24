package com.massivecraft.factions.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.data.json.JSONFaction;

import java.lang.reflect.Type;

public class OldJSONFactionDeserializer implements JsonDeserializer<JSONFaction> {
    @Override
    public JSONFaction deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        jsonElement.getAsJsonObject().remove("id");
        return FactionsPlugin.getInstance().getGson().fromJson(jsonElement, JSONFaction.class);
    }
}
