package com.massivecraft.factions.config.file;

import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.config.annotation.Comment;

import java.lang.reflect.Field;
import java.util.logging.Level;

public class DefaultOfflinePermissionsConfig {
    @Comment("Offline permissions settings\n" +
            "If you don't enable offline permissions in the main config and never plan to do so,\n" +
            "  you can ignore this file entirely. If you plan to do so in the future, you should probably\n" +
            "  set this file up a bit as these defaults will sit in the faction configs regardless.\n" +
            "\n" +
            "Each main section represents one permission.\n" +
            "Inside is each relation.\n" +
            "Each relation has a default value (true=allowed, false=disallowed)\n" +
            "  and true/false for if it's locked to editing by factions admins.")
    private DefaultPermissionsConfig.Permissions offlinePermissions = new DefaultPermissionsConfig.Permissions();

    public DefaultOfflinePermissionsConfig() {
        try {
            for (Field field : DefaultPermissionsConfig.Permissions.class.getDeclaredFields()) {
                field.setAccessible(true);
                Object o = field.get(offlinePermissions);
                if (o instanceof DefaultPermissionsConfig.Permissions.FullPermInfo) {
                    DefaultPermissionsConfig.Permissions.FullPermInfo i = (DefaultPermissionsConfig.Permissions.FullPermInfo) o;
                    i.recruit = null;
                    i.normal = null;
                    i.moderator = null;
                    i.coleader = null;
                } else if (o instanceof DefaultPermissionsConfig.Permissions.FactionOnlyPermInfo) {
                    field.set(offlinePermissions, null);
                }
            }
        } catch (IllegalAccessException e) {
            FactionsPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to update offline permission special states", e);
        }
    }

    public DefaultPermissionsConfig.Permissions getPermissions() {
        return this.offlinePermissions;
    }
}