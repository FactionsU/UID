package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.data.MemoryFaction;
import dev.kitteh.factions.event.FPlayerLeaveEvent;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.util.BanInfo;
import dev.kitteh.factions.util.Permission;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

import java.util.logging.Level;

public class CmdBan implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().ban();
            Command.Builder<Sender> build = builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .commandDescription(Cloudy.desc(tl.getDescription()))
                    .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.BAN).and(Cloudy.hasSelfFactionPerms(PermissibleActions.BAN))));

            manager.command(
                    build.required("player", FPlayerParser.of(FPlayerParser.Include.SAME_FACTION, FPlayerParser.Include.ROLE_BELOW, FPlayerParser.Include.OTHER_FACTION))
                            .handler(this::handle)
            );

            manager.command(build.meta(HIDE_IN_HELP, true).handler(ctx -> help.queryCommands(Cmd.rootCommand() + " ban <player>", ctx.sender())));
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().ban();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        FPlayer target = context.get("player");
        Faction faction = sender.faction();

        if (sender == target) {
            sender.sendRichMessage(tl.getSelf());
            return;
        } else if (target.faction() == faction && target.role().isAtLeast(sender.role())) {
            sender.sendRichMessage(tl.getInsufficientRank(), Placeholder.unparsed("player", target.name()));
            return;
        }

        if (faction.bans().stream().map(BanInfo::banned).anyMatch(u -> u.equals(target.uniqueId()))) {
            sender.sendRichMessage(tl.getAlreadyBanned(), Placeholder.unparsed("player", target.name()));
            return;
        }

        if (target.faction() == faction) {
            FPlayerLeaveEvent event = new FPlayerLeaveEvent(target, faction, FPlayerLeaveEvent.Reason.BANNED);
            Bukkit.getServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                AbstractFactionsPlugin.instance().log(Level.WARNING, "Attempted to ban " + target.name() + " but a plugin cancelled the kick event.");
                return;
            }

            ((MemoryFaction) faction).removeMember(target);
            target.resetFactionData(true);
        }

        faction.ban(target, sender);
        faction.deInvite(target);

        target.sendRichMessage(tl.getTarget(), Placeholder.unparsed("faction", faction.tag()));
        faction.sendRichMessage(tl.getBanned(),
                FPlayerResolver.of("player", sender),
                FPlayerResolver.of("target", target)
        );
    }
}
