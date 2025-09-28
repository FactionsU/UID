package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FPlayerJoinEvent;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.Bukkit;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdJoin implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            Command.Builder<Sender> build = builder.literal("join")
                    .commandDescription(Cloudy.desc(TL.COMMAND_JOIN_DESCRIPTION))
                    .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.JOIN).and(Cloudy.isPlayer())));

            manager.command(
                    build.required("faction", FactionParser.of())
                            .handler(this::handle)
            );

            manager.command(build.meta(HIDE_IN_HELP, true).handler(ctx -> help.queryCommands("f join <faction>", ctx.sender())));
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        Faction faction = context.get("faction");

        if (!faction.isNormal()) {
            sender.msgLegacy(TL.COMMAND_JOIN_SYSTEMFACTION);
            return;
        }

        if (faction == sender.faction()) {
            sender.msgLegacy(TL.COMMAND_JOIN_ALREADYMEMBERFIXED, faction.tagLegacy(sender));
            return;
        }

        int max = faction.memberLimit();
        if (faction.size() >= max) {
            sender.msgLegacy(TL.COMMAND_JOIN_ATLIMIT, faction.tagLegacy(sender), max, sender.describeToLegacy(sender, false));
            return;
        }

        if (sender.hasFaction()) {
            sender.msgLegacy(TL.COMMAND_JOIN_INOTHERFACTIONFIXED);
            return;
        }

        if (!FactionsPlugin.instance().landRaidControl().canJoinFaction(faction, sender)) {
            return;
        }

        if (!(faction.open() || faction.hasInvite(sender))) {
            sender.msgLegacy(TL.COMMAND_JOIN_REQUIRESINVITATION);
            if (!faction.isBanned(sender)) {
                faction.msgLegacy(TL.COMMAND_JOIN_ATTEMPTEDJOIN, sender.describeToLegacy(faction, true));
            }
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make sure they can pay
        if (!context.sender().canAffordCommand(FactionsPlugin.instance().conf().economy().getCostJoin(), TL.COMMAND_JOIN_TOJOIN)) {
            return;
        }

        // Check for ban
        if (faction.isBanned(sender)) {
            sender.msgLegacy(TL.COMMAND_JOIN_BANNED, faction.tagLegacy(sender));
            return;
        }

        // trigger the join event (cancellable)
        FPlayerJoinEvent joinEvent = new FPlayerJoinEvent(sender, faction, FPlayerJoinEvent.Reason.COMMAND);
        Bukkit.getServer().getPluginManager().callEvent(joinEvent);
        if (joinEvent.isCancelled()) {
            return;
        }

        // then make 'em pay (if applicable)
        if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostJoin(), TL.COMMAND_JOIN_TOJOIN, TL.COMMAND_JOIN_FORJOIN)) {
            return;
        }

        sender.msgLegacy(TL.COMMAND_JOIN_SUCCESS, sender.describeToLegacy(sender, true), faction.tagLegacy(sender));

        faction.msgLegacy(TL.COMMAND_JOIN_JOINED, sender.describeToLegacy(faction, true));

        sender.resetFactionData();
        sender.faction(faction);
        faction.deInvite(sender);
        sender.role(faction.defaultRole());
        sender.asPlayer().updateCommands();

        if (FactionsPlugin.instance().conf().logging().isFactionJoin()) {
            AbstractFactionsPlugin.instance().log(TL.COMMAND_JOIN_JOINEDLOG.toString(), sender.name(), faction.tag());
        }
    }
}
