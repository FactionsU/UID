package dev.kitteh.factions.command.defaults.toggle;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.chat.ChatTarget;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdToggleChat implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(builder.literal("chat").literal("ally")
                    .commandDescription(Cloudy.desc(TL.COMMAND_TOGGLEALLIANCECHAT_DESCRIPTION))
                    .permission(builder.commandPermission()
                            .and(Cloudy.predicate(s -> FactionsPlugin.getInstance().conf().factions().chat().internalChat().isRelationChatEnabled()))
                            .and(Cloudy.hasPermission(Permission.CHAT))
                            .and(Cloudy.hasFaction()))
                    .handler(ctx -> this.handle(ctx, ChatTarget.Relation.ALLY))
            );

            manager.command(builder.literal("chat").literal("truce")
                    .commandDescription(Cloudy.desc(TL.COMMAND_TOGGLETRUCECHAT_DESCRIPTION))
                    .permission(builder.commandPermission()
                            .and(Cloudy.predicate(s -> FactionsPlugin.getInstance().conf().factions().chat().internalChat().isRelationChatEnabled()))
                            .and(Cloudy.hasPermission(Permission.CHAT))
                            .and(Cloudy.hasFaction()))
                    .handler(ctx -> this.handle(ctx, ChatTarget.Relation.TRUCE))
            );
        };
    }

    private void handle(CommandContext<Sender> context, ChatTarget target) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        TL tl;
        if (target == ChatTarget.Relation.ALLY) {
            sender.ignoreAllianceChat(!sender.ignoreAllianceChat());
            tl = sender.ignoreAllianceChat() ? TL.COMMAND_TOGGLEALLIANCECHAT_IGNORE : TL.COMMAND_TOGGLEALLIANCECHAT_UNIGNORE;
        } else {
            sender.ignoreTruceChat(!sender.ignoreTruceChat());
            tl = sender.ignoreTruceChat() ? TL.COMMAND_TOGGLETRUCECHAT_IGNORE : TL.COMMAND_TOGGLETRUCECHAT_UNIGNORE;
        }
        context.sender().msg(tl);
    }
}
