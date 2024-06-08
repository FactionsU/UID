package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.PermissibleActions;
import com.massivecraft.factions.perms.Role;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;

public class CmdDelhome extends FCommand {

    public CmdDelhome() {
        this.aliases.add("delhome");

        this.requirements = new CommandRequirements.Builder(Permission.DELHOME)
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

        if (!context.faction.hasHome()) {
            context.msg(TL.COMMAND_HOME_NOHOME + (context.fPlayer.getRole().value < Role.MODERATOR.value ? TL.GENERIC_ASKYOURLEADER.toString() : TL.GENERIC_YOUSHOULD.toString()));
            context.sendMessage(FCmdRoot.getInstance().cmdSethome.getUsageTemplate(context));
            return;
        }

        if (FactionsPlugin.getInstance().conf().factions().homes().isRequiredToHaveHomeBeforeSettingWarps() && !context.faction.getWarps().isEmpty()) {
            context.msg(TL.COMMAND_HOME_WARPSREMAIN);
            return;
        }

        if (!context.payForCommand(FactionsPlugin.getInstance().conf().economy().getCostDelhome(), TL.COMMAND_DELHOME_TOSET, TL.COMMAND_DELHOME_FORSET)) {
            return;
        }

        context.faction.delHome();

        context.faction.msg(TL.COMMAND_DELHOME_DEL, context.fPlayer.describeTo(context.faction, true));
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_DELHOME_DESCRIPTION;
    }

}
