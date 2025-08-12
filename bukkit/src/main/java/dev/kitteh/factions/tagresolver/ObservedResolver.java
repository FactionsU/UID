package dev.kitteh.factions.tagresolver;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.util.TL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
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
public abstract class ObservedResolver implements TagResolver {
    protected final String name;
    protected final @Nullable FPlayer observer;
    protected final @Nullable Player observerPlayer;

    protected ObservedResolver(String name, @Nullable FPlayer observer) {
        this.name = name;
        this.observer = observer;
        this.observerPlayer = this.observer == null ? null : this.observer.asPlayer();
    }

    protected ObservedResolver(String name, @Nullable Player observer) {
        this.name = name;
        this.observer = observer == null ? null : FPlayers.fPlayers().get(observer);
        this.observerPlayer = observer;
    }

    @Override
    public @Nullable Tag resolve(String name, ArgumentQueue arguments, Context ctx) throws ParsingException {
        if (!this.name.equals(name)) {
            return null;
        }

        return this.solve(arguments, ctx);
    }

    protected abstract Tag solve(ArgumentQueue arguments, Context ctx);

    @Override
    public boolean has(String name) {
        return name.equals(this.name);
    }

    public static Tag tag(String string) {
        return Tag.selfClosingInserting(Component.text(string));
    }

    public static Tag tagLegacy(String string) {
        return Tag.selfClosingInserting(LegacyComponentSerializer.legacySection().deserialize(string));
    }

    public static Tag tagLegacy(TL tl) {
        return Tag.selfClosingInserting(LegacyComponentSerializer.legacySection().deserialize(tl.toString()));
    }

    public static Tag tag(double d) {
        return Tag.selfClosingInserting(Component.text(d));
    }

    public static Tag tag(int in) {
        return Tag.selfClosingInserting(Component.text(in));
    }

    public static Tag tag(ComponentLike component) {
        return Tag.selfClosingInserting(component);
    }

    public static Tag tag(TextColor color) {
        return Tag.styling(c->c.color(color));
    }
}