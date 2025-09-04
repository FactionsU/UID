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
    private static final MiniMessage limitedMiniMessage = createLimitedMiniMessage();

    private static MiniMessage createLimitedMiniMessage() {
        // Build a TagResolver set that is compatible across Adventure versions.
        TagResolver.Builder tags = TagResolver.builder()
                .resolver(StandardTags.color())
                .resolver(StandardTags.decorations());
        // Try optional tags that may not exist on older servers
        try {
            tags.resolver(StandardTags.rainbow());
        } catch (Throwable ignored) {
            // Older Adventure: no rainbow tag
        }
        try {
            // Use reflection to avoid linking errors if 'pride' doesn't exist
            var m = StandardTags.class.getMethod("pride");
            Object resolver = m.invoke(null);
            if (resolver instanceof TagResolver tr) {
                tags.resolver(tr);
            }
        } catch (Throwable ignored) {
            // Older Adventure: no pride tag
        }
        return MiniMessage.builder().tags(tags.build()).build();
    }

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

    public static String toLegacy(ComponentLike componentLike) {
        return LegacyComponentSerializer.legacySection().serialize(componentLike.asComponent());
    }
}
