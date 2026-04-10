package dev.kitteh.factions.integration;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class VaultPerms {
    private Permission perms = null;

    public VaultPerms() {
        try {
            RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
            if (rsp != null) {
                perms = rsp.getProvider();
            }
        } catch (NoClassDefFoundError ignored) {
        }
    }

    public String getName() {
        return perms == null ? "nope" : perms.getName();
    }

    public Object getPerms() {
        return perms;
    }
}
