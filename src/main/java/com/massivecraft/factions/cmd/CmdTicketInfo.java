package com.massivecraft.factions.cmd;

import com.google.gson.Gson;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.scheduler.BukkitRunnable;
import org.kitteh.pastegg.PasteBuilder;
import org.kitteh.pastegg.PasteContent;
import org.kitteh.pastegg.PasteFile;
import org.kitteh.pastegg.Visibility;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class CmdTicketInfo extends FCommand {
    public CmdTicketInfo() {
        super();
        this.aliases.add("ticketinfo");
        this.optionalArgs.put("full", null);

        this.requirements = new CommandRequirements.Builder(Permission.DEBUG).build();
    }

    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "MismatchedQueryAndUpdateOfCollection", "unused"})
    private static class Info {
        private UUID uuid;
        private String pluginVersion;
        private String javaVersion;
        private String serverVersion;
        private String serverName;
        private String userName;
        private boolean likesCats;
        private UUID userUUID;
        private String online;
        private int num;
        private List<PlayerInfo> permissions;
        private List<PluginInfo> plugins;

        private static class PlayerInfo {
            private static class PermInfo {
                private String node;
                private boolean has;

                public PermInfo(String node, boolean has) {
                    this.node = node;
                    this.has = has;
                }
            }

            private String name;
            private UUID uuid;
            private List<PermInfo> permissions;

            public PlayerInfo(Player player) {
                this.name = player.getName();
                this.uuid = player.getUniqueId();
                this.permissions = new ArrayList<>();
                for (Permission permission : Permission.values()) {
                    this.permissions.add(new PermInfo(permission.toString(), player.hasPermission(permission.toString())));
                }
            }
        }

        private static class PluginInfo {
            private String name;
            private String version;
            private List<String> authors;
            private List<String> depend;
            private List<String> softdepend;
            private List<String> loadBefore;
            private boolean enabled;

            PluginInfo(Plugin plugin) {
                this.name = plugin.getName();
                PluginDescriptionFile desc = plugin.getDescription();
                this.version = desc.getVersion();
                this.authors = desc.getAuthors();
                this.depend = desc.getDepend().isEmpty() ? null : desc.getDepend();
                this.softdepend = desc.getSoftDepend().isEmpty() ? null : desc.getSoftDepend();
                this.loadBefore = desc.getLoadBefore().isEmpty() ? null : desc.getLoadBefore();
                this.enabled = plugin.isEnabled();
            }
        }
    }

    @Override
    public void perform(CommandContext context) {
        FactionsPlugin plugin = FactionsPlugin.getInstance();
        Info info = new Info();
        info.uuid = plugin.getServerUUID();
        info.pluginVersion = plugin.getDescription().getVersion();
        info.javaVersion = System.getProperty("java.version");
        info.likesCats = plugin.likesCats;
        info.serverName = Bukkit.getName();
        info.serverVersion = Bukkit.getVersion();
        info.userName = context.sender.getName();
        info.userUUID = context.player == null ? null : context.player.getUniqueId();
        try {
            info.num = FactionsPlugin.class.getDeclaredMethods().length;
        } catch (Throwable ignored) {
        }

        Audience audience = plugin.getAdventure().sender(context.sender);

        boolean full = context.argAsString(0, "").equalsIgnoreCase("full");

        if (full) {
            info.plugins = new ArrayList<>();
            for (Plugin plug : Bukkit.getPluginManager().getPlugins()) {
                info.plugins.add(new Info.PluginInfo(plug));
            }

            if (!Bukkit.getOnlinePlayers().isEmpty()) {
                info.permissions = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    info.permissions.add(new Info.PlayerInfo(player));
                }
            }
        }

        new BukkitRunnable() {
            private final PasteBuilder builder = new PasteBuilder().name("FactionsUUID Ticket Info")
                    .visibility(Visibility.UNLISTED)
                    .expires(ZonedDateTime.now(ZoneOffset.UTC).plusDays(7));

            private void add(String name, String content) {
                builder.addFile(new PasteFile(name, new PasteContent(PasteContent.ContentType.TEXT, content)));
            }

            private String getFile(Path file) {
                try {
                    return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    return ExceptionUtils.getFullStackTrace(e);
                }
            }

            @Override
            public void run() {
                try {
                    Path dataPath = FactionsPlugin.getInstance().getDataFolder().toPath();
                    String spigotConf = getFile(Paths.get("spigot.yml"));
                    info.online = Boolean.toString(Bukkit.getOnlineMode());
                    if (!Bukkit.getOnlineMode()) {
                        for (String line : spigotConf.split("\n")) {
                            if (line.contains("bungeecord") && line.contains("true")) {
                                info.online = "Bungee";
                                break;
                            }
                        }
                    }
                    add("info.json", new Gson().toJson(info));
                    if (full) {
                        add("startup.txt", plugin.getStartupLog());
                        if (!plugin.getStartupExceptionLog().isEmpty()) {
                            add("startupexceptions.txt", plugin.getStartupExceptionLog());
                        }
                        add("main.conf", getFile(dataPath.resolve("config/main.conf")));
                    }

                    PasteBuilder.PasteResult result = builder.build();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (result.getPaste().isPresent()) {
                                String delKey = result.getPaste().get().getDeletionKey().orElse("No deletion key");
                                audience.sendMessage(Component.text().color(NamedTextColor.YELLOW).content("Share this URL: https://info.factions.support/" + result.getPaste().get().getId()));
                                if (context.sender instanceof Player) {
                                    FactionsPlugin.getInstance().getLogger().info("Share this URL: https://info.factions.support/" + result.getPaste().get().getId());
                                }
                            } else {
                                audience.sendMessage(Component.text().color(NamedTextColor.RED).content("ERROR! Could not generate ticket info. See console for why."));
                                FactionsPlugin.getInstance().getLogger().warning("Received: " + result.getMessage());
                            }
                        }
                    }.runTask(FactionsPlugin.getInstance());
                } catch (Exception e) {
                    FactionsPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to execute ticketinfo command", e);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            audience.sendMessage(Component.text().color(NamedTextColor.RED).content("ERROR! Could not generate ticket info. See console for why."));
                        }
                    }.runTask(FactionsPlugin.getInstance());
                }
            }
        }.runTaskAsynchronously(FactionsPlugin.getInstance());
        audience.sendMessage(Component.text().color(NamedTextColor.YELLOW).content("Now running..."));
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_TICKETINFO_DESCRIPTION;
    }
}
