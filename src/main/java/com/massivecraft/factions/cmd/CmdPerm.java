package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.config.file.PermissionsConfig;
import com.massivecraft.factions.config.file.TranslationsConfig;
import com.massivecraft.factions.data.MemoryFaction;
import com.massivecraft.factions.perms.PermSelector;
import com.massivecraft.factions.perms.PermSelectorRegistry;
import com.massivecraft.factions.perms.PermissibleAction;
import com.massivecraft.factions.perms.PermissibleActionRegistry;
import com.massivecraft.factions.perms.Role;
import com.massivecraft.factions.perms.selector.UnknownSelector;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CmdPerm extends FCommand {

    public CmdPerm() {
        super();
        this.aliases.addAll(FactionsPlugin.getInstance().tl().commands().permissions().getAliases());

        this.requirements = new CommandRequirements.Builder(Permission.PERMISSIONS)
                .memberOnly()
                .withRole(Role.ADMIN)
                .build();

        this.addSubCommand(new CmdPermAdd());
        this.addSubCommand(new CmdPermList());
        this.addSubCommand(new CmdPermListOverride());
        this.addSubCommand(new CmdPermMove());
        this.addSubCommand(new CmdPermRemove());
        this.addSubCommand(new CmdPermShow());
        this.addSubCommand(new CmdPermShowOverride());
        this.addSubCommand(new CmdPermReset());
    }

    private static int length(ComponentBuilder<TextComponent, TextComponent.Builder> builder) {
        return ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(builder.build())).length();
    }

    abstract static class CmdPermAbstract extends FCommand {
        CmdPermAbstract() {
            this.requirements = new CommandRequirements.Builder(Permission.PERMISSIONS)
                    .memberOnly()
                    .withRole(Role.ADMIN)
                    .noErrorOnManyArgs()
                    .build();
        }

        @Override
        public void perform(CommandContext context) {
            Audience audience = FactionsPlugin.getInstance().getAdventure().player(context.player);
            LinkedHashMap<PermSelector, Map<String, Boolean>> permissions = ((MemoryFaction) context.faction).getPermissions();
            this.perform(context, audience, permissions, FactionsPlugin.getInstance().tl().commands().permissions());
        }

        abstract void perform(CommandContext context, Audience audience, LinkedHashMap<PermSelector, Map<String, Boolean>> permissions, TranslationsConfig.Commands.Permissions tl);
    }

    class CmdPermAdd extends CmdPermAbstract {
        CmdPermAdd() {
            this.aliases.addAll(FactionsPlugin.getInstance().tl().commands().permissions().add().getAliases());
        }

        void perform(CommandContext context, Audience audience, LinkedHashMap<PermSelector, Map<String, Boolean>> permissions, TranslationsConfig.Commands.Permissions tl) {
            if (context.args.isEmpty()) {
                List<String> selectors = new ArrayList<>(PermSelectorRegistry.getSelectors());
                Collections.sort(selectors);

                ComponentBuilder<TextComponent, TextComponent.Builder> build = Component.text();
                build.append(MiniMessage.miniMessage().deserialize(tl.add().getAvailableSelectorsIntro()));
                ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(build.build()));
                int x = length(build);

                String commandPiece = '/' + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + ' ' +
                        tl.getAliases().getFirst() + ' ' + tl.add().getAliases().getFirst() + ' ';

                for (String selector : selectors) {
                    build.append(MiniMessage.miniMessage().deserialize(tl.add().getAvailableSelectorsSelector(),
                            Placeholder.parsed("command", commandPiece + selector),
                            Placeholder.unparsed("selector", selector)
                    ));
                    x = length(build);
                    if (x >= 50) {
                        x = 0;
                        audience.sendMessage(build);
                        build = Component.text();
                    }
                }
                if (x > 0) {
                    audience.sendMessage(build);
                }
            } else if (context.args.size() == 1) {
                String target = context.argAsString(0);
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
                            audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.add().getSelectorCreateFail(),
                                    Placeholder.unparsed("error", ex == null ? "???" : ex)));
                        } else {
                            audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.add().getSelectorNotFound()));
                        }
                    } else {
                        if (descriptor.acceptsEmpty()) {
                            selector = descriptor.create("");
                            break glass;
                        }
                        Map<String, String> options = descriptor.getOptions(context.faction);
                        if (descriptor.getInstructions() != null) {
                            audience.sendMessage(Component.text(descriptor.getInstructions()));
                            audience.sendMessage(MiniMessage.miniMessage().deserialize('/' + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + ' ' +
                                    tl.getAliases().getFirst() + ' ' + tl.add().getAliases().getFirst() + ' ' + descriptor.getName() + ':' + tl.add().getSelectorOptionHere()));
                        }
                        if (options != null) {
                            ComponentBuilder<TextComponent, TextComponent.Builder> build = Component.text();
                            build.append(MiniMessage.miniMessage().deserialize(tl.add().getSelectorOptionsIntro()));
                            int x = length(build);

                            String commandPiece = '/' + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + ' ' +
                                    tl.getAliases().getFirst() + ' ' + tl.add().getAliases().getFirst() + ' ';

                            for (Map.Entry<String, String> entry : options.entrySet()) {
                                build.append(MiniMessage.miniMessage().deserialize(tl.add().getSelectorOptionsItem(),
                                        Placeholder.parsed("command", commandPiece + entry.getKey()),
                                        Placeholder.unparsed("display", entry.getValue())));
                                x = length(build);
                                if (x >= 50) {
                                    x = 0;
                                    audience.sendMessage(build);
                                    build = Component.text();
                                }
                            }
                            if (x > 0) {
                                audience.sendMessage(build);
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
                            audience.sendMessage(build);
                            build = Component.text();
                        }
                    }
                    if (x > 0) {
                        audience.sendMessage(build);
                    }
                } else { // Add selector
                    permissions.put(selector, new LinkedHashMap<>());
                    listSelectors(context, audience, permissions, tl);
                }
            } else if (context.args.size() == 2) {
                audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.add().getActionAllowDenyOptions(),
                        Placeholder.unparsed("allow", tl.add().getActionAllowAlias().getFirst()),
                        Placeholder.unparsed("deny", tl.add().getActionDenyAlias().getFirst())));
            } else if (context.args.size() == 3) {
                PermSelector selector = PermSelectorRegistry.create(context.argAsString(0), false);
                if (selector instanceof UnknownSelector) {
                    audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.add().getSelectorNotFound()));
                } else if (!permissions.containsKey(selector)) {
                    audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.add().getSelectorNotFound()));
                } else {
                    PermissibleAction action = PermissibleActionRegistry.get(context.argAsString(1));
                    if (action == null || FactionsPlugin.getInstance().getConfigManager().getPermissionsConfig().getHiddenActions().contains(action.getName())) {
                        audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.add().getActionNotFound()));
                    } else {
                        Boolean allow = null;
                        String choice = context.argAsString(2);
                        if (tl.add().getActionAllowAlias().stream().anyMatch(i -> i.equalsIgnoreCase(choice))) {
                            allow = true;
                        } else if (tl.add().getActionDenyAlias().stream().anyMatch(i -> i.equalsIgnoreCase(choice))) {
                            allow = false;
                        }
                        if (allow == null) {
                            audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.add().getActionAllowDenyOptions(),
                                    Placeholder.unparsed("allow", tl.add().getActionAllowAlias().getFirst()),
                                    Placeholder.unparsed("deny", tl.add().getActionDenyAlias().getFirst())));
                            return;
                        }
                        permissions.get(selector).put(action.getName(), allow);
                        showSelector(context, -1, selector, audience, permissions, tl);
                    }
                }
            }
        }
    }

    class CmdPermList extends CmdPermAbstract {
        CmdPermList() {
            this.aliases.addAll(FactionsPlugin.getInstance().tl().commands().permissions().list().getAliases());
        }

        void perform(CommandContext context, Audience audience, LinkedHashMap<PermSelector, Map<String, Boolean>> permissions, TranslationsConfig.Commands.Permissions tl) {
            listSelectors(context, audience, permissions, tl);
        }
    }

    class CmdPermListOverride extends CmdPermAbstract {
        CmdPermListOverride() {
            this.aliases.addAll(FactionsPlugin.getInstance().tl().commands().permissions().listOverride().getAliases());
        }

        void perform(CommandContext context, Audience audience, LinkedHashMap<PermSelector, Map<String, Boolean>> permissions, TranslationsConfig.Commands.Permissions tl) {
            listOverrideSelectors(context, audience, tl);
        }
    }

    class CmdPermMove extends CmdPermAbstract {
        CmdPermMove() {
            this.aliases.addAll(FactionsPlugin.getInstance().tl().commands().permissions().move().getAliases());
        }

        void perform(CommandContext context, Audience audience, LinkedHashMap<PermSelector, Map<String, Boolean>> permissions, TranslationsConfig.Commands.Permissions tl) {
            if (context.args.size() < 2) {
                //
                return;
            }
            int num = context.argAsInt(0, -1);
            if (num < 1) {
                //
                return;
            }
            int hold;
            String choice = context.argAsString(1);
            if (tl.move().getAliasUp().stream().anyMatch(i -> i.equalsIgnoreCase(choice))) {
                hold = num - 1;
            } else if (tl.move().getAliasDown().stream().anyMatch(i -> i.equalsIgnoreCase(choice))) {
                hold = num;
            } else {
                audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.move().getErrorOptions(),
                        Placeholder.unparsed("up", tl.move().getAliasUp().getFirst()),
                        Placeholder.unparsed("down", tl.move().getAliasDown().getFirst())));
                return;
            }
            if (hold < 1) {
                audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.move().getErrorHighest()));
            } else if (hold > permissions.size() - 1) {
                audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.move().getErrorLowest()));
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
                ((MemoryFaction) context.faction).setPermissions(newMap);
                permissions = newMap;
            }
            listSelectors(context, audience, permissions, tl);
        }
    }

    class CmdPermRemove extends CmdPermAbstract {
        CmdPermRemove() {
            this.aliases.addAll(FactionsPlugin.getInstance().tl().commands().permissions().remove().getAliases());
        }

        void perform(CommandContext context, Audience audience, LinkedHashMap<PermSelector, Map<String, Boolean>> permissions, TranslationsConfig.Commands.Permissions tl) {
            if (context.args.isEmpty()) {
                listSelectors(context, audience, permissions, tl);
            } else if (context.args.size() == 1) {
                PermSelector selector = PermSelectorRegistry.create(context.argAsString(0), false);
                permissions.remove(selector);
                listSelectors(context, audience, permissions, tl);
            } else {
                PermSelector selector = PermSelectorRegistry.create(context.argAsString(0), false);
                Map<String, Boolean> map = permissions.get(selector);
                if (map != null) {
                    String target = context.argAsString(1);
                    map.keySet().removeIf(s -> s.equalsIgnoreCase(target));
                }
                showSelector(context, -1, selector, audience, permissions, tl);
            }
        }
    }

    class CmdPermReset extends CmdPermAbstract {
        CmdPermReset() {
            this.aliases.addAll(FactionsPlugin.getInstance().tl().commands().permissions().reset().getAliases());
        }

        void perform(CommandContext context, Audience audience, LinkedHashMap<PermSelector, Map<String, Boolean>> permissions, TranslationsConfig.Commands.Permissions tl) {
            if (context.args.size() == 1 && context.argAsString(0, "").equals(tl.reset().getConfirmWord())) {
                ((MemoryFaction) context.faction).resetPerms();
                audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.reset().getResetComplete()));
            } else {
                String cmd = '/' + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + ' ' + tl.getAliases().getFirst() + ' ' + tl.reset().getAliases().getFirst() + ' ' + tl.reset().getConfirmWord();
                audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.reset().getWarning(),
                        Placeholder.parsed("command", cmd)));
            }
        }
    }

    class CmdPermShow extends CmdPermAbstract {
        CmdPermShow() {
            this.aliases.addAll(FactionsPlugin.getInstance().tl().commands().permissions().show().getAliases());
            this.optionalArgs.put("selector", null);
        }

        void perform(CommandContext context, Audience audience, LinkedHashMap<PermSelector, Map<String, Boolean>> permissions, TranslationsConfig.Commands.Permissions tl) {
            if (context.args.isEmpty()) {
                listSelectors(context, audience, permissions, tl);
                return;
            }
            int index = context.argAsInt(0, -1);
            PermSelector selector = null;
            if (index < 1) {
                selector = PermSelectorRegistry.create(context.argAsString(0), false);
            }
            showSelector(context, index, selector, audience, permissions, tl);
        }
    }

    class CmdPermShowOverride extends CmdPermAbstract {
        CmdPermShowOverride() {
            this.aliases.addAll(FactionsPlugin.getInstance().tl().commands().permissions().showOverride().getAliases());
            this.optionalArgs.put("selector", null);
        }

        void perform(CommandContext context, Audience audience, LinkedHashMap<PermSelector, Map<String, Boolean>> permissions, TranslationsConfig.Commands.Permissions tl) {
            if (context.args.isEmpty()) {
                listOverrideSelectors(context, audience, tl);
                return;
            }
            int index = context.argAsInt(0, -1);
            PermSelector selector = null;
            if (index < 1) {
                selector = PermSelectorRegistry.create(context.argAsString(0), false);
            }
            showOverrideSelector(context, index, selector, audience, tl);
        }
    }

    @Override
    public void perform(CommandContext context) {
        listSelectors(context, FactionsPlugin.getInstance().getAdventure().player(context.player), ((MemoryFaction) context.faction).getPermissions(), FactionsPlugin.getInstance().tl().commands().permissions());
    }

    private void listSelectors(CommandContext context, Audience audience, LinkedHashMap<PermSelector, Map<String, Boolean>> permissions, TranslationsConfig.Commands.Permissions tl) {
        int x = 0;
        String commandPiece = '/' + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + ' ' +
                tl.getAliases().getFirst() + ' ';
        String movePiece = tl.move().getAliases().getFirst() + ' ';
        String removePiece = tl.remove().getAliases().getFirst() + ' ';
        String showPiece = tl.show().getAliases().getFirst() + ' ';
        if (!tl.list().getHeader().isEmpty()) {
            audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.list().getHeader(),
                    Placeholder.parsed("commandadd", commandPiece + tl.add().getAliases().getFirst()),
                    Placeholder.parsed("commandoverride", commandPiece + tl.listOverride().getAliases().getFirst())));
        }
        for (Map.Entry<PermSelector, ?> entry : permissions.entrySet()) {
            x++;
            audience.sendMessage(MiniMessage.miniMessage().deserialize(
                    tl.list().getItem(),
                    Placeholder.component("name", entry.getKey().displayName()),
                    Placeholder.component("value", entry.getKey().displayValue(context.faction)),
                    Placeholder.parsed("commandmoveup", commandPiece + movePiece + x + ' ' + tl.move().getAliasUp().getFirst()),
                    Placeholder.parsed("commandmovedown", commandPiece + movePiece + x + ' ' + tl.move().getAliasDown().getFirst()),
                    Placeholder.parsed("commandremove", commandPiece + removePiece + entry.getKey().serialize()),
                    Placeholder.parsed("commandshow", commandPiece + showPiece + x),
                    Placeholder.parsed("rownumber", Integer.toString(x))));
        }
        if (!tl.list().getFooter().isEmpty()) {
            audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.list().getFooter(),
                    Placeholder.parsed("commandadd", commandPiece + tl.add().getAliases().getFirst()),
                    Placeholder.parsed("commandoverride", commandPiece + tl.listOverride().getAliases().getFirst())));
        }
    }

    private void listOverrideSelectors(CommandContext context, Audience audience, TranslationsConfig.Commands.Permissions tl) {
        PermissionsConfig conf = FactionsPlugin.getInstance().getConfigManager().getPermissionsConfig();
        List<PermSelector> order = conf.getOverridePermissionsOrder();
        Map<PermSelector, Map<String, Boolean>> permissions = conf.getOverridePermissions();
        int x = 0;
        String commandPiece = '/' + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + ' ' +
                tl.getAliases().getFirst() + ' ' + tl.showOverride().getAliases().getFirst() + ' ';
        if (!tl.listOverride().getHeader().isEmpty()) {
            audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.listOverride().getHeader()));
        }
        for (PermSelector selector : order) {
            Set<String> actions = new HashSet<>(permissions.get(selector).keySet());
            conf.getHiddenActions().forEach(actions::remove);
            if (actions.isEmpty()) {
                continue;
            }
            x++;
            audience.sendMessage(MiniMessage.miniMessage().deserialize(
                    tl.listOverride().getItem(),
                    Placeholder.component("name", selector.displayName()),
                    Placeholder.component("value", selector.displayValue(context.faction)),
                    Placeholder.parsed("commandshow", commandPiece + x),
                    Placeholder.unparsed("rownumber", Integer.toString(x))));
        }
        if (!tl.listOverride().getFooter().isEmpty()) {
            audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.listOverride().getFooter()));
        }
    }

    private void showSelector(CommandContext context, int index, PermSelector selector, Audience audience, LinkedHashMap<PermSelector, Map<String, Boolean>> permissions, TranslationsConfig.Commands.Permissions tl) {
        int x = 0;
        // don't show priority that only has hiddens
        String commandPiece = '/' + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + ' ' +
                tl.getAliases().getFirst() + ' ';
        boolean notShown = true;
        for (Map.Entry<PermSelector, Map<String, Boolean>> entry : permissions.entrySet()) {
            if (++x == index || entry.getKey().equals(selector)) {
                notShown = false;
                if (!tl.show().getHeader().isEmpty()) {
                    audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.show().getHeader(),
                            Placeholder.component("name", entry.getKey().displayName()),
                            Placeholder.component("value", entry.getKey().displayValue(context.faction)),
                            Placeholder.unparsed("rownumber", Integer.toString(x)),
                            Placeholder.parsed("command", commandPiece + tl.add().getAliases().getFirst() + ' ' + entry.getKey().serialize())));
                }
                for (Map.Entry<String, Boolean> e : entry.getValue().entrySet()) {
                    if (FactionsPlugin.getInstance().getConfigManager().getPermissionsConfig().getHiddenActions().contains(e.getKey())) {
                        continue;
                    }
                    PermissibleAction action = PermissibleActionRegistry.get(e.getKey());
                    audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.show().getItem(),
                            Placeholder.unparsed("shortdesc", action.getShortDescription()),
                            Placeholder.unparsed("desc", action.getDescription()),
                            Placeholder.unparsed("state", e.getValue().toString()),
                            Placeholder.parsed("commandremove", commandPiece + tl.remove().getAliases().getFirst() + ' ' + entry.getKey().serialize() + ' ' + action.getName())));
                }
                if (!tl.show().getFooter().isEmpty()) {
                    audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.show().getFooter(),
                            Placeholder.component("name", entry.getKey().displayName()),
                            Placeholder.component("value", entry.getKey().displayValue(context.faction)),
                            Placeholder.unparsed("rownumber", Integer.toString(x)),
                            Placeholder.parsed("command", commandPiece + tl.add().getAliases().getFirst() + ' ' + entry.getKey().serialize())));
                }
            }
        }
        if (notShown) {
            audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.show().getSelectorNotFound()));
        }
    }

    private void showOverrideSelector(CommandContext context, int index, PermSelector selector, Audience audience, TranslationsConfig.Commands.Permissions tl) {
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
            audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.showOverride().getSelectorNotFound()));
            return;
        }

        if (!tl.showOverride().getHeader().isEmpty()) {
            audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.showOverride().getHeader(),
                    Placeholder.component("name", selector.displayName()),
                    Placeholder.component("value", selector.displayValue(context.faction)),
                    Placeholder.unparsed("rownumber", Integer.toString(x))));
        }
        for (Map.Entry<String, Boolean> e : permissions.get(selector).entrySet()) {
            if (FactionsPlugin.getInstance().getConfigManager().getPermissionsConfig().getHiddenActions().contains(e.getKey())) {
                continue;
            }
            PermissibleAction action = PermissibleActionRegistry.get(e.getKey());
            audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.showOverride().getItem(),
                    Placeholder.unparsed("shortdesc", action.getShortDescription()),
                    Placeholder.unparsed("desc", action.getDescription()),
                    Placeholder.unparsed("state", e.getValue().toString())));
        }
        if (!tl.showOverride().getFooter().isEmpty()) {
            audience.sendMessage(MiniMessage.miniMessage().deserialize(tl.showOverride().getFooter(),
                    Placeholder.component("name", selector.displayName()),
                    Placeholder.component("value", selector.displayValue(context.faction)),
                    Placeholder.unparsed("rownumber", Integer.toString(x))));
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_PERMS_DESCRIPTION;
    }
}
