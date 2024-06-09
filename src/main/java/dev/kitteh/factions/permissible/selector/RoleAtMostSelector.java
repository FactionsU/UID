package dev.kitteh.factions.permissible.selector;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.permissible.Role;

public class RoleAtMostSelector extends AbstractRoleSelector {
    public static final String NAME = "role-atmost";
    public static final Descriptor DESCRIPTOR = new AbstractRoleSelector.RoleDescriptor(NAME, FactionsPlugin.getInstance().tl().permissions().selectors().roleAtMost()::getDisplayName, RoleAtMostSelector::new);

    public RoleAtMostSelector(Role role) {
        super(DESCRIPTOR, role);
    }

    @Override
    public boolean test(Role role) {
        return role != null && role.isAtMost(this.role);
    }
}
