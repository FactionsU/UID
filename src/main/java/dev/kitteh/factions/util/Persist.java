package dev.kitteh.factions.util;

import dev.kitteh.factions.plugin.AbstractFactionsPlugin;

import java.io.File;
import java.lang.reflect.Type;
import java.util.logging.Level;

public class Persist {

    private final AbstractFactionsPlugin plugin;

    public Persist(AbstractFactionsPlugin plugin) {
        this.plugin = plugin;
    }

    public File getFile(String name) {
        return new File(plugin.getDataFolder(), name + ".json");
    }

    // SAVE

    public void save(Object instance, String name) {
        save(instance, getFile(name));
    }

    public void save(Object instance, File file) {
        DiscUtil.writeCatch(file, plugin.gson().toJson(instance), true);
    }

    // LOAD BY TYPE
    public <T> T load(Type typeOfT, String name) {
        return load(typeOfT, getFile(name));
    }

    public <T> T load(Type typeOfT, File file) {
        String content = DiscUtil.readCatch(file);
        if (content == null) {
            return null;
        }

        try {
            return plugin.gson().fromJson(content, typeOfT);
        } catch (
                Exception ex) {    // output the error message rather than full stack trace; error parsing the file, most likely
            plugin.log(Level.WARNING, ex.getMessage());
        }

        return null;
    }
}
