package dev.kitteh.factions.tagresolver;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.util.TL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public abstract class ObservedResolver extends HelperResolver {
    protected final @Nullable FPlayer observer;
    protected final @Nullable Player observerPlayer;

    protected ObservedResolver(String name, @Nullable FPlayer observer) {
        super(name);
        this.observer = observer;
        this.observerPlayer = this.observer == null ? null : this.observer.asPlayer();
    }

    protected ObservedResolver(String name, @Nullable Player observer) {
        super(name);
        this.observer = observer == null ? null : FPlayers.fPlayers().get(observer);
        this.observerPlayer = observer;
    }
}