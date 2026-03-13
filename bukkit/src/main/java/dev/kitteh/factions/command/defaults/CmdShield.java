package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.upgrade.UpgradeSettings;
import dev.kitteh.factions.upgrade.Upgrades;
import dev.kitteh.factions.util.MiscUtil;
import dev.kitteh.factions.util.Permission;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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
            var tl = FactionsPlugin.instance().tl().commands().shield();
            Command.Builder<Sender> shield = builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .commandDescription(Cloudy.desc(tl.getDescription()))
                    .permission(builder.commandPermission()
                            .and(Cloudy.predicate(s -> Universe.universe().isUpgradeEnabled(Upgrades.SHIELD)))
                            .and(Cloudy.hasPermission(Permission.SHIELD).and(Cloudy.isPlayer()))
                    );

            manager.command(shield.handler(ctx -> this.handle(ctx, false)));
            manager.command(shield.literal(tl.getCommandStatus()).handler(ctx -> this.handle(ctx, false)));
            manager.command(shield.literal(tl.getCommandActivate()).handler(ctx -> this.handle(ctx, true)));
        };
    }

    private void handle(CommandContext<Sender> context, boolean exec) {
        var tl = FactionsPlugin.instance().tl().commands().shield();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        int lvl = faction.upgradeLevel(Upgrades.SHIELD);
        if (exec && sender.hasFaction() && lvl > 0 && !faction.shieldActive() && faction.shieldCooldownRemaining().isZero() && sender.faction().hasAccess(sender, PermissibleActions.SHIELD, sender.lastStoodAt())) {
            UpgradeSettings settings = Universe.universe().upgradeSettings(Upgrades.SHIELD);
            BigDecimal duration = settings.valueAt(Upgrades.Variables.DURATION, lvl);
            BigDecimal cooldown = settings.valueAt(Upgrades.Variables.COOLDOWN, lvl);

            faction.shield(Duration.ofSeconds(duration.longValue()), Duration.ofSeconds(cooldown.longValue()));

            faction.sendRichMessage(tl.getActivated(), FPlayerResolver.of("player", sender), Placeholder.unparsed("duration", MiscUtil.durationString(faction.shieldRemaining())));

            return;
        }

        if (faction.shieldActive()) {
            sender.sendRichMessage(tl.getStatusActive(), Placeholder.unparsed("duration", MiscUtil.durationString(faction.shieldRemaining())));
        } else {
            sender.sendRichMessage(tl.getStatusNotActive());
            if (lvl > 0) {
                UpgradeSettings settings = Universe.universe().upgradeSettings(Upgrades.SHIELD);
                if (faction.shieldCooldownRemaining().isZero()) {
                    BigDecimal duration = settings.valueAt(Upgrades.Variables.DURATION, lvl);
                    BigDecimal cooldown = settings.valueAt(Upgrades.Variables.COOLDOWN, lvl);
                    sender.sendRichMessage(tl.getStatusAvailable(),
                            Placeholder.unparsed("duration", MiscUtil.durationString(duration.longValue())),
                            Placeholder.unparsed("cooldown", MiscUtil.durationString(cooldown.longValue()))
                    );
                } else {
                    sender.sendRichMessage(tl.getStatusCooldown(), Placeholder.unparsed("cooldown", MiscUtil.durationString(faction.shieldCooldownRemaining())));
                }
            }
        }
    }
}
