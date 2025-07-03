package dev.kitteh.factions.integration;

import com.elmakers.mine.bukkit.api.entity.TeamProvider;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.protection.BlockBreakManager;
import com.elmakers.mine.bukkit.api.protection.BlockBuildManager;
import com.elmakers.mine.bukkit.api.protection.EntityTargetingManager;
import com.elmakers.mine.bukkit.api.protection.PVPManager;
import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.listener.FactionsBlockListener;
import dev.kitteh.factions.listener.FactionsEntityListener;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class Magic implements BlockBuildManager, BlockBreakManager, PVPManager, TeamProvider, EntityTargetingManager, Listener {
    public static boolean init(Plugin plugin) {
        if (plugin instanceof MagicAPI) {
            try {
                int v = Integer.parseInt(plugin.getDescription().getVersion().split("\\.")[0]);
                if (v < 8) {
                    AbstractFactionsPlugin.instance().getLogger().info("Found Magic, but only supporting version 8+");
                    return false;
                }
            } catch (NumberFormatException ignored) {
                AbstractFactionsPlugin.instance().getLogger().info("Found Magic, but could not determine version");
                return false;
            }
            AbstractFactionsPlugin.instance().getLogger().info("Integrating with Magic!");
            ((MagicAPI) plugin).getController().register(new Magic());
            return true;
        }
        return false;
    }

    @Override
    public boolean hasBuildPermission(Player player, Block block) {
        if (block == null) {
            return true;
        }
        if (player == null) {
            return Board.board().factionAt(new FLocation(block)).isWilderness();
        }
        return FactionsBlockListener.playerCanBuildDestroyBlock(player, block.getLocation(), PermissibleActions.BUILD, true);
    }

    @Override
    public boolean hasBreakPermission(Player player, Block block) {
        if (block == null) {
            return true;
        }
        if (player == null) {
            return Board.board().factionAt(new FLocation(block)).isWilderness();
        }
        return FactionsBlockListener.playerCanBuildDestroyBlock(player, block.getLocation(), PermissibleActions.DESTROY, true);
    }

    @Override
    public boolean isPVPAllowed(Player player, Location location) {
        if (player == null && !FactionsPlugin.instance().conf().plugins().magic().isUsePVPSettingForMagicMobs()) {
            return true;
        }
        MainConfig.Factions facConf = FactionsPlugin.instance().conf().factions();
        if (facConf.pvp().getWorldsIgnorePvP().contains(location.getWorld().getName())) {
            return true;
        }
        if (player != null && facConf.protection().getPlayersWhoBypassAllProtection().contains(player.getName())) {
            return true;
        }
        Faction defFaction = Board.board().factionAt(new FLocation(location));
        if (defFaction.noPvPInTerritory()) {
            return false;
        }
        if (player != null) {
            FPlayer attacker = FPlayers.fPlayers().get(player);
            if (attacker.loginPVPDisabled()) {
                return false;
            }

            Faction locFaction = Board.board().factionAt(new FLocation(player));
            if (locFaction.noPvPInTerritory() || locFaction.isSafeZone()) {
                return false;
            }
        }

        Faction locFaction = Board.board().factionAt(new FLocation(location));
        if (locFaction.noPvPInTerritory()) {
            return false;
        }
        return !locFaction.isSafeZone();
    }

    @Override
    public boolean isFriendly(Entity attacker, Entity entity) {
        if (!(attacker instanceof Player && entity instanceof Player)) {
            return false;
        }
        FPlayer attack = FPlayers.fPlayers().get((Player) attacker);
        FPlayer defend = FPlayers.fPlayers().get((Player) entity);
        if (attack.faction().isWilderness() || defend.faction().isWilderness()) {
            return false;
        }
        return attack.relationTo(defend).isAtLeast(Relation.TRUCE);
    }

    @Override
    public boolean canTarget(Entity source, Entity target) {
        return FactionsEntityListener.canDamage(source, target, false);
    }
}
