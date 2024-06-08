package com.massivecraft.factions.config.file;

import com.google.common.reflect.TypeToken;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.config.annotation.Comment;
import com.massivecraft.factions.config.annotation.DefinedType;
import com.massivecraft.factions.config.annotation.WipeOnReload;
import com.massivecraft.factions.integration.dynmap.DynmapStyle;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "InnerClassMayBeStatic"})
public class DynmapConfig {
    public class Dynmap {
        @Comment("Should the dynmap integration be used?")
        private boolean enabled = true;

        @Comment("Name of the Factions layer")
        private String layerName = "Factions";

        @Comment("Should the layer be visible per default")
        private boolean layerVisible = true;

        @Comment("Ordering priority in layer menu (low goes before high - default is 0)")
        private int layerPriority = 2;

        @Comment("(optional) set minimum zoom level before layer is visible (0 = default, always visible)")
        private int layerMinimumZoom = 0;

        @Comment("Format for popup")
        private String description =
                """
                        <div class="infowindow">
                        <span style="font-weight: bold; font-size: 150%;">{faction}</span><br>
                        <span style="font-style: italic; font-size: 110%;">{description}</span><br>\
                        <br>
                        <span style="font-weight: bold;">Leader:</span> %players.leader%<br>
                        <span style="font-weight: bold;">Admins:</span> %players.admins.count%<br>
                        <span style="font-weight: bold;">Moderators:</span> %players.moderators.count%<br>
                        <span style="font-weight: bold;">Members:</span> %players.normals.count%<br>
                        <span style="font-weight: bold;">TOTAL:</span> %players.count%<br>
                        </br>
                        <span style="font-weight: bold;">Bank:</span> %money%<br>
                        <br>
                        </div>""";

        @Comment("Warp popup")
        private String warpDescription = "Warp: %warpname%";

        @Comment("Enable the %money% macro. Only do this if you know your economy manager is thread-safe.")
        private boolean descriptionMoney = false;

        @Comment("Allow players in faction to see one another on Dynmap (only relevant if Dynmap has 'player-info-protected' enabled)")
        private boolean visibilityByFaction = true;

        @Comment("If not empty, *only* listed factions (by name or ID) will be shown.\n" +
                "To show all factions in a world, use 'world:worldnamehere'")
        private Set<String> visibleFactions = new HashSet<>();

        @Comment("To hide all factions in a world, use 'world:worldnamehere'")
        private Set<String> hiddenFactions = new HashSet<>();

        @Comment("Set to true if you do a permanent country map")
        private boolean onlyUpdateWorldOnce = false;

        @Comment("Update claims and homes with dynmap every X seconds. Default is 300 (5 minutes)\n" +
                "Minimum value: 1. To disable updating claims, edit onlyUpdateWorldOnce instead")
        private int claimUpdatePeriod = 300;

        @Comment("Should home markers be shown?")
        private boolean showMarkers = true;

        @Comment("Should warp markers be shown?")
        private boolean showWarpMarkers = false;

        public boolean isEnabled() {
            return enabled;
        }

        public String getLayerName() {
            return layerName;
        }

        public boolean isLayerVisible() {
            return layerVisible;
        }

        public int getLayerPriority() {
            return layerPriority;
        }

        public int getLayerMinimumZoom() {
            return layerMinimumZoom;
        }

        public String getDescription() {
            return description;
        }

        public String getWarpDescription() {
            return warpDescription;
        }

        public boolean isDescriptionMoney() {
            return descriptionMoney;
        }

        public boolean isVisibilityByFaction() {
            return visibilityByFaction;
        }

        public Set<String> getVisibleFactions() {
            return visibleFactions;
        }

        public Set<String> getHiddenFactions() {
            return hiddenFactions;
        }

        public boolean isOnlyUpdateWorldOnce() {
            return onlyUpdateWorldOnce;
        }

        public int getClaimUpdatePeriod() {
            return claimUpdatePeriod;
        }

        public boolean isShowMarkers() {
            return showMarkers;
        }
        public boolean isShowWarpMarkers() {
            return showWarpMarkers;
        }

        // This is used for config loading
        @SuppressWarnings("unused")
        private transient TypeToken<Map<String, Style>> factionStylesToken = new TypeToken<>() {
        };

        @Comment("Per-faction overrides")
        @DefinedType
        private Map<String, Style> factionStyles = new HashMap<>() {
            {
                this.put("-1", new DynmapConfig.Style("#FF00FF", "#FF00FF"));
                this.put("-2", new DynmapConfig.Style("#FF0000", "#FF0000"));
            }
        };

