package com.massivecraft.factions.gui;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * This class implements an easy way to map
 * config values like: materials, lore, name, color
 * to a respective ItemStack, also has utility methods
 * to merge, clone, etc
 */
public class SimpleItem {
    private String name;
    private List<String> lore;
    private Material material;
    private boolean enchant;

    SimpleItem(Builder builder) {
        this.name = builder.name;
        this.lore = builder.lore;
        this.material = builder.material;
    }

    public SimpleItem(SimpleItem item) {
        this.name = item.name;
        this.lore = item.lore;
        this.material = item.material;
        this.enchant = item.enchant;
    }

    public ItemStack get() {
        if (isValid()) {
            ItemStack itemStack = new ItemStack(material);
            ItemMeta meta = itemStack.getItemMeta();

            if (name != null) {
                meta.setDisplayName(name);
            }
            // Empty list if not specified
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            if (enchant) {
                meta.addEnchant(Enchantment.LUCK, 1, true);
            }

            itemStack.setItemMeta(meta);
            return itemStack;
        } else {
            return new ItemStack(Material.AIR);
        }
    }

    // All non-null values in 'from' will be merged into this ItemGUI
    public void merge(SimpleItem from) {
        if (from.material != null) {
            material = from.material;
        }
        if (from.name != null) {
            name = from.name;
        }
        if (!from.lore.isEmpty()) {
            lore = from.lore;
        }
    }

    public boolean isValid() {
        // For an ItemStack to be built this class needs the material, if more information is available then it will be used
        return material != null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public boolean isEnchant() {
        return enchant;
    }

    public void setEnchant(boolean enchant) {
        this.enchant = enchant;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Material material;
        private String name;
        private List<String> lore;

        private Builder() {
        }

        public Builder setLore(List<String> lore) {
            this.lore = lore;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setMaterial(Material material) {
            this.material = material;
            return this;
        }

        public SimpleItem build() {
            return new SimpleItem(this);
        }
    }
}
