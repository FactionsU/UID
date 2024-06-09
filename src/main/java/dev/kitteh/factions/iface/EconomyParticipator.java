package dev.kitteh.factions.iface;

import dev.kitteh.factions.util.TL;
import org.bukkit.OfflinePlayer;

public interface EconomyParticipator extends RelationParticipator {

    String getAccountId();

    OfflinePlayer getOfflinePlayer();

    void msg(String str, Object... args);

    void msg(TL translation, Object... args);
}
