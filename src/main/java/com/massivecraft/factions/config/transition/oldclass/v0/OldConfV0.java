package com.massivecraft.factions.config.transition.oldclass.v0;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class OldConfV0 {
    List<String> baseCommandAliases = new ArrayList<>();

    ChatColor colorMember = ChatColor.GREEN;
    ChatColor colorAlly = ChatColor.LIGHT_PURPLE;
    ChatColor colorTruce = ChatColor.DARK_PURPLE;
    ChatColor colorNeutral = ChatColor.WHITE;
    ChatColor colorEnemy = ChatColor.RED;

    ChatColor colorPeaceful = ChatColor.GOLD;
    ChatColor colorWilderness = ChatColor.GRAY;
    ChatColor colorSafezone = ChatColor.GOLD;
    ChatColor colorWar = ChatColor.DARK_RED;

    double powerPlayerMax = 10.0;
    double powerPlayerMin = -10.0;
    double powerPlayerStarting = 0.0;
    double powerPerMinute = 0.2;
    double powerPerDeath = 4.0;
    boolean powerRegenOffline = false;
    double powerOfflineLossPerDay = 0.0;
    double powerOfflineLossLimit = 0.0;
    double powerFactionMax = 0.0;

    String prefixAdmin = "***";
    String prefixColeader = "**";
    String prefixMod = "*";
    String prefixNormal = "+";
    String prefixRecruit = "-";

    boolean allowMultipleColeaders = false;

    int factionTagLengthMin = 3;
    int factionTagLengthMax = 10;
    boolean factionTagForceUpperCase = false;

    boolean newFactionsDefaultOpen = false;

    int factionMemberLimit = 0;

    String newPlayerStartingFactionID = "0";

    boolean showMapFactionKey = true;
    boolean showNeutralFactionsOnMap = true;
    boolean showEnemyFactionsOnMap = true;
    boolean showTruceFactionsOnMap = true;

    boolean canLeaveWithNegativePower = true;

    boolean factionOnlyChat = true;
    boolean chatTagEnabled = true;
    transient boolean chatTagHandledByAnotherPlugin = false;
    boolean chatTagRelationColored = true;
    String chatTagReplaceString = "[FACTION]";
    String chatTagInsertAfterString = "";
    String chatTagInsertBeforeString = "";
    int chatTagInsertIndex = 0;
    boolean chatTagPadBefore = false;
    boolean chatTagPadAfter = true;
    String chatTagFormat = "%s" + ChatColor.WHITE;
    boolean alwaysShowChatTag = true;
    String factionChatFormat = "%s:" + ChatColor.WHITE + " %s";
    String allianceChatFormat = ChatColor.LIGHT_PURPLE + "%s:" + ChatColor.WHITE + " %s";
    String truceChatFormat = ChatColor.DARK_PURPLE + "%s:" + ChatColor.WHITE + " %s";
    String modChatFormat = ChatColor.RED + "%s:" + ChatColor.WHITE + " %s";

    boolean broadcastDescriptionChanges = false;
    boolean broadcastTagChanges = false;

    double saveToFileEveryXMinutes = 30.0;

    double autoLeaveAfterDaysOfInactivity = 10.0;
    double autoLeaveRoutineRunsEveryXMinutes = 5.0;
    int autoLeaveRoutineMaxMillisecondsPerTick = 5;  // 1 server tick is roughly 50ms, so default max 10% of a tick
    boolean removePlayerDataWhenBanned = true;
    boolean autoLeaveDeleteFPlayerData = true; // Let them just remove player from Faction.

    boolean worldGuardChecking = false;
    boolean worldGuardBuildPriority = false;

    boolean logFactionCreate = true;
    boolean logFactionDisband = true;
    boolean logFactionJoin = true;
    boolean logFactionKick = true;
    boolean logFactionLeave = true;
    boolean logLandClaims = true;
    boolean logLandUnclaims = true;
    boolean logMoneyTransactions = true;
    boolean logPlayerCommands = true;

    boolean handleExploitObsidianGenerators = true;
    boolean handleExploitEnderPearlClipping = true;
    boolean handleExploitInteractionSpam = true;
    boolean handleExploitTNTWaterlog = false;
    boolean handleExploitLiquidFlow = false;

    boolean homesEnabled = true;
    boolean homesMustBeInClaimedTerritory = true;
    boolean homesTeleportToOnDeath = true;
    boolean homesRespawnFromNoPowerLossWorlds = true;
    boolean homesTeleportCommandEnabled = true;
    boolean homesTeleportCommandEssentialsIntegration = true;
    boolean homesTeleportCommandSmokeEffectEnabled = true;
    float homesTeleportCommandSmokeEffectThickness = 3f;
    boolean homesTeleportAllowedFromEnemyTerritory = true;
    boolean homesTeleportAllowedFromDifferentWorld = true;
    double homesTeleportAllowedEnemyDistance = 32.0;
    boolean homesTeleportIgnoreEnemiesIfInOwnTerritory = true;

    boolean disablePVPBetweenNeutralFactions = false;
    boolean disablePVPForFactionlessPlayers = false;
    boolean enablePVPAgainstFactionlessInAttackersLand = false;

    int noPVPDamageToOthersForXSecondsAfterLogin = 3;

    boolean peacefulTerritoryDisablePVP = true;
    boolean peacefulTerritoryDisableMonsters = false;
    boolean peacefulTerritoryDisableBoom = false;
    boolean peacefulMembersDisablePowerLoss = true;

    boolean permanentFactionsDisableLeaderPromotion = false;

    boolean claimsMustBeConnected = false;
    boolean claimsCanBeUnconnectedIfOwnedByOtherFaction = true;
    int claimsRequireMinFactionMembers = 1;
    int claimedLandsMax = 0;
    int lineClaimLimit = 5;

    int radiusClaimFailureLimit = 9;

    double considerFactionsReallyOfflineAfterXMinutes = 0.0;

    int actionDeniedPainAmount = 1;

    Set<String> permanentFactionMemberDenyCommands = new LinkedHashSet<>();

    Set<String> territoryNeutralDenyCommands = new LinkedHashSet<>();
    Set<String> territoryEnemyDenyCommands = new LinkedHashSet<>();
    Set<String> territoryAllyDenyCommands = new LinkedHashSet<>();
    Set<String> warzoneDenyCommands = new LinkedHashSet<>();
    Set<String> wildernessDenyCommands = new LinkedHashSet<>();

    boolean territoryBlockCreepers = false;
    boolean territoryBlockCreepersWhenOffline = false;
    boolean territoryBlockFireballs = false;
    boolean territoryBlockFireballsWhenOffline = false;
    boolean territoryBlockTNT = false;
    boolean territoryBlockTNTWhenOffline = false;
    boolean territoryDenyEndermanBlocks = true;
    boolean territoryDenyEndermanBlocksWhenOffline = true;

    boolean safeZoneDenyBuild = true;
    boolean safeZoneDenyUseage = true;
    boolean safeZoneBlockTNT = true;
    boolean safeZonePreventAllDamageToPlayers = false;
    boolean safeZoneDenyEndermanBlocks = true;

    boolean warZoneDenyBuild = true;
    boolean warZoneDenyUseage = true;
    boolean warZoneBlockCreepers = false;
    boolean warZoneBlockFireballs = false;
    boolean warZoneBlockTNT = true;
    boolean warZonePowerLoss = true;
    boolean warZoneFriendlyFire = false;
    boolean warZoneDenyEndermanBlocks = true;

    boolean wildernessDenyBuild = false;
    boolean wildernessDenyUseage = false;
    boolean wildernessBlockCreepers = false;
    boolean wildernessBlockFireballs = false;
    boolean wildernessBlockTNT = false;
    boolean wildernessPowerLoss = true;
    boolean wildernessDenyEndermanBlocks = false;

    boolean ownedAreasEnabled = true;
    int ownedAreasLimitPerFaction = 0;
    boolean ownedAreasModeratorsCanSet = false;
    boolean ownedAreaModeratorsBypass = true;
    boolean ownedAreaDenyBuild = true;
    boolean ownedAreaPainBuild = false;
    boolean ownedAreaProtectMaterials = true;
    boolean ownedAreaDenyUseage = true;

    boolean ownedMessageOnBorder = true;
    boolean ownedMessageInsideTerritory = true;
    boolean ownedMessageByChunk = false;

    boolean pistonProtectionThroughDenyBuild = true;

    Set<Material> territoryProtectedMaterials = Collections.emptySet();
    Set<Material> territoryDenyUseageMaterials = EnumSet.noneOf(Material.class);
    Set<Material> territoryProtectedMaterialsWhenOffline = EnumSet.noneOf(Material.class);
    Set<Material> territoryDenyUseageMaterialsWhenOffline = EnumSet.noneOf(Material.class);

    boolean econEnabled = false;
    String econUniverseAccount = "";
    double econCostClaimWilderness = 30.0;
    double econCostClaimFromFactionBonus = 30.0;
    double econOverclaimRewardMultiplier = 0.0;
    double econClaimAdditionalMultiplier = 0.5;
    double econClaimRefundMultiplier = 0.7;
    double econClaimUnconnectedFee = 0.0;
    double econCostCreate = 100.0;
    double econCostOwner = 15.0;
    double econCostSethome = 30.0;
    double econCostJoin = 0.0;
    double econCostLeave = 0.0;
    double econCostKick = 0.0;
    double econCostInvite = 0.0;
    double econCostHome = 0.0;
    double econCostTag = 0.0;
    double econCostDesc = 0.0;
    double econCostTitle = 0.0;
    double econCostList = 0.0;
    double econCostMap = 0.0;
    double econCostPower = 0.0;
    double econCostShow = 0.0;
    double econCostStuck = 0.0;
    double econCostOpen = 0.0;
    double econCostAlly = 0.0;
    double econCostTruce = 0.0;
    double econCostEnemy = 0.0;
    double econCostNeutral = 0.0;
    double econCostNoBoom = 0.0;

    boolean bankEnabled = true;
    boolean bankMembersCanWithdraw = false; //Have to be at least moderator to withdraw or pay money to another faction
    boolean bankFactionPaysCosts = true; //The faction pays for faction command costs, such as sethome
    boolean bankFactionPaysLandCosts = true; //The faction pays for land claiming costs.

    Set<String> playersWhoBypassAllProtection = new LinkedHashSet<>();

    Set<String> worldsNoClaiming = new LinkedHashSet<>();
    Set<String> worldsNoPowerLoss = new LinkedHashSet<>();
    Set<String> worldsIgnorePvP = new LinkedHashSet<>();
    Set<String> worldsNoWildernessProtection = new LinkedHashSet<>();

    String vaultPrefix = "faction-%s";
    int defaultMaxVaults = 0;

    int mapHeight = 17;
    int mapWidth = 49;
}
