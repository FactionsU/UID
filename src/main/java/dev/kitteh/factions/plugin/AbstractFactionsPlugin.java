package dev.kitteh.factions.plugin;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.GameProfile;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.chat.ChatTarget;
import dev.kitteh.factions.command.CommandsRoot;
import dev.kitteh.factions.config.ConfigManager;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.config.file.TranslationsConfig;
import dev.kitteh.factions.data.MemoryFPlayer;
import dev.kitteh.factions.data.MemoryFaction;
import dev.kitteh.factions.data.SaveTask;
import dev.kitteh.factions.event.FactionCreateEvent;
import dev.kitteh.factions.event.FactionEvent;
import dev.kitteh.factions.event.FactionRelationEvent;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.integration.Essentials;
import dev.kitteh.factions.integration.IntegrationManager;
import dev.kitteh.factions.integration.LuckPerms;
import dev.kitteh.factions.integration.VaultPerms;
import dev.kitteh.factions.integration.Worldguard;
import dev.kitteh.factions.integration.dynmap.EngineDynmap;
import dev.kitteh.factions.integration.permcontext.ContextManager;
import dev.kitteh.factions.landraidcontrol.LandRaidControl;
import dev.kitteh.factions.listener.FactionsBlockListener;
import dev.kitteh.factions.listener.FactionsChatListener;
import dev.kitteh.factions.listener.FactionsEntityListener;
import dev.kitteh.factions.listener.FactionsExploitListener;
import dev.kitteh.factions.listener.FactionsPlayerListener;
import dev.kitteh.factions.listener.PortalListener;
import dev.kitteh.factions.listener.UpgradeListener;
import dev.kitteh.factions.permissible.PermSelector;
import dev.kitteh.factions.permissible.PermSelectorRegistry;
import dev.kitteh.factions.permissible.PermissibleActionRegistry;
import dev.kitteh.factions.upgrade.LeveledValueProvider;
import dev.kitteh.factions.upgrade.Upgrade;
import dev.kitteh.factions.upgrade.UpgradeRegistry;
import dev.kitteh.factions.upgrade.UpgradeVariable;
import dev.kitteh.factions.util.AutoLeaveTask;
import dev.kitteh.factions.util.ComponentDispatcher;
import dev.kitteh.factions.util.FlightUtil;
import dev.kitteh.factions.util.LazyLocation;
import dev.kitteh.factions.util.MaterialDb;
import dev.kitteh.factions.util.Metrics;
import dev.kitteh.factions.util.SeeChunkUtil;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.TextUtil;
import dev.kitteh.factions.util.WorldTracker;
import dev.kitteh.factions.util.WorldUtil;
import dev.kitteh.factions.util.adapter.ChatTargetTypeAdapter;
import dev.kitteh.factions.util.adapter.LeveledValueProviderDeserializer;
import dev.kitteh.factions.util.adapter.LeveledValueProviderEquationSerializer;
import dev.kitteh.factions.util.adapter.MapFLocToStringSetTypeAdapter;
import dev.kitteh.factions.util.adapter.MyLocationTypeAdapter;
import dev.kitteh.factions.util.adapter.PermSelectorTypeAdapter;
import dev.kitteh.factions.util.adapter.SelectorPermsAdapter;
import dev.kitteh.factions.util.adapter.UpgradeTypeAdapter;
import dev.kitteh.factions.util.adapter.UpgradeVariableTypeAdapter;
import dev.kitteh.factions.util.adapter.WorldTrackerTypeAdapter;
import io.papermc.lib.PaperLib;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class AbstractFactionsPlugin extends JavaPlugin implements FactionsPlugin {

    // Our single plugin instance.
    // Single 4 life.
    private static AbstractFactionsPlugin instance;
    private static final int OLDEST_MODERN_SUPPORTED = 2104;
    private static final String OLDEST_MODERN_SUPPORTED_STRING = "1.21.4";

    public static AbstractFactionsPlugin getInstance() {
        return instance;
    }

    private ConfigManager configManager;

    private Integer saveTask = null;
    private boolean autoSave = true;
    private boolean loadSuccessful = false;

    // Some utils
    private TextUtil txt;

    public TextUtil txt() {
        return txt;
    }

    public void grumpException(RuntimeException e) {
        this.grumpyExceptions.add(e);
    }

    // Persist related
    private Gson gson;

    // holds f stuck start times
    private final Map<UUID, Long> timers = new HashMap<>();

    //holds f stuck taskids
    private final Map<UUID, Integer> stuckMap = new HashMap<>();

    private Integer autoLeaveTask = null;

    private SeeChunkUtil seeChunkUtil;
    private Worldguard worldguard;
    private LandRaidControl landRaidControl;
    private boolean luckPermsSetup;
    private IntegrationManager integrationManager;

    private Metrics metrics;
    private UUID serverUUID;
    private String startupLog = "NOTFINISHED";
    private String startupExceptionLog = "NOTFINISHED";
    private final List<RuntimeException> grumpyExceptions = new ArrayList<>();
    private VaultPerms vaultPerms;
    public final boolean likesCats = Arrays.stream(AbstractFactionsPlugin.class.getDeclaredMethods()).anyMatch(m -> m.isSynthetic() && m.getName().startsWith("loadCon") && m.getName().endsWith("0"));
    private Method getOffline;
    private String mcVersionString;
    private String updateCheck;
    private Response updateResponse;

    public AbstractFactionsPlugin() {
        instance = this;
    }

    @Override
    public void onLoad() {
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
        //noinspection ResultOfMethodCallIgnored
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
        getLogger().info("Loading as a " + this.pluginType() + "plugin");
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
        if (versionInteger < OLDEST_MODERN_SUPPORTED) {
            getLogger().info("");
            getLogger().warning("FactionsUUID expects at least " + OLDEST_MODERN_SUPPORTED_STRING + " and may not work on your version.");
        }
        getLogger().info("");

        this.getLogger().info("Server UUID " + this.serverUUID);

        try {
            this.getOffline = this.getServer().getClass().getDeclaredMethod("getOfflinePlayer", GameProfile.class);
        } catch (Exception e) {
            this.getLogger().log(Level.WARNING, "Faction economy lookups will be slower:", e);
        }

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
            //noinspection ResultOfMethodCallIgnored
            dataFolder.mkdir();
        }

        // Load Material database
        MaterialDb.load();

        // Create Utility Instances
        WorldUtil.init(this.conf().restrictWorlds());

        this.txt = new TextUtil();
        initTXT();

        // Register recurring tasks
        if (saveTask == null && this.conf().factions().other().getSaveToFileEveryXMinutes() > 0.0) {
            long saveTicks = (long) (20 * 60 * this.conf().factions().other().getSaveToFileEveryXMinutes()); // Approximately every 30 min by default
            saveTask = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new SaveTask(this), saveTicks, saveTicks);
        }

        int loadedPlayers = Instances.PLAYERS.load();
        int loadedFactions = Instances.FACTIONS.load();
        for (FPlayer fPlayer : FPlayers.fPlayers().all()) {
            ((MemoryFPlayer) fPlayer).cleanupDeserialization();
            int factionId = ((MemoryFPlayer) fPlayer).getFactionId();
            Faction faction = Factions.factions().get(factionId);
            if (faction == null) {
                log("Invalid faction id on " + fPlayer.name() + ":" + factionId);
                fPlayer.resetFactionData();
                continue;
            }
            ((MemoryFaction) faction).addMember(fPlayer);
        }
        int loadedClaims = Instances.BOARD.load();
        Instances.BOARD.clean();
        Instances.UNIVERSE.load();
        AbstractFactionsPlugin.getInstance().getLogger().info("Loaded " + loadedPlayers + " players in " + loadedFactions + " factions with " + loadedClaims + " claims");

        ContextManager.init(this);
        if (getServer().getPluginManager().getPlugin("PermissionsEx") != null) {
            getLogger().info(" ");
            getLogger().warning("Notice: PermissionsEx is dead. We suggest using LuckPerms. https://luckperms.net/");
            getLogger().info(" ");
        }
        if (getServer().getPluginManager().getPlugin("GroupManager") != null) {
            getLogger().info(" ");
            getLogger().warning("Notice: GroupManager died in 2014. We suggest using LuckPerms instead. https://luckperms.net/");
            getLogger().info(" ");
        }

        // start up task which runs the autoLeaveAfterDaysOfInactivity routine
        startAutoLeaveTask(false);

        // Run before initializing listeners to handle reloads properly.
        double delay = Math.floor(conf().commands().seeChunk().getParticleUpdateTime() * 20);
        seeChunkUtil = new SeeChunkUtil();
        seeChunkUtil.runTaskTimer(this, 0, (long) delay);
        // End run before registering event handlers.

        // Register Event Handlers
        getServer().getPluginManager().registerEvents(new FactionsPlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new FactionsChatListener(this), this);
        getServer().getPluginManager().registerEvents(new FactionsEntityListener(this), this);
        getServer().getPluginManager().registerEvents(new FactionsExploitListener(this), this);
        getServer().getPluginManager().registerEvents(new FactionsBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new PortalListener(this), this);
        getServer().getPluginManager().registerEvents(new UpgradeListener(), this);

        if (conf().commands().fly().isEnable()) {
            FlightUtil.start();
        }

        if (ChatColor.stripColor(TL.NOFACTION_PREFIX.toString()).equals("[4-]")) {
            getLogger().warning("Looks like you have an old, mistaken 'nofactions-prefix' in your lang.yml. It currently displays [4-] which is... strange.");
        }

        new CommandsRoot(this);

        // Integration time
        getServer().getPluginManager().registerEvents(integrationManager = new IntegrationManager(this), this);

        new BukkitRunnable() {
            @Override
            public void run() {
                closeRegistries(PermissibleActionRegistry.class, PermSelectorRegistry.class, UpgradeRegistry.class);

                Econ.setup();
                vaultPerms = new VaultPerms();
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
                        out.write(AbstractFactionsPlugin.this.updateCheck.getBytes(StandardCharsets.UTF_8));
                    }
                    String reply = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
                    Response response = new Gson().fromJson(reply, Response.class);
                    if (response.isSuccess()) {
                        if (response.isUpdateAvailable()) {
                            AbstractFactionsPlugin.this.updateResponse = response;
                            if (response.isUrgent()) {
                                AbstractFactionsPlugin.this.getServer().getOnlinePlayers().forEach(AbstractFactionsPlugin.this::updateNotification);
                            }
                            AbstractFactionsPlugin.this.getLogger().warning("Update available: " + response.getLatestVersion() + (response.getMessage() == null ? "" : (" - " + response.getMessage())));
                        }
                    } else {
                        if (response.getMessage().equals("INVALID")) {
                            this.cancel();
                        } else if (response.getMessage().equals("TOO_FAST")) {
                            // Nothing for now
                        } else {
                            AbstractFactionsPlugin.this.getLogger().warning("Failed to check for updates: " + response.getMessage());
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }.runTaskTimerAsynchronously(this, 1, 20 /* ticks */ * 60 /* seconds in a minute */ * 60 /* minutes in an hour*/);
    }

    private void closeRegistries(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            try {
                Method close = clazz.getDeclaredMethod("close");
                close.setAccessible(true);
                close.invoke(null);
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Failed to close registry: " + clazz.getSimpleName(), e);
            }
        }
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

        // Overall stats
        this.metricsLine("factions", () -> Factions.factions().all().size() - 3);
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
            AbstractFactionsPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to save lang.yml", e);
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

    @Override
    public Gson gson() {
        return gson;
    }

    @Override
    public SeeChunkUtil seeChunkUtil() {
        return seeChunkUtil;
    }

    // -------------------------------------------- //
    // LANG AND TAGS
    // -------------------------------------------- //

    private void initTXT() {
        Map<String, String> rawTags = new LinkedHashMap<>();
        rawTags.put("l", "<green>"); // logo
        rawTags.put("a", "<gold>"); // art
        rawTags.put("n", "<silver>"); // notice
        rawTags.put("i", "<yellow>"); // info
        rawTags.put("g", "<lime>"); // good
        rawTags.put("b", "<rose>"); // bad
        rawTags.put("h", "<pink>"); // highlight
        rawTags.put("c", "<aqua>"); // command
        rawTags.put("p", "<teal>"); // parameter

        Type type = new TypeToken<Map<String, String>>() {
        }.getType();

        Map<String, String> tagsFromFile = null;

        try {
            String content = Files.readString(AbstractFactionsPlugin.getInstance().getDataFolder().toPath().resolve("tags.json"));
            tagsFromFile = AbstractFactionsPlugin.getInstance().gson().fromJson(content, type);
        } catch (Exception e) {
            AbstractFactionsPlugin.getInstance().log(Level.WARNING, e.getMessage());
        }

        if (tagsFromFile != null) {
            rawTags.putAll(tagsFromFile);
        }

        for (Map.Entry<String, String> rawTag : rawTags.entrySet()) {
            this.txt.tags.put(rawTag.getKey(), TextUtil.parseColor(rawTag.getValue()));
        }
    }

    @Override
    public Map<UUID, Integer> stuckMap() {
        return this.stuckMap;
    }

    @Override
    public Map<UUID, Long> timers() {
        return this.timers;
    }

    // -------------------------------------------- //
    // LOGGING
    // -------------------------------------------- //
    @Override
    public void log(String msg) {
        log(Level.INFO, msg);
    }

    @Override
    public void log(String str, Object... args) {
        log(Level.INFO, this.txt.parse(str, args));
    }

    @Override
    public void log(Level level, String str, Object... args) {
        log(level, this.txt.parse(str, args));
    }

    @Override
    public void log(Level level, String msg) {
        this.getLogger().log(level, msg);
    }

    @Override
    public boolean autoSave() {
        return this.autoSave;
    }

    @Override
    public void autoSave(boolean val) {
        this.autoSave = val;
    }

    @Override
    public ConfigManager configManager() {
        return this.configManager;
    }

    @Override
    public MainConfig conf() {
        return this.configManager.mainConfig();
    }

    @Override
    public TranslationsConfig tl() {
        return this.configManager.translationsConfig();
    }

    @Override
    public LandRaidControl landRaidControl() {
        return this.landRaidControl;
    }

    public Worldguard getWorldguard() {
        return this.worldguard;
    }

    public GsonBuilder getGsonBuilder(boolean confNotLoaded) {
        Type mapFLocToStringSetType = new TypeToken<Map<FLocation, Set<String>>>() {
        }.getType();
        Type worldTrackerMapType = new com.google.common.reflect.TypeToken<Object2ObjectOpenHashMap<String, WorldTracker>>() {
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
                .registerTypeAdapter(ChatTarget.class, new ChatTargetTypeAdapter())
                .registerTypeAdapter(UpgradeVariable.class, new UpgradeVariableTypeAdapter())
                .registerTypeAdapter(Upgrade.class, new UpgradeTypeAdapter())
                .registerTypeAdapter(LeveledValueProvider.class, new LeveledValueProviderDeserializer())
                .registerTypeAdapter(LeveledValueProvider.Equation.class, new LeveledValueProviderEquationSerializer())
                .registerTypeAdapter(worldTrackerMapType, new WorldTrackerTypeAdapter())
                .registerTypeAdapter(MemoryFaction.Permissions.SelectorPerms.class, new SelectorPermsAdapter());
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
            Instances.FACTIONS.forceSave(true);
            Instances.PLAYERS.forceSave(true);
            Instances.BOARD.forceSave(true);
            Instances.UNIVERSE.forceSave(true);
        }
        if (this.luckPermsSetup) {
            LuckPerms.shutdown(this);
        }
        ContextManager.shutdown();
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

    public String getPrimaryGroup(OfflinePlayer player) {
        return this.vaultPerms.getPrimaryGroup(player);
    }

    @Override
    public void debug(Level level, String s) {
        if (conf().getaVeryFriendlyFactionsConfig().isDebug()) {
            getLogger().log(level, s);
        }
    }

    @Override
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

    @Override
    public OfflinePlayer factionOfflinePlayer(String name) {
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

    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "unused"})
    private static class UpdateCheck {
        private String pluginName;
        private String pluginVersion;
        private String serverName;
        private String serverVersion;
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
        ComponentDispatcher.send(player, Component.text().color(TextColor.fromHexString("#e35959"))
                .content("FactionsUUID Update Available: " + updateResponse.getLatestVersion()));
        if (updateResponse.isUrgent()) {
            ComponentDispatcher.send(player, Component.text().color(TextColor.fromHexString("#5E0B15"))
                    .content("This is an important update. Download and restart ASAP."));
        }
        if (updateResponse.getComponent() != null) {
            ComponentDispatcher.send(player, updateResponse.getComponent());
        }
        player.sendMessage(ChatColor.GREEN + "Get it at " + ChatColor.DARK_AQUA + "https://www.spigotmc.org/resources/factionsuuid.1035/");
    }

    @Override
    public IntegrationManager integrationManager() {
        return this.integrationManager;
    }

    protected abstract String pluginType();
}
