package dev.kitteh.factions.permissible.selector;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.permissible.Role;

public class RoleSingleSelector extends AbstractRoleSelector {
    public static final String NAME = "role-single";
    public static final Descriptor DESCRIPTOR = new AbstractRoleSelector.RoleDescriptor(NAME, FactionsPlugin.getInstance().tl().permissions().selectors().roleSingle()::getDisplayName, RoleSingleSelector::new);

    public RoleSingleSelector(Role role) {
        super(DESCRIPTOR, role);
    }

    @Override
    public boolean test(Role role) {
        return role == this.role;
    }
}
