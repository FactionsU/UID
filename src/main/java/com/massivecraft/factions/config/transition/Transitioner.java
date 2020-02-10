package com.massivecraft.factions.config.transition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.config.Loader;
import com.massivecraft.factions.config.transition.oldclass.v0.NewMemoryFaction;
import com.massivecraft.factions.config.transition.oldclass.v0.OldAccessV0;
import com.massivecraft.factions.config.transition.oldclass.v0.OldConfV0;
import com.massivecraft.factions.config.transition.oldclass.v0.OldMemoryFactionV0;
import com.massivecraft.factions.config.transition.oldclass.v0.OldPermissableActionV0;
import com.massivecraft.factions.config.transition.oldclass.v0.OldPermissableV0;
import com.massivecraft.factions.config.transition.oldclass.v0.OldPermissionsMapTypeAdapterV0;
import com.massivecraft.factions.config.transition.oldclass.v0.TransitionConfigV0;
import com.massivecraft.factions.config.transition.oldclass.v1.OldMainConfigV1;
import com.massivecraft.factions.config.transition.oldclass.v1.TransitionConfigV1;
import com.massivecraft.factions.util.EnumTypeAdapter;
import com.massivecraft.factions.util.LazyLocation;
import com.massivecraft.factions.util.MapFLocToStringSetTypeAdapter;
import com.massivecraft.factions.util.MyLocationTypeAdapter;
import com.massivecraft.factions.util.material.FactionMaterial;
import com.massivecraft.factions.util.material.adapter.FactionMaterialAdapter;
import com.massivecraft.factions.util.material.adapter.MaterialAdapter;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.bukkit.Material;

import java.io.File;
import java.io.IOException;
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

    public static void transition(FactionsPlugin plugin) {
        Transitioner transitioner = new Transitioner(plugin);
        transitioner.migrateV0();

        Path confPath = plugin.getDataFolder().toPath().resolve("config").resolve("main.conf");
        if (!confPath.toFile().exists()) {
            return;
        }

        HoconConfigurationLoader loader = Loader.getLoader("main");
        try {
            CommentedConfigurationNode rootNode = loader.load();
            CommentedConfigurationNode versionNode = rootNode.getNode("aVeryFriendlyFactionsConfig").getNode("version");

            if (versionNode.isVirtual()) {
                transitioner.migrateV1(loader);
                rootNode = loader.load();
                versionNode = rootNode.getNode("aVeryFriendlyFactionsConfig").getNode("version");
                if (versionNode.isVirtual()) {
                    return; // Failure!
                }
            }

            int version = rootNode.getNode("aVeryFriendlyFactionsConfig").getNode("version").getInt();
            if (version < 3) {
                transitioner.migrateV2(rootNode);
            }

            loader.save(rootNode);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save configuration migration! Data may be lost, requiring restoration from backups.", e);
        }
    }

    private Transitioner(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    private void migrateV0() {
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
            Loader.loadAndSave("main", newConfig);
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

    private void migrateV1(HoconConfigurationLoader loader) {
        Path pluginFolder = this.plugin.getDataFolder().toPath();
        Path configPath = pluginFolder.resolve("config.yml");
        Path oldConfigFolder = pluginFolder.resolve("oldConfig");
        if (!configPath.toFile().exists()) {
            this.plugin.getLogger().warning("Found a main.conf from before 0.5.4 but no config.yml was found! Might lose some config information!");
            return;
        }
        try {
            OldMainConfigV1 oldConf = new OldMainConfigV1();
            Loader.load(loader, oldConf);
            TransitionConfigV1 newConf = new TransitionConfigV1();
            Loader.load(loader, newConf);
            newConf.update(oldConf, this.plugin.getConfig());
            Loader.loadAndSave(loader, newConf);
            if (!oldConfigFolder.toFile().exists()) {
                oldConfigFolder.toFile().mkdir();
            }
            Files.move(configPath, oldConfigFolder.resolve("config.yml"));
        } catch (Exception e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not migrate configuration", e);
        }
    }

    private void migrateV2(CommentedConfigurationNode node) {
        node.getNode("factions").getNode("enterTitles").getNode("title").setValue("");
        node.getNode("factions").getNode("enterTitles").getNode("subtitle").setValue("{faction-relation-color}{faction}");
        node.getNode("aVeryFriendlyFactionsConfig").getNode("version").setValue(3);
        node.getNode("scoreboard").getNode("constant").getNode("factionlessTitle").setValue(node.getNode("scoreboard").getNode("constant").getNode("title").getString());

        this.plugin.getLogger().info("Detected a config from before 0.5.7");
        this.plugin.getLogger().info("  Setting default enterTitles settings based on old style. Visit main.conf to edit.");
        this.plugin.getLogger().info("  Setting default constant scoreboard factionlessTitle settings based on normal title. Visit main.conf to edit.");
    }
}
