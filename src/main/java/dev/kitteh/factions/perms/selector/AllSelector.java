package dev.kitteh.factions.perms.selector;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.perms.Selectable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class AllSelector extends AbstractSelector {
    public static final String NAME = "all";
    public static final Descriptor DESCRIPTOR = new BasicDescriptor(NAME, FactionsPlugin.getInstance().tl().permissions().selectors().all()::getDisplayName, s -> new AllSelector()).acceptEmpty();

    public AllSelector() {
        super(DESCRIPTOR);
    }

    @Override
    public boolean test(Selectable selectable, Faction faction) {
        return true;
    }

    @Override
    public String serializeValue() {
        return NAME;
    }

    @Override
    public Component displayValue(Faction context) {
        return MiniMessage.miniMessage().deserialize(FactionsPlugin.getInstance().tl().permissions().selectors().all().getDisplayValue());
    }
}
