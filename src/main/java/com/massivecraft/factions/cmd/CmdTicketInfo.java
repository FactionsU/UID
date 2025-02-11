package com.massivecraft.factions.cmd;

import com.google.gson.Gson;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;

public class CmdTicketInfo extends FCommand {
    @SuppressWarnings("unused")
    private static class TicketResponse {
        private boolean success;
        private String message;
    }

    public CmdTicketInfo() {
        super();
        this.aliases.add("ticketinfo");
        this.optionalArgs.put("full", null);

        this.requirements = new CommandRequirements.Builder(Permission.DEBUG).build();
    }

    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "MismatchedQueryAndUpdateOfCollection", "unused"})
    private static class TicketInfo {
        private final String type = "FUUID";
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
        private String startup;
        private String exceptions;
        private String mainconf;

        private static class PlayerInfo {
            private static class PermInfo {
                private final String node;
                private final boolean has;

                public PermInfo(String node, boolean has) {
                    this.node = node;
                    this.has = has;
                }
            }

            private final String name;
            private final UUID uuid;
            private final List<PermInfo> permissions;

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
            private final String name;
            private final String version;
            private final List<String> authors;
            private final List<String> depend;
            private final List<String> softdepend;
            private final List<String> loadBefore;
            private final boolean enabled;

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
        TicketInfo info = new TicketInfo();
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
                info.plugins.add(new TicketInfo.PluginInfo(plug));
            }

            if (!Bukkit.getOnlinePlayers().isEmpty()) {
                info.permissions = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    info.permissions.add(new TicketInfo.PlayerInfo(player));
                }
            }
        }

        new BukkitRunnable() {
            private String getFile(Path file) {
                try {
                    return Files.readString(file);
                } catch (IOException e) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter, true);
                    e.printStackTrace(printWriter);
                    return stringWriter.getBuffer().toString();
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
                    if (full) {
                        info.startup = plugin.getStartupLog();
                        if (!plugin.getStartupExceptionLog().isEmpty()) {
                            info.exceptions = plugin.getStartupExceptionLog();
                        }
                        info.mainconf = getFile(dataPath.resolve("config/main.conf"));
                    }

                    Gson gson = new Gson();
                    String string = gson.toJson(info);
                    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                    GZIPOutputStream gzip = new GZIPOutputStream(byteStream);
                    gzip.write(string.getBytes(StandardCharsets.UTF_8));
                    gzip.finish();
                    byte[] bytes = byteStream.toByteArray();
                    URL url = new URI("https://ticket.plugin.party/ticket").toURL();
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(3000);
                    connection.setReadTimeout(3000);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/octet-stream");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setDoOutput(true);
                    try (OutputStream os = connection.getOutputStream()) {
                        os.write(bytes, 0, bytes.length);
                    }
                    StringBuilder content = new StringBuilder();
                    try (
                            InputStream stream = connection.getInputStream();
                            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                            BufferedReader in = new BufferedReader(reader)) {
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }
                    }
                    TicketResponse response = gson.fromJson(content.toString(), TicketResponse.class);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (response.success) {
                                String url = response.message;
                                audience.sendMessage(Component.text().color(NamedTextColor.YELLOW).content("Share this URL: " + url).clickEvent(ClickEvent.openUrl(url)));
                                if (context.sender instanceof Player) {
                                    FactionsPlugin.getInstance().getLogger().info("Share this URL: " + url);
                                }
                            } else {
                                audience.sendMessage(Component.text().color(NamedTextColor.RED).content("ERROR! Could not generate ticket info. See console for why."));
                                FactionsPlugin.getInstance().getLogger().warning("Received: " + response.message);
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
