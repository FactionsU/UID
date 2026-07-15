package dev.kitteh.factions.util.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import dev.kitteh.factions.upgrade.LeveledValueProvider;

import java.lang.reflect.Type;

public class LeveledValueProviderEquationSerializer implements JsonSerializer<LeveledValueProvider.Equation> {
    @Override
    public JsonElement serialize(LeveledValueProvider.Equation src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("expression", src.expression().getExpressionString());
        return obj;
    }
}
