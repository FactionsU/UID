package dev.kitteh.factions.integration;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.data.MemoryBoard;
import dev.kitteh.factions.landraidcontrol.DTRControl;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.tag.FactionTag;
import dev.kitteh.factions.tag.Tag;
import dev.kitteh.factions.util.TL;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public class PapiExpansion extends PlaceholderExpansion implements Relational {
    private static final String mapChars = "0123456789abcdef";

    // Identifier for this expansion
    @Override
    public String getIdentifier() {
        return "factionsuuid";
    }

    @Override
    public String getAuthor() {
        return "mbaxter";
    }

    // Return the plugin version since this expansion is bundled with the dependency
    @Override
    public String getVersion() {
        return AbstractFactionsPlugin.instance().getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    // Relational placeholders
    @Override
    public @Nullable String onPlaceholderRequest(Player p1, Player p2, String placeholder) {
        FPlayer fp1 = FPlayers.fPlayers().get(p1);
        FPlayer fp2 = FPlayers.fPlayers().get(p2);

        return switch (placeholder) {
            case "relation" -> fp1.relationTo(fp2).nicename;
            case "relation_color" -> fp1.colorLegacyStringTo(fp2);
            default -> null;
        };
    }

    @Override
    public @Nullable String onPlaceholderRequest(@Nullable Player player, String placeholder) {
        return this.onRequest(player, placeholder);
    }

    @Override
    public @Nullable String onRequest(@Nullable OfflinePlayer player, String placeholder) {
        if (player == null) {
            return "";
        }

        FPlayer fPlayer = FPlayers.fPlayers().get(player);
        Faction faction = fPlayer.faction();
        boolean territory = false;
        if (placeholder.contains("faction_territory")) {
            faction = Board.board().factionAt(fPlayer.lastStoodAt());
            placeholder = placeholder.replace("_territory", "");
            territory = true;
        }

        if (placeholder.startsWith("player_map_")) {
            List<Component> list = ((MemoryBoard) Board.board()).getScoreboardMap(fPlayer);
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
            case "player_name" -> fPlayer.name();
            case "player_lastseen" -> {
                String humanized = DurationFormatUtils.formatDurationWords(System.currentTimeMillis() - fPlayer.lastLogin(), true, true) + TL.COMMAND_STATUS_AGOSUFFIX;
                yield fPlayer.isOnline() ? ChatColor.GREEN + TL.COMMAND_STATUS_ONLINE.toString() : (System.currentTimeMillis() - fPlayer.lastLogin() < 432000000 ? ChatColor.YELLOW + humanized : ChatColor.RED + humanized);
            }
            case "player_group" -> AbstractFactionsPlugin.instance().getPrimaryGroup(Bukkit.getOfflinePlayer(fPlayer.uniqueId()));
            case "player_balance" -> Econ.isSetup() ? Econ.getFriendlyBalance(fPlayer) : TL.ECON_OFF.format("balance");
            case "player_power" -> String.valueOf(fPlayer.powerRounded());
            case "player_maxpower" -> String.valueOf(fPlayer.powerMaxRounded());
            case "player_kills" -> String.valueOf(fPlayer.kills());
            case "player_deaths" -> String.valueOf(fPlayer.deaths());
            case "player_role" -> fPlayer.hasFaction() ? fPlayer.role().getPrefix() : "";
            case "player_role_name" -> fPlayer.hasFaction() ? fPlayer.role().translation() : TL.PLACEHOLDER_ROLE_NAME.toString();
            case "player_factionless" -> fPlayer.hasFaction() ? "" : TL.GENERIC_FACTIONLESS.toString();
            // Then Faction stuff
            case "faction_name" -> (fPlayer.hasFaction() || territory) ? faction.tag() : TL.NOFACTION_PREFIX.toString();
            case "faction_name_custom" -> (fPlayer.hasFaction() || territory) ? Tag.parsePlain(fPlayer, TL.PLACEHOLDER_CUSTOM_FACTION.toString()) : "";
            case "faction_only_space" -> (fPlayer.hasFaction() || territory) ? " " : "";
            case "faction_internal_id" -> faction.id() + "";
            case "faction_power" -> String.valueOf(faction.power());
            case "faction_powermax" -> String.valueOf(faction.powerMax());
            case "faction_dtr" -> (fPlayer.hasFaction() || territory) ? DTRControl.round(faction.dtr()) : "";
            case "faction_dtrmax" -> {
                if ((fPlayer.hasFaction() || territory) && FactionsPlugin.instance().landRaidControl() instanceof DTRControl) {
                    yield DTRControl.round(((DTRControl) FactionsPlugin.instance().landRaidControl()).getMaxDTR(faction));
                }
                yield "";
            }
            case "faction_dtr_frozen" -> {
                if ((fPlayer.hasFaction() || territory) && FactionsPlugin.instance().landRaidControl() instanceof DTRControl) {
                    yield FactionTag.DTR_FROZEN.replace(FactionTag.DTR_FROZEN.getTag(), faction);
                }
                yield "";
            }
            case "faction_dtr_frozen_time" -> {
                if ((fPlayer.hasFaction() || territory) && FactionsPlugin.instance().landRaidControl() instanceof DTRControl) {
                    yield FactionTag.DTR_FROZEN_TIME.replace(FactionTag.DTR_FROZEN_TIME.getTag(), faction);
                }
                yield "";
            }
            case "faction_maxclaims" -> (fPlayer.hasFaction() || territory) ? String.valueOf(FactionsPlugin.instance().landRaidControl().landLimit(faction)) : "";
            case "faction_description" -> faction.description();
            case "faction_claims" -> String.valueOf(faction.claims().size());
            case "faction_founded" -> TL.sdf.format(faction.founded());
            case "faction_joining" -> (faction.open() ? TL.COMMAND_SHOW_UNINVITED.toString() : TL.COMMAND_SHOW_INVITATION.toString());
            case "faction_peaceful" -> faction.isPeaceful() ? FactionsPlugin.instance().conf().colors().relations().getNeutral() + TL.COMMAND_SHOW_PEACEFUL.toString() : "";
            case "faction_powerboost" -> {
                double powerBoost = faction.powerBoost();
                yield (powerBoost == 0.0) ? "" : (powerBoost > 0.0 ? TL.COMMAND_SHOW_BONUS.toString() : TL.COMMAND_SHOW_PENALTY.toString()) + powerBoost + ")";
            }
            case "faction_leader" -> {
                FPlayer fAdmin = faction.admin();
                yield fAdmin == null ? "Server" : fAdmin.name().substring(0, fAdmin.name().length() > 14 ? 13 : fAdmin.name().length());
            }
            case "faction_warps" -> String.valueOf(faction.warps().size());
            case "faction_raidable" -> {
                boolean raid = FactionsPlugin.instance().landRaidControl().isRaidable(faction);
                yield raid ? TL.RAIDABLE_TRUE.toString() : TL.RAIDABLE_FALSE.toString();
            }
            case "faction_home_world" -> faction.home() instanceof Location home ? home.getWorld().getName() : "";
            case "faction_home_x" -> faction.home() instanceof Location home ? String.valueOf(home.getBlockX()) : "";
            case "faction_home_y" -> faction.home() instanceof Location home ? String.valueOf(home.getBlockY()) : "";
            case "faction_home_z" -> faction.home() instanceof Location home ? String.valueOf(home.getBlockZ()) : "";
            case "faction_land_value" -> Econ.shouldBeUsed() ? Econ.moneyString(Econ.calculateTotalLandValue(faction.claimCount())) : TL.ECON_OFF.format("value");
            case "faction_land_refund" -> Econ.shouldBeUsed() ? Econ.moneyString(Econ.calculateTotalLandRefund(faction.claimCount())) : TL.ECON_OFF.format("refund");
            case "faction_bank_balance" -> Econ.shouldBeUsed() ? Econ.moneyString(Econ.getBalance(faction)) : TL.ECON_OFF.format("balance");
            case "faction_tnt_balance" -> FactionTag.TNT_BALANCE.replace(FactionTag.TNT_BALANCE.getTag(), faction);
            case "faction_tnt_max_balance" -> FactionTag.TNT_MAX.replace(FactionTag.TNT_MAX.getTag(), faction);
            case "faction_allies" -> String.valueOf(faction.relationCount(Relation.ALLY));
            case "faction_allies_players" -> String.valueOf(this.countOn(faction, Relation.ALLY, null, fPlayer));
            case "faction_allies_players_online" -> String.valueOf(this.countOn(faction, Relation.ALLY, true, fPlayer));
            case "faction_allies_players_offline" -> String.valueOf(this.countOn(faction, Relation.ALLY, false, fPlayer));
            case "faction_enemies" -> String.valueOf(faction.relationCount(Relation.ENEMY));
            case "faction_enemies_players" -> String.valueOf(this.countOn(faction, Relation.ENEMY, null, fPlayer));
            case "faction_enemies_players_online" -> String.valueOf(this.countOn(faction, Relation.ENEMY, true, fPlayer));
            case "faction_enemies_players_offline" -> String.valueOf(this.countOn(faction, Relation.ENEMY, false, fPlayer));
            case "faction_truces" -> String.valueOf(faction.relationCount(Relation.TRUCE));
            case "faction_truces_players" -> String.valueOf(this.countOn(faction, Relation.TRUCE, null, fPlayer));
            case "faction_truces_players_online" -> String.valueOf(this.countOn(faction, Relation.TRUCE, true, fPlayer));
            case "faction_truces_players_offline" -> String.valueOf(this.countOn(faction, Relation.TRUCE, false, fPlayer));
            case "faction_online" -> String.valueOf(faction.membersOnlineAsPlayers().size());
            case "faction_offline" -> String.valueOf(faction.members().size() - faction.membersOnlineAsPlayers().size());
            case "faction_size" -> String.valueOf(faction.members().size());
            case "faction_kills" -> String.valueOf(faction.kills());
            case "faction_deaths" -> String.valueOf(faction.deaths());
            case "faction_maxvaults" -> String.valueOf(faction.maxVaults());
            case "faction_relation_color" -> fPlayer.colorLegacyStringTo(faction);
            default -> null;
        };
    }

    private int countOn(Faction f, Relation relation, @Nullable Boolean status, FPlayer player) {
        int count = 0;
        for (Faction faction : Factions.factions().all()) {
            if (faction.relationTo(f) == relation) {
                if (status == null) {
                    count += faction.members().size();
                } else if (status) {
                    count += faction.membersOnline(true, player).size();
                } else {
                    count += faction.membersOnline(false, player).size();
                }
            }
        }
        return count;
    }
}
