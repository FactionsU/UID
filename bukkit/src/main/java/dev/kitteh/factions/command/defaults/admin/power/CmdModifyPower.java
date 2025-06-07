package dev.kitteh.factions.command.defaults.admin.power;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.DoubleParser;

import java.util.function.BiConsumer;

public class CmdModifyPower implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> manager.command(
                builder.literal("modify")
                        .commandDescription(Cloudy.desc(TL.COMMAND_MODIFYPOWER_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.MODIFY_POWER)))
                        .required("player", FPlayerParser.of(FPlayerParser.Include.ALL))
                        .required("change", DoubleParser.doubleParser())
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer player = context.get("player");
        Double number = context.get("change");

        player.alterPower(number);
        int newPower = player.powerRounded(); // int so we don't have super long doubles.
        context.sender().msg(TL.COMMAND_MODIFYPOWER_ADDED, number, player.name(), newPower);
    }
}
