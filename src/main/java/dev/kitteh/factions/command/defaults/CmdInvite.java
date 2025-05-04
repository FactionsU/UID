package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.util.ComponentDispatcher;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdInvite implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("invite")
                            .commandDescription(Cloudy.desc(TL.COMMAND_INVITE_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.INVITE).and(Cloudy.hasSelfFactionPerms(PermissibleActions.INVITE))))
                            .required("player", FPlayerParser.of(FPlayerParser.Include.OTHER_FACTION))
                            .flag(manager.flagBuilder("delete"))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.getFaction();

        FPlayer target = context.get("player");

        if (target.getFaction() == faction) {
            sender.msg(TL.COMMAND_INVITE_ALREADYMEMBER, target.getName(), faction.getTag());
            return;
        }

        if (context.flags().hasFlag("delete")) {
            faction.deinvite(target);
            target.msg(TL.COMMAND_DEINVITE_REVOKED, sender.describeTo(target), faction.describeTo(target));
            faction.msg(TL.COMMAND_DEINVITE_REVOKES, sender.describeTo(faction), target.describeTo(faction));
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        if (!context.sender().payForCommand(FactionsPlugin.getInstance().conf().economy().getCostInvite(), TL.COMMAND_INVITE_TOINVITE, TL.COMMAND_INVITE_FORINVITE)) {
            return;
        }

        if (faction.isBanned(target)) {
            sender.msg(TL.COMMAND_INVITE_BANNED, target.getName());
            return;
        }

        faction.invite(target);
        if (target.isOnline()) {
            LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
            Component component = legacy.deserialize(sender.describeTo(target))
                    .append(legacy.deserialize(TL.COMMAND_INVITE_INVITEDYOU.toString()).color(NamedTextColor.YELLOW))
                    .append(legacy.deserialize(faction.describeTo(target)));
            component = component.hoverEvent(legacy.deserialize(TL.COMMAND_INVITE_CLICKTOJOIN.toString()).asHoverEvent())
                    .clickEvent(ClickEvent.runCommand("/" + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + " join " + ChatColor.stripColor(faction.getTag())));
            ComponentDispatcher.send(target.getPlayer(), component);
        }

        //you.msg("%s<i> invited you to %s",context.fPlayer.describeTo(you, true), context.faction.describeTo(you));
        faction.msg(TL.COMMAND_INVITE_INVITED, sender.describeTo(faction), target.describeTo(faction));
    }
}
