package dev.kitteh.factions.perms.selector;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.perms.Relation;

public class RelationSingleSelector extends AbstractRelationSelector {
    public static final String NAME = "relation-single";
    public static final Descriptor DESCRIPTOR = new RelationDescriptor(NAME, FactionsPlugin.getInstance().tl().permissions().selectors().relationSingle()::getDisplayName, RelationSingleSelector::new);

    public RelationSingleSelector(Relation relation) {
        super(DESCRIPTOR, relation);
    }

    @Override
    public boolean test(Relation relation) {
        return relation == this.relation;
    }
}
