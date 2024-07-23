package com.massivecraft.factions.cmd.claim;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.CommandRequirements;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class CmdWarunclaimall extends FCommand {

    public CmdWarunclaimall() {
        this.aliases.add("warunclaimall");
        this.aliases.add("wardeclaimall");

        //this.requiredArgs.add("");
        this.optionalArgs.put("world", "all");

        this.requirements = new CommandRequirements.Builder(Permission.MANAGE_WAR_ZONE).build();
    }

    @Override
    public void perform(CommandContext context) {
        String worldName = context.argAsString(0);
        World world = null;

        if (worldName != null) {
            world = Bukkit.getWorld(worldName);
        }

        if (world == null) {
            Board.getInstance().unclaimAll(Factions.getInstance().getWarZone());
        } else {
            Board.getInstance().unclaimAllInWorld(Factions.getInstance().getWarZone(), world);
        }

        if (FactionsPlugin.getInstance().conf().logging().isLandUnclaims()) {
            FactionsPlugin.getInstance().log(TL.COMMAND_WARUNCLAIMALL_LOG.format(context.fPlayer.getName()));
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_WARUNCLAIMALL_DESCRIPTION;
    }

}
