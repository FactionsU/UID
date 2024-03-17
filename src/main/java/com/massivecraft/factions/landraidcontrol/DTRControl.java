package com.massivecraft.factions.landraidcontrol;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.config.file.MainConfig;
import com.massivecraft.factions.event.DTRLossEvent;
import com.massivecraft.factions.integration.Essentials;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.util.TL;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Stream;

public class DTRControl implements LandRaidControl {
    private static FactionsPlugin plugin;

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
        return conf().getLandStarting() + (faction.getFPlayers().size() * conf().getLandPerPlayer());
    }

    @Override
    public boolean canJoinFaction(Faction faction, FPlayer player, CommandContext context) {
        if (faction.isFrozenDTR() && conf().isFreezePreventsJoin()) {
            context.msg(TL.DTR_CANNOT_FROZEN);
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
    public boolean canDisbandFaction(Faction faction, CommandContext context) {
        if (faction.isFrozenDTR() && conf().isFreezePreventsDisband()) {
            context.msg(TL.DTR_CANNOT_FROZEN);
            return false;
        }
        return true;
    }

    @Override
    public boolean canKick(FPlayer toKick, CommandContext context) {
        if (toKick.getFaction().isNormal()) {
            Faction faction = toKick.getFaction();
            if (!FactionsPlugin.getInstance().conf().commands().kick().isAllowKickInEnemyTerritory() &&
                    Board.getInstance().getFactionAt(toKick.getLastStoodAt()).getRelationTo(faction) == Relation.ENEMY) {
                context.msg(TL.COMMAND_KICK_ENEMYTERRITORY);
                return false;
            }
            if (faction.isFrozenDTR() && conf().getFreezeKickPenalty() > 0) {
                faction.setDTR(Math.max(conf().getMinDTR(), faction.getDTR() - conf().getFreezeKickPenalty()));
                context.msg(TL.DTR_KICK_PENALTY);
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
        FPlayer fplayer = FPlayers.getInstance().getByPlayer(player);
        Faction faction = fplayer.getFaction();
        if (!faction.isNormal()) {
            return;
        }

        DTRLossEvent dtrLossEvent = new DTRLossEvent(faction, fplayer);
        if (FactionsPlugin.getInstance().getWorldguard() != null && FactionsPlugin.getInstance().getWorldguard().isNoLossFlag(player)) {
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
                FPlayer fKiller = FPlayers.getInstance().getByPlayer(player.getKiller());
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
        Stream<Player> stream = faction.getOnlinePlayers().stream().filter(p -> plugin.worldUtil().isEnabled(p.getWorld()));
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
