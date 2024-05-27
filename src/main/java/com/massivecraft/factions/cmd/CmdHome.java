package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.event.FPlayerTeleportEvent;
import com.massivecraft.factions.integration.Essentials;
import com.massivecraft.factions.integration.IntegrationManager;
import com.massivecraft.factions.perms.PermissibleActions;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.SmokeUtil;
import com.massivecraft.factions.util.TL;
import com.massivecraft.factions.util.WarmUpUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class CmdHome extends FCommand {

    public CmdHome() {
        super();
        this.aliases.add("home");

        this.optionalArgs.put("faction", "yours");

        this.requirements = new CommandRequirements.Builder(Permission.HOME)
                .playerOnly()
                .noDisableOnLock()
                .build();
    }

    @Override
    public void perform(final CommandContext context) {
        Faction targetFaction = context.argAsFaction(0, context.fPlayer == null ? null : context.faction);

        if (targetFaction != context.faction && context.fPlayer.isAdminBypassing()) {
            if (targetFaction.hasHome()) {
                FactionsPlugin.getInstance().teleport(context.player, targetFaction.getHome());
            } else {
                context.fPlayer.msg(TL.COMMAND_HOME_NOHOME);
            }
            return;
        }

        if (!FactionsPlugin.getInstance().conf().factions().homes().isEnabled()) {
            context.fPlayer.msg(TL.COMMAND_HOME_DISABLED);
            return;
        }

        if (!FactionsPlugin.getInstance().conf().factions().homes().isTeleportCommandEnabled()) {
            context.fPlayer.msg(TL.COMMAND_HOME_TELEPORTDISABLED);
            return;
        }

        if (!targetFaction.hasHome()) {
            if (targetFaction == context.faction) {
                if (context.faction.hasAccess(context.fPlayer, PermissibleActions.SETHOME, context.fPlayer.getLastStoodAt())) {
                    context.fPlayer.msg(TL.COMMAND_HOME_NOHOME.toString() + TL.GENERIC_YOUSHOULD);
                } else {
                    context.fPlayer.msg(TL.COMMAND_HOME_NOHOME.toString() + TL.GENERIC_ASKYOURLEADER);
                }
                context.fPlayer.sendMessage(FCmdRoot.getInstance().cmdSethome.getUsageTemplate(context));
            } else {
                context.fPlayer.msg(TL.COMMAND_HOME_NOHOME);
            }
            return;
        }

        if (!targetFaction.hasAccess(context.fPlayer, PermissibleActions.HOME, context.fPlayer.getLastStoodAt())) {
            context.fPlayer.msg(TL.COMMAND_HOME_DENIED, targetFaction.getTag(context.fPlayer));
            return;
        }

        if (!FactionsPlugin.getInstance().conf().factions().homes().isTeleportAllowedFromEnemyTerritory() && context.fPlayer.isInEnemyTerritory()) {
            context.fPlayer.msg(TL.COMMAND_HOME_INENEMY);
            return;
        }

        if (!FactionsPlugin.getInstance().conf().factions().homes().isTeleportAllowedFromDifferentWorld() && context.player.getWorld().getUID() != targetFaction.getHome().getWorld().getUID()) {
            context.fPlayer.msg(TL.COMMAND_HOME_WRONGWORLD);
            return;
        }

        Faction faction = Board.getInstance().getFactionAt(new FLocation(context.player.getLocation()));
        final Location loc = context.player.getLocation().clone();

        // if player is not in a safe zone or their own faction territory, only allow teleport if no enemies are nearby
        if (FactionsPlugin.getInstance().conf().factions().homes().getTeleportAllowedEnemyDistance() > 0 &&
                !faction.isSafeZone() &&
                (!context.fPlayer.isInOwnTerritory() || !FactionsPlugin.getInstance().conf().factions().homes().isTeleportIgnoreEnemiesIfInOwnTerritory())) {
            World w = loc.getWorld();
            double x = loc.getX();
            double y = loc.getY();
            double z = loc.getZ();

            for (Player p : context.player.getServer().getOnlinePlayers()) {
                if (p == null || !p.isOnline() || p.isDead() || p == context.player || p.getWorld() != w) {
                    continue;
                }

                FPlayer fp = FPlayers.getInstance().getByPlayer(p);
                if (context.fPlayer.getRelationTo(fp) != Relation.ENEMY || fp.isVanished()) {
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

                context.fPlayer.msg(TL.COMMAND_HOME_ENEMYNEAR, String.valueOf(FactionsPlugin.getInstance().conf().factions().homes().getTeleportAllowedEnemyDistance()));
                return;
            }
        }

        Location destination = targetFaction.getHome();
        FPlayerTeleportEvent tpEvent = new FPlayerTeleportEvent(context.fPlayer, destination, FPlayerTeleportEvent.PlayerTeleportReason.HOME);
        Bukkit.getServer().getPluginManager().callEvent(tpEvent);
        if (tpEvent.isCancelled()) {
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        if (!context.payForCommand(FactionsPlugin.getInstance().conf().economy().getCostHome(), TL.COMMAND_HOME_TOTELEPORT.toString(), TL.COMMAND_HOME_FORTELEPORT.toString())) {
            return;
        }

        // if Essentials teleport handling is enabled and available, pass the teleport off to it (for delay and cooldown)
        if (FactionsPlugin.getInstance().getIntegrationManager().isEnabled(IntegrationManager.Integration.ESS) && Essentials.handleTeleport(context.player, destination)) {
            return;
        }

        context.doWarmUp(WarmUpUtil.Warmup.HOME, TL.WARMUPS_NOTIFY_TELEPORT, "Home", () -> {
            // Create a smoke effect
            if (FactionsPlugin.getInstance().conf().factions().homes().isTeleportCommandSmokeEffectEnabled()) {
                List<Location> smokeLocations = new ArrayList<>();
                smokeLocations.add(loc);
                smokeLocations.add(loc.add(0, 1, 0));
                smokeLocations.add(destination);
                smokeLocations.add(destination.clone().add(0, 1, 0));
                SmokeUtil.spawnCloudRandom(smokeLocations, FactionsPlugin.getInstance().conf().factions().homes().getTeleportCommandSmokeEffectThickness());
            }

            FactionsPlugin.getInstance().teleport(context.player, destination);
        }, this.plugin.conf().commands().home().getDelay());
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_HOME_DESCRIPTION;
    }

}
