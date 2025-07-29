package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.chat.ChatTarget;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdChat implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            manager.command(builder.literal("chat").literal("public")
                    .commandDescription(Cloudy.desc(TL.COMMAND_CHAT_DESCRIPTION))
                    .permission(builder.commandPermission()
                            .and(Cloudy.predicate(s ->
                                    FactionsPlugin.instance().conf().factions().chat().internalChat().isFactionMemberChatEnabled() ||
                                            FactionsPlugin.instance().conf().factions().chat().internalChat().isRelationChatEnabled()))
                            .and(Cloudy.hasPermission(Permission.CHAT))
                            .and(Cloudy.hasFaction()))
                    .handler(ctx -> this.handle(ctx, ChatTarget.PUBLIC))
            );

            Command.Builder<Sender> roleBuilder = builder.literal("chat")
                    .commandDescription(Cloudy.desc(TL.COMMAND_CHAT_DESCRIPTION))
                    .permission(builder.commandPermission()
                            .and(Cloudy.predicate(s -> FactionsPlugin.instance().conf().factions().chat().internalChat().isFactionMemberChatEnabled()))
                            .and(Cloudy.hasPermission(Permission.CHAT))
                            .and(Cloudy.hasFaction()));
            manager.command(roleBuilder.handler(ctx -> this.handle(ctx, ChatTarget.Role.ALL)));
            manager.command(roleBuilder.literal("faction").handler(ctx -> this.handle(ctx, ChatTarget.Role.ALL)));
            manager.command(roleBuilder.literal("coleader").permission(roleBuilder.commandPermission().and(Cloudy.isAtLeastRole(Role.COLEADER))).handler(ctx -> this.handle(ctx, ChatTarget.Role.COLEADER)));
            manager.command(roleBuilder.literal("mod").permission(roleBuilder.commandPermission().and(Cloudy.isAtLeastRole(Role.MODERATOR))).handler(ctx -> this.handle(ctx, ChatTarget.Role.MODERATOR)));
            manager.command(roleBuilder.literal("member").permission(roleBuilder.commandPermission().and(Cloudy.isAtLeastRole(Role.NORMAL))).handler(ctx -> this.handle(ctx, ChatTarget.Role.NORMAL)));

            Command.Builder<Sender> relationBuilder = builder.literal("chat")
                    .commandDescription(Cloudy.desc(TL.COMMAND_CHAT_DESCRIPTION))
                    .permission(builder.commandPermission()
                            .and(Cloudy.predicate(s -> FactionsPlugin.instance().conf().factions().chat().internalChat().isRelationChatEnabled()))
                            .and(Cloudy.hasPermission(Permission.CHAT))
                            .and(Cloudy.hasFaction()));
            manager.command(relationBuilder.literal("ally").handler(ctx -> this.handle(ctx, ChatTarget.Relation.ALLY)));
            manager.command(relationBuilder.literal("truce").handler(ctx -> this.handle(ctx, ChatTarget.Relation.TRUCE)));
        };
    }

    private void handle(CommandContext<Sender> context, ChatTarget target) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        sender.chatTarget(target);

        TL tl = switch (target) {
            case ChatTarget.Relation r -> r.relation() == Relation.ALLY ? TL.COMMAND_CHAT_MODE_ALLIANCE : TL.COMMAND_CHAT_MODE_TRUCE;
            case ChatTarget.Role r -> switch (r.role()) {
                case COLEADER -> TL.COMMAND_CHAT_MODE_COLEADER;
                case MODERATOR -> TL.COMMAND_CHAT_MODE_MOD;
                case NORMAL -> TL.COMMAND_CHAT_MODE_NORMAL;
                default -> TL.COMMAND_CHAT_MODE_FACTION;
            };
            default -> TL.COMMAND_CHAT_MODE_PUBLIC;
        };
        context.sender().msg(tl);
    }
}
