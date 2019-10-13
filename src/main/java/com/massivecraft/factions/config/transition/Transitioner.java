package com.massivecraft.factions.config.transition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.config.Loader;
import com.massivecraft.factions.config.transition.oldclass.v0.OldAccessV0;
import com.massivecraft.factions.config.transition.oldclass.v0.NewMemoryFaction;
import com.massivecraft.factions.config.transition.oldclass.v0.OldConfV0;
import com.massivecraft.factions.config.transition.oldclass.v0.OldMemoryFactionV0;
import com.massivecraft.factions.config.transition.oldclass.v0.OldPermissableV0;
import com.massivecraft.factions.config.transition.oldclass.v0.OldPermissableActionV0;
import com.massivecraft.factions.config.transition.oldclass.v0.OldPermissionsMapTypeAdapterV0;
import com.massivecraft.factions.config.transition.oldclass.v0.TransitionConfigV0;
import com.massivecraft.factions.util.EnumTypeAdapter;
import com.massivecraft.factions.util.LazyLocation;
import com.massivecraft.factions.util.MapFLocToStringSetTypeAdapter;
import com.massivecraft.factions.util.MyLocationTypeAdapter;
import com.massivecraft.factions.util.material.FactionMaterial;
import com.massivecraft.factions.util.material.adapter.FactionMaterialAdapter;
import com.massivecraft.factions.util.material.adapter.MaterialAdapter;
import org.bukkit.Material;

import java.io.File;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class Transitioner {
    private FactionsPlugin plugin;
    private Gson gsonV0;

    public Transitioner(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    public void migrateV0() {
        Path pluginFolder = this.plugin.getDataFolder().toPath();
        Path configFolder = pluginFolder.resolve("config");
        if (configFolder.toFile().exists()) {
            // Found existing config, so nothing to do here.
            return;
        }
        Path oldConf = pluginFolder.resolve("conf.json");
        if (!oldConf.toFile().exists()) {
            // No config, no conversion!
            return;
        }
        Path oldConfigFolder = pluginFolder.resolve("oldConfig");
        File oldConfigFolderFile = oldConfigFolder.toFile();
        if (oldConfigFolderFile.exists()) {
            // Found existing oldConfig, implying it was already upgraded once
            this.plugin.getLogger().warning("Found no 'config' folder, but an 'oldConfig' exists. Not attempting conversion.");
            return;
        }
        this.plugin.getLogger().info("Found no 'config' folder. Starting configuration transition...");
        this.buildV0Gson();
        try {
            OldConfV0 conf = this.gsonV0.fromJson(new String(Files.readAllBytes(oldConf), StandardCharsets.UTF_8), OldConfV0.class);
            TransitionConfigV0 newConfig = new TransitionConfigV0(conf);
            Loader.load("main", newConfig, "If you see this message, transitioning your config only got part way.");
            oldConfigFolderFile.mkdir();
            Path dataFolder = pluginFolder.resolve("data");
            dataFolder.toFile().mkdir();
            Files.move(pluginFolder.resolve("board.json"), dataFolder.resolve("board.json"));
            Files.move(pluginFolder.resolve("players.json"), dataFolder.resolve("players.json"));

            Path oldFactions = pluginFolder.resolve("factions.json");
            Map<String, OldMemoryFactionV0> data = this.gsonV0.fromJson(new String(Files.readAllBytes(oldFactions), StandardCharsets.UTF_8), new TypeToken<Map<String, OldMemoryFactionV0>>() {
            }.getType());
            Map<String, NewMemoryFaction> newData = new HashMap<>();
            data.forEach((id, fac) -> newData.put(id, new NewMemoryFaction(fac)));
            Files.write(dataFolder.resolve("factions.json"), this.plugin.getGson().toJson(newData).getBytes(StandardCharsets.UTF_8));

            Files.move(oldFactions, oldConfigFolder.resolve("factions.json"));
            Files.move(oldConf, oldConfigFolder.resolve("conf.json"));
            this.plugin.getLogger().info("Transition complete!");
        } catch (Exception e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not convert old conf.json", e);
        }
    }

    private void buildV0Gson() {
        Type mapFLocToStringSetType = new TypeToken<Map<FLocation, Set<String>>>() {
        }.getType();

        Type accessTypeAdatper = new TypeToken<Map<OldPermissableV0, Map<OldPermissableActionV0, OldAccessV0>>>() {
        }.getType();

        Type factionMaterialType = new TypeToken<FactionMaterial>() {
        }.getType();

        Type materialType = new TypeToken<Material>() {
        }.getType();

        this.gsonV0 = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .enableComplexMapKeySerialization()
                .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.VOLATILE)
                .registerTypeAdapter(factionMaterialType, new FactionMaterialAdapter())
                .registerTypeAdapter(materialType, new MaterialAdapter())
                .registerTypeAdapter(accessTypeAdatper, new OldPermissionsMapTypeAdapterV0())
                .registerTypeAdapter(LazyLocation.class, new MyLocationTypeAdapter())
                .registerTypeAdapter(mapFLocToStringSetType, new MapFLocToStringSetTypeAdapter())
                .registerTypeAdapterFactory(EnumTypeAdapter.ENUM_FACTORY).create();
    }
}
