package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FPlayerTeleportEvent;
import dev.kitteh.factions.integration.Essentials;
import dev.kitteh.factions.integration.IntegrationManager;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.SmokeUtil;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.WarmUpUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class CmdHome implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("home")
                            .commandDescription(Cloudy.desc(TL.COMMAND_HOME_DESCRIPTION))
                            .permission(builder.commandPermission().and(
                                    Cloudy.hasPermission(Permission.HOME)
                                            .and(Cloudy.hasFaction())
                                            .and(Cloudy.predicate(s -> FactionsPlugin.getInstance().conf().factions().homes().isEnabled()))
                                            .and(Cloudy.predicate(s -> FactionsPlugin.getInstance().conf().factions().homes().isTeleportCommandEnabled()))
                            ))
                            .flag(
                                    manager.flagBuilder("faction")
                                            .withComponent(FactionParser.of(FactionParser.Include.PLAYERS))
                                            .withPermission(Cloudy.isBypass())
                            )
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();

        Faction targetFaction = sender.getFaction();

        if (sender.isAdminBypassing() && context.flags().get("faction") instanceof Faction fac) {
            if (targetFaction.hasHome()) {
                AbstractFactionsPlugin.getInstance().teleport(player, fac.getHome());
            } else {
                sender.msg(TL.COMMAND_HOME_NOHOME);
            }
            return;
        }

        if (!targetFaction.hasHome()) {
            sender.msg(TL.COMMAND_HOME_NOHOME);
            return;
        }

        if (!targetFaction.hasAccess(sender, PermissibleActions.HOME, sender.getLastStoodAt())) {
            sender.msg(TL.COMMAND_HOME_DENIED, targetFaction.getTag(sender));
            return;
        }

        if (!FactionsPlugin.getInstance().conf().factions().homes().isTeleportAllowedFromEnemyTerritory() && sender.isInEnemyTerritory()) {
            sender.msg(TL.COMMAND_HOME_INENEMY);
            return;
        }

        if (!FactionsPlugin.getInstance().conf().factions().homes().isTeleportAllowedFromDifferentWorld() && player.getWorld().getUID() != targetFaction.getHome().getWorld().getUID()) {
            sender.msg(TL.COMMAND_HOME_WRONGWORLD);
            return;
        }

        Faction faction = Board.board().factionAt(new FLocation(player.getLocation()));
        final Location loc = player.getLocation().clone();

        // if player is not in a safe zone or their own faction territory, only allow teleport if no enemies are nearby
        if (FactionsPlugin.getInstance().conf().factions().homes().getTeleportAllowedEnemyDistance() > 0 &&
                !faction.isSafeZone() &&
                (!sender.isInOwnTerritory() || !FactionsPlugin.getInstance().conf().factions().homes().isTeleportIgnoreEnemiesIfInOwnTerritory())) {
            World w = loc.getWorld();
            double x = loc.getX();
            double y = loc.getY();
            double z = loc.getZ();

            for (Player p : player.getServer().getOnlinePlayers()) {
                if (p == null || !p.isOnline() || p.isDead() || p.getUniqueId().equals(player.getUniqueId()) || p.getWorld() != w) {
                    continue;
                }

                FPlayer fp = FPlayers.fPlayers().get(p);
                if (sender.getRelationTo(fp) != Relation.ENEMY || fp.isVanished()) {
                    continue;
                }

                Location l = p.getLocation();
                double dx = Math.abs(x - l.getX());
                double dy = Math.abs(y - l.getY());
                double dz = Math.abs(z - l.getZ());
                double max = FactionsPlugin.getInstance().conf().factions().homes().getTeleportAllowedEnemyDistance();

                // box-shaped distance check
                if (dx > max || dy > max || dz > max) {
                    continue;
                }

                sender.msg(TL.COMMAND_HOME_ENEMYNEAR, String.valueOf(FactionsPlugin.getInstance().conf().factions().homes().getTeleportAllowedEnemyDistance()));
                return;
            }
        }

        Location destination = targetFaction.getHome();
        FPlayerTeleportEvent tpEvent = new FPlayerTeleportEvent(sender, destination, FPlayerTeleportEvent.Reason.HOME);
        Bukkit.getServer().getPluginManager().callEvent(tpEvent);
        if (tpEvent.isCancelled()) {
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        if (!context.sender().payForCommand(FactionsPlugin.getInstance().conf().economy().getCostHome(), TL.COMMAND_HOME_TOTELEPORT, TL.COMMAND_HOME_FORTELEPORT)) {
            return;
        }

        // if Essentials teleport handling is enabled and available, pass the teleport off to it (for delay and cooldown)
        if (FactionsPlugin.getInstance().getIntegrationManager().isEnabled(IntegrationManager.Integration.ESS) && Essentials.handleTeleport(player, destination)) {
            return;
        }

        WarmUpUtil.process(sender, WarmUpUtil.Warmup.HOME, TL.WARMUPS_NOTIFY_TELEPORT, "Home", () -> {
            Player plr = sender.getPlayer();

            if (plr == null) return;
            // Create a smoke effect
            if (FactionsPlugin.getInstance().conf().factions().homes().isTeleportCommandSmokeEffectEnabled()) {
                List<Location> smokeLocations = new ArrayList<>();
                smokeLocations.add(loc);
                smokeLocations.add(loc.add(0, 1, 0));
                smokeLocations.add(destination);
                smokeLocations.add(destination.clone().add(0, 1, 0));
                SmokeUtil.spawnCloudRandom(smokeLocations, FactionsPlugin.getInstance().conf().factions().homes().getTeleportCommandSmokeEffectThickness());
            }

            AbstractFactionsPlugin.getInstance().teleport(plr, destination);
        }, FactionsPlugin.getInstance().conf().commands().home().getDelay());
    }
}
