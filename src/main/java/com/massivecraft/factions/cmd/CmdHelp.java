package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;


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
        if (FactionsPlugin.getInstance().getConfig().getBoolean("use-old-help", true)) {
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
        ConfigurationSection help = FactionsPlugin.getInstance().getConfig().getConfigurationSection("help");
        if (help == null) {
            help = FactionsPlugin.getInstance().getConfig().createSection("help"); // create new help section
            List<String> error = new ArrayList<>();
            error.add("&cUpdate help messages in config.yml!");
            error.add("&cSet use-old-help for legacy help messages");
            help.set("'1'", error); // add default error messages
        }
        String pageArg = context.argAsString(0, "1");
        List<String> page = help.getStringList(pageArg);
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
        pageLines.add(FCmdRoot.getInstance().cmdHelp.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdList.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdShow.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdPower.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdJoin.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdLeave.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdChat.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdToggleAllianceChat.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdHome.getUseageTemplate(context, true));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_NEXTCREATE.toString()));
        helpPages.add(pageLines);

        pageLines = new ArrayList<>();
        pageLines.add(FCmdRoot.getInstance().cmdCreate.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdDescription.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdTag.getUseageTemplate(context, true));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_INVITATIONS.toString()));
        pageLines.add(FCmdRoot.getInstance().cmdOpen.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdInvite.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdDeinvite.getUseageTemplate(context, true));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_HOME.toString()));
        pageLines.add(FCmdRoot.getInstance().cmdSethome.getUseageTemplate(context, true));
        helpPages.add(pageLines);

        if (Econ.isSetup() && FactionsPlugin.getInstance().conf().economy().isEnabled() && FactionsPlugin.getInstance().conf().economy().isBankEnabled()) {
            pageLines = new ArrayList<>();
            pageLines.add("");
            pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_BANK_1.toString()));
            pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_BANK_2.toString()));
            pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_BANK_3.toString()));
            pageLines.add("");
            pageLines.add(FCmdRoot.getInstance().cmdMoney.getUseageTemplate(context, true));
            pageLines.add("");
            pageLines.add("");
            pageLines.add("");
            helpPages.add(pageLines);
        }

        pageLines = new ArrayList<>();
        pageLines.add(FCmdRoot.getInstance().cmdClaim.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdAutoClaim.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdUnclaim.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdUnclaimall.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdKick.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdMod.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdAdmin.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdTitle.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdSB.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdSeeChunk.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdStatus.getUseageTemplate(context, true));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_PLAYERTITLES.toString()));
        helpPages.add(pageLines);

        pageLines = new ArrayList<>();
        pageLines.add(FCmdRoot.getInstance().cmdMap.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdBoom.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdOwner.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdOwnerList.getUseageTemplate(context, true));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_OWNERSHIP_1.toString()));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_OWNERSHIP_2.toString()));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_OWNERSHIP_3.toString()));
        helpPages.add(pageLines);

        pageLines = new ArrayList<>();
        pageLines.add(FCmdRoot.getInstance().cmdDisband.getUseageTemplate(context, true));
        pageLines.add("");
        pageLines.add(FCmdRoot.getInstance().cmdRelationAlly.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdRelationNeutral.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdRelationEnemy.getUseageTemplate(context, true));
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
        pageLines.add(FCmdRoot.getInstance().cmdBypass.getUseageTemplate(context, true));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_ADMIN_1.toString()));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_ADMIN_2.toString()));
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_ADMIN_3.toString()));
        pageLines.add(FCmdRoot.getInstance().cmdSafeunclaimall.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdWarunclaimall.getUseageTemplate(context, true));
        //TODO:TL
        pageLines.add(plugin.txt().parse("<i>Note: " + FCmdRoot.getInstance().cmdUnclaim.getUseageTemplate(context, false) + FactionsPlugin.getInstance().txt().parse("<i>") + " works on safe/war zones as well."));
        pageLines.add(FCmdRoot.getInstance().cmdPeaceful.getUseageTemplate(context, true));
        helpPages.add(pageLines);

        pageLines = new ArrayList<>();
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_MOAR_2.toString()));
        pageLines.add(FCmdRoot.getInstance().cmdChatSpy.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdPermanent.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdPermanentPower.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdPowerBoost.getUseageTemplate(context, true));
        helpPages.add(pageLines);

        pageLines = new ArrayList<>();
        pageLines.add(plugin.txt().parse(TL.COMMAND_HELP_MOAR_3.toString()));
        pageLines.add(FCmdRoot.getInstance().cmdLock.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdReload.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdSaveAll.getUseageTemplate(context, true));
        pageLines.add(FCmdRoot.getInstance().cmdVersion.getUseageTemplate(context, true));
        helpPages.add(pageLines);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_HELP_DESCRIPTION;
    }
}

