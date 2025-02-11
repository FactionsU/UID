package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.PermissibleAction;
import com.massivecraft.factions.perms.Role;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;

public class CommandRequirements {

    // Permission required to execute command
    private final Permission permission;

    // Must be player
    private final boolean playerOnly;
    // Must be member of faction
    private final boolean memberOnly;

    // Must be at least this role
    private final Role role;

    // PermissibleAction check if the player has allowed for this before checking the role
    private final PermissibleAction action;

    // Commodore stuffs
    private final Class<? extends BrigadierProvider> brigadier;

    // Edge case handling
    private boolean errorOnManyArgs;
    private boolean disableOnLock;

    private CommandRequirements(Permission permission, boolean playerOnly, boolean memberOnly, Role role, PermissibleAction action, Class<? extends BrigadierProvider> brigadier) {
        this.permission = permission;
        this.playerOnly = playerOnly;
        this.memberOnly = memberOnly;
        this.role = role;
        this.action = action;
        this.brigadier = brigadier;
    }

    public boolean computeRequirements(CommandContext context, boolean informIfNot) {
        // Did not modify CommandRequirements return true
        if (permission == null) {
            return true;
        }

        if (context.player != null) {
            // Is Player
            if (!context.fPlayer.hasFaction() && memberOnly) {
                if (informIfNot) {
                    context.msg(TL.GENERIC_MEMBERONLY);
                }
                return false;
            }

            if (!FactionsPlugin.getInstance().getPermUtil().has(context.sender, permission.node, informIfNot)) {
                return false;
            }

            // Permissible Action provided compute that before role
            if (action != null) {
                boolean access = context.faction.hasAccess(context.fPlayer, action, context.fPlayer.getLastStoodAt());
                if (!access) {
                    if (informIfNot) {
                        context.msg(TL.GENERIC_NOPERMISSION, action.getShortDescription());
                    }
                    return false;
                }
                // They have been explicitly allowed
                return true;
            } else {
                if ((role != null && !context.fPlayer.getRole().isAtLeast(role)) && informIfNot) {
                    context.msg(TL.GENERIC_YOUMUSTBE, role.translation);
                }
                return role == null || context.fPlayer.getRole().isAtLeast(role);
            }
        } else {
            if (playerOnly) {
                if (informIfNot) {
                    context.sender.sendMessage(TL.GENERIC_PLAYERONLY.toString());
                }
                return false;
            }
            return context.sender.hasPermission(permission.node);
        }
    }

    public boolean isErrorOnManyArgs() {
        return errorOnManyArgs;
    }

    public boolean isDisableOnLock() {
        return disableOnLock;
    }

    public Class<? extends BrigadierProvider> getBrigadier() {
        return brigadier;
    }

    public static class Builder {

        private final Permission permission;

        private boolean playerOnly = false;
        private boolean memberOnly = false;

        private Role role = null;
        private PermissibleAction action;

        private Class<? extends BrigadierProvider> brigadier;

        private boolean errorOnManyArgs = true;
        private boolean disableOnLock = true;

        public Builder(Permission permission) {
            this.permission = permission;
        }

        public Builder playerOnly() {
            playerOnly = true;
            return this;
        }

        public Builder memberOnly() {
            playerOnly = true;
            memberOnly = true;
            return this;
        }

        public Builder withRole(Role role) {
            this.role = role;
            return this;
        }

        public Builder withAction(PermissibleAction action) {
            this.action = action;
            return this;
        }

        public Builder brigadier(Class<? extends BrigadierProvider> brigadier) {
            this.brigadier = brigadier;
            return this;
        }

        public CommandRequirements build() {
            CommandRequirements requirements = new CommandRequirements(permission, playerOnly, memberOnly, role, action, brigadier);
            requirements.errorOnManyArgs = errorOnManyArgs;
            requirements.disableOnLock = disableOnLock;
            return requirements;
        }

        public Builder noErrorOnManyArgs() {
            errorOnManyArgs = false;
            return this;
        }

        public Builder noDisableOnLock() {
            disableOnLock = false;
            return this;
        }

    }

}
