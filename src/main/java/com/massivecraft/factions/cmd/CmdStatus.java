package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.landraidcontrol.PowerControl;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;

import java.util.ArrayList;

public class CmdStatus extends FCommand {

    public CmdStatus() {
        super();
        this.aliases.add("status");
        this.aliases.add("s");

        this.requirements = new CommandRequirements.Builder(Permission.STATUS)
                .memberOnly()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        ArrayList<String> ret = new ArrayList<>();
        for (FPlayer fp : context.faction.getFPlayers()) {
            String humanized = DurationFormatUtils.formatDurationWords(System.currentTimeMillis() - fp.getLastLoginTime(), true, true) + TL.COMMAND_STATUS_AGOSUFFIX;
            String last = fp.isOnline() ? ChatColor.GREEN + TL.COMMAND_STATUS_ONLINE.toString() : (System.currentTimeMillis() - fp.getLastLoginTime() < 432000000 ? ChatColor.YELLOW + humanized : ChatColor.RED + humanized);
            String power = "";
            if (FactionsPlugin.getInstance().getLandRaidControl() instanceof PowerControl) {
                power = ChatColor.YELLOW + String.valueOf(fp.getPowerRounded()) + " / " + fp.getPowerMaxRounded() + ChatColor.RESET;
            } else {
                power = "n/a";
            }
            ret.add(String.format(TL.COMMAND_STATUS_FORMAT.toString(), ChatColor.GOLD + fp.getRole().getPrefix() + fp.getName() + ChatColor.RESET, power, last).trim());
        }
        context.fPlayer.sendMessage(ret);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_STATUS_DESCRIPTION;
    }

}
