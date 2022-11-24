package com.massivecraft.factions.integration.depenizen;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;

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
public class FactionsPlayerProperties implements Property {

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "FactionsPlayer";
    }

    @Override
    public void adjust(Mechanism mechanism) {
        // None
    }

    public static boolean describes(ObjectTag object) {
        return object instanceof PlayerTag;
    }

    public static FactionsPlayerProperties getFrom(ObjectTag object) {
        if (!describes(object)) {
            return null;
        } else {
            return new FactionsPlayerProperties((PlayerTag) object);
        }
    }

    public static final String[] handledTags = new String[]{
            "factions", "faction"
    };

    public static final String[] handledMechs = new String[]{
    }; // None

    private FactionsPlayerProperties(PlayerTag object) {
        player = object;
    }

    public FPlayer getFPlayer() {
        return FPlayers.getInstance().getById(player.getUUID().toString());
    }

    PlayerTag player;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute.startsWith("factions")) {

            attribute = attribute.fulfill(1);

            // <--[tag]
            // @attribute <PlayerTag.factions.power>
            // @returns ElementTag(Decimal)
            // @plugin Depenizen, Factions
            // @description
            // Returns the player's power level.
            // -->
            if (attribute.startsWith("power")) {
                return new ElementTag(getFPlayer().getPower()).getObjectAttribute(attribute.fulfill(1));
            } else if (getFPlayer().hasFaction()) {

                // <--[tag]
                // @attribute <PlayerTag.factions.role>
                // @returns ElementTag
                // @plugin Depenizen, Factions
                // @description
                // Returns the player's role in their faction.
                // Note: In modern Factions these are called ranks instead of roles.
                // -->
                if (attribute.startsWith("role")) {
                    if (getFPlayer().getRole() != null) {
                        return new ElementTag(getFPlayer().getRole().toString()).getObjectAttribute(attribute.fulfill(1));
                    }
                }

                // <--[tag]
                // @attribute <PlayerTag.factions.title>
                // @returns ElementTag
                // @plugin Depenizen, Factions
                // @description
                // Returns the player's title.
                // -->
                else if (attribute.startsWith("title")) {
                    if (getFPlayer().getTitle() != null) {
                        return new ElementTag(getFPlayer().getTitle()).getObjectAttribute(attribute.fulfill(1));
                    }
                }
            }

        }

        // <--[tag]
        // @attribute <PlayerTag.faction>
        // @returns FactionTag
        // @plugin Depenizen, Factions
        // @description
        // Returns the player's faction.
        // -->
        else if (attribute.startsWith("faction")) {
            return new FactionTag(getFPlayer().getFaction()).getObjectAttribute(attribute.fulfill(1));
        }

        return null;

    }
}
