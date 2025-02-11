package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.perms.Role;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CmdAnnounce extends FCommand {

    public CmdAnnounce() {
        super();
        this.aliases.add("ann");
        this.aliases.add("announce");

        this.requiredArgs.add("message");

        this.requirements = new CommandRequirements.Builder(Permission.ANNOUNCE).memberOnly()
                .withRole(Role.MODERATOR)
                .noErrorOnManyArgs()
                .noDisableOnLock()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        String prefix = ChatColor.GREEN + context.faction.getTag() + ChatColor.YELLOW + " [" + ChatColor.GRAY + context.player.getName() + ChatColor.YELLOW + "] " + ChatColor.RESET;
        String message = StringUtils.join(context.args, " ");

        for (Player player : context.faction.getOnlinePlayers()) {
            player.sendMessage(prefix + message);
        }

        // Add for offline players.
        for (FPlayer fp : context.faction.getFPlayersWhereOnline(false)) {
            context.faction.addAnnouncement(fp, prefix + message);
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_ANNOUNCE_DESCRIPTION;
    }

}
