package dev.kitteh.factions.util;

import dev.kitteh.factions.config.file.MainConfig;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.HashSet;

@NullMarked
public class WorldUtil {
    private static HashSet<String> worlds = new HashSet<>();
    private static boolean check;
    private static boolean whitelist;

    public static void init(MainConfig.RestrictWorlds conf) {
        check = conf.isRestrictWorlds();
        if (!check) {
            return;
        }
        worlds = new HashSet<>(conf.getWorldList());
        whitelist = conf.isWhitelist();
    }

    private static boolean isEnabled(String name) {
        if (!check) {
            return true;
        }
        return whitelist == worlds.contains(name);
    }

    public static boolean isEnabled(World world) {
        return isEnabled(world.getName());
    }

    public static boolean isEnabled(CommandSender sender) {
        if (sender instanceof Player) {
            return isEnabled(((Player) sender).getWorld().getName());
        }
        return true;
    }
}
