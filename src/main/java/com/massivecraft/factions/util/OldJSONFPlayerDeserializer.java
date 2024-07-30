package com.massivecraft.factions.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.data.json.JSONFPlayer;

import java.lang.reflect.Type;

public class OldJSONFPlayerDeserializer implements JsonDeserializer<JSONFPlayer> {
    @Override
    public JSONFPlayer deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonElement factionIdE = jsonElement.getAsJsonObject().remove("factionId");
        String factionIdS = factionIdE.getAsString();
        int factionId;
        try {
            factionId = Integer.parseInt(factionIdS);
        } catch (NumberFormatException e) {
            factionId = 0;
        }
        jsonElement.getAsJsonObject().addProperty("factionId", factionId);
        return FactionsPlugin.getInstance().getGson().fromJson(jsonElement, JSONFPlayer.class);
    }
}
