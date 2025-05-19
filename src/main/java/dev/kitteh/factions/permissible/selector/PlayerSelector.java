package dev.kitteh.factions.permissible.selector;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.permissible.Selectable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@NullMarked
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
            Optional<FPlayer> p = FPlayers.fPlayers().all().stream().filter(pp -> pp.name().equalsIgnoreCase(str)).findFirst();
            if (p.isPresent()) {
                uuid = p.get().uniqueId();
            }
        }
        if (uuid == null) {
            throw new IllegalArgumentException("Unknown player name " + str);
        }
        this.uuid = uuid;
    }

    public PlayerSelector(UUID uuid) {
        super(DESCRIPTOR);
        this.uuid = Objects.requireNonNull(uuid);
    }

    @Override
    public boolean test(Selectable selectable, Faction faction) {
        return selectable instanceof FPlayer && ((FPlayer) selectable).uniqueId().equals(this.uuid);
    }

    @Override
    public String serializeValue() {
        return uuid.toString();
    }

    @Override
    public Component displayValue(Faction context) {
        FPlayer player = FPlayers.fPlayers().get(this.uuid);
        return player.name().equals(player.uniqueId().toString()) ?
                MiniMessage.miniMessage().deserialize(FactionsPlugin.getInstance().tl().permissions().selectors().player().getUuidValue(), Placeholder.unparsed("uuid", this.uuid.toString()))
                : LegacyComponentSerializer.legacySection().deserialize(player.relationTo(context).chatColor() + player.name());
    }
}
