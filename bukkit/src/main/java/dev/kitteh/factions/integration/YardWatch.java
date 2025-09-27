package dev.kitteh.factions.integration;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.protection.Protection;
import dev.kitteh.factions.util.WorldUtil;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class YardWatch implements me.youhavetrouble.yardwatch.Protection {
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isProtected(Location location) {
        return WorldUtil.isEnabled(location) && !new FLocation(location).faction().isWilderness();
    }

    @Override
    public boolean canBreakBlock(Player player, BlockState blockState) {
        return !WorldUtil.isEnabled(blockState.getWorld()) ||
                !Protection.denyBuildOrDestroyBlock(
                        player,
                        blockState.getLocation(),
                        PermissibleActions.DESTROY,
                        false
                );
    }

    @Override
    public boolean canPlaceBlock(Player player, Location location) {
        return !WorldUtil.isEnabled(location) ||
                !Protection.denyBuildOrDestroyBlock(
                        player,
                        location,
                        PermissibleActions.BUILD,
                        false
                );
    }

    @Override
    public boolean canInteract(Player player, BlockState blockState) {
        return !WorldUtil.isEnabled(blockState.getWorld()) ||
                !Protection.denyUseBlock(
                        player,
                        blockState.getType(),
                        blockState.getLocation(),
                        false
                );
    }

    @Override
    public boolean canInteract(Player player, Entity target) {
        return !WorldUtil.isEnabled(target) || !Protection.denyInteract(player, target.getLocation());
    }

    @Override
    public boolean canDamage(Entity damager, Entity target) {
        return !WorldUtil.isEnabled(target) || !Protection.denyDamage(damager, target, false);
    }
}
