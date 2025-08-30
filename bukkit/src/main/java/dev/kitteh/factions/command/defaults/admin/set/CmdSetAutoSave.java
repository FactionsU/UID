package dev.kitteh.factions.command.defaults.admin.set;

import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.BooleanParser;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdSetAutoSave implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> manager.command(
                builder.literal("autosave")
                        .commandDescription(Cloudy.desc(TL.COMMAND_SETAUTOSAVE_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.AUTOSAVE)))
                        .required("state", BooleanParser.booleanParser(true))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        boolean state = context.get("state");
        AbstractFactionsPlugin.instance().autoSave(state);
        context.sender().msgLegacy(state ? TL.COMMAND_SETAUTOSAVE_ENABLED : TL.COMMAND_SETAUTOSAVE_DISABLED);
    }
}
