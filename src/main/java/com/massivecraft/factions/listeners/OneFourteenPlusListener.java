package com.massivecraft.factions.listeners;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.PermissibleAction;
import com.massivecraft.factions.perms.PermissibleActions;
import com.massivecraft.factions.util.TL;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;

public class OneFourteenPlusListener extends AbstractListener {
    private final FactionsPlugin plugin;

    public OneFourteenPlusListener(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void doYouHaveALibraryCard(PlayerTakeLecternBookEvent event) {
        Player player = event.getPlayer();
        if (this.plugin.conf().factions().protection().getPlayersWhoBypassAllProtection().contains(player.getName())) {
            return;
        }

        FPlayer me = FPlayers.getInstance().getByPlayer(player);
        if (me.isAdminBypassing()) {
            return;
        }

        FLocation location = new FLocation(event.getLectern().getLocation());
        Faction otherFaction = Board.getInstance().getFactionAt(location);
        if (this.plugin.getLandRaidControl().isRaidable(otherFaction)) {
            return;
        }

        PermissibleAction action = PermissibleActions.CONTAINER;
        if (!otherFaction.hasAccess(me, action, location)) {
            me.msg(TL.GENERIC_NOPERMISSION, action);
            event.setCancelled(true);
        }
    }
}
