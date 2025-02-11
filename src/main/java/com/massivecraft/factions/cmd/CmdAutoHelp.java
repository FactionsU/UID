package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.util.TL;

import java.util.ArrayList;

public class CmdAutoHelp extends FCommand {

    public CmdAutoHelp() {
        this.aliases.add("?");
        this.aliases.add("h");
        this.aliases.add("help");

        this.setHelpShort("");

        this.optionalArgs.put("page", "1");
    }

    @Override
    public void perform(CommandContext context) {
        if (context.commandChain.isEmpty()) {
            return;
        }
        FCommand pcmd = context.commandChain.getLast();

        ArrayList<String> lines = new ArrayList<>(pcmd.helpLong);

        for (FCommand scmd : pcmd.subCommands) {
            if (scmd.visibility == CommandVisibility.VISIBLE) {
                lines.add(scmd.getUsageTemplate(context, true));
            }
            // TODO deal with other visibilities
        }

        context.sendMessage(FactionsPlugin.getInstance().txt().getPage(lines, context.argAsInt(0, 1), TL.COMMAND_AUTOHELP_HELPFOR + pcmd.aliases.getFirst() + "\""));
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_HELP_DESCRIPTION;
    }
}
