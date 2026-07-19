package dev.kitteh.factions.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.AsciiCompass;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.WorldTracker;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
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

@ApiStatus.Internal
@NullMarked
public abstract class MemoryBoard implements Board {
    protected Object2ObjectMap<String, WorldTracker> worldTrackers = new Object2ObjectOpenHashMap<>();

    protected WorldTracker getAndCreate(String world) {
        return this.worldTrackers.computeIfAbsent(world, _ -> new WorldTracker(world));
    }

    private int getIdAt(FLocation flocation) {
        WorldTracker tracker = worldTrackers.get(flocation.worldName());
        //noinspection ConstantValue
        if (tracker != null) {
            int result = tracker.idAt(flocation);
            return result == WorldTracker.NO_MATCH ? 0 : result;
        }
        return 0;
    }

    @Override
    public Faction factionAt(FLocation location) {
        return Factions.factions().get(getIdAt(location)) instanceof Faction faction ? faction : Factions.factions().wilderness();
    }

    private void setIdAt(int id, FLocation flocation) {
        unclaim(flocation);

        this.getAndCreate(flocation.worldName()).addClaim(id, flocation);
    }

    @Override
    public void claim(FLocation location, Faction faction) {
        if (faction.isWilderness()) {
            this.unclaim(location);
        } else {
            this.setIdAt(faction.id(), location);
        }
    }

    @Override
    public void unclaim(FLocation location) {
        Objects.requireNonNull(location);
        Faction faction = factionAt(location);
        faction.warps().values().removeIf(location::contains);
        if (location.world().isChunkLoaded(location.x(), location.z())) {
            for (Entity entity : location.asChunk().getEntities()) {
                if (entity instanceof Player) {
                    FPlayer fPlayer = FPlayers.fPlayers().get((Player) entity);
                    if (!fPlayer.adminBypass() && fPlayer.flying()) {
                        fPlayer.flying(false);
                    }
                    if (fPlayer.warmingUp()) {
                        fPlayer.cancelWarmup();
                        fPlayer.sendRichMessage(Confs.tl().commands().generic().getWarmupCancelled());
                    }
                }
            }
        }

        // Clear zone
        if (faction.isNormal()) {
            faction.zones().set(faction.zones().main(), location);
        }

        WorldTracker tracker = worldTrackers.get(location.worldName());
        //noinspection ConstantValue
        if (tracker != null) {
            tracker.removeClaim(location);
        }
    }

    @Override
    public Set<FLocation> allClaims(Faction faction) {
        return worldTrackers.values().stream().flatMap(tracker -> tracker.allClaims(faction.id()).stream()).collect(Collectors.toSet());
    }

    public LongSet allClaimsAsLongs(Faction faction, World world) {
        WorldTracker tracker = worldTrackers.get(world.getName());
        //noinspection ConstantValue
        return tracker == null ? new LongArraySet() : tracker.allClaimsAsLong(faction.id());
    }

    public Int2ObjectMap<LongList> getAllClaimsForDynmap(World world) {
        WorldTracker tracker = worldTrackers.get(world.getName());
        //noinspection ConstantValue
        return tracker == null ? new Int2ObjectOpenHashMap<>() : tracker.allClaimsForDynmap();
    }

    @Override
    public void unclaimAll(Faction faction) {
        if (faction.isNormal()) {
            faction.clearWarps();
        }
        clean(faction);
    }

    @Override
    public void unclaimAllInWorld(Faction faction, World world) {
        WorldTracker tracker = worldTrackers.get(world.getName());
        //noinspection ConstantValue
        if (tracker != null) {
            tracker.allClaims(faction.id()).forEach(this::unclaim);
        }
    }

    public void clean(Faction faction) {
        List<FLocation> locations = this.worldTrackers.values().stream().flatMap(wt -> wt.allClaims(faction.id()).stream()).toList();
        for (FPlayer fPlayer : FPlayers.fPlayers().online()) {
            if (locations.contains(fPlayer.lastStoodAt())) {
                if (Confs.main().commands().fly().isEnable() && !fPlayer.adminBypass() && fPlayer.flying()) {
                    fPlayer.flying(false);
                }
                if (fPlayer.warmingUp()) {
                    fPlayer.cancelWarmup();
                    fPlayer.sendRichMessage(Confs.tl().commands().generic().getWarmupCancelled());
                }
            }
        }
        this.worldTrackers.values().forEach(wt -> wt.removeAllClaims(faction.id()));
    }

