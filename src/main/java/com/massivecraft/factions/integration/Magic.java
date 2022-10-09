package com.massivecraft.factions.integration;

import com.elmakers.mine.bukkit.api.entity.TeamProvider;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.protection.BlockBreakManager;
import com.elmakers.mine.bukkit.api.protection.BlockBuildManager;
import com.elmakers.mine.bukkit.api.protection.EntityTargetingManager;
import com.elmakers.mine.bukkit.api.protection.PVPManager;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.config.file.MainConfig;
import com.massivecraft.factions.listeners.FactionsBlockListener;
import com.massivecraft.factions.listeners.FactionsEntityListener;
import com.massivecraft.factions.perms.PermissibleActions;
import com.massivecraft.factions.perms.Relation;
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
                    FactionsPlugin.getInstance().getLogger().info("Found Magic, but only supporting version 8+");
                    return false;
                }
            } catch (NumberFormatException ignored) {
                FactionsPlugin.getInstance().getLogger().info("Found Magic, but could not determine version");
                return false;
            }
            FactionsPlugin.getInstance().getLogger().info("Integrating with Magic!");
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
            return Board.getInstance().getFactionAt(new FLocation(block)).isWilderness();
        }
        return FactionsBlockListener.playerCanBuildDestroyBlock(player, block.getLocation(), PermissibleActions.BUILD, true);
    }

    @Override
    public boolean hasBreakPermission(Player player, Block block) {
        if (block == null) {
            return true;
        }
        if (player == null) {
            return Board.getInstance().getFactionAt(new FLocation(block)).isWilderness();
        }
        return FactionsBlockListener.playerCanBuildDestroyBlock(player, block.getLocation(), PermissibleActions.DESTROY, true);
    }

    @Override
    public boolean isPVPAllowed(Player player, Location location) {
        if (player == null && !FactionsPlugin.getInstance().conf().magicPlugin().isUsePVPSettingForMagicMobs()) {
            return true;
        }
        MainConfig.Factions facConf = FactionsPlugin.getInstance().conf().factions();
        if (facConf.pvp().getWorldsIgnorePvP().contains(location.getWorld().getName())) {
            return true;
        }
        if (player != null && facConf.protection().getPlayersWhoBypassAllProtection().contains(player.getName())) {
            return true;
        }
        Faction defFaction = Board.getInstance().getFactionAt(new FLocation(location));
        if (defFaction.noPvPInTerritory()) {
            return false;
        }
        if (player != null) {
            FPlayer attacker = FPlayers.getInstance().getByPlayer(player);
            if (attacker.hasLoginPvpDisabled()) {
                return false;
            }

            Faction locFaction = Board.getInstance().getFactionAt(new FLocation(attacker));
            if (locFaction.noPvPInTerritory() || locFaction.isSafeZone()) {
                return false;
            }
        }

        Faction locFaction = Board.getInstance().getFactionAt(new FLocation(location));
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
        FPlayer attack = FPlayers.getInstance().getByPlayer((Player) attacker);
        FPlayer defend = FPlayers.getInstance().getByPlayer((Player) entity);
        if (attack.getFaction().isWilderness() || defend.getFaction().isWilderness()) {
            return false;
        }
        return attack.getRelationTo(defend).isAtLeast(Relation.TRUCE);
    }

    @Override
    public boolean canTarget(Entity source, Entity target) {
        return FactionsEntityListener.canDamage(source, target, false);
    }
}
