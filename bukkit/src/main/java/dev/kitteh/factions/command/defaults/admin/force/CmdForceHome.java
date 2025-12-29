package dev.kitteh.factions.command.defaults.admin.force;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FPlayerTeleportEvent;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.util.Permission;
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
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().admin().force().home();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.AHOME)))
                            .required("target", FPlayerParser.of(FPlayerParser.Include.ONLINE))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().admin().force().home();

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
                        sender.sendRichMessage(tl.getSuccess(), FPlayerResolver.of("player", sender.fPlayerOrNull(), target));
                        target.sendRichMessage(tl.getSuccessNotice());
                    }
                });

            } else {
                sender.sendRichMessage(tl.getNoHome(), FPlayerResolver.of("player", sender.fPlayerOrNull(), target));
            }
        } else {
            sender.sendRichMessage(tl.getOffline(), FPlayerResolver.of("player", sender.fPlayerOrNull(), target));
        }
    }
}
