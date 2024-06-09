package dev.kitteh.factions.cmd;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.struct.Permission;
import dev.kitteh.factions.util.TL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.UUID;

public class CmdShowInvites extends FCommand {

    public CmdShowInvites() {
        super();
        this.aliases.add("showinvites");

        this.requirements = new CommandRequirements.Builder(Permission.SHOW_INVITES)
                .memberOnly()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
        Component component = legacy.deserialize(TL.COMMAND_SHOWINVITES_PENDING.toString()).color(NamedTextColor.GOLD);
        for (UUID id : context.faction.getInvites()) {
            FPlayer fp = FPlayers.getInstance().getById(id);
            String name = fp != null ? fp.getName() : id.toString();
            component = component.append(Component.text().color(NamedTextColor.WHITE).content(name + " ")
                    .hoverEvent(legacy.deserialize(TL.COMMAND_SHOWINVITES_CLICKTOREVOKE.format(name)).asHoverEvent())
                    .clickEvent(ClickEvent.runCommand("/" + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + " deinvite " + name))
            );
        }

        FactionsPlugin.getInstance().getAdventure().player(context.player).sendMessage(component);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_SHOWINVITES_DESCRIPTION;
    }

}
