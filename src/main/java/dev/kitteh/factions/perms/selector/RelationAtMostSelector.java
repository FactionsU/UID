package dev.kitteh.factions.perms.selector;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.perms.Relation;

public class RelationAtMostSelector extends AbstractRelationSelector {
    public static final String NAME = "relation-atmost";
    public static final Descriptor DESCRIPTOR = new RelationDescriptor(NAME, FactionsPlugin.getInstance().tl().permissions().selectors().relationAtMost()::getDisplayName, RelationAtMostSelector::new);

    public RelationAtMostSelector(Relation relation) {
        super(DESCRIPTOR, relation);
    }

    @Override
    public boolean test(Relation relation) {
        return relation != null && relation != Relation.MEMBER && relation.isAtMost(this.relation);
    }
}
