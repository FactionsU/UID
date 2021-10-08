package com.massivecraft.factions.config.transition.oldclass.v0;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.perms.Role;
import com.massivecraft.factions.struct.BanInfo;
import com.massivecraft.factions.util.LazyLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class OldMemoryFactionV0 {
    String id = null;
    boolean peacefulExplosionsEnabled;
    boolean permanent;
    String tag;
    String description;
    boolean open;
    boolean peaceful;
    Integer permanentPower;
    LazyLocation home;
    long foundedDate;
    double powerBoost;
    Map<String, Relation> relationWish = new HashMap<>();
    Map<FLocation, Set<String>> claimOwnership = new ConcurrentHashMap<>();
    Set<String> invites = new HashSet<>();
    HashMap<String, List<String>> announcements = new HashMap<>();
    ConcurrentHashMap<String, LazyLocation> warps = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> warpPasswords = new ConcurrentHashMap<>();
    long lastDeath;
    int maxVaults;
    Role defaultRole;
    Map<String, Map<String, OldAccessV0>> permissions = new HashMap<>();
    Set<BanInfo> bans = new HashSet<>();

    private OldMemoryFactionV0() {
    }
}
