package dev.kitteh.factions.scoreboard;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Mini;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public abstract class FSidebarProvider {
    public static FSidebarProvider defaultSidebar() {
        return new FDefaultSidebar();
    }

    public static FSidebarProvider info(Faction faction) {
        return new FInfoSidebar(faction);
    }

    public abstract Component getTitle(FPlayer fplayer);

    public abstract List<Component> getLines(FPlayer fplayer);

    public Component process(String message, FPlayer fPlayer, Faction faction) {
        return Mini.parse(message, FPlayerResolver.of("player", fPlayer, fPlayer), FactionResolver.of(fPlayer, faction));
    }

    private static class FDefaultSidebar extends FSidebarProvider {
        @Override
        public Component getTitle(FPlayer fplayer) {
            String title;
            if (FactionsPlugin.instance().conf().scoreboard().constant().isFactionlessEnabled() && !fplayer.hasFaction()) {
                title = FactionsPlugin.instance().tl().scoreboard().constant().getFactionlessTitle();
            } else {
                title = FactionsPlugin.instance().tl().scoreboard().constant().getNormalTitle();
            }
            return this.process(title, fplayer, fplayer.faction());
        }

        @Override
        public List<Component> getLines(FPlayer fplayer) {
            List<String> content;
            if (FactionsPlugin.instance().conf().scoreboard().constant().isFactionlessEnabled() && !fplayer.hasFaction()) {
                content = FactionsPlugin.instance().tl().scoreboard().constant().getFactionlessContent();
            } else {
                content = FactionsPlugin.instance().tl().scoreboard().constant().getNormalContent();
            }
            return content.stream()
                    .map(line -> this.process(line, fplayer, fplayer.faction()))
                    .toList();
        }
    }

    private static class FInfoSidebar extends FSidebarProvider {
        private final Faction faction;

        public FInfoSidebar(Faction faction) {
            this.faction = faction;
        }

        @Override
        public Component getTitle(FPlayer fPlayer) {
            return this.process(FactionsPlugin.instance().tl().scoreboard().info().getTitle(), fPlayer, this.faction);
        }

        @Override
        public List<Component> getLines(FPlayer fplayer) {
            return FactionsPlugin.instance().tl().scoreboard().info().getContent()
                    .stream()
                    .map(line -> this.process(line, fplayer, this.faction))
                    .toList();
        }
    }
}
