package dev.kitteh.factions.cmd;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.perms.Role;
import dev.kitteh.factions.struct.Permission;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.TextUtil;

public class CmdTitle extends FCommand {

    public CmdTitle() {
        this.aliases.add("title");

        this.requiredArgs.add("player");
        this.optionalArgs.put("title", "title");

        this.requirements = new CommandRequirements.Builder(Permission.TITLE)
                .memberOnly()
                .withRole(Role.MODERATOR)
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        FPlayer you = context.argAsBestFPlayerMatch(0);
        if (you == null) {
            return;
        }

        context.args.removeFirst();
        String title = TextUtil.implode(context.args, " ");

        title = title.replaceAll(",", "");

        if (!context.canIAdministerYou(context.fPlayer, you)) {
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        if (!context.payForCommand(FactionsPlugin.getInstance().conf().economy().getCostTitle(), TL.COMMAND_TITLE_TOCHANGE, TL.COMMAND_TITLE_FORCHANGE)) {
            return;
        }

        you.setTitle(context.sender, title);

        // Inform
        context.faction.msg(TL.COMMAND_TITLE_CHANGED, context.fPlayer.describeTo(context.faction, true), you.describeTo(context.faction, true));
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_TITLE_DESCRIPTION;
    }

}
