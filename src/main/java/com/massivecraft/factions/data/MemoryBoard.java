package com.massivecraft.factions.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.integration.LWC;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.util.AsciiCompass;
import com.massivecraft.factions.util.TL;
import com.massivecraft.factions.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public abstract class MemoryBoard extends Board {

    public class MemoryBoardMap extends HashMap<FLocation, String> {
        private static final long serialVersionUID = -6689617828610585368L;

        final Multimap<String, FLocation> factionToLandMap = HashMultimap.create();

        @Override
        public String put(FLocation floc, String factionId) {
            String previousValue = super.put(floc, factionId);
            if (previousValue != null) {
                factionToLandMap.remove(previousValue, floc);
            }

            factionToLandMap.put(factionId, floc);
            return previousValue;
        }

        @Override
        public String remove(Object key) {
            String result = super.remove(key);
            if (result != null) {
                FLocation floc = (FLocation) key;
                factionToLandMap.remove(result, floc);
            }

            return result;
        }

        @Override
        public void clear() {
            super.clear();
            factionToLandMap.clear();
        }

        public int getOwnedLandCount(String factionId) {
            return factionToLandMap.get(factionId).size();
        }

        public void removeFaction(String factionId) {
            Collection<FLocation> fLocations = factionToLandMap.removeAll(factionId);
            for (FPlayer fPlayer : FPlayers.getInstance().getOnlinePlayers()) {
                if (fLocations.contains(fPlayer.getLastStoodAt())) {
                    if (FactionsPlugin.getInstance().conf().commands().fly().isEnable() && !fPlayer.isAdminBypassing() && fPlayer.isFlying()) {
                        fPlayer.setFlying(false);
                    }
                    if (fPlayer.isWarmingUp()) {
                        fPlayer.clearWarmup();
                        fPlayer.msg(TL.WARMUPS_CANCELLED);
                    }
                }
            }
            for (FLocation floc : fLocations) {
                super.remove(floc);
            }
        }
    }

    private final char[] mapKeyChrs = "\\/#$%=&^ABCDEFGHJKLMNOPQRSTUVWXYZ1234567890abcdeghjmnopqrsuvwxyz?".toCharArray();

    public MemoryBoardMap flocationIds = new MemoryBoardMap();

    //----------------------------------------------//
    // Get and Set
    //----------------------------------------------//
    public String getIdAt(FLocation flocation) {
        if (!flocationIds.containsKey(flocation)) {
            return "0";
        }

        return flocationIds.get(flocation);
    }

    public Faction getFactionAt(FLocation flocation) {
        return Factions.getInstance().getFactionById(getIdAt(flocation));
    }

    public void setIdAt(String id, FLocation flocation) {
        clearOwnershipAt(flocation);

        if (id.equals("0")) {
            removeAt(flocation);
        }

        flocationIds.put(flocation, id);
    }

    public void setFactionAt(Faction faction, FLocation flocation) {
        setIdAt(faction.getId(), flocation);
    }

    public void removeAt(FLocation flocation) {
        Faction faction = getFactionAt(flocation);
        faction.getWarps().values().removeIf(lazyLocation -> flocation.isInChunk(lazyLocation.getLocation()));
        for (Entity entity : flocation.getChunk().getEntities()) {
            if (entity instanceof Player) {
                FPlayer fPlayer = FPlayers.getInstance().getByPlayer((Player) entity);
                if (!fPlayer.isAdminBypassing() && fPlayer.isFlying()) {
                    fPlayer.setFlying(false);
                }
                if (fPlayer.isWarmingUp()) {
                    fPlayer.clearWarmup();
                    fPlayer.msg(TL.WARMUPS_CANCELLED);
                }
            }
        }
        clearOwnershipAt(flocation);
        flocationIds.remove(flocation);
    }

    public Set<FLocation> getAllClaims(String factionId) {
        Set<FLocation> locs = new HashSet<>();
        for (Entry<FLocation, String> entry : flocationIds.entrySet()) {
            if (entry.getValue().equals(factionId)) {
                locs.add(entry.getKey());
            }
        }
        return locs;
    }

    public Set<FLocation> getAllClaims(Faction faction) {
        return getAllClaims(faction.getId());
    }

    // not to be confused with claims, ownership referring to further member-specific ownership of a claim
    public void clearOwnershipAt(FLocation flocation) {
        Faction faction = getFactionAt(flocation);
        if (faction != null && faction.isNormal()) {
            faction.clearClaimOwnership(flocation);
        }
    }

    public void unclaimAll(String factionId) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction != null && faction.isNormal()) {
            faction.clearAllClaimOwnership();
            faction.clearWarps();
        }
        clean(factionId);
    }

    public void unclaimAllInWorld(String factionId, World world) {
        for (FLocation loc : getAllClaims(factionId)) {
            if (loc.getWorldName().equals(world.getName())) {
                removeAt(loc);
            }
        }
    }

    public void clean(String factionId) {
        if (LWC.getEnabled() && FactionsPlugin.getInstance().conf().lwc().isResetLocksOnUnclaim()) {
            for (Entry<FLocation, String> entry : flocationIds.entrySet()) {
                if (entry.getValue().equals(factionId)) {
                    LWC.clearAllLocks(entry.getKey());
                }
            }
        }

        flocationIds.removeFaction(factionId);
    }

    // Is this coord NOT completely surrounded by coords claimed by the same faction?
    // Simpler: Is there any nearby coord with a faction other than the faction here?
    public boolean isBorderLocation(FLocation flocation) {
        Faction faction = getFactionAt(flocation);
        FLocation a = flocation.getRelative(1, 0);
        FLocation b = flocation.getRelative(-1, 0);
        FLocation c = flocation.getRelative(0, 1);
        FLocation d = flocation.getRelative(0, -1);
        return faction != getFactionAt(a) || faction != getFactionAt(b) || faction != getFactionAt(c) || faction != getFactionAt(d);
    }

    // Is this coord connected to any coord claimed by the specified faction?
    public boolean isConnectedLocation(FLocation flocation, Faction faction) {
        FLocation a = flocation.getRelative(1, 0);
        FLocation b = flocation.getRelative(-1, 0);
        FLocation c = flocation.getRelative(0, 1);
        FLocation d = flocation.getRelative(0, -1);
        return faction == getFactionAt(a) || faction == getFactionAt(b) || faction == getFactionAt(c) || faction == getFactionAt(d);
    }

    /**
     * Checks if there is another faction within a given radius other than Wilderness. Used for HCF feature that
     * requires a 'buffer' between factions.
     *
     * @param flocation - center location.
     * @param faction   - faction checking for.
     * @param radius    - chunk radius to check.
     * @return true if another Faction is within the radius, otherwise false.
     */
    public boolean hasFactionWithin(FLocation flocation, Faction faction, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }

                FLocation relative = flocation.getRelative(x, z);
                Faction other = getFactionAt(relative);

                if (other.isNormal() && other != faction) {
                    return true;
                }
            }
        }
        return false;
    }


    //----------------------------------------------//
    // Cleaner. Remove orphaned foreign keys
    //----------------------------------------------//

    public void clean() {
        Iterator<Entry<FLocation, String>> iter = flocationIds.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<FLocation, String> entry = iter.next();
            if (!Factions.getInstance().isValidFactionId(entry.getValue())) {
                if (LWC.getEnabled() && FactionsPlugin.getInstance().conf().lwc().isResetLocksOnUnclaim()) {
                    LWC.clearAllLocks(entry.getKey());
                }
                FactionsPlugin.getInstance().log("Board cleaner removed " + entry.getValue() + " from " + entry.getKey());
                iter.remove();
            }
        }
    }

    //----------------------------------------------//
    // Coord count
    //----------------------------------------------//

    public int getFactionCoordCount(String factionId) {
        return flocationIds.getOwnedLandCount(factionId);
    }

    public int getFactionCoordCount(Faction faction) {
        return getFactionCoordCount(faction.getId());
    }

    public int getFactionCoordCountInWorld(Faction faction, String worldName) {
        String factionId = faction.getId();
        int ret = 0;
        for (Entry<FLocation, String> entry : flocationIds.entrySet()) {
            if (entry.getValue().equals(factionId) && entry.getKey().getWorldName().equals(worldName)) {
                ret += 1;
            }
        }
        return ret;
    }

    //----------------------------------------------//
    // Map generation
    //----------------------------------------------//

    /**
     * The map is relative to a coord and a faction north is in the direction of decreasing x east is in the direction
     * of decreasing z
     */
    public List<Component> getMap(FPlayer fplayer, FLocation flocation, double inDegrees) {
        Faction faction = fplayer.getFaction();
        ArrayList<Component> ret = new ArrayList<>();
        Faction factionLoc = getFactionAt(flocation);
        ret.add(TextUtil.titleizeC("(" + flocation.getCoordString() + ") " + factionLoc.getTag(fplayer)));

        // Get the compass
        List<Component> asciiCompass = AsciiCompass.getAsciiCompass(inDegrees, "<red>", "<gold>");

        int halfWidth = FactionsPlugin.getInstance().conf().map().getWidth() / 2;
        // Use player's value for height
        int halfHeight = fplayer.getMapHeight() / 2;
        FLocation topLeft = flocation.getRelative(-halfWidth, -halfHeight);
        int width = halfWidth * 2 + 1;
        int height = halfHeight * 2 + 1;

        if (FactionsPlugin.getInstance().conf().map().isShowFactionKey()) {
            height--;
        }

        Map<String, String> fList = new HashMap<>();
        int chrIdx = 0;

        // For each row
        for (int dz = 0; dz < height; dz++) {
            // Draw and add that row
            TextComponent.Builder builder = Component.text();

            if (dz < 3) {
                builder.append(asciiCompass.get(dz));
            }
            for (int dx = (dz < 3 ? 6 : 3); dx < width; dx++) {
                if (dx == halfWidth && dz == halfHeight) {
                    builder.append(Component.text().content("+").color(FactionsPlugin.getInstance().conf().map().getSelfColor()).hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(FactionsPlugin.getInstance().txt().parse(TL.CLAIM_YOUAREHERE.toString())))));
                } else {
                    FLocation flocationHere = topLeft.getRelative(dx, dz);
                    Faction factionHere = getFactionAt(flocationHere);
                    Relation relation = fplayer.getRelationTo(factionHere);
                    if (factionHere.isWilderness()) {
                        builder.append(Component.text().content("-").color(FactionsPlugin.getInstance().conf().colors().factions().getWilderness()));
                    } else if (factionHere.isSafeZone()) {
                        builder.append(Component.text().content("+").color(FactionsPlugin.getInstance().conf().colors().factions().getSafezone()));
                    } else if (factionHere.isWarZone()) {
                        builder.append(Component.text().content("+").color(FactionsPlugin.getInstance().conf().colors().factions().getWarzone()));
                    } else if (factionHere == faction || factionHere == factionLoc || relation.isAtLeast(Relation.ALLY) ||
                            (FactionsPlugin.getInstance().conf().map().isShowNeutralFactionsOnMap() && relation.equals(Relation.NEUTRAL)) ||
                            (FactionsPlugin.getInstance().conf().map().isShowEnemyFactions() && relation.equals(Relation.ENEMY)) ||
                            FactionsPlugin.getInstance().conf().map().isShowTruceFactions() && relation.equals(Relation.TRUCE)) {
                        if (!fList.containsKey(factionHere.getTag())) {
                            fList.put(factionHere.getTag(), String.valueOf(this.mapKeyChrs[Math.min(chrIdx++, this.mapKeyChrs.length - 1)]));
                        }
                        String tag = fList.get(factionHere.getTag());
                        builder.append(Component.text().content(tag).color(factionHere.getTextColorTo(faction)));
                    } else {
                        builder.append(Component.text().content("-").color(NamedTextColor.GRAY));
                    }
                }
            }
            ret.add(builder.build());
        }

        // Add the faction key
        if (FactionsPlugin.getInstance().conf().map().isShowFactionKey()) {
            TextComponent.Builder builder = Component.text();
            for (String key : fList.keySet()) {
                final Relation relation = fplayer.getRelationTo(Factions.getInstance().getByTag(key));
                builder.append(Component.text().content(String.format("%s: %s ", fList.get(key), key)).color(relation.getTextColor()));
            }
            ret.add(builder.build());
        }

        return ret;
    }

    enum Dir {
        N, E, S, W
    }

    private Cache<FPlayer, List<Component>> mapCache = CacheBuilder.newBuilder()
            .expireAfterWrite(500, TimeUnit.MILLISECONDS).build();

    public List<Component> getScoreboardMap(FPlayer fplayer) {
        try {
            return new ArrayList<>(mapCache.get(fplayer, () -> this.makeScoreboardMap(fplayer)));
        } catch (ExecutionException e) {
            return new ArrayList<>();
        }
    }

    private List<Component> makeScoreboardMap(FPlayer fplayer) {
        FLocation flocation = fplayer.getLastStoodAt();
        Faction faction = fplayer.getFaction();
        ArrayList<Component> ret = new ArrayList<>();
        Faction factionLoc = getFactionAt(flocation);

        int halfWidth = FactionsPlugin.getInstance().conf().map().getScoreboardWidth() / 2;
        int halfHeight = FactionsPlugin.getInstance().conf().map().getScoreboardHeight() / 2;
        int width = halfWidth * 2 + 1;
        int height = halfHeight * 2 + 1;
        double degrees = (fplayer.getPlayer().getLocation().getYaw() - 180) % 360;
        if (degrees < 0) {
            degrees += 360;
        }
        Dir dir;
        if (degrees < 45 || degrees >= 315) {
            dir = Dir.N;
        } else if (degrees < 135) {
            dir = Dir.E;
        } else if (degrees < 225) {
            dir = Dir.S;
        } else {
            dir = Dir.W;
        }

        FLocation topLeft;
        switch (dir) {
            case N:
                topLeft = flocation.getRelative(-halfWidth, -halfHeight);
                break;
            case S:
                topLeft = flocation.getRelative(halfWidth, halfHeight);
                break;
            case E:
                topLeft = flocation.getRelative(halfHeight, halfWidth);
                break;
            default:
                topLeft = flocation.getRelative(-halfHeight, -halfWidth);
        }

        // For each row
        for (int r = 0; r < height; r++) {
            // Draw and add that row
            TextComponent.Builder builder = Component.text();

            for (int c = 0; c < width; c++) {
                if (c == halfWidth && r == halfHeight) {
                    builder.append(Component.text().content("\u2B1B").color(FactionsPlugin.getInstance().conf().map().getSelfColor()));
                } else {
                    FLocation flocationHere;
                    switch (dir) {
                        case N:
                            flocationHere = topLeft.getRelative(c, r);
                            break;
                        case S:
                            flocationHere = topLeft.getRelative(-c, -r);
                            break;
                        case E:
                            flocationHere = topLeft.getRelative(-r, -(width - c - 1));
                            break;
                        default:
                            flocationHere = topLeft.getRelative(r, width - c - 1);
                    }
                    Faction factionHere = getFactionAt(flocationHere);
                    Relation relation = fplayer.getRelationTo(factionHere);
                    if (factionHere.isWilderness()) {
                        builder.append(Component.text().content("\u2B1B").color(FactionsPlugin.getInstance().conf().colors().factions().getWilderness()));
                    } else if (factionHere.isSafeZone()) {
                        builder.append(Component.text().content("\u2B1B").color(FactionsPlugin.getInstance().conf().colors().factions().getSafezone()));
                    } else if (factionHere.isWarZone()) {
                        builder.append(Component.text().content("\u2B1B").color(FactionsPlugin.getInstance().conf().colors().factions().getWarzone()));
                    } else if (factionHere == faction || factionHere == factionLoc || relation.isAtLeast(Relation.ALLY) ||
                            (FactionsPlugin.getInstance().conf().map().isShowNeutralFactionsOnMap() && relation.equals(Relation.NEUTRAL)) ||
                            (FactionsPlugin.getInstance().conf().map().isShowEnemyFactions() && relation.equals(Relation.ENEMY)) ||
                            FactionsPlugin.getInstance().conf().map().isShowTruceFactions() && relation.equals(Relation.TRUCE)) {
                        builder.append(Component.text().content("\u2B1B").color(factionHere.getTextColorTo(faction)));
                    } else {
                        builder.append(Component.text().content("\u2B1B").color(NamedTextColor.GRAY));
                    }
                }
            }
            ret.add(builder.build());
        }

        return ret;
    }

    public abstract void convertFrom(MemoryBoard old);
}
