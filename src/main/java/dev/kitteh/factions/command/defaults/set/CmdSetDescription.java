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
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.StringParser;

import java.util.function.BiConsumer;

public class CmdSetDescription implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("description")
                            .commandDescription(Cloudy.desc(TL.COMMAND_DESCRIPTION_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.DESCRIPTION).and(Cloudy.isAtLeastRole(Role.MODERATOR))))
                            .required("description", StringParser.greedyStringParser())
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostDesc(), TL.COMMAND_DESCRIPTION_TOCHANGE, TL.COMMAND_DESCRIPTION_FORCHANGE)) {
            return;
        }

        // since "&" color tags seem to work even through plain old FPlayer.sendMessage() for some reason, we need to break those up
        // And replace all the % because it messes with string formatting and this is an easy way around that.
        String desc = context.get("description");
        desc = desc.replaceAll("%", "").replaceAll("(&([a-f0-9klmnor]))", "& $2");
        int limit = FactionsPlugin.instance().conf().commands().description().getMaxLength();
        if (limit > 0 && desc.length() > limit) {
            sender.msg(TL.COMMAND_DESCRIPTION_TOOLONG, String.valueOf(limit));
            return;
        }
        faction.description(desc);

        if (!FactionsPlugin.instance().conf().factions().chat().isBroadcastDescriptionChanges()) {
            sender.msg(TL.COMMAND_DESCRIPTION_CHANGED, faction.describeTo(sender));
            sender.sendMessage(faction.description());
            return;
        }

        sender.msg(TL.COMMAND_DESCRIPTION_CHANGES, faction.describeTo(sender));
        sender.sendMessage(faction.description());  // players can inject "&" or "`" or "<i>" or whatever in their description; &k is particularly interesting looking
    }
}
