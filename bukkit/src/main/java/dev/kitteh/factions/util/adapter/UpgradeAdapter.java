package dev.kitteh.factions.util.adapter;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.upgrade.Upgrade;
import dev.kitteh.factions.upgrade.UpgradeRegistry;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.util.Set;

public class UpgradeAdapter extends TypeAdapter<Upgrade> {
    @Override
    public void write(JsonWriter out, Upgrade value) throws IOException {
        out.value(value.name());
    }

    @Override
    public Upgrade read(JsonReader in) throws IOException {
        JsonToken peek = in.peek();

        if (peek != JsonToken.STRING) {
            throw new JsonParseException("Non-string token found for Upgrade! Found " + peek.name());
        }
        String string = in.nextString();

        Upgrade upgrade = UpgradeRegistry.getUpgrade(string);

        if (upgrade == null) {
            AbstractFactionsPlugin.instance().getLogger().warning("No upgrade found for universe.json entry '" + string + "'");
            return new Upgrade.Simple(
                    string,
                    Component.text("UNKNOWN " + string),
                    Component.text("UNKNOWN UPGRADE"),
                    (_, _) -> Component.text("?????"),
                    1,
                    Set.of()
            );
        }

        return upgrade;
    }
}
