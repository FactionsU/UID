package com.massivecraft.factions.perms.selector;

import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.Relation;

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
