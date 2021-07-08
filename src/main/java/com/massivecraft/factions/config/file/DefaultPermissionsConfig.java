package com.massivecraft.factions.config.file;

import com.massivecraft.factions.config.annotation.Comment;
import com.massivecraft.factions.perms.Permissible;
import com.massivecraft.factions.perms.PermissiblePermDefaultInfo;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.perms.Role;

@SuppressWarnings("FieldMayBeFinal")
public class DefaultPermissionsConfig {
    public static class Permissions {
        public static class FactionOnlyPermInfo {
            protected PermissiblePermDefaultInfo coleader = PermissiblePermDefaultInfo.defaultFalse();
            protected PermissiblePermDefaultInfo moderator = PermissiblePermDefaultInfo.defaultFalse();
            protected PermissiblePermDefaultInfo normal = PermissiblePermDefaultInfo.defaultFalse();
            protected PermissiblePermDefaultInfo recruit = PermissiblePermDefaultInfo.defaultFalse();

            public PermissiblePermDefaultInfo get(Permissible permissible) {
                if (permissible instanceof Role) {
                    switch ((Role) permissible) {
                        case COLEADER:
                            return this.coleader;
                        case MODERATOR:
                            return this.moderator;
                        case NORMAL:
                            return this.normal;
                        case RECRUIT:
                            return this.recruit;
                    }
                }
                // TODO print warning
                return null;
            }
        }

        public static class FullPermInfo extends FactionOnlyPermInfo {
            protected PermissiblePermDefaultInfo ally = PermissiblePermDefaultInfo.defaultFalse();
            protected PermissiblePermDefaultInfo truce = PermissiblePermDefaultInfo.defaultFalse();
            protected PermissiblePermDefaultInfo neutral = PermissiblePermDefaultInfo.defaultFalse();
            protected PermissiblePermDefaultInfo enemy = PermissiblePermDefaultInfo.defaultFalse();

            public PermissiblePermDefaultInfo get(Permissible permissible) {
                if (permissible instanceof Relation) {
                    switch ((Relation) permissible) {
                        case ALLY:
                            return this.ally;
                        case TRUCE:
                            return this.truce;
                        case NEUTRAL:
                            return this.neutral;
                        case ENEMY:
                            return this.enemy;
                    }
                }
                return super.get(permissible);
            }
        }

        public FactionOnlyPermInfo getBan() {
            return this.ban;
        }

        public FullPermInfo getBuild() {
            return this.build;
        }

        public FullPermInfo getDestroy() {
            return this.destroy;
        }

        public FullPermInfo getFrostWalk() {
            return this.frostWalk;
        }

        public FullPermInfo getPainBuild() {
            return this.painBuild;
        }

        public FullPermInfo getDoor() {
            return this.door;
        }

        public FullPermInfo getButton() {
            return this.button;
        }

        public FullPermInfo getLever() {
            return this.lever;
        }

        public FullPermInfo getContainer() {
            return this.container;
        }

        public FactionOnlyPermInfo getInvite() {
            return this.invite;
        }

        public FactionOnlyPermInfo getKick() {
            return this.kick;
        }

        public FullPermInfo getItem() {
            return this.item;
        }

        public FullPermInfo getHome() {
            return this.home;
        }

        public FactionOnlyPermInfo getSetHome() {
            return this.sethome;
        }

        public FactionOnlyPermInfo getListClaims() {
            return this.listClaims;
        }

        public FactionOnlyPermInfo getEconomy() {
            return this.economy;
        }

        public FactionOnlyPermInfo getTerritory() {
            return this.territory;
        }

        public FactionOnlyPermInfo getTNTDeposit() {
            return this.tntDeposit;
        }

        public FactionOnlyPermInfo getTNTWithdraw() {
            return this.tntWithdraw;
        }

        public FactionOnlyPermInfo getOwner() {
            return this.owner;
        }

        public FullPermInfo getPlate() {
            return this.plate;
        }

        public FactionOnlyPermInfo getDisband() {
            return this.disband;
        }

        public FactionOnlyPermInfo getPromote() {
            return this.promote;
        }

        public FactionOnlyPermInfo getSetWarp() {
            return this.setwarp;
        }

        public FullPermInfo getWarp() {
            return this.warp;
        }

        public FullPermInfo getFly() {
            return this.fly;
        }

