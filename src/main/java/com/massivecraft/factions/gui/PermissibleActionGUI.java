package com.massivecraft.factions.gui;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.Permissible;
import com.massivecraft.factions.perms.PermissibleAction;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.util.TL;
import com.massivecraft.factions.util.material.MaterialDb;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;

import java.util.HashMap;
import java.util.Map;

public class PermissibleActionGUI extends GUI<PermissibleAction> implements GUI.Backable {
    private static final SimpleItem backItem = SimpleItem.builder().setMaterial(MaterialDb.get("ARROW")).setName(TL.GUI_BUTTON_BACK.toString()).build();
    private static final SimpleItem base;
    private static final String locked;
    private static final String allow;
    private static final String deny;
    private static final String allowLower;
    private static final String denyLower;

    static {
        base = SimpleItem.builder().setLore(FactionsPlugin.getInstance().conf().commands().perms().getGuiLore()).setName("&8[{action-access-color}{action}&8]").build();
        locked = ' ' + TL.GUI_PERMS_ACTION_LOCKED.toString();
        allow = TL.GUI_PERMS_ACTION_ALLOW.toString();
        allowLower = allow.toLowerCase();
        deny = TL.GUI_PERMS_ACTION_DENY.toString();
        denyLower = deny.toLowerCase();
    }

    private final Permissible permissible;
    private final boolean online;

    public PermissibleActionGUI(boolean online, FPlayer user, Permissible permissible) {
        super(user, (int) Math.ceil(((double) PermissibleAction.values().length) / 9d) + 1);
        this.permissible = permissible;
        this.online = online;
        build();
    }

    @Override
    protected String getName() {
        String bit = FactionsPlugin.getInstance().conf().factions().other().isSeparateOfflinePerms() ?
                TL.GUI_PERMS_ACTION_ONLINEOFFLINEBIT.format(online ? TL.GUI_PERMS_ONLINE.toString() : TL.GUI_PERMS_OFFLINE)
                : "";
        return TL.GUI_PERMS_ACTION_NAME.format(permissible.name().toLowerCase(), bit);
    }

    @Override
    protected String parse(String toParse, PermissibleAction action) {
        String actionName = action.getShortDescription().substring(0, 1).toUpperCase() + action.getShortDescription().substring(1);
        toParse = toParse.replace("{action}", actionName);

        boolean access = user.getFaction().hasAccess(online, permissible, action);
        String extra = "";
        if (user.getFaction().isLocked(online, permissible, action)) {
            extra = locked;
        }

        toParse = toParse.replace("{action-access}", (access ? allow : deny) + extra);
        toParse = toParse.replace("{action-access-color}", access ? ChatColor.GREEN.toString() : ChatColor.DARK_RED.toString());
        toParse = toParse.replace("{action-desc}", action.getDescription());

        return toParse;
    }

    @Override
    public void click(int slot, ClickType clickType) {
        if (FactionsPlugin.getInstance().conf().factions().other().isSeparateOfflinePerms() && permissible instanceof Relation && slot == this.back + 4) {
            new PermissibleActionGUI(!online, user, permissible).open();
        } else {
            super.click(slot, clickType);
        }
    }

    @Override
    protected void onClick(PermissibleAction action, ClickType click) {
        boolean access;
        if (click == ClickType.LEFT) {
            access = true;
        } else if (click == ClickType.RIGHT) {
            access = false;
        } else {
            return;
        }
        if (user.getFaction().setPermission(online, permissible, action, access)) {
            // Reload item to reparse placeholders
            buildItem(action);
            user.msg(TL.COMMAND_PERM_SET, action.getShortDescription(), access ? allowLower : denyLower, permissible.name());
            FactionsPlugin.getInstance().log(TL.COMMAND_PERM_SET.format(action.getShortDescription(), access ? "Allow" : "Deny", permissible.name()) + " for faction " + user.getTag());
        } else {
            user.msg(TL.COMMAND_PERM_INVALID_SET);
        }
    }

    @Override
    protected Map<Integer, PermissibleAction> createSlotMap() {
        Map<Integer, PermissibleAction> map = new HashMap<>();
        int i = 0;
        for (PermissibleAction action : PermissibleAction.values()) {
            if (this.permissible instanceof Relation && action.isFactionOnly()) {
                continue;
            }
            map.put(i++, action);
        }
        return map;
    }

    @Override
    protected SimpleItem getItem(PermissibleAction permissibleAction) {
        SimpleItem item = new SimpleItem(base);

        item.setEnchant(user.getFaction().hasAccess(online, permissible, permissibleAction));
        Material material = permissibleAction.getMaterial();
        item.setMaterial(material == Material.AIR ? Material.STONE : material);
        return item;
    }

    @Override
    protected Map<Integer, SimpleItem> createDummyItems() {
        Map<Integer, SimpleItem> map = new HashMap<>();
        map.put(this.back = ((PermissibleAction.values().length / 9) + 1) * 9, backItem);
        if (FactionsPlugin.getInstance().conf().factions().other().isSeparateOfflinePerms() && permissible instanceof Relation) {
            map.put(this.back + 4, PermissibleRelationGUI.offlineSwitch);
        }
        return map;
    }

    // For dummy items only parseDefault is called, but we want to provide the relation placeholders, so: Override
    @Override
    protected String parseDefault(String string) {
        String permissibleName = permissible.toString().substring(0, 1).toUpperCase() + permissible.toString().substring(1);
        String parsed = string.replace("{relation-color}", permissible.getColor().toString());
        parsed = parsed.replace("{relation}", permissibleName);
        return super.parseDefault(parsed);
    }

    @Override
    public void onBack() {
        new PermissibleRelationGUI(online, user).open();
    }
}
