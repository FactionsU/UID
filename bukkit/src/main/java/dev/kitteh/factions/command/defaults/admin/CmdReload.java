package dev.kitteh.factions.command.defaults.admin;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdReload implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> manager.command(
                builder.literal("reload")
                        .commandDescription(Cloudy.desc(TL.COMMAND_RELOAD_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.RELOAD)))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        long timeInitStart = System.currentTimeMillis();
        FactionsPlugin.instance().configManager().loadConfigs();
        AbstractFactionsPlugin.instance().loadLang();
        long timeReload = (System.currentTimeMillis() - timeInitStart);

        context.sender().msg(TL.COMMAND_RELOAD_TIME, timeReload);
    }
}
