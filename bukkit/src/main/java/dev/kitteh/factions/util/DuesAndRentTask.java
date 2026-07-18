package dev.kitteh.factions.util;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.policy.DuesFailurePolicy;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.policy.RentFailurePolicy;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.event.FPlayerLeaveEvent;
import dev.kitteh.factions.event.FactionAutoDisbandEvent;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.plugin.Instances;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ApiStatus.Internal
public class DuesAndRentTask implements Runnable {
    private static boolean running = false;

    @Override
    public void run() {
        if (running || (!Econ.duesEnabled() && !Econ.rentEnabled())) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate lastCollected = Universe.universe().lastDuesCollectionDate();
        if (lastCollected != null && !today.isAfter(lastCollected)) { // Already done today!
            return;
        }

        running = true;
        try {
            Universe.universe().lastDuesCollectionDate(today);
            collect(today);
        } finally {
            running = false;
        }
    }

    private void collect(LocalDate today) {
        boolean log = Confs.main().logging().isMoneyTransactions();
        boolean dues = Econ.duesEnabled();
        boolean rent = Econ.rentEnabled();
        for (Faction faction : Factions.factions().all()) {
            if (!faction.isNormal()) {
                continue;
            }

            if (dues) {
                collectDues(faction, today, log);
            }
            // May need dues to pay rent!
            if (rent) {
                collectRent(faction, today, log);
            }
        }
        Instances.PLAYERS.forceSave(false);
        Instances.FACTIONS.forceSave(false);
        Instances.UNIVERSE.forceSave(false);
    }

    private void collectDues(Faction faction, LocalDate today, boolean log) {
        DuesFailurePolicy policy = faction.duesFailurePolicy();
        List<FPlayer> toDismiss = new ArrayList<>();

        for (FPlayer member : new ArrayList<>(faction.members())) {
            double owed = faction.dues(member.role()) + member.duesDebt();
            if (owed <= 0) {
                continue;
            }

            if (Econ.transferMoney(faction, member, faction, owed, false)) {
                member.duesDebt(0);
                if (log) {
                    AbstractFactionsPlugin.instance().log(String.format("Collected %s in daily dues from %s for faction bank: %s", Econ.moneyString(owed), member.name(), faction.tag()));
                }
                continue;
            }

            // Uh oh, couldn't pay it!

            if (Econ.has(member, owed)) {
                member.addMissedDuesDate(today);
                member.duesDebt(owed);
                AbstractFactionsPlugin.instance().log(String.format("%s could not afford %s in daily dues for faction %s, but they could pay. Adding debt, assumed plugin error.", member.name(), Econ.moneyString(owed), faction.tag()));
                continue;
            }

            switch (policy) {
                case RECORD -> member.addMissedDuesDate(today);
                case DEMOTE -> {
                    Role demoted = Role.getRelative(member.role(), -1);
                    if (demoted != null) {
                        member.role(demoted);
                        if (member.asPlayer() instanceof Player player) {
                            player.updateCommands();
                        }
                    }
                }
                case DEBT -> {
                    member.addMissedDuesDate(today);
                    member.duesDebt(owed);
                }
                case DISMISS -> toDismiss.add(member);
            }

            if (log) {
                AbstractFactionsPlugin.instance().log(String.format("%s could not afford %s in daily dues for faction %s; applying policy %s", member.name(), Econ.moneyString(owed), faction.tag(), policy.name()));
            }
        }

        for (FPlayer member : toDismiss) {
            member.leave(false);
        }
    }

    private void collectRent(Faction faction, LocalDate today, boolean log) {
        int logDays = Confs.main().economy().getRentMissedPaymentLogDays();
        faction.pruneMissedRentDatesBefore(logDays > 0 ? today.minusDays(Math.min(logDays, 3000)) : LocalDate.MAX);

        double owed = Econ.calculateRent(faction) + faction.rentDebt();
        if (owed <= 0) {
            return;
        }

        if (Econ.has(faction, owed)) {
            Econ.modifyMoney(faction, -owed);
            Econ.modifyRentGatheringAccountMoney(owed);
            faction.rentDebt(0);
            faction.consecutiveMissedRentDays(0); // C-C-C-Combo breaker
            if (log) {
                AbstractFactionsPlugin.instance().log(String.format("Collected %s in daily rent from faction bank: %s", Econ.moneyString(owed), faction.tag()));
            }
            return;
        }

        if (logDays > 0) {
            faction.addMissedRentDate(today);
        }

        RentFailurePolicy policy = Confs.main().economy().getRentFailurePolicy();
        switch (policy) {
            case DEBT -> {
                faction.rentDebt(owed);
                int streak = faction.consecutiveMissedRentDays() + 1;
                faction.consecutiveMissedRentDays(streak);
                int maxMissed = Confs.main().economy().getRentDisbandAfterConsecutiveMissedDays();
                if (maxMissed > 0 && streak >= maxMissed) {
                    if (log) {
                        AbstractFactionsPlugin.instance().log(String.format("Faction %s disbanded after missing rent %d days in a row.", faction.tag(), streak));
                    }
                    disband(faction);
                    return;
                }
            }
            case UNCLAIM_ALL -> {
                Board.board().unclaimAll(faction);
                faction.rentDebt(0);
                faction.consecutiveMissedRentDays(0);
            }
            case UNCLAIM_UNTIL_AFFORD -> unclaimUntilAfford(faction);
            case DISBAND -> disband(faction);
        }

        if (log) {
            AbstractFactionsPlugin.instance().log(String.format("Faction %s could not afford %s in daily rent; applying policy %s", faction.tag(), Econ.moneyString(owed), policy.name()));
        }
    }

    private void unclaimUntilAfford(Faction faction) {
        List<FLocation> claims = new ArrayList<>(faction.claims());
        claims.sort(Comparator.comparingLong(DuesAndRentTask::inhabitedTime));

        int index = 0;
        double owed = Econ.calculateRent(faction) + faction.rentDebt();
        while (owed > 0 && !Econ.has(faction, owed) && index < claims.size()) {
            Board.board().unclaim(claims.get(index));
            index++;
            owed = Econ.calculateRent(faction) + faction.rentDebt();
        }

        if (owed <= 0 || Econ.has(faction, owed)) {
            if (owed > 0) {
                Econ.modifyMoney(faction, -owed);
                Econ.modifyRentGatheringAccountMoney(owed);
            }
            faction.rentDebt(0);
            faction.consecutiveMissedRentDays(0);
        } else {
            faction.rentDebt(owed);
        }
    }

    private void disband(Faction faction) {
        for (FPlayer member : new ArrayList<>(faction.members())) {
            Bukkit.getServer().getPluginManager().callEvent(new FPlayerLeaveEvent(member, faction, FPlayerLeaveEvent.Reason.DISBAND));
        }
        Bukkit.getServer().getPluginManager().callEvent(new FactionAutoDisbandEvent(faction));
        Factions.factions().remove(faction);
    }

    private static long inhabitedTime(FLocation location) {
        long time = location.cachedInhabitedTime();
        return time == -1L ? Long.MAX_VALUE : time;
    }
}
