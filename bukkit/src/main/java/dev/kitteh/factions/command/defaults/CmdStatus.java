package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.landraidcontrol.PowerControl;
import dev.kitteh.factions.util.Permission;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdStatus implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        var tl = FactionsPlugin.instance().tl().commands().status();
        return (manager, builder, _) -> manager.command(
                builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                        .commandDescription(Cloudy.desc(tl.getDescription()))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.STATUS).and(Cloudy.hasFaction())))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().status();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        for (FPlayer fp : sender.faction().members()) {
            String humanized = DurationFormatUtils.formatDurationWords(System.currentTimeMillis() - fp.lastLogin(), true, true) + tl.getAgoSuffix();
            String lastSeen;
            if (fp.isOnline()) {
                lastSeen = tl.getOnline();
            } else {
                lastSeen = (System.currentTimeMillis() - fp.lastLogin() < 432000000 ? "<yellow>" : "<red>") + humanized;
            }

            String power;
            if (FactionsPlugin.instance().landRaidControl() instanceof PowerControl) {
                power = fp.powerRounded() + " / " + fp.powerMaxRounded();
            } else {
                power = "n/a";
            }

            String playerName = fp.role().getPrefix() + fp.name();
            sender.sendRichMessage(tl.getFormat(),
                    Placeholder.parsed("player", "<gold>" + playerName),
                    Placeholder.parsed("power", power),
                    Placeholder.parsed("last_seen", lastSeen));
        }
    }
}
