package dev.kitteh.factions.perms.selector;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.perms.Selectable;
import net.kyori.adventure.text.Component;

public class UnknownSelector extends AbstractSelector {
    private final String text;

    public UnknownSelector(String text) {
        super(new BasicDescriptor("unknown", FactionsPlugin.getInstance().tl().permissions().selectors().unknown()::getDisplayName, UnknownSelector::new));
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean test(Selectable selectable, Faction faction) {
        return false;
    }

    @Override
    public String serialize() {
        return text;
    }

    @Override
    public String serializeValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Component displayValue(Faction context) {
        return Component.text(text);
    }
}
