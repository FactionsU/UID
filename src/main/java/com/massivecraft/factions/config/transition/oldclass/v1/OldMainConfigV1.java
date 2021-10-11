package com.massivecraft.factions.config.transition.oldclass.v1;

import com.massivecraft.factions.config.annotation.Comment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"InnerClassMayBeStatic", "unused"})
public class OldMainConfigV1 {
    public class Colors {
        public class Relations {
            public String member = "GREEN";
            public String ally = "LIGHT_PURPLE";
            public String truce = "DARK_PURPLE";
            public String neutral = "WHITE";
            public String enemy = "RED";
            public String peaceful = "GOLD";
        }

        public class Factions {
            public String wilderness = "GRAY";
            public String safezone = "GOLD";
            public String warzone = "DARK_RED";
        }

        public Factions factions = new Factions();
        public Relations relations = new Relations();
    }

    public class Factions {
        public class LandRaidControl {
            public class Power {
                public double playerMin = -10.0D;
                public double playerMax = 10.0D;
                public double playerStarting = 0.0D;
                @Comment("Default health rate of 0.2 takes 5 minutes to recover one power")
                public double powerPerMinute = 0.2;
                @Comment("How much is lost on death")
                public double lossPerDeath = 4.0;
                @Comment("Does a player regenerate power while offline?")
                public boolean regenOffline = false;
                @Comment("A player loses this much per day offline")
                public double offlineLossPerDay = 0.0;
                @Comment("A player stops losing power from being offline once they reach this amount")
                public double offlineLossLimit = 0.0;
                @Comment("If greater than 0, used as a cap for how much power a faction can have\nAdditional power from players beyond this acts as a \"buffer\" of sorts")
                public double factionMax = 0.0;
                public boolean respawnHomeFromNoPowerLossWorlds = true;
                public Set<String> worldsNoPowerLoss = new HashSet<>();
                public boolean peacefulMembersDisablePowerLoss = true;
                public boolean warZonePowerLoss = true;
                public boolean wildernessPowerLoss = true;
                @Comment("Disallow joining/leaving/kicking while power is negative")
                public boolean canLeaveWithNegativePower = true;
            }

            @Comment("Sets the mode of land/raid control")
            public String system = "power";
            @Comment("Controls the power system of land/raid control\nSet the 'system' value to 'power' to use this system")
            public Power power = new Power();
        }

        public class Prefix {
            public String admin = "***";
            public String coleader = "**";
            public String mod = "*";
            public String normal = "+";
            public String recruit = "-";

            public String getAdmin() {
                return admin;
            }

            public String getColeader() {
                return coleader;
            }

            public String getMod() {
                return mod;
            }

            public String getNormal() {
                return normal;
            }

            public String getRecruit() {
                return recruit;
            }
        }

        public class Chat {
            @Comment("Allow for players to chat only within their faction, with allies, etc.\n" +
                    "Set to false to only allow public chats through this plugin.")
            public boolean factionOnlyChat = true;
            // Configuration on the Faction tag in chat messages.
            @Comment("If true, disables adding of faction tag so another plugin can manage this")
            public boolean tagHandledByAnotherPlugin = false;
            public boolean tagRelationColored = true;
            public String tagReplaceString = "[FACTION]";
            public String tagInsertAfterString = "";
            public String tagInsertBeforeString = "";
            public int tagInsertIndex = 0;
            public boolean tagPadBefore = false;
            public boolean tagPadAfter = true;
            public String tagFormat = "%s\u00A7f";
            public boolean alwaysShowChatTag = true;
            public String factionChatFormat = "%s:\u00A7f %s";
            public String allianceChatFormat = "\u00A7d%s:\u00A7f %s";
            public String truceChatFormat = "\u00A75%s:\u00A7f %s";
            public String modChatFormat = "\u00A7c%s:\u00A7f %s";
            public boolean broadcastDescriptionChanges = false;
            public boolean broadcastTagChanges = false;
        }

