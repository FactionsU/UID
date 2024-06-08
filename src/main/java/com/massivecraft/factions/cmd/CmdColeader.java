package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.Role;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class CmdColeader extends FCommand {

    public CmdColeader() {
        super();
        this.aliases.add("coleader");
        this.aliases.add("setcoleader");
        this.aliases.add("cl");
        this.aliases.add("setcl");

        this.requiredArgs.add("player");

        this.requirements = new CommandRequirements.Builder(Permission.COLEADER)
                .memberOnly()
                .withRole(Role.ADMIN)
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        FPlayer target = context.argAsBestFPlayerMatch(0);
        if (target == null) {
            LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
            Component component = legacy.deserialize(TL.COMMAND_COLEADER_CANDIDATES.toString()).color(NamedTextColor.GOLD);
            for (FPlayer player : context.faction.getFPlayersWhereRole(Role.MODERATOR)) {
                String s = player.getName();
                component = component.append(Component.text().color(NamedTextColor.WHITE)
                        .content(s + " ")
                        .hoverEvent(legacy.deserialize(TL.COMMAND_COLEADER_CLICKTOPROMOTE + s))
                        .clickEvent(ClickEvent.runCommand("/" + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + " coleader " + s))
                );
            }
            FactionsPlugin.getInstance().getAdventure().player(context.player).sendMessage(component);
            return;
        }

        boolean permAny = Permission.COLEADER_ANY.has(context.sender, false);
        Faction targetFaction = target.getFaction();

        if (targetFaction != context.faction && !permAny) {
            context.msg(TL.COMMAND_COLEADER_NOTMEMBER, target.describeTo(context.fPlayer, true));
            return;
        }

        if (context.fPlayer != null && context.fPlayer.getRole() != Role.ADMIN && !permAny) {
            context.msg(TL.COMMAND_COLEADER_NOTADMIN);
            return;
        }

        if (target == context.fPlayer && !permAny) {
            context.msg(TL.COMMAND_COLEADER_SELF);
            return;
        }

        if (target.getRole() == Role.ADMIN) {
            context.msg(TL.COMMAND_COLEADER_TARGETISADMIN);
            return;
        }

        if (target.getRole() == Role.COLEADER) {
            // Revoke
            target.setRole(Role.MODERATOR);
            targetFaction.msg(TL.COMMAND_COLEADER_REVOKED, target.describeTo(targetFaction, true));
            context.msg(TL.COMMAND_COLEADER_REVOKES, target.describeTo(context.fPlayer, true));
            return;
        }

        // Check to see if we should allow multiple coleaders or not.
        if (!FactionsPlugin.getInstance().conf().factions().other().isAllowMultipleColeaders() && !targetFaction.getFPlayersWhereRole(Role.COLEADER).isEmpty()) {
            context.msg(TL.COMMAND_COLEADER_ALREADY_COLEADER);
            return;
        }

        // Give
        target.setRole(Role.COLEADER);
        targetFaction.msg(TL.COMMAND_COLEADER_PROMOTED, target.describeTo(targetFaction, true));
        context.msg(TL.COMMAND_COLEADER_PROMOTES, target.describeTo(context.fPlayer, true));
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_COLEADER_DESCRIPTION;
    }

}
