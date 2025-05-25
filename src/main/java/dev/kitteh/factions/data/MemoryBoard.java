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
    public Faction factionAt(FLocation flocation) {
        return Factions.factions().get(getIdAt(flocation)) instanceof Faction faction ? faction : Factions.factions().wilderness();
    }

    private void setIdAt(int id, FLocation flocation) {
        unclaim(flocation);

        this.getAndCreate(flocation.worldName()).addClaim(id, flocation);
    }

    @Override
    public void claim(FLocation flocation, Faction faction) {
        if (faction.isWilderness()) {
            this.unclaim(flocation);
        } else {
            this.setIdAt(faction.id(), flocation);
        }
    }

    @Override
    public void unclaim(FLocation flocation) {
        Objects.requireNonNull(flocation);
        Faction faction = factionAt(flocation);
        faction.warps().values().removeIf(flocation::contains);
        if (flocation.world().isChunkLoaded(flocation.x(), flocation.z())) {
            for (Entity entity : flocation.asChunk().getEntities()) {
                if (entity instanceof Player) {
                    FPlayer fPlayer = FPlayers.fPlayers().get((Player) entity);
                    if (!fPlayer.adminBypass() && fPlayer.flying()) {
                        fPlayer.flying(false);
                    }
                    if (fPlayer.warmingUp()) {
                        fPlayer.cancelWarmup();
                        fPlayer.msg(TL.WARMUPS_CANCELLED);
                    }
                }
            }
        }

        // Clear zone
        if (faction.isNormal()) {
            faction.zones().set(faction.zones().main(), flocation);
        }

        WorldTracker tracker = worldTrackers.get(flocation.worldName());
        //noinspection ConstantValue
        if (tracker != null) {
            tracker.removeClaim(flocation);
        }
    }

    @Override
    public Set<FLocation> allClaims(Faction faction) {
        return worldTrackers.values().stream().flatMap(tracker -> tracker.allClaims(faction.id()).stream()).collect(Collectors.toSet());
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
                if (FactionsPlugin.instance().conf().commands().fly().isEnable() && !fPlayer.adminBypass() && fPlayer.flying()) {
                    fPlayer.flying(false);
                }
                if (fPlayer.warmingUp()) {
                    fPlayer.cancelWarmup();
                    fPlayer.msg(TL.WARMUPS_CANCELLED);
                }
            }
        }
        this.worldTrackers.values().forEach(wt -> wt.removeAllClaims(faction.id()));
    }

    //----------------------------------------------//
    // Cleaner. Remove orphaned foreign keys
    //----------------------------------------------//

    public void clean() {
        for (WorldTracker tracker : worldTrackers.values()) {
            for (int factionId : tracker.ids()) {
                if (Factions.factions().get(factionId) == null) {
                    this.worldTrackers.values().stream().flatMap(wt -> wt.allClaims(factionId).stream())
                            .forEach(loc -> FactionsPlugin.instance().log("Board cleaner removed id " + factionId + " from " + loc));
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
        Faction faction = fplayer.faction();
        ArrayList<Component> ret = new ArrayList<>();
        Faction factionLoc = factionAt(flocation);
        ret.add(TextUtil.titleizeC("(" + flocation.asCoordString() + ") " + factionLoc.tagString(fplayer)));

        // Get the compass
        List<Component> asciiCompass = AsciiCompass.of(inDegrees, "<red>", "<gold>");

        int halfWidth = FactionsPlugin.instance().conf().map().getWidth() / 2;
        // Use player's value for height
        int halfHeight = fplayer.mapHeight() / 2;
        FLocation topLeft = flocation.relative(-halfWidth, -halfHeight);
        int width = halfWidth * 2 + 1;
        int height = halfHeight * 2 + 1;

        if (FactionsPlugin.instance().conf().map().isShowFactionKey()) {
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
                    builder.append(Component.text().content("+").color(FactionsPlugin.instance().conf().map().getSelfColor()).hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(TextUtil.parse(TL.CLAIM_YOUAREHERE.toString())))));
                } else {
                    FLocation flocationHere = topLeft.relative(dx, dz);
                    Faction factionHere = factionAt(flocationHere);
                    Relation relation = fplayer.relationTo(factionHere);
                    if (factionHere.isWilderness()) {
                        builder.append(Component.text().content("-").color(FactionsPlugin.instance().conf().colors().factions().getWilderness()));
                    } else if (factionHere.isSafeZone()) {
                        builder.append(Component.text().content("+").color(FactionsPlugin.instance().conf().colors().factions().getSafezone()));
                    } else if (factionHere.isWarZone()) {
                        builder.append(Component.text().content("+").color(FactionsPlugin.instance().conf().colors().factions().getWarzone()));
                    } else if (factionHere == faction || factionHere == factionLoc || relation.isAtLeast(Relation.ALLY) ||
                            (FactionsPlugin.instance().conf().map().isShowNeutralFactionsOnMap() && relation.equals(Relation.NEUTRAL)) ||
                            (FactionsPlugin.instance().conf().map().isShowEnemyFactions() && relation.equals(Relation.ENEMY)) ||
                            FactionsPlugin.instance().conf().map().isShowTruceFactions() && relation.equals(Relation.TRUCE)) {
                        if (!fList.containsKey(factionHere.tag())) {
                            fList.put(factionHere.tag(), String.valueOf(this.mapKeyChrs[Math.min(chrIdx++, this.mapKeyChrs.length - 1)]));
                        }
                        String tag = fList.get(factionHere.tag());
                        builder.append(Component.text().content(tag).color(factionHere.textColorTo(faction)));
                    } else {
                        builder.append(Component.text().content("-").color(NamedTextColor.GRAY));
                    }
                }
            }
            ret.add(builder.build());
        }

        // Add the faction key
        if (FactionsPlugin.instance().conf().map().isShowFactionKey()) {
            TextComponent.Builder builder = Component.text();
            for (String key : fList.keySet()) {
                final Relation relation = fplayer.relationTo(Factions.factions().get(key));
                builder.append(Component.text().content(String.format("%s: %s ", fList.get(key), key)).color(relation.color()));
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
        FLocation flocation = fplayer.lastStoodAt();
        Faction faction = fplayer.faction();
        Player player = fplayer.asPlayer();
        if (player == null) {
            return List.of();
        }
        ArrayList<Component> ret = new ArrayList<>();
        Faction factionLoc = factionAt(flocation);

        int halfWidth = FactionsPlugin.instance().conf().map().getScoreboardWidth() / 2;
        int halfHeight = FactionsPlugin.instance().conf().map().getScoreboardHeight() / 2;
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
                    builder.append(Component.text().content("⬛").color(FactionsPlugin.instance().conf().map().getSelfColor()));
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
                        builder.append(Component.text().content("⬛").color(FactionsPlugin.instance().conf().colors().factions().getWilderness()));
                    } else if (factionHere.isSafeZone()) {
                        builder.append(Component.text().content("⬛").color(FactionsPlugin.instance().conf().colors().factions().getSafezone()));
                    } else if (factionHere.isWarZone()) {
                        builder.append(Component.text().content("⬛").color(FactionsPlugin.instance().conf().colors().factions().getWarzone()));
                    } else if (factionHere == faction || factionHere == factionLoc || relation.isAtLeast(Relation.ALLY) ||
                            (FactionsPlugin.instance().conf().map().isShowNeutralFactionsOnMap() && relation.equals(Relation.NEUTRAL)) ||
                            (FactionsPlugin.instance().conf().map().isShowEnemyFactions() && relation.equals(Relation.ENEMY)) ||
                            FactionsPlugin.instance().conf().map().isShowTruceFactions() && relation.equals(Relation.TRUCE)) {
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
