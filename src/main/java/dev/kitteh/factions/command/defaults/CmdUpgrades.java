package dev.kitteh.factions.command.defaults;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.upgrade.Upgrade;
import dev.kitteh.factions.upgrade.UpgradeRegistry;
import dev.kitteh.factions.upgrade.UpgradeSettings;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

public class CmdUpgrades implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("upgrades")
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.UPGRADES).and(Cloudy.hasFaction())))
                            .commandDescription(Cloudy.desc(TL.COMMAND_UPGRADES_DESCRIPTION))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();
        Faction faction = sender.getFaction();

        ChestGui gui = new ChestGui(6, ComponentHolder.of(Component.text("Upgrades")));

        gui.setOnGlobalDrag(e -> e.setCancelled(true));
        gui.setOnGlobalClick(e -> e.setCancelled(true));

        PaginatedPane upperPane = new PaginatedPane(0, 0, 9, 5);
        upperPane.populateWithGuiItems(UpgradeRegistry.getUpgrades().stream()
                .sorted(Comparator.comparing(Upgrade::name))
                .filter(Universe.getInstance()::isUpgradeEnabled)
                .map(upgrade -> {
                    UpgradeSettings settings = Universe.getInstance().getUpgradeSettings(upgrade);


                    ItemStack stack = buildStack(settings, sender);

                    GuiItem guiItem = new GuiItem(stack);

                    guiItem.setAction(e -> {
                        e.setCancelled(true);
                        if (!sender.getFaction().hasAccess(sender, PermissibleActions.UPGRADE, null)) {
                            return;
                        }

                        int lvl = faction.getUpgradeLevel(upgrade);

                        if (lvl == settings.maxLevel()) {
                            return;
                        }

                        int buyHeight = 3;
                        ChestGui buyGui = new ChestGui(buyHeight, "Purchase " + upgrade.name() + "?");
                        buyGui.setOnGlobalDrag(ee -> e.setCancelled(true));
                        buyGui.setOnGlobalClick(ee -> e.setCancelled(true));

                        StaticPane buyMenu = new StaticPane(0, 0, 9, buyHeight);

                        ItemStack buy = new ItemStack(Material.GREEN_CONCRETE);
                        ItemMeta met = buy.getItemMeta();
                        met.setDisplayName(ChatColor.GREEN + "Buy for " + settings.costAt(lvl + 1));
                        buy.setItemMeta(met);

                        GuiItem buyItem = new GuiItem(buy);
                        buyItem.setAction(ee -> {
                            ee.setCancelled(true);

                            if (context.sender().payForCommand(settings.costAt(lvl + 1).doubleValue(), TL.COMMAND_UPGRADES_TOUPGRADE, TL.COMMAND_UPGRADES_FORUPGRADE)) {
                                faction.setUpgradeLevel(upgrade, lvl + 1);
                                guiItem.setItem(buildStack(settings, sender));
                                gui.show(player);
                            } else {
                                ItemStack nope = new ItemStack(Material.RED_CONCRETE);
                                ItemMeta metaa = nope.getItemMeta();
                                metaa.setDisplayName(ChatColor.RED + "Cannot afford this upgrade. Click cancel to go back.");
                                nope.setItemMeta(metaa);
                                buyItem.setItem(nope);
                                buyItem.setAction(eee -> eee.setCancelled(true));
                                buyGui.update();
                            }
                        });

                        buyMenu.addItem(buyItem, 2, 1);

                        ItemStack cancel = new ItemStack(Material.BARRIER);
                        met = cancel.getItemMeta();
                        met.setDisplayName(ChatColor.RED + "Cancel");
                        cancel.setItemMeta(met);

                        GuiItem cancelItem = new GuiItem(cancel);
                        cancelItem.setAction(ee -> {
                            e.setCancelled(true);
                            gui.show(player);
                        });

                        buyMenu.addItem(cancelItem, 6, 1);

                        buyGui.addPane(buyMenu);

                        buyGui.show(player);
                    });

                    return guiItem;
                })
                .toList());

        StaticPane bottomMenu = new StaticPane(0, 5, 9, 1);

        if (upperPane.getPages() > 1) {
            ItemStack left = new ItemStack(Material.BLUE_TERRACOTTA);
            ItemMeta meta = left.getItemMeta();
            meta.setDisplayName("Previous");
            left.setItemMeta(meta);
            GuiItem prev = new GuiItem(left);
            prev.setAction(event -> {
                event.setCancelled(true);
                int newPage = upperPane.getPage() - 1;
                if (newPage < 0) {
                    newPage = upperPane.getPages() - 1;
                }
                upperPane.setPage(newPage);
                gui.update();
            });
            bottomMenu.addItem(prev, 0, 0);

            ItemStack right = new ItemStack(Material.BLUE_TERRACOTTA);
            meta = right.getItemMeta();
            meta.setDisplayName("Next");
            right.setItemMeta(meta);
            GuiItem next = new GuiItem(right);
            next.setAction(event -> {
                event.setCancelled(true);
                int newPage = upperPane.getPage() + 1;
                if (newPage >= upperPane.getPages()) {
                    newPage = 0;
                }
                upperPane.setPage(newPage);
                gui.update();
            });
            bottomMenu.addItem(next, 8, 0);
        }

        gui.addPane(upperPane);
        gui.addPane(bottomMenu);

        gui.show(player);
    }

    private ItemStack buildStack(UpgradeSettings settings, FPlayer sender) {
        Upgrade upgrade = settings.upgrade();

        int lvl = sender.getFaction().getUpgradeLevel(upgrade);

        ItemStack stack = new ItemStack(Material.STONE);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(upgrade.nameComponent()) + " " + (lvl < 1 || settings.maxLevel() == 1 ? "" : lvl));

        List<String> lore = new ArrayList<>();

        lore.add(LegacyComponentSerializer.legacySection().serialize(upgrade.description()));
        if (lvl > 0) {
            lore.add(LegacyComponentSerializer.legacySection().serialize(upgrade.details(settings, lvl)));
        }
        lore.add(" ");
        if (lvl < settings.maxLevel()) {
            lore.add("Upgrade available: Costs " + settings.costAt(lvl + 1));
            if (sender.getFaction().hasAccess(sender, PermissibleActions.UPGRADE, null)) {
                lore.add("  Click to purchase!");
            } else {
                lore.add("  You lack permission to purchase upgrades");
            }
            lore.add(LegacyComponentSerializer.legacySection().serialize(upgrade.nameComponent()) + " " + (lvl + 1));
            lore.add(LegacyComponentSerializer.legacySection().serialize(upgrade.details(settings, lvl + 1)));
        } else {
            lore.add("Max level!");
        }
        meta.setLore(lore);

        stack.setItemMeta(meta);

        return stack;
    }
}
