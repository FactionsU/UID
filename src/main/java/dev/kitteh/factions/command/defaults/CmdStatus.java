package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.landraidcontrol.PowerControl;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class CmdStatus implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("status")
                            .commandDescription(Cloudy.desc(TL.COMMAND_STATUS_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.STATUS).and(Cloudy.hasFaction())))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        ArrayList<String> ret = new ArrayList<>();
        for (FPlayer fp : sender.faction().members()) {
            String humanized = DurationFormatUtils.formatDurationWords(System.currentTimeMillis() - fp.lastLogin(), true, true) + TL.COMMAND_STATUS_AGOSUFFIX;
            String last = fp.isOnline() ? ChatColor.GREEN + TL.COMMAND_STATUS_ONLINE.toString() : (System.currentTimeMillis() - fp.lastLogin() < 432000000 ? ChatColor.YELLOW + humanized : ChatColor.RED + humanized);
            String power;
            if (FactionsPlugin.instance().landRaidControl() instanceof PowerControl) {
                power = ChatColor.YELLOW + String.valueOf(fp.powerRounded()) + " / " + fp.powerMaxRounded() + ChatColor.RESET;
            } else {
                power = "n/a";
            }
            ret.add(String.format(TL.COMMAND_STATUS_FORMAT.toString(), ChatColor.GOLD + fp.role().getPrefix() + fp.name() + ChatColor.RESET, power, last).trim());
        }
        sender.sendMessage(ret);
    }
}
