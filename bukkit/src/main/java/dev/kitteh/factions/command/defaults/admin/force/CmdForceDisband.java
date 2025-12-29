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
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdForceDisband implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().disband();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.DISBAND_ANY)))
                            .required("faction", FactionParser.of(FactionParser.Include.SELF))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        Faction faction = context.get("faction");
        this.doIt(context.sender(), faction, false);
    }

    private void doIt(Sender sender, Faction faction, boolean confirmed) {
        var tl = FactionsPlugin.instance().tl().commands().disband();

        if (!faction.isNormal()) {
            sender.sendRichMessage(tl.getDeniedSpecial(), FactionResolver.of(sender.fPlayerOrNull(), faction));
            return;
        }

        if (faction.isPermanent()) {
            sender.sendRichMessage(tl.getDeniedPermanent(), FactionResolver.of(sender.fPlayerOrNull(), faction));
            return;
        }

        if (!confirmed && sender.fPlayerOrNull() instanceof FPlayer fp) {
            String conf = CmdConfirm.add(fp, s -> this.doIt(sender, faction, true));
            sender.sendRichMessage(tl.getConfirm(), FactionResolver.of(sender.fPlayerOrNull(), faction), Placeholder.unparsed("command", conf));
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
        if (sender.fPlayerOrNull() instanceof FPlayer fp) {
            for (FPlayer fplayer : FPlayers.fPlayers().online()) {
                String message = fplayer.faction() == faction ? tl.getBroadcastYours() : tl.getBroadcastNotYours();
                fplayer.sendRichMessage(message, FactionResolver.of(fplayer, faction), FPlayerResolver.of("player", fplayer, fp));
            }
        } else {
            for (FPlayer fplayer : FPlayers.fPlayers().online()) {
                String message = fplayer.faction() == faction ? tl.getBroadcastConsoleYours() : tl.getBroadcastConsoleNotYours();
                fplayer.sendRichMessage(message, FactionResolver.of(fplayer, faction));
            }
        }

        if (FactionsPlugin.instance().conf().logging().isFactionDisband()) {
            AbstractFactionsPlugin.instance().log("The faction " + faction.tag() + " (" + faction.id() + ") was disbanded by " + nameForLog + ".");
        }

        if (Econ.shouldBeUsedWithBanks()) {
            //Give all the faction's money to the disbander
            double amount = Econ.getBalance(faction);

            if (amount > 0.0) {
                if (sender.fPlayerOrNull() instanceof FPlayer fp) {
                    Econ.transferMoney(fp, faction, fp, amount, false);
                    String amountString = Econ.moneyString(amount);
                    sender.sendRichMessage(tl.getEconHoldings(), Placeholder.unparsed("amount", amountString));
                    AbstractFactionsPlugin.instance().log(fp.name() + " has been given bank holdings of " + amountString + " from disbanding " + faction.tag() + ".");
                } else {
                    Econ.setBalance(faction, 0);
                }
            }
        }

        Factions.factions().remove(faction);
    }
}
