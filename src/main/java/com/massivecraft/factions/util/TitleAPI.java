package com.massivecraft.factions.util;

import com.massivecraft.factions.FactionsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * With help from https://www.spigotmc.org/threads/send-titles-to-players-using-spigot-1-8-1-11-2.48819/
 */
public class TitleAPI {

    private static TitleAPI instance;
    private boolean supportsAPI = false;
    private boolean bailOut = false;

    private Map<String, Class> classCache = new HashMap<>();

    private Method methodChatTitle;
    private Method methodGetHandle;
    private Method methodSendPacket;
    private Constructor<?> titleConstructor;
    private Field fieldTitle;
    private Field fieldSubTitle;
    private Field fieldPlayerConnection;

    public TitleAPI() {
        instance = this;

        try {
            Player.class.getMethod("sendTitle", String.class, String.class, int.class, int.class, int.class);
            supportsAPI = true;
            FactionsPlugin.getInstance().getLogger().info("Found API support for sending player titles :D");
        } catch (NoSuchMethodException e) {
            try {
                this.methodChatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class);
                this.titleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
                this.fieldTitle = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE");
                this.fieldSubTitle = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE");

                FactionsPlugin.getInstance().getLogger().info("Didn't find API support for sending titles, using reflection instead.");
            } catch (Exception ex) {
                bailOut = true;
                FactionsPlugin.getInstance().getLogger().log(Level.SEVERE, "Didn't find API support for sending titles, and failed to use reflection. Title support disabled.", ex);
            }
        }
    }

    /**
     * Send a title to player
     *
     * @param player      Player to send the title to
     * @param title       The text displayed in the title
     * @param subtitle    The text displayed in the subtitle
     * @param fadeInTime  The time the title takes to fade in
     * @param showTime    The time the title is displayed
     * @param fadeOutTime The time the title takes to fade out
     */
    public void sendTitle(Player player, String title, String subtitle, int fadeInTime, int showTime, int fadeOutTime) {
        if (supportsAPI) {
            player.sendTitle(title, subtitle, fadeInTime, showTime, fadeOutTime);
            return;
        }

        if (bailOut) {
            return;
        }

        try {
            Object chatTitle = methodChatTitle.invoke(null, "{\"text\": \"" + title + "\"}");
            Object chatsubTitle = methodChatTitle.invoke(null, "{\"text\": \"" + subtitle + "\"}");

            Object titlePacket = titleConstructor.newInstance(fieldTitle.get(null), chatTitle, fadeInTime, showTime, fadeOutTime);
            Object subTitlePacket = titleConstructor.newInstance(fieldSubTitle.get(null), chatsubTitle, fadeInTime, showTime, fadeOutTime);

            sendPacket(player, titlePacket);
            sendPacket(player, subTitlePacket);
        } catch (Exception e) {
            FactionsPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to send title via reflection", e);
        }
    }

    private void sendPacket(Player player, Object packet) {
        try {
            if (this.methodGetHandle == null) {
                this.methodGetHandle = player.getClass().getMethod("getHandle");
            }
            Object handle = this.methodGetHandle.invoke(player);
            if (this.fieldPlayerConnection == null) {
                this.fieldPlayerConnection = handle.getClass().getField("playerConnection");
            }
            Object playerConnection = this.fieldPlayerConnection.get(handle);
            if (this.methodSendPacket == null) {
                this.methodSendPacket = playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet"));
            }
            this.methodSendPacket.invoke(playerConnection, packet);
        } catch (Exception e) {
            FactionsPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to send title packet via reflection", e);
        }
    }

    /**
     * Get NMS class using reflection
     *
     * @param name Name of the class
     * @return Class
     */
    private Class<?> getNMSClass(String name) throws ClassNotFoundException {
        String versionName = "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + name;

        if (classCache.containsKey(versionName)) {
            return classCache.get(versionName);
        }

        Class clazz = Class.forName(versionName);
        classCache.put(name, clazz);
        return clazz;
    }

    public static TitleAPI getInstance() {
        return instance;
    }
}
