package dev.kitteh.factions.command;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.defaults.CmdAnnounce;
import dev.kitteh.factions.command.defaults.CmdBan;
import dev.kitteh.factions.command.defaults.CmdChat;
import dev.kitteh.factions.command.defaults.CmdClaim;
import dev.kitteh.factions.command.defaults.CmdConfirm;
import dev.kitteh.factions.command.defaults.CmdCoords;
import dev.kitteh.factions.command.defaults.CmdCreate;
import dev.kitteh.factions.command.defaults.CmdDTR;
import dev.kitteh.factions.command.defaults.CmdDisband;
import dev.kitteh.factions.command.defaults.CmdFly;
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
import dev.kitteh.factions.command.defaults.CmdShow;
import dev.kitteh.factions.command.defaults.CmdStatus;
import dev.kitteh.factions.command.defaults.CmdStuck;
import dev.kitteh.factions.command.defaults.CmdTNT;
import dev.kitteh.factions.command.defaults.CmdToggle;
import dev.kitteh.factions.command.defaults.CmdTop;
import dev.kitteh.factions.command.defaults.CmdUnban;
import dev.kitteh.factions.command.defaults.CmdUnclaim;
import dev.kitteh.factions.command.defaults.CmdVault;
import dev.kitteh.factions.command.defaults.CmdVersion;
import dev.kitteh.factions.command.defaults.CmdWarp;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CommandsRoot {
    private record Register(BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer,
                            Plugin providingPlugin, String command) {
    }

    private static Map<String, Register> registry = new ConcurrentHashMap<>();
    private static Map<String, Register> adminRegistry = new ConcurrentHashMap<>();

    public static void register(Plugin providingPlugin, String command, BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer) {
        if (registry == null) {
            throw new IllegalStateException("Registration closed");
        }
        if (providingPlugin == FactionsPlugin.getInstance()) {
            throw new IllegalStateException("Use your own plugin!");
        }
        registry.put(command, new Register(consumer, providingPlugin, command));
    }

    public static void registerAdmin(Plugin providingPlugin, String command, BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer) {
        if (adminRegistry == null) {
            throw new IllegalStateException("Registration closed");
        }
        if (providingPlugin == FactionsPlugin.getInstance()) {
            throw new IllegalStateException("Use your own plugin!");
        }
        adminRegistry.put(command, new Register(consumer, providingPlugin, command));
    }

    private static void registerInternal(String command, Cmd cmd) {
        if (registry.containsKey(command)) {
            return; // Silent death? TODO
        }
        registry.put(command, new Register(cmd.consumer(), FactionsPlugin.getInstance(), command));
    }

    private static void registerAdminInternal(String command, Cmd cmd) {
        if (adminRegistry.containsKey(command)) {
            return; // Silent death? TODO
        }
        adminRegistry.put(command, new Register(cmd.consumer(), FactionsPlugin.getInstance(), command));
    }

    public CommandsRoot(FactionsPlugin plugin) {
        this.register();

        LegacyPaperCommandManager<Sender> manager = new LegacyPaperCommandManager<>(plugin, ExecutionCoordinator.simpleCoordinator(), SenderMapper.create(Sender::of, Sender::sender));
        if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            manager.registerBrigadier();
        }

        manager.captionRegistry().registerProvider(new Captioner<>());

        List<String> aliases = new ArrayList<>(plugin.conf().getCommandBase());
        String main = aliases.removeFirst();
        Command.Builder<Sender> builder = manager.commandBuilder(main, aliases.toArray(new String[0]))
                .permission(Cloudy.predicate(sender -> WorldUtil.isEnabled(sender.sender())));
        registry.values().forEach(reg -> {
            try {
                reg.consumer.accept(manager, builder);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to register command '" + reg.command + "' from plugin '" + reg.providingPlugin.getName() + "'", e);
            }
        });

        Command.Builder<Sender> builderAdmin = manager.commandBuilder("fa")
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
                        .handler(context -> {
                            help.queryCommands(context.get("query"), context.sender());
                        })
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
        registerInternal("show", new CmdShow());
        registerInternal("status", new CmdStatus());
        registerInternal("stuck", new CmdStuck());
        registerInternal("tnt", new CmdTNT());
        registerInternal("toggle", new CmdToggle());
        registerInternal("top", new CmdTop());
        registerInternal("unban", new CmdUnban());
        registerInternal("unclaim", new CmdUnclaim());
        registerInternal("version", new CmdVersion());
        registerInternal("warp", new CmdWarp());

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
