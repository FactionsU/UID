package com.massivecraft.factions.integration.dynmap;

import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.config.file.DynmapConfig;

public class DynmapStyle {
    public static final String DEFAULT_LINE_COLOR = "#00FF00";
    public static final double DEFAULT_LINE_OPACITY = 0.8D;
    public static final int DEFAULT_LINE_WEIGHT = 3;
    public static final String DEFAULT_FILL_COLOR = "#00FF00";
    public static final double DEFAULT_FILL_OPACITY = 0.35D;
    public static final String DEFAULT_HOME_MARKER = "greenflag";
    public static final boolean DEFAULT_BOOST = false;

    private static DynmapStyle defaultStyle = new DynmapStyle()
            .setLineColor(DEFAULT_LINE_COLOR)
            .setLineOpacity(DEFAULT_LINE_OPACITY)
            .setLineWeight(DEFAULT_LINE_WEIGHT)
            .setFillColor(DEFAULT_FILL_COLOR)
            .setFillOpacity(DEFAULT_FILL_OPACITY)
            .setHomeMarker(DEFAULT_HOME_MARKER)
            .setBoost(DEFAULT_BOOST);

    public static DynmapStyle getDefault() {
        return defaultStyle;
    }

    private static DynmapConfig.Style styleConf() {
        return FactionsPlugin.getInstance().getConfigManager().getDynmapConfig().style();
    }

    // -------------------------------------------- //
    // FIELDS
    // -------------------------------------------- //
    private String lineColor = null;

    public int getLineColor() {
        return getColor(coalesce(this.lineColor, styleConf().getLineColor(), DEFAULT_FILL_COLOR));
    }

    public DynmapStyle setLineColor(String strokeColor) {
        this.lineColor = strokeColor;
        return this;
    }

    private Double lineOpacity = null;

    public double getLineOpacity() {
        return coalesce(this.lineOpacity, styleConf().getLineOpacity(), DEFAULT_LINE_OPACITY);
    }

    public DynmapStyle setLineOpacity(Double strokeOpacity) {
        this.lineOpacity = strokeOpacity;
        return this;
    }

    private Integer lineWeight = null;

    public int getLineWeight() {
        return coalesce(this.lineWeight, styleConf().getLineWeight(), DEFAULT_LINE_WEIGHT);
    }

    public DynmapStyle setLineWeight(Integer strokeWeight) {
        this.lineWeight = strokeWeight;
        return this;
    }

    private String fillColor = null;

    public int getFillColor() {
        return getColor(coalesce(this.fillColor, styleConf().getFillColor(), DEFAULT_FILL_COLOR));
    }

    public DynmapStyle setFillColor(String fillColor) {
        this.fillColor = fillColor;
        return this;
    }

    private Double fillOpacity = null;

    public double getFillOpacity() {
        return coalesce(this.fillOpacity, styleConf().getFillOpacity(), DEFAULT_FILL_OPACITY);
    }

    public DynmapStyle setFillOpacity(Double fillOpacity) {
        this.fillOpacity = fillOpacity;
        return this;
    }

    private String homeMarker = null;

    public String getHomeMarker() {
        return coalesce(this.homeMarker, styleConf().getHomeMarker(), DEFAULT_HOME_MARKER);
    }

    public DynmapStyle setHomeMarker(String homeMarker) {
        this.homeMarker = homeMarker;
        return this;
    }

    private Boolean boost = null;

    public boolean getBoost() {
        return coalesce(this.boost, styleConf().isStyleBoost(), DEFAULT_BOOST);
    }

    public DynmapStyle setBoost(Boolean boost) {
        this.boost = boost;
        return this;
    }

    // -------------------------------------------- //
    // UTIL
    // -------------------------------------------- //

    private static <T> T coalesce(T first, T second, T defaultItem) {
        return first != null ? first : (second != null ? second : defaultItem);
    }

    public static int getColor(String string) {
        int ret = 0x00FF00;
        try {
            ret = Integer.parseInt(string.substring(1), 16);
        } catch (NumberFormatException ignored) {
        }
        return ret;
    }
}
