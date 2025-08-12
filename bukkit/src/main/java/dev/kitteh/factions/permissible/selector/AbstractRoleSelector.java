package dev.kitteh.factions.permissible.selector;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.permissible.PermSelector;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.permissible.Selectable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public abstract class AbstractRoleSelector extends AbstractSelector {
    public static class RoleDescriptor extends BasicDescriptor {
        private @Nullable List<PermSelector> roleSelectors;
        private final Function<Role, PermSelector> function;

        public RoleDescriptor(String name, Supplier<String> displayName, Function<Role, PermSelector> function) {
            super(name, displayName, input -> function.apply(Objects.requireNonNull(Role.fromString(input))));
            this.function = function;
        }

        @Override
        public Map<String, String> options(Faction faction) {
            List<PermSelector> available = new ArrayList<>(roleSelectors == null ? (roleSelectors = Arrays.stream(Role.values()).map(function).collect(Collectors.toList())) : roleSelectors);
            available.removeAll(faction.permissions().selectors());

            Map<String, String> map = new LinkedHashMap<>();

            for (PermSelector selector : available) {
                map.put(selector.serialize(), ((AbstractRoleSelector) selector).role().translation());
            }

            return map;
        }
    }

    protected final Role role;

    public AbstractRoleSelector(PermSelector.Descriptor descriptor, Role role) {
        super(descriptor);
        this.role = Objects.requireNonNull(role);
    }

    public Role role() {
        return role;
    }

    @Override
    public String serializeValue() {
        return this.role.name();
    }

    @Override
    public Component displayValue(Faction context) {
        return LegacyComponentSerializer.legacySection().deserialize(this.role.translation()).color(this.role.color());
    }

    @Override
    public boolean test(Selectable selectable, Faction faction) {
        if (selectable instanceof FPlayer player) {
            if (player.faction() == faction) {
                return test(player.role());
            }
        } else if (selectable instanceof Role) {
            return test((Role) selectable);
        }
        return false;
    }

    public abstract boolean test(@Nullable Role role);
}
