package dev.kitteh.factions.permissible.selector;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.permissible.Selectable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public class FactionSelector extends AbstractSelector {
    public static final String NAME = "faction";
    public static final Descriptor DESCRIPTOR = new BasicDescriptor(NAME, FactionsPlugin.instance().tl().permissions().selectors().faction()::getDisplayName, FactionSelector::new)
            .withInstructions(FactionsPlugin.instance().tl().permissions().selectors().faction()::getInstructions);

    private final int id;
    private final String lastKnown;
    private static final String delimiter = "¤";

    public FactionSelector(String str) {
        super(DESCRIPTOR);
        String[] split = str.split(" "); // Old mistake
        if (split.length == 2) {
            Faction faction = Factions.factions().get(Integer.parseInt(split[0]));
            this.id = Integer.parseInt(split[0]);
            this.lastKnown = faction == null ? split[1] : faction.tag();
            return;
        }
        split = str.split(delimiter);
        if (split.length == 1) {
            Faction faction = Factions.factions().get(str);
            this.id = faction.id();
            this.lastKnown = faction.tag();
        } else {
            Faction faction = Factions.factions().get(Integer.parseInt(split[0]));
            this.id = Integer.parseInt(split[0]);
            this.lastKnown = faction == null ? split[1] : faction.tag();
        }
    }

    @Override
    public boolean test(Selectable selectable, Faction faction) {
        Faction fac = null;
        if (selectable instanceof Faction) {
            fac = (Faction) selectable;
        } else if (selectable instanceof FPlayer) {
            fac = ((FPlayer) selectable).faction();
        }
        return fac != null && fac.id() == this.id;
    }

    @Override
    public String serializeValue() {
        Faction faction = Factions.factions().get(this.id);
        return this.id + delimiter + (faction == null ? this.lastKnown : faction.tag());
    }

    @Override
    public Component displayValue(Faction context) {
        Faction faction = Factions.factions().get(this.id);
        return faction == null ?
                MiniMessage.miniMessage().deserialize(FactionsPlugin.instance().tl().permissions().selectors().faction().getDisbandedValue(), Placeholder.unparsed("lastknown", this.lastKnown)) :
                LegacyComponentSerializer.legacySection().deserialize(faction.tagString(context));
    }

    @Override
    public int hashCode() {
        return Objects.hash("factionselector", this.id);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FactionSelector fs && fs.id == this.id;
    }
}
