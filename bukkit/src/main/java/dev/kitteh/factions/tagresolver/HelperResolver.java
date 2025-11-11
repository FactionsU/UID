package dev.kitteh.factions.tagresolver;

import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.TL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@ApiStatus.AvailableSince("4.3.0")
@NullMarked
public abstract class HelperResolver implements TagResolver {
    protected final String name;

    protected HelperResolver(String name) {
        this.name = name;
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

    public static Tag tagMini(String string, TagResolver... resolvers) {
        return Tag.selfClosingInserting(Mini.parse(string, resolvers));
    }

    public static Tag tagLegacy(String string) {
        return Tag.selfClosingInserting(LegacyComponentSerializer.legacySection().deserialize(string));
    }

    public static Tag tagLegacyIns(String string) {
        return Tag.inserting(LegacyComponentSerializer.legacySection().deserialize(string));
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
        return Tag.styling(c -> c.color(color));
    }

    public static Tag tagToggle(boolean condition, ArgumentQueue args) {
        boolean flip = args.hasNext() && args.pop().lowerValue().equals("else");
        if (flip) {
            condition = !condition;
        }
        if (condition) {
            return (Modifying) (current, depth) -> {
                if (depth == 0) {
                    return current;
                }
                return Component.empty();
            };
        } else {
            return (Modifying) (current, depth) -> Component.empty();
        }
    }

    public static Tag tagTip(List<String> lines, TagResolver... resolvers) {
        TextComponent.Builder builder = Component.text();
        boolean newLine = false;
        for (String line : lines) {
            if (newLine) {
                builder.appendNewline();
            }
            newLine = true;
            builder.append(Mini.parse(line, resolvers));
        }
        return Tag.styling(b->b.hoverEvent(HoverEvent.showText(builder.build())));
    }

    public static Tag empty() {
        return tag(Component.empty());
    }
}