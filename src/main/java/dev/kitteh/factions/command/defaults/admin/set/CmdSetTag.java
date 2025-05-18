package dev.kitteh.factions.command.defaults.admin.set;

import dev.kitteh.factions.*;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FactionRenameEvent;
import dev.kitteh.factions.scoreboard.FTeamWrapper;
import dev.kitteh.factions.util.MiscUtil;
import dev.kitteh.factions.util.TL;
import org.bukkit.Bukkit;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.StringParser;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class CmdSetTag implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("tag")
                            .commandDescription(Cloudy.desc(TL.COMMAND_TAG_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.isBypass()))
                            .required("faction", FactionParser.of(FactionParser.Include.SELF))
                            .required("tag", StringParser.stringParser())
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        Faction faction = context.get("faction");
        String tag = context.get("tag");

        // TODO does not first shouldCancel cover selfcase?
        if (Factions.factions().isTagTaken(tag) && !MiscUtil.getComparisonString(tag).equals(faction.getComparisonTag())) {
            sender.msg(TL.COMMAND_TAG_TAKEN);
            return;
        }

        ArrayList<String> errors = MiscUtil.validateTag(tag);
        if (!errors.isEmpty()) {
            sender.sendMessage(errors);
            return;
        }

        // trigger the faction rename event (cancellable)
        FactionRenameEvent renameEvent = new FactionRenameEvent(sender, tag);
        Bukkit.getServer().getPluginManager().callEvent(renameEvent);
        if (renameEvent.isCancelled()) {
            return;
        }

        String oldTag = faction.getTag();
        faction.setTag(tag);

        // Inform
        for (FPlayer fplayer : FPlayers.fPlayers().getOnlinePlayers()) {
            if (fplayer.getFaction() == faction) {
                fplayer.msg(TL.COMMAND_TAG_FACTION, sender.describeTo(faction, true), faction.getTag(faction));
                continue;
            }

            // Broadcast the tag change (if applicable)
            if (FactionsPlugin.getInstance().conf().factions().chat().isBroadcastTagChanges()) {
                Faction fac = fplayer.getFaction();
                fplayer.msg(TL.COMMAND_TAG_CHANGED, sender.getColorStringTo(fac) + oldTag, faction.getTag(fac));
            }
        }

        FTeamWrapper.updatePrefixes(faction);
    }
}
