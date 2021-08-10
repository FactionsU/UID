package com.massivecraft.factions.perms.selector;

import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.Role;

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
