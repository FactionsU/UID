package com.massivecraft.factions.perms.selector;

import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.Role;

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
