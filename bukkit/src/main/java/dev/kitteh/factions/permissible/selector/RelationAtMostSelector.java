package dev.kitteh.factions.permissible.selector;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.permissible.Relation;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public class RelationAtMostSelector extends AbstractRelationSelector {
    public static final String NAME = "relation-atmost";
    public static final Descriptor DESCRIPTOR = new RelationDescriptor(NAME, FactionsPlugin.instance().tl().permissions().selectors().relationAtMost()::getDisplayName, RelationAtMostSelector::new);

    public RelationAtMostSelector(Relation relation) {
        super(DESCRIPTOR, relation);
    }

    @Override
    public boolean test(@Nullable Relation relation) {
        return relation != null && relation != Relation.MEMBER && relation.isAtMost(this.relation);
    }
}
