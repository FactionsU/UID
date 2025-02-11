package com.massivecraft.factions;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.massivecraft.factions.cmd.FCmdRoot;
import com.massivecraft.factions.config.ConfigManager;
import com.massivecraft.factions.config.file.MainConfig;
import com.massivecraft.factions.config.file.TranslationsConfig;
import com.massivecraft.factions.data.SaveTask;
import com.massivecraft.factions.event.FactionCreateEvent;
import com.massivecraft.factions.event.FactionEvent;
import com.massivecraft.factions.event.FactionRelationEvent;
import com.massivecraft.factions.event.FactionsPluginRegistrationTimeEvent;
import com.massivecraft.factions.integration.ClipPlaceholderAPIManager;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.integration.Essentials;
import com.massivecraft.factions.integration.IntegrationManager;
import com.massivecraft.factions.integration.LWC;
import com.massivecraft.factions.integration.LuckPerms;
import com.massivecraft.factions.integration.VaultPerms;
import com.massivecraft.factions.integration.Worldguard;
import com.massivecraft.factions.integration.dynmap.EngineDynmap;
import com.massivecraft.factions.integration.permcontext.ContextManager;
import com.massivecraft.factions.landraidcontrol.LandRaidControl;
import com.massivecraft.factions.listeners.FactionsBlockListener;
import com.massivecraft.factions.listeners.FactionsChatListener;
import com.massivecraft.factions.listeners.FactionsEntityListener;
import com.massivecraft.factions.listeners.FactionsExploitListener;
import com.massivecraft.factions.listeners.FactionsPlayerListener;
import com.massivecraft.factions.listeners.versionspecific.PortalListener_114;
import com.massivecraft.factions.perms.PermSelector;
import com.massivecraft.factions.perms.PermSelectorRegistry;
import com.massivecraft.factions.perms.PermSelectorTypeAdapter;
import com.massivecraft.factions.perms.PermissibleActionRegistry;
import com.massivecraft.factions.struct.ChatMode;
import com.massivecraft.factions.util.AutoLeaveTask;
import com.massivecraft.factions.util.EnumTypeAdapter;
import com.massivecraft.factions.util.FlightUtil;
import com.massivecraft.factions.util.LazyLocation;
import com.massivecraft.factions.util.MapFLocToStringSetTypeAdapter;
import com.massivecraft.factions.util.Metrics;
import com.massivecraft.factions.util.MyLocationTypeAdapter;
import com.massivecraft.factions.util.PermUtil;
import com.massivecraft.factions.util.Persist;
import com.massivecraft.factions.util.SeeChunkUtil;
import com.massivecraft.factions.util.TL;
import com.massivecraft.factions.util.TextUtil;
import com.massivecraft.factions.util.WorldUtil;
import com.massivecraft.factions.util.material.MaterialDb;
import com.massivecraft.factions.util.particle.BukkitParticleProvider;
import com.mojang.authlib.GameProfile;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FactionsPlugin extends JavaPlugin implements FactionsAPI {

    // Our single plugin instance.
    // Single 4 life.
    private static FactionsPlugin instance;
    private static int mcVersion;
    private static final int OLDEST_MODERN_SUPPORTED = 2004; // 1.20.4
    private static final String OLDEST_MODERN_SUPPORTED_STRING = "1.20.4";

    public static FactionsPlugin getInstance() {
        return instance;
    }

    public static int getMCVersion() {
        return mcVersion;
    }

    private ConfigManager configManager;

    private Integer saveTask = null;
    private boolean autoSave = true;
    private boolean loadSuccessful = false;

    // Some utils
    private Persist persist;
    private TextUtil txt;
    private WorldUtil worldUtil;

    public TextUtil txt() {
        return txt;
    }

    public WorldUtil worldUtil() {
        return worldUtil;
    }

    public void grumpException(RuntimeException e) {
        this.grumpyExceptions.add(e);
    }

    private PermUtil permUtil;

    // Persist related
    private Gson gson;

    // holds f stuck start times
    private final Map<UUID, Long> timers = new HashMap<>();

    //holds f stuck taskids
    private final Map<UUID, Integer> stuckMap = new HashMap<>();

    // Persistence related
    private boolean locked = false;

    private Integer autoLeaveTask = null;

    private ClipPlaceholderAPIManager clipPlaceholderAPIManager;
    private boolean mvdwPlaceholderAPIManager = false;
    private final Set<String> pluginsHandlingChat = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private SeeChunkUtil seeChunkUtil;
    private BukkitParticleProvider particleProvider;
    private Worldguard worldguard;
    private LandRaidControl landRaidControl;
    private boolean luckPermsSetup;
    private IntegrationManager integrationManager;

    private Metrics metrics;
    private final Pattern factionsVersionPattern = Pattern.compile("b(\\d{1,4})");
    private UUID serverUUID;
    private String startupLog = "NOTFINISHED";
    private String startupExceptionLog = "NOTFINISHED";
    private final List<RuntimeException> grumpyExceptions = new ArrayList<>();
    private VaultPerms vaultPerms;
    public final boolean likesCats = Arrays.stream(FactionsPlugin.class.getDeclaredMethods()).anyMatch(m -> m.isSynthetic() && m.getName().startsWith("loadCon") && m.getName().endsWith("0"));
    private Method getOffline;
    private BukkitAudiences adventure;
    private String mcVersionString;
    private String updateCheck;
    private Response updateResponse;

    public FactionsPlugin() {
        instance = this;
    }

    // Everything is pain.
    @Override
    public void onLoad() {
        IntegrationManager.onLoad(this);
        try {
            Class.forName("com.sk89q.worldguard.WorldGuard");
            Worldguard.onLoad();
        } catch (Exception ignored) {
            // eh
        }
    }

    @Override
    public void onEnable() {
        this.loadSuccessful = false;
        this.adventure = BukkitAudiences.create(this);
        StringBuilder startupBuilder = new StringBuilder();
        StringBuilder startupExceptionBuilder = new StringBuilder();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record.getMessage() != null && record.getMessage().contains("Loaded class {0}")) {
                    return;
                }
                startupBuilder.append('[').append(record.getLevel().getName()).append("] ").append(record.getMessage()).append('\n');
                if (record.getThrown() != null) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    record.getThrown().printStackTrace(printWriter);
                    startupExceptionBuilder.append('[').append(record.getLevel().getName()).append("] ").append(record.getMessage()).append('\n')
                            .append(stringWriter).append('\n');
                }
            }

            @Override
            public void flush() {

            }

            @Override
            public void close() throws SecurityException {

            }
        };
        getLogger().addHandler(handler);
        getLogger().info("=== Starting up! ===");
        long timeEnableStart = System.currentTimeMillis();

        if (!this.grumpyExceptions.isEmpty()) {
            this.grumpyExceptions.forEach(e -> getLogger().log(Level.WARNING, "Found issue with plugin touching FactionsUUID before it starts up!", e));
        }

        UpdateCheck update = new UpdateCheck("FactionsUUID", this.getDescription().getVersion(), this.getServer().getName(), this.getServer().getVersion());
        update.meow = this.getClass().getDeclaredMethods().length;
        // Ensure basefolder exists!
        this.getDataFolder().mkdirs();

        byte[] m = Bukkit.getMotd().getBytes(StandardCharsets.UTF_8);
        if (m.length == 0) {
            m = new byte[]{0x6b, 0x69, 0x74, 0x74, 0x65, 0x6e};
        }
        int u = intOr(update.spigotId = "%%__USER__%%", 987654321), n = intOr("%%__NONCE__%%", 1234567890), x = 0, p = Math.min(Bukkit.getMaxPlayers(), 65535);
        long ms = (0x4fac & 0xffffL);
        if (n != 1234567890) {
            ms += (n & 0xffffffffL) << 32;
            x = 4;
        }
        for (int i = 0; x < 6; i++, x++) {
            if (i == m.length) {
                i = 0;
            }
            ms += ((m[i] & 0xFFL) << (8 + (8 * (6 - x))));
        }
        this.serverUUID = new UUID(ms, ((0xaf & 0xffL) << 56) + ((0xac & 0xffL) << 48) + (u & 0xffffffffL) + ((p & 0xffffL) << 32));

        // Version party
        Pattern versionPattern = Pattern.compile("1\\.(\\d{1,2})(?:\\.(\\d{1,2}))?");
        Matcher versionMatcher = versionPattern.matcher(this.getServer().getVersion());

        getLogger().info("");
        getLogger().info("Factions UUID!");
        getLogger().info("Version " + this.getDescription().getVersion());
        getLogger().info("");
        getLogger().info("Need support? https://factions.support/help/");
        getLogger().info("");
        Integer versionInteger = null;
        if (versionMatcher.find()) {
            try {
                int minor = Integer.parseInt(versionMatcher.group(1));
                String patchS = versionMatcher.group(2);
                int patch = (patchS == null || patchS.isEmpty()) ? 0 : Integer.parseInt(patchS);
                versionInteger = (minor * 100) + patch;
                this.mcVersionString = "1." + minor + (patchS == null ? "" : ('.' + patchS));
                getLogger().info("Detected Minecraft " + versionMatcher.group());
            } catch (NumberFormatException ignored) {
            }
        }
        if (versionInteger == null) {
            getLogger().warning("");
            getLogger().warning("Could not identify version. Going with least supported version, " + OLDEST_MODERN_SUPPORTED_STRING + ".");
            getLogger().warning("Please visit our support live chat for help - https://factions.support/help/");
            getLogger().warning("");
            versionInteger = OLDEST_MODERN_SUPPORTED;
            this.mcVersionString = this.getServer().getVersion();
        }
        mcVersion = versionInteger;
        if (mcVersion < OLDEST_MODERN_SUPPORTED) {
            getLogger().info("");
            getLogger().warning("FactionsUUID expects at least " + OLDEST_MODERN_SUPPORTED_STRING + " and may not work on your version.");
        }
        getLogger().info("");

        this.getLogger().info("Server UUID " + this.serverUUID);

        loadLang();

        this.gson = this.getGsonBuilder(true).create();
        // Load Conf from disk
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfigs();
        this.gson = this.getGsonBuilder(false).create();

        if (this.conf().data().json().useEfficientStorage()) {
            getLogger().info("Using space efficient (less readable) storage.");
        }

        this.landRaidControl = LandRaidControl.getByName(this.conf().factions().landRaidControl().getSystem());

        File dataFolder = new File(this.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        // Load Material database
        MaterialDb.load();

        // Create Utility Instances
        this.permUtil = new PermUtil(this);
        this.persist = new Persist(this);
        this.worldUtil = new WorldUtil(this);

        this.txt = new TextUtil(mcVersion < 1600);
        initTXT();

        // attempt to get first command defined in plugin.yml as reference command, if any commands are defined in there
        // reference command will be used to prevent "unknown command" console messages
        String refCommand = "";
        try {
            Map<String, Map<String, Object>> refCmd = this.getDescription().getCommands();
            if (refCmd != null && !refCmd.isEmpty()) {
                refCommand = (String) (refCmd.keySet().toArray()[0]);
            }
        } catch (ClassCastException ignored) {
        }

        // Register recurring tasks
        if (saveTask == null && this.conf().factions().other().getSaveToFileEveryXMinutes() > 0.0) {
            long saveTicks = (long) (20 * 60 * this.conf().factions().other().getSaveToFileEveryXMinutes()); // Approximately every 30 min by default
            saveTask = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new SaveTask(this), saveTicks, saveTicks);
        }

        int loadedPlayers = FPlayers.getInstance().load();
        int loadedFactions = Factions.getInstance().load();
        for (FPlayer fPlayer : FPlayers.getInstance().getAllFPlayers()) {
            Faction faction = Factions.getInstance().getFactionById(fPlayer.getFactionIntId());
            if (faction == null) {
                log("Invalid faction id on " + fPlayer.getName() + ":" + fPlayer.getFactionIntId());
                fPlayer.resetFactionData(false);
                continue;
            }
            faction.addFPlayer(fPlayer);
        }
        int loadedClaims = Board.getInstance().load();
        Board.getInstance().clean();
        FactionsPlugin.getInstance().getLogger().info("Loaded " + loadedPlayers + " players in " + loadedFactions + " factions with " + loadedClaims + " claims");

        // Add Base Commands
        FCmdRoot cmdBase = new FCmdRoot();

        ContextManager.init(this);
        if (getServer().getPluginManager().getPlugin("PermissionsEx") != null) {
            getLogger().info(" ");
            getLogger().warning("Notice: PermissionsEx dead. We suggest using LuckPerms. https://luckperms.net/");
            getLogger().info(" ");
        }
        if (getServer().getPluginManager().getPlugin("GroupManager") != null) {
            getLogger().info(" ");
            getLogger().warning("Notice: GroupManager died in 2014. We suggest using LuckPerms instead. https://luckperms.net/");
            getLogger().info(" ");
        }
        Plugin lwc = getServer().getPluginManager().getPlugin("LWC");
        if (lwc != null && lwc.getDescription().getWebsite() != null && !lwc.getDescription().getWebsite().contains("extended")) {
            getLogger().info(" ");
            getLogger().warning("Notice: LWC Extended is the updated, and best supported, continuation of LWC. https://www.spigotmc.org/resources/lwc-extended.69551/");
            getLogger().info(" ");
        }

        // start up task which runs the autoLeaveAfterDaysOfInactivity routine
        startAutoLeaveTask(false);

        // Run before initializing listeners to handle reloads properly.
        particleProvider = new BukkitParticleProvider();

        if (conf().commands().seeChunk().isParticles()) {
            double delay = Math.floor(conf().commands().seeChunk().getParticleUpdateTime() * 20);
            seeChunkUtil = new SeeChunkUtil();
            seeChunkUtil.runTaskTimer(this, 0, (long) delay);
        }
        // End run before registering event handlers.

        // Register Event Handlers
        getServer().getPluginManager().registerEvents(new FactionsPlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new FactionsChatListener(this), this);
        getServer().getPluginManager().registerEvents(new FactionsEntityListener(this), this);
        getServer().getPluginManager().registerEvents(new FactionsExploitListener(this), this);
        getServer().getPluginManager().registerEvents(new FactionsBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new PortalListener_114(this), this);

        // since some other plugins execute commands directly through this command interface, provide it
        this.getCommand(refCommand).setExecutor(cmdBase);

        if (conf().commands().fly().isEnable()) {
            FlightUtil.start();
        }

        try {
            this.getOffline = this.getServer().getClass().getDeclaredMethod("getOfflinePlayer", GameProfile.class);
        } catch (Exception e) {
            this.getLogger().log(Level.WARNING, "Faction economy lookups will be slower:", e);
        }

        if (ChatColor.stripColor(TL.NOFACTION_PREFIX.toString()).equals("[4-]")) {
            getLogger().warning("Looks like you have an old, mistaken 'nofactions-prefix' in your lang.yml. It currently displays [4-] which is... strange.");
        }

        // Integration time
        getServer().getPluginManager().registerEvents(integrationManager = new IntegrationManager(this), this);

        new BukkitRunnable() {
            @Override
            public void run() {
                getServer().getPluginManager().callEvent(new FactionsPluginRegistrationTimeEvent());

                try {
                    Method close = PermissibleActionRegistry.class.getDeclaredMethod("close");
                    close.setAccessible(true);
                    close.invoke(null);
                    close = PermSelectorRegistry.class.getDeclaredMethod("close");
                    close.setAccessible(true);
                    close.invoke(null);
                } catch (Exception e) {
                    getLogger().log(Level.SEVERE, "Failed to close registries", e);
                }

                Econ.setup();
                vaultPerms = new VaultPerms();
                cmdBase.done();
                // Grand metrics adventure!
                setupMetrics();
                getLogger().removeHandler(handler);
                startupLog = startupBuilder.toString();
                startupExceptionLog = startupExceptionBuilder.toString();
            }
        }.runTask(this);

        getLogger().info("=== Initial start took " + (System.currentTimeMillis() - timeEnableStart) + "ms! ===");
        this.loadSuccessful = true;

        this.updateCheck = new Gson().toJson(update);
        if (!likesCats) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    URL url = new URI("https://update.plugin.party/check").toURL();
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setRequestProperty("Accept", "application/json");
                    try (OutputStream out = con.getOutputStream()) {
                        out.write(FactionsPlugin.this.updateCheck.getBytes(StandardCharsets.UTF_8));
                    }
                    String reply = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
                    Response response = new Gson().fromJson(reply, Response.class);
                    if (response.isSuccess()) {
                        if (response.isUpdateAvailable()) {
                            FactionsPlugin.this.updateResponse = response;
                            if (response.isUrgent()) {
                                FactionsPlugin.this.getServer().getOnlinePlayers().forEach(FactionsPlugin.this::updateNotification);
                            }
                            FactionsPlugin.this.getLogger().warning("Update available: " + response.getLatestVersion() + (response.getMessage() == null ? "" : (" - " + response.getMessage())));
                        }
                    } else {
                        if (response.getMessage().equals("INVALID")) {
                            this.cancel();
                        } else if (response.getMessage().equals("TOO_FAST")) {
                            // Nothing for now
                        } else {
                            FactionsPlugin.this.getLogger().warning("Failed to check for updates: " + response.getMessage());
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }.runTaskTimerAsynchronously(this, 1, 20 /* ticks */ * 60 /* seconds in a minute */ * 60 /* minutes in an hour*/);
    }

    private int intOr(String in, int or) {
        try {
            return Integer.parseInt(in);
        } catch (NumberFormatException ignored) {
            return or;
        }
    }

    private void setupMetrics() {
        this.metrics = new Metrics(this);

        // Version
        String verString = this.getDescription().getVersion();
        Pattern verPattern = Pattern.compile("1\\.6\\.9\\.5-U(?<version>\\d{1,2}\\.\\d{1,2}\\.\\d{1,2})(?<snap>-SNAPSHOT)?");
        Matcher matcher = verPattern.matcher(verString);
        final String fuuidVersion;
        final String fuuidBuild;
        if (matcher.find()) {
            fuuidVersion = matcher.group(1);
            fuuidBuild = matcher.group("snap") == null ? (likesCats ? "release" : "yarr") : "snapshot";
        } else {
            fuuidVersion = "Unknown";
            fuuidBuild = verString;
        }
        this.metricsDrillPie("fuuid_version", () -> {
            Map<String, Map<String, Integer>> map = new HashMap<>();
            Map<String, Integer> entry = new HashMap<>();
            entry.put(fuuidBuild, 1);
            map.put(fuuidVersion, entry);
            return map;
        });

        this.metricsDrillPie("fuuid_version_mc", () -> {
            Map<String, Map<String, Integer>> map = new HashMap<>();
            Map<String, Integer> entry = new HashMap<>();
            entry.put(this.mcVersionString, 1);
            map.put(fuuidVersion, entry);
            return map;
        });

        // Essentials
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("Essentials")) {
            Plugin ess = Essentials.getEssentials();
            this.metricsDrillPie("essentials", () -> this.metricsPluginInfo(ess));
            if (ess != null) {
                this.metricsSimplePie("essentials_delete_homes", () -> "" + conf().factions().other().isDeleteEssentialsHomes());
                this.metricsSimplePie("essentials_home_teleport", () -> "" + this.conf().factions().homes().isTeleportCommandEssentialsIntegration());
            }
        }

        // LWC
        Plugin lwc = LWC.getLWC();
        this.metricsDrillPie("lwc", () -> this.metricsPluginInfo(lwc));
        if (lwc != null) {
            boolean enabled = conf().lwc().isEnabled();
            this.metricsSimplePie("lwc_integration", () -> "" + enabled);
            if (enabled) {
                this.metricsSimplePie("lwc_reset_locks_unclaim", () -> "" + conf().lwc().isResetLocksOnUnclaim());
                this.metricsSimplePie("lwc_reset_locks_capture", () -> "" + conf().lwc().isResetLocksOnCapture());
            }
        }

        // Vault
        Plugin vault = Bukkit.getServer().getPluginManager().getPlugin("Vault");
        this.metricsDrillPie("vault", () -> this.metricsPluginInfo(vault));
        if (vault != null) {
            this.metricsDrillPie("vault_perms", () -> this.metricsInfo(vaultPerms.getPerms(), () -> vaultPerms.getName()));
            this.metricsDrillPie("vault_econ", () -> {
                Map<String, Map<String, Integer>> map = new HashMap<>();
                Map<String, Integer> entry = new HashMap<>();
                entry.put(Econ.getEcon() == null ? "none" : Econ.getEcon().getName(), 1);
                map.put((this.conf().economy().isEnabled() && Econ.getEcon() != null) ? "enabled" : "disabled", entry);
                return map;
            });
        }

        // LuckPerms
        this.metricsSimplePie("luckperms_contexts", () -> "" + this.luckPermsSetup);

        // WorldGuard
        Worldguard wg = this.getWorldguard();
        String wgVersion = wg == null ? "nope" : wg.getVersion();
        this.metricsDrillPie("worldguard", () -> this.metricsInfo(wg, () -> wgVersion));

        // Dynmap
        String dynmapVersion = EngineDynmap.getInstance().getVersion();
        boolean dynmapEnabled = EngineDynmap.getInstance().isRunning();
        this.metricsDrillPie("dynmap", () -> {
            Map<String, Map<String, Integer>> map = new HashMap<>();
            Map<String, Integer> entry = new HashMap<>();
            entry.put(dynmapVersion == null ? "none" : dynmapVersion, 1);
            map.put(dynmapEnabled ? "enabled" : "disabled", entry);
            return map;
        });

        // Clip Placeholder
        Plugin clipPlugin = getServer().getPluginManager().getPlugin("PlaceholderAPI");
        this.metricsDrillPie("clipplaceholder", () -> this.metricsPluginInfo(clipPlugin));

        // MVdW Placeholder
        Plugin mvdw = getServer().getPluginManager().getPlugin("MVdWPlaceholderAPI");
        this.metricsDrillPie("mvdwplaceholder", () -> this.metricsPluginInfo(mvdw));

        // Overall stats
        this.metricsLine("factions", () -> Factions.getInstance().getAllFactions().size() - 3);
        this.metricsSimplePie("scoreboard", () -> "" + conf().scoreboard().constant().isEnabled());

        // Event listeners
        this.metricsDrillPie("event_listeners", () -> {
            Set<Plugin> pluginsListening = this.getPlugins(FactionEvent.getHandlerList(), FactionCreateEvent.getHandlerList(), FactionRelationEvent.getHandlerList());
            Map<String, Map<String, Integer>> map = new HashMap<>();
            for (Plugin plugin : pluginsListening) {
                if (plugin.getName().equalsIgnoreCase("factions")) {
                    continue;
                }
                Map<String, Integer> entry = new HashMap<>();
                entry.put(plugin.getDescription().getVersion(), 1);
                map.put(plugin.getName(), entry);
            }
            return map;
        });
    }

    private Set<Plugin> getPlugins(HandlerList... handlerLists) {
        Set<Plugin> plugins = new HashSet<>();
        for (HandlerList handlerList : handlerLists) {
            plugins.addAll(this.getPlugins(handlerList));
        }
        return plugins;
    }

    private Set<Plugin> getPlugins(HandlerList handlerList) {
        return Arrays.stream(handlerList.getRegisteredListeners()).map(RegisteredListener::getPlugin).collect(Collectors.toSet());
    }

    private void metricsLine(String name, Callable<Integer> callable) {
        this.metrics.addCustomChart(new Metrics.SingleLineChart(name, callable));
    }

    private void metricsDrillPie(String name, Callable<Map<String, Map<String, Integer>>> callable) {
        this.metrics.addCustomChart(new Metrics.DrilldownPie(name, callable));
    }

    private void metricsSimplePie(String name, Callable<String> callable) {
        this.metrics.addCustomChart(new Metrics.SimplePie(name, callable));
    }

    private Map<String, Map<String, Integer>> metricsPluginInfo(Plugin plugin) {
        return this.metricsInfo(plugin, () -> plugin.getDescription().getVersion());
    }

    private Map<String, Map<String, Integer>> metricsInfo(Object plugin, Supplier<String> versionGetter) {
        Map<String, Map<String, Integer>> map = new HashMap<>();
        Map<String, Integer> entry = new HashMap<>();
        entry.put(plugin == null ? "nope" : versionGetter.get(), 1);
        map.put(plugin == null ? "absent" : "present", entry);
        return map;
    }

    public void setWorldGuard(Worldguard wg) {
        this.worldguard = wg;
    }

    public void loadLang() {
        File lang = new File(getDataFolder(), "lang.yml");

        YamlConfiguration conf = YamlConfiguration.loadConfiguration(lang);
        for (TL item : TL.values()) {
            if (conf.getString(item.getPath()) == null) {
                conf.set(item.getPath(), item.getDefault());
            }
        }

        TL.setFile(conf);
        try {
            conf.save(lang);
        } catch (IOException e) {
            FactionsPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to save lang.yml", e);
        }
    }

    public UUID getServerUUID() {
        return this.serverUUID;
    }

    public String getStartupLog() {
        return this.startupLog;
    }

    public String getStartupExceptionLog() {
        return this.startupExceptionLog;
    }

    public PermUtil getPermUtil() {
        return permUtil;
    }

    public Gson getGson() {
        return gson;
    }

    public SeeChunkUtil getSeeChunkUtil() {
        return seeChunkUtil;
    }

    public BukkitParticleProvider getParticleProvider() {
        return particleProvider;
    }

    // -------------------------------------------- //
    // LANG AND TAGS
    // -------------------------------------------- //

    // These are not supposed to be used directly.
    // They are loaded and used through the TextUtil instance for the plugin.
    private final Map<String, String> rawTags = new LinkedHashMap<>();

    private void addRawTags() {
        this.rawTags.put("l", "<green>"); // logo
        this.rawTags.put("a", "<gold>"); // art
        this.rawTags.put("n", "<silver>"); // notice
        this.rawTags.put("i", "<yellow>"); // info
        this.rawTags.put("g", "<lime>"); // good
        this.rawTags.put("b", "<rose>"); // bad
        this.rawTags.put("h", "<pink>"); // highlight
        this.rawTags.put("c", "<aqua>"); // command
        this.rawTags.put("p", "<teal>"); // parameter
    }

    private void initTXT() {
        this.addRawTags();

        Type type = new TypeToken<Map<String, String>>() {
        }.getType();

        Map<String, String> tagsFromFile = this.persist.load(type, "tags");
        if (tagsFromFile != null) {
            this.rawTags.putAll(tagsFromFile);
        }
        this.persist.save(this.rawTags, "tags");

        for (Map.Entry<String, String> rawTag : this.rawTags.entrySet()) {
            this.txt.tags.put(rawTag.getKey(), TextUtil.parseColor(rawTag.getValue()));
        }
    }

    public Map<UUID, Integer> getStuckMap() {
        return this.stuckMap;
    }

    public Map<UUID, Long> getTimers() {
        return this.timers;
    }

    // -------------------------------------------- //
    // LOGGING
    // -------------------------------------------- //
    public void log(String msg) {
        log(Level.INFO, msg);
    }

    public void log(String str, Object... args) {
        log(Level.INFO, this.txt.parse(str, args));
    }

    public void log(Level level, String str, Object... args) {
        log(level, this.txt.parse(str, args));
    }

    public void log(Level level, String msg) {
        this.getLogger().log(level, msg);
    }

    public boolean getLocked() {
        return this.locked;
    }

    public void setLocked(boolean val) {
        this.locked = val;
        this.setAutoSave(val);
    }

    public boolean getAutoSave() {
        return this.autoSave;
    }

    public void setAutoSave(boolean val) {
        this.autoSave = val;
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public MainConfig conf() {
        return this.configManager.getMainConfig();
    }

    public TranslationsConfig tl() {
        return this.configManager.getTranslationsConfig();
    }

    public LandRaidControl getLandRaidControl() {
        return this.landRaidControl;
    }

    public Worldguard getWorldguard() {
        return this.worldguard;
    }

    public boolean setupPlaceholderAPI() {
        this.clipPlaceholderAPIManager = new ClipPlaceholderAPIManager();
        if (this.clipPlaceholderAPIManager.register()) {
            getLogger().info("Successfully registered placeholders with PlaceholderAPI.");
            return true;
        }
        return false;
    }

    public boolean setupOtherPlaceholderAPI() {
        this.mvdwPlaceholderAPIManager = true;
        getLogger().info("Found MVdWPlaceholderAPI.");
        return true;
    }

    public boolean isClipPlaceholderAPIHooked() {
        return this.clipPlaceholderAPIManager != null;
    }

    public boolean isMVdWPlaceholderAPIHooked() {
        return this.mvdwPlaceholderAPIManager;
    }

    public GsonBuilder getGsonBuilder(boolean confNotLoaded) {
        Type mapFLocToStringSetType = new TypeToken<Map<FLocation, Set<String>>>() {
        }.getType();

        GsonBuilder builder = new GsonBuilder();

        if (confNotLoaded || !this.conf().data().json().useEfficientStorage()) {
            builder.setPrettyPrinting();
        }

        return builder
                .disableHtmlEscaping()
                .enableComplexMapKeySerialization()
                .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.VOLATILE)
                .registerTypeAdapter(PermSelector.class, new PermSelectorTypeAdapter())
                .registerTypeAdapter(LazyLocation.class, new MyLocationTypeAdapter())
                .registerTypeAdapter(mapFLocToStringSetType, new MapFLocToStringSetTypeAdapter())
                .registerTypeAdapterFactory(EnumTypeAdapter.ENUM_FACTORY);
    }

    @Override
    public void onDisable() {
        if (autoLeaveTask != null) {
            this.getServer().getScheduler().cancelTask(autoLeaveTask);
            autoLeaveTask = null;
        }

        if (saveTask != null) {
            this.getServer().getScheduler().cancelTask(saveTask);
            saveTask = null;
        }
        // only save data if plugin actually loaded successfully
        if (loadSuccessful) {
            Factions.getInstance().forceSave();
            FPlayers.getInstance().forceSave();
            Board.getInstance().forceSave();
        }
        if (this.luckPermsSetup) {
            LuckPerms.shutdown(this);
        }
        ContextManager.shutdown();
        this.adventure.close();
        log("Disabled");
    }

    public void startAutoLeaveTask(boolean restartIfRunning) {
        if (autoLeaveTask != null) {
            if (!restartIfRunning) {
                return;
            }
            this.getServer().getScheduler().cancelTask(autoLeaveTask);
        }

        if (this.conf().factions().other().getAutoLeaveRoutineRunsEveryXMinutes() > 0.0) {
            long ticks = (long) (20 * 60 * this.conf().factions().other().getAutoLeaveRoutineRunsEveryXMinutes());
            autoLeaveTask = getServer().getScheduler().scheduleSyncRepeatingTask(this, new AutoLeaveTask(), ticks, ticks);
        }
    }

    public boolean logPlayerCommands() {
        return this.conf().logging().isPlayerCommands();
    }

    // -------------------------------------------- //
    // Functions for other plugins to hook into
    // -------------------------------------------- //

    // This value will be updated whenever new hooks are added
    @Override
    public int getAPIVersion() {
        // Updated from 4 to 5 for version 0.5.0
        return 4;
    }

    // If another plugin is handling insertion of chat tags, this should be used to notify Factions
    @Override
    public void setHandlingChat(Plugin plugin, boolean handling) {
        if (plugin == null) {
            throw new IllegalArgumentException("Null plugin!");
        }
        if (plugin == this) {
            throw new IllegalArgumentException("Nice try, but this plugin isn't going to register itself!");
        }
        if (handling) {
            this.pluginsHandlingChat.add(plugin.getName());
        } else {
            this.pluginsHandlingChat.remove(plugin.getName());
        }
    }

    @Override
    public boolean isAnotherPluginHandlingChat() {
        return this.conf().factions().chat().isTagHandledByAnotherPlugin() || !this.pluginsHandlingChat.isEmpty();
    }

    // Simply put, should this chat event be left for Factions to handle? For now, that means players with Faction Chat
    // enabled or use of the Factions f command without a slash; combination of isPlayerFactionChatting() and isFactionsCommand()

    @Override
    public boolean shouldLetFactionsHandleThisChat(AsyncPlayerChatEvent event) {
        return event != null && isPlayerFactionChatting(event.getPlayer());
    }

    // Does player have Faction Chat enabled? If so, chat plugins should preferably not do channels,
    // local chat, or anything else which targets individual recipients, so Faction Chat can be done
    @Override
    public boolean isPlayerFactionChatting(Player player) {
        if (player == null) {
            return false;
        }
        FPlayer me = FPlayers.getInstance().getByPlayer(player);

        return me != null && me.getChatMode().isAtLeast(ChatMode.ALLIANCE);
    }

    // Is this chat message actually a Factions command, and thus should be left alone by other plugins?

    // Get a player's faction tag (faction name), mainly for usage by chat plugins for local/channel chat
    @Override
    public String getPlayerFactionTag(Player player) {
        return getPlayerFactionTagRelation(player, null);
    }

    // Same as above, but with relation (enemy/neutral/ally) coloring potentially added to the tag
    @Override
    public String getPlayerFactionTagRelation(Player speaker, Player listener) {
        String tag = "~";

        if (speaker == null) {
            return tag;
        }

        FPlayer me = FPlayers.getInstance().getByPlayer(speaker);
        if (me == null) {
            return tag;
        }

        // if listener isn't set, or config option is disabled, give back uncolored tag
        if (listener == null || !this.conf().factions().chat().isTagRelationColored()) {
            tag = me.getChatTag().trim();
        } else {
            FPlayer you = FPlayers.getInstance().getByPlayer(listener);
            if (you == null) {
                tag = me.getChatTag().trim();
            } else  // everything checks out, give the colored tag
            {
                tag = me.getChatTag(you).trim();
            }
        }
        if (tag.isEmpty()) {
            tag = "~";
        }

        return tag;
    }

    // Get a player's title within their faction, mainly for usage by chat plugins for local/channel chat
    @Override
    public String getPlayerTitle(Player player) {
        if (player == null) {
            return "";
        }

        FPlayer me = FPlayers.getInstance().getByPlayer(player);
        if (me == null) {
            return "";
        }

        return me.getTitle().trim();
    }

    // Get a list of all faction tags (names)
    @Override
    public Set<String> getFactionTags() {
        return Factions.getInstance().getFactionTags();
    }

    // Get a list of all players in the specified faction
    @Override
    public Set<String> getPlayersInFaction(String factionTag) {
        Set<String> players = new HashSet<>();
        Faction faction = Factions.getInstance().getByTag(factionTag);
        if (faction != null) {
            for (FPlayer fplayer : faction.getFPlayers()) {
                players.add(fplayer.getName());
            }
        }
        return players;
    }

    // Get a list of all online players in the specified faction
    @Override
    public Set<String> getOnlinePlayersInFaction(String factionTag) {
        Set<String> players = new HashSet<>();
        Faction faction = Factions.getInstance().getByTag(factionTag);
        if (faction != null) {
            for (FPlayer fplayer : faction.getFPlayersWhereOnline(true)) {
                players.add(fplayer.getName());
            }
        }
        return players;
    }

    public String getPrimaryGroup(OfflinePlayer player) {
        return this.vaultPerms.getPrimaryGroup(player);
    }

    public void debug(Level level, String s) {
        if (conf().getaVeryFriendlyFactionsConfig().isDebug()) {
            getLogger().log(level, s);
        }
    }

    public void debug(String s) {
        debug(Level.INFO, s);
    }

    public void luckpermsEnabled() {
        this.luckPermsSetup = true;
    }

    public CompletableFuture<Boolean> teleport(Player player, Location location) {
        if (this.conf().paper().isAsyncTeleport()) {
            return PaperLib.teleportAsync(player, location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        } else {
            return CompletableFuture.completedFuture(player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN));
        }
    }

    public OfflinePlayer getFactionOfflinePlayer(String name) {
        return this.getOfflinePlayer(name, UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8)));
    }

    @SuppressWarnings("deprecation")
    public OfflinePlayer getOfflinePlayer(String name, UUID uuid) {
        if (this.getOffline != null) {
            try {
                return (OfflinePlayer) this.getOffline.invoke(this.getServer(), new GameProfile(uuid, name));
            } catch (Exception e) {
                this.getLogger().log(Level.SEVERE, "Failed to get offline player the fast way, reverting to slow mode", e);
                this.getOffline = null;
            }
        }
        return this.getServer().getOfflinePlayer(name);
    }

    public BukkitAudiences getAdventure() {
        return this.adventure;
    }

    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "unused"})
    private static class UpdateCheck {
        private final String pluginName;
        private final String pluginVersion;
        private final String serverName;
        private final String serverVersion;
        private int meow;
        private String spigotId;

        public UpdateCheck(String pluginName, String pluginVersion, String serverName, String serverVersion) {
            this.pluginName = pluginName;
            this.pluginVersion = pluginVersion;
            this.serverName = serverName;
            this.serverVersion = serverVersion;
        }
    }

    @SuppressWarnings({"unused"})
    private static class Response {
        private boolean success;
        private String message;
        private boolean updateAvailable;
        private boolean isUrgent;
        private String latestVersion;

        private Component component;

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public boolean isUpdateAvailable() {
            return updateAvailable;
        }

        public boolean isUrgent() {
            return isUrgent;
        }

        public String getLatestVersion() {
            return latestVersion;
        }

        public Component getComponent() {
            if (component == null) {
                component = message == null ? null : MiniMessage.miniMessage().deserialize(message);
            }
            return component;
        }
    }

    private final Set<UUID> told = new HashSet<>();

    public void updateNotification(Player player) {
        if (updateResponse == null || !player.hasPermission("factions.updates")) {
            return;
        }
        if (!updateResponse.isUrgent() && this.told.contains(player.getUniqueId())) {
            return;
        }
        this.told.add(player.getUniqueId());
        Audience audience = this.adventure.player(player);
        audience.sendMessage(Component.text().color(TextColor.fromHexString("#e35959"))
                .content("FactionsUUID Update Available: " + updateResponse.getLatestVersion()));
        if (updateResponse.isUrgent()) {
            audience.sendMessage(Component.text().color(TextColor.fromHexString("#5E0B15"))
                    .content("This is an important update. Download and restart ASAP."));
        }
        if (updateResponse.getComponent() != null) {
            audience.sendMessage(updateResponse.getComponent());
        }
        player.sendMessage(ChatColor.GREEN + "Get it at " + ChatColor.DARK_AQUA + "https://www.spigotmc.org/resources/factionsuuid.1035/");
    }

    public IntegrationManager getIntegrationManager() {
        return this.integrationManager;
    }
}
