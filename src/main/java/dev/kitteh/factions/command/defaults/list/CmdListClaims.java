package dev.kitteh.factions.command.defaults.list;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.StringParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;

public class CmdListClaims implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("claims")
                            .commandDescription(Cloudy.desc(TL.COMMAND_LISTCLAIMS_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.LISTCLAIMS).and(Cloudy.hasSelfFactionPerms(PermissibleActions.LISTCLAIMS))))
                            .flag(manager.flagBuilder("world").withComponent(StringParser.stringParser()))
                            .flag(
                                    manager.flagBuilder("faction")
                                            .withComponent(FactionParser.of(FactionParser.Include.SELF))
                                            .withPermission(Cloudy.hasPermission(Permission.LISTCLAIMS_OTHER))
                            )
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        Faction faction = sender.faction();
        if (context.flags().hasFlag("faction") && context.sender().hasPermission(Permission.LISTCLAIMS_OTHER)) {
            faction = context.flags().get("faction");
        }

        World world = ((Sender.Player) context.sender()).player().getWorld();
        if (context.flags().hasFlag("world")) {
            String worldName = context.flags().get("world");
            world = Bukkit.getWorld(worldName);
            if (world == null) {
                sender.msg(TL.COMMAND_LISTCLAIMS_INVALIDWORLD, worldName);
                return;
            }
        }

        Map<Long, FLoc> worldClaims = new HashMap<>();
        for (FLocation loc : Board.board().allClaims(faction)) {
            if (loc.world().equals(world)) {
                worldClaims.put(getLong(loc.x(), loc.z()), new FLoc(loc.x(), loc.z()));
            }
        }
        if (worldClaims.isEmpty()) {
            sender.msg(TL.COMMAND_LISTCLAIMS_NOCLAIMS, faction.tag(), world.getName());
            return;
        }
        Set<FLoc> set;
        Queue<FLoc> queue;
        FLoc cur;
        List<FLoc> displayList = new ArrayList<>();
        for (FLoc activeLoc : worldClaims.values()) {
            if (activeLoc.processed) {
                continue;
            }
            set = new HashSet<>();
            set.add(activeLoc);
            queue = new LinkedList<>();
            queue.add(activeLoc);
            while (!queue.isEmpty()) {
                cur = queue.poll();
                cur.processed = true;
                addIf(set, queue, worldClaims.get(getLong(cur.x, cur.z + 1)));
                addIf(set, queue, worldClaims.get(getLong(cur.x, cur.z - 1)));
                addIf(set, queue, worldClaims.get(getLong(cur.x + 1, cur.z)));
                addIf(set, queue, worldClaims.get(getLong(cur.x - 1, cur.z)));
            }
            if (set.size() == 1) {
                displayList.add(activeLoc);
            } else {
                long tX = 0;
                long tZ = 0;
                for (FLoc loc : set) {
                    tX += loc.x;
                    tZ += loc.z;
                }
                cur = new FLoc((int) (((double) tX) / set.size() + 0.5), (int) (((double) tZ) / set.size() + 0.5));
                cur.total = set.size();
                displayList.add(cur);
            }
        }

        sender.msg(TL.COMMAND_LISTCLAIMS_MESSAGE, faction.tag(), world.getName());
        StringBuilder builder = new StringBuilder();
        String str;
        final String separator = "   ";
        for (FLoc loc : displayList) {
            str = (loc.x << 4 | 8) + "," + (loc.z << 4 | 8) + (loc.total > 1 ? (" (" + loc.total + ")") : "");
            if (builder.length() + separator.length() + str.length() > 75) {
                sender.msg(builder.toString());
                builder.setLength(0);
            } else if (!builder.isEmpty()) {
                builder.append(separator);
            }
            builder.append(str);
        }
        if (!builder.isEmpty()) {
            sender.msg(builder.toString());
        }
    }

    private void addIf(Set<FLoc> set, Queue<FLoc> queue, FLoc loc) {
        if (loc != null && !set.contains(loc)) {
            set.add(loc);
            queue.add(loc);
        }
    }

    private long getLong(long x, int z) {
        return (x << 32) | z;
    }

    private static class FLoc {
        private final int x;
        private final int z;
        private boolean processed;
        private int total = 1;

        public FLoc(long x, long z) {
            this.x = (int) x;
            this.z = (int) z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FLoc fLoc = (FLoc) o;
            return x == fLoc.x && z == fLoc.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, z);
        }
    }
}
