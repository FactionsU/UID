package dev.kitteh.factions.scoreboard;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public class FScoreboard {
    private static final Map<FPlayer, FScoreboard> fscoreboards = new HashMap<>();

    private final Scoreboard scoreboard;
    private final FPlayer fplayer;
    private final BufferedObjective bufferedObjective;
    private FSidebarProvider temporaryProvider;
    private boolean removed = false;

    public static void init(Player player, FPlayer fplayer) {
        FScoreboard fboard = new FScoreboard(player, fplayer);
        fscoreboards.put(fplayer, fboard);

        if (fplayer.hasFaction()) {
            FTeamWrapper.applyUpdates(fplayer.faction());
        }
        FTeamWrapper.track(fboard);
    }

    public static void remove(FPlayer fplayer, Player player) {
        FScoreboard fboard = fscoreboards.remove(fplayer);

        if (fboard != null) {
            if (fboard.scoreboard == player.getScoreboard()) {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
            fboard.removed = true;
            FTeamWrapper.untrack(fboard);
        }
    }

    public static FScoreboard get(FPlayer fplayer) {
        return fscoreboards.get(fplayer);
    }

    private FScoreboard(Player player, FPlayer fplayer) {
        this.fplayer = fplayer;

        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.bufferedObjective = new BufferedObjective(scoreboard);

        player.setScoreboard(scoreboard);

        updateObjective();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (removed) {
                    cancel();
                    return;
                }

                if (temporaryProvider == null) {
                    updateObjective();
                }
            }
        }.runTaskTimer(AbstractFactionsPlugin.instance(), 20, 20);
    }

    protected FPlayer getFPlayer() {
        return fplayer;
    }

    protected Scoreboard getScoreboard() {
        return scoreboard;
    }

    public void setSidebarVisibility(boolean visible) {
        bufferedObjective.setDisplaySlot(visible ? DisplaySlot.SIDEBAR : null);
    }

    public void setTemporarySidebar(Faction faction) {
        final FSidebarProvider provider = FSidebarProvider.info(faction);
        temporaryProvider = provider;
        updateObjective();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (removed) {
                    return;
                }

                if (temporaryProvider == provider) {
                    temporaryProvider = null;
                    updateObjective();
                }
            }
        }.runTaskLater(AbstractFactionsPlugin.instance(), FactionsPlugin.instance().conf().scoreboard().info().getExpiration() * 20L);
    }

    private void updateObjective() {
        FSidebarProvider provider = temporaryProvider != null ? temporaryProvider : FSidebarProvider.defaultSidebar();

        bufferedObjective.setTitle(provider.getTitle(fplayer));
        bufferedObjective.setAllLines(provider.getLines(fplayer));
        bufferedObjective.flip();
    }
}
