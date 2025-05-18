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
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.IntegerParser;

import java.util.List;
import java.util.function.BiConsumer;

public class CmdTNTSiphon implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("siphon")
                            .commandDescription(Cloudy.desc(TL.COMMAND_TNT_SIPHON_DESCRIPTION))
                            .permission(
                                    builder.commandPermission()
                                            .and(Cloudy.hasPermission(Permission.TNT_SIPHON))
                                            .and(Cloudy.hasSelfFactionPerms(PermissibleActions.TNTDEPOSIT))
                            )
                            .required("radius", IntegerParser.integerParser(1))
                            .optional("amount", IntegerParser.integerParser(1))
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
        final int radius = context.get("radius");
        final int amount = context.getOrDefault("amount", -1);

        if (radius <= 0) {
            sender.msg(TL.COMMAND_TNT_SIPHON_FAIL_POSITIVE);
            return;
        }

        if (FactionsPlugin.getInstance().conf().commands().tnt().isAboveMaxStorage(faction.getTNTBank() + 1)) {
            sender.msg(TL.COMMAND_TNT_SIPHON_FAIL_FULL);
            return;
        }

        if (radius > FactionsPlugin.getInstance().conf().commands().tnt().getMaxRadius()) {
            sender.msg(TL.COMMAND_TNT_SIPHON_FAIL_MAXRADIUS, radius, FactionsPlugin.getInstance().conf().commands().tnt().getMaxRadius());
            return;
        }

        List<Dispenser> list = CmdTNTFill.getDispensers(player.getLocation(), radius, faction);

        int canTake;
        if (FactionsPlugin.getInstance().conf().commands().tnt().getMaxStorage() < 0) {
            canTake = Integer.MAX_VALUE;
        } else {
            canTake = FactionsPlugin.getInstance().conf().commands().tnt().getMaxStorage();
        }

        canTake -= faction.getTNTBank();

        if (amount > 0 && amount < canTake) {
            canTake = amount;
        }

        int remaining = canTake;

        for (Dispenser dispenser : list) {
            if (remaining > (64 * 9)) {
                remaining -= CmdTNTFill.getCount(dispenser.getInventory().all(Material.TNT).values());
                dispenser.getInventory().remove(Material.TNT);
            } else {
                remaining = CmdTNTFill.getCount(dispenser.getInventory().removeItem(CmdTNTFill.getStacks(remaining)).values());
            }
            if (remaining == 0) {
                break;
            }
        }

        int acquired = canTake - remaining;

        faction.setTNTBank(faction.getTNTBank() + acquired);

        sender.msg(TL.COMMAND_TNT_SIPHON_MESSAGE, acquired, faction.getTNTBank());
    }
}
