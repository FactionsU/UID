package com.massivecraft.factions.config.transition.oldclass.v0;

import com.massivecraft.factions.perms.PermissibleActions;
import com.massivecraft.factions.perms.PermissibleAction;

public enum OldPermissableActionV0 {
    BAN("ban", PermissibleActions.BAN),
    BUILD("build", PermissibleActions.BUILD),
    DESTROY("destroy", PermissibleActions.DESTROY),
    FROST_WALK("frostwalk", PermissibleActions.FROSTWALK),
    PAIN_BUILD("painbuild", PermissibleActions.PAINBUILD),
    DOOR("door", PermissibleActions.DOOR),
    BUTTON("button", PermissibleActions.BUTTON),
    LEVER("lever", PermissibleActions.LEVER),
    CONTAINER("container", PermissibleActions.CONTAINER),
    INVITE("invite", PermissibleActions.INVITE),
    KICK("kick", PermissibleActions.KICK),
    ITEM("items", PermissibleActions.ITEM), // generic for most items
    SETHOME("sethome", PermissibleActions.SETHOME),
    WITHDRAW("withdraw", PermissibleActions.ECONOMY),
    TERRITORY("territory", PermissibleActions.TERRITORY),
    ACCESS("access", PermissibleActions.PLATE),
    DISBAND("disband", PermissibleActions.DISBAND),
    PROMOTE("promote", PermissibleActions.PROMOTE),
    SETWARP("setwarp", PermissibleActions.SETWARP),
    WARP("warp", PermissibleActions.WARP),
    FLY("fly", PermissibleActions.FLY),
    ;

    private String name;

    private PermissibleAction action;

    OldPermissableActionV0(String name, PermissibleAction newPermissibleAction) {
        this.name = name;
        this.action = newPermissibleAction;
    }

    public PermissibleAction getNew() {
        return this.action;
    }

    /**
     * Get the friendly name of this action. Used for editing in commands.
     *
     * @return friendly name of the action as a String.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Case insensitive check for action.
     *
     * @param check check
     * @return permissible
     */
    public static OldPermissableActionV0 fromString(String check) {
        for (OldPermissableActionV0 permissableAction : values()) {
            if (permissableAction.name().equalsIgnoreCase(check)) {
                return permissableAction;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}
