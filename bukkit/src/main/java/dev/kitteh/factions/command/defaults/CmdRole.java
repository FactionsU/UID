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
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Permission;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdRole implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().role();
            Command.Builder<Sender> build = builder
                    .literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .commandDescription(Cloudy.desc(tl.getDescription()));

            Command.Builder<Sender> roleBuilder = build.required("member", FPlayerParser.of(FPlayerParser.Include.SAME_FACTION));

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

            manager.command(build.meta(HIDE_IN_HELP, true).handler(ctx -> help.queryCommands(Cmd.rootCommand() + " role <member>", ctx.sender())));
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
        var tl = FactionsPlugin.instance().tl().commands().role();
        if (!target.faction().equals(sender.faction())) {
            sender.sendRichMessage(tl.getWrongFaction());
            return;
        }
        if (target.role() == Role.ADMIN || targetNewRole == null || targetNewRole == Role.ADMIN || target.role().isAtLeast(sender.role()) || targetNewRole.isAtLeast(sender.role())) {
            sender.sendRichMessage(tl.getNotAllowed());
            return;
        }

        if (targetNewRole == Role.COLEADER &&
                !FactionsPlugin.instance().conf().factions().other().isAllowMultipleColeaders() &&
                !target.faction().members(Role.COLEADER).isEmpty()
        ) {
            sender.sendRichMessage(tl.getAlreadyColeader());
            return;
        }

        target.role(targetNewRole);
        if (target.asPlayer() instanceof Player player) {
            player.updateCommands();
        }

        TagResolver rolePlaceholder = Placeholder.unparsed("role", targetNewRole.translation());
        target.sendRichMessage(tl.getUpdated(), FPlayerResolver.of("player", target), rolePlaceholder);
        sender.sendRichMessage(tl.getUpdated(), FPlayerResolver.of("player", target), rolePlaceholder);
    }

    private void handleAdmin(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().role();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        FPlayer target = context.get("member");

        if (!sender.hasFaction() || sender.role() != Role.ADMIN) {
            sender.sendRichMessage(tl.getNotAdmin());
            return;
        }

        if (sender == target) {
            sender.sendRichMessage(tl.getTargetSelf());
            return;
        }

        if (sender.faction() != target.faction()) {
            sender.sendRichMessage(tl.getNotMember(), FPlayerResolver.of("player", target));
            return;
        }

        Bukkit.getServer().getPluginManager().callEvent(new FactionNewAdminEvent(target, sender.faction()));

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

        sender.faction().sendRichMessage(tl.getPromoted(),
                FPlayerResolver.of("player", sender),
                Placeholder.unparsed("target", target.name()),
                FactionResolver.of(sender.faction()));
    }
}
