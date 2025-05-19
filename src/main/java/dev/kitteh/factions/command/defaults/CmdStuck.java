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
import dev.kitteh.factions.integration.Essentials;
import dev.kitteh.factions.integration.IntegrationManager;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.SpiralTask;
import dev.kitteh.factions.util.TL;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdStuck implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> manager.command(
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
        // TODO handle delay 0
        final long delay = Math.max(1, FactionsPlugin.instance().conf().commands().stuck().getDelay());
        final int radius = FactionsPlugin.instance().conf().commands().stuck().getRadius();
        final int searchRadius = FactionsPlugin.instance().conf().commands().stuck().getSearchRadius();

        if (FactionsPlugin.instance().stuckMap().containsKey(player.getUniqueId())) {
            long wait = FactionsPlugin.instance().timers().get(player.getUniqueId()) - System.currentTimeMillis();
            String time = DurationFormatUtils.formatDuration(wait, TL.COMMAND_STUCK_TIMEFORMAT.toString(), true);
            sender.msg(TL.COMMAND_STUCK_EXISTS, time);
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

            final int id = new BukkitRunnable() {

                @Override
                public void run() {
                    if (!FactionsPlugin.instance().stuckMap().containsKey(player.getUniqueId())) {
                        return;
                    }

                    // check for world difference or radius exceeding
                    final World world = chunk.world();
                    if (world.getUID() != player.getWorld().getUID() || sentAt.distance(player.getLocation()) > radius) {
                        sender.msg(TL.COMMAND_STUCK_OUTSIDE.format(radius));
                        FactionsPlugin.instance().timers().remove(player.getUniqueId());
                        FactionsPlugin.instance().stuckMap().remove(player.getUniqueId());
                        return;
                    }

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
                                sender.msg(TL.COMMAND_STUCK_TELEPORT, tp.getBlockX(), tp.getBlockY(), tp.getBlockZ());
                                FactionsPlugin.instance().timers().remove(player.getUniqueId());
                                FactionsPlugin.instance().stuckMap().remove(player.getUniqueId());
                                if (FactionsPlugin.instance().integrationManager().isEnabled(IntegrationManager.Integration.ESS) && !Essentials.handleTeleport(player, tp)) {
                                    AbstractFactionsPlugin.getInstance().teleport(player, tp);
                                    FactionsPlugin.instance().debug("/f stuck used regular teleport, not essentials!");
                                }
                                this.stop();
                                return false;
                            }
                            return true;
                        }

                        @Override
                        public void finish() {
                            sender.msg(TL.COMMAND_STUCK_FAILED);
                        }
                    };
                }
            }.runTaskLater(AbstractFactionsPlugin.getInstance(), delay * 20).getTaskId();

            FactionsPlugin.instance().timers().put(player.getUniqueId(), System.currentTimeMillis() + (delay * 1000));
            long wait = FactionsPlugin.instance().timers().get(player.getUniqueId()) - System.currentTimeMillis();
            String time = DurationFormatUtils.formatDuration(wait, TL.COMMAND_STUCK_TIMEFORMAT.toString(), true);
            sender.msg(TL.COMMAND_STUCK_START, time);
            FactionsPlugin.instance().stuckMap().put(player.getUniqueId(), id);
        }
    }
}
