package dev.kitteh.factions.command.defaults.set;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.util.Permission;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.SuggestionProvider;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdSetDefaultRole implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = FactionsPlugin.instance().tl().commands().set().defaultRole();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.DEFAULTRANK).and(Cloudy.isAtLeastRole(Role.ADMIN))))
                            .required("role", StringParser.stringParser(), SuggestionProvider.suggestingStrings(Role.COLEADER.getRoleNamesAtOrBelow()))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().set().defaultRole();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        String roleString = context.get("role");
        Role target = Role.fromString(roleString.toUpperCase());
        if (target == null) {
            sender.sendRichMessage(tl.getInvalidRole(), Placeholder.unparsed("role", roleString));
            return;
        }

        if (target == Role.ADMIN) {
            sender.sendRichMessage(tl.getNotThatRole());
            return;
        }

        sender.faction().defaultRole(target);
        sender.sendRichMessage(tl.getSuccess(), Placeholder.unparsed("role", target.nicename));
    }
}
