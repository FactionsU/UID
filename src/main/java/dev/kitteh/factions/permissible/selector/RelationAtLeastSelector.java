package dev.kitteh.factions.permissible.selector;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.permissible.Relation;

public class RelationAtLeastSelector extends AbstractRelationSelector {
    public static final String NAME = "relation-atleast";
    public static final Descriptor DESCRIPTOR = new RelationDescriptor(NAME, FactionsPlugin.getInstance().tl().permissions().selectors().relationAtLeast()::getDisplayName, RelationAtLeastSelector::new);

    public RelationAtLeastSelector(Relation relation) {
        super(DESCRIPTOR, relation);
    }

    @Override
    public boolean test(Relation relation) {
        return relation != null && relation != Relation.MEMBER && relation.isAtLeast(this.relation);
    }
}
