package dev.kitteh.factions.permissible.selector;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.permissible.Role;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class RoleAtLeastSelector extends AbstractRoleSelector {
    public static final String NAME = "role-atleast";
    public static final Descriptor DESCRIPTOR = new AbstractRoleSelector.RoleDescriptor(NAME, FactionsPlugin.instance().tl().permissions().selectors().roleAtLeast()::getDisplayName, RoleAtLeastSelector::new);

    public RoleAtLeastSelector(Role role) {
        super(DESCRIPTOR, role);
    }

    @Override
    public boolean test(@Nullable Role role) {
        return role != null && role.isAtLeast(this.role);
    }
}
