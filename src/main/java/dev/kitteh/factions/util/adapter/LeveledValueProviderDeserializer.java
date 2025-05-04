package dev.kitteh.factions.util.adapter;

import com.google.gson.*;
import dev.kitteh.factions.upgrade.LeveledValueProvider;

import java.lang.reflect.Type;

public class LeveledValueProviderDeserializer implements JsonDeserializer<LeveledValueProvider> {
    @Override
    public LeveledValueProvider deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();

        if (object.has("levels")) {
            return jsonDeserializationContext.deserialize(jsonElement, LeveledValueProvider.LevelMap.class);
        } else if (object.has("expression")) {
            return LeveledValueProvider.Equation.of(object.getAsJsonPrimitive("expression").getAsString());
        } else {
            throw new JsonParseException("Unexpected JSON object: " + object);
        }
    }
}
