package dev.kitteh.factions.util;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.config.file.MainConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@ApiStatus.Internal
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

        //noinspection removal
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

    public static List<Component> validateTag(String str) {
        var tl = FactionsPlugin.instance().tl().general().factionTag();
        var conf = FactionsPlugin.instance().conf().factions().other();
        ArrayList<Component> errors = new ArrayList<>();

        for (String blacklistItem : conf.getNameBlacklist()) {
            if (str.toLowerCase().contains(blacklistItem.toLowerCase())) {
                errors.add(Mini.parse(tl.getBlacklisted()));
                break;
            }
        }

        if (getComparisonString(str).length() < conf.getTagLengthMin()) {
            errors.add(Mini.parse(tl.getTooShort(), Placeholder.unparsed("min", String.valueOf(conf.getTagLengthMin()))));
        }

        if (str.length() > conf.getTagLengthMax()) {
            errors.add(Mini.parse(tl.getTooLong(), Placeholder.unparsed("max", String.valueOf(conf.getTagLengthMax()))));
        }

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
            errors.add(Mini.parse(tl.getAlphanumeric(), Placeholder.unparsed("chars", String.join("", badChars))));
        }

        return errors;
    }

    public static String durationString(long seconds) {
        return durationString(Duration.ofSeconds(seconds));
    }

    @ApiStatus.AvailableSince("4.6.0")
    public static LocalTime floorToHalfHour(LocalTime time) {
        LocalTime floored = time.withSecond(0).withNano(0);
        return floored.withMinute(floored.getMinute() >= 30 ? 30 : 0);
    }

    public static String durationString(Duration duration) {
        var dur = FactionsPlugin.instance().tl().general().duration();
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        List<String> items = new ArrayList<>();
        if (days > 0) {
            items.add(String.format(days == 1 ? dur.getDay() : dur.getDays(), days));
        }
        if (hours > 0) {
            items.add(String.format(hours == 1 ? dur.getHour() : dur.getHours(), hours));
        }
        if (minutes > 0) {
            items.add(String.format(minutes == 1 ? dur.getMinute() : dur.getMinutes(), minutes));
        }
        if (seconds > 0) {
            items.add(String.format(seconds == 1 ? dur.getSecond() : dur.getSeconds(), seconds));
        }
        if (items.size() == 1) {
            return items.getFirst();
        } else if (items.size() == 2) {
            String and = dur.getAnd();
            and = and.isBlank() ? " " : " " + and + " ";
            return items.getFirst() + and + items.getLast();
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            builder.append(items.get(i));
            if (i < items.size() - 2) {
                builder.append(", ");
            } else if (i == items.size() - 2) {
                String and = dur.getAnd();
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

    public static @Nullable Object colorToParticleColor(TextColor color, Class<?> dataClass) {
        return colorToParticleColor(Color.fromRGB(color.red(), color.green(), color.blue()), dataClass);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static @Nullable Object colorToParticleColor(Color color, Class<?> dataClass) {
        if (dataClass == Color.class) {
            return color;
        } else if (dataClass == Particle.DustOptions.class) {
            return new Particle.DustOptions(color, 1);
        } else if (dataClass == Particle.Spell.class) {
            return new Particle.Spell(color, 0.5F);
        } else {
            return null;
        }
    }

    @Deprecated(forRemoval = true, since = "4.5.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    public static String commandRoot() {
        return FactionsPlugin.instance().tl().commands().generic().getCommandRoot().getFirstAlias();
    }
}
