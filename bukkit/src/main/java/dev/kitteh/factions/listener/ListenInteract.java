package dev.kitteh.factions.listener;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.integration.Graves;
import dev.kitteh.factions.permissible.PermissibleAction;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.protection.Protection;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.WorldUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.NumberConversions;

import java.util.HashMap;
import java.util.Map;

public class ListenInteract implements Listener {
    private final FactionsPlugin plugin;

    public ListenInteract(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer())) {
            return;
        }

        boolean check = false;
        EntityType type = event.getRightClicked().getType();
        if (type == EntityType.ITEM_FRAME || type == EntityType.GLOW_ITEM_FRAME) {
            if (Protection.denyUseBlock(event.getPlayer(), Material.ITEM_FRAME, event.getRightClicked().getLocation(), true)) {
                event.setCancelled(true);
            }
        } else if (type == EntityType.HORSE ||
                type == EntityType.SKELETON_HORSE ||
                type == EntityType.ZOMBIE_HORSE ||
                type == EntityType.DONKEY ||
                type == EntityType.MULE ||
                type == EntityType.LLAMA ||
                type == EntityType.TRADER_LLAMA ||
                type == EntityType.PIG ||
                type == EntityType.LEASH_KNOT ||
                type.name().contains("_MINECART") ||
                type.name().contains("_CHEST_BOAT")
        ) {
            check = true;
        }

        if (check && !this.plugin.conf().factions().protection().getEntityInteractExceptions().contains(event.getRightClicked().getType().name()) &&
                Protection.denyInteract(event.getPlayer(), event.getRightClicked().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer())) {
            return;
        }

        // only need to check right-clicks and physical as of MC 1.4+; good performance boost
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.PHYSICAL) {
            return;
        }

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        if (block == null) {
            return;  // clicked in air, apparently
        }

        if (Graves.allowAnyway(block)) {
            return;
        }

        if (event.getAction() == Action.PHYSICAL && block.getType() == Material.FARMLAND) {
            if (Protection.denyBuildOrDestroyBlock(player, block, PermissibleActions.DESTROY, true)) {
                event.setCancelled(true);
            }
        }

        if (Protection.denyUseBlock(player, block.getType(), block.getLocation(), true)) {
            event.setCancelled(true);
            if (block.getType().name().endsWith("_PLATE")) {
                return;
            }
            if (this.plugin.conf().exploits().isInteractionSpam()) {
                String name = player.getName();
                InteractAttemptSpam attempt = interactSpammers.computeIfAbsent(name, n -> new InteractAttemptSpam());
                int count = attempt.increment();
                if (count >= 10) {
                    FPlayer me = FPlayers.fPlayers().get(player);
                    me.msgLegacy(TL.PLAYER_OUCH);
                    player.damage(NumberConversions.floor((double) count / 10));
                }
            }
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;  // only interested on right-clicks for below
        }

        ItemStack item;
        if ((item = event.getItem()) != null) {
            Material material = item.getType();
            String materialName = item.getType().name();
            if (material == Material.ARMOR_STAND || material == Material.END_CRYSTAL || materialName.contains("MINECART")) {
                if (!this.plugin.conf().factions().specialCase().getIgnoreBuildMaterials().contains(item.getType()) &&
                        Protection.denyBuildOrDestroyBlock(event.getPlayer(), event.getClickedBlock().getRelative(event.getBlockFace()), PermissibleActions.BUILD, true)) {
                    event.setCancelled(true);
                }
            }
        }

        if (Protection.denyUseItem(player, block.getLocation(), event.getMaterial(), true, true)) {
            event.setCancelled(true);
        }
    }

    // for handling people who repeatedly spam attempts to open a door (or similar) in another faction's territory
    private final Map<String, InteractAttemptSpam> interactSpammers = new HashMap<>();

    private static class InteractAttemptSpam {
        private int attempts = 0;
        private long lastAttempt = System.currentTimeMillis();

        // returns the current attempt count
        public int increment() {
            long Now = System.currentTimeMillis();
            if (Now > lastAttempt + 2000) {
                attempts = 1;
            } else {
                attempts++;
            }
            lastAttempt = Now;
            return attempts;
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (!WorldUtil.isEnabled(event.getBlock())) {
            return;
        }

        if (Protection.denyUseItem(event.getPlayer(), event.getBlock().getLocation(), event.getBlock().getType(), false, true)) {
            event.setCancelled(true);
        }
    }

    // For some reason onPlayerInteract() sometimes misses bucket events depending on distance (something like 2-3 blocks away isn't detected),
    // but these separate bucket events below always fire without fail
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer())) {
            return;
        }

        Block block = event.getBlockClicked();
        Player player = event.getPlayer();

        if (Protection.denyUseItem(player, block.getRelative(event.getBlockFace()).getLocation(), event.getBucket(), true, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer())) {
            return;
        }

        Block block = event.getBlockClicked();
        Player player = event.getPlayer();

        if (Protection.denyUseItem(player, block.getLocation(), event.getBucket(), true, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void doYouHaveALibraryCard(PlayerTakeLecternBookEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer())) {
            return;
        }

        Player player = event.getPlayer();
        if (this.plugin.conf().factions().protection().getPlayersWhoBypassAllProtection().contains(player.getName())) {
            return;
        }

        FPlayer me = FPlayers.fPlayers().get(player);
        if (me.adminBypass()) {
            return;
        }

        FLocation location = new FLocation(event.getLectern().getLocation());
        Faction otherFaction = location.faction();
        if (this.plugin.landRaidControl().isRaidable(otherFaction)) {
            return;
        }

        PermissibleAction action = PermissibleActions.CONTAINER;
        if (!otherFaction.hasAccess(me, action, location)) {
            me.msgLegacy(TL.GENERIC_NOPERMISSION, action.shortDescription());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onArmorStand(PlayerArmorStandManipulateEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer())) {
            return;
        }

        if (Protection.denyUseBlock(event.getPlayer(), Material.ARMOR_STAND, event.getRightClicked().getLocation(), true)) {
            event.setCancelled(true);
        }
    }
}
