package dev.kitteh.factions.command.defaults.admin.force;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FactionNewAdminEvent;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdForceRole implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            Command.Builder<Sender> build = builder
                    .literal("role")
                    .commandDescription(Description.of(TL.COMMAND_ROLE_DESCRIPTION.toString()));

            Command.Builder<Sender> roleBuilder = build
                    .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.FORCE_ROLE)))
                    .required("member", FPlayerParser.of(FPlayerParser.Include.ALL));

            manager.command(
                    roleBuilder.literal("promote")
                            .handler(this::handlePromote)
            );

            manager.command(
                    roleBuilder.literal("demote")
                            .handler(this::handleDemote)
            );

            manager.command(
                    roleBuilder.literal("recruit")
                            .handler(ctx -> this.handleRole(ctx, Role.RECRUIT))
            );
            manager.command(
                    roleBuilder.literal("member")
                            .handler(ctx -> this.handleRole(ctx, Role.NORMAL))
            );

            manager.command(
                    roleBuilder.literal("moderator")
                            .handler(ctx -> this.handleRole(ctx, Role.MODERATOR))
            );

            manager.command(
                    roleBuilder.literal("coleader")
                            .handler(ctx -> this.handleRole(ctx, Role.COLEADER))
            );

            manager.command(
                    roleBuilder.literal("admin")
                            .handler(this::handleAdmin)
            );

            manager.command(build.meta(HIDE_IN_HELP, true).handler(ctx -> help.queryCommands("fa force role <member>", ctx.sender())));
        };
    }

    private void handleRole(CommandContext<Sender> context, Role role) {
        FPlayer target = context.get("member");

        this.handle(context.sender(), target, role);
    }

    private void handlePromote(CommandContext<Sender> context) {
        FPlayer target = context.get("member");

        this.handle(context.sender(), target, Role.getRelative(target.role(), 1));
    }

    private void handleDemote(CommandContext<Sender> context) {
        FPlayer target = context.get("member");

        this.handle(context.sender(), target, Role.getRelative(target.role(), -1));
    }

    private void handle(Sender sender, FPlayer target, Role targetNewRole) {
        if (targetNewRole == null || targetNewRole == Role.ADMIN) {
            sender.msg(TL.COMMAND_ROLE_NOT_ALLOWED);
            return;
        }

        if (targetNewRole == Role.COLEADER &&
                !FactionsPlugin.instance().conf().factions().other().isAllowMultipleColeaders() &&
                !target.faction().members(Role.COLEADER).isEmpty()
        ) {
            sender.msg(TL.COMMAND_COLEADER_ALREADY_COLEADER);
            return;
        }

        target.role(targetNewRole);
        if (target.asPlayer() instanceof Player player) {
            player.updateCommands();
        }

        target.msgLegacy(TL.COMMAND_ROLE_UPDATED, target.name(), targetNewRole.nicename);
        sender.msg(TL.COMMAND_ROLE_UPDATED, target.name(), targetNewRole.nicename);
    }

    private void handleAdmin(CommandContext<Sender> context) {
        FPlayer target = context.get("member");
        FPlayer oldAdmin = target.faction().admin();
        Faction faction = target.faction();
        if (target == oldAdmin) {
            return;
        }

        Bukkit.getServer().getPluginManager().callEvent(new FactionNewAdminEvent(target, faction));

        // promote target player, and demote existing admin
        boolean allowMultipleColeaders = FactionsPlugin.instance().conf().factions().other().isAllowMultipleColeaders();
        boolean noColeaders = faction.members(Role.COLEADER).isEmpty();
        if (oldAdmin != null) {
            oldAdmin.role((allowMultipleColeaders || noColeaders) ? Role.COLEADER : Role.MODERATOR);
            if (oldAdmin.asPlayer() instanceof Player player) {
                player.updateCommands();
            }
        }
        target.role(Role.ADMIN);
        if (target.asPlayer() instanceof Player player) {
            player.updateCommands();
        }

        // Inform all players
        faction.msgLegacy(TL.COMMAND_ADMIN_PROMOTED, "Server", target.describeToLegacy(faction), faction.describeToLegacy(faction));
        FPlayer senderMaybe = context.sender().fPlayerOrNull();
        if (senderMaybe == null || senderMaybe.faction() != faction) {
            context.sender().msg(TL.COMMAND_ADMIN_PROMOTED, "You", target.describeToLegacy(senderMaybe), faction.describeToLegacy(senderMaybe));
        }
    }
}
