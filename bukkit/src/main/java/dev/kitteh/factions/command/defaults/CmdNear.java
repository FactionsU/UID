package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CmdNear implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = Confs.tl().commands().near();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.NEAR).and(Cloudy.hasFaction())))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        int radius = Confs.main().commands().near().getRadius();
        Set<FPlayer> onlineMembers = faction.membersOnline(true, sender);
        List<FPlayer> nearbyMembers = new ArrayList<>();

        int radiusSquared = radius * radius;
        Location loc = ((Sender.Player) context.sender()).player().getLocation();
        Location cur = new Location(loc.getWorld(), 0, 0, 0);
        for (FPlayer player : onlineMembers) {
            if (player == sender) {
                continue;
            }

            player.asPlayer().getLocation(cur);
            if (cur.getWorld().getUID().equals(loc.getWorld().getUID()) && cur.distanceSquared(loc) <= radiusSquared) {
                nearbyMembers.add(player);
            }
        }

        var tl = Confs.tl().commands().near();
        if (nearbyMembers.isEmpty()) {
            sender.sendRichMessage(tl.getNoneNearby());
            return;
        }

        List<Component> messages = new ArrayList<>();

        while (!nearbyMembers.isEmpty()) {
            TextComponent.Builder builder = Component.text();
            if (messages.isEmpty()) {
                builder.append(Mini.parse(tl.getStartOfLine(), sender));
            }
            for (int x = 0; x < 20 && !nearbyMembers.isEmpty(); x++) {
                FPlayer member = nearbyMembers.removeFirst();
                builder.append(Mini.parse(tl.getPerPlayer(), sender,
                        FPlayerResolver.of("player", member),
                        Placeholder.unparsed("distance", String.valueOf(Math.round(loc.distance(member.asPlayer().getLocation()))))));
                if (x < 19 && !nearbyMembers.isEmpty()) {
                    builder.append(Component.text(", "));
                }
            }
            messages.add(builder.build());
        }

        for (Component message : messages) {
            sender.sendMessage(message);
        }
    }
}
