package dev.kitteh.factions.command;

import dev.kitteh.factions.util.ComponentDispatcher;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public record LilAudience(Sender sender) implements Audience {
    @Override
    public void sendMessage(@NotNull Component message) {
        ComponentDispatcher.send(sender.sender(), message);
    }
}
