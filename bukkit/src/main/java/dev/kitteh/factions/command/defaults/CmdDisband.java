package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FPlayerLeaveEvent;
import dev.kitteh.factions.event.FactionDisbandEvent;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Permission;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdDisband implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().disband();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.DISBAND).and(Cloudy.hasSelfFactionPerms(PermissibleActions.DISBAND))))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        this.doIt(sender, false);
    }

    private void doIt(FPlayer sender, boolean confirmed) {
        var tl = FactionsPlugin.instance().tl().commands().disband();

        Faction faction = sender.faction();

        if (!faction.isNormal()) {
            sender.sendRichMessage(tl.getDeniedSpecial(), FactionResolver.of(sender, faction));
            return;
        }

        if (!faction.hasAccess(sender, PermissibleActions.DISBAND, sender.lastStoodAt())) {
            sender.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getActionGeneric(),
                    FactionResolver.of(sender, faction),
                    Placeholder.unparsed("action", PermissibleActions.DISBAND.shortDescription())
                    );
            return;
        }

        if (faction.isPermanent()) {
            sender.sendRichMessage(tl.getDeniedPermanent(), FactionResolver.of(sender, faction));
            return;
        }

        if (!FactionsPlugin.instance().landRaidControl().canDisbandFaction(faction, sender)) {
            return;
        }

        if (!confirmed) {
            String conf = CmdConfirm.add(sender, s -> this.doIt(s, true));
            sender.sendRichMessage(tl.getConfirm(), FactionResolver.of(sender, faction), Placeholder.unparsed("command", conf));
            return;
        }

        FactionDisbandEvent disbandEvent = new FactionDisbandEvent(sender, faction);
        Bukkit.getServer().getPluginManager().callEvent(disbandEvent);
        if (disbandEvent.isCancelled()) {
            return;
        }

        // Send FPlayerLeaveEvent for each player in the faction
        for (FPlayer fplayer : faction.members()) {
            Bukkit.getServer().getPluginManager().callEvent(new FPlayerLeaveEvent(fplayer, faction, FPlayerLeaveEvent.Reason.DISBAND));
        }

        // Inform all players
        for (FPlayer fplayer : FPlayers.fPlayers().online()) {
            String message = fplayer.faction() == faction ? tl.getBroadcastYours() : tl.getBroadcastNotYours();
            fplayer.sendRichMessage(message, FactionResolver.of(fplayer, faction), FPlayerResolver.of("player", fplayer, sender));
        }
        if (FactionsPlugin.instance().conf().logging().isFactionDisband()) {
            AbstractFactionsPlugin.instance().log("The faction " + faction.tag() + " (" + faction.id() + ") was disbanded by " + sender.name() + ".");
        }

        if (Econ.shouldBeUsedWithBanks()) {
            //Give all the faction's money to the disbander
            double amount = Econ.getBalance(faction);

            if (amount > 0.0) {
                Econ.transferMoney(sender, faction, sender, amount, false);
                String amountString = Econ.moneyString(amount);
                sender.sendRichMessage(tl.getEconHoldings(), Placeholder.unparsed("amount", amountString));
                AbstractFactionsPlugin.instance().log(sender.name() + " has been given bank holdings of " + amountString + " from disbanding " + faction.tag() + ".");
            }
        }

        Factions.factions().remove(faction);
    }
}
