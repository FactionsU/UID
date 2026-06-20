package dev.kitteh.factions.command.defaults.toggle;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.chat.ChatTarget;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.util.Permission;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdToggleChat implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().chat();
            manager.command(builder.literal("chat").literal("ally")
                    .commandDescription(Cloudy.desc(tl.getAllianceChatDescription()))
                    .permission(builder.commandPermission()
                            .and(Cloudy.predicate(s -> FactionsPlugin.instance().conf().factions().chat().internalChat().isRelationChatEnabled()))
                            .and(Cloudy.hasPermission(Permission.CHAT))
                            .and(Cloudy.hasFaction()))
                    .handler(ctx -> this.handle(ctx, ChatTarget.Relation.ALLY))
            );

            manager.command(builder.literal("chat").literal("truce")
                    .commandDescription(Cloudy.desc(tl.getTruceChatDescription()))
                    .permission(builder.commandPermission()
                            .and(Cloudy.predicate(s -> FactionsPlugin.instance().conf().factions().chat().internalChat().isRelationChatEnabled()))
                            .and(Cloudy.hasPermission(Permission.CHAT))
                            .and(Cloudy.hasFaction()))
                    .handler(ctx -> this.handle(ctx, ChatTarget.Relation.TRUCE))
            );
        };
    }

    private void handle(CommandContext<Sender> context, ChatTarget target) {
        var tl = FactionsPlugin.instance().tl().commands().chat();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        String message;
        if (target == ChatTarget.Relation.ALLY) {
            sender.ignoreAllianceChat(!sender.ignoreAllianceChat());
            message = sender.ignoreAllianceChat() ? tl.getAllianceChatIgnore() : tl.getAllianceChatUnignore();
        } else {
            sender.ignoreTruceChat(!sender.ignoreTruceChat());
            message = sender.ignoreTruceChat() ? tl.getTruceChatIgnore() : tl.getTruceChatUnignore();
        }
        context.sender().sendRichMessage(message);
    }
}