    public void clean() {
        for (WorldTracker tracker : worldTrackers.values()) {
            for (int factionId : tracker.ids()) {
                if (Factions.factions().get(factionId) == null) {
                    this.worldTrackers.values().stream().flatMap(wt -> wt.allClaims(factionId).stream())
                            .forEach(loc -> AbstractFactionsPlugin.instance().log("Board cleaner removed id " + factionId + " from " + loc));
                    this.worldTrackers.values().forEach(wt -> wt.removeAllClaims(factionId));
                }
            }
        }
    }

    public int getFactionCoordCount(int factionId) {
        return this.worldTrackers.values().stream().mapToInt(wt -> wt.countClaims(factionId)).sum();
    }

    @Override
    public int claimCount(Faction faction) {
        return getFactionCoordCount(faction.id());
    }

    @Override
    public int claimCount(Faction faction, World world) {
        WorldTracker tracker = worldTrackers.get(world.getName());
        //noinspection ConstantValue
        return tracker == null ? 0 : tracker.countClaims(faction.id());
    }

    @Override
    public long cachedInhabitedTime(FLocation location) {
        if (location.loaded()) {
            return location.asChunk().getInhabitedTime();
        }
        WorldTracker tracker = worldTrackers.get(location.worldName());
        //noinspection ConstantValue
        if (tracker != null) {
            tracker.inhabited(location);
        }
        return -1L;
    }

    public void cachedInhabitedTime(FLocation location, long inhabitedTime) {
        WorldTracker tracker = worldTrackers.get(location.worldName());
        //noinspection ConstantValue
        if (tracker != null) {
            tracker.inhabited(location, inhabitedTime);
        }
    }

    public int getTotalCount() {
        return this.worldTrackers.values().stream().mapToInt(WorldTracker::countClaims).sum();
    }

