package dev.kitteh.factions.cmd.tnt;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.cmd.CommandContext;
import dev.kitteh.factions.cmd.CommandRequirements;
import dev.kitteh.factions.cmd.FCommand;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CmdTNTWithdraw extends FCommand {
    public CmdTNTWithdraw() {
        super();
        this.aliases.add("withdraw");
        this.aliases.add("w");
        this.requiredArgs.add("amount");

        this.requirements = new CommandRequirements.Builder(Permission.TNT_WITHDRAW).withAction(PermissibleActions.TNTWITHDRAW).memberOnly().build();
    }

    @Override
    public void perform(CommandContext context) {
        Player player = context.player;
        if (!context.faction.equals(Board.getInstance().getFactionAt(new FLocation(player.getLocation())))) {
            context.msg(TL.COMMAND_TNT_TERRITORYONLY);
            return;
        }
        int amount = context.argAsInt(0, -1);
        if (amount <= 0) {
            context.msg(TL.COMMAND_TNT_WITHDRAW_FAIL_POSITIVE, amount);
            return;
        }

        if (context.faction.getTNTBank() < amount) {
            context.msg(TL.COMMAND_TNT_WITHDRAW_FAIL_NOTENOUGH, amount);
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

        context.faction.setTNTBank(context.faction.getTNTBank() - amount + notTaken);
        context.msg(TL.COMMAND_TNT_WITHDRAW_MESSAGE, (amount - notTaken), context.faction.getTNTBank());
    }


    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_TNT_WITHDRAW_DESCRIPTION;
    }
}
