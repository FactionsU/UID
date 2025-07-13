package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.upgrade.UpgradeSettings;
import dev.kitteh.factions.upgrade.Upgrades;
import dev.kitteh.factions.util.MiscUtil;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.math.BigDecimal;
import java.time.Duration;
import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdShield implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            Command.Builder<Sender> shield = builder.literal("shield")
                    .commandDescription(Cloudy.desc(TL.COMMAND_SHIELD_DESCRIPTION))
                    .permission(builder.commandPermission()
                            .and(Cloudy.predicate(s -> Universe.universe().isUpgradeEnabled(Upgrades.SHIELD)))
                            .and(Cloudy.hasPermission(Permission.SHIELD).and(Cloudy.isPlayer()))
                    );

            manager.command(shield.handler(ctx -> this.handle(ctx, false)));
            manager.command(shield.literal("status").handler(ctx -> this.handle(ctx, false)));
            manager.command(shield.literal("activate").handler(ctx -> this.handle(ctx, true)));
        };
    }

    private void handle(CommandContext<Sender> context, boolean exec) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        int lvl = faction.upgradeLevel(Upgrades.SHIELD);
        if (exec && sender.hasFaction() && lvl > 0 && !faction.shieldActive() && faction.shieldCooldownRemaining().isZero() && sender.faction().hasAccess(sender, PermissibleActions.SHIELD, sender.lastStoodAt())) {
            UpgradeSettings settings = Universe.universe().upgradeSettings(Upgrades.SHIELD);
            BigDecimal duration = settings.valueAt(Upgrades.Variables.DURATION, lvl);
            BigDecimal cooldown = settings.valueAt(Upgrades.Variables.COOLDOWN, lvl);

            faction.shield(Duration.ofSeconds(duration.longValue()), Duration.ofSeconds(cooldown.longValue()));

            faction.msgLegacy(TL.COMMAND_SHIELD_ACTIVATED, sender.describeToLegacy(faction), MiscUtil.durationString(faction.shieldRemaining()));

            return;
        }

        if (faction.shieldActive()) {
            sender.msgLegacy(TL.COMMAND_SHIELD_ACTIVE, MiscUtil.durationString(faction.shieldRemaining()));
        } else {
            sender.msgLegacy(TL.COMMAND_SHIELD_NOT_SET);
            if (lvl > 0) {
                UpgradeSettings settings = Universe.universe().upgradeSettings(Upgrades.SHIELD);
                if (faction.shieldCooldownRemaining().isZero()) {
                    BigDecimal duration = settings.valueAt(Upgrades.Variables.DURATION, lvl);
                    BigDecimal cooldown = settings.valueAt(Upgrades.Variables.COOLDOWN, lvl);
                    sender.msgLegacy(TL.COMMAND_SHIELD_AVAILABLE, MiscUtil.durationString(duration.longValue()), MiscUtil.durationString(cooldown.longValue()));
                } else {
                    sender.msgLegacy(TL.COMMAND_SHIELD_COOLDOWN, MiscUtil.durationString(faction.shieldCooldownRemaining()));
                }
            }
        }
    }
}
