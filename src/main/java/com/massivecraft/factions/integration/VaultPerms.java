package com.massivecraft.factions.integration;

import com.massivecraft.factions.FactionsPlugin;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultPerms {
    private Permission perms = null;

    public VaultPerms() {
        try {
            RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
            if (rsp != null) {
                perms = rsp.getProvider();
            }
        } catch (NoClassDefFoundError ex) {
            return;
        }
        if (perms != null) {
            FactionsPlugin.getInstance().getLogger().info("Using Vault with permissions plugin " + perms.getName());
        }
    }

    public String getName() {
        return perms == null ? "nope" : perms.getName();
    }

    public Object getPerms() {
        return perms;
    }

    public String getPrimaryGroup(OfflinePlayer player) {
        return perms == null || !perms.hasGroupSupport() ? " " : perms.getPrimaryGroup(Bukkit.getWorlds().get(0).toString(), player);
    }
}
