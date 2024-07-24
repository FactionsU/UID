package com.massivecraft.factions.perms.selector;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.Selectable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class FactionSelector extends AbstractSelector {
    public static final String NAME = "faction";
    public static final Descriptor DESCRIPTOR = new BasicDescriptor(NAME, FactionsPlugin.getInstance().tl().permissions().selectors().faction()::getDisplayName, FactionSelector::new)
            .withInstructions(FactionsPlugin.getInstance().tl().permissions().selectors().faction()::getInstructions);

    private final int id;
    private final String lastKnown;
    private static final String delimiter = "¤";

    public FactionSelector(String str) {
        super(DESCRIPTOR);
        String[] split = str.split(" "); // Old mistake
        if (split.length == 2) {
            Faction faction = Factions.getInstance().getFactionById(Integer.parseInt(split[0]));
            this.id = Integer.parseInt(split[0]);
            this.lastKnown = faction == null ? split[1] : faction.getTag();
            return;
        }
        split = str.split(delimiter);
        if (split.length == 1) {
            Faction faction = Factions.getInstance().getByTag(str);
            this.id = faction.getIntId();
            this.lastKnown = faction.getTag();
        } else {
            Faction faction = Factions.getInstance().getFactionById(Integer.parseInt(split[0]));
            this.id = Integer.parseInt(split[0]);
            this.lastKnown = faction == null ? split[1] : faction.getTag();
        }
    }

    @Override
    public boolean test(Selectable selectable, Faction faction) {
        Faction fac = null;
        if (selectable instanceof Faction) {
            fac = (Faction) selectable;
        } else if (selectable instanceof FPlayer) {
            fac = ((FPlayer) selectable).getFaction();
        }
        return fac != null && fac.getIntId() == this.id;
    }

    @Override
    public String serializeValue() {
        Faction faction = Factions.getInstance().getFactionById(this.id);
        return this.id + delimiter + (faction == null ? this.lastKnown : faction.getTag());
    }

    @Override
    public Component displayValue(Faction context) {
        Faction faction = Factions.getInstance().getFactionById(this.id);
        return faction == null ?
                MiniMessage.miniMessage().deserialize(FactionsPlugin.getInstance().tl().permissions().selectors().faction().getDisbandedValue(), Placeholder.unparsed("lastknown", this.lastKnown)) :
                LegacyComponentSerializer.legacySection().deserialize(faction.getTag(context));
    }
}
