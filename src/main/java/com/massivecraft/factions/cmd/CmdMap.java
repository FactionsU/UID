package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;


public class CmdMap extends FCommand {

    public CmdMap() {
        super();
        this.aliases.add("map");

        this.optionalArgs.put("on/off", "once");

        this.requirements = new CommandRequirements.Builder(Permission.MAP)
                .playerOnly()
                .noDisableOnLock()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        if (context.argIsSet(0)) {
            if (context.argAsBool(0, !context.fPlayer.isMapAutoUpdating())) {
                // Turn on

                // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
                if (!context.payForCommand(FactionsPlugin.getInstance().conf().economy().getCostMap(), "to show the map", "for showing the map")) {
                    return;
                }

                context.fPlayer.setMapAutoUpdating(true);
                context.msg(TL.COMMAND_MAP_UPDATE_ENABLED);

                // And show the map once
                showMap(context);
            } else {
                // Turn off
                context.fPlayer.setMapAutoUpdating(false);
                context.msg(TL.COMMAND_MAP_UPDATE_DISABLED);
            }
        } else {
            // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
            if (!context.payForCommand(FactionsPlugin.getInstance().conf().economy().getCostMap(), TL.COMMAND_MAP_TOSHOW, TL.COMMAND_MAP_FORSHOW)) {
                return;
            }

            showMap(context);
        }
    }

    public void showMap(CommandContext context) {
        Audience audience = FactionsPlugin.getInstance().getAdventure().player(context.player);
        for (Component component : Board.getInstance().getMap(context.fPlayer, new FLocation(context.fPlayer), context.fPlayer.getPlayer().getLocation().getYaw())) {
            audience.sendMessage(component);
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_MAP_DESCRIPTION;
    }

}
