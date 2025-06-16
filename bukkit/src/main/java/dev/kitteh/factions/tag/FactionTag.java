package dev.kitteh.factions.tag;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.landraidcontrol.DTRControl;
import dev.kitteh.factions.landraidcontrol.PowerControl;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.TextUtil;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Location;

import java.util.function.BiFunction;
import java.util.function.Function;

public enum FactionTag implements Tag {
    INTERNAL_ID("faction-internal-id", (fac) -> String.valueOf(fac.id())),
    HOME_X("x", (fac) -> fac.home() instanceof Location loc ? String.valueOf(loc.getBlockX()) : Tag.isMinimalShow() ? null : "{ig}"),
    HOME_Y("y", (fac) -> fac.home() instanceof Location loc ? String.valueOf(loc.getBlockY()) : Tag.isMinimalShow() ? null : "{ig}"),
    HOME_Z("z", (fac) -> fac.home() instanceof Location loc ? String.valueOf(loc.getBlockZ()) : Tag.isMinimalShow() ? null : "{ig}"),
    CHUNKS("chunks", (fac) -> String.valueOf(fac.claimCount())),
    WARPS("warps", (fac) -> String.valueOf(fac.warps().size())),
    HEADER("header", (fac, fp) -> TextUtil.titleize(fac.tagLegacy(fp))),
    POWER("power", (fac) -> String.valueOf(fac.power())),
    MAX_POWER("maxPower", (fac) -> String.valueOf(fac.powerMax())),
    POWER_BOOST("power-boost", (fac) -> {
        double powerBoost = fac.powerBoost();
        return (powerBoost == 0.0) ? "" : (powerBoost > 0.0 ? TL.COMMAND_SHOW_BONUS.toString() : TL.COMMAND_SHOW_PENALTY.toString() + powerBoost + ")");
    }),
    LEADER("leader", (fac) -> {
        FPlayer fAdmin = fac.admin();
        return fAdmin == null ? TL.TAG_LEADER_OWNERLESS.toString() : fAdmin.name().substring(0, fAdmin.name().length() > 14 ? 13 : fAdmin.name().length());
    }),
    JOINING("joining", (fac) -> (fac.open() ? TL.COMMAND_SHOW_UNINVITED.toString() : TL.COMMAND_SHOW_INVITATION.toString())),
    FACTION("faction", (fac) -> fac.tag()),
    FACTION_RELATION_COLOR("faction-relation-color", (fac, fp) -> fp == null ? "" : fp.colorLegacyStringTo(fac)),
    HOME_WORLD("world", (fac) -> fac.home() instanceof Location loc ? loc.getWorld().getName() : Tag.isMinimalShow() ? null : "{ig}"),
    RAIDABLE("raidable", (fac) -> {
        boolean raid = FactionsPlugin.instance().landRaidControl().isRaidable(fac);
        return raid ? TL.RAIDABLE_TRUE.toString() : TL.RAIDABLE_FALSE.toString();
    }),
    DTR("dtr", (fac) -> {
        if (FactionsPlugin.instance().landRaidControl() instanceof PowerControl) {
            int dtr = fac.claimCount() >= fac.power() ? 0 : (int) Math.ceil(((double) (fac.power() - fac.claimCount())) / FactionsPlugin.instance().conf().factions().landRaidControl().power().getLossPerDeath());
            return TL.COMMAND_SHOW_DEATHS_TIL_RAIDABLE.format(dtr);
        } else {
            return DTRControl.round(fac.dtr());
        }
    }),
    MAX_DTR("max-dtr", (fac) -> {
        if (FactionsPlugin.instance().landRaidControl() instanceof DTRControl dtrControl) {
            return DTRControl.round(dtrControl.getMaxDTR(fac));
        }
        return Tag.isMinimalShow() ? null : "{ig}";
    }),
    DTR_FROZEN("dtr-frozen-status", (fac -> TL.DTR_FROZEN_STATUS_MESSAGE.format(fac.dtrFrozen() ? TL.DTR_FROZEN_STATUS_TRUE.toString() : TL.DTR_FROZEN_STATUS_FALSE.toString()))),
    DTR_FROZEN_TIME("dtr-frozen-time", (fac -> TL.DTR_FROZEN_TIME_MESSAGE.format(fac.dtrFrozen() ?
            DurationFormatUtils.formatDuration(fac.dtrFrozenUntil() - System.currentTimeMillis(), FactionsPlugin.instance().conf().factions().landRaidControl().dtr().getFreezeTimeFormat()) :
            TL.DTR_FROZEN_TIME_NOTFROZEN.toString()))),
    MAX_CHUNKS("max-chunks", (fac -> String.valueOf(FactionsPlugin.instance().landRaidControl().landLimit(fac)))),
    PEACEFUL("peaceful", (fac) -> fac.peaceful() ? FactionsPlugin.instance().conf().colors().relations().getPeaceful() + TL.COMMAND_SHOW_PEACEFUL.toString() : ""),
    PERMANENT("permanent", (fac) -> fac.permanent() ? "permanent" : "{notPermanent}"), // no braces needed
    LAND_VALUE("land-value", (fac) -> Econ.shouldBeUsed() ? Econ.moneyString(Econ.calculateTotalLandValue(fac.claimCount())) : Tag.isMinimalShow() ? null : TL.ECON_OFF.format("value")),
    DESCRIPTION("description", fac -> fac.description()),
    CREATE_DATE("create-date", (fac) -> TL.sdf.format(fac.founded().toEpochMilli())),
    LAND_REFUND("land-refund", (fac) -> Econ.shouldBeUsed() ? Econ.moneyString(Econ.calculateTotalLandRefund(fac.claimCount())) : Tag.isMinimalShow() ? null : TL.ECON_OFF.format("refund")),
    BANK_BALANCE("faction-balance", (fac) -> {
        if (Econ.shouldBeUsed()) {
            return FactionsPlugin.instance().conf().economy().isBankEnabled() ? Econ.moneyString(Econ.getBalance(fac)) : Tag.isMinimalShow() ? null : TL.ECON_OFF.format("balance");
        }
        return Tag.isMinimalShow() ? null : TL.ECON_OFF.format("balance");
    }),
    TNT_BALANCE("tnt-balance", (fac) -> {
        if (FactionsPlugin.instance().conf().commands().tnt().isEnable()) {
            return String.valueOf(fac.tntBank());
        }
        return Tag.isMinimalShow() ? null : "";
    }),
    TNT_MAX("tnt-max-balance", (fac) -> {
        if (FactionsPlugin.instance().conf().commands().tnt().isEnable()) {
            return String.valueOf(fac.tntBankMax());
        }
        return Tag.isMinimalShow() ? null : "";
    }),
    ALLIES_COUNT("allies", (fac) -> String.valueOf(fac.relationCount(Relation.ALLY))),
    ENEMIES_COUNT("enemies", (fac) -> String.valueOf(fac.relationCount(Relation.ENEMY))),
    TRUCES_COUNT("truces", (fac) -> String.valueOf(fac.relationCount(Relation.TRUCE))),
    ONLINE_COUNT("online", (fac, fp) -> {
        if (fp != null && fp.isOnline()) {
            return String.valueOf(fac.membersOnline(true, fp).size());
        } else {
            // Only console should ever get here.
            return String.valueOf(fac.membersOnline(true).size());
        }
    }),
    OFFLINE_COUNT("offline", (fac, fp) -> {
        if (fp != null && fp.isOnline()) {
            return String.valueOf(fac.members().size() - fac.membersOnline(true, fp).size());
        } else {
            // Only console should ever get here.
            return String.valueOf(fac.membersOnline(false).size());
        }
    }),
    FACTION_SIZE("members", (fac) -> String.valueOf(fac.members().size())),
    FACTION_KILLS("faction-kills", (fac) -> String.valueOf(fac.kills())),
    FACTION_DEATHS("faction-deaths", (fac) -> String.valueOf(fac.deaths())),
    FACTION_BANCOUNT("faction-bancount", (fac) -> String.valueOf(fac.bans().size())),
    FACTION_LINK("faction-link", (fac) -> fac.link()),
    ;

