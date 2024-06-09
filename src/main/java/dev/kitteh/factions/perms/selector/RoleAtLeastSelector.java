package dev.kitteh.factions.perms.selector;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.perms.Role;

public class RoleAtLeastSelector extends AbstractRoleSelector {
    public static final String NAME = "role-atleast";
    public static final Descriptor DESCRIPTOR = new AbstractRoleSelector.RoleDescriptor(NAME, FactionsPlugin.getInstance().tl().permissions().selectors().roleAtLeast()::getDisplayName, RoleAtLeastSelector::new);

    public RoleAtLeastSelector(Role role) {
        super(DESCRIPTOR, role);
    }

    @Override
    public boolean test(Role role) {
        return role != null && role.isAtLeast(this.role);
    }
}
