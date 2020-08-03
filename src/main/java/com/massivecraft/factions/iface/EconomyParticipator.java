package com.massivecraft.factions.iface;

import com.massivecraft.factions.util.TL;
import org.bukkit.OfflinePlayer;

public interface EconomyParticipator extends RelationParticipator {

    String getAccountId();

    OfflinePlayer getOfflinePlayer();

    void msg(String str, Object... args);

    void msg(TL translation, Object... args);
}
