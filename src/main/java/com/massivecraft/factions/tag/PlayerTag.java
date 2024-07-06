package com.massivecraft.factions.tag;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.util.TL;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.function.Function;

public enum PlayerTag implements Tag {
    GROUP("group", (fp) -> {
        if (fp.isOnline()) {
            return FactionsPlugin.getInstance().getPrimaryGroup(fp.getPlayer());
        } else {
            return "";
        }
    }),
    LAST_SEEN("lastSeen", (fp) -> {
        if (fp.isOnline() && !fp.isVanished()) {
            return ChatColor.GREEN + TL.COMMAND_STATUS_ONLINE.toString();
        }
        long duration = Math.max(System.currentTimeMillis() - fp.getLastLoginTime(), FactionsPlugin.getInstance().conf().factions().other().getMinimumLastSeenTime() * 1000L);
        String humanized = DurationFormatUtils.formatDurationWords(duration, true, true) + TL.COMMAND_STATUS_AGOSUFFIX;
        return duration < 432000000 ? ChatColor.YELLOW + humanized : ChatColor.RED + humanized;
    }),
    PLAYER_BALANCE("balance", (fp) -> Econ.isSetup() ? Econ.getFriendlyBalance(fp) : (Tag.isMinimalShow() ? null : TL.ECON_OFF.format("balance"))),
    PLAYER_POWER("player-power", (fp) -> String.valueOf(fp.getPowerRounded())),
    PLAYER_MAXPOWER("player-maxpower", (fp) -> String.valueOf(fp.getPowerMaxRounded())),
    PLAYER_KILLS("player-kills", (fp) -> String.valueOf(fp.getKills())),
    PLAYER_DEATHS("player-deaths", (fp) -> String.valueOf(fp.getDeaths())),
    PLAYER_DISPLAYNAME("player-displayname", (fp) -> {
        if (fp.isOnline()) {
            return fp.getPlayer().getDisplayName();
        } else {
            return fp.getName();
        }
    }),
    PLAYER_NAME("name", FPlayer::getName),
    PLAYER_ROLE("player-role-prefix", (fp) -> fp.hasFaction() ? fp.getRole().getPrefix() : ""),
    TOTAL_ONLINE_VISIBLE("total-online-visible", (fp) -> {
        if (fp == null) {
            return String.valueOf(Bukkit.getOnlinePlayers().size());
        }
        int count = 0;
        Player me = fp.getPlayer();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (me.canSee(player)) {
                count++;
            }
        }
        return String.valueOf(count);
    }),
    ;

    private final String tag;
    private final Function<FPlayer, String> function;

    public static String parse(String text, FPlayer player) {
        for (PlayerTag tag : PlayerTag.values()) {
            text = tag.replace(text, player);
        }
        return text;
    }

    PlayerTag(String tag, Function<FPlayer, String> function) {
        this.tag = '{' + tag + '}';
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

    public String replace(String text, FPlayer player) {
        if (!this.foundInString(text)) {
            return text;
        }
        String result = this.function.apply(player);
        return result == null ? null : text.replace(this.tag, result);
    }
}
