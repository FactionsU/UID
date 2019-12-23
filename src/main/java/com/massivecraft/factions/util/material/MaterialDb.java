package com.massivecraft.factions.util.material;

import com.google.gson.reflect.TypeToken;
import com.massivecraft.factions.FactionsPlugin;
import org.bukkit.Material;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;

public class MaterialDb {

    /*

    This utility has no concept of block metadata, converts if necessary 1.13
    material names to < 1.12 materials, or keeps 1.13 materials.

    Useful as we don't really need extra metadata for stuff like territory block breaking checking.

        "ACACIA_BOAT": {
            "material": "ACACIA_BOAT",
            "legacy": "BOAT_ACACIA"
        }

     */

    private static MaterialDb instance;

    public boolean legacy = true;
    public MaterialProvider provider;

    private MaterialDb() {
    }

    public Material get(String name) {
        return provider.resolve(name);
    }

    public static void load() {
        instance = new MaterialDb();
        if (instance.legacy = FactionsPlugin.getMCVersion() < 1300) { // Before 1.13
            FactionsPlugin.getInstance().getLogger().info("Using legacy support for materials");
        }

        InputStreamReader reader = new InputStreamReader(FactionsPlugin.getInstance().getResource("materials.json"));
        Type typeToken = new TypeToken<HashMap<String, MaterialProvider.MaterialData>>() {
        }.getType();
        HashMap<String, MaterialProvider.MaterialData> materialData = FactionsPlugin.getInstance().getGson().fromJson(reader, typeToken);
        FactionsPlugin.getInstance().getLogger().info(String.format("Loaded %s material mappings.", materialData.keySet().size()));
        instance.provider = new MaterialProvider(materialData);
    }

    public static MaterialDb getInstance() {
        return instance;
    }

}
