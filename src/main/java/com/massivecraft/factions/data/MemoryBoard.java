package com.massivecraft.factions.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class MemoryBoard extends Board {

    protected static class WorldTracker {
        public static final int NO_FACTION = Integer.MIN_VALUE;
        private final String worldName;
        private final Long2IntMap chunkToFaction = new Long2IntOpenHashMap();
        private final Int2ObjectMap<LongSet> factionToChunk = new Int2ObjectOpenHashMap<>();

        private WorldTracker(String worldName) {
            chunkToFaction.defaultReturnValue(NO_FACTION);
            this.worldName = worldName;
        }

        private LongSet getOrCreate(int faction) {
            return factionToChunk.computeIfAbsent(faction, k -> new LongOpenHashSet());
        }

        public void addClaim(int faction, FLocation location) {
            long mort = Morton.get(location);
            removeClaim(location);
            if (faction != 0) {
                chunkToFaction.put(mort, faction);
                getOrCreate(faction).add(mort);
            }
        }

        public void addClaimOnLoad(int faction, int x, int z) {
            long mort = Morton.get(x, z);
            chunkToFaction.put(mort, faction);
            getOrCreate(faction).add(mort);
        }

        public void removeClaim(FLocation location) {
            long mort = Morton.get(location);
            int formerOwner = chunkToFaction.remove(mort);
            if (formerOwner != NO_FACTION) {
                LongSet set = factionToChunk.get(formerOwner);
                if (set != null) {
                    set.remove(mort);
                }
            }
        }

        public void removeAllClaims(int faction) {
            LongSet claims = factionToChunk.remove(faction);
            if (claims != null) {
                claims.forEach(chunkToFaction::remove);
            }
        }

        public List<FLocation> getAllClaims(int faction) {
            LongSet longs = this.factionToChunk.get(faction);
            if (longs == null) {
                return List.of();
            }
            return longs.longStream().mapToObj(mort -> new FLocation(this.worldName, Morton.getX(mort), Morton.getZ(mort))).toList();
        }

        public Long2IntMap getChunkToFactionForSave() {
            return chunkToFaction;
        }

        public Int2ObjectMap<LongList> getAllClaimsForDynmap() {
            Int2ObjectMap<LongList> newMap = new Int2ObjectOpenHashMap<>();
            this.factionToChunk.int2ObjectEntrySet().forEach(entry -> newMap.put(entry.getIntKey(), new LongArrayList(entry.getValue())));
            return newMap;
        }

        public int getFactionIdAt(FLocation location) {
            return chunkToFaction.get(Morton.get(location));
        }

        public int countFactionClaims(int faction) {
            return this.factionToChunk.getOrDefault(faction, LongSet.of()).size();
        }

        public int countFactionClaims() {
            return this.chunkToFaction.size();
        }

        public IntList getFactionIds() {
            return new IntArrayList(this.factionToChunk.keySet());
        }
    }

    /**
     * Simple two-ints-in-a-long Morton code.
     */
    public static final class Morton {
        public static long get(FLocation location) {
            return Morton.get((int) location.getX(), (int) location.getZ());
        }

        /**
         * Gets a Morton code for the given coordinates.
         *
         * @param x x coordinate
         * @param z z coordinate
         * @return Morton code for the coordinates
         */
        public static long get(int x, int z) {
            return (Morton.spreadOut(z) << 1) + Morton.spreadOut(x);
        }

        /**
         * Gets the X value from a given Morton code.
         *
         * @param mortonCode Morton code
         * @return x coordinate
         */
        public static int getX(long mortonCode) {
            return Morton.comeTogether(mortonCode);
        }

        /**
         * Gets the Z value from a given Morton code.
         *
         * @param mortonCode Morton code
         * @return z coordinate
         */
        public static int getZ(long mortonCode) {
            return Morton.comeTogether(mortonCode >> 1);
        }

        private static long spreadOut(long l) {
            l &= 0x00000000FFFFFFFFL;
            l = (l | (l << 16)) & 0x0000FFFF0000FFFFL;
            l = (l | (l << 8)) & 0x00FF00FF00FF00FFL;
            l = (l | (l << 4)) & 0x0F0F0F0F0F0F0F0FL;
            l = (l | (l << 2)) & 0x3333333333333333L;
            l = (l | (l << 1)) & 0x5555555555555555L;
            return l;
        }

        private static int comeTogether(long l) {
            l = l & 0x5555555555555555L;
            l = (l | (l >> 1)) & 0x3333333333333333L;
            l = (l | (l >> 2)) & 0x0F0F0F0F0F0F0F0FL;
            l = (l | (l >> 4)) & 0x00FF00FF00FF00FFL;
            l = (l | (l >> 8)) & 0x0000FFFF0000FFFFL;
            l = (l | (l >> 16)) & 0x00000000FFFFFFFFL;
            return (int) l;
        }
    }

    private final char[] mapKeyChrs = "\\/#$%=&^ABCDEFGHJKLMNOPQRSTUVWXYZ1234567890abcdeghjmnopqrsuvwxyz?".toCharArray();

    protected Object2ObjectMap<String, WorldTracker> worldTrackers = new Object2ObjectOpenHashMap<>();

    protected WorldTracker getAndCreate(String world) {
        return this.worldTrackers.computeIfAbsent(world, k -> new WorldTracker(world));
    }

    //----------------------------------------------//
    // Get and Set
    //----------------------------------------------//
    public int getIntIdAt(FLocation flocation) {
        WorldTracker tracker = worldTrackers.get(flocation.getWorldName());
        if (tracker != null) {
            int result = tracker.getFactionIdAt(flocation);
            return result == WorldTracker.NO_FACTION ? 0 : result;
        }
        return 0;
    }

    public String getIdAt(FLocation flocation) {
        return String.valueOf(this.getIntIdAt(flocation));
    }

    public Faction getFactionAt(FLocation flocation) {
        return Factions.getInstance().getFactionById(getIntIdAt(flocation));
    }

    public void setIdAt(String id, FLocation flocation) {
        this.setIdAt(Integer.parseInt(id), flocation);
    }

    public void setIdAt(int id, FLocation flocation) {
        removeAt(flocation);

        this.getAndCreate(flocation.getWorldName()).addClaim(id, flocation);
    }

    public void setFactionAt(Faction faction, FLocation flocation) {
        setIdAt(faction.getIntId(), flocation);
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

        WorldTracker tracker = worldTrackers.get(flocation.getWorldName());
        if (tracker != null) {
            tracker.removeClaim(flocation);
        }
    }

    public Set<FLocation> getAllClaims(String factionId) {
        return this.getAllClaims(Integer.parseInt(factionId));
    }

    public Set<FLocation> getAllClaims(int factionId) {
        return worldTrackers.values().stream().flatMap(tracker -> tracker.getAllClaims(factionId).stream()).collect(Collectors.toSet());
    }

    public Set<FLocation> getAllClaims(Faction faction) {
        return getAllClaims(faction.getIntId());
    }

    public Int2ObjectMap<LongList> getAllClaimsForDynmap(World world) {
        WorldTracker tracker = worldTrackers.get(world.getName());
        return tracker == null ? new Int2ObjectOpenHashMap<>() : tracker.getAllClaimsForDynmap();
    }

    // not to be confused with claims, ownership referring to further member-specific ownership of a claim
    public void clearOwnershipAt(FLocation flocation) {
        Faction faction = getFactionAt(flocation);
        if (faction != null && faction.isNormal()) {
            faction.clearClaimOwnership(flocation);
        }
    }

    public void unclaimAll(Faction faction) {
        this.unclaimAll(faction.getIntId());
    }

    public void unclaimAll(String factionId) {
        this.unclaimAll(Integer.parseInt(factionId));
    }

    public void unclaimAll(int factionId) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction != null && faction.isNormal()) {
            faction.clearAllClaimOwnership();
            faction.clearWarps();
        }
        clean(factionId);
    }

    public void unclaimAllInWorld(Faction faction, World world) {
        this.unclaimAllInWorld(faction.getIntId(), world);
    }

    public void unclaimAllInWorld(String factionId, World world) {
        this.unclaimAllInWorld(Integer.parseInt(factionId), world);
    }

    public void unclaimAllInWorld(int factionId, World world) {
        WorldTracker tracker = worldTrackers.get(world.getName());
        if (tracker != null) {
            tracker.getAllClaims(factionId).forEach(this::removeAt);
        }
    }

    public void clean(int factionId) {
        List<FLocation> locations = this.worldTrackers.values().stream().flatMap(wt -> wt.getAllClaims(factionId).stream()).toList();
        if (LWC.getEnabled() && FactionsPlugin.getInstance().conf().lwc().isResetLocksOnUnclaim()) {
            locations.forEach(LWC::clearAllLocks);
        }
        for (FPlayer fPlayer : FPlayers.getInstance().getOnlinePlayers()) {
            if (locations.contains(fPlayer.getLastStoodAt())) {
                if (FactionsPlugin.getInstance().conf().commands().fly().isEnable() && !fPlayer.isAdminBypassing() && fPlayer.isFlying()) {
                    fPlayer.setFlying(false);
                }
                if (fPlayer.isWarmingUp()) {
                    fPlayer.clearWarmup();
                    fPlayer.msg(TL.WARMUPS_CANCELLED);
                }
            }
        }
        this.worldTrackers.values().forEach(wt -> wt.removeAllClaims(factionId));
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
        boolean lwc = LWC.getEnabled() && FactionsPlugin.getInstance().conf().lwc().isResetLocksOnUnclaim();
        for (WorldTracker tracker : worldTrackers.values()) {
            for (int factionId : tracker.getFactionIds()) {
                if (!Factions.getInstance().isValidFactionId(factionId)) {
                    this.worldTrackers.values().stream().flatMap(wt -> wt.getAllClaims(factionId).stream()).forEach(loc -> {
                        if (lwc) {
                            LWC.clearAllLocks(loc);
                        }
                        FactionsPlugin.getInstance().log("Board cleaner removed id " + factionId + " from " + loc);
                    });
                    this.worldTrackers.values().forEach(wt -> wt.removeAllClaims(factionId));
                }
            }
        }
    }

    //----------------------------------------------//
    // Coord count
    //----------------------------------------------//

    public int getFactionCoordCount(String factionId) {
        return this.getFactionCoordCount(Integer.parseInt(factionId));
    }

    public int getFactionCoordCount(int factionId) {
        return this.worldTrackers.values().stream().mapToInt(wt -> wt.countFactionClaims(factionId)).sum();
    }

    public int getFactionCoordCount(Faction faction) {
        return getFactionCoordCount(faction.getIntId());
    }

    public int getFactionCoordCountInWorld(Faction faction, String worldName) {
        WorldTracker tracker = worldTrackers.get(worldName);
        return tracker == null ? 0 : tracker.countFactionClaims(faction.getIntId());
    }

    public int getTotalCount() {
        return this.worldTrackers.values().stream().mapToInt(WorldTracker::countFactionClaims).sum();
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

        FLocation topLeft = switch (dir) {
            case N -> flocation.getRelative(-halfWidth, -halfHeight);
            case S -> flocation.getRelative(halfWidth, halfHeight);
            case E -> flocation.getRelative(halfHeight, halfWidth);
            default -> flocation.getRelative(-halfHeight, -halfWidth);
        };

        // For each row
        for (int r = 0; r < height; r++) {
            // Draw and add that row
            TextComponent.Builder builder = Component.text();

            for (int c = 0; c < width; c++) {
                if (c == halfWidth && r == halfHeight) {
                    builder.append(Component.text().content("\u2B1B").color(FactionsPlugin.getInstance().conf().map().getSelfColor()));
                } else {
                    FLocation flocationHere = switch (dir) {
                        case N -> topLeft.getRelative(c, r);
                        case S -> topLeft.getRelative(-c, -r);
                        case E -> topLeft.getRelative(-r, -(width - c - 1));
                        default -> topLeft.getRelative(r, width - c - 1);
                    };
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