        public class Homes {
            public boolean enabled = true;
            public boolean mustBeInClaimedTerritory = true;
            public boolean teleportToOnDeath = true;
            public boolean teleportCommandEnabled = true;
            public boolean teleportCommandEssentialsIntegration = true;
            public boolean teleportCommandSmokeEffectEnabled = true;
            public float teleportCommandSmokeEffectThickness = 3f;
            public boolean teleportAllowedFromEnemyTerritory = true;
            public boolean teleportAllowedFromDifferentWorld = true;
            public double teleportAllowedEnemyDistance = 32.0;
            public boolean teleportIgnoreEnemiesIfInOwnTerritory = true;
        }

        public class PVP {
            public boolean disablePVPBetweenNeutralFactions = false;
            public boolean disablePVPForFactionlessPlayers = false;
            public boolean enablePVPAgainstFactionlessInAttackersLand = false;
            public int noPVPDamageToOthersForXSecondsAfterLogin = 3;
            public Set<String> worldsIgnorePvP = new HashSet<>();
        }

        public class SpecialCase {
            public boolean peacefulTerritoryDisablePVP = true;
            public boolean peacefulTerritoryDisableMonsters = false;
            public boolean peacefulTerritoryDisableBoom = false;
            public boolean permanentFactionsDisableLeaderPromotion = false;
        }

        public class Claims {
            public boolean mustBeConnected = false;
            public boolean canBeUnconnectedIfOwnedByOtherFaction = true;
            public int requireMinFactionMembers = 1;
            public int landsMax = 0;
            public int lineClaimLimit = 5;
            @Comment("If someone is doing a radius claim and the process fails to claim land this many times in a row, it will exit")
            public int radiusClaimFailureLimit = 9;
            public Set<String> worldsNoClaiming = new HashSet<>();
        }

        public class Protection {
            @Comment("Commands which will be prevented if the player is a member of a permanent faction")
            public Set<String> permanentFactionMemberDenyCommands = new HashSet<>();

            @Comment("Commands which will be prevented when in claimed territory of a neutral faction")
            public Set<String> territoryNeutralDenyCommands = new HashSet<>();
            @Comment("Commands which will be prevented when in claimed territory of an enemy faction")
            public Set<String> territoryEnemyDenyCommands = new HashSet<String>() {
                {
                    this.add("home");
                    this.add("sethome");
                    this.add("spawn");
                    this.add("tpahere");
                    this.add("tpaccept");
                    this.add("tpa");
                }
            };
            @Comment("Commands which will be prevented when in claimed territory of an ally faction")
            public Set<String> territoryAllyDenyCommands = new HashSet<>();
            @Comment("Commands which will be prevented when in warzone")
            public Set<String> warzoneDenyCommands = new HashSet<>();
            @Comment("Commands which will be prevented when in wilderness")
            public Set<String> wildernessDenyCommands = new HashSet<>();

            public boolean territoryBlockCreepers = false;
            public boolean territoryBlockCreepersWhenOffline = false;
            public boolean territoryBlockFireballs = false;
            public boolean territoryBlockFireballsWhenOffline = false;
            public boolean territoryBlockTNT = false;
            public boolean territoryBlockTNTWhenOffline = false;
            public boolean territoryDenyEndermanBlocks = true;
            public boolean territoryDenyEndermanBlocksWhenOffline = true;

            public boolean safeZoneDenyBuild = true;
            public boolean safeZoneDenyUsage = true;
            public boolean safeZoneBlockTNT = true;
            public boolean safeZonePreventAllDamageToPlayers = false;
            public boolean safeZoneDenyEndermanBlocks = true;

            public boolean warZoneDenyBuild = true;
            public boolean warZoneDenyUsage = true;
            public boolean warZoneBlockCreepers = true;
            public boolean warZoneBlockFireballs = true;
            public boolean warZoneBlockTNT = true;
            public boolean warZoneFriendlyFire = false;
            public boolean warZoneDenyEndermanBlocks = true;

