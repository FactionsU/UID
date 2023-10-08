package com.massivecraft.factions.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Mini {
    public static Component parse(String input) {
        return miniMessage().deserialize(input);
    }

    public static Component parse(String input, TagResolver... tagResolvers) {
        return miniMessage().deserialize(input, tagResolvers);
    }

    public static Component parse(String input, Iterable<TagResolver> tagResolvers) {
        return miniMessage().deserialize(input, TagResolver.resolver(tagResolvers));
    }

    public static MiniMessage miniMessage() {
        return MiniMessage.miniMessage();
    }

    public static String legacy(ComponentLike componentLike) {
        return LegacyComponentSerializer.legacySection().serialize(componentLike.asComponent());
    }
}