package dev.kitteh.factions.command.defaults.admin.dtr;

import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.landraidcontrol.DTRControl;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdDTRResetAll implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("reset-all")
                            .commandDescription(Cloudy.desc(TL.COMMAND_DTR_MODIFY_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.MODIFY_DTR)))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        DTRControl dtr = (DTRControl) FactionsPlugin.getInstance().getLandRaidControl();
        Factions.factions().all().forEach(target -> target.dtr(dtr.getMaxDTR(target)));
        context.sender().msg(TL.COMMAND_DTR_MODIFY_DONE, "EVERYONE", "MAX");
    }
}
