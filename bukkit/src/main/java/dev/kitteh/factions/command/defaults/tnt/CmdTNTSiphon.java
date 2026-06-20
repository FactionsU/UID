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
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.IntegerParser;

import java.util.List;
import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdTNTSiphon implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = FactionsPlugin.instance().tl().commands().tnt();
            manager.command(
                    builder.literal(tl.getSubCmdSiphon())
                            .commandDescription(Cloudy.desc(tl.getSiphonDescription()))
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
        var tl = FactionsPlugin.instance().tl().commands().tnt();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();
        Faction faction = sender.faction();

        if (!faction.equals(Board.board().factionAt(new FLocation(player.getLocation())))) {
            sender.sendRichMessage(tl.getTerritoryOnly());
            return;
        }
        final int radius = context.get("radius");
        final int amount = context.getOrDefault("amount", -1);

        if (radius <= 0) {
            sender.sendRichMessage(tl.getSiphonFailPositive());
            return;
        }

        int bankMax = faction.tntBankMax();
        if (bankMax <= faction.tntBank()) {
            sender.sendRichMessage(tl.getSiphonFailFull());
            return;
        }

        if (radius > FactionsPlugin.instance().conf().commands().tnt().getMaxRadius()) {
            sender.sendRichMessage(tl.getSiphonFailMaxRadius(),
                    Placeholder.unparsed("value", String.valueOf(radius)),
                    Placeholder.unparsed("max", String.valueOf(FactionsPlugin.instance().conf().commands().tnt().getMaxRadius())));
            return;
        }

        List<Dispenser> list = CmdTNTFill.getDispensers(player.getLocation(), radius, faction);

        int canTake = bankMax - faction.tntBank();

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

        faction.tntBank(faction.tntBank() + acquired);

        sender.sendRichMessage(tl.getSiphonMessage(),
                Placeholder.unparsed("count", String.valueOf(acquired)),
                Placeholder.unparsed("total", String.valueOf(faction.tntBank())));
    }
}
