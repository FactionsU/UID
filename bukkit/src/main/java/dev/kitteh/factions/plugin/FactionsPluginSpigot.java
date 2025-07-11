package dev.kitteh.factions.plugin;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.listener.FactionsLegacyChatListener;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.SimplePluginManager;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FactionsPluginSpigot extends AbstractFactionsPlugin {
    @Override
    protected String pluginType() {
        return "Spigot";
    }

    @Override
    public void onPluginLoad() {
        // Fix for legacy dependency system's grumpiness about not defining softdepends because it's a nightmare
        try {
            Field depGraph = SimplePluginManager.class.getDeclaredField("dependencyGraph");
            depGraph.setAccessible(true);
            Object graph = depGraph.get(this.getServer().getPluginManager());
            Method putEdge = graph.getClass().getDeclaredMethod("putEdge", Object.class, Object.class);
            putEdge.setAccessible(true);
            String pluginName = this.getDescription().getName();
            for (String depend : this.integrationManager().integrationNames()) {
                putEdge.invoke(graph, pluginName, depend);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void registerEvents() {
        this.getServer().getPluginManager().registerEvents(new FactionsLegacyChatListener(this), this);
    }

    @Override
    public CompletableFuture<Boolean> teleport(Player player, Location location) {
        return CompletableFuture.completedFuture(player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN));
    }

    @Override
    public void addCommands(BiConsumer<String, Cmd> reg, BiConsumer<String, Cmd> adminReg, Consumer<Supplier<CommandManager<Sender>>> commandManager) {
        commandManager.accept(() -> {
            LegacyPaperCommandManager<Sender> manager = new LegacyPaperCommandManager<>(
                    FactionsPluginSpigot.this,
                    ExecutionCoordinator.simpleCoordinator(),
                    SenderMapper.create(
                            sender -> {
                                if (sender instanceof org.bukkit.entity.Player player) {
                                    FPlayer fp = FPlayers.fPlayers().get(player);
                                    return new Sender.Player.Impl(sender, player, fp, fp.faction());
                                } else {
                                    return new Sender.Console.Impl(sender);
                                }
                            },
                            Sender::sender
                    )
            );
            if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
                manager.registerBrigadier();
            }
            return manager;
        });
    }
}
