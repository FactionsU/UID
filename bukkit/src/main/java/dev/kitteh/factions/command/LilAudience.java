package dev.kitteh.factions.command;

import dev.kitteh.factions.util.ComponentDispatcher;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@ApiStatus.Internal
@NullMarked
// Lil Audience is my (w)rapper name
public record LilAudience(CommandSender sender) implements Audience {
    @Override
    public void sendMessage(Component message) {
        ComponentDispatcher.send(sender, message);
    }
}
