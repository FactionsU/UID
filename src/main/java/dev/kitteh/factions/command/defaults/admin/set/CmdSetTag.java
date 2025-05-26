package dev.kitteh.factions.command.defaults.admin.set;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
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

import java.util.List;
import java.util.function.BiConsumer;

public class CmdSetTag implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> manager.command(
                builder.literal("tag")
                        .commandDescription(Cloudy.desc(TL.COMMAND_TAG_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.isBypass()))
                        .required("faction", FactionParser.of(FactionParser.Include.SELF))
                        .required("tag", StringParser.stringParser())
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        Faction faction = context.get("faction");
        String tag = context.get("tag");

        if (Factions.factions().get(tag) != null) {
            sender.msg(TL.COMMAND_TAG_TAKEN);
            return;
        }

        List<String> errors = MiscUtil.validateTag(tag);
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

        String oldTag = faction.tag();
        faction.tag(tag);

        // Inform
        for (FPlayer fplayer : FPlayers.fPlayers().online()) {
            if (fplayer.faction() == faction) {
                fplayer.msg(TL.COMMAND_TAG_FACTION, sender.describeToLegacy(faction, true), faction.tagLegacy(faction));
                continue;
            }

            // Broadcast the tag change (if applicable)
            if (FactionsPlugin.instance().conf().factions().chat().isBroadcastTagChanges()) {
                Faction fac = fplayer.faction();
                fplayer.msg(TL.COMMAND_TAG_CHANGED, sender.colorLegacyStringTo(fac) + oldTag, faction.tagLegacy(fac));
            }
        }

        FTeamWrapper.updatePrefixes(faction);
    }
}
