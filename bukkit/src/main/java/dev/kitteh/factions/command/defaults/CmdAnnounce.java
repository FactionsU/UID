package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.ChatColor;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.StringParser;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdAnnounce implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> manager.command(
                builder.literal("announce")
                        .commandDescription(Cloudy.desc(TL.COMMAND_ANNOUNCE_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.ANNOUNCE).and(Cloudy.isAtLeastRole(Role.MODERATOR))))
                        .required("message", StringParser.greedyStringParser())
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        String prefix = ChatColor.GREEN + faction.tag() + ChatColor.YELLOW + " [" + ChatColor.GRAY + sender.name() + ChatColor.YELLOW + "] " + ChatColor.RESET;
        String message = context.get("message");

        faction.sendMessageLegacy(prefix + message);

        // Add for offline players.
        for (FPlayer fp : faction.membersOnline(false)) {
            faction.addAnnouncement(fp, prefix + message);
        }
    }
}
