package dev.kitteh.factions.config;

import dev.kitteh.factions.config.file.DynmapConfig;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.config.file.PermissionsConfig;
import dev.kitteh.factions.config.file.TranslationsConfig;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class Confs {
    private Confs() {
    }

    public static DynmapConfig dynmap() {
        return AbstractFactionsPlugin.instance().configManager().dynmapConfig();
    }

    @SuppressWarnings("ConfusingMainMethod")
    public static MainConfig main() {
        return AbstractFactionsPlugin.instance().configManager().mainConfig();
    }

    public static PermissionsConfig perms() {
        return AbstractFactionsPlugin.instance().configManager().permissionsConfig();
    }

    public static TranslationsConfig tl() {
        return AbstractFactionsPlugin.instance().configManager().translationsConfig();
    }
}
