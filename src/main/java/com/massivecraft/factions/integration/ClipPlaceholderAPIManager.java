package com.massivecraft.factions.integration;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.data.MemoryBoard;
import com.massivecraft.factions.landraidcontrol.DTRControl;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.tag.FactionTag;
import com.massivecraft.factions.tag.Tag;
import com.massivecraft.factions.util.TL;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ClipPlaceholderAPIManager extends PlaceholderExpansion implements Relational {
    private static final String mapChars = "0123456789abcdef";

    // Identifier for this expansion
    @Override
    public String getIdentifier() {
        return "factionsuuid";
    }

    @Override
    public String getAuthor() {
        return "drtshock";
    }

    // Return the plugin version since this expansion is bundled with the dependency
    @Override
    public String getVersion() {
        return FactionsPlugin.getInstance().getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    // Relational placeholders
    @Override
    public String onPlaceholderRequest(Player p1, Player p2, String placeholder) {
        if (p1 == null || p2 == null || placeholder == null) {
            return "";
        }

        FPlayer fp1 = FPlayers.getInstance().getByPlayer(p1);
        FPlayer fp2 = FPlayers.getInstance().getByPlayer(p2);
        if (fp1 == null || fp2 == null) {
            return "";
        }

        return switch (placeholder) {
            case "relation" -> {
                String relationName = fp1.getRelationTo(fp2).nicename;
                yield relationName != null ? relationName : "";
            }
            case "relation_color" -> fp1.getColorStringTo(fp2);
            default -> null;
        };

    }

    @Override
    public String onPlaceholderRequest(Player player, String placeholder) {
        if (player == null || placeholder == null) {
            return "";
        }

        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        Faction faction = fPlayer.getFaction();
        boolean territory = false;
        if (placeholder.contains("faction_territory")) {
            faction = Board.getInstance().getFactionAt(fPlayer.getLastStoodAt());
            placeholder = placeholder.replace("_territory", "");
            territory = true;
        }

        if (placeholder.startsWith("player_map_")) {
            List<Component> list = ((MemoryBoard) Board.getInstance()).getScoreboardMap(fPlayer);
            if (list.isEmpty()) {
                return "";
            }
            int row;
            try {
                row = Integer.parseInt(placeholder.substring("player_map_".length()));
            } catch (NumberFormatException ignored) {
                return "";
            }
            if (row < 1 || row > list.size()) {
                return "";
            }
            row--;
            return ChatColor.COLOR_CHAR + mapChars.substring(row, row + 1) + LegacyComponentSerializer.legacySection().serialize(list.get(row));
        }

        return switch (placeholder) {
            // First list player stuff
            case "player_name" -> fPlayer.getName();
            case "player_lastseen" -> {
                String humanized = DurationFormatUtils.formatDurationWords(System.currentTimeMillis() - fPlayer.getLastLoginTime(), true, true) + TL.COMMAND_STATUS_AGOSUFFIX;
                yield fPlayer.isOnline() ? ChatColor.GREEN + TL.COMMAND_STATUS_ONLINE.toString() : (System.currentTimeMillis() - fPlayer.getLastLoginTime() < 432000000 ? ChatColor.YELLOW + humanized : ChatColor.RED + humanized);
            }
            case "player_group" ->
                    FactionsPlugin.getInstance().getPrimaryGroup(Bukkit.getOfflinePlayer(UUID.fromString(fPlayer.getId())));
            case "player_balance" -> Econ.isSetup() ? Econ.getFriendlyBalance(fPlayer) : TL.ECON_OFF.format("balance");
            case "player_power" -> String.valueOf(fPlayer.getPowerRounded());
            case "player_maxpower" -> String.valueOf(fPlayer.getPowerMaxRounded());
            case "player_kills" -> String.valueOf(fPlayer.getKills());
            case "player_deaths" -> String.valueOf(fPlayer.getDeaths());
            case "player_role" -> fPlayer.hasFaction() ? fPlayer.getRole().getPrefix() : "";
            case "player_role_name" ->
                    fPlayer.hasFaction() ? fPlayer.getRole().getTranslation() : TL.PLACEHOLDER_ROLE_NAME.toString();
            case "player_factionless" -> fPlayer.hasFaction() ? "" : TL.GENERIC_FACTIONLESS.toString();
            // Then Faction stuff
            case "faction_name" ->
                    (fPlayer.hasFaction() || territory) ? faction.getTag() : TL.NOFACTION_PREFIX.toString();
            case "faction_name_custom" ->
                    (fPlayer.hasFaction() || territory) ? Tag.parsePlain(fPlayer, TL.PLACEHOLDER_CUSTOM_FACTION.toString()) : "";
            case "faction_only_space" -> (fPlayer.hasFaction() || territory) ? " " : "";
            case "faction_internal_id" -> faction.getIntId() + "";
            case "faction_power" -> String.valueOf(faction.getPowerRounded());
            case "faction_powermax" -> String.valueOf(faction.getPowerMaxRounded());
            case "faction_dtr" -> (fPlayer.hasFaction() || territory) ? DTRControl.round(faction.getDTR()) : "";
            case "faction_dtrmax" -> {
                if ((fPlayer.hasFaction() || territory) && FactionsPlugin.getInstance().getLandRaidControl() instanceof DTRControl) {
                    yield DTRControl.round(((DTRControl) FactionsPlugin.getInstance().getLandRaidControl()).getMaxDTR(faction));
                }
                yield "";
            }
            case "faction_dtr_frozen" -> {
                if ((fPlayer.hasFaction() || territory) && FactionsPlugin.getInstance().getLandRaidControl() instanceof DTRControl) {
                    yield FactionTag.DTR_FROZEN.replace(FactionTag.DTR_FROZEN.getTag(), faction);
                }
                yield "";
            }
            case "faction_dtr_frozen_time" -> {
                if ((fPlayer.hasFaction() || territory) && FactionsPlugin.getInstance().getLandRaidControl() instanceof DTRControl) {
                    yield FactionTag.DTR_FROZEN_TIME.replace(FactionTag.DTR_FROZEN_TIME.getTag(), faction);
                }
                yield "";
            }
            case "faction_maxclaims" ->
                    (fPlayer.hasFaction() || territory) ? String.valueOf(FactionsPlugin.getInstance().getLandRaidControl().getLandLimit(faction)) : "";
            case "faction_description" -> faction.getDescription();
            case "faction_claims" -> String.valueOf(faction.getAllClaims().size());
            case "faction_founded" -> TL.sdf.format(faction.getFoundedDate());
            case "faction_joining" ->
                    (faction.getOpen() ? TL.COMMAND_SHOW_UNINVITED.toString() : TL.COMMAND_SHOW_INVITATION.toString());
            case "faction_peaceful" ->
                    faction.isPeaceful() ? FactionsPlugin.getInstance().conf().colors().relations().getNeutral() + TL.COMMAND_SHOW_PEACEFUL.toString() : "";
            case "faction_powerboost" -> {
                double powerBoost = faction.getPowerBoost();
                yield (powerBoost == 0.0) ? "" : (powerBoost > 0.0 ? TL.COMMAND_SHOW_BONUS.toString() : TL.COMMAND_SHOW_PENALTY.toString()) + powerBoost + ")";
            }
            case "faction_leader" -> {
                FPlayer fAdmin = faction.getFPlayerAdmin();
                yield fAdmin == null ? "Server" : fAdmin.getName().substring(0, fAdmin.getName().length() > 14 ? 13 : fAdmin.getName().length());
            }
            case "faction_warps" -> String.valueOf(faction.getWarps().size());
            case "faction_raidable" -> {
                boolean raid = FactionsPlugin.getInstance().getLandRaidControl().isRaidable(faction);
                yield raid ? TL.RAIDABLE_TRUE.toString() : TL.RAIDABLE_FALSE.toString();
            }
            case "faction_home_world" -> faction.hasHome() ? faction.getHome().getWorld().getName() : "";
            case "faction_home_x" -> faction.hasHome() ? String.valueOf(faction.getHome().getBlockX()) : "";
            case "faction_home_y" -> faction.hasHome() ? String.valueOf(faction.getHome().getBlockY()) : "";
            case "faction_home_z" -> faction.hasHome() ? String.valueOf(faction.getHome().getBlockZ()) : "";
            case "faction_land_value" ->
                    Econ.shouldBeUsed() ? Econ.moneyString(Econ.calculateTotalLandValue(faction.getLandRounded())) : TL.ECON_OFF.format("value");
            case "faction_land_refund" ->
                    Econ.shouldBeUsed() ? Econ.moneyString(Econ.calculateTotalLandRefund(faction.getLandRounded())) : TL.ECON_OFF.format("refund");
            case "faction_bank_balance" ->
                    Econ.shouldBeUsed() ? Econ.moneyString(Econ.getBalance(faction)) : TL.ECON_OFF.format("balance");
            case "faction_tnt_balance" -> FactionTag.TNT_BALANCE.replace(FactionTag.TNT_BALANCE.getTag(), faction);
            case "faction_tnt_max_balance" -> FactionTag.TNT_MAX.replace(FactionTag.TNT_MAX.getTag(), faction);
            case "faction_allies" -> String.valueOf(faction.getRelationCount(Relation.ALLY));
            case "faction_allies_players" -> String.valueOf(this.countOn(faction, Relation.ALLY, null, fPlayer));
            case "faction_allies_players_online" -> String.valueOf(this.countOn(faction, Relation.ALLY, true, fPlayer));
            case "faction_allies_players_offline" ->
                    String.valueOf(this.countOn(faction, Relation.ALLY, false, fPlayer));
            case "faction_enemies" -> String.valueOf(faction.getRelationCount(Relation.ENEMY));
            case "faction_enemies_players" -> String.valueOf(this.countOn(faction, Relation.ENEMY, null, fPlayer));
            case "faction_enemies_players_online" ->
                    String.valueOf(this.countOn(faction, Relation.ENEMY, true, fPlayer));
            case "faction_enemies_players_offline" ->
                    String.valueOf(this.countOn(faction, Relation.ENEMY, false, fPlayer));
            case "faction_truces" -> String.valueOf(faction.getRelationCount(Relation.TRUCE));
            case "faction_truces_players" -> String.valueOf(this.countOn(faction, Relation.TRUCE, null, fPlayer));
            case "faction_truces_players_online" ->
                    String.valueOf(this.countOn(faction, Relation.TRUCE, true, fPlayer));
            case "faction_truces_players_offline" ->
                    String.valueOf(this.countOn(faction, Relation.TRUCE, false, fPlayer));
            case "faction_online" -> String.valueOf(faction.getOnlinePlayers().size());
            case "faction_offline" -> String.valueOf(faction.getFPlayers().size() - faction.getOnlinePlayers().size());
            case "faction_size" -> String.valueOf(faction.getFPlayers().size());
            case "faction_kills" -> String.valueOf(faction.getKills());
            case "faction_deaths" -> String.valueOf(faction.getDeaths());
            case "faction_maxvaults" -> String.valueOf(faction.getMaxVaults());
            case "faction_relation_color" -> fPlayer.getColorStringTo(faction);
            default -> null;
        };

    }

    private int countOn(Faction f, Relation relation, Boolean status, FPlayer player) {
        int count = 0;
        for (Faction faction : Factions.getInstance().getAllFactions()) {
            if (faction.getRelationTo(f) == relation) {
                if (status == null) {
                    count += faction.getFPlayers().size();
                } else if (status) {
                    count += faction.getFPlayersWhereOnline(true, player).size();
                } else {
                    count += faction.getFPlayersWhereOnline(false, player).size();
                }
            }
        }
        return count;
    }
}
