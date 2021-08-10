package com.massivecraft.factions.perms;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PermissibleActionRegistry {
    private static boolean closed = false;
    private static final Map<String, PermissibleAction> registry = new ConcurrentHashMap<>();

    static {
        for (PermissibleAction action : PermissibleActions.values()) {
            register(action);
        }
    }

    private static void close() {
        closed = true;
    }

    public static PermissibleAction get(String name) {
        return name == null ? null : registry.get(name.toLowerCase());
    }

    public static Collection<? extends PermissibleAction> getActions() {
        return new HashSet<>(registry.values());
    }

    public static void register(PermissibleAction action) {
        if (closed) {
            throw new IllegalStateException("Cannot register PermissibleActions. Must be done during onLoad().");
        }
        if (registry.containsKey(action.getName().toLowerCase())) {
            throw new IllegalArgumentException("Permissible action with name " + action.getName() + " already registered");
        }
        registry.put(action.getName().toLowerCase(), action);
    }
}