            public boolean wildernessDenyBuild = false;
            public boolean wildernessDenyUsage = false;
            public boolean wildernessBlockCreepers = false;
            public boolean wildernessBlockFireballs = false;
            public boolean wildernessBlockTNT = false;

            public boolean wildernessDenyEndermanBlocks = false;

            public boolean pistonProtectionThroughDenyBuild = true;

            public Set<String> territoryDenyUsageMaterials = new HashSet<>();
            public Set<String> territoryDenyUsageMaterialsWhenOffline = new HashSet<>();

            @Comment("Mainly for other plugins/mods that use a fake player to take actions, which shouldn't be subject to our protections")
            public Set<String> playersWhoBypassAllProtection = new HashSet<>();
            public Set<String> worldsNoWildernessProtection = new HashSet<>();

            public Protection() {
                protectUsage("FIRE_CHARGE");
                protectUsage("FLINT_AND_STEEL");
                protectUsage("BUCKET");
                protectUsage("WATER_BUCKET");
                protectUsage("LAVA_BUCKET");
            }

            public void protectUsage(String material) {
                territoryDenyUsageMaterials.add(material);
                territoryDenyUsageMaterialsWhenOffline.add(material);
            }
        }

        public class OwnedArea {
            public boolean enabled = true;
            public int limitPerFaction = 0;
            public boolean moderatorsBypass = true;
            public boolean denyBuild = true;
            public boolean painBuild = false;
            public boolean protectMaterials = true;
            public boolean denyUsage = true;

            public boolean messageOnBorder = true;
            public boolean messageInsideTerritory = true;
            public boolean messageByChunk = false;
        }

        public Chat chat = new Chat();
        public Homes homes = new Homes();
        public PVP pvp = new PVP();
        public SpecialCase specialCase = new SpecialCase();
        public Claims claims = new Claims();
        public Protection protection = new Protection();
        @Comment("For claimed areas where further faction-member ownership can be defined")
        public OwnedArea ownedArea = new OwnedArea();
        @Comment("Displayed prefixes for different roles within a faction")
        public Prefix prefixes = new Prefix();
        public LandRaidControl landRaidControl = new LandRaidControl();

        public boolean allowMultipleColeaders = false;

        @Comment("Minimum faction tag length")
        public int tagLengthMin = 3;
        @Comment("Maximum faction tag length")
        public int tagLengthMax = 10;
        public boolean tagForceUpperCase = false;

        public boolean newFactionsDefaultOpen = false;

        @Comment("When faction membership hits this limit, players will no longer be able to join using /f join; default is 0, no limit")
        public int factionMemberLimit = 0;

        @Comment("What faction ID to start new players in when they first join the server; default is 0, \"no faction\"")
        public String newPlayerStartingFactionID = "0";

        public double saveToFileEveryXMinutes = 30.0;

        public double autoLeaveAfterDaysOfInactivity = 10.0;
        public double autoLeaveRoutineRunsEveryXMinutes = 5.0;
        public int autoLeaveRoutineMaxMillisecondsPerTick = 5;  // 1 server tick is roughly 50ms, so default max 10% of a tick
        public boolean removePlayerDataWhenBanned = true;
        public boolean autoLeaveDeleteFPlayerData = true; // Let them just remove player from Faction.
        public double considerFactionsReallyOfflineAfterXMinutes = 0.0;
        public int actionDeniedPainAmount = 1;

        @Comment("If enabled, perms can be managed separately for when the faction is offline")
        public boolean separateOfflinePerms = false;
    }

    public class Logging {
        public boolean factionCreate = true;
        public boolean factionDisband = true;
        public boolean factionJoin = true;
        public boolean factionKick = true;
        public boolean factionLeave = true;
        public boolean landClaims = true;
        public boolean landUnclaims = true;
        public boolean moneyTransactions = true;
        public boolean playerCommands = true;
    }

    public class Exploits {
        public boolean obsidianGenerators = true;
        public boolean enderPearlClipping = true;
        public boolean interactionSpam = true;
        public boolean tntWaterlog = false;
        public boolean liquidFlow = false;
        public boolean preventDuping = true;
    }

