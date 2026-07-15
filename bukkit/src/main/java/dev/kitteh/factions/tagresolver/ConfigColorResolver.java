package dev.kitteh.factions.tagresolver;

import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A resolver for extra colors from the config.
 */
@ApiStatus.AvailableSince("4.7.0")
@NullMarked
public class ConfigColorResolver implements TagResolver {
    private static final ConfigColorResolver INSTANCE = new ConfigColorResolver();

    /**
     * Gets this resolver, single-instance, and can be called before config is loaded as it will auto update on config (re)load.
     *
     * @return the resolver
     */
    public static ConfigColorResolver resolver() {
        return INSTANCE;
    }

    /**
     * Updates from config. You do not need to call this.
     */
    public static void update() {
        Map<String, Tag> newMap = new HashMap<>();
        Map<String, TextColor> newColors = new HashMap<>();
        Confs.tl().aColorfulMessage().getColors().forEach((name, color) -> {
            TextColor textColor;
            if (color.startsWith("#")) {
                textColor = TextColor.fromHexString(color);
            } else {
                textColor = NamedTextColor.NAMES.value(color.toLowerCase());
            }
            if (textColor == null) {
                AbstractFactionsPlugin.instance().getLogger().warning("Invalid color for name \"" + name + "\": \"" + color + "\" - Using white until fixed. Can be updated with the command: fa reload");
                textColor = NamedTextColor.WHITE;
            }
            TextColor col = textColor;
            newMap.put(name.toLowerCase(Locale.ROOT), Tag.styling(c -> c.color(col)));
            newColors.put(name.toLowerCase(Locale.ROOT), col);
        });
        INSTANCE.map = Map.copyOf(newMap);
        INSTANCE.colors = Map.copyOf(newColors);
    }

    private ConfigColorResolver() {
    }

    private Map<String, Tag> map = new HashMap<>();
    private Map<String, TextColor> colors = new HashMap<>();

    @Override
    public @Nullable Tag resolve(String name, ArgumentQueue arguments, Context ctx) throws ParsingException {
        return this.map.get(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean has(String name) {
        return this.map.containsKey(name.toLowerCase(Locale.ROOT));
    }

    public @Nullable TextColor color(String name) {
        return this.colors.get(name.toLowerCase(Locale.ROOT));
    }
}
