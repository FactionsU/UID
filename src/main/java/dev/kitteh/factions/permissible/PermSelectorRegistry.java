package dev.kitteh.factions.permissible;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.permissible.selector.AllSelector;
import dev.kitteh.factions.permissible.selector.FactionSelector;
import dev.kitteh.factions.permissible.selector.PlayerSelector;
import dev.kitteh.factions.permissible.selector.RelationAtLeastSelector;
import dev.kitteh.factions.permissible.selector.RelationAtMostSelector;
import dev.kitteh.factions.permissible.selector.RelationSingleSelector;
import dev.kitteh.factions.permissible.selector.RoleAtLeastSelector;
import dev.kitteh.factions.permissible.selector.RoleAtMostSelector;
import dev.kitteh.factions.permissible.selector.RoleSingleSelector;
import dev.kitteh.factions.permissible.selector.UnknownSelector;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

@NullMarked
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

    @SuppressWarnings("unused")
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

    public static PermSelector.@Nullable Descriptor getDescriptor(String name) {
        return registry.get(name.toLowerCase());
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
