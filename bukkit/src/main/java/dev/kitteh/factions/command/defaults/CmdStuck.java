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
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.SpiralTask;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.WarmUpUtil;
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
        return (manager, builder, help) -> manager.command(
                builder.literal("stuck")
                        .commandDescription(Cloudy.desc(TL.COMMAND_STUCK_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.STUCK).and(Cloudy.isPlayer())))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();

        final Location sentAt = player.getLocation();
        final FLocation chunk = new FLocation(sentAt);
        final long delay = Math.max(1, FactionsPlugin.instance().conf().commands().stuck().getDelay());
        final int radius = FactionsPlugin.instance().conf().commands().stuck().getRadius();
        final int searchRadius = FactionsPlugin.instance().conf().commands().stuck().getSearchRadius();

        if (waiting.contains(sender.uniqueId()) || (sender.warmup() instanceof WarmUpUtil.Warmup warmup && warmup == WarmUpUtil.Warmup.STUCK)) {
            sender.msgLegacy(TL.COMMAND_STUCK_ALREADYEXISTS);
        } else {
            FPlayerTeleportEvent tpEvent = new FPlayerTeleportEvent(sender, null, FPlayerTeleportEvent.Reason.STUCK);
            Bukkit.getServer().getPluginManager().callEvent(tpEvent);
            if (tpEvent.isCancelled()) {
                return;
            }

            // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
            if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostStuck(), TL.COMMAND_STUCK_TOSTUCK2, TL.COMMAND_STUCK_FORSTUCK2)) {
                return;
            }

            WarmUpUtil.process(sender, WarmUpUtil.Warmup.STUCK, TL.WARMUPS_NOTIFY_STUCK.format(delay), () -> {
                // check for world difference or radius exceeding
                final World world = chunk.world();
                if (world.getUID() != player.getWorld().getUID() || sentAt.distance(player.getLocation()) > radius) {
                    sender.msgLegacy(TL.COMMAND_STUCK_OUTSIDE, radius);
                    return;
                }

                waiting.add(sender.uniqueId());

                final Board board = Board.board();
                // spiral task to find nearest wilderness chunk
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
                            sender.msgLegacy(TL.COMMAND_STUCK_TELEPORT, tp.getBlockX(), tp.getBlockY(), tp.getBlockZ());
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
                        sender.msgLegacy(TL.COMMAND_STUCK_FAILED);
                    }
                };
            }, delay);
        }
    }
}
