package dev.kitteh.factions.command.defaults.tnt;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.parser.standard.IntegerParser;

import java.util.Map;

public class CmdTNTDeposit implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = Confs.tl().commands().tnt();
            manager.command(
                    builder.literal(tl.getSubCmdDeposit())
                            .commandDescription(Cloudy.desc(tl.getDepositDescription()))
                            .permission(
                                    builder.commandPermission()
                                            .and(Cloudy.hasPermission(Permission.TNT_DEPOSIT))
                                            .and(Cloudy.hasSelfFactionPerms(PermissibleActions.TNTDEPOSIT))
                            )
                            .optional("amount", IntegerParser.integerParser(1))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = Confs.tl().commands().tnt();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();
        Faction faction = sender.faction();

        if (!faction.equals(Board.board().factionAt(new FLocation(player.getLocation())))) {
            sender.sendRichMessage(tl.getTerritoryOnly());
            return;
        }
        int amount = context.getOrDefault("amount", Integer.MAX_VALUE);
        if (amount <= 0) {
            sender.sendRichMessage(tl.getDepositFailPositive());
            return;
        }

        int bankMax = faction.tntBankMax();
        if (bankMax <= faction.tntBank()) {
            sender.sendRichMessage(tl.getDepositFailFull());
            return;
        }

        ItemStack tntStack = new ItemStack(Material.TNT);
        int available = 0;
        for (ItemStack i : player.getInventory().getStorageContents()) {
            if (tntStack.isSimilar(i)) {
                available += i.getAmount();
            }
        }
        amount = Math.min(amount, available);
        amount = Math.min(amount, bankMax);

        int current = amount;
        Map<Integer, ? extends ItemStack> all = player.getInventory().all(Material.TNT);
        for (Map.Entry<Integer, ? extends ItemStack> entry : all.entrySet()) {
            if (!tntStack.isSimilar(entry.getValue())) {
                continue; // Don't eat special items
            }
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
        faction.tntBank(faction.tntBank() + amount);
        sender.sendRichMessage(tl.getDepositSuccess(), Placeholder.unparsed("count", String.valueOf(faction.tntBank())));
    }
}
