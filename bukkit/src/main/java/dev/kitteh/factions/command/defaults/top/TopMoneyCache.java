package dev.kitteh.factions.command.defaults.top;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.integration.Econ;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class TopMoneyCache {
    private static final TopMoneyCache INSTANCE = new TopMoneyCache();

    public static TopMoneyCache get() {
        return INSTANCE;
    }

    private static int taskId = -1;

    public static void start(Plugin plugin) {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        if (taskId != -1) {
            scheduler.cancelTask(taskId);
            taskId = -1;
        }
        int seconds = Confs.main().economy().getTopBalanceCacheOnlineRefreshSeconds();
        if (seconds <= 0) {
            return;
        }
        long expireSeconds = expireSeconds();
        INSTANCE.playerBalances = newBalanceCache(expireSeconds);
        INSTANCE.factionBalances = newBalanceCache(expireSeconds);
        long ticks = 20L * seconds;
        taskId = scheduler.scheduleSyncRepeatingTask(plugin, INSTANCE::refresh, 20L, ticks);
    }

    private static final int EXPIRE_INTERVAL_MULTIPLE = 10;

    private static long expireSeconds() {
        var economy = Confs.main().economy();
        long onlineSeconds = Math.max(1, economy.getTopBalanceCacheOnlineRefreshSeconds());
        long offlineSeconds = Math.max(1, economy.getTopBalanceCacheOfflineRefreshMinutes() * 60L);
        return EXPIRE_INTERVAL_MULTIPLE * Math.max(onlineSeconds, offlineSeconds);
    }

    private static <K> Cache<K, Double> newBalanceCache(long expireSeconds) {
        return CacheBuilder.newBuilder().expireAfterWrite(expireSeconds, TimeUnit.SECONDS).build();
    }

    private Cache<UUID, Double> playerBalances = newBalanceCache(TimeUnit.HOURS.toSeconds(1));
    private Cache<Integer, Double> factionBalances = newBalanceCache(TimeUnit.HOURS.toSeconds(1));
    private final Deque<FPlayer> offlineQueue = new ArrayDeque<>();

    private TopMoneyCache() {
    }

    public double factionTotal(Faction faction) {
        Double bank = factionBalances.getIfPresent(faction.id());
        double total = bank == null ? 0.0 : bank;
        for (FPlayer member : faction.members()) {
            Double balance = playerBalances.getIfPresent(member.uniqueId());
            if (balance != null) {
                total += balance;
            }
        }
        return total;
    }

    public void refresh() {
        if (!Econ.shouldBeUsed()) {
            return;
        }

        if (Confs.main().economy().isBankEnabled()) {
            for (Faction faction : Factions.factions().all()) {
                if (faction.isNormal()) {
                    factionBalances.put(faction.id(), Econ.getBalance(faction));
                }
            }
        }

        Collection<FPlayer> all = FPlayers.fPlayers().all();
        int onlineCount = 0;
        for (FPlayer fPlayer : all) {
            if (fPlayer.isOnline()) {
                playerBalances.put(fPlayer.uniqueId(), Econ.getBalance(fPlayer));
                onlineCount++;
            }
        }

        refreshOfflineBatch(all, all.size() - onlineCount);
    }

    private void refreshOfflineBatch(Collection<FPlayer> all, int offlineTotal) {
        if (offlineTotal <= 0) {
            return;
        }

        var economy = Confs.main().economy();
        double refreshSeconds = Math.max(1, economy.getTopBalanceCacheOnlineRefreshSeconds());
        double offlineSeconds = Math.max(refreshSeconds, economy.getTopBalanceCacheOfflineRefreshMinutes() * 60.0);
        double runsPerOfflineInterval = offlineSeconds / refreshSeconds;
        int batch = (int) Math.max(1, Math.ceil(offlineTotal / runsPerOfflineInterval));

        for (int refreshed = 0; refreshed < batch; refreshed++) {
            if (offlineQueue.isEmpty()) {
                for (FPlayer fPlayer : all) {
                    if (!fPlayer.isOnline()) {
                        offlineQueue.add(fPlayer);
                    }
                }
                if (offlineQueue.isEmpty()) {
                    return;
                }
            }
            FPlayer fPlayer = offlineQueue.poll();
            if (!fPlayer.isOnline()) {
                playerBalances.put(fPlayer.uniqueId(), Econ.getBalance(fPlayer));
            }
        }
    }
}
