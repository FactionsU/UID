package dev.kitteh.factions.command.defaults.admin.force;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FPlayerJoinEvent;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdForceJoin implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().admin().force().join();
            Command.Builder<Sender> build = builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .commandDescription(Cloudy.desc(tl.getDescription()))
                    .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.FORCE_JOIN)));

            manager.command(
                    build
                            .required("player", FPlayerParser.of(FPlayerParser.Include.ONLINE, FPlayerParser.Include.ALL))
                            .required("faction", FactionParser.of())
                            .handler(this::handle)
            );

            manager.command(build.meta(HIDE_IN_HELP, true).handler(ctx -> help.queryCommands("fa force join <player> <faction>", ctx.sender())));
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().admin().force().join();
        Sender sender = context.sender();
        FPlayer target = context.get("player");
        Faction faction = context.get("faction");

        if (!faction.isNormal()) {
            sender.sendRichMessage(tl.getDeniedSpecial());
            return;
        }

        if (target.hasFaction()) {
            sender.sendRichMessage(tl.getDeniedAlreadyHasFaction());
            return;
        }

        // trigger the join event (cancellable)
        FPlayerJoinEvent joinEvent = new FPlayerJoinEvent(target, faction, FPlayerJoinEvent.Reason.COMMAND_FORCE);
        Bukkit.getServer().getPluginManager().callEvent(joinEvent);
        if (joinEvent.isCancelled()) {
            return;
        }

        target.sendRichMessage(tl.getSuccessNoticePlayer(), FactionResolver.of(target, faction));
        sender.sendRichMessage(tl.getSuccess(), FactionResolver.of(sender.fPlayerOrNull(), faction), FPlayerResolver.of("player", sender.fPlayerOrNull(), target));
        faction.membersOnline(true).forEach(fp -> fp.sendRichMessage(tl.getSuccessNotice(), FactionResolver.of(fp, faction), FPlayerResolver.of("player", fp, target)));

        target.resetFactionData();
        target.faction(faction);
        faction.deInvite(target);
        target.role(faction.defaultRole());
        if (target.asPlayer() instanceof Player p) {
            p.updateCommands();
        }

        if (FactionsPlugin.instance().conf().logging().isFactionJoin()) {
            AbstractFactionsPlugin.instance().log(target.name() + " force-joined the faction " + faction.tag());
        }
    }
}
