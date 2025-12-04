package dev.kitteh.factions.integration;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.depenizen.bukkit.Bridge;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.integration.depenizen.FactionTag;
import dev.kitteh.factions.integration.depenizen.FactionsLocationProperties;
import dev.kitteh.factions.integration.depenizen.FactionsNPCProperties;
import dev.kitteh.factions.integration.depenizen.FactionsPlayerProperties;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;

import java.util.logging.Level;

/*
This code is from the Depenizen plugin, modified to work with this plugin.
Original copyright notice is as follows:

Copyright (c) 2019-2022 The Denizen Script Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
@SuppressWarnings("deprecation")
@ApiStatus.Internal
public class Depenizen extends Bridge {
    public static boolean init(Plugin plugin) {
        try {
            if (plugin instanceof com.denizenscript.depenizen.bukkit.Depenizen depenizen) {
                depenizen.registerBridge("Factions", Depenizen::new);
            }
        } catch (Exception e) {
            AbstractFactionsPlugin.instance().getLogger().log(Level.WARNING, "Could not load Depenizen integration", e);
            return false;
        }
        AbstractFactionsPlugin.instance().getLogger().info("Loaded Depenizen integration!");
        return true;
    }

    @Override
    public void init() {
        ObjectFetcher.registerWithObjectFetcher(FactionTag.class, null);
        PropertyParser.registerProperty(FactionsNPCProperties.class, NPCTag.class);
        PropertyParser.registerProperty(FactionsPlayerProperties.class, PlayerTag.class);
        PropertyParser.registerProperty(FactionsLocationProperties.class, LocationTag.class);
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                factionTagEvent(event);
            }
        }, "faction");
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                tagEvent(event);
            }
        }, "factions");
    }

    public void factionTagEvent(ReplaceableTagEvent event) {
        Attribute attribute = event.getAttributes().fulfill(1);
        // <--[tag]
        // @attribute <faction[<name>]>
        // @returns FactionTag
        // @plugin Depenizen, Factions
        // @description
        // Returns the faction for the input name.
        // -->
        String nameOrId = attribute.getParam();

        Faction f = Factions.factions().get(nameOrId);
        if (f == null) {
            try {
                f = Factions.factions().get(Integer.parseInt(nameOrId));
            } catch (NumberFormatException ignored) {
            }
        }
        if (f != null) {
            event.setReplacedObject(new FactionTag(f).getObjectAttribute(attribute.fulfill(1)));
        }
    }

    public void tagEvent(ReplaceableTagEvent event) {
        Attribute attribute = event.getAttributes().fulfill(1);

        // <--[tag]
        // @attribute <factions.list_factions>
        // @returns ListTag(FactionTag)
        // @plugin Depenizen, Factions
        // @description
        // Returns a list of all current factions.
        // -->
        if (attribute.startsWith("list_factions")) {
            ListTag factions = new ListTag();
            for (Faction f : Factions.factions().all()) {
                factions.addObject(new FactionTag(f));
            }
            event.setReplacedObject(factions.getObjectAttribute(attribute.fulfill(1)));
        }
    }
}
