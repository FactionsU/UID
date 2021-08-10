package com.massivecraft.factions.perms.selector;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.data.MemoryFaction;
import com.massivecraft.factions.iface.RelationParticipator;
import com.massivecraft.factions.perms.PermSelector;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.perms.Selectable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractRelationSelector extends AbstractSelector {
    public static class RelationDescriptor extends BasicDescriptor {
        private List<PermSelector> relationSelectors;
        private final Function<Relation, PermSelector> function;

        public RelationDescriptor(String name, Supplier<String> displayName, Function<Relation, PermSelector> function) {
            super(name, displayName, input -> function.apply(Relation.fromString(input)));
            this.function = function;
        }

        @Override
        public Map<String, String> getOptions(Faction faction) {
            List<PermSelector> available = new ArrayList<>(relationSelectors == null ? (relationSelectors = Arrays.stream(Relation.values()).map(function).collect(Collectors.toList())) : relationSelectors);
            available.removeAll(((MemoryFaction) faction).getPermissions().keySet());

            Map<String, String> map = new LinkedHashMap<>();

            for (PermSelector selector : available) {
                map.put(selector.serialize(), ((AbstractRelationSelector) selector).getRelation().getTranslation());
            }

            return map;
        }
    }

    protected final Relation relation;

    public AbstractRelationSelector(Descriptor descriptor, Relation relation) {
        super(descriptor);
        this.relation = relation;
    }

    public Relation getRelation() {
        return relation;
    }

    @Override
    public String serializeValue() {
        return this.relation.name();
    }

    @Override
    public Component displayValue(Faction context) {
        return LegacyComponentSerializer.legacySection().deserialize(this.relation.getColor() + this.relation.getTranslation());
    }

    @Override
    public final boolean test(Selectable selectable, Faction faction) {
        Relation relation = null;
        if (selectable instanceof RelationParticipator) {
            relation = ((RelationParticipator) selectable).getRelationTo(faction);
        } else if (selectable instanceof Relation) {
            relation = (Relation) selectable;
        }
        return test(relation);
    }

    public abstract boolean test(Relation relation);
}
