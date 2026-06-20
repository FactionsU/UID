package dev.kitteh.factions.command.defaults.tnt;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.util.Permission;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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
import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdTNTWithdraw implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = FactionsPlugin.instance().tl().commands().tnt();
            manager.command(
                    builder.literal(tl.getSubCmdWithdraw())
                            .commandDescription(Cloudy.desc(tl.getWithdrawDescription()))
                            .permission(
                                    builder.commandPermission()
                                            .and(Cloudy.hasPermission(Permission.TNT_WITHDRAW))
                                            .and(Cloudy.hasSelfFactionPerms(PermissibleActions.TNTWITHDRAW))
                            )
                            .required("amount", IntegerParser.integerParser(1))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().tnt();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();
        Faction faction = sender.faction();

        if (!faction.equals(Board.board().factionAt(new FLocation(player.getLocation())))) {
            sender.sendRichMessage(tl.getTerritoryOnly());
            return;
        }
        int amount = context.get("amount");
        if (amount <= 0) {
            sender.sendRichMessage(tl.getWithdrawFailPositive());
            return;
        }

        if (faction.tntBank() < amount) {
            sender.sendRichMessage(tl.getWithdrawFailNotEnough(), Placeholder.unparsed("count", String.valueOf(amount)));
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
        sender.sendRichMessage(tl.getWithdrawMessage(),
                Placeholder.unparsed("count", String.valueOf(amount - notTaken)),
                Placeholder.unparsed("remaining", String.valueOf(faction.tntBank())));
    }
}