    private final String tag;
    private final BiFunction<Faction, FPlayer, String> biFunction;
    private final Function<Faction, String> function;

    public static String parse(String text, Faction faction, FPlayer player) {
        for (FactionTag tag : FactionTag.values()) {
            text = tag.replace(text, faction, player);
        }
        return text;
    }

    public static String parse(String text, Faction faction) {
        for (FactionTag tag : FactionTag.values()) {
            text = tag.replace(text, faction);
        }
        return text;
    }

    FactionTag(String tag, BiFunction<Faction, FPlayer, String> function) {
        this(tag, null, function);
    }

    FactionTag(String tag, Function<Faction, String> function) {
        this(tag, function, null);
    }

    FactionTag(String tag, Function<Faction, String> function, BiFunction<Faction, FPlayer, String> biFunction) {
        if (tag.equalsIgnoreCase("permanent")) {
            this.tag = tag;
        } else {
            this.tag = '{' + tag + '}';
        }
        this.biFunction = biFunction;
        this.function = function;
    }

    @Override
    public String getTag() {
        return this.tag;
    }

    @Override
    public boolean foundInString(String test) {
        return test != null && test.contains(this.tag);
    }

    public String replace(String text, Faction faction, FPlayer player) {
        if (!this.foundInString(text)) {
            return text;
        }
        String result = this.function == null ? this.biFunction.apply(faction, player) : this.function.apply(faction);
        return result == null ? null : text.replace(this.tag, result);
    }

    public String replace(String text, Faction faction) {
        return this.replace(text, faction, null);
    }
}
