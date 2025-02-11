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

public class CmdMod extends FCommand {

    public CmdMod() {
        super();
        this.aliases.add("mod");
        this.aliases.add("setmod");
        this.aliases.add("officer");
        this.aliases.add("setofficer");

        this.optionalArgs.put("player", "player");

        this.requirements = new CommandRequirements.Builder(Permission.MOD)
                .memberOnly()
                .withRole(Role.COLEADER)
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        FPlayer you = context.argAsBestFPlayerMatch(0);
        if (you == null) {
            LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
            Component component = legacy.deserialize(TL.COMMAND_MOD_CANDIDATES.toString()).color(NamedTextColor.GOLD);
            for (FPlayer player : context.faction.getFPlayersWhereRole(Role.NORMAL)) {
                String s = player.getName();
                component = component.append(Component.text().color(NamedTextColor.WHITE)
                        .content(s + " ")
                        .hoverEvent(legacy.deserialize(TL.COMMAND_MOD_CLICKTOPROMOTE + s))
                        .clickEvent(ClickEvent.runCommand("/" + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + " mod " + s))
                );
            }

            FactionsPlugin.getInstance().getAdventure().player(context.player).sendMessage(component);
            return;
        }

        boolean permAny = Permission.MOD_ANY.has(context.sender, false);
        Faction targetFaction = you.getFaction();

        if (targetFaction != context.faction && !permAny) {
            context.msg(TL.COMMAND_MOD_NOTMEMBER, you.describeTo(context.fPlayer, true));
            return;
        }

        if (context.fPlayer != null && !context.fPlayer.getRole().isAtLeast(Role.COLEADER) && !permAny) {
            context.msg(TL.COMMAND_MOD_NOTADMIN);
            return;
        }

        if (you == context.fPlayer && !permAny) {
            context.msg(TL.COMMAND_MOD_SELF);
            return;
        }

        if (you.getRole() == Role.ADMIN) {
            context.msg(TL.COMMAND_MOD_TARGETISADMIN);
            return;
        }

        if (you.getRole() == Role.MODERATOR) {
            // Revoke
            you.setRole(Role.NORMAL);
            targetFaction.msg(TL.COMMAND_MOD_REVOKED, you.describeTo(targetFaction, true));
            context.msg(TL.COMMAND_MOD_REVOKES, you.describeTo(context.fPlayer, true));
        } else {
            // Give
            you.setRole(Role.MODERATOR);
            targetFaction.msg(TL.COMMAND_MOD_PROMOTED, you.describeTo(targetFaction, true));
            context.msg(TL.COMMAND_MOD_PROMOTES, you.describeTo(context.fPlayer, true));
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_MOD_DESCRIPTION;
    }

}
