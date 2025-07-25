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
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.TriConsumer;
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
            Command.Builder<Sender> build = builder
                    .literal("clear")
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
                    build.literal("bans")
                            .commandDescription(Description.of(TL.COMMAND_CLEAR_DESCRIPTION_BANS.toString()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.BAN).and(Cloudy.hasSelfFactionPerms(PermissibleActions.BAN))))
                            .handler(this::handleBan)
            );

            manager.command(
                    build.literal("claims")
                            .commandDescription(Description.of(TL.COMMAND_CLEAR_DESCRIPTION_CLAIMS.toString()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.UNCLAIM_ALL).and(Cloudy.isAtLeastRole(Role.ADMIN))))
                            .handler(this::handleUnclaim)
            );

            manager.command(
                    build.literal("warps")
                            .commandDescription(Description.of(TL.COMMAND_CLEAR_DESCRIPTION_WARPS.toString()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.SETWARP).and(Cloudy.hasSelfFactionPerms(PermissibleActions.SETWARP))))
                            .handler(this::handleWarp)
            );

            manager.command(
                    build.literal("invites")
                            .commandDescription(Description.of(TL.COMMAND_CLEAR_DESCRIPTION_INVITES.toString()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.DEINVITE).and(Cloudy.hasSelfFactionPerms(PermissibleActions.INVITE))))
                            .handler(this::handleInvite)
            );

            manager.command(build.meta(HIDE_IN_HELP, true).handler(ctx -> help.queryCommands("f clear *", ctx.sender())));
        };
    }

    private void handleBan(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        String conf = CmdConfirm.add(sender, this::handleBanConf);
        sender.msgLegacy(TL.COMMAND_UNBAN_CLEAR_CONFIRM, conf);
    }

    private void handleBanConf(FPlayer sender) {
        if (sender.asPlayer() instanceof Player p && p.hasPermission(Permission.BAN.node) && sender.faction().hasAccess(sender, PermissibleActions.BAN, sender.lastStoodAt())) {
            sender.faction().bans().clear();
            sender.msgLegacy(TL.COMMAND_UNBAN_CLEAR_SUCCESS);
        }
    }

    private void handleUnclaim(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        CmdUnclaim.unclaimAll(sender, sender.faction(), false);
    }

    private void handleWarp(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        String conf = CmdConfirm.add(sender, this::handleWarpConf);
        sender.msgLegacy(TL.COMMAND_DELFWARP_CLEAR_CONFIRM, conf);
    }

    private void handleWarpConf(FPlayer sender) {
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

                if (!Econ.modifyMoney(purchaser, -fullCost, TL.COMMAND_DELFWARP_TODELETE.toString(), TL.COMMAND_DELFWARP_FORDELETE.toString())) {
                    return;
                }
            }
            sender.faction().warps().clear();
            sender.msgLegacy(TL.COMMAND_DELFWARP_CLEAR_SUCCESS);
        }
    }

    private void handleInvite(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        String conf = CmdConfirm.add(sender, this::handleInviteConf);
        sender.msgLegacy(TL.COMMAND_INVITE_CLEAR_CONFIRM, conf);
    }

    private void handleInviteConf(FPlayer sender) {
        if (sender.asPlayer() instanceof Player p && p.hasPermission(Permission.DEINVITE.node) && sender.faction().hasAccess(sender, PermissibleActions.INVITE, sender.lastStoodAt())) {
            sender.faction().invites().clear();
            sender.msgLegacy(TL.COMMAND_INVITE_CLEAR_SUCCESS);
        }
    }
}
