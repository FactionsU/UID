package dev.kitteh.factions.permissible.selector;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.permissible.Selectable;
import dev.kitteh.factions.util.Mini;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public class AllSelector extends AbstractSelector {
    public static final String NAME = "all";
    public static final Descriptor DESCRIPTOR = new BasicDescriptor(NAME, FactionsPlugin.instance().tl().permissions().selectors().all()::getDisplayName, s -> new AllSelector()).acceptEmpty();

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
        return Mini.parse(FactionsPlugin.instance().tl().permissions().selectors().all().getDisplayValue());
    }
}
