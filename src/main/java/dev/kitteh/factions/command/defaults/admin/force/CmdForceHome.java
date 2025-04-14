package dev.kitteh.factions.command.defaults.admin.force;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FPlayerTeleportEvent;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdForceHome implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("home")
                            .commandDescription(Cloudy.desc(TL.COMMAND_AHOME_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.AHOME)))
                            .required("target", FPlayerParser.of(FPlayerParser.Include.ONLINE))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        Sender sender = context.sender();

        FPlayer target = context.get("target");

        if (target.isOnline()) {
            Faction faction = target.getFaction();
            if (faction.hasHome()) {
                Location destination = faction.getHome();
                FPlayerTeleportEvent tpEvent = new FPlayerTeleportEvent(target, destination, FPlayerTeleportEvent.Reason.AHOME);
                Bukkit.getServer().getPluginManager().callEvent(tpEvent);
                if (tpEvent.isCancelled()) {
                    return;
                }
                FactionsPlugin.getInstance().teleport(target.getPlayer(), destination).thenAccept(success -> {
                    if (success) {
                        sender.msg(TL.COMMAND_AHOME_SUCCESS, target.getName());
                        target.msg(TL.COMMAND_AHOME_TARGET);
                    }
                });

            } else {
                sender.msg(TL.COMMAND_AHOME_NOHOME, target.getName());
            }
        } else {
            sender.msg(TL.COMMAND_AHOME_OFFLINE, target.getName());
        }
    }
}
