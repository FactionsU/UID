package dev.kitteh.factions.landraidcontrol;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.event.DTRLossEvent;
import dev.kitteh.factions.integration.Essentials;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.upgrade.Upgrades;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.WorldUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Stream;

@NullMarked
public class DTRControl implements LandRaidControl {
    private static FactionsPlugin plugin = FactionsPlugin.getInstance();

    public static String round(double dtr) {
        return BigDecimal.valueOf(dtr).setScale(conf().getDecimalDigits(), RoundingMode.UP).toPlainString();
    }

    private static MainConfig.Factions.LandRaidControl.DTR conf() {
        return plugin.conf().factions().landRaidControl().dtr();
    }

    public DTRControl() {
        plugin = FactionsPlugin.getInstance();
    }

    @Override
    public boolean isRaidable(Faction faction) {
        return this.isRaidable(faction, faction.getDTR());
    }

    public boolean isRaidable(Faction faction, double dtr) {
        return !faction.isPeaceful() && dtr <= 0;
    }

    @Override
    public boolean hasLandInflation(Faction faction) {
        return false; // fail all attempts at claiming
    }

    @Override
    public int getLandLimit(Faction faction) {
        int boost = 0;
        int level = faction.getUpgradeLevel(Upgrades.DTR_CLAIM_LIMIT);
        if (level > 0) {
            boost = Universe.universe().upgradeSettings(Upgrades.DTR_CLAIM_LIMIT).costAt(level).intValue();
        }
        return boost + conf().getLandStarting() + (faction.getFPlayers().size() * conf().getLandPerPlayer());
    }

    @Override
    public boolean canJoinFaction(Faction faction, FPlayer player) {
        if (faction.isFrozenDTR() && conf().isFreezePreventsJoin()) {
            player.msg(TL.DTR_CANNOT_FROZEN);
            return false;
        }
        return true;
    }

    @Override
    public boolean canLeaveFaction(FPlayer player) {
        if (player.getFaction().isFrozenDTR() && conf().isFreezePreventsLeave()) {
            player.msg(TL.DTR_CANNOT_FROZEN);
            return false;
        }
        return true;
    }

    @Override
    public boolean canDisbandFaction(Faction faction, FPlayer playerAttempting) {
        if (faction.isFrozenDTR() && conf().isFreezePreventsDisband()) {
            playerAttempting.msg(TL.DTR_CANNOT_FROZEN);
            return false;
        }
        return true;
    }

    @Override
    public boolean canKick(FPlayer toKick, FPlayer playerAttempting) {
        if (toKick.getFaction().isNormal()) {
            Faction faction = toKick.getFaction();
            if (!FactionsPlugin.getInstance().conf().commands().kick().isAllowKickInEnemyTerritory() &&
                    Board.board().factionAt(toKick.getLastStoodAt()).getRelationTo(faction) == Relation.ENEMY) {
                playerAttempting.msg(TL.COMMAND_KICK_ENEMYTERRITORY);
                return false;
            }
            if (faction.isFrozenDTR() && conf().getFreezeKickPenalty() > 0) {
                faction.setDTR(Math.max(conf().getMinDTR(), faction.getDTR() - conf().getFreezeKickPenalty()));
                playerAttempting.msg(TL.DTR_KICK_PENALTY);
            }
        }
        return true;
    }

    @Override
    public void onRespawn(FPlayer player) {
        // Handled on death
    }

    @Override
    public void update(FPlayer player) {
        if (player.getFaction().isNormal()) {
            this.updateDTR(player.getFaction());
        }
    }

    @Override
    public void onDeath(Player player) {
        FPlayer fplayer = FPlayers.fPlayers().get(player);
        Faction faction = fplayer.getFaction();
        if (!faction.isNormal()) {
            return;
        }

        DTRLossEvent dtrLossEvent = new DTRLossEvent(faction, fplayer);
        if (AbstractFactionsPlugin.getInstance().getWorldguard() != null && AbstractFactionsPlugin.getInstance().getWorldguard().isNoLossFlag(player)) {
            dtrLossEvent.setCancelled(true);
        }

        // call Event
        Bukkit.getPluginManager().callEvent(dtrLossEvent);

        if (!dtrLossEvent.isCancelled()) {
            double startingDTR = faction.getDTR();
            faction.setDTR(Math.max(conf().getMinDTR(), faction.getDTR() - conf().getLossPerDeath(player.getWorld())));
            double diff = faction.getDTR() - startingDTR;
            double vamp = conf().getVampirism();
            if (player.getKiller() != null && vamp != 0D && diff > 0) {
                FPlayer fKiller = FPlayers.fPlayers().get(player.getKiller());
                if (faction != fKiller.getFaction()) {
                    double change = vamp * diff;
                    double startingOther = fKiller.getFaction().getDTR();
                    fKiller.getFaction().setDTR(Math.min(conf().getMaxDTR(), faction.getDTR() + change));
                    double killDiff = fKiller.getFaction().getDTR() - startingOther;
                    fKiller.msg(TL.DTR_VAMPIRISM_GAIN, killDiff, fplayer.describeTo(fKiller), fKiller.getFaction().getDTR());
                }
            }
            faction.setFrozenDTR(System.currentTimeMillis() + (conf().getFreezeTime() * 1000L));
        }
    }

    @Override
    public void onQuit(FPlayer player) {
        this.update(player);
    }

    @Override
    public void onJoin(FPlayer player) {
        if (player.getFaction().isNormal()) {
            this.updateDTR(player.getFaction(), 1);
        }
    }

    public void updateDTR(Faction faction) {
        this.updateDTR(faction, 0);
    }

    public void updateDTR(Faction faction, int minusPlayer) {
        long now = System.currentTimeMillis();
        if (faction.getFrozenDTRUntilTime() > now) {
            // Not yet time to regen
            return;
        }
        long millisPassed = now - Math.max(faction.getLastDTRUpdateTime(), faction.getFrozenDTRUntilTime());
        Stream<Player> stream = faction.getOnlinePlayers().stream().filter(p -> WorldUtil.isEnabled(p.getWorld()));
        if (FactionsPlugin.getInstance().conf().plugins().essentialsX().isPreventRegenWhileAfk()) {
            stream = stream.filter(Essentials::isAfk);
        }
        long onlineInEnabledWorlds = stream.count();
        double rate = Math.min(conf().getRegainPerMinuteMaxRate(), Math.max(0, onlineInEnabledWorlds - minusPlayer) * conf().getRegainPerMinutePerPlayer());
        double regain = (millisPassed / (60D * 1000D)) * rate;
        faction.setDTR(Math.min(faction.getDTRWithoutUpdate() + regain, this.getMaxDTR(faction)));
    }

    public double getMaxDTR(Faction faction) {
        return Math.min(conf().getStartingDTR() + (conf().getPerPlayer() * faction.getFPlayers().size()), conf().getMaxDTR());
    }

    public void onDTRChange(Faction faction, double start, double end) {
        boolean raidStart = this.isRaidable(faction, start);
        boolean raidEnd = this.isRaidable(faction, end);
        if (raidEnd && !raidStart) {
            this.announceRaidable(faction);
        } else if (raidStart && !raidEnd) {
            this.announceNotRaidable(faction);
        }
    }
}
