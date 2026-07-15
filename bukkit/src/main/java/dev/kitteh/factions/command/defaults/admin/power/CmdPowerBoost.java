package dev.kitteh.factions.command.defaults.admin.power;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.parser.standard.DoubleParser;

public class CmdPowerBoost implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = Confs.tl().commands().admin().power().powerBoost();
            Command.Builder<Sender> boostBuilder = builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .commandDescription(Cloudy.desc(tl.getDescription()))
                    .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.POWERBOOST)));

            Command.Builder<Sender> boostSetBuilder = boostBuilder.literal(tl.getSubCmdSet());
            Command.Builder<Sender> boostModifyBuilder = boostBuilder.literal(tl.getSubCmdModify());

            manager.command(
                    boostSetBuilder.literal(tl.getSubCmdFaction())
                            .required("faction", FactionParser.of(FactionParser.Include.SELF))
                            .required("value", DoubleParser.doubleParser())
                            .handler(ctx -> this.handleFaction(ctx, false))
            );
            manager.command(
                    boostSetBuilder.literal(tl.getSubCmdPlayer())
                            .required("player", FPlayerParser.of(FPlayerParser.Include.ALL))
                            .required("value", DoubleParser.doubleParser())
                            .handler(ctx -> this.handlePlayer(ctx, false))
            );

            manager.command(
                    boostModifyBuilder.literal(tl.getSubCmdFaction())
                            .required("faction", FactionParser.of(FactionParser.Include.SELF))
                            .required("value", DoubleParser.doubleParser())
                            .handler(ctx -> this.handleFaction(ctx, true))
            );
            manager.command(
                    boostModifyBuilder.literal(tl.getSubCmdPlayer())
                            .required("player", FPlayerParser.of(FPlayerParser.Include.ALL))
                            .required("value", DoubleParser.doubleParser())
                            .handler(ctx -> this.handlePlayer(ctx, true))
            );
        };
    }

    private void handlePlayer(CommandContext<Sender> context, boolean modify) {
        var tl = Confs.tl().commands().admin().power().powerBoost();
        FPlayer target = context.get("player");
        double value = context.get("value");

        if (modify) {
            value += target.powerBoost();
        }

        target.powerBoost(value);

        context.sender().sendRichMessage(tl.getBoost(), FPlayerResolver.of("target", target), Placeholder.unparsed("value", String.valueOf(Math.round(value))));
    }

    private void handleFaction(CommandContext<Sender> context, boolean modify) {
        var tl = Confs.tl().commands().admin().power().powerBoost();
        Faction target = context.get("faction");
        double value = context.get("value");

        if (modify) {
            value += target.powerBoost();
        }

        target.powerBoost(value);

        context.sender().sendRichMessage(tl.getBoost(), FactionResolver.of("target", target), Placeholder.unparsed("value", String.valueOf(Math.round(value))));
    }
}
