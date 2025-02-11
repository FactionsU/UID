package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.perms.Role;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;
import com.massivecraft.factions.util.TextUtil;

public class CmdLink extends FCommand {

    public CmdLink() {
        super();
        this.aliases.add("link");

        this.optionalArgs.put("URL", "URL");

        this.requirements = new CommandRequirements.Builder(Permission.LINK)
                .memberOnly()
                .noErrorOnManyArgs()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        if (context.args.isEmpty()) {
            context.msg(TL.COMMAND_LINK_SHOW, this.plugin.conf().colors().relations().getMember(), context.faction.getLink());
            return;
        }

        if (!context.fPlayer.getRole().isAtLeast(Role.MODERATOR)) {
            context.msg(TL.GENERIC_YOUMUSTBE, Role.MODERATOR.translation);
            return;
        }

        String newLink = TextUtil.implode(context.args, " ").replaceAll("[%\\s]", "").replaceAll("(&([a-f0-9klmnor]))", "& $2");
        if (!newLink.matches("^https?://.+")) {
            context.msg(TL.COMMAND_LINK_INVALIDURL);
            return;
        }

        context.faction.setLink(newLink);

        for (FPlayer fplayer : context.faction.getFPlayersWhereOnline(true)) {
            fplayer.msg(TL.COMMAND_LINK_CHANGED, context.faction.describeTo(context.fPlayer));
            fplayer.sendMessage(context.faction.getLink());
            return;
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_LINK_DESCRIPTION;
    }

}
