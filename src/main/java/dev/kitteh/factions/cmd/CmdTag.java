package dev.kitteh.factions.cmd;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.event.FactionRenameEvent;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.scoreboard.FTeamWrapper;
import dev.kitteh.factions.util.MiscUtil;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.Bukkit;

import java.util.ArrayList;

public class CmdTag extends FCommand {

    public CmdTag() {
        this.aliases.add("tag");
        this.aliases.add("rename");

        this.requiredArgs.add("faction tag");

        this.requirements = new CommandRequirements.Builder(Permission.TAG)
                .memberOnly()
                .withRole(Role.MODERATOR)
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        String tag = context.argAsString(0);

        // TODO does not first shouldCancel cover selfcase?
        if (Factions.getInstance().isTagTaken(tag) && !MiscUtil.getComparisonString(tag).equals(context.faction.getComparisonTag())) {
            context.msg(TL.COMMAND_TAG_TAKEN);
            return;
        }

        ArrayList<String> errors = MiscUtil.validateTag(tag);
        if (!errors.isEmpty()) {
            context.sendMessage(errors);
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make sure they can pay
        if (!context.canAffordCommand(FactionsPlugin.getInstance().conf().economy().getCostTag(), TL.COMMAND_TAG_TOCHANGE.toString())) {
            return;
        }

        // trigger the faction rename event (cancellable)
        FactionRenameEvent renameEvent = new FactionRenameEvent(context.fPlayer, tag);
        Bukkit.getServer().getPluginManager().callEvent(renameEvent);
        if (renameEvent.isCancelled()) {
            return;
        }

        // then make 'em pay (if applicable)
        if (!context.payForCommand(FactionsPlugin.getInstance().conf().economy().getCostTag(), TL.COMMAND_TAG_TOCHANGE, TL.COMMAND_TAG_FORCHANGE)) {
            return;
        }

        String oldtag = context.faction.getTag();
        context.faction.setTag(tag);

        // Inform
        for (FPlayer fplayer : FPlayers.getInstance().getOnlinePlayers()) {
            if (fplayer.getFactionIntId() == context.faction.getIntId()) {
                fplayer.msg(TL.COMMAND_TAG_FACTION, context.fPlayer.describeTo(context.faction, true), context.faction.getTag(context.faction));
                continue;
            }

            // Broadcast the tag change (if applicable)
            if (FactionsPlugin.getInstance().conf().factions().chat().isBroadcastTagChanges()) {
                Faction faction = fplayer.getFaction();
                fplayer.msg(TL.COMMAND_TAG_CHANGED, context.fPlayer.getColorStringTo(faction) + oldtag, context.faction.getTag(faction));
            }
        }

        FTeamWrapper.updatePrefixes(context.faction);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_TAG_DESCRIPTION;
    }

}
