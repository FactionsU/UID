package dev.kitteh.factions.plugin;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.command.paper.CmdUpgrades;
import dev.kitteh.factions.listener.ListenPaperChat;
import dev.kitteh.factions.scoreboard.BufferedObjective;
import dev.kitteh.factions.util.ComponentDispatcher;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ApiStatus.Internal
public class FactionsPluginPaper extends AbstractFactionsPlugin {
    @Override
    protected String pluginType() {
        return "fully-featured Paper";
    }

    @Override
    public void onPluginLoad() {
        ComponentDispatcher.setSenders(
                (commandSender, component) -> commandSender.sendMessage(component),
                (commandSender, component) -> commandSender.sendActionBar(component)
        );

        // Try to apply Paper's scoreboard NumberFormat if available; otherwise, keep default
        try {
            Class<?> nf = Class.forName("io.papermc.paper.scoreboard.numbers.NumberFormat", false, this.getClass().getClassLoader());
            Object blank = nf.getMethod("blank").invoke(null);
            BufferedObjective.objectiveConsumer = objective -> {
                try {
                    objective.getClass().getMethod("numberFormat", nf).invoke(objective, blank);
                } catch (Throwable ignored) {
                    // Silently ignore on older servers where the method/type isn't present
                }
            };
        } catch (Throwable ignored) {
            // API not present; do not alter the default objective consumer
        }
    }

    private interface PaperSender extends Sender {
        record PlayerImpl(CommandSourceStack commandSourceStack, CommandSender sender, org.bukkit.entity.Player player, FPlayer fPlayer, Faction faction) implements Sender.Player, PaperSender {
        }

        record ConsoleImpl(CommandSourceStack commandSourceStack, CommandSender sender) implements Sender.Console, PaperSender {
        }

        CommandSourceStack commandSourceStack();
    }

    @Override
    public void addCommands(BiConsumer<String, Cmd> reg, Consumer<Supplier<CommandManager<Sender>>> commandManager) {
        boolean hasPaperDialogs = false;
        try {
            // Detect availability of Paper Dialog API introduced in later 1.21.x
            ClassLoader cl = this.getClass().getClassLoader();
            Class.forName("io.papermc.paper.dialog.Dialog", false, cl);
            Class.forName("io.papermc.paper.registry.data.dialog.action.DialogAction", false, cl);
            hasPaperDialogs = true;
        } catch (ClassNotFoundException ignored) {
            this.getLogger().warning("Paper Dialog API not found. Falling back to inventory-based upgrades UI.");
        }

        if (hasPaperDialogs) {
            // Register Paper-specific upgrades command only when supported by the server
            reg.accept("upgrades", new CmdUpgrades());
        }

        commandManager.accept(() ->
                PaperCommandManager.<Sender>builder(SenderMapper.create(
                                css -> {
                                    if (css.getSender() instanceof Player player) {
                                        FPlayer fp = FPlayers.fPlayers().get(player);
                                        return new PaperSender.PlayerImpl(css, player, player, fp, fp.faction());
                                    } else {
                                        return new PaperSender.ConsoleImpl(css, css.getSender());
                                    }
                                },
                                sender -> ((PaperSender) sender).commandSourceStack()
                        )).executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                        .buildOnEnable(FactionsPluginPaper.this));

    }

    @Override
    protected void registerServerSpecificEvents() {
        this.getServer().getPluginManager().registerEvents(new ListenPaperChat(), this);
    }

    @Override
    public CompletableFuture<Boolean> teleport(Player player, Location location) {
        return player.teleportAsync(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    @Override
    protected String getPluginName() {
        return this.getPluginMeta().getName();
    }
}
