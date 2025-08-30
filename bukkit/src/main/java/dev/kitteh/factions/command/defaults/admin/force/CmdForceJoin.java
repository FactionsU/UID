package dev.kitteh.factions.command.defaults.admin.force;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FPlayerJoinEvent;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdForceJoin implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            Command.Builder<Sender> build = builder.literal("join")
                    .commandDescription(Cloudy.desc(TL.COMMAND_JOIN_DESCRIPTION))
                    .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.FORCE_JOIN)));

            manager.command(
                    build
                            .required("player", FPlayerParser.of(FPlayerParser.Include.ONLINE, FPlayerParser.Include.ALL))
                            .required("faction", FactionParser.of())
                            .handler(this::handle)
            );

            manager.command(build.meta(HIDE_IN_HELP, true).handler(ctx -> help.queryCommands("fa force join <player> <faction>", ctx.sender())));
        };
    }

    private void handle(CommandContext<Sender> context) {
        Sender sender = context.sender();
        FPlayer target = context.get("player");
        Faction faction = context.get("faction");

        if (!faction.isNormal()) {
            sender.msgLegacy(TL.COMMAND_JOIN_SYSTEMFACTION);
            return;
        }

        if (target.hasFaction()) {
            sender.msgLegacy(TL.COMMAND_FORCE_JOIN_INOTHERFACTION);
            return;
        }

        // trigger the join event (cancellable)
        FPlayerJoinEvent joinEvent = new FPlayerJoinEvent(target, faction, FPlayerJoinEvent.Reason.COMMAND_FORCE);
        Bukkit.getServer().getPluginManager().callEvent(joinEvent);
        if (joinEvent.isCancelled()) {
            return;
        }

        sender.msgLegacy(TL.COMMAND_FORCE_JOIN_SUCCESS, target.describeToLegacy(faction, true), faction.tagLegacy(target));

        faction.msgLegacy(TL.COMMAND_JOIN_JOINED, target.describeToLegacy(faction, true));

        target.resetFactionData();
        target.faction(faction);
        faction.deInvite(target);
        target.role(faction.defaultRole());
        if (target.asPlayer() instanceof Player p) {
            p.updateCommands();
        }

        if (FactionsPlugin.instance().conf().logging().isFactionJoin()) {
            AbstractFactionsPlugin.instance().log(TL.COMMAND_JOIN_JOINEDLOG.toString(), target.name(), faction.tag());
        }
    }
}