        @Comment("Can ban others from the faction")
        private FactionOnlyPermInfo ban = new FactionOnlyPermInfo() {
            {
                this.coleader= PermissiblePermDefaultInfo.defaultTrue();
                this.moderator= PermissiblePermDefaultInfo.defaultTrue();
            }
        };
        @Comment("Can build in faction territory (while not raidable)")
        private FullPermInfo build = new FullPermInfo() {
            {
                this.coleader= PermissiblePermDefaultInfo.defaultTrue();
                this.moderator= PermissiblePermDefaultInfo.defaultTrue();
                this.normal= PermissiblePermDefaultInfo.defaultTrue();
                this.recruit= PermissiblePermDefaultInfo.defaultTrue();
            }
        };
        @Comment("Can destroy in faction territory (while not raidable)")
        private FullPermInfo destroy = new FullPermInfo() {
            {
                this.coleader= PermissiblePermDefaultInfo.defaultTrue();
                this.moderator= PermissiblePermDefaultInfo.defaultTrue();
                this.normal= PermissiblePermDefaultInfo.defaultTrue();
                this.recruit= PermissiblePermDefaultInfo.defaultTrue();
            }
        };
        @Comment("Can frost walk in faction territory (while not raidable)")
        private FullPermInfo frostWalk = new FullPermInfo() {
            {
                this.coleader= PermissiblePermDefaultInfo.defaultTrue();
                this.moderator= PermissiblePermDefaultInfo.defaultTrue();
                this.normal= PermissiblePermDefaultInfo.defaultTrue();
                this.recruit= PermissiblePermDefaultInfo.defaultTrue();
            }
        };
        @Comment("Allows building/destroying in faction territory but causes pain (while not raidable)")
        private FullPermInfo painBuild = new FullPermInfo();
        @Comment("Use doors in faction territory (while not raidable)")
        private FullPermInfo door = new FullPermInfo() {
            {
                this.coleader= PermissiblePermDefaultInfo.defaultTrue();
                this.moderator= PermissiblePermDefaultInfo.defaultTrue();
                this.normal= PermissiblePermDefaultInfo.defaultTrue();
                this.recruit= PermissiblePermDefaultInfo.defaultTrue();
                this.ally= PermissiblePermDefaultInfo.defaultTrue();
            }
        };
        @Comment("Use buttons in faction territory (while not raidable)")
        private FullPermInfo button = new FullPermInfo() {
            {
                this.coleader= PermissiblePermDefaultInfo.defaultTrue();
                this.moderator= PermissiblePermDefaultInfo.defaultTrue();
                this.normal= PermissiblePermDefaultInfo.defaultTrue();
                this.recruit= PermissiblePermDefaultInfo.defaultTrue();
                this.ally= PermissiblePermDefaultInfo.defaultTrue();
            }
        };
        @Comment("Use levers in faction territory (while not raidable)")
        private FullPermInfo lever = new FullPermInfo() {
            {
                this.coleader= PermissiblePermDefaultInfo.defaultTrue();
                this.moderator= PermissiblePermDefaultInfo.defaultTrue();
                this.normal= PermissiblePermDefaultInfo.defaultTrue();
                this.recruit= PermissiblePermDefaultInfo.defaultTrue();
                this.ally= PermissiblePermDefaultInfo.defaultTrue();
            }
        };
        @Comment("Use containers in faction territory (while not raidable)")
        private FullPermInfo container = new FullPermInfo() {
            {
                this.coleader= PermissiblePermDefaultInfo.defaultTrue();
                this.moderator= PermissiblePermDefaultInfo.defaultTrue();
                this.normal= PermissiblePermDefaultInfo.defaultTrue();
                this.recruit= PermissiblePermDefaultInfo.defaultTrue();
            }
        };
        @Comment("Able to invite others to the faction")
        private FactionOnlyPermInfo invite = new FactionOnlyPermInfo() {
            {
                this.coleader= PermissiblePermDefaultInfo.defaultTrue();
                this.moderator= PermissiblePermDefaultInfo.defaultTrue();
            }
        };
        private FactionOnlyPermInfo kick = new FactionOnlyPermInfo() {
            {
                this.coleader= PermissiblePermDefaultInfo.defaultTrue();
                this.moderator= PermissiblePermDefaultInfo.defaultTrue();
            }
        };
        @Comment("Use items in faction territory (while not raidable)")
        private FullPermInfo item = new FullPermInfo() {
            {
                this.coleader= PermissiblePermDefaultInfo.defaultTrue();
                this.moderator= PermissiblePermDefaultInfo.defaultTrue();
                this.normal= PermissiblePermDefaultInfo.defaultTrue();
                this.recruit= PermissiblePermDefaultInfo.defaultTrue();
            }
        };
        @Comment("Can visit the faction home")
        private FullPermInfo home = new FullPermInfo() {
            {
                this.coleader= PermissiblePermDefaultInfo.defaultTrue();
                this.moderator= PermissiblePermDefaultInfo.defaultTrue();
                this.normal= PermissiblePermDefaultInfo.defaultTrue();
                this.recruit= PermissiblePermDefaultInfo.defaultTrue();
            }
        };
        @Comment("Can see faction claim list")
        private FactionOnlyPermInfo listClaims = new FactionOnlyPermInfo() {
            {
                this.coleader= PermissiblePermDefaultInfo.defaultTrue();
                this.moderator= PermissiblePermDefaultInfo.defaultTrue();
            }
        };
        @Comment("Can set the faction home")
        private FactionOnlyPermInfo sethome = new FactionOnlyPermInfo() {
            {
                this.coleader= PermissiblePermDefaultInfo.defaultTrue();
            }
        };
        @Comment("Can access faction economy")
        private FactionOnlyPermInfo economy = new FactionOnlyPermInfo() {
            {
                this.coleader= PermissiblePermDefaultInfo.defaultTrue();
            }
        };
        @Comment("Can claim/unclaim faction territory")
        private FactionOnlyPermInfo territory = new FactionOnlyPermInfo() {
            {
                this.coleader= PermissiblePermDefaultInfo.defaultTrue();
                this.moderator= PermissiblePermDefaultInfo.defaultTrue();
            }
        };
        @Comment("Can deposit TNT into the bank")
        private FactionOnlyPermInfo tntDeposit = new FactionOnlyPermInfo() {
            {
                this.coleader= PermissiblePermDefaultInfo.defaultTrue();
                this.moderator= PermissiblePermDefaultInfo.defaultTrue();
                this.normal= PermissiblePermDefaultInfo.defaultTrue();
                this.recruit= PermissiblePermDefaultInfo.defaultTrue();
            }
        };
        @Comment("Can withdraw TNT from the bank")
        private FactionOnlyPermInfo tntWithdraw = new FactionOnlyPermInfo() {
            {
                this.coleader= PermissiblePermDefaultInfo.defaultTrue();
                this.moderator= PermissiblePermDefaultInfo.defaultTrue();
            }
        };
        @Comment("Can created owned areas with /f owner")
        private FactionOnlyPermInfo owner = new FactionOnlyPermInfo();
        @Comment("Can interact with plates")
        private FullPermInfo plate = new FullPermInfo() {
            {
                this.coleader= PermissiblePermDefaultInfo.defaultTrue();
                this.moderator= PermissiblePermDefaultInfo.defaultTrue();
                this.normal= PermissiblePermDefaultInfo.defaultTrue();
                this.recruit= PermissiblePermDefaultInfo.defaultTrue();
                this.ally= PermissiblePermDefaultInfo.defaultTrue();
            }
        };
        private FactionOnlyPermInfo disband = new FactionOnlyPermInfo();
        @Comment("Can promote members up to their own role within the faction")
        private FactionOnlyPermInfo promote = new FactionOnlyPermInfo() {
            {
                this.coleader= PermissiblePermDefaultInfo.defaultTrue();
                this.moderator= PermissiblePermDefaultInfo.defaultTrue();
            }
        };
        @Comment("Can set a faction warp")
        private FactionOnlyPermInfo setwarp = new FactionOnlyPermInfo() {
            {
                this.coleader= PermissiblePermDefaultInfo.defaultTrue();
                this.moderator= PermissiblePermDefaultInfo.defaultTrue();
            }
        };
        @Comment("Can use faction warps")
        private FullPermInfo warp = new FullPermInfo() {
            {
                this.coleader= PermissiblePermDefaultInfo.defaultTrue();
                this.moderator= PermissiblePermDefaultInfo.defaultTrue();
                this.normal= PermissiblePermDefaultInfo.defaultTrue();
                this.recruit= PermissiblePermDefaultInfo.defaultTrue();
            }
        };
        @Comment("Can fly in faction territory")
        private FullPermInfo fly = new FullPermInfo() {
            {
                this.coleader= PermissiblePermDefaultInfo.defaultTrue();
                this.moderator= PermissiblePermDefaultInfo.defaultTrue();
                this.normal= PermissiblePermDefaultInfo.defaultTrue();
                this.recruit= PermissiblePermDefaultInfo.defaultTrue();
                this.ally= PermissiblePermDefaultInfo.defaultTrue();
            }
        };
    }

    @Comment("Permissions settings\n" +
            "Each main section represents one permission.\n" +
            "Inside is each relation.\n" +
            "Each relation has a default value (true=allowed, false=disallowed)\n" +
            "  and true/false for if it's locked to editing by factions admins.")
    private Permissions permissions = new Permissions();

    public Permissions getPermissions() {
        return this.permissions;
    }
}
