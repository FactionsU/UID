package dev.kitteh.factions.util;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.config.file.MainConfig;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@NullMarked
public class MiscUtil {
    private static final Map<String, EntityType> entityTypeMap;
    public static final Function<String, EntityType> ENTITY_TYPE_FUNCTION;
    public static final Function<String, Material> MATERIAL_FUNCTION;
    private static final Map<String, CreatureSpawnEvent.SpawnReason> spawnReasonMap;
    public static final Function<String, CreatureSpawnEvent.SpawnReason> SPAWN_REASON_FUNCTION;
    private final String nums = "12%%__USER__%%34";

    static {
        entityTypeMap = new HashMap<>();
        for (EntityType entityType : EntityType.values()) {
            entityTypeMap.put(entityType.name(), entityType);
        }
        ENTITY_TYPE_FUNCTION = (string) -> entityTypeMap.get(string.toUpperCase());

        MATERIAL_FUNCTION = (string) -> MaterialDb.get(string, null);

        spawnReasonMap = new HashMap<>();
        for (CreatureSpawnEvent.SpawnReason reason : CreatureSpawnEvent.SpawnReason.values()) {
            spawnReasonMap.put(reason.name(), reason);
        }
        SPAWN_REASON_FUNCTION = (string) -> spawnReasonMap.get(string.toUpperCase());
    }

    public static <Type> Set<Type> typeSetFromStringSet(Set<@Nullable String> stringSet, Function<String, Type> function) {
        Set<Type> typeSet = new HashSet<>();
        for (String string : stringSet) {
            if (string != null) {
                Type item = function.apply(string);
                if (item != null) {
                    typeSet.add(item);
                }
            }
        }
        return Collections.unmodifiableSet(typeSet);
    }

    public static String getComparisonString(String str) {
        StringBuilder ret = new StringBuilder();

        str = ChatColor.stripColor(str);
        str = str.toLowerCase();

        MainConfig.Factions.Other conf = FactionsPlugin.instance().conf().factions().other();
        for (char c : str.toCharArray()) {
            if (conf.isValidTagCharacter(c)) {
                ret.append(c);
            }
        }
        return ret.toString().toLowerCase();
    }

    public static List<String> validateTag(String str) {
        ArrayList<String> errors = new ArrayList<>();

        for (String blacklistItem : FactionsPlugin.instance().conf().factions().other().getNameBlacklist()) {
            if (str.toLowerCase().contains(blacklistItem.toLowerCase())) {
                errors.add(TextUtil.parse(TL.GENERIC_FACTIONTAG_BLACKLIST.toString()));
                break;
            }
        }

        if (getComparisonString(str).length() < FactionsPlugin.instance().conf().factions().other().getTagLengthMin()) {
            errors.add(TextUtil.parse(TL.GENERIC_FACTIONTAG_TOOSHORT.toString(), FactionsPlugin.instance().conf().factions().other().getTagLengthMin()));
        }

        if (str.length() > FactionsPlugin.instance().conf().factions().other().getTagLengthMax()) {
            errors.add(TextUtil.parse(TL.GENERIC_FACTIONTAG_TOOLONG.toString(), FactionsPlugin.instance().conf().factions().other().getTagLengthMax()));
        }

        MainConfig.Factions.Other conf = FactionsPlugin.instance().conf().factions().other();
        List<String> badChars = null;
        for (char c : str.toCharArray()) {
            if (!conf.isValidTagCharacter(c)) {
                if (badChars == null) {
                    badChars = new ArrayList<>();
                }
                badChars.add(Character.toString(c));
            }
        }
        if (badChars != null) {
            errors.add(TextUtil.parse(TL.GENERIC_FACTIONTAG_ALPHANUMERIC.toString(), String.join("", badChars)));
        }

        return errors;
    }

    public static String durationString(long seconds) {
        return durationString(Duration.ofSeconds(seconds));
    }

    public static String durationString(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        List<String> items = new ArrayList<>();
        if (days > 0) {
            items.add((days == 1 ? TL.DURATION_DAY : TL.DURATION_DAYS).format(days));
        }
        if (hours > 0) {
            items.add((hours == 1 ? TL.DURATION_HOUR : TL.DURATION_HOURS).format(hours));
        }
        if (minutes > 0) {
            items.add((minutes == 1 ? TL.DURATION_MINUTE : TL.DURATION_MINUTES).format(minutes));
        }
        if (seconds > 0) {
            items.add((seconds == 1 ? TL.DURATION_SECOND : TL.DURATION_SECONDS).format(seconds));
        }
        if (items.size() == 1) {
            return items.getFirst();
        } else if (items.size() == 2) {
            String and = TL.DURATION_AND.toString();
            and = and.isBlank() ? " " : " " + and + " ";
            return items.getFirst() + and + items.getLast();
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            builder.append(items.get(i));
            if (i < items.size() - 1) {
                builder.append(", ");
            } else if (i == items.size() - 1) {
                String and = TL.DURATION_AND.toString();
                and = and.isBlank() ? "" : and + " ";
                builder.append(", ").append(and);
            }
        }
        return builder.toString();
    }

    public static Iterable<FPlayer> rankOrder(Iterable<FPlayer> players) {
        List<FPlayer> admins = new ArrayList<>();
        List<FPlayer> coleaders = new ArrayList<>();
        List<FPlayer> moderators = new ArrayList<>();
        List<FPlayer> normal = new ArrayList<>();
        List<FPlayer> recruit = new ArrayList<>();

        for (FPlayer player : players) {
            switch (player.role()) {
                case ADMIN:
                    admins.add(player);
                    break;

                case COLEADER:
                    coleaders.add(player);
                    break;

                case MODERATOR:
                    moderators.add(player);
                    break;

                case NORMAL:
                    normal.add(player);
                    break;

                case RECRUIT:
                    recruit.add(player);
                    break;
            }
        }

        List<FPlayer> ret = new ArrayList<>();
        ret.addAll(admins);
        ret.addAll(coleaders);
        ret.addAll(moderators);
        ret.addAll(normal);
        ret.addAll(recruit);
        return ret;
    }

    public static String commandRoot() {
        return FactionsPlugin.instance().tl().commands().generic().getCommandRoot().getFirstAlias();
    }
}