    /**
     * The map is relative to a coord and a faction north is in the direction of decreasing x east is in the direction
     * of decreasing z
     */
    public List<Component> getMap(FPlayer fPlayer, FLocation fLocation, double inDegrees) {
        Faction playerFaction = fPlayer.faction();
        ArrayList<Component> ret = new ArrayList<>();
        Faction factionLoc = factionAt(fLocation);

        var tl = Confs.tl().commands().map();

        char[] mapChars = tl.getMapOutputFactionCharacters().toCharArray();
        if (mapChars.length == 0) {
            mapChars = "\\/#$%=&^ABCDEFGHJKLMNOPQRSTUVWXYZ1234567890abcdeghjmnopqrsuvwxyz?".toCharArray();
        }

        Component other = Mini.parse(tl.getMapOutputOther());
        Component safezone = Mini.parse(tl.getMapOutputSafezone());
        Component warzone = Mini.parse(tl.getMapOutputWarzone());
        Component wilderness = Mini.parse(tl.getMapOutputWilderness());

        Component title = Mini.parse(tl.getMapOutputTitle(), fPlayer,
                Placeholder.unparsed("x", String.valueOf(fLocation.x())),
                Placeholder.unparsed("z", String.valueOf(fLocation.z())),
                FactionResolver.of(factionLoc)
        );

        ret.add(title);

        // Get the compass
        List<Component> asciiCompass = AsciiCompass.of(inDegrees);

        int halfWidth = Confs.main().map().getWidth() / 2;
        // Use player's value for height
        int halfHeight = fPlayer.mapHeight() / 2;
        FLocation topLeft = fLocation.relative(-halfWidth, -halfHeight);
        int width = halfWidth * 2 + 1;
        int height = halfHeight * 2 + 1;

        if (Confs.main().map().isShowFactionKey()) {
            height--;
        }

        Map<Faction, Component> fList = new HashMap<>();
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
                    builder.append(Component.text().content("+").color(Confs.main().map().getSelfColor()).hoverEvent(HoverEvent.showText(Mini.parse(Confs.tl().claiming().claim().getYouAreHere()))));
                } else {
                    FLocation fLocationHere = topLeft.relative(dx, dz);
                    Faction factionHere = factionAt(fLocationHere);
                    Relation relation = fPlayer.relationTo(factionHere);
                    if (factionHere.isWilderness()) {
                        builder.append(wilderness);
                    } else if (factionHere.isSafeZone()) {
                        builder.append(safezone);
                    } else if (factionHere.isWarZone()) {
                        builder.append(warzone);
                    } else if (factionHere == playerFaction || factionHere == factionLoc || relation.isAtLeast(Relation.ALLY) ||
                            (Confs.main().map().isShowNeutralFactionsOnMap() && relation.equals(Relation.NEUTRAL)) ||
                            (Confs.main().map().isShowEnemyFactions() && relation.equals(Relation.ENEMY)) ||
                            Confs.main().map().isShowTruceFactions() && relation.equals(Relation.TRUCE)) {
                        if (!fList.containsKey(factionHere)) {
                            fList.put(factionHere, Component.text().content(String.valueOf(mapChars[Math.min(chrIdx++, mapChars.length - 1)])).color(factionHere.textColorTo(playerFaction)).build());
                        }
                        builder.append(fList.get(factionHere));
                    } else {
                        builder.append(other);
                    }
                }
            }
            ret.add(builder.build());
        }

        // Add the faction key
        if (Confs.main().map().isShowFactionKey()) {
            TextComponent.Builder builder = Component.text();
            fList.forEach((fac, label) ->
                    builder.append(Mini.parse(tl.getMapOutputKeyItem(), fPlayer,
                            FactionResolver.of(fac),
                            Placeholder.component("label", label)
                    )));
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
        FLocation flocation = fplayer.lastStoodAt();
        Faction faction = fplayer.faction();
        Player player = fplayer.asPlayer();
        if (player == null) {
            return List.of();
        }
        ArrayList<Component> ret = new ArrayList<>();
        Faction factionLoc = factionAt(flocation);

        int halfWidth = Confs.main().map().getScoreboardWidth() / 2;
        int halfHeight = Confs.main().map().getScoreboardHeight() / 2;
        int width = halfWidth * 2 + 1;
        int height = halfHeight * 2 + 1;
        double degrees = (player.getLocation().getYaw() - 180) % 360;
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
            case N -> flocation.relative(-halfWidth, -halfHeight);
            case S -> flocation.relative(halfWidth, halfHeight);
            case E -> flocation.relative(halfHeight, halfWidth);
            default -> flocation.relative(-halfHeight, -halfWidth);
        };

        // For each row
        for (int r = 0; r < height; r++) {
            // Draw and add that row
            TextComponent.Builder builder = Component.text();

            for (int c = 0; c < width; c++) {
                if (c == halfWidth && r == halfHeight) {
                    builder.append(Component.text().content("⬛").color(Confs.main().map().getSelfColor()));
                } else {
                    FLocation flocationHere = switch (dir) {
                        case N -> topLeft.relative(c, r);
                        case S -> topLeft.relative(-c, -r);
                        case E -> topLeft.relative(-r, -(width - c - 1));
                        default -> topLeft.relative(r, width - c - 1);
                    };
                    Faction factionHere = factionAt(flocationHere);
                    Relation relation = fplayer.relationTo(factionHere);
                    if (factionHere.isWilderness()) {
                        builder.append(Component.text().content("⬛").color(NamedTextColor.GRAY));
                    } else if (factionHere.isSafeZone()) {
                        builder.append(Component.text().content("⬛").color(Confs.tl().colors().factions().getSafezone()));
                    } else if (factionHere.isWarZone()) {
                        builder.append(Component.text().content("⬛").color(Confs.tl().colors().factions().getWarzone()));
                    } else if (factionHere == faction || factionHere == factionLoc || relation.isAtLeast(Relation.ALLY) ||
                            (Confs.main().map().isShowNeutralFactionsOnMap() && relation.equals(Relation.NEUTRAL)) ||
                            (Confs.main().map().isShowEnemyFactions() && relation.equals(Relation.ENEMY)) ||
                            Confs.main().map().isShowTruceFactions() && relation.equals(Relation.TRUCE)) {
                        builder.append(Component.text().content("⬛").color(factionHere.textColorTo(faction)));
                    } else {
                        builder.append(Component.text().content("⬛").color(NamedTextColor.GRAY));
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