        @WipeOnReload
        private transient Map<String, DynmapStyle> styles;

        public Map<String, DynmapStyle> getFactionStyles() {
            if (styles == null) {
                styles = new HashMap<>();
                Map<String, ?> mappy = factionStyles;
                for (Map.Entry<String, ?> e : mappy.entrySet()) {
                    String faction = e.getKey();
                    Object s = e.getValue();
                    if (s instanceof Style style) {
                        styles.put(faction, new DynmapStyle()
                                .setLineColor(style.getLineColor())
                                .setLineOpacity(style.getLineOpacity())
                                .setLineWeight(style.getLineWeight())
                                .setFillColor(style.getFillColor())
                                .setFillOpacity(style.getFillOpacity())
                                .setHomeMarker(style.getHomeMarker())
                                .setBoost(style.isStyleBoost()));
                    } else if (s instanceof Map) {
                        DynmapStyle style = new DynmapStyle();
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) s;
                        if (map.containsKey("homeMarker")) {
                            style.setHomeMarker(map.get("homeMarker").toString());
                        }
                        if (map.containsKey("fillOpacity")) {
                            style.setFillOpacity(getDouble(map.get("fillOpacity").toString()));
                        }
                        if (map.containsKey("lineWeight")) {
                            style.setLineWeight(getInt(map.get("lineWeight").toString()));
                        }
                        if (map.containsKey("lineColor")) {
                            style.setLineColor(map.get("lineColor").toString());
                        }
                        if (map.containsKey("styleBoost")) {
                            style.setBoost(Boolean.parseBoolean(map.get("styleBoost").toString()));
                        }
                        if (map.containsKey("fillColor")) {
                            style.setFillColor(map.get("fillColor").toString());
                        }
                        if (map.containsKey("lineOpacity")) {
                            style.setLineOpacity(getDouble(map.get("lineOpacity").toString()));
                        }
                        styles.put(faction, style);
                    } else {
                        FactionsPlugin.getInstance().getLogger().severe("Found broken Dynmap style entry for faction '" + faction + '\'');
                    }
                }
            }
            return styles;
        }

        private int getInt(String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
                return 1;
            }
        }

        private double getDouble(String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException ignored) {
                return 1;
            }
        }
    }

    @ConfigSerializable
    public class Style {
        // Region Style
        @Setting
        private String lineColor = DynmapStyle.DEFAULT_LINE_COLOR;
        @Setting
        private double lineOpacity = DynmapStyle.DEFAULT_LINE_OPACITY;
        @Setting
        private int lineWeight = DynmapStyle.DEFAULT_LINE_WEIGHT;
        @Setting
        private String fillColor = DynmapStyle.DEFAULT_FILL_COLOR;
        @Setting
        private double fillOpacity = DynmapStyle.DEFAULT_FILL_OPACITY;
        @Setting
        private String homeMarker = DynmapStyle.DEFAULT_HOME_MARKER;
        @Setting
        private String warpMarker = DynmapStyle.DEFAULT_WARP_MARKER;
        @Setting
        private boolean styleBoost = DynmapStyle.DEFAULT_BOOST;

        private Style() {
            // Yay
        }

        private Style(String lineColor, String fillColor) {
            this.lineColor = lineColor;
            this.fillColor = fillColor;
        }

        public String getLineColor() {
            return lineColor;
        }

        public double getLineOpacity() {
            return lineOpacity;
        }

        public int getLineWeight() {
            return lineWeight;
        }

        public String getFillColor() {
            return fillColor;
        }

        public double getFillOpacity() {
            return fillOpacity;
        }

        public String getHomeMarker() {
            return homeMarker;
        }

        public String getWarpMarker() {
            return warpMarker;
        }

        public boolean isStyleBoost() {
            return styleBoost;
        }

        @Override
        public String toString() {
            return "Style{" +
                    "lineColor='" + lineColor + '\'' +
                    ", lineOpacity=" + lineOpacity +
                    ", lineWeight=" + lineWeight +
                    ", fillColor='" + fillColor + '\'' +
                    ", fillOpacity=" + fillOpacity +
                    ", homeMarker='" + homeMarker + '\'' +
                    ", warpMarker='" + warpMarker + '\'' +
                    ", styleBoost=" + styleBoost +
                    '}';
        }
    }

    private Dynmap dynmap = new Dynmap();
    private Style style = new Style();

    public Dynmap dynmap() {
        return dynmap;
    }

    public Style style() {
        return style;
    }
}