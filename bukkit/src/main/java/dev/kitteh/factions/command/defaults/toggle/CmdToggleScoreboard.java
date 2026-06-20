package dev.kitteh.factions.command.defaults.toggle;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.scoreboard.FScoreboard;
import dev.kitteh.factions.util.Permission;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdToggleScoreboard implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        var tl = FactionsPlugin.instance().tl().commands().toggleScoreboard();
        return (manager, builder, help) -> manager.command(
                builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                        .commandDescription(Cloudy.desc(tl.getDescription()))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.SCOREBOARD).and(Cloudy.isPlayer())))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().toggleScoreboard();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        boolean toggleTo = !sender.showScoreboard();
        FScoreboard board = FScoreboard.get(sender);
        if (board == null) {
            sender.sendRichMessage(tl.getDisabled());
        } else {
            sender.sendRichMessage(tl.getToggleSb(), Placeholder.unparsed("value", String.valueOf(toggleTo)));
            board.setSidebarVisibility(toggleTo);
        }
        sender.showScoreboard(toggleTo);
    }
}
