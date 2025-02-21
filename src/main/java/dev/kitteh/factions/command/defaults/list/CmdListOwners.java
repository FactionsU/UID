package dev.kitteh.factions.command.defaults.list;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdListOwners implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("owner")
                            .commandDescription(Cloudy.desc(TL.COMMAND_OWNERLIST_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.predicate(s->FactionsPlugin.getInstance().conf().factions().ownedArea().isEnabled()).and(Cloudy.hasPermission(Permission.OWNERLIST).and(Cloudy.hasFaction()))))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        if (!FactionsPlugin.getInstance().conf().factions().ownedArea().isEnabled()) {
            sender.msg(TL.COMMAND_OWNERLIST_DISABLED);
            return;
        }

        FLocation flocation = new FLocation(((Sender.Player) context.sender()).player());

        Faction faction = sender.getFaction();

        if (Board.getInstance().getFactionAt(flocation) != faction) {
            if (!sender.isAdminBypassing()) {
                sender.msg(TL.COMMAND_OWNERLIST_WRONGFACTION);
                return;
            }
            faction = Board.getInstance().getFactionAt(flocation);
            if (!faction.isNormal()) {
                sender.msg(TL.COMMAND_OWNERLIST_NOTCLAIMED);
                return;
            }
        }

        String owners = faction.getOwnerListString(flocation);

        if (owners.isEmpty()) {
            sender.msg(TL.COMMAND_OWNERLIST_NONE);
            return;
        }

        sender.msg(TL.COMMAND_OWNERLIST_OWNERS, owners);
    }
}
