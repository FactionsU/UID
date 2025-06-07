package dev.kitteh.factions.util.adapter;

import com.google.gson.*;
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
