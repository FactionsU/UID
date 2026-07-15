package dev.kitteh.factions.command.defaults.admin.force;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.event.FactionNewAdminEvent;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdForceRole implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = Confs.tl().commands().role();
            Command.Builder<Sender> build = builder
                    .literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .commandDescription(Cloudy.desc(tl.getDescription()));

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

            String forceTl = Confs.tl().commands().admin().force().getFirstAlias();

            manager.command(build.meta(HIDE_IN_HELP, true).handler(ctx -> help.queryCommands(Cmd.rootAdminCommand() + " " + forceTl + " " + tl.getFirstAlias() + " <member>", ctx.sender())));
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
        var tl = Confs.tl().commands().role();
        if (targetNewRole == null || targetNewRole == Role.ADMIN) {
            sender.sendRichMessage(tl.getNotAllowed());
            return;
        }

        if (targetNewRole == Role.COLEADER &&
                !Confs.main().factions().other().isAllowMultipleColeaders() &&
                !target.faction().members(Role.COLEADER).isEmpty()
        ) {
            sender.sendRichMessage(tl.getAlreadyColeader());
            return;
        }

        target.role(targetNewRole);
        if (target.asPlayer() instanceof Player player) {
            player.updateCommands();
        }

        var rolePlaceholder = Placeholder.unparsed("role", targetNewRole.translation());
        target.sendRichMessage(tl.getUpdated(), FPlayerResolver.of("player", target), rolePlaceholder);
        sender.sendRichMessage(tl.getUpdated(), FPlayerResolver.of("player", target), rolePlaceholder);
    }

    private void handleAdmin(CommandContext<Sender> context) {
        var tl = Confs.tl().commands().role();
        FPlayer target = context.get("member");
        FPlayer oldAdmin = target.faction().admin();
        Faction faction = target.faction();
        if (target == oldAdmin) {
            return;
        }

        Bukkit.getServer().getPluginManager().callEvent(new FactionNewAdminEvent(target, faction));

        // promote target player, and demote existing admin
        boolean allowMultipleColeaders = Confs.main().factions().other().isAllowMultipleColeaders();
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
        faction.sendRichMessage(tl.getPromoted(), Placeholder.unparsed("player", "Server"), FPlayerResolver.of("target", target), FactionResolver.of(faction));
        FPlayer senderMaybe = context.sender().fPlayerOrNull();
        if (senderMaybe == null || senderMaybe.faction() != faction) {
            context.sender().sendRichMessage(tl.getPromoted(), Placeholder.unparsed("player", "You"), FPlayerResolver.of("target", target), FactionResolver.of(faction));
        }
    }
}
