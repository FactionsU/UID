package dev.kitteh.factions.landraidcontrol;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.util.TL;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.util.stream.Stream;

@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public interface LandRaidControl {
    static LandRaidControl getByName(String name) {
        return switch (name.toLowerCase()) {
            case "dtr" -> new DTRControl();
            default -> new PowerControl();
        };
    }

    boolean isRaidable(Faction faction);

    boolean hasLandInflation(Faction faction);

    int landLimit(Faction faction);

    default int possibleClaimCount(Faction faction) {
        return this.landLimit(faction) - faction.claimCount();
    }

    boolean canJoinFaction(Faction faction, FPlayer player);

    boolean canLeaveFaction(FPlayer player);

    boolean canDisbandFaction(Faction faction, FPlayer playerAttempting);

    boolean canKick(FPlayer toKick, FPlayer playerAttempting);

    void onRespawn(FPlayer player);

    void onDeath(Player player);

    void onQuit(FPlayer player);

    void onJoin(FPlayer player);

    void update(FPlayer player);

    default void announceRaidable(Faction faction) {
        if (FactionsPlugin.instance().conf().factions().landRaidControl().isAnnounceRaidable()) {
            Stream<FPlayer> stream = FPlayers.fPlayers().online().stream();
            if (FactionsPlugin.instance().conf().factions().landRaidControl().isAnnounceToEnemyOnly()) {
                stream = stream.filter(fp -> fp.faction() == faction || fp.relationTo(faction) == Relation.ENEMY);
            }
            stream.forEach(fp -> fp.sendMessageLegacy(TL.RAIDABLE_NOWRAIDABLE.format(faction.tagLegacy(fp))));
        }
    }

    default void announceNotRaidable(Faction faction) {
        if (FactionsPlugin.instance().conf().factions().landRaidControl().isAnnounceNotRaidable()) {
            Stream<FPlayer> stream = FPlayers.fPlayers().online().stream();
            if (FactionsPlugin.instance().conf().factions().landRaidControl().isAnnounceToEnemyOnly()) {
                stream = stream.filter(fp -> fp.faction() == faction || fp.relationTo(faction) == Relation.ENEMY);
            }
            stream.forEach(fp -> fp.sendMessageLegacy(TL.RAIDABLE_NOLONGERRAIDABLE.format(faction.tagLegacy(fp))));
        }
    }
}
