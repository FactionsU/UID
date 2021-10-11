package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.perms.PermissibleActions;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

public class CmdListClaims extends FCommand {

    public CmdListClaims() {
        super();
        this.aliases.add("listclaims");
        this.aliases.add("listclaim");

        this.optionalArgs.put("world", "currentworld");
        this.optionalArgs.put("faction", "yours");

        this.requirements = new CommandRequirements.Builder(Permission.LISTCLAIMS)
                .withAction(PermissibleActions.LISTCLAIMS)
                .memberOnly()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        World world = context.player.getWorld();
        if (context.argIsSet(0)) {
            world = Bukkit.getWorld(context.argAsString(0));
        }
        if (world == null) {
            context.msg(TL.COMMAND_LISTCLAIMS_INVALIDWORLD, context.argAsString(0));
            return;
        }
        Faction faction = context.faction;
        if (context.argIsSet(1)) {
            if (Permission.LISTCLAIMS_OTHER.has(context.sender, true)) {
                faction = context.argAsFaction(1);
            } else {
                return;
            }
        }
        if (faction == null) {
            context.msg(TL.GENERIC_NOFACTIONMATCH, context.argAsString(1));
            return;
        }
        Map<Long, FLoc> worldClaims = new HashMap<>();
        for (FLocation loc : Board.getInstance().getAllClaims(faction)) {
            if (loc.getWorld().equals(world)) {
                worldClaims.put(getLong(loc.getX(), (int) loc.getZ()), new FLoc(loc.getX(), loc.getZ()));
            }
        }
        if (worldClaims.isEmpty()) {
            context.msg(TL.COMMAND_LISTCLAIMS_NOCLAIMS, faction.getTag(), world.getName());
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

        context.msg(TL.COMMAND_LISTCLAIMS_MESSAGE, faction.getTag(), world.getName());
        StringBuilder builder = new StringBuilder();
        String str;
        final String separator = "   ";
        for (FLoc loc : displayList) {
            str = (loc.x << 4 | 8) + "," + (loc.z << 4 | 8) + (loc.total > 1 ? (" (" + loc.total + ")") : "");
            if (builder.length() + separator.length() + str.length() > 75) {
                context.msg(builder.toString());
                builder.setLength(0);
            } else if (builder.length() > 0) {
                builder.append(separator);
            }
            builder.append(str);
        }
        if (builder.length() > 0) {
            context.msg(builder.toString());
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

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_LISTCLAIMS_DESCRIPTION;
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
