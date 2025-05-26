package dev.kitteh.factions.command;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.defaults.*;
import dev.kitteh.factions.command.defaults.admin.CmdAdminDTR;
import dev.kitteh.factions.command.defaults.admin.CmdAdminForce;
import dev.kitteh.factions.command.defaults.admin.CmdAdminPower;
import dev.kitteh.factions.command.defaults.admin.CmdAdminSet;
import dev.kitteh.factions.command.defaults.admin.CmdBypass;
import dev.kitteh.factions.command.defaults.admin.CmdChatSpy;
import dev.kitteh.factions.command.defaults.admin.CmdMoneyModify;
import dev.kitteh.factions.command.defaults.admin.CmdReload;
import dev.kitteh.factions.command.defaults.admin.CmdSaveAll;
import dev.kitteh.factions.command.defaults.admin.CmdTicketInfo;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.WorldUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.help.result.CommandEntry;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

@NullMarked
public class CommandsRoot {
    private record Register(BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer,
                            Plugin providingPlugin, String command) {
    }

    private static @Nullable Map<String, Register> registry = new ConcurrentHashMap<>();
    private static @Nullable Map<String, Register> adminRegistry = new ConcurrentHashMap<>();

    static void register(Plugin providingPlugin, String command, BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer) {
        reg(providingPlugin, command, consumer, registry);
    }

    static void registerAdmin(Plugin providingPlugin, String command, BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer) {
        reg(providingPlugin, command, consumer, adminRegistry);
    }

    private static void reg(Plugin providingPlugin, String command, BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer, @Nullable Map<String, Register> adminRegistry) {
        if (adminRegistry == null) {
            throw new IllegalStateException("Registration closed");
        }
        if (Objects.requireNonNull(providingPlugin) == FactionsPlugin.instance()) {
            throw new IllegalArgumentException("Use your own plugin!");
        }
        adminRegistry.put(Objects.requireNonNull(command), new Register(Objects.requireNonNull(consumer), providingPlugin, command));
    }

    private void registerInternal(String command, Cmd cmd) {
        if (registry == null) {
            return;
        }
        if (registry.containsKey(command)) {
            AbstractFactionsPlugin.instance().getLogger().info("Skipping internal '" + command + "' command because it is already registered.");
            return;
        }
        registry.put(command, new Register(cmd.consumer(), AbstractFactionsPlugin.instance(), command));
    }

    private void registerAdminInternal(String command, Cmd cmd) {
        if (adminRegistry == null) {
            return;
        }
        if (adminRegistry.containsKey(command)) {
            AbstractFactionsPlugin.instance().getLogger().info("Skipping internal admin '" + command + "' command because it is already registered.");
            return;
        }
        adminRegistry.put(command, new Register(cmd.consumer(), AbstractFactionsPlugin.instance(), command));
    }

    public CommandsRoot(AbstractFactionsPlugin plugin) {
        if (registry == null || adminRegistry == null) {
            throw new IllegalStateException("Second attempt at creating this class!");
        }
        this.register();

        LegacyPaperCommandManager<Sender> manager = new LegacyPaperCommandManager<>(plugin, ExecutionCoordinator.simpleCoordinator(), SenderMapper.create(Sender::of, Sender::sender));
        if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            manager.registerBrigadier();
        }

        manager.captionRegistry().registerProvider(new Captioner());

        var main = plugin.tl().commands().generic().getCommandRoot();
        Command.Builder<Sender> builder = manager.commandBuilder(main.getFirstAlias(), main.getSecondaryAliases())
                .permission(Cloudy.predicate(sender -> WorldUtil.isEnabled(sender.sender())));
        registry.values().forEach(reg -> {
            try {
                reg.consumer.accept(manager, builder);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to register command '" + reg.command + "' from plugin '" + reg.providingPlugin.getName() + "'", e);
            }
        });

