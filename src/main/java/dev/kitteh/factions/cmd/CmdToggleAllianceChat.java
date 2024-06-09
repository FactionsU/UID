package dev.kitteh.factions.cmd;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;

public class CmdToggleAllianceChat extends FCommand {

    public CmdToggleAllianceChat() {
        super();
        this.aliases.add("tac");
        this.aliases.add("togglealliancechat");
        this.aliases.add("ac");

        this.requirements = new CommandRequirements.Builder(Permission.TOGGLE_ALLIANCE_CHAT)
                .memberOnly()
                .noDisableOnLock()
                .build();
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_TOGGLEALLIANCECHAT_DESCRIPTION;
    }

    @Override
    public void perform(CommandContext context) {
        if (!FactionsPlugin.getInstance().conf().factions().chat().isFactionOnlyChat()) {
            context.msg(TL.COMMAND_CHAT_DISABLED.toString());
            return;
        }

        boolean ignoring = context.fPlayer.isIgnoreAllianceChat();

        context.msg(ignoring ? TL.COMMAND_TOGGLEALLIANCECHAT_UNIGNORE : TL.COMMAND_TOGGLEALLIANCECHAT_IGNORE);
        context.fPlayer.setIgnoreAllianceChat(!ignoring);
    }
}
