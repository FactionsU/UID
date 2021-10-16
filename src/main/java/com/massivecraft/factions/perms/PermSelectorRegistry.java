package com.massivecraft.factions.perms;

import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.selector.AllSelector;
import com.massivecraft.factions.perms.selector.FactionSelector;
import com.massivecraft.factions.perms.selector.PlayerSelector;
import com.massivecraft.factions.perms.selector.RelationAtLeastSelector;
import com.massivecraft.factions.perms.selector.RelationAtMostSelector;
import com.massivecraft.factions.perms.selector.RelationSingleSelector;
import com.massivecraft.factions.perms.selector.RoleAtLeastSelector;
import com.massivecraft.factions.perms.selector.RoleAtMostSelector;
import com.massivecraft.factions.perms.selector.RoleSingleSelector;
import com.massivecraft.factions.perms.selector.UnknownSelector;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class PermSelectorRegistry {
    private static boolean closed = false;
    private static final Map<String, PermSelector.Descriptor> registry = new ConcurrentHashMap<>();

    static {
        register(AllSelector.DESCRIPTOR);

        register(FactionSelector.DESCRIPTOR);

        register(PlayerSelector.DESCRIPTOR);

        register(RoleSingleSelector.DESCRIPTOR);
        register(RoleAtLeastSelector.DESCRIPTOR);
        register(RoleAtMostSelector.DESCRIPTOR);

        register(RelationSingleSelector.DESCRIPTOR);
        register(RelationAtLeastSelector.DESCRIPTOR);
        register(RelationAtMostSelector.DESCRIPTOR);
    }

    private static void close() {
        closed = true;
    }

    public static PermSelector create(String input, boolean log) {
        try {
            return createOrThrow(input);
        } catch (Exception e) {
            if (log) {
                FactionsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not parse perm selector: " + input, e);
            }
            return new UnknownSelector(input);
        }
    }

    public static PermSelector createOrThrow(String input) {
        String[] split = input.split(":", 2);
        PermSelector.Descriptor descriptor = getDescriptor(split[0]);
        if (split.length < 2 || descriptor == null) {
            throw new IllegalArgumentException(split.length < 2 ? "Missing ':'" : "Unknown selector");
        }
        return descriptor.create(split[1]);
    }

    public static Set<String> getSelectors() {
        return registry.keySet();
    }

    public static PermSelector.Descriptor getDescriptor(String name) {
        return name == null ? null : registry.get(name.toLowerCase());
    }

    public static void register(PermSelector.Descriptor descriptor) {
        if (closed) {
            throw new IllegalStateException("Cannot register PermSelectors. Must be done during onLoad().");
        }
        String name = descriptor.getName();
        if (registry.containsKey(name.toLowerCase())) {
            throw new IllegalArgumentException("PermSelector with name " + name + " already registered");
        }
        registry.put(name.toLowerCase(), descriptor);
    }
}
