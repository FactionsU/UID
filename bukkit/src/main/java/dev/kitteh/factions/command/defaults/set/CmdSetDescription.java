package dev.kitteh.factions.command.defaults.set;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.parser.standard.StringParser;

public class CmdSetDescription implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = Confs.tl().commands().set().description();
            Command.Builder<Sender> build = builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .commandDescription(Cloudy.desc(tl.getDescription()))
                    .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.DESCRIPTION).and(Cloudy.isAtLeastRole(Role.MODERATOR))));

            manager.command(
                    build.required("description", StringParser.greedyStringParser())
                            .handler(this::handle)
            );

            manager.command(build.meta(HIDE_IN_HELP, true).handler(ctx -> help.queryCommands(Cmd.rootCommand() + " " + tl.getFirstAlias() + " <description>", ctx.sender())));
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = Confs.tl().commands().set().description();
        var econTl = Confs.tl().economy().actions();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        if (!context.sender().payForCommand(Confs.main().economy().getCostDesc(), econTl.getDescriptionTo(), econTl.getDescriptionFor())) {
            return;
        }

        String desc = context.get("description");
        int limit = Confs.main().commands().description().getMaxLength();
        if (limit > 0 && desc.length() > limit) {
            sender.sendRichMessage(tl.getToolong(), Placeholder.unparsed("limit", String.valueOf(limit)));
            return;
        }
        faction.description(desc);

        if (!Confs.main().factions().chat().isBroadcastDescriptionChanges()) {
            sender.sendRichMessage(tl.getChanged(), FactionResolver.of(faction));
            sender.sendMessage(Component.text(faction.description()));
            return;
        }

        faction.sendRichMessage(tl.getChanges(), FactionResolver.of(faction));
        sender.sendMessage(Component.text(faction.description()));
    }
}
