package dev.kitteh.factions.command.paper;

import dev.kitteh.factions.Universe;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.config.file.TranslationsConfig;
import dev.kitteh.factions.plugin.Instances;
import dev.kitteh.factions.upgrade.LeveledValueProvider;
import dev.kitteh.factions.upgrade.Upgrade;
import dev.kitteh.factions.upgrade.UpgradePrerequisite;
import dev.kitteh.factions.upgrade.UpgradeRegistry;
import dev.kitteh.factions.upgrade.UpgradeSettings;
import dev.kitteh.factions.upgrade.UpgradeVariable;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class CmdAdminUpgrades implements Cmd {
    // Should really be equation by this point...
    private static final int ARBITRARY_MAP_LIMIT = 50;

    private final Map<UUID, Draft> drafts = new ConcurrentHashMap<>();

    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = Confs.tl().commands().admin().upgrades();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.UPGRADES_MANAGE)))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        Player player = ((Sender.Player) context.sender()).player();
        this.drafts.remove(player.getUniqueId());
        player.showDialog(this.listMenu(player));
    }

    private static TranslationsConfig.Commands.Admin.Upgrades tl() {
        return Confs.tl().commands().admin().upgrades();
    }

    private static final class Draft {
        private final Map<Upgrade, DraftSettings> upgrades = new LinkedHashMap<>();
    }

    private Draft draft(Audience audience) {
        return this.drafts.computeIfAbsent(((Player) audience).getUniqueId(), _ -> new Draft());
    }

    private DraftSettings draftSettings(Audience audience, Upgrade upgrade) {
        return this.draft(audience).upgrades.computeIfAbsent(upgrade, DraftSettings::fresh);
    }

    private static class DraftSettings {
        private final Upgrade upgrade;
        private final boolean originalEnabled;
        private boolean enabled;
        private int maxLevel;
        private int startingLevel;
        private boolean settingsDirty;
        private final DraftProvider cost;
        private final Map<UpgradeVariable, DraftProvider> variables = new LinkedHashMap<>();
        private final List<UpgradePrerequisite> prerequisites = new ArrayList<>();

        private DraftSettings(Upgrade upgrade, boolean enabled, UpgradeSettings settings) {
            this.upgrade = upgrade;
            this.originalEnabled = enabled;
            this.enabled = enabled;
            this.maxLevel = settings.maxLevel();
            this.startingLevel = settings.startingLevel();
            this.cost = DraftProvider.from(settings.costProvider());
            for (UpgradeVariable variable : upgrade.variables()) {
                LeveledValueProvider provider = settings.variableProvider(variable);
                this.variables.put(variable, DraftProvider.from(provider));
            }
            this.prerequisites.addAll(settings.prerequisites());
        }

        private static DraftSettings fresh(Upgrade upgrade) {
            return new DraftSettings(upgrade, Universe.universe().isUpgradeEnabled(upgrade), Universe.universe().upgradeSettings(upgrade));
        }

        private boolean enabledChanged() {
            return this.enabled != this.originalEnabled;
        }

        private boolean changed() {
            return this.enabledChanged() || this.settingsDirty;
        }

        private UpgradeSettings toSettings() {
            Map<UpgradeVariable, LeveledValueProvider> vars = new LinkedHashMap<>();
            this.variables.forEach((variable, draftProvider) -> vars.put(variable, draftProvider.built()));
            return new UpgradeSettings(this.upgrade, vars, this.maxLevel, this.startingLevel, this.cost.built(), new ArrayList<>(this.prerequisites));
        }
    }

    private static final class DraftProvider {
        private boolean equation;
        private String expression = "level"; // Safe default
        private final TreeMap<Integer, BigDecimal> levels = new TreeMap<>();

        private static DraftProvider from(LeveledValueProvider source) {
            DraftProvider draftProvider = new DraftProvider();
            if (source instanceof LeveledValueProvider.Equation eq) {
                draftProvider.equation = true;
                draftProvider.expression = eq.expressionString();
            } else if (source instanceof LeveledValueProvider.LevelMap(Int2ObjectArrayMap<BigDecimal> lvls)) {
                draftProvider.equation = false;
                draftProvider.levels.putAll(lvls);
            }
            return draftProvider;
        }
        
        private LeveledValueProvider built() {
            if (this.equation) {
                return LeveledValueProvider.Equation.of(this.expression);
            }
            return LeveledValueProvider.LevelMap.of(new TreeMap<>(this.levels));
        }
    }

    private Dialog listMenu(Audience audience) {
        Draft draft = this.draft(audience);
        var tl = tl();
        var list = tl.listPage();

        List<ActionButton> buttons = new ArrayList<>(UpgradeRegistry.getUpgrades().stream()
                .sorted(Comparator.comparing(Upgrade::name))
                .map(upgrade -> {
                    DraftSettings draftSettings = draft.upgrades.get(upgrade);
                    boolean enabled = draftSettings != null ? draftSettings.enabled : Universe.universe().isUpgradeEnabled(upgrade);
                    String format = (draftSettings != null && draftSettings.changed())
                            ? list.getButtonModified()
                            : (enabled ? list.getButtonEnabled() : list.getButtonDisabled());
                    return ActionButton.builder(Mini.parse(format, Placeholder.component("upgrade", upgrade.nameComponent())))
                            .action(DialogAction.customClick((_, aud) -> aud.showDialog(this.editMenu(aud, upgrade)), Dialogue.CLICK_CALLBACK))
                            .build();
                })
                .toList());

        long changeCount = draft.upgrades.values().stream().filter(DraftSettings::changed).count();
        if (changeCount == 0) {
            buttons.add(ActionButton.builder(Mini.parse(list.getReviewButtonNone())).build());
        } else {
            buttons.add(ActionButton.builder(Mini.parse(list.getReviewButton(), Placeholder.parsed("count", String.valueOf(changeCount))))
                    .action(DialogAction.customClick((_, aud) -> aud.showDialog(this.reviewMenu(aud)), Dialogue.CLICK_CALLBACK))
                    .build());
        }

        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Mini.parse(list.getTitle()))
                        .body(Dialogue.body(list.getBody()))
                        .build())
                .type(DialogType.multiAction(buttons,
                        ActionButton.builder(Mini.parse(tl.general().getDone()))
                                .action(DialogAction.customClick((_, aud) -> aud.closeDialog(), Dialogue.CLICK_CALLBACK)).build(),
                        2)
                ));
    }

    private Dialog editMenu(Audience audience, Upgrade upgrade) {
        DraftSettings draftSettings = this.draftSettings(audience, upgrade);
        var tl = tl();
        var edit = tl.editPage();

        TagResolver upgradeTag = Placeholder.component("upgrade", upgrade.nameComponent());

        List<DialogBody> body = Dialogue.body(edit.getBody(),
                Placeholder.component("description", upgrade.description()),
                Placeholder.component("enabled", Mini.parse(draftSettings.enabled ? edit.getEnabledYes() : edit.getEnabledNo())),
                Placeholder.unparsed("maxlevel", String.valueOf(draftSettings.maxLevel)),
                Placeholder.unparsed("upgrademaxlevel", capLabel(upgrade.maxLevel())),
                Placeholder.unparsed("startinglevel", String.valueOf(draftSettings.startingLevel))
        );

        List<ActionButton> buttons = new ArrayList<>();
        buttons.add(ActionButton.builder(Mini.parse(draftSettings.enabled ? edit.getDisableButton() : edit.getEnableButton()))
                .action(DialogAction.customClick((_, aud) -> {
                    draftSettings.enabled = !draftSettings.enabled;
                    aud.showDialog(this.editMenu(aud, upgrade));
                }, Dialogue.CLICK_CALLBACK)).build());

        if (upgrade.maxLevel() > 1) {
            buttons.add(ActionButton.builder(Mini.parse(edit.getMaxLevelButton()))
                    .action(DialogAction.customClick((_, aud) -> aud.showDialog(this.maxLevelMenu(aud, upgrade)), Dialogue.CLICK_CALLBACK))
                    .build());
        }

        buttons.add(ActionButton.builder(Mini.parse(edit.getCostButton()))
                .action(DialogAction.customClick((_, aud) -> aud.showDialog(this.providerMenu(aud, upgrade, null)), Dialogue.CLICK_CALLBACK))
                .build());

        for (UpgradeVariable variable : draftSettings.variables.keySet()) {
            buttons.add(ActionButton.builder(Mini.parse(edit.getVariableButton(), Placeholder.unparsed("variable", variable.name())))
                    .action(DialogAction.customClick((_, aud) -> aud.showDialog(this.providerMenu(aud, upgrade, variable)), Dialogue.CLICK_CALLBACK))
                    .build());
        }

        buttons.add(ActionButton.builder(Mini.parse(edit.getPrerequisitesButton()))
                .action(DialogAction.customClick((_, aud) -> aud.showDialog(this.prerequisitesMenu(aud, upgrade)), Dialogue.CLICK_CALLBACK))
                .build());

        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Mini.parse(edit.getTitle(), upgradeTag))
                        .body(body)
                        .build())
                .type(DialogType.multiAction(buttons, this.backToList(), 2)));
    }

    private Dialog maxLevelMenu(Audience audience, Upgrade upgrade) {
        DraftSettings draftSettings = this.draftSettings(audience, upgrade);
        var tl = tl();
        var page = tl.maxLevelPage();

        TagResolver upgradeTag = Placeholder.component("upgrade", upgrade.nameComponent());
        TagResolver maxTag = Placeholder.unparsed("upgrademaxlevel", capLabel(upgrade.maxLevel()));

        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Mini.parse(page.getTitle(), upgradeTag))
                        .body(Dialogue.body(page.getBody(), maxTag))
                        .inputs(List.of(
                                DialogInput.text("maxlevel", Mini.parse(page.getInputLabel()))
                                        .width(200)
                                        .initial(String.valueOf(draftSettings.maxLevel))
                                        .maxLength(12)
                                        .build()
                        ))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(Mini.parse(page.getConfirm()))
                                .action(DialogAction.customClick((r, aud) -> {
                                    Integer value = parseInt(r.getText("maxlevel"));
                                    if (value == null || value < 1) {
                                        aud.showDialog(this.error(tl.error().getInvalidNumber(), () -> this.maxLevelMenu(aud, upgrade)));
                                        return;
                                    }
                                    int clamped = Math.min(value, upgrade.maxLevel());
                                    if (clamped != draftSettings.maxLevel) {
                                        draftSettings.maxLevel = clamped;
                                        if (draftSettings.startingLevel > clamped) {
                                            draftSettings.startingLevel = clamped;
                                        }
                                        draftSettings.settingsDirty = true;
                                    }
                                    aud.showDialog(this.editMenu(aud, upgrade));
                                }, Dialogue.CLICK_CALLBACK)).build(),
                        this.back(() -> this.editMenu(audience, upgrade))
                )));
    }

    private Dialog providerMenu(Audience audience, Upgrade upgrade, UpgradeVariable variable) {
        DraftSettings draftSettings = this.draftSettings(audience, upgrade);
        DraftProvider draftProvider = variable == null ? draftSettings.cost : draftSettings.variables.get(variable);
        var tl = tl();
        var page = tl.providerPage();

        String targetName = variable == null ? tl.general().getCostLabel() : variable.name();
        TagResolver targetTag = Placeholder.unparsed("target", targetName);
        TagResolver upgradeTag = Placeholder.component("upgrade", upgrade.nameComponent());
        TagResolver typeTag = Placeholder.unparsed("type", draftProvider.equation ? page.getTypeEquation() : page.getTypeLevelMap());

        List<ActionButton> buttons = new ArrayList<>();
        buttons.add(ActionButton.builder(Mini.parse(page.getEquationButton()))
                .action(DialogAction.customClick((_, aud) -> aud.showDialog(this.equationMenu(aud, upgrade, variable)), Dialogue.CLICK_CALLBACK)).build());
        buttons.add(ActionButton.builder(Mini.parse(page.getLevelMapButton()))
                .action(DialogAction.customClick((_, aud) -> {
                    if (draftSettings.maxLevel > ARBITRARY_MAP_LIMIT) {
                        aud.showDialog(this.error(tl.error().getMapLimitExceeded().replace("<limit>", String.valueOf(ARBITRARY_MAP_LIMIT)),
                                () -> this.providerMenu(aud, upgrade, variable)));
                        return;
                    }
                    aud.showDialog(this.levelMapMenu(aud, upgrade, variable));
                }, Dialogue.CLICK_CALLBACK)).build());

        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Mini.parse(page.getTitle(), upgradeTag, targetTag))
                        .body(Dialogue.body(page.getBody(), typeTag))
                        .build())
                .type(DialogType.multiAction(buttons, this.back(() -> this.editMenu(audience, upgrade)), 1)));
    }

    private Dialog equationMenu(Audience audience, Upgrade upgrade, UpgradeVariable variable) {
        DraftSettings draftSettings = this.draftSettings(audience, upgrade);
        DraftProvider draftProvider = variable == null ? draftSettings.cost : draftSettings.variables.get(variable);
        var tl = tl();
        var page = tl.equationPage();

        String targetName = variable == null ? tl.general().getCostLabel() : variable.name();
        TagResolver targetTag = Placeholder.unparsed("target", targetName);
        TagResolver upgradeTag = Placeholder.component("upgrade", upgrade.nameComponent());

        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Mini.parse(page.getTitle(), upgradeTag, targetTag))
                        .body(Dialogue.body(page.getBody(), upgradeTag, targetTag))
                        .inputs(List.of(
                                DialogInput.text("expression", Mini.parse(page.getInputLabel()))
                                        .width(300)
                                        .initial(draftProvider.expression)
                                        .maxLength(256)
                                        .build()
                        ))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(Mini.parse(page.getConfirm()))
                                .action(DialogAction.customClick((r, aud) -> {
                                    String expr = r.getText("expression");
                                    if (expr == null || expr.isBlank() || !LeveledValueProvider.Equation.isValidExpression(expr)) {
                                        aud.showDialog(this.error(tl.error().getInvalidEquation(), () -> this.equationMenu(aud, upgrade, variable)));
                                        return;
                                    }
                                    String trimmed = expr.trim();
                                    if (!draftProvider.equation || !draftProvider.expression.equals(trimmed)) {
                                        draftSettings.settingsDirty = true;
                                    }
                                    draftProvider.equation = true;
                                    draftProvider.expression = trimmed;
                                    aud.showDialog(this.editMenu(aud, upgrade));
                                }, Dialogue.CLICK_CALLBACK)).build(),
                        this.back(() -> this.providerMenu(audience, upgrade, variable))
                )));
    }

    private Dialog levelMapMenu(Audience audience, Upgrade upgrade, UpgradeVariable variable) {
        DraftSettings draftSettings = this.draftSettings(audience, upgrade);
        DraftProvider draftProvider = variable == null ? draftSettings.cost : draftSettings.variables.get(variable);
        var tl = tl();
        var page = tl.levelMapPage();

        String targetName = variable == null ? tl.general().getCostLabel() : variable.name();
        TagResolver targetTag = Placeholder.unparsed("target", targetName);
        TagResolver upgradeTag = Placeholder.component("upgrade", upgrade.nameComponent());

        LeveledValueProvider sample = draftProvider.built();
        int levels = Math.min(draftSettings.maxLevel, ARBITRARY_MAP_LIMIT);
        List<DialogInput> inputs = new ArrayList<>();
        for (int level = 1; level <= levels; level++) {
            BigDecimal current = (!draftProvider.equation && draftProvider.levels.containsKey(level))
                    ? draftProvider.levels.get(level)
                    : sample.get(level);
            inputs.add(DialogInput.text("level_" + level, Mini.parse(page.getInputLabel(), Placeholder.unparsed("level", String.valueOf(level))))
                    .width(200)
                    .initial(current == null ? "0" : current.toPlainString())
                    .maxLength(24)
                    .build());
        }

        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Mini.parse(page.getTitle(), upgradeTag, targetTag))
                        .body(Dialogue.body(page.getBody(), upgradeTag, targetTag, Placeholder.unparsed("maxlevel", String.valueOf(draftSettings.maxLevel))))
                        .inputs(inputs)
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(Mini.parse(page.getConfirm()))
                                .action(DialogAction.customClick((view, aud) -> {
                                    TreeMap<Integer, BigDecimal> parsed = new TreeMap<>();
                                    for (int level = 1; level <= levels; level++) {
                                        BigDecimal value = parseDecimal(view.getText("level_" + level));
                                        if (value == null) {
                                            aud.showDialog(this.error(tl.error().getInvalidNumber(), () -> this.levelMapMenu(aud, upgrade, variable)));
                                            return;
                                        }
                                        parsed.put(level, value);
                                    }
                                    if (draftProvider.equation || !draftProvider.levels.equals(parsed)) {
                                        draftSettings.settingsDirty = true;
                                    }
                                    draftProvider.equation = false;
                                    draftProvider.levels.clear();
                                    draftProvider.levels.putAll(parsed);
                                    aud.showDialog(this.editMenu(aud, upgrade));
                                }, Dialogue.CLICK_CALLBACK)).build(),
                        this.back(() -> this.providerMenu(audience, upgrade, variable))
                )));
    }

    private Dialog prerequisitesMenu(Audience audience, Upgrade upgrade) {
        DraftSettings draftSettings = this.draftSettings(audience, upgrade);
        var tl = tl();
        var page = tl.prerequisitesPage();

        TagResolver upgradeTag = Placeholder.component("upgrade", upgrade.nameComponent());

        List<ActionButton> buttons = new ArrayList<>();
        List<DialogBody> body;
        if (draftSettings.prerequisites.isEmpty()) {
            body = Dialogue.body(page.getBodyNone());
        } else {
            Component component = Mini.parse(page.getBodyHeader(), upgradeTag);
            for (UpgradePrerequisite prerequisite : draftSettings.prerequisites) {
                Component requiredName = prerequisiteName(prerequisite);
                TagResolver requiredTag = Placeholder.component("upgrade", requiredName);
                TagResolver levelTag = Placeholder.unparsed("level", String.valueOf(prerequisite.minLevel()));
                component = component.appendNewline().append(Mini.parse(page.getLineEntry(), requiredTag, levelTag));
                buttons.add(ActionButton.builder(Mini.parse(page.getRemoveButton(), requiredTag, levelTag))
                        .action(DialogAction.customClick((_, aud) -> {
                            draftSettings.prerequisites.remove(prerequisite);
                            draftSettings.settingsDirty = true;
                            aud.showDialog(this.prerequisitesMenu(aud, upgrade));
                        }, Dialogue.CLICK_CALLBACK))
                        .build());
            }
            body = List.of(DialogBody.plainMessage(component, 400));
        }

        buttons.add(ActionButton.builder(Mini.parse(page.getAddButton()))
                .action(DialogAction.customClick((_, aud) -> aud.showDialog(this.addPrerequisiteMenu(aud, upgrade)), Dialogue.CLICK_CALLBACK))
                .build());

        List<DialogBody> finalBody = body;
        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Mini.parse(page.getTitle(), upgradeTag))
                        .body(finalBody)
                        .build())
                .type(DialogType.multiAction(buttons, this.back(() -> this.editMenu(audience, upgrade)), 1)));
    }

    private Dialog addPrerequisiteMenu(Audience audience, Upgrade upgrade) {
        DraftSettings draftSettings = this.draftSettings(audience, upgrade);
        var tl = tl();
        var page = tl.addPrerequisitePage();

        TagResolver upgradeTag = Placeholder.component("upgrade", upgrade.nameComponent());

        List<ActionButton> buttons = UpgradeRegistry.getUpgrades().stream()
                .sorted(Comparator.comparing(Upgrade::name))
                .filter(candidate -> candidate != upgrade
                        && draftSettings.prerequisites.stream().noneMatch(p -> p.upgrade().equalsIgnoreCase(candidate.name())))
                .map(candidate -> ActionButton.builder(Mini.parse(page.getEntryButton(), Placeholder.component("upgrade", candidate.nameComponent())))
                        .action(DialogAction.customClick((_, aud) -> aud.showDialog(this.prerequisiteLevelMenu(aud, upgrade, candidate)), Dialogue.CLICK_CALLBACK))
                        .build())
                .toList();

        List<DialogBody> body = buttons.isEmpty() ? Dialogue.body(page.getBodyNone()) : Dialogue.body(page.getBody());

        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Mini.parse(page.getTitle(), upgradeTag))
                        .body(body)
                        .build())
                .type(DialogType.multiAction(buttons, this.back(() -> this.prerequisitesMenu(audience, upgrade)), 2)));
    }

    private Dialog prerequisiteLevelMenu(Audience audience, Upgrade upgrade, Upgrade required) {
        var tl = tl();
        var page = tl.prerequisiteLevelPage();

        TagResolver upgradeTag = Placeholder.component("upgrade", required.nameComponent());
        TagResolver parentTag = Placeholder.component("parent", upgrade.nameComponent());
        TagResolver maxTag = Placeholder.unparsed("upgrademaxlevel", capLabel(required.maxLevel()));

        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Mini.parse(page.getTitle(), upgradeTag, parentTag))
                        .body(Dialogue.body(page.getBody(), upgradeTag, parentTag, maxTag))
                        .inputs(List.of(
                                DialogInput.text("level", Mini.parse(page.getInputLabel()))
                                        .width(200)
                                        .initial("1")
                                        .maxLength(12)
                                        .build()
                        ))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(Mini.parse(page.getConfirm()))
                                .action(DialogAction.customClick((r, aud) -> {
                                    Integer value = parseInt(r.getText("level"));
                                    if (value == null || value < 1) {
                                        aud.showDialog(this.error(tl.error().getInvalidNumber(), () -> this.prerequisiteLevelMenu(aud, upgrade, required)));
                                        return;
                                    }
                                    int clamped = Math.min(value, required.maxLevel());
                                    DraftSettings draftSettings = this.draftSettings(aud, upgrade);
                                    draftSettings.prerequisites.removeIf(p -> p.upgrade().equalsIgnoreCase(required.name()));
                                    draftSettings.prerequisites.add(new UpgradePrerequisite(required.name(), clamped));
                                    draftSettings.settingsDirty = true;
                                    aud.showDialog(this.prerequisitesMenu(aud, upgrade));
                                }, Dialogue.CLICK_CALLBACK)).build(),
                        this.back(() -> this.addPrerequisiteMenu(audience, upgrade))
                )));
    }

    private static Component prerequisiteName(UpgradePrerequisite prerequisite) {
        Upgrade required = UpgradeRegistry.getUpgrade(prerequisite.upgrade());
        return required == null ? Component.text(prerequisite.upgrade()) : required.nameComponent();
    }

    private Dialog reviewMenu(Audience audience) {
        Draft draft = this.draft(audience);
        var tl = tl();
        var page = tl.reviewPage();

        List<DraftSettings> changed = draft.upgrades.values().stream().filter(DraftSettings::changed).toList();

        if (changed.isEmpty()) {
            return Dialog.create(builder -> builder.empty()
                    .base(DialogBase.builder(Mini.parse(page.getTitle()))
                            .body(Dialogue.body(page.getBodyNone()))
                            .build())
                    .type(DialogType.notice(this.backToList())));
        }

        Component body = Mini.parse(page.getBodyHeader());
        for (DraftSettings draftSettings : changed) {
            TagResolver upgradeTag = Placeholder.component("upgrade", draftSettings.upgrade.nameComponent());
            if (draftSettings.enabledChanged()) {
                body = body.appendNewline().append(Mini.parse(page.getLineEnabled(), upgradeTag,
                        Placeholder.component("from", Mini.parse(draftSettings.originalEnabled ? tl.editPage().getEnabledYes() : tl.editPage().getEnabledNo())),
                        Placeholder.component("to", Mini.parse(draftSettings.enabled ? tl.editPage().getEnabledYes() : tl.editPage().getEnabledNo()))));
            }
            if (draftSettings.settingsDirty) {
                body = body.appendNewline().append(Mini.parse(page.getLineSettings(), upgradeTag));
            }
        }

        Component finalBody = body;
        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Mini.parse(page.getTitle()))
                        .body(List.of(DialogBody.plainMessage(finalBody, 400)))
                        .build())
                .type(DialogType.multiAction(
                        List.of(
                                ActionButton.builder(Mini.parse(page.getApplyButton()))
                                        .action(DialogAction.customClick((_, aud) -> aud.showDialog(this.apply(aud)), Dialogue.CLICK_CALLBACK))
                                        .build(),
                                ActionButton.builder(Mini.parse(page.getReEditButton()))
                                        .action(DialogAction.customClick((_, aud) -> aud.showDialog(this.listMenu(aud)), Dialogue.CLICK_CALLBACK))
                                        .build(),
                                ActionButton.builder(Mini.parse(page.getCancelButton()))
                                        .action(DialogAction.customClick((_, aud) -> {
                                            this.drafts.remove(((Player) aud).getUniqueId());
                                            aud.closeDialog();
                                        }, Dialogue.CLICK_CALLBACK))
                                        .build()
                        ),
                        this.backToList(),
                        1
                )));
    }

    private Dialog apply(Audience audience) {
        Draft draft = this.draft(audience);
        var tl = tl();

        List<DraftSettings> changed = draft.upgrades.values().stream().filter(DraftSettings::changed).toList();

        // Validate everything before applying anything, so a flaw cannot leave a partial apply.
        for (DraftSettings draftSettings : changed) {
            if (draftSettings.settingsDirty) {
                try {
                    draftSettings.toSettings();
                } catch (IllegalArgumentException e) {
                    return this.error(draftSettings.upgrade.name() + ": " + e.getMessage(), () -> this.reviewMenu(audience));
                }
            }
        }

        int count = 0;
        for (DraftSettings draftSettings : changed) {
            if (draftSettings.settingsDirty) {
                Universe.universe().upgradeSettings(draftSettings.toSettings());
            }
            if (draftSettings.enabledChanged()) {
                Universe.universe().upgradeEnabled(draftSettings.upgrade, draftSettings.enabled);
            }
            count++;
        }

        if (count > 0) {
            Instances.UNIVERSE.forceSave(false);
        }

        this.drafts.remove(((Player) audience).getUniqueId());

        int finalCount = count;
        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Mini.parse(tl.applied().getTitle()))
                        .body(Dialogue.body(tl.applied().getBody(), Placeholder.unparsed("count", String.valueOf(finalCount))))
                        .build())
                .type(DialogType.notice(ActionButton.builder(Mini.parse(tl.general().getDone())).build())));
    }

    private Dialog error(String reason, Supplier<Dialog> back) {
        var tl = tl();
        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Mini.parse(tl.error().getTitle()))
                        .body(Dialogue.body(tl.error().getBody(), Placeholder.unparsed("reason", reason)))
                        .build())
                .type(DialogType.notice(this.back(back))));
    }

    private ActionButton backToList() {
        return ActionButton.builder(Mini.parse(tl().general().getReturnToList()))
                .action(DialogAction.customClick((_, aud) -> aud.showDialog(this.listMenu(aud)), Dialogue.CLICK_CALLBACK))
                .build();
    }

    private ActionButton back(Supplier<Dialog> target) {
        return ActionButton.builder(Mini.parse(tl().general().getBack()))
                .action(DialogAction.customClick((_, aud) -> aud.showDialog(target.get()), Dialogue.CLICK_CALLBACK))
                .build();
    }

    private static String capLabel(int max) {
        return max == Integer.MAX_VALUE ? tl().editPage().getUnlimited() : String.valueOf(max);
    }

    private static Integer parseInt(String text) {
        if (text == null) {
            return null;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static BigDecimal parseDecimal(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
