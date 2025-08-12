package dev.kitteh.factions.permissible.selector;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Participator;
import dev.kitteh.factions.permissible.PermSelector;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.permissible.Selectable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public abstract class AbstractRelationSelector extends AbstractSelector {
    public static class RelationDescriptor extends BasicDescriptor {
        private @Nullable List<PermSelector> relationSelectors;
        private final Function<Relation, PermSelector> function;

        public RelationDescriptor(String name, Supplier<String> displayName, Function<Relation, PermSelector> function) {
            super(name, displayName, input -> function.apply(Relation.fromString(input)));
            this.function = function;
        }

        @Override
        public Map<String, String> options(Faction faction) {
            List<PermSelector> available = new ArrayList<>(relationSelectors == null ? (relationSelectors = Arrays.stream(Relation.values()).map(function).collect(Collectors.toList())) : relationSelectors);
            available.removeAll(faction.permissions().selectors());

            Map<String, String> map = new LinkedHashMap<>();

            for (PermSelector selector : available) {
                map.put(selector.serialize(), ((AbstractRelationSelector) selector).relation().translation());
            }

            return map;
        }
    }

    protected final Relation relation;

    public AbstractRelationSelector(Descriptor descriptor, Relation relation) {
        super(descriptor);
        this.relation = relation;
    }

    public Relation relation() {
        return relation;
    }

    @Override
    public String serializeValue() {
        return this.relation.name();
    }

    @Override
    public Component displayValue(Faction context) {
        return LegacyComponentSerializer.legacySection().deserialize(this.relation.translation()).color(this.relation.color());
    }

    @Override
    public final boolean test(Selectable selectable, Faction faction) {
        Relation relation = null;
        if (selectable instanceof Participator) {
            relation = ((Participator) selectable).relationTo(faction);
        } else if (selectable instanceof Relation) {
            relation = (Relation) selectable;
        }
        return test(relation);
    }

    public abstract boolean test(@Nullable Relation relation);
}
