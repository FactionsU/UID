package com.massivecraft.factions.util;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.Role;
import com.massivecraft.factions.util.material.MaterialDb;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;

public class MiscUtil {
    private static final Map<String, EntityType> entityTypeMap;
    public static final Function<String, EntityType> ENTITY_TYPE_FUNCTION;
    public static final Function<String, Material> MATERIAL_FUNCTION;
    private static final Map<String, CreatureSpawnEvent.SpawnReason> spawnReasonMap;
    public static final Function<String, CreatureSpawnEvent.SpawnReason> SPAWN_REASON_FUNCTION;

    static {
        entityTypeMap = new HashMap<>();
        for (EntityType entityType : EntityType.values()) {
            entityTypeMap.put(entityType.name(), entityType);
        }
        ENTITY_TYPE_FUNCTION = (string) -> string == null ? null : entityTypeMap.get(string.toUpperCase());

        MATERIAL_FUNCTION = (string) -> {
            Material mat = null;
            if (string != null) {
                mat = MaterialDb.get(string, null);
            }
            return mat;
        };

        spawnReasonMap = new HashMap<>();
        for (CreatureSpawnEvent.SpawnReason reason : CreatureSpawnEvent.SpawnReason.values()) {
            spawnReasonMap.put(reason.name(), reason);
        }
        SPAWN_REASON_FUNCTION = (string) -> string == null ? null : spawnReasonMap.get(string.toUpperCase());
    }

    public static <Type> Set<Type> typeSetFromStringSet(Set<String> stringSet, Function<String, Type> function) {
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

    // Inclusive range
    public static long[] range(long start, long end) {
        long[] values = new long[(int) Math.abs(end - start) + 1];

        if (end < start) {
            long oldstart = start;
            start = end;
            end = oldstart;
        }

        for (long i = start; i <= end; i++) {
            values[(int) (i - start)] = i;
        }

        return values;
    }

    /// TODO create tag whitelist!!
    public static HashSet<String> substanceChars = new HashSet<>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"));

    public static String getComparisonString(String str) {
        StringBuilder ret = new StringBuilder();

        str = ChatColor.stripColor(str);
        str = str.toLowerCase();

        for (char c : str.toCharArray()) {
            if (substanceChars.contains(String.valueOf(c))) {
                ret.append(c);
            }
        }
        return ret.toString().toLowerCase();
    }

    public static ArrayList<String> validateTag(String str) {
        ArrayList<String> errors = new ArrayList<>();

        for (String blacklistItem : FactionsPlugin.getInstance().conf().factions().other().getNameBlacklist()) {
            if (str.toLowerCase().contains(blacklistItem.toLowerCase())) {
                errors.add(FactionsPlugin.getInstance().txt().parse(TL.GENERIC_FACTIONTAG_BLACKLIST.toString()));
                break;
            }
        }

        if (getComparisonString(str).length() < FactionsPlugin.getInstance().conf().factions().other().getTagLengthMin()) {
            errors.add(FactionsPlugin.getInstance().txt().parse(TL.GENERIC_FACTIONTAG_TOOSHORT.toString(), FactionsPlugin.getInstance().conf().factions().other().getTagLengthMin()));
        }

        if (str.length() > FactionsPlugin.getInstance().conf().factions().other().getTagLengthMax()) {
            errors.add(FactionsPlugin.getInstance().txt().parse(TL.GENERIC_FACTIONTAG_TOOLONG.toString(), FactionsPlugin.getInstance().conf().factions().other().getTagLengthMax()));
        }

        List<String> badChars = null;
        for (char c : str.toCharArray()) {
            if (!substanceChars.contains(String.valueOf(c))) {
                if (badChars == null) {
                    badChars = new ArrayList<>();
                }
                badChars.add(Character.toString(c));
            }
        }
        if (badChars != null) {
            errors.add(FactionsPlugin.getInstance().txt().parse(TL.GENERIC_FACTIONTAG_ALPHANUMERIC.toString(), String.join("", badChars)));
        }

        return errors;
    }

    public static Iterable<FPlayer> rankOrder(Iterable<FPlayer> players) {
        List<FPlayer> admins = new ArrayList<>();
        List<FPlayer> coleaders = new ArrayList<>();
        List<FPlayer> moderators = new ArrayList<>();
        List<FPlayer> normal = new ArrayList<>();
        List<FPlayer> recruit = new ArrayList<>();

        for (FPlayer player : players) {

            // Fix for some data being broken when we added the recruit rank.
            if (player.getRole() == null) {
                player.setRole(Role.NORMAL);
                FactionsPlugin.getInstance().log(Level.WARNING, String.format("Player %s had null role. Setting them to normal. This isn't good D:", player.getName()));
            }

            switch (player.getRole()) {
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
}
