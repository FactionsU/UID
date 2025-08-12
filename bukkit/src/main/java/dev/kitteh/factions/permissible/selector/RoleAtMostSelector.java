package dev.kitteh.factions.permissible.selector;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.permissible.Role;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public class RoleAtMostSelector extends AbstractRoleSelector {
    public static final String NAME = "role-atmost";
    public static final Descriptor DESCRIPTOR = new AbstractRoleSelector.RoleDescriptor(NAME, FactionsPlugin.instance().tl().permissions().selectors().roleAtMost()::getDisplayName, RoleAtMostSelector::new);

    public RoleAtMostSelector(Role role) {
        super(DESCRIPTOR, role);
    }

    @Override
    public boolean test(@Nullable Role role) {
        return role != null && role.isAtMost(this.role);
    }
}
