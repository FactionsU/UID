package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.PermissibleActions;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class CmdDeinvite extends FCommand {

    public CmdDeinvite() {
        super();
        this.aliases.add("deinvite");
        this.aliases.add("deinv");

        this.optionalArgs.put("player", "player");
        //this.optionalArgs.put("", "");

        this.requirements = new CommandRequirements.Builder(Permission.DEINVITE)
                .memberOnly()
                .withAction(PermissibleActions.INVITE)
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        FPlayer you = context.argAsBestFPlayerMatch(0);
        if (you == null) {
            LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
            Component component = legacy.deserialize(TL.COMMAND_DEINVITE_CANDEINVITE.toString()).color(NamedTextColor.GOLD);
            for (String id : context.faction.getInvites()) {
                FPlayer fp = FPlayers.getInstance().getById(id);
                String name = fp != null ? fp.getName() : id;
                component = component.append(Component.text().color(NamedTextColor.GRAY).content(name + " ")
                        .hoverEvent(legacy.deserialize(TL.COMMAND_DEINVITE_CLICKTODEINVITE.format(name)).asHoverEvent())
                        .clickEvent(ClickEvent.runCommand("/" + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + " deinvite " + name))
                );
            }
            FactionsPlugin.getInstance().getAdventure().player(context.player).sendMessage(component);
            return;
        }

        if (you.getFaction() == context.faction) {
            context.msg(TL.COMMAND_DEINVITE_ALREADYMEMBER, you.getName(), context.faction.getTag());
            context.msg(TL.COMMAND_DEINVITE_MIGHTWANT, FCmdRoot.getInstance().cmdKick.getUsageTemplate(context));
            return;
        }

        context.faction.deinvite(you);

        you.msg(TL.COMMAND_DEINVITE_REVOKED, context.fPlayer.describeTo(you), context.faction.describeTo(you));

        context.faction.msg(TL.COMMAND_DEINVITE_REVOKES, context.fPlayer.describeTo(context.faction), you.describeTo(context.faction));
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_DEINVITE_DESCRIPTION;
    }

}