        var admin = plugin.tl().commands().generic().getCommandAdminRoot();
        Command.Builder<Sender> builderAdmin = manager.commandBuilder(admin.getFirstAlias(), admin.getSecondaryAliases())
                .permission(Cloudy.predicate(sender -> WorldUtil.isEnabled(sender.sender())));
        adminRegistry.values().forEach(reg -> {
            try {
                reg.consumer.accept(manager, builderAdmin);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to register admin command '" + reg.command + "' from plugin '" + reg.providingPlugin.getName() + "'", e);
            }
        });

        MinecraftHelp<Sender> help = MinecraftHelp.<Sender>builder()
                .commandManager(manager)
                .audienceProvider(LilAudience::new)
                .commandPrefix("/f help")
                .build();

        manager.command(
                builder.literal("help")
                        .optional(
                                "query",
                                StringParser.greedyStringParser(),
                                DefaultValue.constant(""),
                                SuggestionProvider.blocking((ctx, in) -> manager.createHelpHandler()
                                        .queryRootIndex(ctx.sender())
                                        .entries()
                                        .stream()
                                        .map(CommandEntry::syntax)
                                        .map(Suggestion::suggestion)
                                        .collect(Collectors.toList())
                                )
                        )
                        .handler(context -> help.queryCommands(context.get("query"), context.sender()))
        );

        registry = null; // Last step!
        adminRegistry = null;
    }

    private void register() {
        registerInternal("announce", new CmdAnnounce());
        registerInternal("ban", new CmdBan());
        registerInternal("chat", new CmdChat());
        registerInternal("claim", new CmdClaim());
        registerInternal("confirm", new CmdConfirm());
        registerInternal("coords", new CmdCoords());
        registerInternal("create", new CmdCreate());
        registerInternal("disband", new CmdDisband());
        registerInternal("dtr", new CmdDTR());
        registerInternal("fly", new CmdFly());
        registerInternal("grace", new CmdGrace());
        registerInternal("home", new CmdHome());
        registerInternal("invite", new CmdInvite());
        registerInternal("join", new CmdJoin());
        registerInternal("kick", new CmdKick());
        registerInternal("leave", new CmdLeave());
        registerInternal("link", new CmdLink());
        registerInternal("list", new CmdList());
        registerInternal("map", new CmdMap());
        registerInternal("money", new CmdMoney());
        registerInternal("near", new CmdNear());
        registerInternal("power", new CmdPower());
        registerInternal("relation", new CmdRelation());
        registerInternal("role", new CmdRole());
        registerInternal("set", new CmdSet());
        registerInternal("shield", new CmdShield());
        registerInternal("show", new CmdShow());
        registerInternal("status", new CmdStatus());
        registerInternal("stuck", new CmdStuck());
        registerInternal("tnt", new CmdTNT());
        registerInternal("toggle", new CmdToggle());
        registerInternal("top", new CmdTop());
        registerInternal("unban", new CmdUnban());
        registerInternal("unclaim", new CmdUnclaim());
        registerInternal("upgrades", new CmdUpgrades());
        registerInternal("version", new CmdVersion());
        registerInternal("warp", new CmdWarp());
        registerInternal("zone", new CmdZone());

        if (Bukkit.getServer().getPluginManager().isPluginEnabled("PlayerVaults")) {
            registerInternal("vault", new CmdVault());
        }

        registerAdminInternal("bypass", new CmdBypass());
        registerAdminInternal("chatspy", new CmdChatSpy());
        registerAdminInternal("dtr", new CmdAdminDTR());
        registerAdminInternal("force", new CmdAdminForce());
        registerAdminInternal("money", new CmdMoneyModify());
        registerAdminInternal("power", new CmdAdminPower());
        registerAdminInternal("reload", new CmdReload());
        registerAdminInternal("save-all", new CmdSaveAll());
        registerAdminInternal("set", new CmdAdminSet());
        registerAdminInternal("ticketinfo", new CmdTicketInfo());

    }
}
