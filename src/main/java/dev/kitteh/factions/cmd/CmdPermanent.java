package dev.kitteh.factions.cmd;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.struct.Permission;
import dev.kitteh.factions.util.TL;


public class CmdPermanent extends FCommand {

    public CmdPermanent() {
        super();
        this.aliases.add("permanent");

        this.requiredArgs.add("faction");

        this.requirements = new CommandRequirements.Builder(Permission.SET_PERMANENT).build();
    }

    @Override
    public void perform(CommandContext context) {
        Faction faction = context.argAsFaction(0);
        if (faction == null) {
            return;
        }

        String change;
        if (faction.isPermanent()) {
            change = TL.COMMAND_PERMANENT_REVOKE.toString();
            faction.setPermanent(false);
        } else {
            change = TL.COMMAND_PERMANENT_GRANT.toString();
            faction.setPermanent(true);
        }

        FactionsPlugin.getInstance().log((context.fPlayer == null ? "A server admin" : context.fPlayer.getName()) + " " + change + " the faction \"" + faction.getTag() + "\".");

        // Inform all players
        for (FPlayer fplayer : FPlayers.getInstance().getOnlinePlayers()) {
            String blame = (context.fPlayer == null ? TL.GENERIC_SERVERADMIN.toString() : context.fPlayer.describeTo(fplayer, true));
            if (fplayer.getFaction() == faction) {
                fplayer.msg(TL.COMMAND_PERMANENT_YOURS, blame, change);
            } else {
                fplayer.msg(TL.COMMAND_PERMANENT_OTHER, blame, change, faction.getTag(fplayer));
            }
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_PERMANENT_DESCRIPTION;
    }
}
