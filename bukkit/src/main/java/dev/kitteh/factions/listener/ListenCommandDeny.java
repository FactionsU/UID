package dev.kitteh.factions.listener;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.chat.ChatTarget;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.WorldUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Set;

public class ListenCommandDeny implements Listener {
    private final AbstractFactionsPlugin plugin;

    public ListenCommandDeny(AbstractFactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!WorldUtil.isEnabled(event.getPlayer())) {
            return;
        }

        String cmd = event.getMessage().split(" ")[0];

        if (this.plugin.conf().factions().chat().internalChat().isTriggerPublicChat(cmd.startsWith("/") ? cmd.substring(1) : cmd)) {
            FPlayer p = FPlayers.fPlayers().get(event.getPlayer());
            p.chatTarget(ChatTarget.PUBLIC);
            p.msgLegacy(TL.COMMAND_CHAT_MODE_PUBLIC);
        }

        if (this.denyCommand(event.getMessage(), event.getPlayer())) {
            if (plugin.conf().logging().isPlayerCommands()) {
                plugin.getLogger().info("[PLAYER_COMMAND] " + event.getPlayer().getName() + ": " + event.getMessage());
            }
            event.setCancelled(true);
        }
    }

    // Planned for removal, or would be in Protection
    private boolean denyCommand(String fullCmd, Player player) {
        MainConfig.Factions.Protection protection = FactionsPlugin.instance().conf().factions().protection();
        if ((protection.getTerritoryNeutralDenyCommands().isEmpty() &&
                protection.getTerritoryEnemyDenyCommands().isEmpty() &&
                protection.getPermanentFactionMemberDenyCommands().isEmpty() &&
                protection.getWildernessDenyCommands().isEmpty() &&
                protection.getTerritoryAllyDenyCommands().isEmpty() &&
                protection.getTerritoryTruceDenyCommands().isEmpty() &&
                protection.getWarzoneDenyCommands().isEmpty())) {
            return false;
        }

        fullCmd = fullCmd.toLowerCase();

        FPlayer me = FPlayers.fPlayers().get(player);

        String shortCmd;  // command without the slash at the beginning
        if (fullCmd.startsWith("/")) {
            shortCmd = fullCmd.substring(1);
        } else {
            shortCmd = fullCmd;
            fullCmd = "/" + fullCmd;
        }

        if (me.hasFaction() &&
                !me.adminBypass() &&
                !protection.getPermanentFactionMemberDenyCommands().isEmpty() &&
                me.faction().isPermanent() &&
                isCommandInSet(fullCmd, shortCmd, protection.getPermanentFactionMemberDenyCommands())) {
            me.msgLegacy(TL.PLAYER_COMMAND_PERMANENT, fullCmd);
            return true;
        }

        Faction at = new FLocation(player.getLocation()).faction();
        if (at.isWilderness() && !protection.getWildernessDenyCommands().isEmpty() && !me.adminBypass() && isCommandInSet(fullCmd, shortCmd, protection.getWildernessDenyCommands())) {
            me.msgLegacy(TL.PLAYER_COMMAND_WILDERNESS, fullCmd);
            return true;
        }

        Relation rel = at.relationTo(me);
        if (at.isNormal() && rel.isAlly() && !protection.getTerritoryAllyDenyCommands().isEmpty() && !me.adminBypass() && isCommandInSet(fullCmd, shortCmd, protection.getTerritoryAllyDenyCommands())) {
            me.msgLegacy(TL.PLAYER_COMMAND_ALLY, fullCmd);
            return true;
        }

        if (at.isNormal() && rel.isTruce() && !protection.getTerritoryTruceDenyCommands().isEmpty() && !me.adminBypass() && isCommandInSet(fullCmd, shortCmd, protection.getTerritoryTruceDenyCommands())) {
            me.msgLegacy(TL.PLAYER_COMMAND_TRUCE, fullCmd);
            return true;
        }

        if (at.isNormal() && rel.isNeutral() && !protection.getTerritoryNeutralDenyCommands().isEmpty() && !me.adminBypass() && isCommandInSet(fullCmd, shortCmd, protection.getTerritoryNeutralDenyCommands())) {
            me.msgLegacy(TL.PLAYER_COMMAND_NEUTRAL, fullCmd);
            return true;
        }

        if (at.isNormal() && rel.isEnemy() && !protection.getTerritoryEnemyDenyCommands().isEmpty() && !me.adminBypass() && isCommandInSet(fullCmd, shortCmd, protection.getTerritoryEnemyDenyCommands())) {
            me.msgLegacy(TL.PLAYER_COMMAND_ENEMY, fullCmd);
            return true;
        }

        if (at.isWarZone() && !protection.getWarzoneDenyCommands().isEmpty() && !me.adminBypass() && isCommandInSet(fullCmd, shortCmd, protection.getWarzoneDenyCommands())) {
            me.msgLegacy(TL.PLAYER_COMMAND_WARZONE, fullCmd);
            return true;
        }

        return false;
    }

    private boolean isCommandInSet(String fullCmd, String shortCmd, Set<String> set) {
        for (String string : set) {
            if (string == null) {
                continue;
            }
            string = string.toLowerCase();
            if (fullCmd.startsWith(string) || shortCmd.startsWith(string)) {
                return true;
            }
        }
        return false;
    }
}
