package dev.kitteh.factions.command.defaults.set;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.file.PermissionsConfig;
import dev.kitteh.factions.config.file.TranslationsConfig;
import dev.kitteh.factions.data.MemoryFaction;
import dev.kitteh.factions.permissible.PermSelector;
import dev.kitteh.factions.permissible.PermSelectorRegistry;
import dev.kitteh.factions.permissible.PermState;
import dev.kitteh.factions.permissible.PermissibleAction;
import dev.kitteh.factions.permissible.PermissibleActionRegistry;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.permissible.selector.UnknownSelector;
import dev.kitteh.factions.upgrade.Upgrade;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.MiscUtil;
import dev.kitteh.factions.util.Permission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.ChatColor;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CmdSetPerm implements Cmd {
    private final Function<CommandContext<Sender>, String> firstCmdBit;
    private final Function<CommandContext<Sender>, Faction.Permissions> permissionsGetter;
    private final Resetter resetter;

    public interface Resetter {
        boolean reset(CommandContext<Sender> context);
    }

    public CmdSetPerm() {
        this.firstCmdBit = (ctx) -> '/' + MiscUtil.commandRoot() + ' ' +
                FactionsPlugin.instance().tl().commands().set().getFirstAlias() + ' ' +
                FactionsPlugin.instance().tl().commands().permissions().getFirstAlias() + ' ';
        this.permissionsGetter = context -> ((Sender.Player) context.sender()).faction().permissions();
        this.resetter = context -> {
            ((MemoryFaction) ((Sender.Player) context.sender()).faction()).resetPerms();
            return true;
        };
    }

    public CmdSetPerm(Function<CommandContext<Sender>, String> firstCmdBit, Function<CommandContext<Sender>, Faction.Permissions> permissionsGetter, Resetter resetter) {
        this.firstCmdBit = firstCmdBit;
        this.permissionsGetter = permissionsGetter;
        this.resetter = resetter;
    }

    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().permissions();
            List<String> aliases = new ArrayList<>(tl.getAliases());
            Command.Builder<Sender> permBuilder = builder.literal(aliases.removeFirst(), aliases.toArray(new String[0]))
                    .commandDescription(Cloudy.desc(tl.getDescription()))
                    .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.PERMISSIONS).and(Cloudy.isAtLeastRole(Role.ADMIN))));

            manager.command(permBuilder.handler(this::handleList));

            List<String> addAliases = new ArrayList<>(tl.add().getAliases());
            manager.command(
                    permBuilder.literal(addAliases.removeFirst(), addAliases.toArray(new String[0]))
                            .optional("selector", StringParser.quotedStringParser())
                            .optional("perm", StringParser.stringParser())
                            .optional("allowdeny", StringParser.stringParser())
                            .handler(this::handleAdd)
            );

            List<String> listAliases = new ArrayList<>(tl.list().getAliases());
            manager.command(permBuilder.literal(listAliases.removeFirst(), listAliases.toArray(new String[0])).handler(this::handleList));

            List<String> listOverrideAliases = new ArrayList<>(tl.listOverride().getAliases());
            manager.command(permBuilder.literal(listOverrideAliases.removeFirst(), listOverrideAliases.toArray(new String[0])).handler(this::handleListOverrides));

            List<String> moveAliases = new ArrayList<>(tl.move().getAliases());
            List<String> moveChoices = new ArrayList<>(tl.move().getAliasUp());
            moveChoices.addAll(tl.move().getAliasDown());
            manager.command(
                    permBuilder.literal(moveAliases.removeFirst(), moveAliases.toArray(new String[0]))
                            .required("position", IntegerParser.integerParser(1))
                            .required("direction", StringParser.stringParser(), SuggestionProvider.suggestingStrings(moveChoices))
                            .handler(this::handleMove)
            );

            List<String> removeAliases = new ArrayList<>(tl.remove().getAliases());
            manager.command(
                    permBuilder.literal(removeAliases.removeFirst(), removeAliases.toArray(new String[0]))
                            .required("selector", StringParser.quotedStringParser())
                            .optional("perm", StringParser.stringParser())
                            .handler(this::handleRemove)
            );

            List<String> resetAliases = new ArrayList<>(tl.reset().getAliases());
            manager.command(
                    permBuilder.literal(resetAliases.removeFirst(), resetAliases.toArray(new String[0]))
                            .optional("confirm", StringParser.stringParser())
                            .handler(this::handleReset)
            );

            List<String> showAliases = new ArrayList<>(tl.show().getAliases());
            manager.command(
                    permBuilder.literal(showAliases.removeFirst(), showAliases.toArray(new String[0]))
                            .required("selector", StringParser.quotedStringParser())
                            .handler(this::handleShow)
            );

            List<String> showOverrideAliases = new ArrayList<>(tl.showOverride().getAliases());
            manager.command(
                    permBuilder.literal(showOverrideAliases.removeFirst(), showOverrideAliases.toArray(new String[0]))
                            .required("selector", StringParser.quotedStringParser())
                            .handler(this::handleShowOverride)
            );
        };
    }

    private void handleList(CommandContext<Sender> context) {
        Faction.Permissions permissions = this.permissionsGetter.apply(context);
        if (permissions == null) {
            return;
        }
        listSelectors(context, ((Sender.Player) context.sender()).faction(), context.sender(), permissions, FactionsPlugin.instance().tl().commands().permissions());
    }

    private void handleListOverrides(CommandContext<Sender> context) {
        listOverrideSelectors(context, ((Sender.Player) context.sender()).faction(), context.sender(), FactionsPlugin.instance().tl().commands().permissions());
    }

    private void handleAdd(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().permissions();
        Faction faction = ((Sender.Player) context.sender()).faction();
        Faction.Permissions permissions = this.permissionsGetter.apply(context);
        if (permissions == null) {
            return;
        }

        Optional<String> argSelector = context.optional("selector");
        Optional<String> argAction = context.optional("perm");
        Optional<String> argAllowDeny = context.optional("allowdeny");

        if (argSelector.isEmpty()) {
            this.handleAddDefault(context, context.sender(), tl);
        } else if (argAction.isEmpty()) {
            this.handleAddSelector(context, argSelector.get(), context.sender(), faction, permissions, tl);
        } else if (argAllowDeny.isEmpty()) {
            context.sender().sendRichMessage(tl.add().getActionAllowDenyOptions(),
                    Placeholder.unparsed("allow", tl.add().getActionAllowAlias().getFirst()),
                    Placeholder.unparsed("deny", tl.add().getActionDenyAlias().getFirst()));
        } else {
            this.handleAddPerm(context, argSelector.get(), argAction.get(), argAllowDeny.get(), context.sender(), faction, permissions, tl);
        }
    }

    private void handleAddDefault(CommandContext<Sender> context, Sender sender, TranslationsConfig.Commands.Permissions tl) {
        List<String> selectors = new ArrayList<>(PermSelectorRegistry.getSelectors());
        Collections.sort(selectors);

        ComponentBuilder<TextComponent, TextComponent.Builder> build = Component.text();
        build.append(Mini.parse(tl.add().getAvailableSelectorsIntro()));
        ChatColor.stripColor(Mini.toLegacy(build.build()));
        int x = length(build);

        String commandPiece = this.firstCmdBit.apply(context) + tl.add().getAliases().getFirst() + ' ';

        for (String selector : selectors) {
            build.append(Mini.parse(tl.add().getAvailableSelectorsSelector(),
                    Placeholder.parsed("command", commandPiece + "\"" + selector + "\""),
                    Placeholder.unparsed("selector", selector)
            ));
            x = length(build);
            if (x >= 50) {
                x = 0;
                sender.sendMessage(build);
                build = Component.text();
            }
        }
        if (x > 0) {
            sender.sendMessage(build);
        }
    }

    private void handleAddSelector(CommandContext<Sender> context, String target, Sender sender, Faction faction, Faction.Permissions permissions, TranslationsConfig.Commands.Permissions tl) {
        PermSelector selector = null;
        String ex = null;
        try {
            selector = PermSelectorRegistry.createOrThrow(target);
        } catch (Exception e) {
            ex = e.getMessage();
        }
        glass:
        if (selector == null) {
            PermSelector.Descriptor descriptor = PermSelectorRegistry.getDescriptor(target);
            if (descriptor == null) {
                if (target.contains(":")) {
                    sender.sendRichMessage(tl.add().getSelectorCreateFail(),
                            Placeholder.unparsed("error", ex == null ? "???" : ex));
                } else {
                    sender.sendRichMessage(tl.add().getSelectorNotFound());
                }
            } else {
                if (descriptor.acceptsEmpty()) {
                    selector = descriptor.create("");
                    break glass;
                }
                Map<String, String> options = descriptor.options(faction);
                if (descriptor.instructions() instanceof String instructions) {
                    sender.sendMessage(Component.text(instructions));
                    sender.sendRichMessage(this.firstCmdBit.apply(context) + tl.add().getAliases().getFirst() + " \"" + descriptor.name() + ':' + tl.add().getSelectorOptionHere() + "\"");
                }
                if (options != null) {
                    ComponentBuilder<TextComponent, TextComponent.Builder> build = Component.text();
                    build.append(Mini.parse(tl.add().getSelectorOptionsIntro()));
                    int x = length(build);

                    String commandPiece = this.firstCmdBit.apply(context) + tl.add().getAliases().getFirst() + ' ';

                    for (Map.Entry<String, String> entry : options.entrySet()) {
                        build.append(Mini.parse(tl.add().getSelectorOptionsItem(),
                                Placeholder.parsed("command", commandPiece + '"' + entry.getKey() + '"'),
                                Placeholder.unparsed("display", entry.getValue())));
                        x = length(build);
                        if (x >= 50) {
                            x = 0;
                            sender.sendMessage(build);
                            build = Component.text();
                        }
                    }
                    if (x > 0) {
                        sender.sendMessage(build);
                    }
                }
            }
            return;
        }
        if (permissions.has(selector)) { // List options to add to this selector
            List<String> actions = PermissibleActionRegistry.getActions().stream()
                    // Filter out unfulfilled prerequisites
                    .filter(action -> !(action.prerequisite() instanceof Upgrade prereq) || faction.upgradeLevel(prereq) > 0)
                    .map(PermissibleAction::name).collect(Collectors.toCollection(ArrayList::new));
            // Remove hidden actions
            actions.removeAll(FactionsPlugin.instance().configManager().permissionsConfig().getHiddenActions());
            // Remove actions already in use
            actions.removeAll(permissions.get(selector).actions());
            Collections.sort(actions);

            ComponentBuilder<TextComponent, TextComponent.Builder> build = Component.text();
            build.append(Mini.parse(tl.add().getActionOptionsIntro()));
            int x = length(build);

            String commandPiece = this.firstCmdBit.apply(context) + tl.add().getAliases().getFirst() + " \"" + selector.serialize() + "\" ";

            for (String action : actions) {
                build.append(Mini.parse(tl.add().getActionOptionsItem(),
                        Placeholder.unparsed("description", PermissibleActionRegistry.get(action) instanceof PermissibleAction a ? a.description() : "???"),
                        Placeholder.unparsed("action", action),
                        Placeholder.parsed("commandtrue", commandPiece + action + ' ' + tl.add().getActionAllowAlias().getFirst()),
                        Placeholder.parsed("commandfalse", commandPiece + action + ' ' + tl.add().getActionDenyAlias().getFirst())));
                x = length(build);
                if (x >= 50) {
                    x = 0;
                    sender.sendMessage(build);
                    build = Component.text();
                }
            }
            if (x > 0) {
                sender.sendMessage(build);
            }
        } else { // Add selector
            permissions.add(selector);
            listSelectors(context, faction, sender, permissions, tl);
        }
    }

    private void handleAddPerm(CommandContext<Sender> context, String argSelector, String argAction, String choice, Sender sender, Faction faction, Faction.Permissions permissions, TranslationsConfig.Commands.Permissions tl) {
        PermSelector selector = PermSelectorRegistry.create(argSelector, false);
        if (selector instanceof UnknownSelector) {
            sender.sendRichMessage(tl.add().getSelectorNotFound());
        } else if (!permissions.has(selector)) {
            sender.sendRichMessage(tl.add().getSelectorNotFound());
        } else {
            PermissibleAction action = PermissibleActionRegistry.get(argAction);
            if (action == null || FactionsPlugin.instance().configManager().permissionsConfig().getHiddenActions().contains(action.name())) {
                sender.sendRichMessage(tl.add().getActionNotFound());
            } else {
                Boolean allow = null;
                if (tl.add().getActionAllowAlias().stream().anyMatch(i -> i.equalsIgnoreCase(choice))) {
                    allow = true;
                } else if (tl.add().getActionDenyAlias().stream().anyMatch(i -> i.equalsIgnoreCase(choice))) {
                    allow = false;
                }
                if (allow == null) {
                    sender.sendRichMessage(tl.add().getActionAllowDenyOptions(),
                            Placeholder.unparsed("allow", tl.add().getActionAllowAlias().getFirst()),
                            Placeholder.unparsed("deny", tl.add().getActionDenyAlias().getFirst()));
                    return;
                }
                permissions.add(selector).set(action, PermState.of(allow));
                showSelector(context, faction, -1, selector, sender, permissions, tl);
            }
        }
    }

    private void handleMove(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().permissions();
        Faction faction = ((Sender.Player) context.sender()).faction();
        Faction.Permissions permissions = this.permissionsGetter.apply(context);
        if (permissions == null) {
            return;
        }

        int position = context.get("position");
        position--; // User input -> Index
        String direction = context.get("direction");
        boolean up = false;
        if (position < 0 || position >= permissions.selectors().size()) {
            context.sender().sendRichMessage(tl.move().getErrorInvalidPosition());
            return;
        }
        PermSelector selector = permissions.selectors().get(position);

        int newPos;
        if (tl.move().getAliasUp().stream().anyMatch(i -> i.equalsIgnoreCase(direction))) {
            up = true;
            newPos = position - 1;
        } else if (tl.move().getAliasDown().stream().anyMatch(i -> i.equalsIgnoreCase(direction))) {
            newPos = position + 1;
        } else {
            context.sender().sendRichMessage(tl.move().getErrorOptions(),
                    Placeholder.unparsed("up", tl.move().getAliasUp().getFirst()),
                    Placeholder.unparsed("down", tl.move().getAliasDown().getFirst()));
            return;
        }
        if (newPos < 0) {
            context.sender().sendRichMessage(tl.move().getErrorHighest());
        } else if (newPos > permissions.selectors().size() - 1) {
            context.sender().sendRichMessage(tl.move().getErrorLowest());
        } else {
            if (up) {
                permissions.moveSelectorUp(selector);
            } else {
                permissions.moveSelectorDown(selector);
            }
        }
        listSelectors(context, faction, context.sender(), permissions, tl);
    }

    private void handleRemove(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().permissions();
        Faction faction = ((Sender.Player) context.sender()).faction();
        Faction.Permissions permissions = this.permissionsGetter.apply(context);
        if (permissions == null) {
            return;
        }

        String argSelector = context.get("selector");
        Optional<String> argAction = context.optional("perm");

        if (argAction.isEmpty()) {
            PermSelector selector = PermSelectorRegistry.create(argSelector, false);
            permissions.remove(selector);
            listSelectors(context, faction, context.sender(), permissions, tl);
        } else {
            PermSelector selector = PermSelectorRegistry.create(argSelector, false);
            if (permissions.has(selector)) {
                permissions.get(selector).set(argAction.get(), PermState.UNSET);
            }
            showSelector(context, faction, -1, selector, context.sender(), permissions, tl);
        }
    }

    private void handleReset(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().permissions();

        Optional<String> confirm = context.optional("confirm");

        if (confirm.isPresent() && confirm.get().equalsIgnoreCase(tl.reset().getConfirmWord())) {
            if (this.resetter.reset(context)) {
                context.sender().sendRichMessage(tl.reset().getResetComplete());
            }
        } else {
            String cmd = this.firstCmdBit.apply(context) + tl.reset().getAliases().getFirst() + ' ' + tl.reset().getConfirmWord();
            context.sender().sendRichMessage(tl.reset().getWarning(),
                    Placeholder.parsed("command", cmd));
        }
    }

    private void handleShow(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().permissions();
        Faction faction = ((Sender.Player) context.sender()).faction();
        Faction.Permissions permissions = this.permissionsGetter.apply(context);
        if (permissions == null) {
            return;
        }

        String argSelector = context.get("selector");

        int index = -1;
        try {
            index = Integer.parseInt(argSelector);
        } catch (NumberFormatException ignored) {
        }

        PermSelector selector = null;
        if (index < 1) {
            selector = PermSelectorRegistry.create(argSelector, false);
        }
        showSelector(context, faction, index, selector, context.sender(), permissions, tl);
    }

    private void handleShowOverride(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().permissions();
        Faction faction = ((Sender.Player) context.sender()).faction();

        String argSelector = context.get("selector");

        int index = -1;
        try {
            index = Integer.parseInt(argSelector);
        } catch (NumberFormatException ignored) {
        }

        PermSelector selector = null;
        if (index < 1) {
            selector = PermSelectorRegistry.create(argSelector, false);
        }
        showOverrideSelector(faction, index, selector, context.sender(), tl);
    }

    private static int length(ComponentBuilder<TextComponent, TextComponent.Builder> builder) {
        return ChatColor.stripColor(Mini.toLegacy(builder.build())).length();
    }

    private void listSelectors(CommandContext<Sender> context, Faction faction, Sender sender, Faction.Permissions permissions, TranslationsConfig.Commands.Permissions tl) {
        int x = 0;
        String commandPiece = this.firstCmdBit.apply(context);
        String movePiece = tl.move().getAliases().getFirst() + ' ';
        String removePiece = tl.remove().getAliases().getFirst() + ' ';
        String showPiece = tl.show().getAliases().getFirst() + ' ';
        if (!tl.list().getHeader().isEmpty()) {
            sender.sendRichMessage(tl.list().getHeader(),
                    Placeholder.parsed("commandadd", commandPiece + tl.add().getAliases().getFirst()),
                    Placeholder.parsed("commandoverride", commandPiece + tl.listOverride().getAliases().getFirst()));
        }
        for (PermSelector selector : permissions.selectors()) {
            x++;
            sender.sendRichMessage(
                    tl.list().getItem(),
                    Placeholder.component("name", selector.displayName()),
                    Placeholder.component("value", selector.displayValue(faction)),
                    Placeholder.parsed("commandmoveup", commandPiece + movePiece + x + ' ' + tl.move().getAliasUp().getFirst()),
                    Placeholder.parsed("commandmovedown", commandPiece + movePiece + x + ' ' + tl.move().getAliasDown().getFirst()),
                    Placeholder.parsed("commandremove", commandPiece + removePiece + "\"" + selector.serialize() + "\""),
                    Placeholder.parsed("commandshow", commandPiece + showPiece + x),
                    Placeholder.parsed("rownumber", Integer.toString(x)));
        }
        if (!tl.list().getFooter().isEmpty()) {
            sender.sendRichMessage(tl.list().getFooter(),
                    Placeholder.parsed("commandadd", commandPiece + tl.add().getAliases().getFirst()),
                    Placeholder.parsed("commandoverride", commandPiece + tl.listOverride().getAliases().getFirst()));
        }
    }

    private void listOverrideSelectors(CommandContext<Sender> context, Faction faction, Sender sender, TranslationsConfig.Commands.Permissions tl) {
        PermissionsConfig conf = FactionsPlugin.instance().configManager().permissionsConfig();
        List<PermSelector> order = conf.getOverridePermissionsOrder();
        Map<PermSelector, Map<String, Boolean>> permissions = conf.getOverridePermissions();
        int x = 0;
        String commandPiece = this.firstCmdBit.apply(context) + tl.showOverride().getAliases().getFirst() + ' ';
        if (!tl.listOverride().getHeader().isEmpty()) {
            sender.sendRichMessage(tl.listOverride().getHeader());
        }
        for (PermSelector selector : order) {
            Set<String> actions = new HashSet<>(permissions.get(selector).keySet());
            conf.getHiddenActions().forEach(actions::remove);
            if (actions.isEmpty()) {
                continue;
            }
            x++;
            sender.sendRichMessage(
                    tl.listOverride().getItem(),
                    Placeholder.component("name", selector.displayName()),
                    Placeholder.component("value", selector.displayValue(faction)),
                    Placeholder.parsed("commandshow", commandPiece + x),
                    Placeholder.unparsed("rownumber", Integer.toString(x)));
        }
        if (!tl.listOverride().getFooter().isEmpty()) {
            sender.sendRichMessage(tl.listOverride().getFooter());
        }
    }

    private void showSelector(CommandContext<Sender> context, Faction faction, int index, PermSelector selector, Sender sender, Faction.Permissions permissions, TranslationsConfig.Commands.Permissions tl) {
        int x = 0;
        // don't show priority that only has hiddens
        String commandPiece = this.firstCmdBit.apply(context);
        boolean notShown = true;
        for (PermSelector sel : permissions.selectors()) {
            if (++x == index || sel.equals(selector)) {
                notShown = false;
                if (!tl.show().getHeader().isEmpty()) {
                    sender.sendRichMessage(tl.show().getHeader(),
                            Placeholder.component("name", sel.displayName()),
                            Placeholder.component("value", sel.displayValue(faction)),
                            Placeholder.unparsed("rownumber", Integer.toString(x)),
                            Placeholder.parsed("command", commandPiece + tl.add().getAliases().getFirst() + " \"" + sel.serialize() + "\""));
                }
                Faction.Permissions.SelectorPerms perms = permissions.get(sel);
                for (String actionName : permissions.get(sel).actions()) {
                    if (FactionsPlugin.instance().configManager().permissionsConfig().getHiddenActions().contains(actionName.toUpperCase())) {
                        continue;
                    }
                    PermissibleAction action = PermissibleActionRegistry.get(actionName);
                    sender.sendRichMessage(tl.show().getItem(),
                            Placeholder.unparsed("shortdesc", action == null ? "???" : action.shortDescription()),
                            Placeholder.unparsed("desc", action == null ? "???" : action.description()),
                            Placeholder.unparsed("state", perms.get(actionName).toString()),
                            Placeholder.parsed("commandremove", commandPiece + tl.remove().getAliases().getFirst() + " \"" + sel.serialize() + "\" " + actionName));
                }
                if (!tl.show().getFooter().isEmpty()) {
                    sender.sendRichMessage(tl.show().getFooter(),
                            Placeholder.component("name", sel.displayName()),
                            Placeholder.component("value", sel.displayValue(faction)),
                            Placeholder.unparsed("rownumber", Integer.toString(x)),
                            Placeholder.parsed("command", commandPiece + tl.add().getAliases().getFirst() + " \"" + sel.serialize() + "\""));
                }
            }
        }
        if (notShown) {
            sender.sendRichMessage(tl.show().getSelectorNotFound());
        }
    }

    private void showOverrideSelector(Faction faction, int index, PermSelector selector, Sender sender, TranslationsConfig.Commands.Permissions tl) {
        PermissionsConfig conf = FactionsPlugin.instance().configManager().permissionsConfig();
        List<PermSelector> order = conf.getOverridePermissionsOrder();
        Map<PermSelector, Map<String, Boolean>> permissions = conf.getOverridePermissions();
        int x = 0;
        if (selector == null && index >= 0) {
            for (PermSelector sel : order) {
                Set<String> actions = new HashSet<>(permissions.get(sel).keySet());
                conf.getHiddenActions().forEach(actions::remove);
                if (actions.isEmpty()) {
                    continue;
                }
                x++;
                if (x == index) {
                    selector = sel;
                    break;
                }
            }
        } else if (selector != null) {
            // Check for hidden selector
            if (order.contains(selector)) {
                Set<String> actions = new HashSet<>(permissions.get(selector).keySet());
                conf.getHiddenActions().forEach(actions::remove);
                if (actions.isEmpty()) {
                    selector = null;
                }
            } else {
                selector = null;
            }
        }
        if (selector == null) {
            sender.sendRichMessage(tl.showOverride().getSelectorNotFound());
            return;
        }

        if (!tl.showOverride().getHeader().isEmpty()) {
            sender.sendRichMessage(tl.showOverride().getHeader(),
                    Placeholder.component("name", selector.displayName()),
                    Placeholder.component("value", selector.displayValue(faction)),
                    Placeholder.unparsed("rownumber", Integer.toString(x)));
        }
        for (Map.Entry<String, Boolean> e : permissions.get(selector).entrySet()) {
            if (FactionsPlugin.instance().configManager().permissionsConfig().getHiddenActions().contains(e.getKey())) {
                continue;
            }
            PermissibleAction action = PermissibleActionRegistry.get(e.getKey());
            sender.sendRichMessage(tl.showOverride().getItem(),
                    Placeholder.unparsed("shortdesc", action == null ? "???" : action.shortDescription()),
                    Placeholder.unparsed("desc", action == null ? "???" : action.description()),
                    Placeholder.unparsed("state", e.getValue().toString()));
        }
        if (!tl.showOverride().getFooter().isEmpty()) {
            sender.sendRichMessage(tl.showOverride().getFooter(),
                    Placeholder.component("name", selector.displayName()),
                    Placeholder.component("value", selector.displayValue(faction)),
                    Placeholder.unparsed("rownumber", Integer.toString(x)));
        }
    }
}
