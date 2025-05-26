package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FPlayerJoinEvent;
import dev.kitteh.factions.event.FactionAttemptCreateEvent;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.MiscUtil;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.Bukkit;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.StringParser;

import java.util.List;
import java.util.function.BiConsumer;

public class CmdCreate implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> manager.command(
                builder.literal("create")
                        .commandDescription(Cloudy.desc(TL.COMMAND_CREATE_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.CREATE).and(Cloudy.isPlayer())))
                        .required("tag", StringParser.stringParser())
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        String tag = context.get("tag");

        if (sender.hasFaction()) {
            sender.msgLegacy(TL.COMMAND_CREATE_MUSTLEAVE);
            return;
        }

        if (Factions.factions().get(tag) != null) {
            sender.msgLegacy(TL.COMMAND_CREATE_INUSE);
            return;
        }

        List<String> tagValidationErrors = MiscUtil.validateTag(tag);
        if (!tagValidationErrors.isEmpty()) {
            sender.sendMessageLegacy(tagValidationErrors);
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make sure they can pay
        if (!context.sender().canAffordCommand(FactionsPlugin.instance().conf().economy().getCostCreate(), TL.COMMAND_CREATE_TOCREATE)) {
            return;
        }

        FactionAttemptCreateEvent attemptEvent = new FactionAttemptCreateEvent(sender, tag);
        Bukkit.getServer().getPluginManager().callEvent(attemptEvent);
        if (attemptEvent.isCancelled()) {
            return;
        }

        // then make 'em pay (if applicable)
        if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostCreate(), TL.COMMAND_CREATE_TOCREATE, TL.COMMAND_CREATE_FORCREATE)) {
            return;
        }

        Faction faction = Factions.factions().create(sender, tag);

        // trigger the faction join event for the creator
        FPlayerJoinEvent joinEvent = new FPlayerJoinEvent(sender, faction, FPlayerJoinEvent.Reason.CREATE);
        Bukkit.getServer().getPluginManager().callEvent(joinEvent);
        // join event cannot be cancelled or you'll have an empty faction

        // finish setting up the FPlayer
        sender.role(Role.ADMIN);
        sender.faction(faction);
        sender.asPlayer().updateCommands();

        for (FPlayer follower : FPlayers.fPlayers().online()) {
            follower.msgLegacy(TL.COMMAND_CREATE_CREATED, sender.describeToLegacy(follower, true), faction.tagLegacy(follower));
        }

        if (FactionsPlugin.instance().conf().logging().isFactionCreate()) {
            AbstractFactionsPlugin.instance().log(sender.name() + TL.COMMAND_CREATE_CREATEDLOG + tag);
        }
    }
}
