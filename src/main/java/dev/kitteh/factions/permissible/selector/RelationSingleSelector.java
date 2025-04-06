package dev.kitteh.factions.permissible.selector;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.permissible.Relation;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class RelationSingleSelector extends AbstractRelationSelector {
    public static final String NAME = "relation-single";
    public static final Descriptor DESCRIPTOR = new RelationDescriptor(NAME, FactionsPlugin.getInstance().tl().permissions().selectors().relationSingle()::getDisplayName, RelationSingleSelector::new);

    public RelationSingleSelector(Relation relation) {
        super(DESCRIPTOR, relation);
    }

    @Override
    public boolean test(@Nullable Relation relation) {
        return relation == this.relation;
    }
}
