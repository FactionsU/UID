package dev.kitteh.factions.command.defaults.set;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.BooleanParser;

import java.util.function.BiConsumer;

public class CmdSetOpen implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> manager.command(
                builder.literal("open")
                        .commandDescription(Cloudy.desc(TL.COMMAND_OPEN_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.OPEN).and(Cloudy.isAtLeastRole(Role.MODERATOR))))
                        .optional("openstate", BooleanParser.booleanParser(true))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();
        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostOpen(), TL.COMMAND_OPEN_TOOPEN, TL.COMMAND_OPEN_FOROPEN)) {
            return;
        }

        faction.open(context.getOrDefault("openstate", !faction.open()));

        String open = faction.open() ? TL.COMMAND_OPEN_OPEN.toString() : TL.COMMAND_OPEN_CLOSED.toString();

        // Inform
        for (FPlayer fplayer : FPlayers.fPlayers().online()) {
            if (fplayer.faction() == faction) {
                fplayer.msg(TL.COMMAND_OPEN_CHANGES, sender.name(), open);
                continue;
            }
            fplayer.msg(TL.COMMAND_OPEN_CHANGED, faction.tagString(fplayer.faction()), open);
        }
    }
}
