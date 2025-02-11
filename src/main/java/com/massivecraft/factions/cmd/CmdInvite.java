package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.PermissibleActions;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

public class CmdInvite extends FCommand {

    public CmdInvite() {
        super();
        this.aliases.add("invite");
        this.aliases.add("inv");

        this.requiredArgs.add("player");

        this.requirements = new CommandRequirements.Builder(Permission.INVITE)
                .memberOnly()
                .withAction(PermissibleActions.INVITE)
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        FPlayer target = context.argAsBestFPlayerMatch(0);
        if (target == null) {
            return;
        }

        if (target.getFaction() == context.faction) {
            context.msg(TL.COMMAND_INVITE_ALREADYMEMBER, target.getName(), context.faction.getTag());
            context.msg(TL.GENERIC_YOUMAYWANT + FCmdRoot.getInstance().cmdKick.getUsageTemplate(context));
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        if (!context.payForCommand(FactionsPlugin.getInstance().conf().economy().getCostInvite(), TL.COMMAND_INVITE_TOINVITE.toString(), TL.COMMAND_INVITE_FORINVITE.toString())) {
            return;
        }

        if (context.faction.isBanned(target)) {
            context.msg(TL.COMMAND_INVITE_BANNED, target.getName());
            return;
        }

        context.faction.invite(target);
        if (target.isOnline()) {
            LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
            Component component = legacy.deserialize(context.fPlayer.describeTo(target, true))
                    .append(legacy.deserialize(TL.COMMAND_INVITE_INVITEDYOU.toString()).color(NamedTextColor.YELLOW))
                    .append(legacy.deserialize(context.faction.describeTo(target)));
            component = component.hoverEvent(legacy.deserialize(TL.COMMAND_INVITE_CLICKTOJOIN.toString()).asHoverEvent())
                    .clickEvent(ClickEvent.runCommand("/" + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + " join " + ChatColor.stripColor(context.faction.getTag())));
            FactionsPlugin.getInstance().getAdventure().player(target.getPlayer()).sendMessage(component);
        }

        //you.msg("%s<i> invited you to %s",context.fPlayer.describeTo(you, true), context.faction.describeTo(you));
        context.faction.msg(TL.COMMAND_INVITE_INVITED, context.fPlayer.describeTo(context.faction, true), target.describeTo(context.faction));
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_INVITE_DESCRIPTION;
    }

}
