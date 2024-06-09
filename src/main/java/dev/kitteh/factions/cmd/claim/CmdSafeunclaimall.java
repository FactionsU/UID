package dev.kitteh.factions.cmd.claim;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.cmd.CommandContext;
import dev.kitteh.factions.cmd.CommandRequirements;
import dev.kitteh.factions.cmd.FCommand;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class CmdSafeunclaimall extends FCommand {

    public CmdSafeunclaimall() {
        this.aliases.add("safeunclaimall");
        this.aliases.add("safedeclaimall");

        this.optionalArgs.put("world", "all");

        this.requirements = new CommandRequirements.Builder(Permission.MANAGE_SAFE_ZONE).build();
    }

    @Override
    public void perform(CommandContext context) {
        String worldName = context.argAsString(0);
        World world = null;

        if (worldName != null) {
            world = Bukkit.getWorld(worldName);
        }

        if (world == null) {
            Board.getInstance().unclaimAll(Factions.getInstance().getSafeZone());
        } else {
            Board.getInstance().unclaimAllInWorld(Factions.getInstance().getSafeZone(), world);
        }

        context.msg(TL.COMMAND_SAFEUNCLAIMALL_UNCLAIMED);

        if (FactionsPlugin.getInstance().conf().logging().isLandUnclaims()) {
            FactionsPlugin.getInstance().log(TL.COMMAND_SAFEUNCLAIMALL_UNCLAIMEDLOG.format(context.sender.getName()));
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_SAFEUNCLAIMALL_DESCRIPTION;
    }

}
