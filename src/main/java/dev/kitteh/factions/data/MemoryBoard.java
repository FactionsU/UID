package dev.kitteh.factions.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.integration.LWC;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.util.AsciiCompass;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.TextUtil;
import dev.kitteh.factions.util.WorldTracker;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongList;
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
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@NullMarked
public abstract class MemoryBoard implements Board {

    private final char[] mapKeyChrs = "\\/#$%=&^ABCDEFGHJKLMNOPQRSTUVWXYZ1234567890abcdeghjmnopqrsuvwxyz?".toCharArray();

    protected Object2ObjectMap<String, WorldTracker> worldTrackers = new Object2ObjectOpenHashMap<>();

    protected WorldTracker getAndCreate(String world) {
        return this.worldTrackers.computeIfAbsent(world, k -> new WorldTracker(world));
    }

    //----------------------------------------------//
    // Get and Set
    //----------------------------------------------//
    public int getIdAt(FLocation flocation) {
        WorldTracker tracker = worldTrackers.get(flocation.getWorldName());
        if (tracker != null) {
            int result = tracker.getIdAt(flocation);
            return result == WorldTracker.NO_MATCH ? 0 : result;
        }
        return 0;
    }

    public Faction getFactionAt(FLocation flocation) {
        return Factions.getInstance().getFactionById(getIdAt(flocation));
    }

    public void setIdAt(int id, FLocation flocation) {
        removeAt(flocation);

        this.getAndCreate(flocation.getWorldName()).addClaim(id, flocation);
    }

    public void setFactionAt(Faction faction, FLocation flocation) {
        setIdAt(faction.getId(), flocation);
    }

    public void removeAt(FLocation flocation) {
        Objects.requireNonNull(flocation);
        Faction faction = getFactionAt(flocation);
        faction.getWarps().values().removeIf(flocation::isInChunk);
        if (flocation.getWorld().isChunkLoaded(flocation.x(), flocation.z())) {
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
        }
        clearOwnershipAt(flocation);

        WorldTracker tracker = worldTrackers.get(flocation.getWorldName());
        if (tracker != null) {
            tracker.removeClaim(flocation);
        }
    }

    public Set<FLocation> getAllClaims(Faction faction) {
        return worldTrackers.values().stream().flatMap(tracker -> tracker.getAllClaims(faction.getId()).stream()).collect(Collectors.toSet());
    }

    public Int2ObjectMap<LongList> getAllClaimsForDynmap(World world) {
        WorldTracker tracker = worldTrackers.get(world.getName());
        return tracker == null ? new Int2ObjectOpenHashMap<>() : tracker.getAllClaimsForDynmap();
    }

    // not to be confused with claims, ownership referring to further member-specific ownership of a claim
    private void clearOwnershipAt(FLocation flocation) {
        Faction faction = getFactionAt(flocation);
        if (faction.isNormal()) {
            faction.clearClaimOwnership(flocation);
        }
    }

    public void unclaimAll(Faction faction) {
        if (faction.isNormal()) {
            faction.clearAllClaimOwnership();
            faction.clearWarps();
        }
        clean(faction);
    }

    public void unclaimAllInWorld(Faction faction, World world) {
        WorldTracker tracker = worldTrackers.get(world.getName());
        if (tracker != null) {
            tracker.getAllClaims(faction.getId()).forEach(this::removeAt);
        }
    }

    public void clean(Faction faction) {
        List<FLocation> locations = this.worldTrackers.values().stream().flatMap(wt -> wt.getAllClaims(faction.getId()).stream()).toList();
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
        this.worldTrackers.values().forEach(wt -> wt.removeAllClaims(faction.getId()));
    }

    //----------------------------------------------//
    // Cleaner. Remove orphaned foreign keys
    //----------------------------------------------//

    public void clean() {
        boolean lwc = LWC.getEnabled() && FactionsPlugin.getInstance().conf().lwc().isResetLocksOnUnclaim();
        for (WorldTracker tracker : worldTrackers.values()) {
            for (int factionId : tracker.getIDs()) {
                if (Factions.getInstance().getFactionById(factionId) == null) {
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

    public int getFactionCoordCount(int factionId) {
        return this.worldTrackers.values().stream().mapToInt(wt -> wt.countClaims(factionId)).sum();
    }

    public int getFactionCoordCount(Faction faction) {
        return getFactionCoordCount(faction.getId());
    }

    public int getFactionCoordCountInWorld(Faction faction, String worldName) {
        WorldTracker tracker = worldTrackers.get(worldName);
        return tracker == null ? 0 : tracker.countClaims(faction.getId());
    }

    public int getTotalCount() {
        return this.worldTrackers.values().stream().mapToInt(WorldTracker::countClaims).sum();
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

    private final Cache<FPlayer, List<Component>> mapCache = CacheBuilder.newBuilder()
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

    public abstract void forceSave(boolean sync);

    public abstract int load();
}
