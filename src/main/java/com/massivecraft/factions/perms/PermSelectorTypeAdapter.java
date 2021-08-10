package com.massivecraft.factions.perms;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.selector.PlayerSelector;
import com.massivecraft.factions.perms.selector.RelationSingleSelector;
import com.massivecraft.factions.perms.selector.RoleSingleSelector;
import com.massivecraft.factions.perms.selector.UnknownSelector;

import java.io.IOException;
import java.util.UUID;

public class PermSelectorTypeAdapter extends TypeAdapter<PermSelector> {
    private static boolean legacy = false;

    public static boolean isLegacy() {
        return legacy;
    }

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
        if (legacy) {
            selector = getLegacy(string);
        }
        if (selector == null) {
            selector = PermSelectorRegistry.create(string, true);
        }
        if (selector instanceof UnknownSelector) {
            PermSelector s = getLegacy(string);
            if (s != null) {
                selector = s;
                legacy = true;
                FactionsPlugin.getInstance().debug("Legacy PermSelector import go!");
            }
        }
        return selector;
    }

    private PermSelector getLegacy(String string) {
        Permissible permissible = Role.fromString(string.toUpperCase());
        if (permissible != null) {
            return new RoleSingleSelector((Role) permissible);
        }
        permissible = Relation.fromString(string.toUpperCase());
        if (permissible != null && permissible != Relation.NEUTRAL) {
            return new RelationSingleSelector((Relation) permissible);
        }
        try {
            UUID uuid = UUID.fromString(string);
            return new PlayerSelector(uuid);
        } catch (IllegalArgumentException ignored) {
        }

        return null;
    }
}
