package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FPlayerTeleportEvent;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.LazyLocation;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import dev.kitteh.factions.util.WarmUpUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.parser.standard.StringParser;

import java.util.UUID;
import java.util.function.BiConsumer;

public class CmdWarp implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().warp();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.WARP).and(Cloudy.hasSelfFactionPerms(PermissibleActions.WARP).or(Cloudy.isBypass()))))
                            .optional("warp", StringParser.stringParser())
                            .flag(manager.flagBuilder("password").withComponent(StringParser.stringParser()))
                            .flag(manager.flagBuilder("faction").withComponent(FactionParser.of()))
                            .handler(ctx -> handle(ctx, this::menu))
            );
        };
    }

    public static void handle(CommandContext<Sender> context, BiConsumer<Sender, Faction> consumer) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = context.flags().get("faction") instanceof Faction fac ? fac : sender.faction();
        var tl = FactionsPlugin.instance().tl().commands().warp();

        if (!context.sender().isBypass() && !faction.hasAccess(sender, PermissibleActions.WARP, sender.lastStoodAt())) {
            sender.sendRichMessage(tl.getNoPermission(), FactionResolver.of(sender, faction));
            return;
        }

        String warpName = context.getOrDefault("warp", null);
        if (warpName == null) {
            if (faction.warps().isEmpty()) {
                context.sender().sendRichMessage(tl.getNoWarps(), FactionResolver.of(sender, faction));
            } else {
                consumer.accept(context.sender(), faction);
            }
        } else {
            final String passwordAttempt = context.flags().get("password") instanceof String s ? s : "";

            LazyLocation destination = faction.warp(warpName);
            if (destination != null) {
                if (!sender.adminBypass() && faction.hasWarpPassword(warpName) && !faction.isWarpPassword(warpName, passwordAttempt)) {
                    sender.sendRichMessage(tl.getInvalidPassword());
                    return;
                }

                teleport(sender, faction, warpName, context.sender(), destination);
            } else {
                sender.sendRichMessage(tl.getInvalidWarp(), Placeholder.unparsed("warp", warpName));
            }
        }
    }

    public static void teleport(FPlayer sender, Faction faction, String warpName, Sender commandSender, LazyLocation destination) {
        var tl = FactionsPlugin.instance().tl().commands().warp();

        FPlayerTeleportEvent tpEvent = new FPlayerTeleportEvent(sender, destination.asLocation(), FPlayerTeleportEvent.Reason.WARP);
        Bukkit.getServer().getPluginManager().callEvent(tpEvent);
        if (tpEvent.isCancelled()) {
            return;
        }
        // Check transaction AFTER password check.
        if (!transact(sender, commandSender)) {
            return;
        }
        final FPlayer fPlayer = sender;
        final UUID uuid = sender.uniqueId();

        int delay = FactionsPlugin.instance().conf().commands().warp().getDelay();
        WarmUpUtil.process(sender, WarmUpUtil.Warmup.WARP, Mini.parse(tl.getWarmup(), Placeholder.unparsed("warp", warpName), Placeholder.unparsed("seconds", String.valueOf(delay))), () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (destination == faction.warp(warpName) && player != null) {
                AbstractFactionsPlugin.instance().teleport(player, destination.asLocation()).thenAccept(success -> {
                    if (success) {
                        fPlayer.sendRichMessage(tl.getWarped(), Placeholder.unparsed("warp", warpName));
                    }
                });
            }
        }, delay);
    }

    private void menu(Sender sender, Faction faction) {
        sender.sendRichMessage(String.join(", ", faction.warps().keySet()));
    }

    private static boolean transact(FPlayer player, Sender sender) {
        return player.adminBypass() || sender.payForCommand(FactionsPlugin.instance().conf().economy().getCostWarp(), FactionsPlugin.instance().tl().economy().actions().getWarpTo(), FactionsPlugin.instance().tl().economy().actions().getWarpFor());
    }
}
