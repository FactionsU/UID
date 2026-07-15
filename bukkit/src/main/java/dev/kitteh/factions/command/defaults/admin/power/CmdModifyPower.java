package dev.kitteh.factions.command.defaults.admin.power;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.parser.standard.DoubleParser;

public class CmdModifyPower implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = Confs.tl().commands().admin().power().modifyPower();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.MODIFY_POWER)))
                            .required("player", FPlayerParser.of(FPlayerParser.Include.ALL))
                            .required("change", DoubleParser.doubleParser())
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = Confs.tl().commands().admin().power().modifyPower();
        FPlayer player = context.get("player");
        Double number = context.get("change");

        player.alterPower(number);
        int newPower = player.powerRounded();
        context.sender().sendRichMessage(tl.getAdded(),
                Placeholder.unparsed("change", String.valueOf(number)),
                FPlayerResolver.of("player", player),
                Placeholder.unparsed("power", String.valueOf(newPower)));
    }
}
