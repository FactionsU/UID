package dev.kitteh.factions.util;

import dev.kitteh.factions.FLocation;
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
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public class WorldTracker {
    public static final int NO_MATCH = Integer.MIN_VALUE;
    private final String worldName;
    private final Long2IntMap chunkToID = new Long2IntOpenHashMap();
    private final Int2ObjectMap<LongSet> IDToChunk = new Int2ObjectOpenHashMap<>();

    public WorldTracker(String worldName) {
        chunkToID.defaultReturnValue(NO_MATCH);
        this.worldName = worldName;
    }

    private LongSet getOrCreateClaims(int id) {
        return IDToChunk.computeIfAbsent(id, k -> new LongOpenHashSet());
    }

    public void addClaim(int id, FLocation location) {
        long mort = Morton.get(location);
        removeClaim(location);
        if (id != 0) {
            chunkToID.put(mort, id);
            getOrCreateClaims(id).add(mort);
        }
    }

    public void addClaimOnLoad(int id, int x, int z) {
        long mort = Morton.get(x, z);
        chunkToID.put(mort, id);
        getOrCreateClaims(id).add(mort);
    }

    public void removeClaim(FLocation location) {
        long mort = Morton.get(location);
        int formerOwner = chunkToID.remove(mort);
        if (formerOwner != NO_MATCH) {
            LongSet set = IDToChunk.get(formerOwner);
            if (set != null) {
                set.remove(mort);
            }
        }
    }

    public void removeAllClaims(int id) {
        LongSet claims = IDToChunk.get(id);
        if (claims != null) {
            claims.forEach(chunkToID::remove);
        }
    }

    public List<FLocation> getAllClaims(int id) {
        LongSet longs = this.IDToChunk.get(id);
        if (longs == null) {
            return List.of();
        }
        return longs.longStream().mapToObj(mort -> new FLocation(this.worldName, Morton.getX(mort), Morton.getZ(mort))).toList();
    }

    public Long2IntMap getChunkToIDForSave() {
        return chunkToID;
    }

    public Int2ObjectMap<LongList> getAllClaimsForDynmap() {
        Int2ObjectMap<LongList> newMap = new Int2ObjectOpenHashMap<>();
        this.IDToChunk.int2ObjectEntrySet().forEach(entry -> newMap.put(entry.getIntKey(), new LongArrayList(entry.getValue())));
        return newMap;
    }

    public int getIdAt(FLocation location) {
        return chunkToID.get(Morton.get(location));
    }

    public int countClaims(int id) {
        return this.IDToChunk.getOrDefault(id, LongSet.of()).size();
    }

    public int countClaims() {
        return this.chunkToID.size();
    }

    public IntList getIDs() {
        return new IntArrayList(this.IDToChunk.keySet());
    }
}