package dev.kitteh.factions.util;

import dev.kitteh.factions.FactionsPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;

@NullMarked
@Deprecated
public class PermUtil {
    public final Map<String, String> permissionDescriptions = new HashMap<>();

    protected final FactionsPlugin plugin;

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
        if (me.hasPermission(perm)) {
            return true;
        } else if (informSenderIfNot) {
            me.sendMessage(this.getForbiddenMessage(perm));
        }
        return false;
    }
}
