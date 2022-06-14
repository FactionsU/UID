package com.massivecraft.factions.perms.selector;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.Selectable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Optional;
import java.util.UUID;

public class PlayerSelector extends AbstractSelector {
    public static final String NAME = "player";
    public static final Descriptor DESCRIPTOR = new BasicDescriptor(NAME, FactionsPlugin.getInstance().tl().permissions().selectors().player()::getDisplayName, PlayerSelector::new)
            .withInstructions(FactionsPlugin.getInstance().tl().permissions().selectors().player()::getInstructions);

    private final UUID uuid;

    public PlayerSelector(String str) {
        super(DESCRIPTOR);
        UUID uuid = null;
        try {
            uuid = UUID.fromString(str);
        } catch (IllegalArgumentException e) {
            Optional<FPlayer> p = FPlayers.getInstance().getAllFPlayers().stream().filter(pp -> pp.getName().equalsIgnoreCase(str)).findFirst();
            if (p.isPresent()) {
                uuid = UUID.fromString(p.get().getId());
            }
        }
        if (uuid == null) {
            throw new IllegalArgumentException("Unknown player name " + str);
        }
        this.uuid = uuid;
    }

    public PlayerSelector(UUID uuid) {
        super(DESCRIPTOR);
        if (uuid == null) {
            throw new IllegalArgumentException("Null UUID");
        }
        this.uuid = uuid;
    }

    @Override
    public boolean test(Selectable selectable, Faction faction) {
        return selectable instanceof FPlayer && ((FPlayer) selectable).getId().equals(this.uuid.toString());
    }

    @Override
    public String serializeValue() {
        return uuid.toString();
    }

    @Override
    public Component displayValue(Faction context) {
        FPlayer player = FPlayers.getInstance().getById(this.uuid.toString());
        return player == null ?
                MiniMessage.miniMessage().deserialize(FactionsPlugin.getInstance().tl().permissions().selectors().player().getUuidValue(), Placeholder.unparsed("uuid", this.uuid.toString()))
                : LegacyComponentSerializer.legacySection().deserialize(player.getRelationTo(context).getColor() + player.getName());
    }
}
