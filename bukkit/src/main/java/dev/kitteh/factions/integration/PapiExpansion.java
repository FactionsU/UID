package dev.kitteh.factions.integration;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.landraidcontrol.DTRControl;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.plugin.Instances;
import dev.kitteh.factions.upgrade.Upgrades;
import dev.kitteh.factions.util.LegacyCruft;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.MiscUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.List;

@ApiStatus.Internal
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
            case "relation" -> fp1.relationTo(fp2).translation();
            case "relation_color" -> LegacyCruft.getLegacyString(fp1.textColorTo(fp2));
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
            List<Component> list = Instances.BOARD.getScoreboardMap(fPlayer);
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
            int keyChar = row % mapChars.length();
            return ChatColor.COLOR_CHAR + mapChars.substring(keyChar, keyChar + 1) + Mini.toLegacy(list.get(row));
        }

        return switch (placeholder) {
            case "grace_status" -> {
                Duration remaining = Universe.universe().graceRemaining();
                if (remaining.isZero()) {
                    yield Confs.tl().commands().grace().getNotSet();
                } else {
                    yield Mini.toLegacy(Mini.parse(Confs.tl().commands().grace().getActive(),
                            Placeholder.unparsed("duration", MiscUtil.durationString(remaining))));
                }
            }

            case "player_name" -> fPlayer.name();
            case "player_name_and_title" -> fPlayer.hasFaction() ? Mini.toLegacy(fPlayer.nameWithTitle()) : fPlayer.name();
            case "player_title" -> fPlayer.hasFaction() ? Mini.toLegacy(fPlayer.title()) : "";
            case "player_lastseen" -> {
                var misc = Confs.tl().placeholders().misc();
                String humanized = DurationFormatUtils.formatDurationWords(System.currentTimeMillis() - fPlayer.lastLogin(), true, true) + misc.getLastSeenSuffix();
                yield fPlayer.isOnline() ? ChatColor.GREEN + misc.getOnline() : (System.currentTimeMillis() - fPlayer.lastLogin() < 432000000 ? ChatColor.YELLOW + humanized : ChatColor.RED + humanized);
            }
            case "player_balance" -> Econ.isSetup() ? Econ.getFriendlyBalance(fPlayer) : Confs.tl().economy().transfer().getOff().replace("<amount>", "balance");
            case "player_power" -> String.valueOf(fPlayer.powerRounded());
            case "player_maxpower" -> String.valueOf(fPlayer.powerMaxRounded());
            case "player_kills" -> String.valueOf(fPlayer.kills());
            case "player_deaths" -> String.valueOf(fPlayer.deaths());
            case "player_role" -> fPlayer.hasFaction() ? fPlayer.role().getPrefix() : "";
            case "player_role_name" -> fPlayer.hasFaction() ? fPlayer.role().translation() : Confs.tl().placeholders().misc().getRoleName();
            case "player_factionless" -> fPlayer.hasFaction() ? "" : Confs.tl().placeholders().misc().getFactionless();

            case "faction_name" -> (fPlayer.hasFaction() || territory) ? faction.tag() : Mini.toLegacy(Mini.parse(Confs.tl().placeholders().misc().getNoFactionPrefix()));
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
                    var misc = Confs.tl().placeholders().misc();
                    yield faction.dtrFrozen() ? misc.getDtrFrozenTrue() : misc.getDtrFrozenFalse();
                }
                yield "";
            }
            case "faction_dtr_frozen_time" -> {
                if ((fPlayer.hasFaction() || territory) && FactionsPlugin.instance().landRaidControl() instanceof DTRControl) {
                    yield faction.dtrFrozen() ?
                            DurationFormatUtils.formatDuration(faction.dtrFrozenUntil() - System.currentTimeMillis(), Confs.main().factions().landRaidControl().dtr().getFreezeTimeFormat()) :
                            Confs.tl().placeholders().misc().getDtrFrozenTimeNotFrozen();
                }
                yield "";
            }
            case "faction_maxclaims" -> (fPlayer.hasFaction() || territory) ? String.valueOf(FactionsPlugin.instance().landRaidControl().landLimit(faction)) : "";
            case "faction_description" -> faction.description();
            case "faction_claims" -> String.valueOf(faction.claims().size());
            case "faction_founded" -> Confs.tl().placeholders().datesAndTimes().formatFactionCreationDate(faction.founded());
            case "faction_joining" -> (faction.open() ? Confs.tl().placeholders().misc().getJoinOpen() : Confs.tl().placeholders().misc().getJoinInvite());
            case "faction_peaceful" -> faction.isPeaceful() ? LegacyCruft.getLegacyString(Confs.main().colors().relations().getPeaceful()) + Confs.tl().placeholders().misc().getPeaceful() : "";
            case "faction_powerboost" -> {
                double powerBoost = faction.powerBoost();
                var misc = Confs.tl().placeholders().misc();
                yield (powerBoost == 0.0) ? "" : (powerBoost > 0.0 ? misc.getPowerBonus() : misc.getPowerPenalty()) + powerBoost + ")";
            }
            case "faction_leader" -> {
                FPlayer fAdmin = faction.admin();
                yield fAdmin == null ? "Server" : fAdmin.name().substring(0, fAdmin.name().length() > 14 ? 13 : fAdmin.name().length());
            }
            case "faction_warps" -> String.valueOf(faction.warps().size());
            case "faction_raidable" -> {
                boolean raid = FactionsPlugin.instance().landRaidControl().isRaidable(faction);
                var misc = Confs.tl().placeholders().misc();
                yield raid ? misc.getRaidableTrue() : misc.getRaidableFalse();
            }
            case "faction_home_world" -> faction.home() instanceof Location home ? (home.getWorld() instanceof World w ? w.getName() : "?????") : "";
            case "faction_home_x" -> faction.home() instanceof Location home ? String.valueOf(home.getBlockX()) : "";
            case "faction_home_y" -> faction.home() instanceof Location home ? String.valueOf(home.getBlockY()) : "";
            case "faction_home_z" -> faction.home() instanceof Location home ? String.valueOf(home.getBlockZ()) : "";
            case "faction_land_value" -> Econ.shouldBeUsed() ? Econ.moneyString(Econ.calculateTotalLandValue(faction.claimCount())) : Mini.toLegacy(Mini.parse(Confs.tl().economy().transfer().getOff(), Placeholder.unparsed("thing", "value")));
            case "faction_land_refund" -> Econ.shouldBeUsed() ? Econ.moneyString(Econ.calculateTotalLandRefund(faction.claimCount())) : Mini.toLegacy(Mini.parse(Confs.tl().economy().transfer().getOff(), Placeholder.unparsed("thing", "refund")));
            case "faction_bank_balance" -> Econ.shouldBeUsed() ? Econ.moneyString(Econ.getBalance(faction)) : Mini.toLegacy(Mini.parse(Confs.tl().economy().transfer().getOff(), Placeholder.unparsed("thing", "balance")));
            case "faction_tnt_balance" -> Universe.universe().isUpgradeEnabled(Upgrades.TNT_BANK) ? String.valueOf(faction.tntBank()) : "";
            case "faction_tnt_max_balance" -> Universe.universe().isUpgradeEnabled(Upgrades.TNT_BANK) ? String.valueOf(faction.tntBankMax()) : "";
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
            case "faction_relation_color" -> LegacyCruft.getLegacyString(fPlayer.textColorTo(faction));
            case "faction_shield_active" -> {
                if (faction.shieldActive()) {
                    yield Mini.toLegacy(Mini.parse(Confs.tl().placeholders().shield().getActiveTrue(), fPlayer,
                            Placeholder.unparsed("remaining", MiscUtil.durationString(faction.shieldRemaining()))));
                } else {
                    yield Mini.toLegacy(Mini.parse(Confs.tl().placeholders().shield().getActiveFalse(), fPlayer));
                }
            }
            case "faction_shield_status" -> {
                if (faction.shieldActive()) {
                    yield Mini.toLegacy(Mini.parse(Confs.tl().placeholders().shield().getStatusTrue(), fPlayer,
                            Placeholder.unparsed("remaining", MiscUtil.durationString(faction.shieldRemaining()))));
                } else {
                    yield Mini.toLegacy(Mini.parse(Confs.tl().placeholders().shield().getStatusFalse(), fPlayer));
                }
            }
            case "faction_shield_remaining" -> MiscUtil.durationString(faction.shieldRemaining());
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
