package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CmdHelp extends FCommand {

    public CmdHelp() {
        super();
        this.aliases.add("help");
        this.aliases.add("h");

        //this.requiredArgs.add("");
        this.optionalArgs.put("page", "1");

        this.requirements = new CommandRequirements.Builder(Permission.HELP).noDisableOnLock().build();
    }

    @Override
    public void perform(CommandContext context) {
        if (FactionsPlugin.getInstance().conf().commands().help().isUseOldHelp()) {
            if (helpPages == null) {
                updateHelp(context);
            }

            int page = context.argAsInt(0, 1);
            context.sendMessage(plugin.txt().titleize("Factions Help (" + page + "/" + helpPages.size() + ")"));

            page -= 1;

            if (page < 0 || page >= helpPages.size()) {
                context.msg(TL.COMMAND_HELP_404.format(String.valueOf(page)));
                return;
            }
            context.sendMessage(helpPages.get(page));
            return;
        }
        Map<String, List<String>> help = FactionsPlugin.getInstance().conf().commands().help().getEntries();
        String pageArg = context.argAsString(0, "1");
        List<String> page = help.get(pageArg);
        if (page == null || page.isEmpty()) {
            context.msg(TL.COMMAND_HELP_404.format(pageArg));
            return;
        }
        for (String helpLine : page) {
            context.sendMessage(FactionsPlugin.getInstance().txt().parse(helpLine));
        }
    }

    //----------------------------------------------//
    // Build the help pages
    //----------------------------------------------//

    public ArrayList<ArrayList<String>> helpPages;

    public void updateHelp(CommandContext context) {
        helpPages = new ArrayList<>();
        ArrayList<String> pageLines;

        pageLines = new ArrayList<>();
        pageLines.add(FCmdRoot.getInstance().cmdHelp.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdList.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdShow.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdPower.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdJoin.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdLeave.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdChat.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdToggleAllianceChat.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdHome.getUsageTemplate(context, true));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_NEXTCREATE.toString()));
        helpPages.add(pageLines);

        pageLines = new ArrayList<>();
        pageLines.add(FCmdRoot.getInstance().cmdCreate.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdDescription.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdTag.getUsageTemplate(context, true));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_INVITATIONS.toString()));
        pageLines.add(FCmdRoot.getInstance().cmdOpen.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdInvite.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdDeinvite.getUsageTemplate(context, true));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_HOME.toString()));
        pageLines.add(FCmdRoot.getInstance().cmdSethome.getUsageTemplate(context, true));
        helpPages.add(pageLines);

        if (Econ.isSetup() && FactionsPlugin.getInstance().conf().economy().isEnabled() && FactionsPlugin.getInstance().conf().economy().isBankEnabled()) {
            pageLines = new ArrayList<>();
            pageLines.add("");
            pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_BANK_1.toString()));
            pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_BANK_2.toString()));
            pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_BANK_3.toString()));
            pageLines.add("");
            pageLines.add(FCmdRoot.getInstance().cmdMoney.getUsageTemplate(context, true));
            pageLines.add("");
            pageLines.add("");
            pageLines.add("");
            helpPages.add(pageLines);
        }

        pageLines = new ArrayList<>();
        pageLines.add(FCmdRoot.getInstance().cmdClaim.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdAutoClaim.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdUnclaim.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdUnclaimall.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdKick.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdMod.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdAdmin.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdTitle.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdSB.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdSeeChunk.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdStatus.getUsageTemplate(context, true));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_PLAYERTITLES.toString()));
        helpPages.add(pageLines);

        pageLines = new ArrayList<>();
        pageLines.add(FCmdRoot.getInstance().cmdMap.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdBoom.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdOwner.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdOwnerList.getUsageTemplate(context, true));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_OWNERSHIP_1.toString()));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_OWNERSHIP_2.toString()));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_OWNERSHIP_3.toString()));
        helpPages.add(pageLines);

        pageLines = new ArrayList<>();
        pageLines.add(FCmdRoot.getInstance().cmdDisband.getUsageTemplate(context, true));
        pageLines.add("");
        pageLines.add(FCmdRoot.getInstance().cmdRelationAlly.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdRelationNeutral.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdRelationEnemy.getUsageTemplate(context, true));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_RELATIONS_1.toString()));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_RELATIONS_2.toString()));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_RELATIONS_3.toString()));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_RELATIONS_4.toString()));
        helpPages.add(pageLines);

        pageLines = new ArrayList<>();
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_RELATIONS_5.toString()));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_RELATIONS_6.toString()));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_RELATIONS_7.toString()));
        pageLines.add(TL.COMMAND_HELP_RELATIONS_8.toString());
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_RELATIONS_9.toString()));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_RELATIONS_10.toString()));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_RELATIONS_11.toString()));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_RELATIONS_12.toString()));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_RELATIONS_13.toString()));
        helpPages.add(pageLines);

        pageLines = new ArrayList<>();
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_PERMISSIONS_1.toString()));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_PERMISSIONS_2.toString()));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_PERMISSIONS_3.toString()));
        pageLines.add(TL.COMMAND_HELP_PERMISSIONS_4.toString());
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_PERMISSIONS_5.toString()));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_PERMISSIONS_6.toString()));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_PERMISSIONS_7.toString()));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_PERMISSIONS_8.toString()));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_PERMISSIONS_9.toString()));
        helpPages.add(pageLines);

        pageLines = new ArrayList<>();
        pageLines.add(TL.COMMAND_HELP_MOAR_1.toString());
        pageLines.add(FCmdRoot.getInstance().cmdBypass.getUsageTemplate(context, true));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_ADMIN_1.toString()));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_ADMIN_2.toString()));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_ADMIN_3.toString()));
        pageLines.add(FCmdRoot.getInstance().cmdSafeunclaimall.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdWarunclaimall.getUsageTemplate(context, true));
        //TODO:TL
        pageLines.add(plugin.txt().parse("<i>Note: " + FCmdRoot.getInstance().cmdUnclaim.getUsageTemplate(context, false) + FactionsPlugin.getInstance().txt().parse("<i>") + " works on safe/war zones as well."));
        pageLines.add(FCmdRoot.getInstance().cmdPeaceful.getUsageTemplate(context, true));
        helpPages.add(pageLines);

        pageLines = new ArrayList<>();
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_MOAR_2.toString()));
        pageLines.add(FCmdRoot.getInstance().cmdChatSpy.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdPermanent.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdPermanentPower.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdPowerBoost.getUsageTemplate(context, true));
        helpPages.add(pageLines);

        pageLines = new ArrayList<>();
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_MOAR_3.toString()));
        pageLines.add(FCmdRoot.getInstance().cmdLock.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdReload.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdSaveAll.getUsageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdVersion.getUsageTemplate(context, true));
        helpPages.add(pageLines);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_HELP_DESCRIPTION;
    }
}

