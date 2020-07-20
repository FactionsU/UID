package com.massivecraft.factions.util.material;

import com.google.gson.reflect.TypeToken;
import com.massivecraft.factions.FactionsPlugin;
import org.bukkit.Material;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class MaterialDb {
    private static Map<String, Material> map;

    private MaterialDb() {
    }

    public static Material get(String name) {
        return get(name, Material.AIR);
    }

    public static Material get(String name, Material defaultMaterial) {
        if (name == null) {
            FactionsPlugin.getInstance().log("Null material name found");
            return defaultMaterial;
        }

        Material material = Material.getMaterial(name);
        if (material == null) {
            material = map.get(name.toUpperCase());
        }

        if (material == null) {
            FactionsPlugin.getInstance().log(Level.INFO, "Material does not exist: " + name.toUpperCase());
            return defaultMaterial;
        }

        return material;
    }

    public static void load() {
        InputStreamReader reader = new InputStreamReader(FactionsPlugin.getInstance().getResource("materials.json"));
        Type typeToken = new TypeToken<HashMap<String, String>>() {
        }.getType();
        HashMap<String, String> materialData = FactionsPlugin.getInstance().getGson().fromJson(reader, typeToken);
        map = new HashMap<>();
        materialData.forEach((n, l) -> {
            Material matN = Material.getMaterial(n);
            Material matL = Material.getMaterial(l);
            boolean nNull = matN == null;
            if (nNull == (matL == null)) {
                return;
            }
            map.put(nNull ? n : l, nNull ? matL : matN);
        });
        FactionsPlugin.getInstance().getLogger().info(String.format("Loaded %s material mappings.", map.size()));
    }
}
