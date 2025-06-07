package dev.kitteh.factions.util.adapter;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dev.kitteh.factions.permissible.PermSelector;
import dev.kitteh.factions.permissible.PermSelectorRegistry;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.permissible.selector.PlayerSelector;
import dev.kitteh.factions.permissible.selector.RelationSingleSelector;
import dev.kitteh.factions.permissible.selector.RoleSingleSelector;
import dev.kitteh.factions.permissible.selector.UnknownSelector;

import java.io.IOException;
import java.util.UUID;

public class PermSelectorAdapter extends TypeAdapter<PermSelector> {
    private static boolean legacy = false;

    @Override
    public void write(JsonWriter out, PermSelector value) throws IOException {
        out.value(value.serialize());
    }

    @Override
    public PermSelector read(JsonReader in) throws IOException {
        JsonToken peek = in.peek();

        if (peek != JsonToken.STRING) {
            throw new JsonParseException("Non-string token found for PermSelector! Found " + peek.name());
        }
        String string = in.nextString();

        PermSelector selector = null;
        if (legacy && !string.contains(":")) {
            selector = getLegacy(string);
        }
        if (selector == null) {
            selector = PermSelectorRegistry.create(string, true);
        }
        if (selector instanceof UnknownSelector) {
            PermSelector s = getLegacy(string);
            if (s != null) {
                selector = s;
                setLegacy();
            }
        }
        return selector;
    }

    public static void setLegacy() {
        legacy = true;
    }

    private PermSelector getLegacy(String string) {
        Role role = Role.fromString(string.toUpperCase());
        if (role != null) {
            return new RoleSingleSelector(role);
        }
        try {
            Relation relation = Relation.valueOf(string.toUpperCase());
            if (relation != Relation.MEMBER) {
                return new RelationSingleSelector(relation);
            }
        } catch (IllegalArgumentException ignored) {
        }
        try {
            UUID uuid = UUID.fromString(string);
            return new PlayerSelector(uuid);
        } catch (IllegalArgumentException ignored) {
        }

        return null;
    }
}
