package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FPlayerLeaveEvent;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.Permission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdKick implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = FactionsPlugin.instance().tl().commands().kick();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.KICK).and(Cloudy.hasSelfFactionPerms(PermissibleActions.KICK))))
                            .optional("target", FPlayerParser.of(FPlayerParser.Include.SAME_FACTION))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().kick();
        var econTl = FactionsPlugin.instance().tl().economy().actions();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        FPlayer toKick = context.getOrDefault("target", null);

        if (toKick == null) {
            Component component = Mini.parse(tl.getCandidates(), sender);
            for (FPlayer player : faction.members(Role.RECRUIT)) {
                String s = player.name();
                component = component.append(Component.text().color(NamedTextColor.WHITE).content(s + " ")
                        .hoverEvent(Mini.parse(tl.getClickToKick(), sender, FPlayerResolver.of("player", player)).asHoverEvent())
                        .clickEvent(ClickEvent.runCommand("/" + Cmd.rootCommand() + " kick " + s))
                );
            }
            for (FPlayer player : faction.members(Role.NORMAL)) {
                String s = player.name();
                component = component.append(Component.text().color(NamedTextColor.WHITE).content(s + " ")
                        .hoverEvent(Mini.parse(tl.getClickToKick(), sender, FPlayerResolver.of("player", player)).asHoverEvent())
                        .clickEvent(ClickEvent.runCommand("/" + Cmd.rootCommand() + " kick " + s))
                );
            }
            if (sender.role().isAtLeast(Role.COLEADER)) {
                for (FPlayer player : faction.members(Role.MODERATOR)) {
                    String s = player.name();
                    component = component.append(Component.text().color(NamedTextColor.GRAY).content(s + " ")
                            .hoverEvent(Mini.parse(tl.getClickToKick(), sender, FPlayerResolver.of("player", player)).asHoverEvent())
                            .clickEvent(ClickEvent.runCommand("/" + Cmd.rootCommand() + " kick " + s))
                    );
                }
                if (sender.role() == Role.ADMIN) {
                    for (FPlayer player : faction.members(Role.COLEADER)) {
                        String s = player.name();
                        component = component.append(Component.text().color(NamedTextColor.RED).content(s + " ")
                                .hoverEvent(Mini.parse(tl.getClickToKick(), sender, FPlayerResolver.of("player", player)).asHoverEvent())
                                .clickEvent(ClickEvent.runCommand("/" + Cmd.rootCommand() + " kick " + s))
                        );
                    }
                }
            }

            context.sender().sendMessage(component);
            return;
        }

        if (sender == toKick) {
            sender.sendRichMessage(tl.getSelf());
            return;
        }

        Faction toKickFaction = toKick.faction();

        if (toKickFaction.isWilderness()) {
            sender.sendRichMessage(tl.getNone());
            return;
        }

        if (toKickFaction != faction) {
            sender.sendRichMessage(tl.getNotMember(),
                    FPlayerResolver.of("player", toKick),
                    FactionResolver.of(faction));
            return;
        }

        if (toKick.role().isAtLeast(sender.role())) {
            sender.sendRichMessage(tl.getInsufficientRank());
            return;
        }

        if (!FactionsPlugin.instance().landRaidControl().canKick(toKick, sender)) {
            return;
        }

        if (!context.sender().canAffordCommand(FactionsPlugin.instance().conf().economy().getCostKick(), econTl.getKickTo())) {
            return;
        }

        FPlayerLeaveEvent event = new FPlayerLeaveEvent(toKick, toKick.faction(), FPlayerLeaveEvent.Reason.KICKED);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostKick(), econTl.getKickTo(), econTl.getKickFor())) {
            return;
        }

        toKickFaction.sendRichMessage(tl.getFactionMsg(),
                FPlayerResolver.of("player", sender),
                FPlayerResolver.of("target", toKick));
        toKick.sendRichMessage(tl.getKicked(),
                FPlayerResolver.of("player", sender),
                FactionResolver.of(toKickFaction));

        if (FactionsPlugin.instance().conf().logging().isFactionKick()) {
            AbstractFactionsPlugin.instance().log(sender.name() + " kicked " + toKick.name() + " from the faction: " + toKickFaction.tag());
        }

        toKickFaction.deInvite(toKick);
        toKick.resetFactionData(true);
    }
}
