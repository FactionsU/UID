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
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.scoreboard.FTeamWrapper;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.Bukkit;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdForceDisband implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> manager.command(
                builder.literal("disband")
                        .commandDescription(Cloudy.desc(TL.COMMAND_DISBAND_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.DISBAND_ANY)))
                        .required("faction", FactionParser.of(FactionParser.Include.SELF))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        Faction faction = context.get("faction");
        this.doIt(context.sender(), faction, false);
    }

    private void doIt(Sender sender, Faction faction, boolean confirmed) {
        if (!faction.isNormal()) {
            sender.msg(TL.COMMAND_DISBAND_IMMUTABLE);
            return;
        }

        if (faction.isPermanent()) {
            sender.msg(TL.COMMAND_DISBAND_MARKEDPERMANENT);
            return;
        }

        if (!confirmed && sender.fPlayerOrNull() instanceof FPlayer fp) {
            String conf = CmdConfirm.add(fp, s -> this.doIt(sender, faction, true));
            sender.msg(TL.COMMAND_DISBAND_CONFIRM, faction.tag(), conf);
            return;
        }

        FactionDisbandEvent disbandEvent = new FactionDisbandEvent(sender.fPlayerOrNull(), faction);
        Bukkit.getServer().getPluginManager().callEvent(disbandEvent);
        if (disbandEvent.isCancelled()) {
            return;
        }

        // Send FPlayerLeaveEvent for each player in the faction
        for (FPlayer fplayer : faction.members()) {
            Bukkit.getServer().getPluginManager().callEvent(new FPlayerLeaveEvent(fplayer, faction, FPlayerLeaveEvent.Reason.DISBAND));
        }

        String nameForLog = sender.fPlayerOrNull() instanceof FPlayer fp ? fp.name() : "Console";

        // Inform all players
        for (FPlayer fplayer : FPlayers.fPlayers().online()) {
            String who = sender.fPlayerOrNull() instanceof FPlayer fp ? fp.describeToLegacy(fplayer): TL.GENERIC_SERVERADMIN.toString();
            if (fplayer.faction() == faction) {
                fplayer.msgLegacy(TL.COMMAND_DISBAND_BROADCAST_YOURS, who);
            } else {
                fplayer.msgLegacy(TL.COMMAND_DISBAND_BROADCAST_NOTYOURS, who, faction.tagLegacy(fplayer));
            }
        }
        if (FactionsPlugin.instance().conf().logging().isFactionDisband()) {
            AbstractFactionsPlugin.instance().log("The faction " + faction.tag() + " (" + faction.id() + ") was disbanded by " + nameForLog + ".");
        }

        if (Econ.shouldBeUsed() && FactionsPlugin.instance().conf().economy().isBankEnabled()) {
            //Give all the faction's money to the disbander
            double amount = Econ.getBalance(faction);

            if (amount > 0.0) {
                if (sender.fPlayerOrNull() instanceof FPlayer fp) {
                    Econ.transferMoney(fp, faction, fp, amount, false);
                    String amountString = Econ.moneyString(amount);
                    fp.msgLegacy(TL.COMMAND_DISBAND_HOLDINGS, amountString);
                    AbstractFactionsPlugin.instance().log(fp.name() + " has been given bank holdings of " + amountString + " from disbanding " + faction.tag() + ".");
                } else {
                    Econ.setBalance(faction, 0);
                }
            }
        }

        Factions.factions().remove(faction);
        FTeamWrapper.applyUpdates(faction);
    }
}
