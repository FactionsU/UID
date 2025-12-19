package dev.kitteh.factions.command;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.defaults.CmdAnnounce;
import dev.kitteh.factions.command.defaults.CmdBan;
import dev.kitteh.factions.command.defaults.CmdChat;
import dev.kitteh.factions.command.defaults.CmdClaim;
import dev.kitteh.factions.command.defaults.CmdClear;
import dev.kitteh.factions.command.defaults.CmdConfirm;
import dev.kitteh.factions.command.defaults.CmdCoords;
import dev.kitteh.factions.command.defaults.CmdCreate;
import dev.kitteh.factions.command.defaults.CmdDTR;
import dev.kitteh.factions.command.defaults.CmdDisband;
import dev.kitteh.factions.command.defaults.CmdFly;
import dev.kitteh.factions.command.defaults.CmdGrace;
import dev.kitteh.factions.command.defaults.CmdHome;
import dev.kitteh.factions.command.defaults.CmdInvite;
import dev.kitteh.factions.command.defaults.CmdJoin;
import dev.kitteh.factions.command.defaults.CmdKick;
import dev.kitteh.factions.command.defaults.CmdLeave;
import dev.kitteh.factions.command.defaults.CmdLink;
import dev.kitteh.factions.command.defaults.CmdList;
import dev.kitteh.factions.command.defaults.CmdMap;
import dev.kitteh.factions.command.defaults.CmdMoney;
import dev.kitteh.factions.command.defaults.CmdNear;
import dev.kitteh.factions.command.defaults.CmdPower;
import dev.kitteh.factions.command.defaults.CmdRelation;
import dev.kitteh.factions.command.defaults.CmdRole;
import dev.kitteh.factions.command.defaults.CmdSet;
import dev.kitteh.factions.command.defaults.CmdShield;
import dev.kitteh.factions.command.defaults.CmdShow;
import dev.kitteh.factions.command.defaults.CmdStatus;
import dev.kitteh.factions.command.defaults.CmdStuck;
import dev.kitteh.factions.command.defaults.CmdTNT;
import dev.kitteh.factions.command.defaults.CmdToggle;
import dev.kitteh.factions.command.defaults.CmdTop;
import dev.kitteh.factions.command.defaults.CmdUnban;
import dev.kitteh.factions.command.defaults.CmdUnclaim;
import dev.kitteh.factions.command.defaults.CmdUpgrades;
import dev.kitteh.factions.command.defaults.CmdVault;
import dev.kitteh.factions.command.defaults.CmdVersion;
import dev.kitteh.factions.command.defaults.CmdWarp;
import dev.kitteh.factions.command.defaults.CmdZone;
import dev.kitteh.factions.command.defaults.admin.CmdAdminDTR;
import dev.kitteh.factions.command.defaults.admin.CmdAdminForce;
import dev.kitteh.factions.command.defaults.admin.CmdAdminPower;
import dev.kitteh.factions.command.defaults.admin.CmdAdminSet;
import dev.kitteh.factions.command.defaults.admin.CmdAdminTNT;
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
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.help.result.CommandEntry;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import dev.kitteh.factions.util.TriConsumer;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

@ApiStatus.Internal
@NullMarked
public class CommandsRoot {
    static {
        registry = new ConcurrentHashMap<>();
        adminRegistry = new ConcurrentHashMap<>();
        register();
        AbstractFactionsPlugin.instance().addCommands(CommandsRoot::registerInternal, CommandsRoot::setCommandManagerSupplier);
    }

    private record Register(TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer,
                            Plugin providingPlugin, String command) {
    }

    private static @Nullable Supplier<CommandManager<Sender>> commandManager;
    private static @Nullable Map<String, Register> registry;
    private static @Nullable Map<String, Register> adminRegistry;

    private static void setCommandManagerSupplier(Supplier<CommandManager<Sender>> commandManagerSupplier) {
        CommandsRoot.commandManager = commandManagerSupplier;
    }

    static void register(Plugin providingPlugin, String command, TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer) {
        reg(providingPlugin, command, consumer, registry, false);
    }

