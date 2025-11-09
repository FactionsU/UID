package dev.kitteh.factions.tag;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.TL;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Function;

@ApiStatus.Internal
@ApiStatus.Obsolete
@Deprecated(forRemoval = true)
public enum PlayerTag implements Tag {
    GROUP("group", (fp) -> {
        if (fp.isOnline()) {
            return AbstractFactionsPlugin.instance().getPrimaryGroup(fp.asPlayer());
        } else {
            return "";
        }
    }),
    LAST_SEEN("lastSeen", (fp) -> {
        if (fp.isOnline() && !fp.isVanished()) {
            return ChatColor.GREEN + TL.COMMAND_STATUS_ONLINE.toString();
        }
        long duration = Math.max(System.currentTimeMillis() - fp.lastLogin(), FactionsPlugin.instance().conf().factions().other().getMinimumLastSeenTime() * 1000L);
        String humanized = DurationFormatUtils.formatDurationWords(duration, true, true) + TL.COMMAND_STATUS_AGOSUFFIX;
        return duration < 432000000 ? ChatColor.YELLOW + humanized : ChatColor.RED + humanized;
    }),
    PLAYER_BALANCE("balance", (fp) -> Econ.isSetup() ? Econ.getFriendlyBalance(fp) : TL.ECON_OFF.format("balance")),
    PLAYER_POWER("player-power", (fp) -> String.valueOf(fp.powerRounded())),
    PLAYER_MAXPOWER("player-maxpower", (fp) -> String.valueOf(fp.powerMaxRounded())),
    PLAYER_KILLS("player-kills", (fp) -> String.valueOf(fp.kills())),
    PLAYER_DEATHS("player-deaths", (fp) -> String.valueOf(fp.deaths())),
    PLAYER_DISPLAYNAME("player-displayname", (fp) -> {
        if (fp.asPlayer() instanceof Player player) {
            return player.getDisplayName();
        } else {
            return fp.name();
        }
    }),
    PLAYER_NAME("name", FPlayer::name),
    PLAYER_ROLE("player-role-prefix", (fp) -> fp.hasFaction() ? fp.role().getPrefix() : ""),
    TOTAL_ONLINE_VISIBLE("total-online-visible", (fp) -> {
        if (fp == null || !(fp.asPlayer() instanceof Player me)) {
            return String.valueOf(Bukkit.getOnlinePlayers().size());
        }
        int count = 0;
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
