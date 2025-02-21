package dev.kitteh.factions.command.defaults.set;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdSetOwner implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("owner")
                            .commandDescription(Cloudy.desc(TL.COMMAND_OWNER_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.predicate(s -> FactionsPlugin.getInstance().conf().factions().ownedArea().isEnabled()).and(Cloudy.hasPermission(Permission.OWNER)).and(Cloudy.hasSelfFactionPerms(PermissibleActions.OWNER))))
                            .optional("player", FPlayerParser.of(FPlayerParser.Include.SAME_FACTION))
                            .handler(this::handle)
            );
        };
    }

    // TODO: Delete this entire nightmare

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();
        Faction faction = sender.getFaction();

        boolean hasBypass = sender.isAdminBypassing();

        if (!FactionsPlugin.getInstance().conf().factions().ownedArea().isEnabled()) {
            sender.msg(TL.COMMAND_OWNER_DISABLED);
            return;
        }

        if (!hasBypass && FactionsPlugin.getInstance().conf().factions().ownedArea().getLimitPerFaction() > 0 && faction.getCountOfClaimsWithOwners() >= FactionsPlugin.getInstance().conf().factions().ownedArea().getLimitPerFaction()) {
            sender.msg(TL.COMMAND_OWNER_LIMIT, FactionsPlugin.getInstance().conf().factions().ownedArea().getLimitPerFaction());
            return;
        }

        FLocation flocation = new FLocation(player);

        Faction factionHere = Board.getInstance().getFactionAt(flocation);
        if (factionHere != faction) {
            if (!factionHere.isNormal()) {
                sender.msg(TL.COMMAND_OWNER_NOTCLAIMED);
                return;
            }
                sender.msg(TL.COMMAND_OWNER_WRONGFACTION);
                return;
        }

        FPlayer target = context.getOrDefault("player", sender);

        String playerName = target.getName();

        if (target.getFaction() != faction) {
            sender.msg(TL.COMMAND_OWNER_NOTMEMBER, playerName);
            return;
        }

        // if no player name was passed, and this claim does already have owners set, clear them
        if (context.optional("player").isEmpty() && faction.doesLocationHaveOwnersSet(flocation)) {
            faction.clearClaimOwnership(flocation);
            sender.msg(TL.COMMAND_OWNER_CLEARED);
            return;
        }

        if (faction.isPlayerInOwnerList(target, flocation)) {
            faction.removePlayerAsOwner(target, flocation);
            sender.msg(TL.COMMAND_OWNER_REMOVED, playerName);
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        if (!context.sender().payForCommand(FactionsPlugin.getInstance().conf().economy().getCostOwner(), TL.COMMAND_OWNER_TOSET, TL.COMMAND_OWNER_FORSET)) {
            return;
        }

        faction.setPlayerAsOwner(target, flocation);

        sender.msg(TL.COMMAND_OWNER_ADDED, playerName);
    }
}
