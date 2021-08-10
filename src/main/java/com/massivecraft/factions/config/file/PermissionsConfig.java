package com.massivecraft.factions.config.file;

import com.google.common.collect.ImmutableMap;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.config.annotation.Comment;
import com.massivecraft.factions.config.annotation.WipeOnReload;
import com.massivecraft.factions.perms.PermSelector;
import com.massivecraft.factions.perms.PermSelectorRegistry;
import com.massivecraft.factions.perms.PermissibleAction;
import com.massivecraft.factions.perms.PermissibleActionRegistry;
import com.massivecraft.factions.perms.PermissibleActions;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.perms.Role;
import com.massivecraft.factions.perms.selector.RelationAtLeastSelector;
import com.massivecraft.factions.perms.selector.RoleAtLeastSelector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"FieldMayBeFinal", "unused", "MismatchedQueryAndUpdateOfCollection"})
public class PermissionsConfig {
    @Comment("This is an auto-updating list of known actions and descriptions.\n" +
            "Editing this list has no effect.")
    @WipeOnReload
    private List<String> availableActions = new ArrayList<String>() {
        {
            for (PermissibleAction action : PermissibleActionRegistry.getActions()) {
                this.add(action.getName() + " - " + action.getDescription());
            }
        }
    };

    @Comment("This is an auto-updating list of known selectors.\n" +
            "Editing this list has no effect.")
    @WipeOnReload
    private List<String> availableSelectors = new ArrayList<String>() {
        {
            this.addAll(PermSelectorRegistry.getSelectors());
        }
    };

    @Comment("List actions here to hide from visibility. Comma separated, in quotes.\n" +
            "Defaults and forced perms defined here will still be used.")
    private List<String> hiddenActions = new ArrayList<>();

    private Map<String, Map<String, Boolean>> defaultPermissions = new LinkedHashMap<>();

    @WipeOnReload
    private transient Map<PermSelector, Map<String, Boolean>> defaultPermissionsMap = null;

    private List<String> defaultPermissionsOrder = new ArrayList<>();

    @WipeOnReload
    private transient List<PermSelector> defaultPermissionsOrderList = null;

    private Map<String, Map<String, Boolean>> overridePermissions = new LinkedHashMap<>();

    @WipeOnReload
    private transient Map<PermSelector, Map<String, Boolean>> overridePermissionsMap = null;

    private List<String> overridePermissionsOrder = new ArrayList<>();

    @WipeOnReload
    private transient List<PermSelector> overridePermissionsOrderList = null;

    public PermissionsConfig() {
        this.defaultPermissionsOrder.add(new RoleAtLeastSelector(Role.COLEADER).serialize());
        this.defaultPermissions.put(new RoleAtLeastSelector(Role.COLEADER).serialize(), ImmutableMap.<String, Boolean>builder()
                .put(PermissibleActions.SETHOME.name(), true)
                .put(PermissibleActions.ECONOMY.name(), true)
                .build());

        this.defaultPermissionsOrder.add(new RoleAtLeastSelector(Role.MODERATOR).serialize());
        this.defaultPermissions.put(new RoleAtLeastSelector(Role.MODERATOR).serialize(), ImmutableMap.<String, Boolean>builder()
                .put(PermissibleActions.BAN.name(), true)
                .put(PermissibleActions.INVITE.name(), true)
                .put(PermissibleActions.KICK.name(), true)
                .put(PermissibleActions.LISTCLAIMS.name(), true)
                .put(PermissibleActions.PROMOTE.name(), true)
                .put(PermissibleActions.SETWARP.name(), true)
                .put(PermissibleActions.TERRITORY.name(), true)
                .put(PermissibleActions.TNTWITHDRAW.name(), true)
                .build());

        this.defaultPermissionsOrder.add(new RoleAtLeastSelector(Role.RECRUIT).serialize());
        this.defaultPermissions.put(new RoleAtLeastSelector(Role.RECRUIT).serialize(), ImmutableMap.<String, Boolean>builder()
                .put(PermissibleActions.BUILD.name(), true)
                .put(PermissibleActions.BUTTON.name(), true)
                .put(PermissibleActions.CONTAINER.name(), true)
                .put(PermissibleActions.DESTROY.name(), true)
                .put(PermissibleActions.DOOR.name(), true)
                .put(PermissibleActions.FLY.name(), true)
                .put(PermissibleActions.FROSTWALK.name(), true)
                .put(PermissibleActions.HOME.name(), true)
                .put(PermissibleActions.ITEM.name(), true)
                .put(PermissibleActions.LEVER.name(), true)
                .put(PermissibleActions.PLATE.name(), true)
                .put(PermissibleActions.TNTDEPOSIT.name(), true)
                .put(PermissibleActions.WARP.name(), true)
                .build());

        this.defaultPermissionsOrder.add(new RelationAtLeastSelector(Relation.ALLY).serialize());
        this.defaultPermissions.put(new RelationAtLeastSelector(Relation.ALLY).serialize(), ImmutableMap.<String, Boolean>builder()
                .put(PermissibleActions.BUTTON.name(), true)
                .put(PermissibleActions.DOOR.name(), true)
                .put(PermissibleActions.FLY.name(), true)
                .put(PermissibleActions.LEVER.name(), true)
                .put(PermissibleActions.PLATE.name(), true)
                .build());
    }

