package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.ChatColor;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdInvite implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = Confs.tl().commands().invite();
            Command.Builder<Sender> build = builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .commandDescription(Cloudy.desc(tl.getDescription()))
                    .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.INVITE).and(Cloudy.hasSelfFactionPerms(PermissibleActions.INVITE))));

            manager.command(
                    build.required("player", FPlayerParser.of(FPlayerParser.Include.NO_FACTION, FPlayerParser.Include.OTHER_FACTION))
                            .flag(manager.flagBuilder("delete"))
                            .handler(this::handle)
            );
            manager.command(build.meta(HIDE_IN_HELP, true).handler(ctx -> help.queryCommands(Cmd.rootCommand() + " " + tl.getFirstAlias() + " <player> [--delete]", ctx.sender())));
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = Confs.tl().commands().invite();
        var econTl = Confs.tl().economy().actions();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        FPlayer target = context.get("player");

        if (target.faction() == faction) {
            sender.sendRichMessage(tl.getAlreadyMember(),
                    FPlayerResolver.of("player", target),
                    FactionResolver.of(faction));
            return;
        }

        if (context.flags().hasFlag("delete")) {
            faction.deInvite(target);
            target.sendRichMessage(tl.getDeinviteRevoked(),
                    FPlayerResolver.of("player", sender),
                    FactionResolver.of(faction));
            faction.sendRichMessage(tl.getDeinviteRevokes(),
                    FPlayerResolver.of("player", sender),
                    Placeholder.unparsed("target", target.name()));
            return;
        }

        if (!context.sender().payForCommand(Confs.main().economy().getCostInvite(), econTl.getInviteTo(), econTl.getInviteFor())) {
            return;
        }

        if (faction.isBanned(target)) {
            sender.sendRichMessage(tl.getBanned(), FPlayerResolver.of("player", target));
            return;
        }

        faction.invite(target);
        if (target.isOnline()) {
            Component senderComponent = Mini.parse("<player>", target, FPlayerResolver.of("player", sender));
            Component factionComponent = Mini.parse("<faction>", target, FactionResolver.of(faction));
            Component component = senderComponent
                    .append(Mini.parse("<yellow>" + tl.getInvitedYou(), target))
                    .append(factionComponent);

            component = component
                    .hoverEvent(Mini.parse(tl.getClickToJoin(), target).asHoverEvent())
                    .clickEvent(ClickEvent.runCommand("/" + Cmd.rootCommand() + " join " + ChatColor.stripColor(faction.tag())));
            target.sendMessage(component);
        }

        faction.sendRichMessage(tl.getInvited(),
                FPlayerResolver.of("player", sender),
                Placeholder.unparsed("target", target.name()));
    }
}
