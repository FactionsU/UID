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
import dev.kitteh.factions.permissible.PermissibleAction;
import dev.kitteh.factions.permissible.PermissibleActionRegistry;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.permissible.selector.UnknownSelector;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class CmdSetPerm implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            var tl = FactionsPlugin.getInstance().tl().commands().permissions();
            List<String> aliases = new ArrayList<>(tl.getAliases());
            Command.Builder<Sender> permBuilder = builder.literal(aliases.removeFirst(), aliases.toArray(new String[0]))
                    .commandDescription(Cloudy.desc(TL.COMMAND_PERMS_DESCRIPTION))
                    .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.PERMISSIONS).and(Cloudy.isAtLeastRole(Role.ADMIN))));

            manager.command(permBuilder.handler(this::handleList));

            List<String> addAliases = new ArrayList<>(tl.add().getAliases());
            manager.command(
                    permBuilder.literal(addAliases.removeFirst(), addAliases.toArray(new String[0]))
                            .optional("selector", StringParser.stringParser())
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
                            .required("selector", StringParser.stringParser())
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
                            .required("selector", StringParser.stringParser())
                            .handler(this::handleShow)
            );

            List<String> showOverrideAliases = new ArrayList<>(tl.showOverride().getAliases());
            manager.command(
                    permBuilder.literal(showOverrideAliases.removeFirst(), showOverrideAliases.toArray(new String[0]))
                            .required("selector", StringParser.stringParser())
                            .handler(this::handleShowOverride)
            );
        };
    }

    private void handleList(CommandContext<Sender> context) {
        Faction faction = ((Sender.Player) context.sender()).faction();
        listSelectors(faction, context.sender(), ((MemoryFaction) faction).getPermissions(), FactionsPlugin.getInstance().tl().commands().permissions());
    }

    private void handleListOverrides(CommandContext<Sender> context) {
        listOverrideSelectors(((Sender.Player) context.sender()).faction(), context.sender(), FactionsPlugin.getInstance().tl().commands().permissions());
    }

    private void handleAdd(CommandContext<Sender> context) {
        var tl = FactionsPlugin.getInstance().tl().commands().permissions();
        Faction faction = ((Sender.Player) context.sender()).faction();
        var permissions = ((MemoryFaction) faction).getPermissions();

        Optional<String> argSelector = context.optional("selector");
        Optional<String> argAction = context.optional("perm");
        Optional<String> argAllowDeny = context.optional("allowdeny");

        if (argSelector.isEmpty()) {
            this.handleAddDefault(context.sender(), tl);
        } else if (argAction.isEmpty()) {
            this.handleAddSelector(argSelector.get(), context.sender(), faction, permissions, tl);
        } else if (argAllowDeny.isEmpty()) {
            context.sender().sendMessage(MiniMessage.miniMessage().deserialize(tl.add().getActionAllowDenyOptions(),
                    Placeholder.unparsed("allow", tl.add().getActionAllowAlias().getFirst()),
                    Placeholder.unparsed("deny", tl.add().getActionDenyAlias().getFirst())));
        } else {
            this.handleAddPerm(argSelector.get(), argAction.get(), argAllowDeny.get(), context.sender(), faction, permissions, tl);
        }
    }

    private void handleAddDefault(Sender sender, TranslationsConfig.Commands.Permissions tl) {
        List<String> selectors = new ArrayList<>(PermSelectorRegistry.getSelectors());
        Collections.sort(selectors);

        ComponentBuilder<TextComponent, TextComponent.Builder> build = Component.text();
        build.append(MiniMessage.miniMessage().deserialize(tl.add().getAvailableSelectorsIntro()));
        ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(build.build()));
        int x = length(build);

        String commandPiece = '/' + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + ' ' +
                FactionsPlugin.getInstance().tl().commands().set().getAliases().getFirst() + ' ' +
                tl.getAliases().getFirst() + ' ' + tl.add().getAliases().getFirst() + ' ';

        for (String selector : selectors) {
            build.append(MiniMessage.miniMessage().deserialize(tl.add().getAvailableSelectorsSelector(),
                    Placeholder.parsed("command", commandPiece + selector),
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

    private void handleAddSelector(String target, Sender sender, Faction faction, LinkedHashMap<PermSelector, Map<String, Boolean>> permissions, TranslationsConfig.Commands.Permissions tl) {
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
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(tl.add().getSelectorCreateFail(),
                            Placeholder.unparsed("error", ex == null ? "???" : ex)));
                } else {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(tl.add().getSelectorNotFound()));
                }
            } else {
                if (descriptor.acceptsEmpty()) {
                    selector = descriptor.create("");
                    break glass;
                }
                Map<String, String> options = descriptor.getOptions(faction);
                if (descriptor.getInstructions() != null) {
                    sender.sendMessage(Component.text(descriptor.getInstructions()));
                    sender.sendMessage(MiniMessage.miniMessage().deserialize('/' + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + ' ' +
                            tl.getAliases().getFirst() + ' ' + tl.add().getAliases().getFirst() + ' ' + descriptor.getName() + ':' + tl.add().getSelectorOptionHere()));
                }
                if (options != null) {
                    ComponentBuilder<TextComponent, TextComponent.Builder> build = Component.text();
                    build.append(MiniMessage.miniMessage().deserialize(tl.add().getSelectorOptionsIntro()));
                    int x = length(build);

                    String commandPiece = '/' + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + ' ' +
                            FactionsPlugin.getInstance().tl().commands().set().getAliases().getFirst() + ' ' +
                            tl.getAliases().getFirst() + ' ' + tl.add().getAliases().getFirst() + ' ';

                    for (Map.Entry<String, String> entry : options.entrySet()) {
                        build.append(MiniMessage.miniMessage().deserialize(tl.add().getSelectorOptionsItem(),
                                Placeholder.parsed("command", commandPiece + entry.getKey()),
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
        if (permissions.containsKey(selector)) { // List options to add to this selector
            List<String> actions = PermissibleActionRegistry.getActions().stream().map(PermissibleAction::getName).collect(Collectors.toCollection(ArrayList::new));
            actions.removeAll(FactionsPlugin.getInstance().getConfigManager().getPermissionsConfig().getHiddenActions());
            actions.removeAll(permissions.get(selector).keySet());
            Collections.sort(actions);

            ComponentBuilder<TextComponent, TextComponent.Builder> build = Component.text();
            build.append(MiniMessage.miniMessage().deserialize(tl.add().getActionOptionsIntro()));
            int x = length(build);

            String commandPiece = '/' + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + ' ' +
                    tl.getAliases().getFirst() + ' ' + tl.add().getAliases().getFirst() + ' ' + selector.serialize() + ' ';

            for (String action : actions) {
                build.append(MiniMessage.miniMessage().deserialize(tl.add().getActionOptionsItem(),
                        Placeholder.unparsed("description", PermissibleActionRegistry.get(action).getDescription()),
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
            permissions.put(selector, new LinkedHashMap<>());
            listSelectors(faction, sender, permissions, tl);
        }
    }

    private void handleAddPerm(String argSelector, String argAction, String choice, Sender sender, Faction faction, LinkedHashMap<PermSelector, Map<String, Boolean>> permissions, TranslationsConfig.Commands.Permissions tl) {
        PermSelector selector = PermSelectorRegistry.create(argSelector, false);
        if (selector instanceof UnknownSelector) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(tl.add().getSelectorNotFound()));
        } else if (!permissions.containsKey(selector)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(tl.add().getSelectorNotFound()));
        } else {
            PermissibleAction action = PermissibleActionRegistry.get(argAction);
            if (action == null || FactionsPlugin.getInstance().getConfigManager().getPermissionsConfig().getHiddenActions().contains(action.getName())) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(tl.add().getActionNotFound()));
            } else {
                Boolean allow = null;
                if (tl.add().getActionAllowAlias().stream().anyMatch(i -> i.equalsIgnoreCase(choice))) {
                    allow = true;
                } else if (tl.add().getActionDenyAlias().stream().anyMatch(i -> i.equalsIgnoreCase(choice))) {
                    allow = false;
                }
                if (allow == null) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(tl.add().getActionAllowDenyOptions(),
                            Placeholder.unparsed("allow", tl.add().getActionAllowAlias().getFirst()),
                            Placeholder.unparsed("deny", tl.add().getActionDenyAlias().getFirst())));
                    return;
                }
                permissions.get(selector).put(action.getName(), allow);
                showSelector(faction, -1, selector, sender, permissions, tl);
            }
        }
    }

    private void handleMove(CommandContext<Sender> context) {
        var tl = FactionsPlugin.getInstance().tl().commands().permissions();
        Faction faction = ((Sender.Player) context.sender()).faction();
        var permissions = ((MemoryFaction) faction).getPermissions();

        int position = context.get("position");
        String direction = context.get("direction");

        int hold;
        if (tl.move().getAliasUp().stream().anyMatch(i -> i.equalsIgnoreCase(direction))) {
            hold = position - 1;
        } else if (tl.move().getAliasDown().stream().anyMatch(i -> i.equalsIgnoreCase(direction))) {
            hold = position;
        } else {
            context.sender().sendMessage(MiniMessage.miniMessage().deserialize(tl.move().getErrorOptions(),
                    Placeholder.unparsed("up", tl.move().getAliasUp().getFirst()),
                    Placeholder.unparsed("down", tl.move().getAliasDown().getFirst())));
            return;
        }
        if (hold < 1) {
            context.sender().sendMessage(MiniMessage.miniMessage().deserialize(tl.move().getErrorHighest()));
        } else if (hold > permissions.size() - 1) {
            context.sender().sendMessage(MiniMessage.miniMessage().deserialize(tl.move().getErrorLowest()));
        } else {
            LinkedHashMap<PermSelector, Map<String, Boolean>> newMap = new LinkedHashMap<>();
            int x = 0;
            Map.Entry<PermSelector, Map<String, Boolean>> holdEntry = null;
            for (Map.Entry<PermSelector, Map<String, Boolean>> entry : permissions.entrySet()) {
                if (++x == hold) {
                    holdEntry = entry;
                } else {
                    newMap.put(entry.getKey(), entry.getValue());
                    if (holdEntry != null) {
                        newMap.put(holdEntry.getKey(), holdEntry.getValue());
                        holdEntry = null;
                    }
                }
            }
            ((MemoryFaction) faction).setPermissions(newMap);
            permissions = newMap;
        }
        listSelectors(faction, context.sender(), permissions, tl);
    }

    private void handleRemove(CommandContext<Sender> context) {
        var tl = FactionsPlugin.getInstance().tl().commands().permissions();
        Faction faction = ((Sender.Player) context.sender()).faction();
        var permissions = ((MemoryFaction) faction).getPermissions();

        String argSelector = context.get("selector");
        Optional<String> argAction = context.optional("perm");

        if (argAction.isEmpty()) {
            PermSelector selector = PermSelectorRegistry.create(argSelector, false);
            permissions.remove(selector);
            listSelectors(faction, context.sender(), permissions, tl);
        } else {
            PermSelector selector = PermSelectorRegistry.create(argSelector, false);
            Map<String, Boolean> map = permissions.get(selector);
            if (map != null) {
                String target = argAction.get();
                map.keySet().removeIf(s -> s.equalsIgnoreCase(target));
            }
            showSelector(faction, -1, selector, context.sender(), permissions, tl);
        }
    }

    private void handleReset(CommandContext<Sender> context) {
        var tl = FactionsPlugin.getInstance().tl().commands().permissions();
        Faction faction = ((Sender.Player) context.sender()).faction();

        Optional<String> confirm = context.optional("confirm");

        if (confirm.isPresent() && confirm.get().equalsIgnoreCase(tl.reset().getConfirmWord())) {
            ((MemoryFaction) faction).resetPerms();
            context.sender().sendMessage(MiniMessage.miniMessage().deserialize(tl.reset().getResetComplete()));
        } else {
            String cmd = '/' + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() +
                    FactionsPlugin.getInstance().tl().commands().set().getAliases().getFirst() + ' ' +
                    ' ' + tl.getAliases().getFirst() + ' ' + tl.reset().getAliases().getFirst() + ' ' + tl.reset().getConfirmWord();
            context.sender().sendMessage(MiniMessage.miniMessage().deserialize(tl.reset().getWarning(),
                    Placeholder.parsed("command", cmd)));
        }
    }

    private void handleShow(CommandContext<Sender> context) {
        var tl = FactionsPlugin.getInstance().tl().commands().permissions();
        Faction faction = ((Sender.Player) context.sender()).faction();
        var permissions = ((MemoryFaction) faction).getPermissions();

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
        showSelector(faction, index, selector, context.sender(), permissions, tl);
    }

    private void handleShowOverride(CommandContext<Sender> context) {
        var tl = FactionsPlugin.getInstance().tl().commands().permissions();
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
        return ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(builder.build())).length();
    }

    private void listSelectors(Faction faction, Sender sender, LinkedHashMap<PermSelector, Map<String, Boolean>> permissions, TranslationsConfig.Commands.Permissions tl) {
        int x = 0;
        String commandPiece = '/' + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + ' ' +
                tl.getAliases().getFirst() + ' ';
        String movePiece = tl.move().getAliases().getFirst() + ' ';
        String removePiece = tl.remove().getAliases().getFirst() + ' ';
        String showPiece = tl.show().getAliases().getFirst() + ' ';
        if (!tl.list().getHeader().isEmpty()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(tl.list().getHeader(),
                    Placeholder.parsed("commandadd", commandPiece + tl.add().getAliases().getFirst()),
                    Placeholder.parsed("commandoverride", commandPiece + tl.listOverride().getAliases().getFirst())));
        }
        for (Map.Entry<PermSelector, ?> entry : permissions.entrySet()) {
            x++;
            sender.sendMessage(MiniMessage.miniMessage().deserialize(
                    tl.list().getItem(),
                    Placeholder.component("name", entry.getKey().displayName()),
                    Placeholder.component("value", entry.getKey().displayValue(faction)),
                    Placeholder.parsed("commandmoveup", commandPiece + movePiece + x + ' ' + tl.move().getAliasUp().getFirst()),
                    Placeholder.parsed("commandmovedown", commandPiece + movePiece + x + ' ' + tl.move().getAliasDown().getFirst()),
                    Placeholder.parsed("commandremove", commandPiece + removePiece + entry.getKey().serialize()),
                    Placeholder.parsed("commandshow", commandPiece + showPiece + x),
                    Placeholder.parsed("rownumber", Integer.toString(x))));
        }
        if (!tl.list().getFooter().isEmpty()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(tl.list().getFooter(),
                    Placeholder.parsed("commandadd", commandPiece + tl.add().getAliases().getFirst()),
                    Placeholder.parsed("commandoverride", commandPiece + tl.listOverride().getAliases().getFirst())));
        }
    }

    private void listOverrideSelectors(Faction faction, Sender sender, TranslationsConfig.Commands.Permissions tl) {
        PermissionsConfig conf = FactionsPlugin.getInstance().getConfigManager().getPermissionsConfig();
        List<PermSelector> order = conf.getOverridePermissionsOrder();
        Map<PermSelector, Map<String, Boolean>> permissions = conf.getOverridePermissions();
        int x = 0;
        String commandPiece = '/' + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + ' ' +
                tl.getAliases().getFirst() + ' ' + tl.showOverride().getAliases().getFirst() + ' ';
        if (!tl.listOverride().getHeader().isEmpty()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(tl.listOverride().getHeader()));
        }
        for (PermSelector selector : order) {
            Set<String> actions = new HashSet<>(permissions.get(selector).keySet());
            conf.getHiddenActions().forEach(actions::remove);
            if (actions.isEmpty()) {
                continue;
            }
            x++;
            sender.sendMessage(MiniMessage.miniMessage().deserialize(
                    tl.listOverride().getItem(),
                    Placeholder.component("name", selector.displayName()),
                    Placeholder.component("value", selector.displayValue(faction)),
                    Placeholder.parsed("commandshow", commandPiece + x),
                    Placeholder.unparsed("rownumber", Integer.toString(x))));
        }
        if (!tl.listOverride().getFooter().isEmpty()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(tl.listOverride().getFooter()));
        }
    }

    private void showSelector(Faction faction, int index, PermSelector selector, Sender sender, LinkedHashMap<PermSelector, Map<String, Boolean>> permissions, TranslationsConfig.Commands.Permissions tl) {
        int x = 0;
        // don't show priority that only has hiddens
        String commandPiece = '/' + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + ' ' +
                FactionsPlugin.getInstance().tl().commands().set().getAliases().getFirst() + ' ' +
                tl.getAliases().getFirst() + ' ';
        boolean notShown = true;
        for (Map.Entry<PermSelector, Map<String, Boolean>> entry : permissions.entrySet()) {
            if (++x == index || entry.getKey().equals(selector)) {
                notShown = false;
                if (!tl.show().getHeader().isEmpty()) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(tl.show().getHeader(),
                            Placeholder.component("name", entry.getKey().displayName()),
                            Placeholder.component("value", entry.getKey().displayValue(faction)),
                            Placeholder.unparsed("rownumber", Integer.toString(x)),
                            Placeholder.parsed("command", commandPiece + tl.add().getAliases().getFirst() + ' ' + entry.getKey().serialize())));
                }
                for (Map.Entry<String, Boolean> e : entry.getValue().entrySet()) {
                    if (FactionsPlugin.getInstance().getConfigManager().getPermissionsConfig().getHiddenActions().contains(e.getKey())) {
                        continue;
                    }
                    PermissibleAction action = PermissibleActionRegistry.get(e.getKey());
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(tl.show().getItem(),
                            Placeholder.unparsed("shortdesc", action.getShortDescription()),
                            Placeholder.unparsed("desc", action.getDescription()),
                            Placeholder.unparsed("state", e.getValue().toString()),
                            Placeholder.parsed("commandremove", commandPiece + tl.remove().getAliases().getFirst() + ' ' + entry.getKey().serialize() + ' ' + action.getName())));
                }
                if (!tl.show().getFooter().isEmpty()) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(tl.show().getFooter(),
                            Placeholder.component("name", entry.getKey().displayName()),
                            Placeholder.component("value", entry.getKey().displayValue(faction)),
                            Placeholder.unparsed("rownumber", Integer.toString(x)),
                            Placeholder.parsed("command", commandPiece + tl.add().getAliases().getFirst() + ' ' + entry.getKey().serialize())));
                }
            }
        }
        if (notShown) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(tl.show().getSelectorNotFound()));
        }
    }

    private void showOverrideSelector(Faction faction, int index, PermSelector selector, Sender sender, TranslationsConfig.Commands.Permissions tl) {
        PermissionsConfig conf = FactionsPlugin.getInstance().getConfigManager().getPermissionsConfig();
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
            sender.sendMessage(MiniMessage.miniMessage().deserialize(tl.showOverride().getSelectorNotFound()));
            return;
        }

        if (!tl.showOverride().getHeader().isEmpty()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(tl.showOverride().getHeader(),
                    Placeholder.component("name", selector.displayName()),
                    Placeholder.component("value", selector.displayValue(faction)),
                    Placeholder.unparsed("rownumber", Integer.toString(x))));
        }
        for (Map.Entry<String, Boolean> e : permissions.get(selector).entrySet()) {
            if (FactionsPlugin.getInstance().getConfigManager().getPermissionsConfig().getHiddenActions().contains(e.getKey())) {
                continue;
            }
            PermissibleAction action = PermissibleActionRegistry.get(e.getKey());
            sender.sendMessage(MiniMessage.miniMessage().deserialize(tl.showOverride().getItem(),
                    Placeholder.unparsed("shortdesc", action.getShortDescription()),
                    Placeholder.unparsed("desc", action.getDescription()),
                    Placeholder.unparsed("state", e.getValue().toString())));
        }
        if (!tl.showOverride().getFooter().isEmpty()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(tl.showOverride().getFooter(),
                    Placeholder.component("name", selector.displayName()),
                    Placeholder.component("value", selector.displayValue(faction)),
                    Placeholder.unparsed("rownumber", Integer.toString(x))));
        }
    }
}
