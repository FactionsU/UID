package dev.kitteh.factions.command.defaults.admin.set;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
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
        return (manager, builder, help) -> manager.command(
                builder.literal("max-vaults")
                        .commandDescription(Cloudy.desc(TL.COMMAND_SETMAXVAULTS_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.predicate(s -> Bukkit.getServer().getPluginManager().isPluginEnabled("PlayerVaults")).and(Cloudy.hasPermission(Permission.SETMAXVAULTS))))
                        .required("faction", FactionParser.of(FactionParser.Include.SELF))
                        .required("number", IntegerParser.integerParser(0))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        Faction targetFaction = context.get("faction");
        int value = context.get("number");

        targetFaction.maxVaults(value);
        context.sender().sender().sendMessage(TL.COMMAND_SETMAXVAULTS_SUCCESS.format(targetFaction.tag(), value));
    }
}
