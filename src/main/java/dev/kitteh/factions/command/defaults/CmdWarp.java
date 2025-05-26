package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FPlayerTeleportEvent;
import dev.kitteh.factions.gui.WarpGUI;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.LazyLocation;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.WarmUpUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.StringParser;

import java.util.UUID;
import java.util.function.BiConsumer;

public class CmdWarp implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> manager.command(
                builder.literal("warp")
                        .commandDescription(Cloudy.desc(TL.COMMAND_FWARP_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.WARP).and(Cloudy.hasSelfFactionPerms(PermissibleActions.WARP).or(Cloudy.isBypass()))))
                        .optional("warp", StringParser.stringParser())
                        .optional("password", StringParser.stringParser())
                        .flag(manager.flagBuilder("faction").withComponent(FactionParser.of()))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        // TODO: check if in combat.

        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = context.flags().get("faction") instanceof Faction fac ? fac : sender.faction();

        if (!context.sender().isBypass() && !faction.hasAccess(sender, PermissibleActions.WARP, sender.lastStoodAt())) {
            sender.msg(TL.COMMAND_FWARP_NOACCESS, faction.tagLegacy(sender));
            return;
        }

        String warpName = context.getOrDefault("warp", null);
        if (warpName == null) {
            WarpGUI ui = new WarpGUI(sender, faction);
            ui.open();
        } else {
            final String passwordAttempt = context.getOrDefault("password", null);

            LazyLocation destination = faction.warp(warpName);
            if (destination != null) {
                if (!sender.adminBypass() && faction.hasWarpPassword(warpName) && !faction.isWarpPassword(warpName, passwordAttempt)) {
                    sender.msg(TL.COMMAND_FWARP_INVALID_PASSWORD);
                    return;
                }

                FPlayerTeleportEvent tpEvent = new FPlayerTeleportEvent(sender, destination.asLocation(), FPlayerTeleportEvent.Reason.WARP);
                Bukkit.getServer().getPluginManager().callEvent(tpEvent);
                if (tpEvent.isCancelled()) {
                    return;
                }
                // Check transaction AFTER password check.
                if (!transact(sender, context)) {
                    return;
                }
                final FPlayer fPlayer = sender;
                final UUID uuid = sender.asPlayer().getUniqueId();

                WarmUpUtil.process(sender, WarmUpUtil.Warmup.WARP, TL.WARMUPS_NOTIFY_TELEPORT, warpName, () -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (destination == faction.warp(warpName) && player != null) {
                        AbstractFactionsPlugin.instance().teleport(player, destination.asLocation()).thenAccept(success -> {
                            if (success) {
                                fPlayer.msg(TL.COMMAND_FWARP_WARPED, warpName);
                            }
                        });
                    }
                }, FactionsPlugin.instance().conf().commands().warp().getDelay());
            } else {
                sender.msg(TL.COMMAND_FWARP_INVALID_WARP, warpName);
            }
        }
    }

    private boolean transact(FPlayer player, CommandContext<Sender> context) {
        return player.adminBypass() || context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostWarp(), TL.COMMAND_FWARP_TOWARP, TL.COMMAND_FWARP_FORWARPING);
    }
}
