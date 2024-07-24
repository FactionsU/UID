package com.massivecraft.factions.integration.depenizen;

import com.denizenscript.denizen.objects.ChunkTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.util.LazyLocation;

import java.util.UUID;

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
public class FactionTag implements ObjectTag {
    public static FactionTag valueOf(String string) {
        return valueOf(string, null);
    }

    @Fetchable("faction")
    public static FactionTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }

        string = string.replace("faction@", "");
        Faction faction = Factions.getInstance().getByTag(string);
        if (faction != null) {
            return new FactionTag(faction);
        }

        return null;
    }

    public static boolean matches(String arg) {
        return valueOf(arg) != null;
    }

    Faction faction;

    public FactionTag(Faction faction) {
        if (faction != null) {
            this.faction = faction;
        } else {
            Debug.echoError("Faction referenced is null!");
        }
    }

    public Faction getFaction() {
        return faction;
    }

    /////////////////////
    //   ObjectTag Methods
    /////////////////

    private String prefix = "Faction";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public ObjectTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public String identify() {
        return "faction@" + faction.getTag();
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        // <--[tag]
        // @attribute <FactionTag.balance>
        // @returns ElementTag(Decimal)
        // @plugin Depenizen, Factions
        // @description
        // Returns the amount of money the faction currently has.
        // -->
        if (attribute.startsWith("balance")) {
            return new ElementTag(Econ.getBalance(faction))
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <FactionTag.warp[<name>]>
        // @returns LocationTag
        // @plugin Depenizen, Factions
        // @description
        // Returns the location of the faction's warp by name, if any.
        // Note that this was previously named "home" instead of "warp".
        // -->
        else if (attribute.startsWith("warp") && attribute.hasParam()) {
            LazyLocation warp = faction.getWarp(attribute.getParam());
            if (warp != null) {
                return new LocationTag(warp.getLocation())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        } else if (attribute.startsWith("home")) { // Legacy sorta-compat
            if (faction.hasHome()) {
                return new LocationTag(faction.getHome())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <FactionTag.id>
        // @returns ElementTag
        // @plugin Depenizen, Factions
        // @description
        // Returns the unique ID for this faction.
        // -->
        else if (attribute.startsWith("id")) {
            return new ElementTag(String.valueOf(faction.getIntId())).getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <FactionTag.is_open>
        // @returns ElementTag(Boolean)
        // @plugin Depenizen, Factions
        // @description
        // Returns true if the faction is open.
        // -->
        else if (attribute.startsWith("isopen") || attribute.startsWith("is_open")) {
            return new ElementTag(faction.getOpen())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <FactionTag.is_peaceful>
        // @returns ElementTag(Boolean)
        // @plugin Depenizen, Factions
        // @description
        // Returns true if the faction is peaceful.
        // -->
        else if (attribute.startsWith("ispeaceful") || attribute.startsWith("is_peaceful")) {
            return new ElementTag(faction.isPeaceful())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <FactionTag.is_permanent>
        // @returns ElementTag(Boolean)
        // @plugin Depenizen, Factions
        // @description
        // Returns true if the faction is permanent.
        // -->
        else if (attribute.startsWith("ispermanent") || attribute.startsWith("is_permanent")) {
            return new ElementTag(faction.isPermanent())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <FactionTag.leader>
        // @returns PlayerTag
        // @plugin Depenizen, Factions
        // @description
        // Returns the faction's leader as a PlayerTag.
        // -->
        else if (attribute.startsWith("leader")) {
            if (faction.getFPlayerAdmin() != null) {
                return new PlayerTag(UUID.fromString(faction.getFPlayerAdmin().getId()))
                        .getObjectAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <FactionTag.name>
        // @returns ElementTag
        // @plugin Depenizen, Factions
        // @description
        // Returns the name of the faction.
        // -->
        else if (attribute.startsWith("name")) {
            return new ElementTag(faction.getTag())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <FactionTag.player_count>
        // @returns ElementTag(Number)
        // @plugin Depenizen, Factions
        // @description
        // Returns the number of players in the faction.
        // -->
        else if (attribute.startsWith("playercount") || attribute.startsWith("player_count")) {
            return new ElementTag(faction.getFPlayers().size())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <FactionTag.power>
        // @returns ElementTag(Decimal)
        // @plugin Depenizen, Factions
        // @description
        // Returns the amount of power the faction currently has.
        // -->
        else if (attribute.startsWith("power")) {
            return new ElementTag(faction.getPower())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <FactionTag.relation[<faction>]>
        // @returns ElementTag
        // @plugin Depenizen, Factions
        // @description
        // Returns the current relation between the faction and another faction.
        // -->
        else if (attribute.startsWith("relation")) {
            FactionTag to = valueOf(attribute.getParam());
            if (to != null) {
                return new ElementTag(faction.getRelationTo(to.getFaction()).toString())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <FactionTag.size>
        // @returns ElementTag(Number)
        // @plugin Depenizen, Factions
        // @description
        // Returns the amount of land the faction has.
        // -->
        else if (attribute.startsWith("size")) {
            return new ElementTag(faction.getAllClaims().size())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <FactionTag.claimed_chunks>
        // @returns ListTag(ChunkTag)
        // @plugin Depenizen, Factions
        // @description
        // Returns a list of all chunks claimed in the faction.
        // -->
        if (attribute.startsWith("claimed_chunks")) {
            ListTag dchunks = new ListTag();
            for (FLocation claim : faction.getAllClaims()) {
                dchunks.addObject(new ChunkTag(claim.getChunk()));
            }
            return dchunks.getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <FactionTag.list_players>
        // @returns ListTag(PlayerTag)
        // @plugin Depenizen, Factions
        // @description
        // Returns a list of all players in the faction.
        // -->
        if (attribute.startsWith("list_players")) {
            ListTag players = new ListTag();
            for (FPlayer ps : faction.getFPlayers()) {
                players.addObject(new PlayerTag(UUID.fromString(ps.getId())));
            }
            return players.getObjectAttribute(attribute.fulfill(1));
        }

        return new ElementTag(identify()).getObjectAttribute(attribute);

    }
}
