package dev.kitteh.factions.cmd;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.event.FactionSetHomeEvent;
import dev.kitteh.factions.perms.PermissibleActions;
import dev.kitteh.factions.struct.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.Bukkit;

public class CmdSethome extends FCommand {

    public CmdSethome() {
        this.aliases.add("sethome");

        this.requirements = new CommandRequirements.Builder(Permission.SETHOME)
                .memberOnly()
                .withAction(PermissibleActions.SETHOME)
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        if (!FactionsPlugin.getInstance().conf().factions().homes().isEnabled()) {
            context.msg(TL.COMMAND_SETHOME_DISABLED);
            return;
        }

        // Can the player set the faction home HERE?
        if (!Permission.BYPASS.has(context.player) &&
                FactionsPlugin.getInstance().conf().factions().homes().isMustBeInClaimedTerritory() &&
                Board.getInstance().getFactionAt(new FLocation(context.player)) != context.faction) {
            context.msg(TL.COMMAND_SETHOME_NOTCLAIMED);
            return;
        }

        if (!context.canAffordCommand(FactionsPlugin.getInstance().conf().economy().getCostSethome(), TL.COMMAND_SETHOME_TOSET.toString())) {
            return;
        }

        FactionSetHomeEvent setHomeEvent = new FactionSetHomeEvent(context.fPlayer, context.player.getLocation());
        Bukkit.getServer().getPluginManager().callEvent(setHomeEvent);
        if (setHomeEvent.isCancelled()) {
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        if (!context.payForCommand(FactionsPlugin.getInstance().conf().economy().getCostSethome(), TL.COMMAND_SETHOME_TOSET, TL.COMMAND_SETHOME_FORSET)) {
            return;
        }

        context.faction.setHome(context.player.getLocation());

        context.faction.msg(TL.COMMAND_SETHOME_SET, context.fPlayer.describeTo(context.faction, true));
        context.faction.sendMessage(FCmdRoot.getInstance().cmdHome.getUsageTemplate(context));
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_SETHOME_DESCRIPTION;
    }

}
