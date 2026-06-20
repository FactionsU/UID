package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.Participator;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdClear implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().clear();
            Command.Builder<Sender> build = builder
                    .literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .permission(builder.commandPermission().and(
                            Cloudy.hasFaction().and(
                                    org.incendo.cloud.permission.Permission.anyOf(
                                            Cloudy.hasPermission(Permission.BAN).and(Cloudy.hasSelfFactionPerms(PermissibleActions.BAN)),
                                            Cloudy.hasPermission(Permission.UNCLAIM_ALL).and(Cloudy.isAtLeastRole(Role.ADMIN)),
                                            Cloudy.hasPermission(Permission.SETWARP).and(Cloudy.hasSelfFactionPerms(PermissibleActions.SETWARP)),
                                            Cloudy.hasPermission(Permission.DEINVITE).and(Cloudy.hasSelfFactionPerms(PermissibleActions.INVITE))
                                    )
                            )
                    ));

            manager.command(
                    build.literal(tl.getSubCmdBans())
                            .commandDescription(Description.of(tl.getDescriptionBans()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.BAN).and(Cloudy.hasSelfFactionPerms(PermissibleActions.BAN))))
                            .handler(this::handleBan)
            );

            manager.command(
                    build.literal(tl.getSubCmdClaims())
                            .commandDescription(Description.of(tl.getDescriptionClaims()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.UNCLAIM_ALL).and(Cloudy.isAtLeastRole(Role.ADMIN))))
                            .handler(this::handleUnclaim)
            );

            manager.command(
                    build.literal(tl.getSubCmdWarps())
                            .commandDescription(Description.of(tl.getDescriptionWarps()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.SETWARP).and(Cloudy.hasSelfFactionPerms(PermissibleActions.SETWARP))))
                            .handler(this::handleWarp)
            );

            manager.command(
                    build.literal(tl.getSubCmdInvites())
                            .commandDescription(Description.of(tl.getDescriptionInvites()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.DEINVITE).and(Cloudy.hasSelfFactionPerms(PermissibleActions.INVITE))))
                            .handler(this::handleInvite)
            );

            manager.command(build.meta(HIDE_IN_HELP, true).handler(ctx -> help.queryCommands(Cmd.rootCommand() + " " + tl.getFirstAlias() + " *", ctx.sender())));
        };
    }

    private void handleBan(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().clear();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        String conf = CmdConfirm.add(sender, this::handleBanConf);
        sender.sendRichMessage(tl.getBansClearConfirm(), Placeholder.unparsed("command", conf));
    }

    private void handleBanConf(FPlayer sender) {
        var tl = FactionsPlugin.instance().tl().commands().clear();
        if (sender.asPlayer() instanceof Player p && p.hasPermission(Permission.BAN.node) && sender.faction().hasAccess(sender, PermissibleActions.BAN, sender.lastStoodAt())) {
            sender.faction().bans().clear();
            sender.sendRichMessage(tl.getBansClearSuccess());
        }
    }

    private void handleUnclaim(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        CmdUnclaim.unclaimAll(sender, sender.faction(), false);
    }

    private void handleWarp(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().clear();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        String conf = CmdConfirm.add(sender, this::handleWarpConf);
        sender.sendRichMessage(tl.getWarpsClearConfirm(), Placeholder.unparsed("command", conf));
    }

    private void handleWarpConf(FPlayer sender) {
        var tl = FactionsPlugin.instance().tl().commands().clear();
        var econTl = FactionsPlugin.instance().tl().economy().actions();
        if (sender.asPlayer() instanceof Player p && p.hasPermission(Permission.SETWARP.node) && sender.faction().hasAccess(sender, PermissibleActions.SETWARP, sender.lastStoodAt())) {
            double cost = FactionsPlugin.instance().conf().economy().getCostDelWarp();
            if (cost > 0D && Econ.shouldBeUsed()) {
                Participator purchaser;
                if (FactionsPlugin.instance().conf().economy().isBankEnabled() && FactionsPlugin.instance().conf().economy().isBankFactionPaysCosts()) {
                    purchaser = sender.faction();
                } else {
                    purchaser = sender;
                }

                double fullCost = sender.faction().warps().size() * cost;

                if (!Econ.modifyMoney(purchaser, -fullCost, econTl.getDelWarpTo(), econTl.getDelWarpFor())) {
                    return;
                }
            }
            sender.faction().warps().clear();
            sender.sendRichMessage(tl.getWarpsClearSuccess());
        }
    }

    private void handleInvite(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().clear();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        String conf = CmdConfirm.add(sender, this::handleInviteConf);
        sender.sendRichMessage(tl.getInvitesClearConfirm(), Placeholder.unparsed("command", conf));
    }

    private void handleInviteConf(FPlayer sender) {
        var tl = FactionsPlugin.instance().tl().commands().clear();
        if (sender.asPlayer() instanceof Player p && p.hasPermission(Permission.DEINVITE.node) && sender.faction().hasAccess(sender, PermissibleActions.INVITE, sender.lastStoodAt())) {
            sender.faction().invites().clear();
            sender.sendRichMessage(tl.getInvitesClearSuccess());
        }
    }
}
