package dev.kitteh.factions.command.defaults.admin.set;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.parser.standard.BooleanParser;

public class CmdSetBoom implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = Confs.tl().commands().admin().set().boom();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.SET_EXPLOSIONS)))
                            .required("faction", FactionParser.of(FactionParser.Include.SELF))
                            .optional("state", BooleanParser.booleanParser(true))
                            .handler(this::handle)
            );
        };
    }

    private void handle(org.incendo.cloud.context.CommandContext<Sender> context) {
        Sender sender = context.sender();

        Faction faction = context.get("faction");

        var tl = Confs.tl().commands().admin().set().boom();

        if (!faction.isNormal()) {
            sender.sendRichMessage(tl.getNotNormal());
            return;
        }

        faction.explosionsEnabled(context.getOrDefault("state", !faction.explosionsEnabled()));

        sender.sendRichMessage(faction.explosionsEnabled() ? tl.getSetNotDisabled() : tl.getSetDisabled(), FactionResolver.of(faction));
    }
}
