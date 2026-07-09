package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FPlayerTeleportEvent;
import dev.kitteh.factions.integration.ExternalChecks;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.SpiralTask;
import dev.kitteh.factions.util.WarmUpUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdStuck implements Cmd {
    private final Set<UUID> waiting = new HashSet<>();

    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        var tl = FactionsPlugin.instance().tl().commands().stuck();
        return (manager, builder, _) -> manager.command(
                builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                        .commandDescription(Cloudy.desc(tl.getDescription()))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.STUCK).and(Cloudy.isPlayer())))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().stuck();
        var econTl = FactionsPlugin.instance().tl().economy().actions();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();

        final Location sentAt = player.getLocation();
        final FLocation chunk = new FLocation(sentAt);
        final long delay = Math.max(1, FactionsPlugin.instance().conf().commands().stuck().getDelay());
        final int radius = FactionsPlugin.instance().conf().commands().stuck().getRadius();
        final int searchRadius = FactionsPlugin.instance().conf().commands().stuck().getSearchRadius();

        if (waiting.contains(sender.uniqueId()) || (sender.warmup() instanceof WarmUpUtil.Warmup warmup && warmup == WarmUpUtil.Warmup.STUCK)) {
            sender.sendRichMessage(tl.getAlreadyExists());
        } else {
            FPlayerTeleportEvent tpEvent = new FPlayerTeleportEvent(sender, null, FPlayerTeleportEvent.Reason.STUCK);
            Bukkit.getServer().getPluginManager().callEvent(tpEvent);
            if (tpEvent.isCancelled()) {
                return;
            }

            if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostStuck(), econTl.getStuckTo(), econTl.getStuckFor())) {
                return;
            }

            WarmUpUtil.process(sender, WarmUpUtil.Warmup.STUCK,
                    Mini.parse(tl.getWarmup(), sender, Placeholder.unparsed("seconds", String.valueOf(delay))),
                    () -> {
                        final World world = chunk.world();
                        if (!world.getUID().equals(player.getWorld().getUID()) || sentAt.distance(player.getLocation()) > radius) {
                            sender.sendRichMessage(tl.getOutside(), Placeholder.unparsed("range", String.valueOf(radius)));
                            return;
                        }

                        waiting.add(sender.uniqueId());

                        final Board board = Board.board();
                        new SpiralTask(new FLocation(player), searchRadius) {
                            final int buffer = FactionsPlugin.instance().conf().worldBorder().getBuffer();

                            @Override
                            public boolean work() {
                                FLocation chunk = currentFLocation();
                                if (chunk.isOutsideWorldBorder(buffer)) {
                                    return true;
                                }

                                Faction faction = board.factionAt(chunk);
                                if (faction.isWilderness()) {
                                    int cx = FLocation.chunkToBlock(chunk.x());
                                    int cz = FLocation.chunkToBlock(chunk.z());
                                    int y = world.getHighestBlockYAt(cx, cz);
                                    Location tp = new Location(world, cx, y, cz);
                                    sender.sendRichMessage(tl.getTeleport(),
                                            Placeholder.unparsed("x", String.valueOf(tp.getBlockX())),
                                            Placeholder.unparsed("y", String.valueOf(tp.getBlockY())),
                                            Placeholder.unparsed("z", String.valueOf(tp.getBlockZ())));
                                    if (!ExternalChecks.tryTeleport(player, tp)) {
                                        AbstractFactionsPlugin.instance().teleport(player, tp);
                                        AbstractFactionsPlugin.instance().debug("/f stuck used regular teleport, not essentials!");
                                    }
                                    this.stop();
                                    waiting.remove(sender.uniqueId());
                                    return false;
                                }
                                return true;
                            }

                            @Override
                            public void finish() {
                                waiting.remove(sender.uniqueId());
                                sender.sendRichMessage(tl.getFailed());
                            }
                        };
                    }, delay);
        }
    }
}
