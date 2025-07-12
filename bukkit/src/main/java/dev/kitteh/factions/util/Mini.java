package dev.kitteh.factions.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public class Mini {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final MiniMessage limitedMiniMessage = MiniMessage.builder()
            .tags(TagResolver.resolver(StandardTags.color(), StandardTags.decorations(), StandardTags.rainbow(), StandardTags.pride()))
            .build();

    public static Component parse(List<String> input, TagResolver... tagResolvers) {
        TextComponent.Builder builder = Component.text();
        for (int i = 0; i < input.size(); i++) {
            if (i > 0) {
                builder.appendNewline();
            }
            String line = input.get(i);
            if (line.isBlank()) {
                builder.append(Component.empty());
            } else {
                builder.append(parse(line, tagResolvers));
            }
        }
        return builder.build();
    }

    public static Component parse(String input) {
        return miniMessage.deserialize(input);
    }

    public static Component parse(String input, TagResolver... tagResolvers) {
        return miniMessage.deserialize(input, tagResolvers);
    }

    public static Component parse(String input, Iterable<TagResolver> tagResolvers) {
        return miniMessage.deserialize(input, TagResolver.resolver(tagResolvers));
    }

    public static Component parseLimited(String input, TagResolver... tagResolvers) {
        return limitedMiniMessage.deserialize(input, tagResolvers);
    }

    public static String legacy(ComponentLike componentLike) {
        return LegacyComponentSerializer.legacySection().serialize(componentLike.asComponent());
    }
}