    public List<String> getHiddenActions() {
        return hiddenActions;
    }

    public Map<PermSelector, Map<String, Boolean>> getDefaultPermissions() {
        if (defaultPermissionsMap == null) {
            defaultPermissionsMap = new LinkedHashMap<>();
            for (Map.Entry<String, Map<String, Boolean>> entry : defaultPermissions.entrySet()) {
                if (!defaultPermissionsOrder.contains(entry.getKey())) {
                    FactionsPlugin.getInstance().getLogger().warning("Found '" + entry.getKey() + "' in defaultPermissions but not in defaultPermissionsOrder. Ignoring.");
                    continue;
                }
                defaultPermissionsMap.put(PermSelectorRegistry.create(entry.getKey(), true), entry.getValue());
            }
        }
        return defaultPermissionsMap;
    }

    public List<PermSelector> getDefaultPermissionsOrder() {
        if (defaultPermissionsOrderList == null) {
            defaultPermissionsOrderList = new ArrayList<>();
            for (String selector : defaultPermissionsOrder) {
                if (!defaultPermissions.containsKey(selector)) {
                    FactionsPlugin.getInstance().getLogger().warning("Found '" + selector + "' in defaultPermissionsOrder but not in defaultPermissions. Ignoring.");
                    continue;
                }
                defaultPermissionsOrderList.add(PermSelectorRegistry.create(selector, true));
            }
        }
        return defaultPermissionsOrderList;
    }

    public Map<PermSelector, Map<String, Boolean>> getOverridePermissions() {
        if (overridePermissionsMap == null) {
            overridePermissionsMap = new LinkedHashMap<>();
            for (Map.Entry<String, Map<String, Boolean>> entry : overridePermissions.entrySet()) {
                if (!overridePermissionsOrder.contains(entry.getKey())) {
                    FactionsPlugin.getInstance().getLogger().warning("Found '" + entry.getKey() + "' in overridePermissions but not in overridePermissionsOrder. Ignoring.");
                    continue;
                }
                overridePermissionsMap.put(PermSelectorRegistry.create(entry.getKey(), true), entry.getValue());
            }
        }
        return overridePermissionsMap;
    }

    public List<PermSelector> getOverridePermissionsOrder() {
        if (overridePermissionsOrderList == null) {
            overridePermissionsOrderList = new ArrayList<>();
            for (String selector : overridePermissionsOrder) {
                if (!overridePermissions.containsKey(selector)) {
                    FactionsPlugin.getInstance().getLogger().warning("Found '" + selector + "' in overridePermissionsOrder but not in overridePermissions. Ignoring.");
                    continue;
                }
                overridePermissionsOrderList.add(PermSelectorRegistry.create(selector, true));
            }
        }
        return overridePermissionsOrderList;
    }
}