    static void registerAdmin(Plugin providingPlugin, String command, TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer) {
        reg(providingPlugin, command, consumer, adminRegistry, true);
    }

    private static void reg(Plugin providingPlugin, String command, TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer, @Nullable Map<String, Register> registry, boolean admin) {
        if (registry == null) {
            throw new IllegalStateException("Registration closed");
        }
        if (Objects.requireNonNull(providingPlugin) == FactionsPlugin.instance()) {
            throw new IllegalArgumentException("Use your own plugin!");
        }
        if (registry.put(Objects.requireNonNull(command), new Register(Objects.requireNonNull(consumer), providingPlugin, command)) == null) {
            AbstractFactionsPlugin.instance().getLogger().info("New " + (admin ? "admin " : "") + "command '" + command + "' registered by " + providingPlugin.getName());
        } else {
            AbstractFactionsPlugin.instance().getLogger().info("Replacement " + (admin ? "admin " : "") + "command '" + command + "' registered by " + providingPlugin.getName());
        }
    }

    private static void registerInternal(String command, Cmd cmd) {
        if (registry == null) {
            return;
        }
        registry.put(command, new Register(cmd.consumer(), AbstractFactionsPlugin.instance(), command));
    }

    private static void registerAdminInternal(String command, Cmd cmd) {
        if (adminRegistry == null) {
            return;
        }
        adminRegistry.put(command, new Register(cmd.consumer(), AbstractFactionsPlugin.instance(), command));
    }

    @SuppressWarnings("unused")
    private static void close() {
        AbstractFactionsPlugin plugin = AbstractFactionsPlugin.instance();

        CommandManager<Sender> manager = CommandsRoot.commandManager.get();

        manager.captionRegistry().registerProvider(new Captioner());

        var main = plugin.tl().commands().generic().getCommandRoot();
        var helpTl = plugin.tl().commands().help();

        MinecraftHelp<Sender> help = MinecraftHelp.<Sender>builder()
                .commandManager(manager)
                .audienceProvider(LilAudience::new)
                .commandPrefix("/" + main.getFirstAlias() + " " + helpTl.getFirstAlias())
                .commandFilter(command -> !command.commandMeta().contains(Cmd.HIDE_IN_HELP))
                .build();

        Command.Builder<Sender> builder = manager.commandBuilder(main.getFirstAlias(), main.getSecondaryAliases())
                .permission(Cloudy.predicate(sender -> WorldUtil.isEnabled(sender.sender())));
        registry.values().forEach(reg -> {
            try {
                reg.consumer.accept(manager, builder, help);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to register command '" + reg.command + "' from plugin '" + reg.providingPlugin.getName() + "'", e);
            }
        });

        var admin = plugin.tl().commands().generic().getCommandAdminRoot();
        Command.Builder<Sender> builderAdmin = manager.commandBuilder(admin.getFirstAlias(), admin.getSecondaryAliases())
                .permission(Cloudy.predicate(sender -> WorldUtil.isEnabled(sender.sender())));
        adminRegistry.values().forEach(reg -> {
            try {
                reg.consumer.accept(manager, builderAdmin, help);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to register admin command '" + reg.command + "' from plugin '" + reg.providingPlugin.getName() + "'", e);
            }
        });

        manager.command(
                builder.literal(helpTl.getFirstAlias(), helpTl.getSecondaryAliases())
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

        manager.command(builder.handler(context -> help.queryCommands("", context.sender())));
        manager.command(builderAdmin.permission(Cloudy.hasPermission("factions.kit.halfmod")).handler(context -> help.queryCommands("", context.sender())));

        registry = null; // Last step!
        adminRegistry = null;
        commandManager = null;
    }

    private static void register() {
        registerInternal("announce", new CmdAnnounce());
        registerInternal("ban", new CmdBan());
        registerInternal("chat", new CmdChat());
        registerInternal("claim", new CmdClaim());
        registerInternal("clear", new CmdClear());
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
        registerAdminInternal("tnt", new CmdAdminTNT());

    }
}
