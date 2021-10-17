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
        public TL getUsageTranslation() {
            return null;
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
            if (context.args.size() == 0) {
                List<String> selectors = new ArrayList<>(PermSelectorRegistry.getSelectors());
                Collections.sort(selectors);

                ComponentBuilder<TextComponent, TextComponent.Builder> build = Component.text();
                build.append(MiniMessage.miniMessage().parse(tl.add().getAvailableSelectorsIntro()));
                ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(build.build()));
                int x = length(build);

                String commandPiece = '/' + FactionsPlugin.getInstance().conf().getCommandBase().get(0) + ' ' +
                        tl.getAliases().get(0) + ' ' + tl.add().getAliases().get(0) + ' ';

                for (String selector : selectors) {
                    build.append(MiniMessage.miniMessage().parse(tl.add().getAvailableSelectorsSelector(),
                            "command", commandPiece + selector,
                            "selector", selector
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
                            audience.sendMessage(MiniMessage.miniMessage().parse(tl.add().getSelectorCreateFail(),
                                    "error", ex == null ? "???" : ex));
                        } else {
                            audience.sendMessage(MiniMessage.miniMessage().parse(tl.add().getSelectorNotFound()));
                        }
                    } else {
                        if (descriptor.acceptsEmpty()) {
                            selector = descriptor.create("");
                            break glass;
                        }
                        Map<String, String> options = descriptor.getOptions(context.faction);
                        if (descriptor.getInstructions() != null) {
                            audience.sendMessage(Component.text(descriptor.getInstructions()));
                            audience.sendMessage(MiniMessage.miniMessage().parse('/' + FactionsPlugin.getInstance().conf().getCommandBase().get(0) + ' ' +
                                    tl.getAliases().get(0) + ' ' + tl.add().getAliases().get(0) + ' ' + descriptor.getName() + ':' + tl.add().getSelectorOptionHere()));
                        }
                        if (options != null) {
                            ComponentBuilder<TextComponent, TextComponent.Builder> build = Component.text();
                            build.append(MiniMessage.miniMessage().parse(tl.add().getSelectorOptionsIntro()));
                            int x = length(build);

                            String commandPiece = '/' + FactionsPlugin.getInstance().conf().getCommandBase().get(0) + ' ' +
                                    tl.getAliases().get(0) + ' ' + tl.add().getAliases().get(0) + ' ';

                            for (Map.Entry<String, String> entry : options.entrySet()) {
                                build.append(MiniMessage.miniMessage().parse(tl.add().getSelectorOptionsItem(),
                                        "command", commandPiece + entry.getKey(),
                                        "display", entry.getValue()));
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
                    build.append(MiniMessage.miniMessage().parse(tl.add().getActionOptionsIntro()));
                    int x = length(build);

                    String commandPiece = '/' + FactionsPlugin.getInstance().conf().getCommandBase().get(0) + ' ' +
                            tl.getAliases().get(0) + ' ' + tl.add().getAliases().get(0) + ' ' + selector.serialize() + ' ';

                    for (String action : actions) {
                        build.append(MiniMessage.miniMessage().parse(tl.add().getActionOptionsItem(),
                                "description", PermissibleActionRegistry.get(action).getDescription(),
                                "action", action,
                                "commandtrue", commandPiece + action + ' ' + tl.add().getActionAllowAlias().get(0),
                                "commandfalse", commandPiece + action + ' ' + tl.add().getActionDenyAlias().get(0)));
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
                audience.sendMessage(MiniMessage.miniMessage().parse(tl.add().getActionAllowDenyOptions(),
                        "allow", tl.add().getActionAllowAlias().get(0),
                        "deny", tl.add().getActionDenyAlias().get(0)));
            } else if (context.args.size() == 3) {
                PermSelector selector = PermSelectorRegistry.create(context.argAsString(0), false);
                if (selector instanceof UnknownSelector) {
                    audience.sendMessage(MiniMessage.miniMessage().parse(tl.add().getSelectorNotFound()));
                } else if (!permissions.containsKey(selector)) {
                    audience.sendMessage(MiniMessage.miniMessage().parse(tl.add().getSelectorNotFound()));
                } else {
                    PermissibleAction action = PermissibleActionRegistry.get(context.argAsString(1));
                    if (action == null || FactionsPlugin.getInstance().getConfigManager().getPermissionsConfig().getHiddenActions().contains(action.getName())) {
                        audience.sendMessage(MiniMessage.miniMessage().parse(tl.add().getActionNotFound()));
                    } else {
                        Boolean allow = null;
                        String choice = context.argAsString(2);
                        if (tl.add().getActionAllowAlias().stream().anyMatch(i -> i.equalsIgnoreCase(choice))) {
                            allow = true;
                        } else if (tl.add().getActionDenyAlias().stream().anyMatch(i -> i.equalsIgnoreCase(choice))) {
                            allow = false;
                        }
                        if (allow == null) {
                            audience.sendMessage(MiniMessage.miniMessage().parse(tl.add().getActionAllowDenyOptions(),
                                    "allow", tl.add().getActionAllowAlias().get(0),
                                    "deny", tl.add().getActionDenyAlias().get(0)));
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
                audience.sendMessage(MiniMessage.miniMessage().parse(tl.move().getErrorOptions(),
                        "up", tl.move().getAliasUp().get(0),
                        "down", tl.move().getAliasDown().get(0)));
                return;
            }
            if (hold < 1) {
                audience.sendMessage(MiniMessage.miniMessage().parse(tl.move().getErrorHighest()));
            } else if (hold > permissions.size() - 1) {
                audience.sendMessage(MiniMessage.miniMessage().parse(tl.move().getErrorLowest()));
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
            if (context.args.size() == 0) {
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
                audience.sendMessage(MiniMessage.miniMessage().parse(tl.reset().getResetComplete()));
            } else {
                String cmd = '/' + FactionsPlugin.getInstance().conf().getCommandBase().get(0) + ' ' + tl.getAliases().get(0) + ' ' + tl.reset().getAliases().get(0) + ' ' + tl.reset().getConfirmWord();
                audience.sendMessage(MiniMessage.miniMessage().parse(tl.reset().getWarning(),
                        "command", cmd));
            }
        }
    }

    class CmdPermShow extends CmdPermAbstract {
        CmdPermShow() {
            this.aliases.addAll(FactionsPlugin.getInstance().tl().commands().permissions().show().getAliases());
            this.optionalArgs.put("selector", null);
        }

        void perform(CommandContext context, Audience audience, LinkedHashMap<PermSelector, Map<String, Boolean>> permissions, TranslationsConfig.Commands.Permissions tl) {
            if (context.args.size() == 0) {
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
            if (context.args.size() == 0) {
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
        String commandPiece = '/' + FactionsPlugin.getInstance().conf().getCommandBase().get(0) + ' ' +
                tl.getAliases().get(0) + ' ';
        String movePiece = tl.move().getAliases().get(0) + ' ';
        String removePiece = tl.remove().getAliases().get(0) + ' ';
        String showPiece = tl.show().getAliases().get(0) + ' ';
        if (!tl.list().getHeader().isEmpty()) {
            audience.sendMessage(MiniMessage.miniMessage().parse(tl.list().getHeader(),
                    "commandadd", commandPiece + tl.add().getAliases().get(0),
                    "commandoverride", commandPiece + tl.listOverride().getAliases().get(0)));
        }
        for (Map.Entry<PermSelector, ?> entry : permissions.entrySet()) {
            x++;
            audience.sendMessage(MiniMessage.miniMessage().parse(
                    tl.list().getItem(),
                    "name", entry.getKey().displayName(),
                    "value", entry.getKey().displayValue(context.faction),
                    "commandmoveup", commandPiece + movePiece + x + ' ' + tl.move().getAliasUp().get(0),
                    "commandmovedown", commandPiece + movePiece + x + ' ' + tl.move().getAliasDown().get(0),
                    "commandremove", commandPiece + removePiece + entry.getKey().serialize(),
                    "commandshow", commandPiece + showPiece + x,
                    "rownumber", Integer.toString(x)));
        }
        if (!tl.list().getFooter().isEmpty()) {
            audience.sendMessage(MiniMessage.miniMessage().parse(tl.list().getFooter(),
                    "commandadd", commandPiece + tl.add().getAliases().get(0),
                    "commandoverride", commandPiece + tl.listOverride().getAliases().get(0)));
        }
    }

    private void listOverrideSelectors(CommandContext context, Audience audience, TranslationsConfig.Commands.Permissions tl) {
        PermissionsConfig conf = FactionsPlugin.getInstance().getConfigManager().getPermissionsConfig();
        List<PermSelector> order = conf.getOverridePermissionsOrder();
        Map<PermSelector, Map<String, Boolean>> permissions = conf.getOverridePermissions();
        int x = 0;
        String commandPiece = '/' + FactionsPlugin.getInstance().conf().getCommandBase().get(0) + ' ' +
                tl.getAliases().get(0) + ' ' + tl.showOverride().getAliases().get(0) + ' ';
        if (!tl.listOverride().getHeader().isEmpty()) {
            audience.sendMessage(MiniMessage.miniMessage().parse(tl.listOverride().getHeader()));
        }
        for (PermSelector selector : order) {
            Set<String> actions = new HashSet<>(permissions.get(selector).keySet());
            conf.getHiddenActions().forEach(actions::remove);
            if (actions.isEmpty()) {
                continue;
            }
            x++;
            audience.sendMessage(MiniMessage.miniMessage().parse(
                    tl.listOverride().getItem(),
                    "name", selector.displayName(),
                    "value", selector.displayValue(context.faction),
                    "commandshow", commandPiece + x,
                    "rownumber", Integer.toString(x)));
        }
        if (!tl.listOverride().getFooter().isEmpty()) {
            audience.sendMessage(MiniMessage.miniMessage().parse(tl.listOverride().getFooter()));
        }
    }

    private void showSelector(CommandContext context, int index, PermSelector selector, Audience audience, LinkedHashMap<PermSelector, Map<String, Boolean>> permissions, TranslationsConfig.Commands.Permissions tl) {
        int x = 0;
        // don't show priority that only has hiddens
        String commandPiece = '/' + FactionsPlugin.getInstance().conf().getCommandBase().get(0) + ' ' +
                tl.getAliases().get(0) + ' ';
        boolean notShown = true;
        for (Map.Entry<PermSelector, Map<String, Boolean>> entry : permissions.entrySet()) {
            if (++x == index || entry.getKey().equals(selector)) {
                notShown = false;
                if (!tl.show().getHeader().isEmpty()) {
                    audience.sendMessage(MiniMessage.miniMessage().parse(tl.show().getHeader(),
                            "name", entry.getKey().displayName(),
                            "value", entry.getKey().displayValue(context.faction),
                            "rownumber", Integer.toString(x),
                            "command", commandPiece + tl.add().getAliases().get(0) + ' ' + entry.getKey().serialize()));
                }
                for (Map.Entry<String, Boolean> e : entry.getValue().entrySet()) {
                    if (FactionsPlugin.getInstance().getConfigManager().getPermissionsConfig().getHiddenActions().contains(e.getKey())) {
                        continue;
                    }
                    PermissibleAction action = PermissibleActionRegistry.get(e.getKey());
                    audience.sendMessage(MiniMessage.miniMessage().parse(tl.show().getItem(),
                            "shortdesc", action.getShortDescription(),
                            "desc", action.getDescription(),
                            "state", e.getValue().toString(),
                            "commandremove", commandPiece + tl.remove().getAliases().get(0) + ' ' + entry.getKey().serialize() + ' ' + action.getName()));
                }
                if (!tl.show().getFooter().isEmpty()) {
                    audience.sendMessage(MiniMessage.miniMessage().parse(tl.show().getFooter(),
                            "name", entry.getKey().displayName(),
                            "value", entry.getKey().displayValue(context.faction),
                            "rownumber", Integer.toString(x),
                            "command", commandPiece + tl.add().getAliases().get(0) + ' ' + entry.getKey().serialize()));
                }
            }
        }
        if (notShown) {
            audience.sendMessage(MiniMessage.miniMessage().parse(tl.show().getSelectorNotFound()));
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
            audience.sendMessage(MiniMessage.miniMessage().parse(tl.showOverride().getSelectorNotFound()));
            return;
        }

        if (!tl.showOverride().getHeader().isEmpty()) {
            audience.sendMessage(MiniMessage.miniMessage().parse(tl.showOverride().getHeader(),
                    "name", selector.displayName(),
                    "value", selector.displayValue(context.faction),
                    "rownumber", Integer.toString(x)));
        }
        for (Map.Entry<String, Boolean> e : permissions.get(selector).entrySet()) {
            if (FactionsPlugin.getInstance().getConfigManager().getPermissionsConfig().getHiddenActions().contains(e.getKey())) {
                continue;
            }
            PermissibleAction action = PermissibleActionRegistry.get(e.getKey());
            audience.sendMessage(MiniMessage.miniMessage().parse(tl.showOverride().getItem(),
                    "shortdesc", action.getShortDescription(),
                    "desc", action.getDescription(),
                    "state", e.getValue().toString()));
        }
        if (!tl.showOverride().getFooter().isEmpty()) {
            audience.sendMessage(MiniMessage.miniMessage().parse(tl.showOverride().getFooter(),
                    "name", selector.displayName(),
                    "value", selector.displayValue(context.faction),
                    "rownumber", Integer.toString(x)));
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_PERMS_DESCRIPTION;
    }
}
