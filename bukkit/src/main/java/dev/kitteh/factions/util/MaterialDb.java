package dev.kitteh.factions.util;

import com.google.gson.reflect.TypeToken;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import org.bukkit.Material;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@Deprecated(forRemoval = true, since = "4.0.0")
public class MaterialDb {
    private static Map<String, Material> map;

    private MaterialDb() {
    }

    public static Material get(String name) {
        return get(name, Material.AIR);
    }

    public static Material get(String name, Material defaultMaterial) {
        if (name == null) {
            return defaultMaterial;
        }

        Material material = Material.getMaterial(name);
        if (material == null) {
            material = map.get(name.toUpperCase());
            if (material != null) {
                AbstractFactionsPlugin.instance().getLogger().warning("Outdated material name \"" + name + "\" found in a config - update it to \"" + material.name() + "\" ASAP!\n" +
                        "This wrong name will stop working in a future plugin update.");
            }
        }

        if (material == null) {
            if (!name.equalsIgnoreCase("exampleMaterial")) {
                AbstractFactionsPlugin.instance().log(Level.INFO, "Material does not exist: " + name.toUpperCase());
            }
            return defaultMaterial;
        }

        return material;
    }

    public static void load() {
        InputStreamReader reader = new InputStreamReader(AbstractFactionsPlugin.instance().getResource("materials.json"));
        Type typeToken = new TypeToken<HashMap<String, String>>() {
        }.getType();
        HashMap<String, String> materialData = AbstractFactionsPlugin.instance().gson().fromJson(reader, typeToken);
        map = new HashMap<>();
        for (Material m : Material.values()) {
            map.put(m.name(), m);
        }
        materialData.forEach((n, l) -> {
            Material matN = Material.getMaterial(n);
            Material matL = Material.getMaterial(l);
            boolean nNull = matN == null;
            if (nNull == (matL == null)) {
                return;
            }
            map.put(nNull ? n : l, nNull ? matL : matN);
        });
    }
}
