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
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdSetHome implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(builder.literal("home")
                    .commandDescription(Cloudy.desc(TL.COMMAND_SETHOME_DESCRIPTION))
                    .permission(builder.commandPermission().and(
                            Cloudy.predicate(s -> FactionsPlugin.getInstance().conf().factions().homes().isEnabled())
                                    .and(Cloudy.hasPermission(Permission.SETHOME))
                                    .and(Cloudy.hasSelfFactionPerms(PermissibleActions.SETHOME))
                    ))
                    .flag(
                            manager.flagBuilder("delete")
                                    .withPermission(
                                            Cloudy.predicate(s -> FactionsPlugin.getInstance().conf().factions().homes().isEnabled())
                                                    .and(Cloudy.hasPermission(Permission.DELHOME))
                                                    .and(Cloudy.hasSelfFactionPerms(PermissibleActions.SETHOME))
                                    )
                    )
                    .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();
        Faction faction = sender.getFaction();

        if (context.flags().hasFlag("delete")) {
            this.handleDel(context, sender, faction);
            return;
        }

        // Can the player set the faction home HERE?
        if (FactionsPlugin.getInstance().conf().factions().homes().isMustBeInClaimedTerritory() &&
                Board.board().getFactionAt(new FLocation(player)) != faction) {
            sender.msg(TL.COMMAND_SETHOME_NOTCLAIMED);
            return;
        }

        if (!context.sender().canAffordCommand(FactionsPlugin.getInstance().conf().economy().getCostSethome(), TL.COMMAND_SETHOME_TOSET)) {
            return;
        }

        FactionSetHomeEvent setHomeEvent = new FactionSetHomeEvent(sender, player.getLocation());
        Bukkit.getServer().getPluginManager().callEvent(setHomeEvent);
        if (setHomeEvent.isCancelled()) {
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        if (!context.sender().payForCommand(FactionsPlugin.getInstance().conf().economy().getCostSethome(), TL.COMMAND_SETHOME_TOSET, TL.COMMAND_SETHOME_FORSET)) {
            return;
        }

        faction.setHome(player.getLocation());

        faction.msg(TL.COMMAND_SETHOME_SET, sender.describeTo(faction, true));
    }

    private void handleDel(CommandContext<Sender> context, FPlayer sender, Faction faction) {
        if (!faction.hasHome()) {
            sender.msg(TL.COMMAND_HOME_NOHOME);
            return;
        }

        if (FactionsPlugin.getInstance().conf().factions().homes().isRequiredToHaveHomeBeforeSettingWarps() && !faction.getWarps().isEmpty()) {
            sender.msg(TL.COMMAND_HOME_WARPSREMAIN);
            return;
        }

        if (!context.sender().payForCommand(FactionsPlugin.getInstance().conf().economy().getCostDelhome(), TL.COMMAND_DELHOME_TOSET, TL.COMMAND_DELHOME_FORSET)) {
            return;
        }

        faction.delHome();

        faction.msg(TL.COMMAND_DELHOME_DEL, sender.describeTo(faction, true));
    }
}
