package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.tag.Tag;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.Location;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class CmdNear implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("near")
                            .commandDescription(Cloudy.desc(TL.COMMAND_NEAR_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.NEAR).and(Cloudy.hasFaction())))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.getFaction();

        int radius = FactionsPlugin.getInstance().conf().commands().near().getRadius();
        Set<FPlayer> onlineMembers = faction.getFPlayersWhereOnline(true, sender);
        List<FPlayer> nearbyMembers = new ArrayList<>();

        int radiusSquared = radius * radius;
        Location loc = ((Sender.Player) context.sender()).player().getLocation();
        Location cur = new Location(loc.getWorld(), 0, 0, 0);
        for (FPlayer player : onlineMembers) {
            if (player == sender) {
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
            playerMessageBuilder.append(parsePlaceholders(sender, member, playerMessage));
        }
        // Append none text if no players where found
        if (playerMessageBuilder.toString().isEmpty()) {
            playerMessageBuilder.append(TL.COMMAND_NEAR_NONE);
        }

        sender.msg(TL.COMMAND_NEAR_PLAYERLIST.toString().replace("{players-nearby}", playerMessageBuilder.toString()));
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
}
