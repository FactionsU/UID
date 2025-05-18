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
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.IntegerParser;

import java.util.Map;
import java.util.function.BiConsumer;

public class CmdTNTDeposit implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("deposit")
                            .commandDescription(Cloudy.desc(TL.COMMAND_TNT_DEPOSIT_DESCRIPTION))
                            .permission(
                                    builder.commandPermission()
                                            .and(Cloudy.hasPermission(Permission.TNT_DEPOSIT))
                                            .and(Cloudy.hasSelfFactionPerms(PermissibleActions.TNTDEPOSIT))
                            )
                            .required("amount", IntegerParser.integerParser(1))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();
        Faction faction = sender.getFaction();

        if (!faction.equals(Board.board().factionAt(new FLocation(player.getLocation())))) {
            sender.msg(TL.COMMAND_TNT_TERRITORYONLY);
            return;
        }
        int amount = context.get("amount");
        if (amount <= 0) {
            sender.msg(TL.COMMAND_TNT_DEPOSIT_FAIL_POSITIVE, amount);
            return;
        }

        if (!player.getInventory().containsAtLeast(new ItemStack(Material.TNT), amount)) {
            sender.msg(TL.COMMAND_TNT_DEPOSIT_FAIL_NOTENOUGH, amount);
            return;
        }

        if (FactionsPlugin.getInstance().conf().commands().tnt().isAboveMaxStorage(faction.getTNTBank() + amount)) {
            if (FactionsPlugin.getInstance().conf().commands().tnt().getMaxStorage() == faction.getTNTBank()) {
                sender.msg(TL.COMMAND_TNT_DEPOSIT_FAIL_FULL, FactionsPlugin.getInstance().conf().commands().tnt().getMaxStorage());
                return;
            }
            amount = FactionsPlugin.getInstance().conf().commands().tnt().getMaxStorage() - faction.getTNTBank();
        }
        int current = amount;
        Map<Integer, ? extends ItemStack> all = player.getInventory().all(Material.TNT);
        for (Map.Entry<Integer, ? extends ItemStack> entry : all.entrySet()) {
            final int count = entry.getValue().getAmount();
            final int newCount = Math.max(0, count - current);
            current -= (count - newCount);
            if (newCount == 0) {
                player.getInventory().setItem(entry.getKey(), null);
            } else {
                player.getInventory().getItem(entry.getKey()).setAmount(newCount);
            }
            if (current == 0) {
                break;
            }
        }
        faction.setTNTBank(faction.getTNTBank() + amount);
        sender.msg(TL.COMMAND_TNT_DEPOSIT_SUCCESS, faction.getTNTBank());
    }
}
