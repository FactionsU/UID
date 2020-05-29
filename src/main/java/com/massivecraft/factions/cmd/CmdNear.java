package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.tag.Tag;
import com.massivecraft.factions.util.TL;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CmdNear extends FCommand {

    public CmdNear() {
        super();
        this.aliases.add("near");

        this.requirements = new CommandRequirements.Builder(Permission.NEAR)
                .memberOnly()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        int radius = FactionsPlugin.getInstance().conf().commands().near().getRadius();
        Set<FPlayer> onlineMembers = context.faction.getFPlayersWhereOnline(true, context.fPlayer);
        List<FPlayer> nearbyMembers = new ArrayList<>();

        int radiusSquared = radius * radius;
        Location loc = context.player.getLocation();
        Location cur = new Location(loc.getWorld(), 0, 0, 0);
        for (FPlayer player : onlineMembers) {
            if (player == context.fPlayer) {
                continue;
            }

            player.getPlayer().getLocation(cur);
            if (cur.getWorld().getUID().equals(loc.getWorld().getUID()) && cur.distanceSquared(loc) <= radiusSquared) {
                nearbyMembers.add(player);
            }
        }

        StringBuilder playerMessageBuilder = new StringBuilder();
        String playerMessage = TL.COMMAND_NEAR_PLAYER.toString();
        for (FPlayer member : nearbyMembers) {
            playerMessageBuilder.append(parsePlaceholders(context.fPlayer, member, playerMessage));
        }
        // Append none text if no players where found
        if (playerMessageBuilder.toString().isEmpty()) {
            playerMessageBuilder.append(TL.COMMAND_NEAR_NONE);
        }

        context.msg(TL.COMMAND_NEAR_PLAYERLIST.toString().replace("{players-nearby}", playerMessageBuilder.toString()));
    }

    private String parsePlaceholders(FPlayer user, FPlayer target, String string) {
        string = Tag.parsePlain(target, string);
        string = Tag.parsePlaceholders(target.getPlayer(), string);
        string = string.replace("{role}", target.getRole().toString());
        string = string.replace("{role-prefix}", target.getRole().getPrefix());
        // Only run distance calculation if needed
        if (string.contains("{distance}")) {
            double distance = Math.round(user.getPlayer().getLocation().distance(target.getPlayer().getLocation()));
            string = string.replace("{distance}", distance + "");
        }
        return string;
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_NEAR_DESCRIPTION;
    }

}
