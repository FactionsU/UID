package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FPlayerJoinEvent;
import dev.kitteh.factions.event.FactionAttemptCreateEvent;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.util.MiscUtil;
import dev.kitteh.factions.util.Permission;
import org.bukkit.Bukkit;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.StringParser;

import java.util.List;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdCreate implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().create();
            Command.Builder<Sender> build = builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .commandDescription(Cloudy.desc(tl.getDescription()))
                    .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.CREATE).and(Cloudy.isPlayer())));

            manager.command(
                    build.required("tag", StringParser.stringParser())
                            .handler(this::handle)
            );

            manager.command(build.meta(HIDE_IN_HELP, true).handler(ctx -> help.queryCommands(Cmd.rootCommand() + " " + tl.getFirstAlias() + " <tag>", ctx.sender())));
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().create();
        var econTl = FactionsPlugin.instance().tl().economy().actions();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        String tag = context.get("tag");

        if (sender.hasFaction()) {
            sender.sendRichMessage(tl.getMustLeave());
            return;
        }

        if (Factions.factions().get(tag) != null) {
            sender.sendRichMessage(tl.getInUse());
            return;
        }

        List<String> tagValidationErrors = MiscUtil.validateTag(tag);
        if (!tagValidationErrors.isEmpty()) {
            sender.sendMessageLegacy(tagValidationErrors);
            return;
        }

        if (!context.sender().canAffordCommand(FactionsPlugin.instance().conf().economy().getCostCreate(), econTl.getCreateTo())) {
            return;
        }

        FactionAttemptCreateEvent attemptEvent = new FactionAttemptCreateEvent(sender, tag);
        Bukkit.getServer().getPluginManager().callEvent(attemptEvent);
        if (attemptEvent.isCancelled()) {
            return;
        }

        if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostCreate(), econTl.getCreateTo(), econTl.getCreateFor())) {
            return;
        }

        Faction faction = Factions.factions().create(sender, tag);

        FPlayerJoinEvent joinEvent = new FPlayerJoinEvent(sender, faction, FPlayerJoinEvent.Reason.CREATE);
        Bukkit.getServer().getPluginManager().callEvent(joinEvent);

        sender.role(Role.ADMIN);
        sender.faction(faction);
        sender.asPlayer().updateCommands();

        for (FPlayer follower : FPlayers.fPlayers().online()) {
            follower.sendRichMessage(tl.getCreated(),
                    FPlayerResolver.of("player", sender),
                    FactionResolver.of(faction));
        }

        if (FactionsPlugin.instance().conf().logging().isFactionCreate()) {
            AbstractFactionsPlugin.instance().log(sender.name() + " created faction " + tag);
        }
    }
}
