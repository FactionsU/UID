package dev.kitteh.factions.event;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

class FactionRenameEventTest {
    @Test
    void administrativeRenameKeepsTargetSeparateFromSenderFaction() {
        Faction own = faction();
        Faction target = faction();
        FPlayer sender = player(own);
        FactionRenameEvent event = new FactionRenameEvent(target, sender, "North");

        assertSame(target, event.getFaction());
        assertNotSame(own, event.getFaction());
        assertSame(sender, event.getFPlayer());
        assertEquals("North", event.getFactionTag());
        assertFalse(event.isCancelled());
        event.setCancelled(true);
        assertTrue(event.isCancelled());
    }

    @Test
    void existingConstructorPreservesOwnFactionBehavior() {
        Faction own = faction();
        FPlayer sender = player(own);
        FactionRenameEvent event = new FactionRenameEvent(sender, "North");
        assertSame(own, event.getFaction());
        assertSame(sender, event.getFPlayer());
        assertEquals("North", event.getFactionTag());
    }

    private static Faction faction() {
        return (Faction) Proxy.newProxyInstance(Faction.class.getClassLoader(),
                new Class<?>[]{Faction.class}, (proxy, method, args) -> {
                    throw new AssertionError("Unexpected faction access: " + method.getName());
                });
    }

    private static FPlayer player(Faction own) {
        return (FPlayer) Proxy.newProxyInstance(FPlayer.class.getClassLoader(),
                new Class<?>[]{FPlayer.class}, (proxy, method, args) -> {
                    if (method.getName().equals("faction") && method.getParameterCount() == 0) {
                        return own;
                    }
                    throw new AssertionError("Unexpected player access: " + method.getName());
                });
    }
}
