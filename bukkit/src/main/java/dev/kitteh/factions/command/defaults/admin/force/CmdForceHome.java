package dev.kitteh.factions.command.defaults.admin.force;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FPlayerTeleportEvent;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdForceHome implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> manager.command(
                builder.literal("home")
                        .commandDescription(Cloudy.desc(TL.COMMAND_AHOME_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.AHOME)))
                        .required("target", FPlayerParser.of(FPlayerParser.Include.ONLINE))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        Sender sender = context.sender();

        FPlayer target = context.get("target");

        if (target.isOnline()) {
            Faction faction = target.faction();
            if (faction.hasHome()) {
                Location destination = faction.home();
                FPlayerTeleportEvent tpEvent = new FPlayerTeleportEvent(target, destination, FPlayerTeleportEvent.Reason.FORCED_HOME);
                Bukkit.getServer().getPluginManager().callEvent(tpEvent);
                if (tpEvent.isCancelled()) {
                    return;
                }
                AbstractFactionsPlugin.instance().teleport(target.asPlayer(), destination).thenAccept(success -> {
                    if (success) {
                        sender.msg(TL.COMMAND_AHOME_SUCCESS, target.name());
                        target.msgLegacy(TL.COMMAND_AHOME_TARGET);
                    }
                });

            } else {
                sender.msg(TL.COMMAND_AHOME_NOHOME, target.name());
            }
        } else {
            sender.msg(TL.COMMAND_AHOME_OFFLINE, target.name());
        }
    }
}
