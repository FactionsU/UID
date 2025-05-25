package dev.kitteh.factions.util.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import dev.kitteh.factions.chat.ChatTarget;

import java.lang.reflect.Type;

public class ChatTargetAdapter implements JsonSerializer<ChatTarget>, JsonDeserializer<ChatTarget> {
    @Override
    public ChatTarget deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement instanceof JsonPrimitive p && p.isString()) {
            String[] pieces = p.getAsString().split(":");
            return switch (pieces[0]) {
                case "RELATION" -> switch (pieces[1]) {
                    case "ALLY" -> ChatTarget.Relation.ALLY;
                    case "TRUCE" -> ChatTarget.Relation.TRUCE;
                    default -> ChatTarget.PUBLIC;
                };
                case "ROLE" -> switch (pieces[1]) {
                    case "COLEADER" -> ChatTarget.Role.COLEADER;
                    case "MODERATOR" -> ChatTarget.Role.MODERATOR;
                    case "NORMAL" -> ChatTarget.Role.NORMAL;
                    case "RECRUIT" -> ChatTarget.Role.ALL;
                    default -> ChatTarget.PUBLIC;
                };
                default -> ChatTarget.PUBLIC;
            };
        }
        return ChatTarget.PUBLIC;
    }

    @Override
    public JsonElement serialize(ChatTarget chatTarget, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(switch (chatTarget) {
            case ChatTarget.Relation r -> "RELATION:" + r.relation().name();
            case ChatTarget.Role r -> "ROLE:" + r.role().name();
            default -> "PUBLIC";
        });
    }
}
