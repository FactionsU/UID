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
import dev.kitteh.factions.util.Mini;
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
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.Comparator;
import java.util.List;
import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

@SuppressWarnings("UnstableApiUsage")
public class CmdUpgrades implements Cmd {
    private static final ClickCallback.Options OPT = ClickCallback.Options.builder().build();

    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().upgrades();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.UPGRADES).and(Cloudy.hasFaction())))
                            .commandDescription(Cloudy.desc(TL.COMMAND_UPGRADES_DESCRIPTION))
                            .handler(this::handle)
            );
        };
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

        var tl = FactionsPlugin.instance().tl().commands().upgrades().paper();

        List<ActionButton> upgrades = UpgradeRegistry.getUpgrades().stream()
                .sorted(Comparator.comparing(Upgrade::name))
                .filter(Universe.universe()::isUpgradeEnabled)
                .map(upgrade -> ActionButton.builder(upgrade.nameComponent())
                        .action(DialogAction.customClick((r, audience) ->
                                audience.showDialog(this.upgradeMenu(audience, upgrade)), OPT)).build())
                .toList();

        TagResolver click = Placeholder.component("click",
                Mini.parse(Econ.shouldBeUsed()
                        ? tl.mainPage().getClickValueIfEconEnabled()
                        : tl.mainPage().getClickValueIfEconDisabled())
        );

        return Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Mini.parse(tl.mainPage().getTitle())).body(this.body(tl.mainPage().getBody(), click)).build())
                .type(DialogType.multiAction(
                        upgrades,
                        ActionButton.builder(Mini.parse(tl.general().getDone())).build(),
                        2
                )));
    }

    private Dialog upgradeMenu(Audience audience, Upgrade upgrade) {
        FPlayer fPlayer = FPlayers.fPlayers().get((Player) audience);
        Faction faction = fPlayer.faction();
        if (!faction.isNormal()) {
            return this.noLongerInFaction();
        }

        var tl = FactionsPlugin.instance().tl().commands().upgrades().paper();
        var info = tl.infoPage();

        int lvl = faction.upgradeLevel(upgrade);
        UpgradeSettings settings = Universe.universe().upgradeSettings(upgrade);
        boolean econ = Econ.shouldBeUsed();

        TextComponent.Builder builder = Component.text()
                .append(upgrade.nameComponent()).appendNewline().appendNewline()
                .append(upgrade.description()).appendNewline().appendNewline();
        if (lvl > 0 && settings.maxLevel() != 1) {
            builder.append(Mini.parse(info.getStatusCurrentLevel(), Placeholder.parsed("level", String.valueOf(lvl))));
        } else if (lvl == 1) {
            builder.append(Mini.parse(info.getStatusUnlocked()));
        } else {
            builder.append(Mini.parse(info.getStatusLocked()));
        }
        if (lvl > 0) {
            builder.appendNewline().appendNewline()
                    .append(upgrade.details(settings, lvl))
                    .appendNewline();
        }

        if (econ) {
            if (lvl < settings.maxLevel()) {
                builder.appendNewline().appendNewline()
                        .append(Mini.parse(info.getUpgradeAvailable())).appendNewline()
                        .append(Mini.parse(info.getUpgradeAvailableCosts(), Placeholder.parsed("cost", String.valueOf(settings.costAt(lvl + 1).doubleValue())))).appendNewline()
                        .appendNewline();
                if (settings.maxLevel() > 1) {
                    builder.append(Mini.parse(info.getUpgradeAvailableLevelNumberIfNotSingleLevel(), Placeholder.parsed("level", String.valueOf(lvl + 1)))).appendNewline();
                }
                builder.append(upgrade.details(settings, lvl + 1));
            } else if (lvl != 1) {
                builder.appendNewline()
                        .append(Mini.parse(info.getUpgradeAtMaxLevel()));
            }
        }

        List<ActionButton> actions;
        if (lvl < settings.maxLevel() && econ && faction.hasAccess(fPlayer, PermissibleActions.UPGRADE, null)) {
            actions = List.of(
                    ActionButton.builder(Mini.parse(info.getPurchaseButton()))
                            .action(DialogAction.customClick((r, aud) ->
                                    aud.showDialog(this.purchaseMenu(aud, upgrade)), OPT))
                            .build()
            );
        } else {
            actions = List.of();
        }

        return Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Mini.parse(info.getTitle()))
                        .body(List.of(
                                DialogBody.plainMessage(builder.build(), 400)
                        )).build())
                .type(actions.isEmpty() ?
                        DialogType.notice(ActionButton.builder(Mini.parse(tl.general().getReturnToList()))
                                .action(DialogAction.customClick((r, aud) ->
                                        aud.showDialog(this.mainMenu(aud)), OPT))
                                .build())
                        :
                        DialogType.multiAction(
                                actions,
                                this.returnToList(),
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

        var tl = FactionsPlugin.instance().tl().commands().upgrades().paper();

        if (lvl == settings.maxLevel()) {
            return Dialog.create(b -> b.empty()
                    .base(DialogBase.builder(Mini.parse(tl.alreadyMax().getTitle()))
                            .body(this.body(tl.alreadyMax().getBody())).build())
                    .type(DialogType.notice(this.returnToUpgrade(upgrade))));
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
                    .base(DialogBase.builder(Mini.parse(tl.purchasePage().getTitle()))
                            .body(
                                    this.body(tl.purchasePage().getBody(),
                                            Placeholder.component("upgrade", upgrade.nameComponent()),
                                            Placeholder.parsed("cost", String.valueOf(cost)))
                            ).build())
                    .type(DialogType.multiAction(
                            List.of(
                                    ActionButton.builder(Mini.parse(tl.purchasePage().getConfirmButton()))
                                            .action(DialogAction.customClick((r, aud) ->
                                                    aud.showDialog(this.makePurchase(aud, upgrade, lvl + 1)), OPT))
                                            .build()
                            ),
                            this.returnToUpgrade(upgrade),
                            2
                    )));
        } else {
            return this.cannotAfford(upgrade);
        }
    }

    private Dialog makePurchase(Audience audience, Upgrade upgrade, int newLvl) {
        FPlayer fPlayer = FPlayers.fPlayers().get((Player) audience);
        Faction faction = fPlayer.faction();
        if (!faction.isNormal()) {
            return this.noLongerInFaction();
        }

        var tl = FactionsPlugin.instance().tl().commands().upgrades().paper();

        if (newLvl != (1 + faction.upgradeLevel(upgrade))) {
            return Dialog.create(b -> b.empty()
                    .base(DialogBase.builder(Mini.parse(tl.noLongerSameLevel().getTitle()))
                            .body(this.body(tl.noLongerSameLevel().getBody())).build())
                    .type(DialogType.notice(this.returnToUpgrade(upgrade))));
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
                    .base(DialogBase.builder(Mini.parse(tl.purchaseComplete().getTitle()))
                            .body(this.body(tl.purchaseComplete().getBody())).build())
                    .type(DialogType.notice(this.returnToUpgrade(upgrade))));
        } else {
            return this.cannotAfford(upgrade);
        }
    }

    private Dialog noLongerInFaction() {
        var tl = FactionsPlugin.instance().tl().commands().upgrades().paper().noLongerInFaction();
        return Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Mini.parse(tl.getTitle()))
                        .body(this.body(tl.getBody())).build())
                .type(DialogType.notice()));
    }

    private Dialog cannotAfford(Upgrade upgrade) {
        var tl = FactionsPlugin.instance().tl().commands().upgrades().paper();
        return Dialog.create(b -> b.empty()
                .base(DialogBase.builder(Mini.parse(tl.cannotAfford().getTitle()))
                        .body(this.body(tl.cannotAfford().getBody())).build())
                .type(DialogType.notice(this.returnToUpgrade(upgrade))));
    }

    private ActionButton returnToUpgrade(Upgrade upgrade) {
        return ActionButton.builder(Mini.parse(FactionsPlugin.instance().tl().commands().upgrades().paper().general().getReturnToInfo()))
                .action(DialogAction.customClick((r, aud) ->
                        aud.showDialog(this.upgradeMenu(aud, upgrade)), OPT))
                .build();
    }

    private ActionButton returnToList() {
        return ActionButton.builder(Mini.parse(FactionsPlugin.instance().tl().commands().upgrades().paper().general().getReturnToList()))
                .action(DialogAction.customClick((r, aud) ->
                        aud.showDialog(this.mainMenu(aud)), OPT))
                .build();
    }

    private List<DialogBody> body(List<String> body, TagResolver... tagResolvers) {
        return List.of(DialogBody.plainMessage(Mini.parse(body, tagResolvers), 400));
    }
}
