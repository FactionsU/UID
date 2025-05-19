package dev.kitteh.factions.command.defaults;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.vaultmanagement.VaultManager;
import com.drtshock.playervaults.vaultmanagement.VaultOperations;
import com.drtshock.playervaults.vaultmanagement.VaultViewInfo;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.configuration.file.YamlConfiguration;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.IntegerParser;

import java.util.function.BiConsumer;

public class CmdVault implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("vault")
                            .commandDescription(Cloudy.desc(TL.COMMAND_VAULT_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.VAULT).and(Cloudy.hasFaction())))
                            .required("number", IntegerParser.integerParser(1))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        int number = context.get("number"); // Default to 0 or show on 0

        if (PlayerVaults.getInstance().getInVault().containsKey(sender.uniqueId().toString())) {
            return; // Already in a vault so they must be trying to dupe.
        }

        int max = faction.maxVaults();
        if (number > max) {
            sender.sendMessage(TL.COMMAND_VAULT_TOOHIGH.format(number, max));
            return;
        }

        // Something like faction-id
        String vaultName = String.format(FactionsPlugin.getInstance().conf().playerVaults().getVaultPrefix(), "" + faction.id());

        if (number < 1) {
            // Message about which vaults that Faction has.
            // List the target
            YamlConfiguration file = VaultManager.getInstance().getPlayerVaultFile(vaultName, false);
            if (file == null) {
                PlayerVaults.getInstance().getTL().vaultDoesNotExist().title().send(context.sender().sender());
            } else {
                StringBuilder sb = new StringBuilder();
                for (String key : file.getKeys(false)) {
                    sb.append(key.replace("vault", "")).append(" ");
                }
                PlayerVaults.getInstance().getTL().existingVaults().title().with("player", faction.tag()).with("vault", sb.toString().trim()).send(context.sender().sender());
            }
            return;
        } // end listing vaults.

        // Attempt to open vault.
        if (VaultOperations.openOtherVault(((Sender.Player) context.sender()).player(), vaultName, String.valueOf(number))) {
            // Success
            PlayerVaults.getInstance().getInVault().put(sender.uniqueId().toString(), new VaultViewInfo(vaultName, number));
        }
    }
}
