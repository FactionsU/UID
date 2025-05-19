package dev.kitteh.factions.command.defaults.tnt;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class CmdTNTWithdraw implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> manager.command(
                builder.literal("withdraw")
                        .commandDescription(Cloudy.desc(TL.COMMAND_TNT_WITHDRAW_DESCRIPTION))
                        .permission(
                                builder.commandPermission()
                                        .and(Cloudy.hasPermission(Permission.TNT_WITHDRAW))
                                        .and(Cloudy.hasSelfFactionPerms(PermissibleActions.TNTWITHDRAW))
                        )
                        .required("amount", IntegerParser.integerParser(1))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();
        Faction faction = sender.faction();

        if (!faction.equals(Board.board().factionAt(new FLocation(player.getLocation())))) {
            sender.msg(TL.COMMAND_TNT_TERRITORYONLY);
            return;
        }
        int amount = context.get("amount");
        if (amount <= 0) {
            sender.msg(TL.COMMAND_TNT_WITHDRAW_FAIL_POSITIVE, amount);
            return;
        }

        if (faction.tntBank() < amount) {
            sender.msg(TL.COMMAND_TNT_WITHDRAW_FAIL_NOTENOUGH, amount);
            return;
        }

        List<ItemStack> stacks = new ArrayList<>();
        int toGive = amount;
        while (toGive > 0) {
            int giving = Math.min(toGive, 64);
            stacks.add(new ItemStack(Material.TNT, giving));
            toGive -= giving;
        }
        Map<Integer, ItemStack> remaining = player.getInventory().addItem(stacks.toArray(new ItemStack[0]));

        int notTaken = 0;

        for (ItemStack stack : remaining.values()) {
            notTaken += stack.getAmount();
        }

        faction.tntBank(faction.tntBank() - amount + notTaken);
        sender.msg(TL.COMMAND_TNT_WITHDRAW_MESSAGE, (amount - notTaken), faction.tntBank());
    }
}
