package dev.kitteh.factions.command.defaults.list;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.UUID;
import java.util.function.BiConsumer;

public class CmdListInvites implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("invites")
                            .commandDescription(Cloudy.desc(TL.COMMAND_SHOWINVITES_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.SHOW_INVITES).and(Cloudy.hasFaction())))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
        Component component = legacy.deserialize(TL.COMMAND_SHOWINVITES_PENDING.toString()).color(NamedTextColor.GOLD);
        for (UUID id : sender.getFaction().getInvites()) {
            FPlayer fp = FPlayers.getInstance().getById(id);
            String name = fp != null ? fp.getName() : id.toString();
            component = component.append(Component.text().color(NamedTextColor.WHITE).content(name + " ")
                    .hoverEvent(legacy.deserialize(TL.COMMAND_SHOWINVITES_CLICKTOREVOKE.format(name)).asHoverEvent())
                    .clickEvent(ClickEvent.runCommand("/" + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + " deinvite " + name))
            );
        }

        context.sender().sendMessage(component);
    }
}
