package dev.kitteh.factions.command.defaults.set;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.parser.standard.BooleanParser;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdSetBoom implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> manager.command(
                builder.literal("explosions")
                        .commandDescription(Cloudy.desc(TL.COMMAND_BOOM_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.NO_BOOM).and(Cloudy.predicate(s-> s.hasFaction() && s.fPlayerOrNull().faction().isPeaceful())).and(Cloudy.isAtLeastRole(Role.MODERATOR))))
                        .optional("state", BooleanParser.booleanParser(true))
                        .handler(this::handle)
        );
    }

    private void handle(org.incendo.cloud.context.CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        if (!faction.isPeaceful()) {
            sender.msgLegacy(TL.COMMAND_BOOM_PEACEFULONLY);
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostNoBoom(), TL.COMMAND_BOOM_TOTOGGLE, TL.COMMAND_BOOM_FORTOGGLE)) {
            return;
        }

        faction.peacefulExplosionsEnabled(context.getOrDefault("state", !faction.peacefulExplosionsEnabled()));

        String enabled = !faction.peacefulExplosionsEnabled() ? TL.GENERIC_DISABLED.toString() : TL.GENERIC_ENABLED.toString();

        faction.msgLegacy(TL.COMMAND_BOOM_ENABLED, sender.describeToLegacy(faction), enabled);
    }
}
