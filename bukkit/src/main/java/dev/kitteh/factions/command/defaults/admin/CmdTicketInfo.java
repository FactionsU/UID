package dev.kitteh.factions.command.defaults.admin;

import com.google.gson.Gson;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.Permission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.scheduler.BukkitRunnable;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

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
import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;

public class CmdTicketInfo implements Cmd {
    @SuppressWarnings("unused")
    private static class TicketResponse {
        private boolean success;
        private String message;
    }

    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> manager.command(
                builder.literal("ticket-info")
                        .commandDescription(Cloudy.desc("Creates requested ticket info"))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.DEBUG)))
                        .flag(manager.flagBuilder("full"))
                        .handler(this::handle)
        );
    }

    @SuppressWarnings({"FieldCanBeLocal", "MismatchedQueryAndUpdateOfCollection", "unused"})
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

    private void handle(CommandContext<Sender> context) {
        AbstractFactionsPlugin plugin = AbstractFactionsPlugin.instance();
        TicketInfo info = new TicketInfo();
        info.uuid = plugin.getServerUUID();
        info.pluginVersion = plugin.getDescription().getVersion();
        info.javaVersion = System.getProperty("java.version");
        info.likesCats = plugin.likesCats;
        info.serverName = Bukkit.getName();
        info.serverVersion = Bukkit.getVersion();
        info.userName = context.sender().sender().getName();
        info.userUUID = context.sender().fPlayerOrNull() instanceof FPlayer fp ? fp.uniqueId() : null;
        try {
            info.num = FactionsPlugin.class.getDeclaredMethods().length;
        } catch (Throwable ignored) {
        }

        boolean full = context.flags().hasFlag("full");

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
                    Path dataPath = AbstractFactionsPlugin.instance().getDataFolder().toPath();
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
                                context.sender().sendMessage(Component.text().color(NamedTextColor.YELLOW).content("Share this URL: " + url).clickEvent(ClickEvent.openUrl(url)));
                                if (context.sender().isPlayer()) {
                                    AbstractFactionsPlugin.instance().getLogger().info("Share this URL: " + url);
                                }
                            } else {
                                context.sender().sendMessage(Component.text().color(NamedTextColor.RED).content("ERROR! Could not generate ticket info. See console for why."));
                                AbstractFactionsPlugin.instance().getLogger().warning("Received: " + response.message);
                            }
                        }
                    }.runTask(AbstractFactionsPlugin.instance());
                } catch (Exception e) {
                    AbstractFactionsPlugin.instance().getLogger().log(Level.SEVERE, "Failed to execute ticketinfo command", e);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            context.sender().sendMessage(Component.text().color(NamedTextColor.RED).content("ERROR! Could not generate ticket info. See console for why."));
                        }
                    }.runTask(AbstractFactionsPlugin.instance());
                }
            }
        }.runTaskAsynchronously(AbstractFactionsPlugin.instance());
        context.sender().sendMessage(Component.text().color(NamedTextColor.YELLOW).content("Now running..."));
    }
}
