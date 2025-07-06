package dev.kitteh.factions.plugin;

import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.ThirdPartyCommands;
import dev.kitteh.factions.command.paper.CmdUpgrades;
import dev.kitteh.factions.listener.FactionsPaperChatListener;
import dev.kitteh.factions.scoreboard.BufferedObjective;
import dev.kitteh.factions.util.ComponentDispatcher;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

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

        BufferedObjective.objectiveConsumer = objective -> objective.numberFormat(NumberFormat.blank());
    }

    @Override
    public void addCommands(BiConsumer<String, Cmd> reg, BiConsumer<String, Cmd> adminReg) {
        reg.accept("upgrades", new CmdUpgrades());
    }

    @Override
    protected void registerEvents() {
        this.getServer().getPluginManager().registerEvents(new FactionsPaperChatListener(), this);
    }

    @Override
    public CompletableFuture<Boolean> teleport(Player player, Location location) {
        return player.teleportAsync(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }
}
