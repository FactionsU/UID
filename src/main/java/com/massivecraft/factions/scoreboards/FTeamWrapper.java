package com.massivecraft.factions.scoreboards;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.config.file.MainConfig;
import com.massivecraft.factions.tag.Tag;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FTeamWrapper {
    private static final Map<Faction, FTeamWrapper> wrappers = new HashMap<>();
    private static final List<FScoreboard> tracking = new ArrayList<>();
    private static int factionTeamPtr;
    private static final Set<Faction> updating = new HashSet<>();

    private final Map<FScoreboard, Team> teams = new HashMap<>();
    private final String teamName;
    private final Faction faction;
    private final Set<OfflinePlayer> members = new HashSet<>();

    public static void applyUpdatesLater(final Faction faction) {
        if (!FScoreboard.isSupportedByServer()) {
            return;
        }

        if (faction.isWilderness()) {
            return;
        }

        if (!FactionsPlugin.getInstance().conf().scoreboard().constant().isPrefixes() && !FactionsPlugin.getInstance().conf().scoreboard().constant().isSuffixes()) {
            return;
        }


        if (updating.add(faction)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    updating.remove(faction);
                    applyUpdates(faction);
                }
            }.runTask(FactionsPlugin.getInstance());
        }
    }

    public static void applyUpdates(Faction faction) {
        if (!FScoreboard.isSupportedByServer()) {
            return;
        }

        if (faction.isWilderness()) {
            return;
        }

        if (!FactionsPlugin.getInstance().conf().scoreboard().constant().isPrefixes() && !FactionsPlugin.getInstance().conf().scoreboard().constant().isSuffixes()) {
            return;
        }

        if (updating.contains(faction)) {
            // Faction will be updated soon.
            return;
        }

        FTeamWrapper wrapper = wrappers.get(faction);
        Set<FPlayer> factionMembers = faction.getFPlayers();

        if (wrapper != null && Factions.getInstance().getFactionById(faction.getId()) == null) {
            // Faction was disbanded
            wrapper.unregister();
            wrappers.remove(faction);
            return;
        }

        if (wrapper == null) {
            wrapper = new FTeamWrapper(faction);
            wrappers.put(faction, wrapper);
        }

        for (OfflinePlayer player : wrapper.getPlayers()) {
            if (!player.isOnline() || !factionMembers.contains(FPlayers.getInstance().getByOfflinePlayer(player))) {
                // Player is offline or no longer in faction
                wrapper.removePlayer(player);
            }
        }

        for (FPlayer fmember : factionMembers) {
            if (!fmember.isOnline()) {
                continue;
            }

            // Scoreboard might not have player; add him/her
            wrapper.addPlayer(fmember.getPlayer());
        }

        wrapper.updatePrefixesAndSuffixes();
    }

    public static void updatePrefixes(Faction faction) {
        if (!FScoreboard.isSupportedByServer()) {
            return;
        }

        if (!wrappers.containsKey(faction)) {
            applyUpdates(faction);
        } else {
            wrappers.get(faction).updatePrefixesAndSuffixes();
        }
    }

    protected static void track(FScoreboard fboard) {
        if (!FScoreboard.isSupportedByServer()) {
            return;
        }
        tracking.add(fboard);
        for (FTeamWrapper wrapper : wrappers.values()) {
            wrapper.add(fboard);
        }
    }

    protected static void untrack(FScoreboard fboard) {
        if (!FScoreboard.isSupportedByServer()) {
            return;
        }
        tracking.remove(fboard);
        for (FTeamWrapper wrapper : wrappers.values()) {
            wrapper.remove(fboard);
        }
    }


    private FTeamWrapper(Faction faction) {
        this.teamName = "faction_" + (factionTeamPtr++);
        this.faction = faction;

        for (FScoreboard fboard : tracking) {
            add(fboard);
        }
    }

    private void add(FScoreboard fboard) {
        Scoreboard board = fboard.getScoreboard();
        Team team = board.registerNewTeam(teamName);
        teams.put(fboard, team);

        for (OfflinePlayer player : getPlayers()) {
            team.addPlayer(player);
        }

        updatePrefixAndSuffix(fboard);
    }

    private void remove(FScoreboard fboard) {
        teams.remove(fboard).unregister();
    }

    private void updatePrefixesAndSuffixes() {
        if (FactionsPlugin.getInstance().conf().scoreboard().constant().isPrefixes() || FactionsPlugin.getInstance().conf().scoreboard().constant().isSuffixes()) {
            for (FScoreboard fboard : teams.keySet()) {
                updatePrefixAndSuffix(fboard);
            }
        }
    }

    private void updatePrefixAndSuffix(FScoreboard fboard) {
        MainConfig.Scoreboard.Constant conf = FactionsPlugin.getInstance().conf().scoreboard().constant();
        if (conf.isPrefixes()) {
            Team team = teams.get(fboard);
            String prefix = this.apply(conf.getPrefixTemplate(), fboard.getFPlayer(), conf.getPrefixLength());

            if (!prefix.equals(team.getPrefix())) {
                team.setPrefix(prefix);
            }
        }
        if (conf.isSuffixes()) {
            Team team = teams.get(fboard);
            String suffix = this.apply(conf.getSuffixTemplate(), fboard.getFPlayer(), conf.getSuffixLength());

            if (!suffix.equals(team.getSuffix())) {
                team.setSuffix(suffix);
            }
        }
    }

    private String apply(String prefixOrSuffix, FPlayer fplayer, int maxLength) {
        prefixOrSuffix = Tag.parsePlaceholders(fplayer.getPlayer(), prefixOrSuffix);
        prefixOrSuffix = prefixOrSuffix.replace("{relationcolor}", faction.getRelationTo(fplayer).getColor().toString());
        int remaining = Math.min("{faction}".length() + maxLength - prefixOrSuffix.length(), faction.getTag().length());
        prefixOrSuffix = prefixOrSuffix.replace("{faction}", remaining > 0 ? faction.getTag().substring(0, remaining) : "");
        prefixOrSuffix = Tag.parsePlain(fplayer, prefixOrSuffix);
        prefixOrSuffix = ChatColor.translateAlternateColorCodes('&', prefixOrSuffix);

        if (prefixOrSuffix.length() > maxLength) {
            prefixOrSuffix = prefixOrSuffix.substring(0, maxLength);
        }
        return prefixOrSuffix;
    }

    private void addPlayer(OfflinePlayer player) {
        if (members.add(player)) {
            for (Team team : teams.values()) {
                team.addPlayer(player);
            }
        }
    }

    private void removePlayer(OfflinePlayer player) {
        if (members.remove(player)) {
            for (Team team : teams.values()) {
                team.removePlayer(player);
            }
        }
    }

    private Set<OfflinePlayer> getPlayers() {
        return new HashSet<>(this.members);
    }

    private void unregister() {
        for (Team team : teams.values()) {
            team.unregister();
        }
        teams.clear();
    }
}
