package com.massivecraft.factions.tag;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.util.MiscUtil;
import com.massivecraft.factions.util.QuadFunction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public enum FancyTag implements Tag {
    ALLIES_LIST("allies-list", (target, fme, prefix, gm) -> processRelation(prefix, target, fme, Relation.ALLY)),
    ENEMIES_LIST("enemies-list", (target, fme, prefix, gm) -> processRelation(prefix, target, fme, Relation.ENEMY)),
    TRUCES_LIST("truces-list", (target, fme, prefix, gm) -> processRelation(prefix, target, fme, Relation.TRUCE)),
    ONLINE_LIST("online-list", (target, fme, prefix, gm) -> {
        List<Component> fancyMessages = new ArrayList<>();
        TextComponent.Builder message = Component.text().append(LegacyComponentSerializer.legacySection().deserialize(FactionsPlugin.getInstance().txt().parse(prefix)));
        boolean first = true;
        for (FPlayer p : MiscUtil.rankOrder(target.getFPlayersWhereOnline(true, fme))) {
            if (fme.getPlayer() != null && !fme.getPlayer().canSee(p.getPlayer())) {
                continue; // skip
            }
            if (!first) {
                message.append(Component.text(", "));
            }
            Component tip = tip(tipPlayer(p, gm));
            message.append(LegacyComponentSerializer.legacySection().deserialize(p.getNameAndTitle()).color(fme.getTextColorTo(p)).hoverEvent(HoverEvent.showText(tip)));
            first = false;
            Component current = message.build();
            if (GsonComponentSerializer.gson().serialize(current).length() > ARBITRARY_LIMIT) {
                fancyMessages.add(current);
                message = Component.text();
            }
        }
        fancyMessages.add(message.build());
        return first && Tag.isMinimalShow() ? null : fancyMessages;
    }),
    OFFLINE_LIST("offline-list", (target, fme, prefix, gm) -> {
        List<Component> fancyMessages = new ArrayList<>();
        TextComponent.Builder message = Component.text().append(LegacyComponentSerializer.legacySection().deserialize(FactionsPlugin.getInstance().txt().parse(prefix)));
        boolean first = true;
        for (FPlayer p : MiscUtil.rankOrder(target.getFPlayers())) {
            // Also make sure to add players that are online BUT can't be seen.
            if (!p.isOnline() || (fme.getPlayer() != null && p.isOnline() && !fme.getPlayer().canSee(p.getPlayer()))) {
                if (!first) {
                    message.append(Component.text(", "));
                }
                Component tip = tip(tipPlayer(p, gm));
                message.append(LegacyComponentSerializer.legacySection().deserialize(p.getNameAndTitle()).color(fme.getTextColorTo(p)).hoverEvent(HoverEvent.showText(tip)));
                first = false;
                Component current = message.build();
                if (GsonComponentSerializer.gson().serialize(current).length() > ARBITRARY_LIMIT) {
                    fancyMessages.add(current);
                    message = Component.text();
                }
            }
        }
        fancyMessages.add(message.build());
        return first && Tag.isMinimalShow() ? null : fancyMessages;
    }),
    ;

    private final String tag;
    private final QuadFunction<Faction, FPlayer, String, Map<UUID, String>, List<Component>> function;

    private static List<Component> processRelation(String prefix, Faction faction, FPlayer fPlayer, Relation relation) {
        List<Component> fancyMessages = new ArrayList<>();
        TextComponent.Builder message = Component.text().append(LegacyComponentSerializer.legacySection().deserialize(FactionsPlugin.getInstance().txt().parse(prefix)));
        boolean first = true;
        for (Faction otherFaction : Factions.getInstance().getAllFactions()) {
            if (otherFaction == faction) {
                continue;
            }
            if (otherFaction.getRelationTo(faction) == relation) {
                if (!first) {
                    message.append(Component.text(", "));
                }
                Component tip = tip(tipFaction(otherFaction, fPlayer));
                message.append(LegacyComponentSerializer.legacySection().deserialize(otherFaction.getTag(fPlayer)).color(fPlayer.getTextColorTo(otherFaction)).hoverEvent(HoverEvent.showText(tip)));
                first = false;
                Component current = message.build();
                if (GsonComponentSerializer.gson().serialize(current).length() > ARBITRARY_LIMIT) {
                    fancyMessages.add(current);
                    message = Component.text();
                }
            }
        }
        fancyMessages.add(message.build());
        return first && Tag.isMinimalShow() ? null : fancyMessages;
    }

    private static Component tip(List<String> lines) {
        TextComponent.Builder tip = Component.text();
        boolean lb = false;
        for (String tipLine : lines) {
            if (lb) {
                tip.appendNewline();
            }
            lb = true;
            tip.append(LegacyComponentSerializer.legacySection().deserialize(tipLine));
        }
        return tip.build();
    }

    public static List<Component> parse(String text, Faction faction, FPlayer player, Map<UUID, String> groupMap) {
        for (FancyTag tag : FancyTag.values()) {
            if (tag.foundInString(text)) {
                return tag.getMessage(text, faction, player, groupMap);
            }
        }
        return Collections.emptyList(); // We really shouldn't be here.
    }

    public static boolean anyMatch(String text) {
        return getMatch(text) != null;
    }

    public static FancyTag getMatch(String text) {
        for (FancyTag tag : FancyTag.values()) {
            if (tag.foundInString(text)) {
                return tag;
            }
        }
        return null;
    }

    /**
     * Parses tooltip variables from config <br> Supports variables for factions only (type 2)
     *
     * @param faction faction to tooltip for
     * @return list of tooltips for a fancy message
     */
    private static List<String> tipFaction(Faction faction, FPlayer player) {
        List<String> lines = new ArrayList<>();
        for (String line : FactionsPlugin.getInstance().conf().commands().toolTips().getFaction()) {
            String string = Tag.parsePlain(faction, player, line);
            if (string == null) {
                continue;
            }
            lines.add(ChatColor.translateAlternateColorCodes('&', string));
        }
        return lines;
    }

    /**
     * Parses tooltip variables from config <br> Supports variables for players and factions (types 1 and 2)
     *
     * @param fplayer player to tooltip for
     * @return list of tooltips for a fancy message
     */
    private static List<String> tipPlayer(FPlayer fplayer, Map<UUID, String> groupMap) {
        List<String> lines = new ArrayList<>();
        for (String line : FactionsPlugin.getInstance().conf().commands().toolTips().getPlayer()) {
            String newLine = line;
            everythingOnYourWayOut:
            if (line.contains("{group}")) {
                if (groupMap != null) {
                    String group = groupMap.getOrDefault(UUID.fromString(fplayer.getId()), "");
                    if (!group.trim().isEmpty()) {
                        newLine = newLine.replace("{group}", group);
                        break everythingOnYourWayOut;
                    }
                }
                continue;
            }
            String string = Tag.parsePlain(fplayer, newLine);
            if (string == null) {
                continue;
            }
            lines.add(ChatColor.translateAlternateColorCodes('&', string));
        }
        return lines;
    }

    FancyTag(String tag, QuadFunction<Faction, FPlayer, String, Map<UUID, String>, List<Component>> function) {
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

    public List<Component> getMessage(String text, Faction faction, FPlayer player, Map<UUID, String> groupMap) {
        if (!this.foundInString(text)) {
            return Collections.emptyList(); // We really, really shouldn't be here.
        }
        return this.function.apply(faction, player, text.replace(this.getTag(), ""), groupMap);
    }
}
