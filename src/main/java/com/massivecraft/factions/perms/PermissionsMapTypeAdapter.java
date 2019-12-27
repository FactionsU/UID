package com.massivecraft.factions.perms;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.util.TL;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class PermissionsMapTypeAdapter implements JsonDeserializer<Map<Permissible, Map<PermissibleAction, Boolean>>> {

    @Override
    public Map<Permissible, Map<PermissibleAction, Boolean>> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {

        try {
            JsonObject obj = json.getAsJsonObject();
            if (obj == null) {
                return null;
            }

            Map<Permissible, Map<PermissibleAction, Boolean>> permissionsMap = new ConcurrentHashMap<>();

            // Top level is Relation
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                Permissible permissible = getPermissible(entry.getKey());

                if (permissible == null) {
                    continue;
                }

                // Second level is the map between action -> access
                Map<PermissibleAction, Boolean> accessMap = new HashMap<>();
                for (Map.Entry<String, JsonElement> entry2 : entry.getValue().getAsJsonObject().entrySet()) {
                    PermissibleAction permissibleAction = PermissibleAction.fromString(entry2.getKey());
                    boolean bool;
                    try {
                        bool = entry2.getValue().getAsBoolean();
                    } catch (Exception e) {
                        continue;
                    }
                    accessMap.put(permissibleAction, bool);
                }
                permissionsMap.put(permissible, accessMap);
            }

            return permissionsMap;

        } catch (Exception ex) {
            FactionsPlugin.getInstance().getLogger().log(Level.SEVERE, "Error encountered while deserializing a PermissionsMap.", ex);
            return null;
        }
    }

    private Permissible getPermissible(String name) {
        // If name is uppercase then it is (probably, no way to completely know) valid if not begin conversion
        if (name.equals(name.toUpperCase())) {
            if (Role.fromString(name.toUpperCase()) != null) {
                return Role.fromString(name.toUpperCase());
            } else if (Relation.fromString(name.toUpperCase()) != null) {
                return Relation.fromString(name.toUpperCase());
            } else {
                return null;
            }
        } else {
            if (name.equals(TL.ROLE_RECRUIT.toString())) {
                return Role.RECRUIT;
            } else if (name.equals(TL.ROLE_NORMAL.toString())) {
                return Role.NORMAL;
            } else if (name.equals(TL.ROLE_MODERATOR.toString())) {
                return Role.MODERATOR;
            } else {
                // If it is explicitly member and its old data then it refers to relation member not role, skip it
                if (name.equals("member")) {
                    return null;
                }
                return Relation.fromString(name);
            }
        }
    }
}
