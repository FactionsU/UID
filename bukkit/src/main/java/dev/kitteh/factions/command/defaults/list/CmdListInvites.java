package dev.kitteh.factions.command.defaults.list;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.util.MiscUtil;
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
import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdListInvites implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().list().invites();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
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
        for (UUID id : sender.faction().invites()) {
            FPlayer fp = FPlayers.fPlayers().get(id);
            String name = fp.name();
            component = component.append(Component.text().color(NamedTextColor.WHITE).content(name + " ")
                    .hoverEvent(legacy.deserialize(TL.COMMAND_SHOWINVITES_CLICKTOREVOKE.format(name)).asHoverEvent())
                    .clickEvent(ClickEvent.runCommand("/" + MiscUtil.commandRoot() + " deinvite " + name))
            );
        }

        context.sender().sendMessage(component);
    }
}
