package dev.kitteh.factions.command.defaults.admin.force;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.command.defaults.CmdConfirm;
import dev.kitteh.factions.event.FPlayerLeaveEvent;
import dev.kitteh.factions.event.FactionDisbandEvent;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.scoreboard.FTeamWrapper;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdForceDisband implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("disband")
                            .commandDescription(Cloudy.desc(TL.COMMAND_DISBAND_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.DISBAND_ANY)))
                            .required("faction", FactionParser.of(FactionParser.Include.SELF))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = context.get("faction");
        this.doIt(sender, faction, false);
    }

    private void doIt(FPlayer sender, Faction faction, boolean confirmed) {
        if (faction.isPermanent()) {
            sender.msg(TL.COMMAND_DISBAND_MARKEDPERMANENT.toString());
            return;
        }

        if (!confirmed) {
            String conf = CmdConfirm.add(sender, s -> this.doIt(s, faction, true));
            // TODO TL
            sender.sendMessage(ChatColor.YELLOW + "Are you sure you want to disband " + faction.getTag() + "? If so, run /f confirm " + conf);
            return;
        }

        FactionDisbandEvent disbandEvent = new FactionDisbandEvent(sender, faction);
        Bukkit.getServer().getPluginManager().callEvent(disbandEvent);
        if (disbandEvent.isCancelled()) {
            return;
        }

        // Send FPlayerLeaveEvent for each player in the faction
        for (FPlayer fplayer : faction.getFPlayers()) {
            Bukkit.getServer().getPluginManager().callEvent(new FPlayerLeaveEvent(fplayer, faction, FPlayerLeaveEvent.Reason.DISBAND));
        }

        // Inform all players
        for (FPlayer fplayer : FPlayers.getInstance().getOnlinePlayers()) {
            String who = sender.describeTo(fplayer);
            if (fplayer.getFaction() == faction) {
                fplayer.msg(TL.COMMAND_DISBAND_BROADCAST_YOURS, who);
            } else {
                fplayer.msg(TL.COMMAND_DISBAND_BROADCAST_NOTYOURS, who, faction.getTag(fplayer));
            }
        }
        if (FactionsPlugin.getInstance().conf().logging().isFactionDisband()) {
            FactionsPlugin.getInstance().log("The faction " + faction.getTag() + " (" + faction.getId() + ") was disbanded by " + sender.getName() + ".");
        }

        if (Econ.shouldBeUsed() && FactionsPlugin.getInstance().conf().economy().isBankEnabled()) {
            //Give all the faction's money to the disbander
            double amount = Econ.getBalance(faction);

            if (amount > 0.0) {
                Econ.transferMoney(sender, faction, sender, amount, false);
                String amountString = Econ.moneyString(amount);
                sender.msg(TL.COMMAND_DISBAND_HOLDINGS, amountString);
                FactionsPlugin.getInstance().log(sender.getName() + " has been given bank holdings of " + amountString + " from disbanding " + faction.getTag() + ".");
            }
        }

        Factions.getInstance().removeFaction(faction);
        FTeamWrapper.applyUpdates(faction);
    }
}
