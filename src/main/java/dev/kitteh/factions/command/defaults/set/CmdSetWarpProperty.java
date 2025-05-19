package dev.kitteh.factions.command.defaults.set;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.parser.standard.StringParser;

import java.util.function.BiConsumer;

public class CmdSetWarpProperty implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> manager.command(
                builder.literal("warp-property")
                        .commandDescription(Cloudy.desc(TL.COMMAND_SETFWARPPROPERTY_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.SETWARP).and(Cloudy.hasSelfFactionPerms(PermissibleActions.SETWARP))))
                        .required("name", StringParser.stringParser())
                        .flag(manager.flagBuilder("password").withComponent(StringParser.stringParser()))
                        .flag(manager.flagBuilder("remove-password"))// TODO more flags, translatable password string
                        .handler(this::handle)
        );
    }

    private void handle(org.incendo.cloud.context.CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        String warp = context.get("name");
        if (faction.warp(warp) == null) {
            sender.msg(TL.COMMAND_SETFWARPPROPERTY_NOWARP, warp);
            return;
        }

        if (context.flags().hasFlag("remove-password")) {
            faction.removeWarpPassword(warp);
            sender.msg(TL.COMMAND_SETFWARPPROPERTY_REMOVEPASSWORD, warp);
        }

        String password = context.flags().get("password");

        if (password != null) {
            faction.setWarpPassword(warp, password);
            sender.msg(TL.COMMAND_SETFWARPPROPERTY_SETPASSWORD, warp);
        }
    }
}
