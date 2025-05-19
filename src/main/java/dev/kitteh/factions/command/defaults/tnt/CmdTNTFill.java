package dev.kitteh.factions.command.defaults.tnt;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.util.Pair;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.IntegerParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class CmdTNTFill implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("fill")
                            .commandDescription(Cloudy.desc(TL.COMMAND_TNT_FILL_DESCRIPTION))
                            .permission(
                                    builder.commandPermission()
                                            .and(Cloudy.hasPermission(Permission.TNT_FILL))
                                            .and(Cloudy.hasSelfFactionPerms(PermissibleActions.TNTWITHDRAW))
                            )
                            .required("radius", IntegerParser.integerParser(1))
                            .required("amount", IntegerParser.integerParser(1))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();
        Faction faction = sender.faction();

        if (!faction.equals(Board.board().factionAt(new FLocation(player.getLocation())))) {
            sender.msg(TL.COMMAND_TNT_TERRITORYONLY);
            return;
        }
        final int radius = context.get("radius");
        final int amount = context.get("amount");

        if (radius <= 0 || amount <= 0) {
            sender.msg(TL.COMMAND_TNT_FILL_FAIL_POSITIVE);
            return;
        }

        if (amount > faction.tntBank()) {
            sender.msg(TL.COMMAND_TNT_FILL_FAIL_NOTENOUGH, amount);
            return;
        }

        if (radius > FactionsPlugin.instance().conf().commands().tnt().getMaxRadius()) {
            sender.msg(TL.COMMAND_TNT_FILL_FAIL_MAXRADIUS, radius, FactionsPlugin.instance().conf().commands().tnt().getMaxRadius());
            return;
        }

        List<Dispenser> list = getDispensers(player.getLocation(), radius, faction);
        Collections.reverse(list);

        int remaining = amount;
        int dispenserCount = 0;
        boolean firstRound = true;

        while (remaining > 0 && !list.isEmpty()) {
            int per = Math.max(1, remaining / list.size());
            Iterator<Dispenser> iterator = list.iterator();
            while (iterator.hasNext() && remaining >= per) {
                int left = getCount(iterator.next().getInventory().addItem(getStacks(per)).values());
                remaining -= per - left;
                if (firstRound && ((per - left) > 0)) {
                    dispenserCount++;
                }
                if (left > 0) {
                    iterator.remove();
                }
            }
            firstRound = false;
        }

        faction.tntBank(faction.tntBank() - amount + remaining);

        sender.msg(TL.COMMAND_TNT_FILL_MESSAGE, amount - remaining, dispenserCount, faction.tntBank());
    }

    static ItemStack[] getStacks(int count) {
        if (count < 65) {
            return new ItemStack[]{new ItemStack(Material.TNT, count)};
        } else {
            List<ItemStack> stack = new ArrayList<>();
            while (count > 0) {
                stack.add(new ItemStack(Material.TNT, Math.min(64, count)));
                count -= Math.min(64, count);
            }
            return stack.toArray(new ItemStack[0]);
        }
    }

    static List<Dispenser> getDispensers(Location location, int radius, Faction faction) {
        List<Pair<Dispenser, Double>> list = new ArrayList<>();
        for (int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
            for (int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
                for (int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
                    Block block = location.getWorld().getBlockAt(x, y, z);
                    if (Board.board().factionAt(new FLocation(block)) != faction) {
                        continue;
                    }
                    if (block.getType() == Material.DISPENSER) {
                        list.add(Pair.of((Dispenser) block.getState(), Math.sqrt(((location.getBlockX() - x) ^ 2) + ((location.getBlockY() - y) ^ 2) + ((location.getBlockZ() - z) ^ 2))));
                    }
                }
            }
        }
        list.sort(Comparator.comparing(Pair::getRight));
        return list.stream().map(Pair::getLeft).collect(Collectors.toCollection(ArrayList::new));
    }

    static int getCount(Collection<? extends ItemStack> items) {
        int count = 0;
        for (ItemStack stack : items) {
            count += stack.getAmount();
        }
        return count;
    }
}
