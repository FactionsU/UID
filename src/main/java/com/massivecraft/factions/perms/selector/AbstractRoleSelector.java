package com.massivecraft.factions.perms.selector;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.data.MemoryFaction;
import com.massivecraft.factions.perms.PermSelector;
import com.massivecraft.factions.perms.Role;
import com.massivecraft.factions.perms.Selectable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractRoleSelector extends AbstractSelector {
    public static class RoleDescriptor extends BasicDescriptor {
        private List<PermSelector> roleSelectors;
        private final Function<Role, PermSelector> function;

        public RoleDescriptor(String name, Supplier<String> displayName, Function<Role, PermSelector> function) {
            super(name, displayName, input -> function.apply(Role.fromString(input)));
            this.function = function;
        }

        @Override
        public Map<String, String> getOptions(Faction faction) {
            List<PermSelector> available = new ArrayList<>(roleSelectors == null ? (roleSelectors = Arrays.stream(Role.values()).map(function).collect(Collectors.toList())) : roleSelectors);
            available.removeAll(((MemoryFaction) faction).getPermissions().keySet());

            Map<String, String> map = new LinkedHashMap<>();

            for (PermSelector selector : available) {
                map.put(selector.serialize(), ((AbstractRoleSelector) selector).getRole().getTranslation());
            }

            return map;
        }
    }

    protected final Role role;

    public AbstractRoleSelector(Descriptor descriptor, Role role) {
        super(descriptor);
        this.role = Objects.requireNonNull(role);
    }

    public Role getRole() {
        return role;
    }

    @Override
    public String serializeValue() {
        return this.role.name();
    }

    @Override
    public Component displayValue(Faction context) {
        return LegacyComponentSerializer.legacySection().deserialize(this.role.getColor() + this.role.getTranslation());
    }

    @Override
    public boolean test(Selectable selectable, Faction faction) {
        if (selectable instanceof FPlayer player) {
            if (player.getFaction() == faction) {
                return test(player.getRole());
            }
        } else if (selectable instanceof Role) {
            return test((Role) selectable);
        }
        return false;
    }

    public abstract boolean test(Role role);
}
