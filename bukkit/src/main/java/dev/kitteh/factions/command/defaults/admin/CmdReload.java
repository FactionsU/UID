package dev.kitteh.factions.command.defaults.admin;

import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdReload implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = Confs.tl().commands().admin().reload();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.RELOAD)))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        long timeInitStart = System.currentTimeMillis();
        AbstractFactionsPlugin.instance().configManager().loadConfigs();

        context.sender().sendRichMessage(Confs.tl().commands().admin().reload().getSuccess(),
                Placeholder.parsed("millis", String.valueOf(System.currentTimeMillis() - timeInitStart)));
    }
}
