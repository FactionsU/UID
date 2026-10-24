package dev.kitteh.factions.command.paper;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.upgrade.Upgrade;
import dev.kitteh.factions.upgrade.UpgradePrerequisite;
import dev.kitteh.factions.upgrade.UpgradeRegistry;
import dev.kitteh.factions.upgrade.UpgradeSettings;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

import java.util.Comparator;
import java.util.List;

public class CmdUpgrades implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = Confs.tl().commands().upgrades();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.UPGRADES).and(Cloudy.hasFaction())))
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();

        player.showDialog(this.mainMenu(sender));
    }

    private Dialog mainMenu(Audience audience) {
        return this.mainMenu(FPlayers.fPlayers().get((Player) audience));
    }

    private Dialog mainMenu(FPlayer sender) {
        Faction faction = sender.faction();
        if (!faction.isNormal()) {
            return this.noLongerInFaction(sender);
        }

        var tl = Confs.tl().commands().upgrades().paper();

        List<ActionButton> upgrades = UpgradeRegistry.getUpgrades().stream()
                .sorted(Comparator.comparing(Upgrade::name))
                .filter(Universe.universe()::isUpgradeEnabled)
                .map(upgrade -> ActionButton.builder(upgrade.nameComponent())
                        .action(DialogAction.customClick((_, audience) ->
                                audience.showDialog(this.upgradeMenu(audience, upgrade)), Dialogue.CLICK_CALLBACK)).build())
                .toList();

        TagResolver click = Placeholder.component("click",
                Mini.parse(Econ.shouldBeUsed()
                                ? tl.mainPage().getClickValueIfEconEnabled()
                                : tl.mainPage().getClickValueIfEconDisabled(),
                        sender)
        );

        return Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Mini.parse(tl.mainPage().getTitle(), sender)).body(Dialogue.body(tl.mainPage().getBody(), sender, click)).build())
                .type(DialogType.multiAction(
                        upgrades,
                        ActionButton.builder(Mini.parse(tl.general().getDone(), sender)).build(),
                        2
                )));
    }

    private Dialog upgradeMenu(Audience audience, Upgrade upgrade) {
        FPlayer fPlayer = FPlayers.fPlayers().get((Player) audience);
        Faction faction = fPlayer.faction();
        if (!faction.isNormal()) {
            return this.noLongerInFaction(fPlayer);
        }

        if (!Universe.universe().isUpgradeEnabled(upgrade)) {
            return this.noLongerHaveAccess(fPlayer);
        }

        var tl = Confs.tl().commands().upgrades().paper();
        var info = tl.infoPage();

        int lvl = faction.upgradeLevel(upgrade);
        UpgradeSettings settings = Universe.universe().upgradeSettings(upgrade);
        boolean econ = Econ.shouldBeUsed();

        TextComponent.Builder builder = Component.text()
                .append(upgrade.nameComponent()).appendNewline().appendNewline()
                .append(upgrade.description()).appendNewline().appendNewline();
        if (lvl > 0 && settings.maxLevel() != 1) {
            builder.append(Mini.parse(info.getStatusCurrentLevel(), fPlayer, Placeholder.parsed("level", String.valueOf(lvl))));
        } else if (lvl == 1) {
            builder.append(Mini.parse(info.getStatusUnlocked(), fPlayer));
        } else {
            builder.append(Mini.parse(info.getStatusLocked(), fPlayer));
        }
        if (lvl > 0) {
            builder.appendNewline().appendNewline()
                    .append(upgrade.details(settings, lvl))
                    .appendNewline();
        }

        boolean prerequisitesMet = settings.prerequisitesMet(faction);

        if (lvl < settings.maxLevel()) {
            if (prerequisitesMet) {
                builder.appendNewline().appendNewline();
                if (econ) {
                    builder.append(Mini.parse(info.getUpgradeAvailable(), fPlayer)).appendNewline()
                            .append(Mini.parse(info.getUpgradeAvailableCosts(), fPlayer, Placeholder.parsed("cost", String.valueOf(settings.costAt(lvl + 1).doubleValue())))).appendNewline()
                            .appendNewline();
                }
                if (settings.maxLevel() > 1) {
                    builder.append(Mini.parse(info.getUpgradeAvailableLevelNumberIfNotSingleLevel(), fPlayer, Placeholder.parsed("level", String.valueOf(lvl + 1)))).appendNewline();
                }
                builder.append(upgrade.details(settings, lvl + 1));
            } else {
                builder.appendNewline().appendNewline()
                        .append(Mini.parse(info.getPrerequisitesHeader(), fPlayer));
                for (UpgradePrerequisite prerequisite : settings.prerequisites()) {
                    Upgrade required = UpgradeRegistry.getUpgrade(prerequisite.upgrade());
                    if (required == null || !Universe.universe().isUpgradeEnabled(required) || faction.upgradeLevel(required) < prerequisite.minLevel()) {
                        Component upgrd = Mini.parse(prerequisite.minLevel() > 1 ? info.getPrerequisiteEntryLevelOverOne() : info.getPrerequisiteEntryLevelOne(), fPlayer,
                                Placeholder.component("upgrade", required == null ? Component.text(prerequisite.upgrade()) : required.nameComponent()),
                                Placeholder.parsed("level", String.valueOf(prerequisite.minLevel())));
                        builder.appendNewline().append(Mini.parse(info.getPrerequisiteEntry(), fPlayer,
                                Placeholder.component("upgrade", upgrd)));
                    }
                }
            }
        } else if (lvl != 1) {
            builder.appendNewline()
                    .append(Mini.parse(info.getUpgradeAtMaxLevel(), fPlayer));
        }

        List<ActionButton> actions;
        if (lvl < settings.maxLevel() && econ && prerequisitesMet && Permission.UPGRADES.has((Player) audience) && faction.hasAccess(fPlayer, PermissibleActions.UPGRADE, null)) {
            actions = List.of(
                    ActionButton.builder(Mini.parse(info.getPurchaseButton(), fPlayer))
                            .action(DialogAction.customClick((_, aud) ->
                                    aud.showDialog(this.purchaseMenu(aud, upgrade)), Dialogue.CLICK_CALLBACK))
                            .build()
            );
        } else {
            actions = List.of();
        }

        return Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Mini.parse(info.getTitle(), fPlayer))
                        .body(List.of(
                                DialogBody.plainMessage(builder.build(), 400)
                        )).build())
                .type(actions.isEmpty() ?
                        DialogType.notice(ActionButton.builder(Mini.parse(tl.general().getReturnToList(), fPlayer))
                                .action(DialogAction.customClick((_, aud) ->
                                        aud.showDialog(this.mainMenu(aud)), Dialogue.CLICK_CALLBACK))
                                .build())
                        :
                        DialogType.multiAction(
                                actions,
                                this.returnToList(fPlayer),
                                2
                        )));
    }

    private Dialog purchaseMenu(Audience audience, Upgrade upgrade) {
        FPlayer fPlayer = FPlayers.fPlayers().get((Player) audience);
        Faction faction = fPlayer.faction();
        if (!faction.isNormal()) {
            return this.noLongerInFaction(fPlayer);
        }

        if (!Econ.shouldBeUsed() || !Permission.UPGRADES.has((Player) audience) || !faction.hasAccess(fPlayer, PermissibleActions.UPGRADE, null) || !Universe.universe().isUpgradeEnabled(upgrade)) {
            return this.noLongerHaveAccess(fPlayer);
        }

        int lvl = faction.upgradeLevel(upgrade);
        UpgradeSettings settings = Universe.universe().upgradeSettings(upgrade);

        var tl = Confs.tl().commands().upgrades().paper();

        if (lvl >= settings.maxLevel()) {
            return this.alreadyMax(upgrade, fPlayer);
        }

        if (!settings.prerequisitesMet(faction)) {
            return this.prerequisitesNotMet(upgrade, fPlayer);
        }

        double cost = settings.costAt(lvl + 1).doubleValue();

        if (this.sender((Player) audience, fPlayer).canAffordCommand(cost, "")) {
            return Dialog.create(b -> b.empty()
                    .base(DialogBase.builder(Mini.parse(tl.purchasePage().getTitle(), fPlayer))
                            .body(
                                    Dialogue.body(tl.purchasePage().getBody(), fPlayer,
                                            Placeholder.component("upgrade", upgrade.nameComponent()),
                                            Placeholder.parsed("cost", String.valueOf(cost)))
                            ).build())
                    .type(DialogType.multiAction(
                            List.of(
                                    ActionButton.builder(Mini.parse(tl.purchasePage().getConfirmButton(), fPlayer))
                                            .action(DialogAction.customClick((_, aud) ->
                                                    aud.showDialog(this.makePurchase(aud, upgrade, lvl + 1, cost)), Dialogue.CLICK_CALLBACK))
                                            .build()
                            ),
                            this.returnToUpgrade(upgrade, fPlayer),
                            2
                    )));
        } else {
            return this.cannotAfford(upgrade, fPlayer);
        }
    }

    private Dialog makePurchase(Audience audience, Upgrade upgrade, int newLvl, double confirmedCost) {
        FPlayer fPlayer = FPlayers.fPlayers().get((Player) audience);
        Faction faction = fPlayer.faction();
        if (!faction.isNormal()) {
            return this.noLongerInFaction(fPlayer);
        }

        if (!Econ.shouldBeUsed() || !Permission.UPGRADES.has((Player) audience) || !faction.hasAccess(fPlayer, PermissibleActions.UPGRADE, null) || !Universe.universe().isUpgradeEnabled(upgrade)) {
            return this.noLongerHaveAccess(fPlayer);
        }

        var tl = Confs.tl().commands().upgrades().paper();

        if (newLvl != (1 + faction.upgradeLevel(upgrade))) {
            return Dialog.create(b -> b.empty()
                    .base(DialogBase.builder(Mini.parse(tl.noLongerSameLevel().getTitle(), fPlayer))
                            .body(Dialogue.body(tl.noLongerSameLevel().getBody(), fPlayer)).build())
                    .type(DialogType.notice(this.returnToUpgrade(upgrade, fPlayer))));
        }

        UpgradeSettings settings = Universe.universe().upgradeSettings(upgrade);

        if (newLvl > settings.maxLevel()) {
            return this.alreadyMax(upgrade, fPlayer);
        }

        if (!settings.prerequisitesMet(faction)) {
            return this.prerequisitesNotMet(upgrade, fPlayer);
        }

        double cost = settings.costAt(newLvl).doubleValue();
        if (cost != confirmedCost) {
            return Dialog.create(b -> b.empty()
                    .base(DialogBase.builder(Mini.parse(tl.priceChanged().getTitle(), fPlayer))
                            .body(Dialogue.body(tl.priceChanged().getBody(), fPlayer,
                                    Placeholder.component("upgrade", upgrade.nameComponent()),
                                    Placeholder.parsed("old-cost", String.valueOf(confirmedCost)),
                                    Placeholder.parsed("cost", String.valueOf(cost)))).build())
                    .type(DialogType.notice(this.returnToUpgrade(upgrade, fPlayer))));
        }

        var econTl = Confs.tl().economy().actions();
        if (this.sender((Player) audience, fPlayer).payForCommand(cost, econTl.getUpgradeTo(), econTl.getUpgradeFor())) {
            faction.upgradeLevel(upgrade, newLvl);
            return Dialog.create(b -> b.empty()
                    .base(DialogBase.builder(Mini.parse(tl.purchaseComplete().getTitle(), fPlayer))
                            .body(Dialogue.body(tl.purchaseComplete().getBody(), fPlayer)).build())
                    .type(DialogType.notice(this.returnToUpgrade(upgrade, fPlayer))));
        } else {
            return this.cannotAfford(upgrade, fPlayer);
        }
    }

    private Sender.Player sender(Player player, FPlayer fPlayer) {
        return new Sender.Player.Impl(player, player, fPlayer, fPlayer.faction());
    }

    private Dialog noLongerInFaction(FPlayer fPlayer) {
        var tl = Confs.tl().commands().upgrades().paper().noLongerInFaction();
        return Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Mini.parse(tl.getTitle(), fPlayer))
                        .body(Dialogue.body(tl.getBody(), fPlayer)).build())
                .type(DialogType.notice()));
    }

    private Dialog noLongerHaveAccess(FPlayer fPlayer) {
        var tl = Confs.tl().commands().upgrades().paper().noLongerHaveAccess();
        return Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Mini.parse(tl.getTitle(), fPlayer))
                        .body(Dialogue.body(tl.getBody(), fPlayer)).build())
                .type(DialogType.notice()));
    }

    private Dialog alreadyMax(Upgrade upgrade, FPlayer fPlayer) {
        var tl = Confs.tl().commands().upgrades().paper().alreadyMax();
        return Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Mini.parse(tl.getTitle(), fPlayer))
                        .body(Dialogue.body(tl.getBody(), fPlayer)).build())
                .type(DialogType.notice(this.returnToUpgrade(upgrade, fPlayer))));
    }

    private Dialog cannotAfford(Upgrade upgrade, FPlayer fPlayer) {
        var tl = Confs.tl().commands().upgrades().paper().cannotAfford();
        return Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Mini.parse(tl.getTitle(), fPlayer))
                        .body(Dialogue.body(tl.getBody(), fPlayer)).build())
                .type(DialogType.notice(this.returnToUpgrade(upgrade, fPlayer))));
    }

    private Dialog prerequisitesNotMet(Upgrade upgrade, FPlayer fPlayer) {
        var tl = Confs.tl().commands().upgrades().paper().prerequisitesNotMet();
        return Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Mini.parse(tl.getTitle(), fPlayer))
                        .body(Dialogue.body(tl.getBody(), fPlayer)).build())
                .type(DialogType.notice(this.returnToUpgrade(upgrade, fPlayer))));
    }

    private ActionButton returnToUpgrade(Upgrade upgrade, FPlayer fPlayer) {
        return ActionButton.builder(Mini.parse(Confs.tl().commands().upgrades().paper().general().getReturnToInfo(), fPlayer))
                .action(DialogAction.customClick((_, aud) ->
                        aud.showDialog(this.upgradeMenu(aud, upgrade)), Dialogue.CLICK_CALLBACK))
                .build();
    }

    private ActionButton returnToList(FPlayer fPlayer) {
        return ActionButton.builder(Mini.parse(Confs.tl().commands().upgrades().paper().general().getReturnToList(), fPlayer))
                .action(DialogAction.customClick((_, aud) ->
                        aud.showDialog(this.mainMenu(aud)), Dialogue.CLICK_CALLBACK))
                .build();
    }
}
