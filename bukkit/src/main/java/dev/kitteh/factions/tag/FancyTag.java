package dev.kitteh.factions.tag;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.MiscUtil;
import dev.kitteh.factions.util.TriFunction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public enum FancyTag {
    ALLIES_LIST("allies-list", (prefix, faction, observer) -> processRelation(prefix, faction, observer, Relation.ALLY)),
    ENEMIES_LIST("enemies-list", (prefix, faction, observer) -> processRelation(prefix, faction, observer, Relation.ENEMY)),
    TRUCES_LIST("truces-list", (prefix, faction, observer) -> processRelation(prefix, faction, observer, Relation.TRUCE)),
    ONLINE_LIST("online-list", (prefix, faction, observer) -> processPlayers(prefix, faction, observer, true)),
    OFFLINE_LIST("offline-list", (prefix, faction, observer) -> processPlayers(prefix, faction, observer, false));

    private static final int ARBITRARY_LIMIT = 20000;

    private final String tag;
    private final TriFunction<Component, Faction, @Nullable FPlayer, List<Component>> function;

    private static List<Component> processRelation(Component prefix, Faction faction, @Nullable FPlayer fPlayer, Relation relation) {
        List<Component> fancyMessages = new ArrayList<>();
        TextComponent.Builder message = Component.text().append(prefix);
        boolean first = true;
        for (Faction otherFaction : Factions.factions().all()) {
            if (otherFaction == faction) {
                continue;
            }
            if (otherFaction.relationTo(faction) == relation) {
                if (!first) {
                    message.append(Component.text(", "));
                }
                message.append(Mini.parse("<faction:tooltip><faction>", FactionResolver.of(fPlayer, otherFaction)));
                first = false;
                Component current = message.build();
                if (GsonComponentSerializer.gson().serialize(current).length() > ARBITRARY_LIMIT) {
                    fancyMessages.add(current);
                    message = Component.text();
                }
            }
        }
        fancyMessages.add(message.build());
        return fancyMessages;
    }

    private static List<Component> processPlayers(Component prefix, Faction faction, @Nullable FPlayer fPlayer, boolean online) {
        List<Component> fancyMessages = new ArrayList<>();
        TextComponent.Builder message = Component.text().append(prefix);
        boolean first = true;
        for (FPlayer p : MiscUtil.rankOrder(faction.membersOnline(online, fPlayer))) {
            if (!first) {
                message.append(Component.text(", "));
            }
            message.append(Mini.parse("<player:tooltip><player:relation_color><player:name_and_title>", FPlayerResolver.of("player", fPlayer, p)));
            first = false;
            Component current = message.build();
            if (GsonComponentSerializer.gson().serialize(current).length() > ARBITRARY_LIMIT) {
                fancyMessages.add(current);
                message = Component.text();
            }
        }
        fancyMessages.add(message.build());
        return fancyMessages;
    }

    @Nullable
    public static FancyTag getMatch(String text) {
        for (FancyTag tag : FancyTag.values()) {
            if (text.contains(tag.tag)) {
                return tag;
            }
        }
        return null;
    }

    FancyTag(String tag, TriFunction<Component, Faction, @Nullable FPlayer, List<Component>> function) {
        this.tag = '{' + tag + '}';
        this.function = function;
    }

    public String tag() {
        return this.tag;
    }

    public List<Component> getComponents(Component prefix, Faction faction, @Nullable FPlayer observer) {
        return this.function.apply(prefix, faction, observer);
    }
}
