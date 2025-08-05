package dev.kitteh.factions.listener;

import dev.kitteh.factions.gui.GUI;
import dev.kitteh.factions.util.WorldUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

public class ListenGUI implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractGUI(InventoryClickEvent event) {
        if (!WorldUtil.isEnabled(event.getWhoClicked())) {
            return;
        }

        Inventory clickedInventory = getClickedInventory(event);
        if (clickedInventory == null) {
            return;
        }
        if (clickedInventory.getHolder() instanceof GUI<?> ui) {
            event.setCancelled(true);
            ui.click(event.getRawSlot());
        }
    }

    private Inventory getClickedInventory(InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();
        InventoryView view = event.getView();
        if (rawSlot < 0 || rawSlot >= view.countSlots()) { // < 0 check also covers situation of InventoryView.OUTSIDE (-999)
            return null;
        }
        if (rawSlot < view.getTopInventory().getSize()) {
            return view.getTopInventory();
        } else {
            return view.getBottomInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMoveGUI(InventoryDragEvent event) {
        if (!WorldUtil.isEnabled(event.getWhoClicked())) {
            return;
        }

        if (event.getInventory().getHolder() instanceof GUI<?>) {
            event.setCancelled(true);
        }
    }
}
