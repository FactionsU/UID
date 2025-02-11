package com.massivecraft.factions.cmd;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.vaultmanagement.VaultManager;
import com.drtshock.playervaults.vaultmanagement.VaultOperations;
import com.drtshock.playervaults.vaultmanagement.VaultViewInfo;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class CmdVault extends FCommand {

    public CmdVault() {
        this.aliases.add("vault");

        this.optionalArgs.put("number", "number");

        this.requirements = new CommandRequirements.Builder(Permission.VAULT)
                .memberOnly()
                .noDisableOnLock()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        /*
             /f vault <number>
         */

        int number = context.argAsInt(0, 0); // Default to 0 or show on 0

        Player player = context.player;

        if (PlayerVaults.getInstance().getInVault().containsKey(player.getUniqueId().toString())) {
            return; // Already in a vault so they must be trying to dupe.
        }

        int max = context.faction.getMaxVaults();
        if (number > max) {
            player.sendMessage(TL.COMMAND_VAULT_TOOHIGH.format(number, max));
            return;
        }

        // Something like faction-id
        String vaultName = String.format(FactionsPlugin.getInstance().conf().playerVaults().getVaultPrefix(), context.faction.getId());

        if (number < 1) {
            // Message about which vaults that Faction has.
            // List the target
            YamlConfiguration file = VaultManager.getInstance().getPlayerVaultFile(vaultName, false);
            if (file == null) {
                PlayerVaults.getInstance().getTL().vaultDoesNotExist().title().send(context.sender);
            } else {
                StringBuilder sb = new StringBuilder();
                for (String key : file.getKeys(false)) {
                    sb.append(key.replace("vault", "")).append(" ");
                }
                PlayerVaults.getInstance().getTL().existingVaults().title().with("player", context.fPlayer.getTag()).with("vault", sb.toString().trim()).send(context.sender);
            }
            return;
        } // end listing vaults.

        // Attempt to open vault.
        if (VaultOperations.openOtherVault(player, vaultName, String.valueOf(number))) {
            // Success
            PlayerVaults.getInstance().getInVault().put(player.getUniqueId().toString(), new VaultViewInfo(vaultName, number));
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_VAULT_DESCRIPTION;
    }

}
