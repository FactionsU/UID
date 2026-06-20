package dev.kitteh.factions.command.defaults.admin.set;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Permission;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.IntegerParser;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdSetMaxVaults implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = FactionsPlugin.instance().tl().commands().admin().set().maxVaults();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.predicate(s -> Bukkit.getServer().getPluginManager().isPluginEnabled("PlayerVaults")).and(Cloudy.hasPermission(Permission.SETMAXVAULTS))))
                            .required("faction", FactionParser.of(FactionParser.Include.SELF))
                            .required("number", IntegerParser.integerParser(0))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().admin().set().maxVaults();
        Faction targetFaction = context.get("faction");
        int value = context.get("number");

        targetFaction.maxVaults(value);
        context.sender().sendRichMessage(tl.getSuccess(), FactionResolver.of(targetFaction), Placeholder.unparsed("value", String.valueOf(value)));
    }
}
