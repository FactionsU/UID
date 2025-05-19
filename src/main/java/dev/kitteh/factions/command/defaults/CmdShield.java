package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdShield implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            Command.Builder<Sender> shield = builder.literal("shield")
                    .commandDescription(Cloudy.desc(TL.COMMAND_SHIELD_DESCRIPTION))
                    .permission(builder.commandPermission().and(Cloudy.predicate(s -> false)).and(Cloudy.hasPermission(Permission.SHIELD).and(Cloudy.isPlayer())));

            manager.command(shield.handler(ctx -> this.handle(ctx, true)));
            manager.command(shield.literal("status").handler(ctx -> this.handle(ctx, false)));
        };
    }

    private void handle(CommandContext<Sender> context, boolean exec) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        if (exec && sender.hasFaction() && sender.faction().hasAccess(sender, PermissibleActions.SHIELD, sender.lastStoodAt())) {
            // TODO menu
        }

        // TODO status

        //sender.msg(TL.COMMAND_LINK_SHOW, FactionsPlugin.getInstance().conf().colors().relations().getMember(), sender.getFaction().getLink());
    }
}