    public class Economy {
        public boolean enabled = false;
        public String universeAccount = "";
        public double costClaimWilderness = 30.0;
        public double costClaimFromFactionBonus = 30.0;
        public double overclaimRewardMultiplier = 0.0;
        public double claimAdditionalMultiplier = 0.5;
        public double claimRefundMultiplier = 0.7;
        public double claimUnconnectedFee = 0.0;
        public double costCreate = 100.0;
        public double costOwner = 15.0;
        public double costSethome = 30.0;
        public double costDelhome = 30.0;
        public double costJoin = 0.0;
        public double costLeave = 0.0;
        public double costKick = 0.0;
        public double costInvite = 0.0;
        public double costHome = 0.0;
        public double costTag = 0.0;
        public double costDesc = 0.0;
        public double costTitle = 0.0;
        public double costList = 0.0;
        public double costMap = 0.0;
        public double costPower = 0.0;
        public double costShow = 0.0;
        public double costStuck = 0.0;
        public double costOpen = 0.0;
        public double costAlly = 0.0;
        public double costTruce = 0.0;
        public double costEnemy = 0.0;
        public double costNeutral = 0.0;
        public double costNoBoom = 0.0;

        @Comment("Faction banks, to pay for land claiming and other costs instead of individuals paying for them")
        public boolean bankEnabled = true;
        @Comment("Have to be at least moderator to withdraw or pay money to another faction")
        public boolean bankMembersCanWithdraw = false;
        @Comment("The faction pays for faction command costs, such as sethome")
        public boolean bankFactionPaysCosts = true;
        @Comment("The faction pays for land claiming costs.")
        public boolean bankFactionPaysLandCosts = true;
    }

    public class Map {
        public int height = 17;
        public int width = 49;
        public boolean showFactionKey = true;
        public boolean showNeutralFactionsOnMap = true;
        public boolean showEnemyFactions = true;
        public boolean showTruceFactions = true;
    }

    public class Data {
        public class Json {
            @Comment("If true, data files will be stored without extra whitespace and linebreaks.\n" +
                    "This becomes less readable, but can cut storage use in half.")
            public boolean efficientStorage = false;
        }

        @Comment("Presently, the only option is JSON.")
        public String storage = "JSON";
        public Json json = new Json();
    }

    public class RestrictWorlds {
        @Comment("If true, Factions will only function on certain worlds")
        public boolean restrictWorlds = false;
        @Comment("If restrictWorlds is true, this setting determines if the world list below is a whitelist or blacklist.\n" +
                "True for whitelist, false for blacklist.")
        public boolean whitelist = true;
        public Set<String> worldList = new HashSet<>();
    }

    public class PlayerVaults {
        @Comment("The %s is for the faction id")
        public String vaultPrefix = "faction-%s";
        public int defaultMaxVaults = 0;
    }

    public class WorldGuard {
        public boolean checking;
        public boolean buildPriority;
    }

    @Comment("The command base (by default f, making the command /f)")
    public List<String> commandBase = new ArrayList<String>() {
        {
            this.add("f");
        }
    };

    @Comment("Colors for relationships and default factions")
    public Colors colors = new Colors();

    public Factions factions = new Factions();
    @Comment("What should be logged?")
    public Logging logging = new Logging();
    @Comment("Controls certain exploit preventions")
    public Exploits exploits = new Exploits();
    @Comment("Economy support requires Vault and a compatible economy plugin")
    public Economy economy = new Economy();
    @Comment("Control for the default settings of /f map")
    public Map map = new Map();
    @Comment("Data storage settings")
    public Data data = new Data();
    public RestrictWorlds restrictWorlds = new RestrictWorlds();
    @Comment("PlayerVaults faction vault settings")
    public PlayerVaults playerVaults = new PlayerVaults();
    @Comment("WorldGuard settings")
    public WorldGuard worldGuard = new WorldGuard();
}