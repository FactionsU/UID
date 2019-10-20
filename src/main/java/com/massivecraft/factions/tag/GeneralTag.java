package com.massivecraft.factions.tag;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.util.TL;
import org.bukkit.Bukkit;

import java.util.function.Supplier;

public enum GeneralTag implements Tag {
    MAX_WARPS("max-warps", () -> String.valueOf(FactionsPlugin.getInstance().conf().commands().warp().getMaxWarps())),
    MAX_ALLIES("max-allies", () -> getRelation(Relation.ALLY)),
    MAX_ENEMIES("max-enemies", () -> getRelation(Relation.ENEMY)),
    MAX_TRUCES("max-truces", () -> getRelation(Relation.TRUCE)),
    FACTIONLESS("factionless", () -> String.valueOf(FPlayers.getInstance().getOnlinePlayers().stream().filter(p -> !p.hasFaction()).count())),
    FACTIONLESS_TOTAL("factionless-total", () -> String.valueOf(FPlayers.getInstance().getAllFPlayers().stream().filter(p -> !p.hasFaction()).count())),
    TOTAL_ONLINE("total-online", () -> String.valueOf(Bukkit.getOnlinePlayers().size())),
    ;

    private final String tag;
    private final Supplier<String> supplier;

    private static String getRelation(Relation relation) {
        if (FactionsPlugin.getInstance().conf().factions().maxRelations().isEnabled()) {
            return String.valueOf(relation.getMax());
        }
        return TL.GENERIC_INFINITY.toString();
    }

    public static String parse(String text) {
        for (GeneralTag tag : GeneralTag.values()) {
            text = tag.replace(text);
        }
        return text;
    }

    GeneralTag(String tag, Supplier<String> supplier) {
        this.tag = '{' + tag + '}';
        this.supplier = supplier;
    }

    @Override
    public String getTag() {
        return this.tag;
    }

    @Override
    public boolean foundInString(String test) {
        return test != null && test.contains(this.tag);
    }

    public String replace(String text) {
        if (!this.foundInString(text)) {
            return text;
        }
        String result = this.supplier.get();
        return result == null ? null : text.replace(this.tag, result);
    }
}
