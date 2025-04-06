package dev.kitteh.factions.config.transition;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.config.Loader;
import dev.kitteh.factions.util.adapter.PermSelectorTypeAdapter;
import dev.kitteh.factions.util.TL;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Transitioner {
    private final FactionsPlugin plugin;

    public static void transition(FactionsPlugin plugin) {
        Transitioner transitioner = new Transitioner(plugin);
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
                transitioner.migrateV5B();
            }
            if (version < 7) {
                transitioner.migrateV6(rootNode);
            }

            // Update the below when bumping version!
            rootNode.getNode("aVeryFriendlyFactionsConfig").getNode("version").setValue(7);

            loader.save(rootNode);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save configuration migration! Data may be lost, requiring restoration from backups.", e);
        }
    }

    private final Path configFolder;
    private final Path oldConfigFolder;

    private Transitioner(FactionsPlugin plugin) {
        this.plugin = plugin;
        Path pluginFolder = this.plugin.getDataFolder().toPath();
        configFolder = pluginFolder.resolve("config");
        oldConfigFolder = pluginFolder.resolve("oldConfig");
    }

    private void migrateV2(CommentedConfigurationNode node) {
        node.getNode("factions").getNode("enterTitles").getNode("title").setValue("");
        node.getNode("factions").getNode("enterTitles").getNode("subtitle").setValue("{faction-relation-color}{faction}");
        node.getNode("scoreboard").getNode("constant").getNode("factionlessTitle").setValue(node.getNode("scoreboard").getNode("constant").getNode("title").getString());

        this.plugin.getLogger().info("Detected a config from before 0.5.7");
        this.plugin.getLogger().info("  Setting default enterTitles settings based on old style. Visit main.conf to edit.");
        this.plugin.getLogger().info("  Setting default constant scoreboard factionlessTitle settings based on normal title. Visit main.conf to edit.");
    }

    private void migrateV3(CommentedConfigurationNode node) {
        node.getNode("scoreboard").getNode("constant").getNode("prefixTemplate").setValue(TL.DEFAULT_PREFIX.toString());

        this.plugin.getLogger().info("Detected a config from before 0.5.14");
        this.plugin.getLogger().info("  1. Setting default scoreboard prefixTemplate based on lang.yml default-prefix setting.");
        this.plugin.getLogger().info("  2. Be aware that \"warZonePreventMonsterSpawns\" has been removed entirely, and there is");
        this.plugin.getLogger().info("     now a new spawning control system added.");
        this.plugin.getLogger().info("  3. The perms gui hover text can now be customized in config, and the allow/deny/locked text");
        this.plugin.getLogger().info("     can now be customized in lang.yml under GUI->PERMS->ACTION");
    }

    private void migrateV4(CommentedConfigurationNode node) {
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
                    this.plugin.getLogger().log(Level.WARNING, "  Failed to create oldConfig folder!", e);
                    return;
                }
            }
        }
        if (defExists) {
            try {
                Files.move(defPermPath, oldConfigFolder.resolve("default_permissions.conf"));
                this.plugin.getLogger().info("  Moved default_permissions.conf to oldConfig");
            } catch (IOException e) {
                this.plugin.getLogger().log(Level.WARNING, "  Failed to move old default_permissions.conf to oldConfig folder!", e);
            }
        }
        if (defOffExists) {
            try {
                Files.move(defPermOffPath, oldConfigFolder.resolve("default_permissions_offline.conf"));
                this.plugin.getLogger().info("  Moved default_permissions_offline.conf to oldConfig");
            } catch (IOException e) {
                this.plugin.getLogger().log(Level.WARNING, "  Failed to move old default_permissions_offline.conf to oldConfig folder!", e);
            }
        }
    }

    private void migrateV5B() {
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

    private void migrateV6(CommentedConfigurationNode node) {
        CommentedConfigurationNode factId = node.getNode("factions").getNode("other").getNode("newPlayerStartingFactionID");
        if (!factId.isVirtual()) {
            if (factId.getValue() instanceof String facIdS) {
                try {
                    factId.setValue(Integer.parseInt(facIdS));
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Failed to migrate new player starting faction ID to numeric ID. Found: " + facIdS);
                }
            }
        }

        boolean startingChat = node.getNode("factions").getNode("chat").getNode("factionOnlyChat").getBoolean(true);
        CommentedConfigurationNode internalChat = node.getNode("factions").getNode("chat").getNode("internalChat");
        internalChat.getNode("factionMemberChatEnabled").setValue(startingChat);
        internalChat.getNode("relationChatEnabled").setValue(startingChat);
    }
}
