package dev.kitteh.factions.plugin;

import dev.kitteh.factions.listener.FactionsPaperChatListener;
import dev.kitteh.factions.scoreboard.BufferedObjective;
import dev.kitteh.factions.util.ComponentDispatcher;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.audience.Audience;

public class FactionsPluginPaper extends AbstractFactionsPlugin {
    @Override
    protected String pluginType() {
        return "fully-featured Paper";
    }

    @Override
    public void onPluginLoad() {
        ComponentDispatcher.setSenders(
                (commandSender, component) -> commandSender.sendMessage(component),
                Audience::sendActionBar
        );

        BufferedObjective.objectiveConsumer = objective -> objective.numberFormat(NumberFormat.blank());
    }

    @Override
    protected void registerEvents() {
        this.getServer().getPluginManager().registerEvents(new FactionsPaperChatListener(), this);
    }
}
