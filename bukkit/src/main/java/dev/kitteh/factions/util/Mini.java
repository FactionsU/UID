package dev.kitteh.factions.util;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.tagresolver.GeneralResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public class Mini {
    private static final MiniMessage miniMessage = MiniMessage.builder().editTags(b -> b.resolver(GeneralResolver.resolver())).build();
    private static final MiniMessage limitedMiniMessage = MiniMessage.builder()
            .tags(TagResolver.resolver(StandardTags.color(), StandardTags.decorations(), StandardTags.rainbow(), StandardTags.pride()))
            .build();

    public static Component parse(List<String> input, TagResolver... tagResolvers) {
        return parse(input, null, tagResolvers);
    }

    @ApiStatus.AvailableSince("4.5.0")
    public static Component parse(List<String> input, @Nullable FPlayer target, TagResolver... tagResolvers) {
        TextComponent.Builder builder = Component.text();
        for (int i = 0; i < input.size(); i++) {
            if (i > 0) {
                builder.appendNewline();
            }
            String line = input.get(i);
            if (line.isBlank()) {
                builder.append(Component.empty());
            } else {
                builder.append(parse(line, target, tagResolvers));
            }
        }
        return builder.build();
    }

    public static Component parse(String input) {
        return parse(input, (FPlayer) null);
    }

    @ApiStatus.AvailableSince("4.5.0")
    public static Component parse(String input, @Nullable FPlayer target) {
        if (target == null) {
            return miniMessage.deserialize(input);
        }
        return miniMessage.deserialize(input, target);
    }

    public static Component parse(String input, TagResolver... tagResolvers) {
        return parse(input, null, tagResolvers);
    }

    @ApiStatus.AvailableSince("4.5.0")
    public static Component parse(String input, @Nullable FPlayer target, TagResolver... tagResolvers) {
        if (target == null) {
            return miniMessage.deserialize(input, tagResolvers);
        }
        return miniMessage.deserialize(input, target, tagResolvers);
    }

    @Deprecated(forRemoval = true, since = "4.5.0")
    public static Component parse(String input, Iterable<TagResolver> tagResolvers) {
        return miniMessage.deserialize(input, TagResolver.resolver(tagResolvers));
    }

    public static Component parseLimited(String input, TagResolver... tagResolvers) {
        return limitedMiniMessage.deserialize(input, tagResolvers);
    }

    public static String toLegacy(ComponentLike componentLike) {
        return LegacyComponentSerializer.legacySection().serialize(componentLike.asComponent());
    }
}