package com.massivecraft.factions.config.transition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.config.Loader;
import com.massivecraft.factions.config.transition.oldclass.v0.MaterialAdapter;
import com.massivecraft.factions.config.transition.oldclass.v0.NewMemoryFaction;
import com.massivecraft.factions.config.transition.oldclass.v0.OldConfV0;
import com.massivecraft.factions.config.transition.oldclass.v0.OldMemoryFactionV0;
import com.massivecraft.factions.config.transition.oldclass.v0.TransitionConfigV0;
import com.massivecraft.factions.config.transition.oldclass.v1.OldMainConfigV1;
import com.massivecraft.factions.config.transition.oldclass.v1.TransitionConfigV1;
import com.massivecraft.factions.perms.PermSelectorTypeAdapter;
import com.massivecraft.factions.util.EnumTypeAdapter;
import com.massivecraft.factions.util.LazyLocation;
import com.massivecraft.factions.util.MapFLocToStringSetTypeAdapter;
import com.massivecraft.factions.util.MyLocationTypeAdapter;
import com.massivecraft.factions.util.TL;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.bukkit.Material;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class Transitioner {
    private final FactionsPlugin plugin;
    private Gson gsonV0;

    public static void transition(FactionsPlugin plugin) {
        Transitioner transitioner = new Transitioner(plugin);
        transitioner.migrateV0();
        transitioner.migrateV5A();

        Path confPath = plugin.getDataFolder().toPath().resolve("config").resolve("main.conf");
        if (!Files.exists(confPath)) {
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
            if (version < 4) {
                transitioner.migrateV3(rootNode);
            }
            if (version < 5) {
                transitioner.migrateV4(rootNode);
            }
            if (version < 6) {
                transitioner.migrateV5B(rootNode);
            }
            // Why do a version bump for this?
            CommentedConfigurationNode factId = rootNode.getNode("factions").getNode("other").getNode("newPlayerStartingFactionID");
            if (!factId.isVirtual()) {
                if (factId.getValue() instanceof String facIdS) {
                    try {
                        factId.setValue(Integer.parseInt(facIdS));
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Failed to migrate new player starting faction ID to numeric ID. Found: " + facIdS);
                    }
                }
            }

            loader.save(rootNode);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save configuration migration! Data may be lost, requiring restoration from backups.", e);
        }
    }

    private final Path pluginFolder;
    private final Path configFolder;
    private final Path oldConfigFolder;

    private Transitioner(FactionsPlugin plugin) {
        this.plugin = plugin;
        this.pluginFolder = this.plugin.getDataFolder().toPath();
        configFolder = pluginFolder.resolve("config");
        oldConfigFolder = pluginFolder.resolve("oldConfig");
    }

    private void migrateV0() {
        if (Files.exists(configFolder)) {
            // Found existing config, so nothing to do here.
            return;
        }
        Path oldConf = pluginFolder.resolve("conf.json");
        if (!Files.exists(oldConf)) {
            // No config, no conversion!
            return;
        }
        if (Files.exists(oldConfigFolder)) {
            // Found existing oldConfig, implying it was already upgraded once
            this.plugin.getLogger().warning("Found no 'config' folder, but an 'oldConfig' exists. Not attempting conversion.");
            return;
        }
        this.plugin.getLogger().info("Found no 'config' folder. Starting configuration transition...");
        this.buildV0Gson();
        try {
            OldConfV0 conf = this.gsonV0.fromJson(Files.readString(oldConf), OldConfV0.class);
            TransitionConfigV0 newConfig = new TransitionConfigV0(conf);
            Loader.loadAndSave("main", newConfig);
            Files.createDirectories(oldConfigFolder);
            Path dataFolder = pluginFolder.resolve("data");
            Files.createDirectories(dataFolder);
            Files.move(pluginFolder.resolve("board.json"), dataFolder.resolve("board.json"));
            Files.move(pluginFolder.resolve("players.json"), dataFolder.resolve("players.json"));

            Path oldFactions = pluginFolder.resolve("factions.json");
            Map<String, OldMemoryFactionV0> data = this.gsonV0.fromJson(Files.readString(oldFactions), new TypeToken<Map<String, OldMemoryFactionV0>>() {
            }.getType());
            Map<String, NewMemoryFaction> newData = new HashMap<>();
            data.forEach((id, fac) -> newData.put(id, new NewMemoryFaction(fac)));
            Files.writeString(dataFolder.resolve("factions.json"), this.plugin.getGson().toJson(newData));

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

        Type materialType = new TypeToken<Material>() {
        }.getType();

        this.gsonV0 = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .enableComplexMapKeySerialization()
                .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.VOLATILE)
                .registerTypeAdapter(materialType, new MaterialAdapter())
                .registerTypeAdapter(LazyLocation.class, new MyLocationTypeAdapter())
                .registerTypeAdapter(mapFLocToStringSetType, new MapFLocToStringSetTypeAdapter())
                .registerTypeAdapterFactory(EnumTypeAdapter.ENUM_FACTORY).create();
    }

    private void migrateV1(HoconConfigurationLoader loader) {
        Path pluginFolder = this.plugin.getDataFolder().toPath();
        Path configPath = pluginFolder.resolve("config.yml");
        Path oldConfigFolder = pluginFolder.resolve("oldConfig");
        if (!Files.exists(configPath)) {
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
            Files.createDirectories(oldConfigFolder);
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

    private void migrateV3(CommentedConfigurationNode node) {
        node.getNode("scoreboard").getNode("constant").getNode("prefixTemplate").setValue(TL.DEFAULT_PREFIX.toString());
        node.getNode("aVeryFriendlyFactionsConfig").getNode("version").setValue(4);

        this.plugin.getLogger().info("Detected a config from before 0.5.14");
        this.plugin.getLogger().info("  1. Setting default scoreboard prefixTemplate based on lang.yml default-prefix setting.");
        this.plugin.getLogger().info("  2. Be aware that \"warZonePreventMonsterSpawns\" has been removed entirely, and there is");
        this.plugin.getLogger().info("     now a new spawning control system added.");
        this.plugin.getLogger().info("  3. The perms gui hover text can now be customized in config, and the allow/deny/locked text");
        this.plugin.getLogger().info("     can now be customized in lang.yml under GUI->PERMS->ACTION");
    }

    private void migrateV4(CommentedConfigurationNode node) {
        node.getNode("aVeryFriendlyFactionsConfig").getNode("version").setValue(5);

        boolean update = node.getNode("factions").getNode("spawning").getNode("updateAutomatically").getBoolean(true);

        out:
        if (update) {
            CommentedConfigurationNode exNode = node.getNode("factions").getNode("spawning").getNode("preventSpawningInSafezoneExceptions");
            if (exNode.isVirtual()) {
                break out;
            }
            List<String> list = new ArrayList<>(exNode.getList(Object::toString));
            list.add("AXOLOTL");
            list.add("GLOW_SQUID");
            node.getNode("factions").getNode("spawning").getNode("preventSpawningInSafezoneExceptions").setValue(list);
        }

        this.plugin.getLogger().info("Detected a config from before 0.5.24 (which adds 1.17 entities)");
        if (update) {
            this.plugin.getLogger().info("  Because you had auto updating enabled, added AXOLOTL and GLOW_SQUID to the safe zone spawning exception list.");
        } else {
            this.plugin.getLogger().info("  If you had auto updating enabled, this would have added AXOLOTL and GLOW_SQUID to the safe zone spawning exception list.");
        }
        this.plugin.getLogger().info("  We chose not to add GOAT due to its affection for ramming.");
    }

    private void migrateV5A() {
        Path defPermPath = configFolder.resolve("default_permissions.conf");
        Path defPermOffPath = configFolder.resolve("default_permissions_offline.conf");
        boolean defExists = Files.exists(defPermPath);
        boolean defOffExists = Files.exists(defPermOffPath);
        if ((defExists || defOffExists)) {
            this.plugin.getLogger().info("Detected now-unused default permissions files.");
            if (!Files.exists(oldConfigFolder)) {
                try {
                    Files.createDirectories(oldConfigFolder);
                } catch (IOException e) {
                    this.plugin.getLogger().log(Level.WARNING, "Failed to create oldConfig folder!", e);
                    return;
                }
            }
        }
        if (defExists) {
            try {
                Files.move(defPermPath, oldConfigFolder.resolve("default_permissions.conf"));
                this.plugin.getLogger().info("  Moved default_permissions.conf to oldConfig");
            } catch (IOException e) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to move old default_permissions.conf to oldConfig folder!", e);
            }
        }
        if (defOffExists) {
            try {
                Files.move(defPermOffPath, oldConfigFolder.resolve("default_permissions_offline.conf"));
                this.plugin.getLogger().info("  Moved default_permissions_offline.conf to oldConfig");
            } catch (IOException e) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to move old default_permissions_offline.conf to oldConfig folder!", e);
            }
        }
    }

    private void migrateV5B(CommentedConfigurationNode node) {
        node.getNode("aVeryFriendlyFactionsConfig").getNode("version").setValue(6);

        this.plugin.getLogger().info("");
        this.plugin.getLogger().info("              !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        this.plugin.getLogger().info("");
        this.plugin.getLogger().info("You are upgrading from a version prior to 0.6.0, so please read closely.");
        this.plugin.getLogger().info("  Faction permissions (/f perms) have changed significantly.");
        this.plugin.getLogger().info("  Permissions in already created factions are preserved, but if you modified the");
        this.plugin.getLogger().info("    default_permissions files you will need to set those again.");
        this.plugin.getLogger().info("  Your old default entries in the default_permissions files are not migrated.");
        this.plugin.getLogger().info("    The original defaults have been restored to the new format.");
        this.plugin.getLogger().info("  'Locked' actions have been replaced with 'override' settings. You will need to");
        this.plugin.getLogger().info("    set up new 'override' settings, if you had any locked actions before.");
        this.plugin.getLogger().info("  See the new file config/permissions.conf for more,");
        this.plugin.getLogger().info("    and be sure to read the changelog for this release.");
        this.plugin.getLogger().info("");
        this.plugin.getLogger().info("              !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        this.plugin.getLogger().info("");

        PermSelectorTypeAdapter.setLegacy();
    }
}
