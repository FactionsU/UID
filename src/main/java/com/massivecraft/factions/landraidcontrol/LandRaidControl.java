package com.massivecraft.factions.landraidcontrol;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.CommandContext;
import org.bukkit.entity.Player;

public interface LandRaidControl {
    static LandRaidControl getByName(String name) {
        switch (name.toLowerCase()) {
            case "dtr":
                return new DTRControl();
            case "power":
            default:
                return new PowerControl();
        }
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
}
