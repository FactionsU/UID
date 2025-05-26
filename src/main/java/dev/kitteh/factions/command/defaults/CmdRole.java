package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FactionNewAdminEvent;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;

import java.util.function.BiConsumer;

public class CmdRole implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            Command.Builder<Sender> roleBuilder = builder
                    .literal("role")
                    .commandDescription(Description.of(TL.COMMAND_ROLE_DESCRIPTION.toString()))
                    .required("member", FPlayerParser.of(FPlayerParser.Include.SAME_FACTION));

            manager.command(
                    roleBuilder.literal("promote")
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.PROMOTE).and(Cloudy.hasSelfFactionPerms(PermissibleActions.PROMOTE))))
                            .handler(this::handlePromote)
            );

            manager.command(
                    roleBuilder.literal("demote")
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.PROMOTE).and(Cloudy.hasSelfFactionPerms(PermissibleActions.PROMOTE))))
                            .handler(this::handleDemote)
            );

            manager.command(
                    roleBuilder.literal("recruit")
                            .permission(builder.commandPermission().and(Cloudy.isAtLeastRole(Role.NORMAL).and(Cloudy.hasPermission(Permission.PROMOTE)).and(Cloudy.hasSelfFactionPerms(PermissibleActions.PROMOTE))))
                            .handler(ctx -> this.handleRole(ctx, Role.RECRUIT))
            );
            manager.command(
                    roleBuilder.literal("member")
                            .permission(builder.commandPermission().and(Cloudy.isAtLeastRole(Role.MODERATOR).and(Cloudy.hasPermission(Permission.PROMOTE)).and(Cloudy.hasSelfFactionPerms(PermissibleActions.PROMOTE))))
                            .handler(ctx -> this.handleRole(ctx, Role.NORMAL))
            );

            manager.command(
                    roleBuilder.literal("moderator")
                            .permission(builder.commandPermission().and(Cloudy.isAtLeastRole(Role.COLEADER).and(Cloudy.hasPermission(Permission.PROMOTE)).and(Cloudy.hasSelfFactionPerms(PermissibleActions.PROMOTE))))
                            .handler(ctx -> this.handleRole(ctx, Role.MODERATOR))
            );

            manager.command(
                    roleBuilder.literal("coleader")
                            .permission(builder.commandPermission().and(Cloudy.isAtLeastRole(Role.ADMIN).and(Cloudy.hasPermission(Permission.PROMOTE)).and(Cloudy.hasSelfFactionPerms(PermissibleActions.PROMOTE))))
                            .handler(ctx -> this.handleRole(ctx, Role.COLEADER))
            );

            manager.command(
                    roleBuilder.literal("admin")
                            .permission(builder.commandPermission().and(Cloudy.isAtLeastRole(Role.ADMIN).and(Cloudy.hasPermission(Permission.ADMIN))))
                            .handler(this::handleAdmin)
            );

        };
    }

    private void handleRole(CommandContext<Sender> context, Role role) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        FPlayer target = context.get("member");

        this.handle(sender, target, role);
    }

    private void handlePromote(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        FPlayer target = context.get("member");

        this.handle(sender, target, Role.getRelative(target.role(), 1));
    }

    private void handleDemote(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        FPlayer target = context.get("member");

        this.handle(sender, target, Role.getRelative(target.role(), -1));
    }

    private void handle(FPlayer sender, FPlayer target, Role targetNewRole) {
        if (!target.faction().equals(sender.faction())) {
            sender.msgLegacy(TL.COMMAND_ROLE_WRONGFACTION);
            return;
        }
        if (targetNewRole == null || targetNewRole == Role.ADMIN || targetNewRole.isAtLeast(sender.role())) {
            sender.msgLegacy(TL.COMMAND_ROLE_NOT_ALLOWED);
            return;
        }

        if (targetNewRole == Role.COLEADER &&
                !FactionsPlugin.instance().conf().factions().other().isAllowMultipleColeaders() &&
                !target.faction().members(Role.COLEADER).isEmpty()
        ) {
            sender.msgLegacy(TL.COMMAND_COLEADER_ALREADY_COLEADER);
            return;
        }

        target.role(targetNewRole);
        if (target.asPlayer() instanceof Player player) {
            player.updateCommands();
        }

        target.msgLegacy(TL.COMMAND_ROLE_UPDATED, target.name(), targetNewRole.nicename);
        sender.msgLegacy(TL.COMMAND_ROLE_UPDATED, target.name(), targetNewRole.nicename);
    }

    private void handleAdmin(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        FPlayer target = context.get("member");

        if (!sender.hasFaction() || sender.role() != Role.ADMIN) {
            sender.msgLegacy(TL.COMMAND_ADMIN_NOTADMIN);
            return;
        }

        if (sender == target) {
            sender.msgLegacy(TL.COMMAND_ADMIN_TARGETSELF);
            return;
        }

        if (sender.faction() != target.faction()) {
            sender.msgLegacy(TL.COMMAND_ADMIN_NOTMEMBER);
            return;
        }

        Bukkit.getServer().getPluginManager().callEvent(new FactionNewAdminEvent(target, sender.faction()));

        // promote target player, and demote existing admin
        boolean allowMultipleColeaders = FactionsPlugin.instance().conf().factions().other().isAllowMultipleColeaders();
        boolean noColeaders = sender.faction().members(Role.COLEADER).isEmpty();
        sender.role((allowMultipleColeaders || noColeaders) ? Role.COLEADER : Role.MODERATOR);
        target.role(Role.ADMIN);
        if (sender.asPlayer() instanceof Player player) {
            player.updateCommands();
        }
        if (target.asPlayer() instanceof Player player) {
            player.updateCommands();
        }

        // Inform all players
        sender.faction().msgLegacy(TL.COMMAND_ADMIN_PROMOTED, sender.describeToLegacy(target), target.describeToLegacy(sender), sender.faction().describeToLegacy(sender));
    }
}
