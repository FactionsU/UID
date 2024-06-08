package com.massivecraft.factions.landraidcontrol;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.util.TL;
import org.bukkit.entity.Player;

import java.util.stream.Stream;

public interface LandRaidControl {
    static LandRaidControl getByName(String name) {
        return switch (name.toLowerCase()) {
            case "dtr" -> new DTRControl();
            default -> new PowerControl();
        };
    }

    boolean isRaidable(Faction faction);

    boolean hasLandInflation(Faction faction);

    int getLandLimit(Faction faction);

    default int getPossibleClaimCount(Faction faction) {
        return this.getLandLimit(faction) - faction.getLandRounded();
    }

    boolean canJoinFaction(Faction faction, FPlayer player, CommandContext context);

    boolean canLeaveFaction(FPlayer player);

    boolean canDisbandFaction(Faction faction, CommandContext context);

    boolean canKick(FPlayer toKick, CommandContext context);

    void onRespawn(FPlayer player);

    void onDeath(Player player);

    void onQuit(FPlayer player);

    void onJoin(FPlayer player);

    void update(FPlayer player);

    default void announceRaidable(Faction faction) {
        if (FactionsPlugin.getInstance().conf().factions().landRaidControl().isAnnounceRaidable()) {
            Stream<FPlayer> stream = FPlayers.getInstance().getOnlinePlayers().stream();
            if (FactionsPlugin.getInstance().conf().factions().landRaidControl().isAnnounceToEnemyOnly()) {
                stream = stream.filter(fp -> fp.getFaction() == faction || fp.getRelationTo(faction) == Relation.ENEMY);
            }
            stream.forEach(fp -> fp.sendMessage(TL.RAIDABLE_NOWRAIDABLE.format(faction.getTag(fp))));
        }
    }

    default void announceNotRaidable(Faction faction) {
        if (FactionsPlugin.getInstance().conf().factions().landRaidControl().isAnnounceRaidable()) {
            Stream<FPlayer> stream = FPlayers.getInstance().getOnlinePlayers().stream();
            if (FactionsPlugin.getInstance().conf().factions().landRaidControl().isAnnounceToEnemyOnly()) {
                stream = stream.filter(fp -> fp.getFaction() == faction || fp.getRelationTo(faction) == Relation.ENEMY);
            }
            stream.forEach(fp -> fp.sendMessage(TL.RAIDABLE_NOLONGERRAIDABLE.format(faction.getTag(fp))));
        }
    }
}
