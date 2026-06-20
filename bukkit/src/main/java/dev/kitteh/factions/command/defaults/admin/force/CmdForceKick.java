package dev.kitteh.factions.command.defaults.admin.force;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FPlayerLeaveEvent;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.util.Permission;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdForceKick implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().kick();
            manager.command(
                    builder.literal("kick")
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.KICK_ANY)))
                            .required("target", FPlayerParser.of(FPlayerParser.Include.ALL))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().kick();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        FPlayer toKick = context.get("target");

        Faction toKickFaction = toKick.faction();

        if (toKickFaction.isWilderness()) {
            sender.sendRichMessage(tl.getNone());
            return;
        }

        // trigger the leave event (cancellable) [reason:kicked]
        FPlayerLeaveEvent event = new FPlayerLeaveEvent(toKick, toKick.faction(), FPlayerLeaveEvent.Reason.ADMIN_KICKED);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        toKickFaction.sendRichMessage(tl.getFactionMsg(), FPlayerResolver.of("player", sender), Placeholder.unparsed("target", toKick.name()));
        toKick.sendRichMessage(tl.getKicked(), FPlayerResolver.of("player", sender), FactionResolver.of(toKickFaction));

        if (FactionsPlugin.instance().conf().logging().isFactionKick()) {
            AbstractFactionsPlugin.instance().log(sender.name() + " kicked " + toKick.name() + " from the faction: " + toKickFaction.tag());
        }

        if (toKick.role() == Role.ADMIN) {
            toKickFaction.promoteNewLeader();
        }

        toKickFaction.deInvite(toKick);
        toKick.resetFactionData(true);
    }
}
