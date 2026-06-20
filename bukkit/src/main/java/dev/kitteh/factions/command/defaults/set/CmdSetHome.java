package dev.kitteh.factions.command.defaults.set;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FactionSetHomeEvent;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.util.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdSetHome implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = FactionsPlugin.instance().tl().commands().set().home();
            manager.command(builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .commandDescription(Cloudy.desc(tl.getDescription()))
                    .permission(builder.commandPermission().and(
                            Cloudy.predicate(_ -> FactionsPlugin.instance().conf().factions().homes().isEnabled())
                                    .and(Cloudy.hasPermission(Permission.SETHOME))
                                    .and(Cloudy.hasSelfFactionPerms(PermissibleActions.SETHOME))
                    ))
                    .flag(
                            manager.flagBuilder("delete")
                                    .withPermission(
                                            Cloudy.predicate(_ -> FactionsPlugin.instance().conf().factions().homes().isEnabled())
                                                    .and(Cloudy.hasPermission(Permission.DELHOME))
                                                    .and(Cloudy.hasSelfFactionPerms(PermissibleActions.SETHOME))
                                    )
                    )
                    .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().set().home();
        var econTl = FactionsPlugin.instance().tl().economy().actions();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();
        Faction faction = sender.faction();

        if (context.flags().hasFlag("delete")) {
            this.handleDel(context, sender, faction);
            return;
        }

        if (FactionsPlugin.instance().conf().factions().homes().isMustBeInClaimedTerritory() &&
                Board.board().factionAt(new FLocation(player)) != faction) {
            sender.sendRichMessage(tl.getNotClaimed());
            return;
        }

        if (!context.sender().canAffordCommand(FactionsPlugin.instance().conf().economy().getCostSethome(), econTl.getSetHomeTo())) {
            return;
        }

        FactionSetHomeEvent setHomeEvent = new FactionSetHomeEvent(sender, player.getLocation());
        Bukkit.getServer().getPluginManager().callEvent(setHomeEvent);
        if (setHomeEvent.isCancelled()) {
            return;
        }

        if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostSethome(), econTl.getSetHomeTo(), econTl.getSetHomeFor())) {
            return;
        }

        faction.home(player.getLocation());
        faction.sendRichMessage(tl.getSet(), FPlayerResolver.of("player", sender));
    }

    private void handleDel(CommandContext<Sender> context, FPlayer sender, Faction faction) {
        var tl = FactionsPlugin.instance().tl().commands().set().home();
        var econTl = FactionsPlugin.instance().tl().economy().actions();

        if (!faction.hasHome()) {
            sender.sendRichMessage(tl.getNoHome());
            return;
        }

        if (FactionsPlugin.instance().conf().factions().homes().isRequiredToHaveHomeBeforeSettingWarps() && !faction.warps().isEmpty()) {
            sender.sendRichMessage(tl.getWarpsRemain());
            return;
        }

        if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostDelhome(), econTl.getDelHomeTo(), econTl.getDelHomeFor())) {
            return;
        }

        faction.removeHome();
        faction.sendRichMessage(tl.getDel(), FPlayerResolver.of("player", sender));
    }
}
