package dev.kitteh.factions.command.paper;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.Participator;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.upgrade.Upgrade;
import dev.kitteh.factions.upgrade.UpgradeRegistry;
import dev.kitteh.factions.upgrade.UpgradeSettings;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

@SuppressWarnings("UnstableApiUsage")
public class CmdUpgrades implements Cmd {
    private static final ClickCallback.Options OPT = ClickCallback.Options.builder().build();

    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> manager.command(
                builder.literal("upgrades")
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.UPGRADES).and(Cloudy.hasFaction())))
                        .commandDescription(Cloudy.desc(TL.COMMAND_UPGRADES_DESCRIPTION))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();
        Faction faction = sender.faction();

        player.showDialog(this.mainMenu(faction));
    }

    private Dialog mainMenu(Audience audience) {
        return this.mainMenu(FPlayers.fPlayers().get((Player) audience).faction());
    }

    private Dialog mainMenu(Faction faction) {
        if (!faction.isNormal()) {
            return this.noLongerInFaction();
        }
        List<ActionButton> upgrades = UpgradeRegistry.getUpgrades().stream()
                .sorted(Comparator.comparing(Upgrade::name))
                .filter(Universe.universe()::isUpgradeEnabled)
                .map(upgrade -> {
                    int lvl = faction.upgradeLevel(upgrade);
                    UpgradeSettings settings = Universe.universe().upgradeSettings(upgrade);

                    TextComponent.Builder builder = Component.text().append(this.nameAtLevel(settings, lvl));
                    if (lvl > 1 && settings.maxLevel() != 1) {
                        builder.append(Component.text(" " + lvl));
                    }
                    builder.appendNewline()
                            .append(upgrade.description());
                    if (lvl > 0) {
                        builder.appendNewline()
                                .append(upgrade.details(settings, lvl));
                    }
                    if (lvl < settings.maxLevel()) {
                        builder.appendNewline()
                                .append(Component.text("Upgrade available: Costs " + settings.costAt(lvl + 1).toBigInteger().intValue()))
                                .appendNewline()
                                .append(this.nameAtLevel(settings, lvl + 1))
                                .appendNewline()
                                .append(upgrade.details(settings, lvl + 1));
                    } else {
                        builder.appendNewline()
                                .append(Component.text("Max level!"));
                    }
                    return ActionButton.builder(upgrade.nameComponent())
                            .tooltip(builder.build())
                            .action(DialogAction.customClick((r, audience) ->
                                    audience.showDialog(this.upgradeMenu(audience, upgrade)), OPT)).build();
                }).toList();

        return Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Component.text("Faction Upgrades"))
                        .body(List.of(
                                DialogBody.plainMessage(Component.text("Upgrades are listed below.")),
                                DialogBody.plainMessage(Component.text("Click for details or to buy an upgrade."))
                        )).build())
                .type(DialogType.multiAction(
                        upgrades,
                        ActionButton.builder(Component.text("Done")).build(),
                        2
                )));
    }

    private Component nameAtLevel(UpgradeSettings settings, int lvl) {
        Component name = settings.upgrade().nameComponent();
        if (lvl > 1 && settings.maxLevel() != 1) {
            return Component.text().append(name).append(Component.text(" " + lvl)).build();
        } else {
            return name;
        }
    }

    private Dialog upgradeMenu(Audience audience, Upgrade upgrade) {
        FPlayer fPlayer = FPlayers.fPlayers().get((Player) audience);
        Faction faction = fPlayer.faction();
        if (!faction.isNormal()) {
            return this.noLongerInFaction();
        }

        int lvl = faction.upgradeLevel(upgrade);
        Component name = upgrade.nameComponent();
        UpgradeSettings settings = Universe.universe().upgradeSettings(upgrade);

        TextComponent.Builder builder = Component.text().append(upgrade.description()).appendNewline();
        if (lvl > 0 && settings.maxLevel() != 1) {
            builder.append(Component.text("Current level: " + lvl));
        } else if (lvl == 1) {
            builder.append(Component.text("Unlocked"));
        } else {
            builder.append(Component.text("Not unlocked"));
        }
        if (lvl > 0) {
            builder.appendNewline().appendNewline()
                    .append(upgrade.details(settings, lvl))
                    .appendNewline();
        }
        if (lvl < settings.maxLevel()) {
            builder.appendNewline().appendNewline()
                    .append(Component.text("Upgrade available!")).appendNewline()
                    .append(Component.text("Costs " + settings.costAt(lvl + 1).toBigInteger().intValue())).appendNewline()
                    .append(name).append(Component.text(" " + (lvl + 1))).appendNewline()
                    .append(upgrade.details(settings, lvl + 1));
        } else if (lvl != 1) {
            builder.appendNewline()
                    .append(Component.text("Max level!"));
        }

        List<ActionButton> actions;
        if (lvl < settings.maxLevel() && Econ.shouldBeUsed() && faction.hasAccess(fPlayer, PermissibleActions.UPGRADE, null)) {
            actions = List.of(
                    ActionButton.builder(Component.text("Purchase upgrade"))
                            .action(DialogAction.customClick((r, aud) ->
                                    aud.showDialog(this.purchaseMenu(aud, upgrade)), OPT))
                            .build()
            );
        } else {
            actions = List.of();
        }

        return Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Component.text("Faction Upgrade: ").append(upgrade.nameComponent()))
                        .body(List.of(
                                DialogBody.plainMessage(builder.build())
                        )).build())
                .type(actions.isEmpty() ?
                        DialogType.notice(ActionButton.builder(Component.text("Return to upgrade list"))
                                .action(DialogAction.customClick((r, aud) ->
                                        aud.showDialog(this.mainMenu(aud)), OPT))
                                .build())
                        :
                        DialogType.multiAction(
                                actions,
                                ActionButton.builder(Component.text("Return to upgrade list"))
                                        .action(DialogAction.customClick((r, aud) ->
                                                aud.showDialog(this.mainMenu(aud)), OPT))
                                        .build(),
                                2
                        )));
    }

    private Dialog purchaseMenu(Audience audience, Upgrade upgrade) {
        FPlayer fPlayer = FPlayers.fPlayers().get((Player) audience);
        Faction faction = fPlayer.faction();
        if (!faction.isNormal()) {
            return this.noLongerInFaction();
        }

        int lvl = faction.upgradeLevel(upgrade);
        UpgradeSettings settings = Universe.universe().upgradeSettings(upgrade);

        if (lvl == settings.maxLevel()) {
            return Dialog.create(b -> b.empty()
                    .base(DialogBase.builder(Component.text("Upgrade already maxed out!"))
                            .body(List.of()).build())
                    .type(DialogType.notice(ActionButton.builder(Component.text("Return to upgrade info"))
                            .action(DialogAction.customClick((r, aud) ->
                                    aud.showDialog(this.upgradeMenu(aud, upgrade)), OPT))
                            .build())));
        }

        double cost = settings.costAt(lvl + 1).doubleValue();

        Participator purchaser;
        if (FactionsPlugin.instance().conf().economy().isBankEnabled() && FactionsPlugin.instance().conf().economy().isBankFactionPaysCosts()) {
            purchaser = faction;
        } else {
            purchaser = fPlayer;
        }

        if (Econ.has(purchaser, cost)) {
            return Dialog.create(b -> b.empty()
                    .base(DialogBase.builder(Component.text("Purchase next level of ").append(upgrade.nameComponent()).append(Component.text("?")))
                            .body(List.of(
                                    DialogBody.plainMessage(Component.text("Cost: " + cost))
                            )).build())
                    .type(DialogType.multiAction(
                            List.of(
                                    ActionButton.builder(Component.text("Confirm Purchase"))
                                            .action(DialogAction.customClick((r, aud) ->
                                                    aud.showDialog(this.makePurchase(aud, upgrade, lvl + 1)), OPT))
                                            .build()
                            ),
                            ActionButton.builder(Component.text("Return to upgrade info"))
                                    .action(DialogAction.customClick((r, aud) ->
                                            aud.showDialog(this.upgradeMenu(aud, upgrade)), OPT))
                                    .build(),
                            2
                    )));
        } else {
            return Dialog.create(b -> b.empty()
                    .base(DialogBase.builder(Component.text("Cannot afford next upgrade level!"))
                            .body(List.of()).build())
                    .type(DialogType.notice(ActionButton.builder(Component.text("Return to upgrade info"))
                            .action(DialogAction.customClick((r, aud) ->
                                    aud.showDialog(this.upgradeMenu(aud, upgrade)), OPT))
                            .build())));
        }
    }

    private Dialog makePurchase(Audience audience, Upgrade upgrade, int newLvl) {
        FPlayer fPlayer = FPlayers.fPlayers().get((Player) audience);
        Faction faction = fPlayer.faction();
        if (!faction.isNormal()) {
            return this.noLongerInFaction();
        }

        if (newLvl != (1 + faction.upgradeLevel(upgrade))) {
            return Dialog.create(b -> b.empty()
                    .base(DialogBase.builder(Component.text("Upgrade changed level while you were in the menu!"))
                            .body(List.of()).build())
                    .type(DialogType.notice(ActionButton.builder(Component.text("Return to upgrade info"))
                            .action(DialogAction.customClick((r, aud) ->
                                    aud.showDialog(this.upgradeMenu(aud, upgrade)), OPT))
                            .build())));
        }

        UpgradeSettings settings = Universe.universe().upgradeSettings(upgrade);

        double cost = settings.costAt(newLvl).doubleValue();

        Participator purchaser;
        if (FactionsPlugin.instance().conf().economy().isBankEnabled() && FactionsPlugin.instance().conf().economy().isBankFactionPaysCosts()) {
            purchaser = faction;
        } else {
            purchaser = fPlayer;
        }

        if (Econ.modifyMoney(purchaser, -cost)) {
            faction.upgradeLevel(upgrade, newLvl);
            return Dialog.create(b -> b.empty()
                    .base(DialogBase.builder(Component.text("Upgrade purchased!"))
                            .body(List.of()).build())
                    .type(DialogType.notice(ActionButton.builder(Component.text("Return to upgrade info"))
                            .action(DialogAction.customClick((r, aud) ->
                                    aud.showDialog(this.upgradeMenu(aud, upgrade)), OPT))
                            .build())));
        } else {
            return Dialog.create(b -> b.empty()
                    .base(DialogBase.builder(Component.text("Cannot afford next upgrade level!"))
                            .body(List.of()).build())
                    .type(DialogType.notice(ActionButton.builder(Component.text("Return to upgrade info"))
                            .action(DialogAction.customClick((r, aud) ->
                                    aud.showDialog(this.upgradeMenu(aud, upgrade)), OPT))
                            .build())));
        }
    }

    private Dialog noLongerInFaction() {
        return Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Component.text("Access Denied"))
                        .body(List.of(
                                DialogBody.plainMessage(Component.text("You are no longer in a faction."))
                        )).build())
                .type(DialogType.notice()));
    }
}
