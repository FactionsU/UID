package dev.kitteh.factions.util.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import dev.kitteh.factions.chat.ChatTarget;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;

import java.lang.reflect.Type;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;

public class LocalTimeAdapter implements JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {
    private final DateTimeFormatter LOCAL_TIME = DateTimeFormatter.ISO_LOCAL_TIME;

    @Override
    public LocalTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        try {
            return LocalTime.parse(jsonElement.getAsString(), LOCAL_TIME);
        }  catch (DateTimeParseException e) {
            AbstractFactionsPlugin.instance().getLogger().log(Level.WARNING, "Could not parse LocalTime from " + jsonElement, e);
            return LocalTime.MIDNIGHT;
        }
    }

    @Override
    public JsonElement serialize(LocalTime localTime, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(localTime.format(LOCAL_TIME));
    }
}
