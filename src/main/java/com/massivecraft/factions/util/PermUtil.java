package com.massivecraft.factions.util;

import com.massivecraft.factions.FactionsPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import java.util.HashMap;
import java.util.Map;


public class PermUtil {

    public Map<String, String> permissionDescriptions = new HashMap<>();

    protected FactionsPlugin plugin;

    public PermUtil(FactionsPlugin plugin) {
        this.plugin = plugin;
        this.setup();
    }

    public String getForbiddenMessage(String perm) {
        return plugin.txt().parse(TL.GENERIC_NOPERMISSION.toString(), getPermissionDescription(perm));
    }

    /**
     * This method hooks into all permission plugins we are supporting
     */
    public final void setup() {
        for (Permission permission : plugin.getDescription().getPermissions()) {
            //p.log("\""+permission.getName()+"\" = \""+permission.getDescription()+"\"");
            this.permissionDescriptions.put(permission.getName(), permission.getDescription());
        }
    }

    public String getPermissionDescription(String perm) {
        String desc = permissionDescriptions.get(perm);
        if (desc == null) {
            return TL.GENERIC_DOTHAT.toString();
        }
        return desc;
    }

    public boolean has(CommandSender me, String perm, boolean informSenderIfNot) {
        if (me == null) {
            return false; // What? How?
        }
        if (me.hasPermission(perm)) {
            return true;
        } else if (informSenderIfNot) {
            me.sendMessage(this.getForbiddenMessage(perm));
        }
        return false;
    }
}
