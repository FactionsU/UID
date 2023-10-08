package com.massivecraft.factions.util;

import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

public class AsciiCompass {
    public enum Point {

        N('N'),
        NE('/'),
        E('E'),
        SE('\\'),
        S('S'),
        SW('/'),
        W('W'),
        NW('\\');

        public final char asciiChar;

        Point(final char asciiChar) {
            this.asciiChar = asciiChar;
        }

        @Override
        public String toString() {
            return String.valueOf(this.asciiChar);
        }

        public String getTranslation() {
            if (this == N) {
                return TL.COMPASS_SHORT_NORTH.toString();
            }
            if (this == E) {
                return TL.COMPASS_SHORT_EAST.toString();
            }
            if (this == S) {
                return TL.COMPASS_SHORT_SOUTH.toString();
            }
            if (this == W) {
                return TL.COMPASS_SHORT_WEST.toString();
            }
            return toString();
        }

        public String toString(boolean isActive, String colorActive, String colorDefault) {
            return (isActive ? colorActive : colorDefault) + getTranslation();
        }
    }

    public static AsciiCompass.Point getCompassPointForDirection(double inDegrees) {
        double degrees = (inDegrees - 180) % 360;
        if (degrees < 0) {
            degrees += 360;
        }

        if (0 <= degrees && degrees < 22.5) {
            return AsciiCompass.Point.N;
        } else if (22.5 <= degrees && degrees < 67.5) {
            return AsciiCompass.Point.NE;
        } else if (67.5 <= degrees && degrees < 112.5) {
            return AsciiCompass.Point.E;
        } else if (112.5 <= degrees && degrees < 157.5) {
            return AsciiCompass.Point.SE;
        } else if (157.5 <= degrees && degrees < 202.5) {
            return AsciiCompass.Point.S;
        } else if (202.5 <= degrees && degrees < 247.5) {
            return AsciiCompass.Point.SW;
        } else if (247.5 <= degrees && degrees < 292.5) {
            return AsciiCompass.Point.W;
        } else if (292.5 <= degrees && degrees < 337.5) {
            return AsciiCompass.Point.NW;
        } else if (337.5 <= degrees && degrees < 360.0) {
            return AsciiCompass.Point.N;
        } else {
            return null;
        }
    }

    public static List<Component> getAsciiCompass(Point point, String colorActive, String colorDefault) {
        ArrayList<Component> ret = new ArrayList<>();
        String row;

        row = "";
        row += Point.NW.toString(Point.NW == point, colorActive, colorDefault);
        row += Point.N.toString(Point.N == point, colorActive, colorDefault);
        row += Point.NE.toString(Point.NE == point, colorActive, colorDefault);
        ret.add(Mini.parse(row.replace("\\","\\\\")));

        row = "";
        row += Point.W.toString(Point.W == point, colorActive, colorDefault);
        row += colorDefault + "+";
        row += Point.E.toString(Point.E == point, colorActive, colorDefault);
        ret.add(Mini.parse(row.replace("\\","\\\\")));

        row = "";
        row += Point.SW.toString(Point.SW == point, colorActive, colorDefault);
        row += Point.S.toString(Point.S == point, colorActive, colorDefault);
        row += Point.SE.toString(Point.SE == point, colorActive, colorDefault);
        ret.add(Mini.parse(row.replace("\\","\\\\")));

        return ret;
    }

    public static List<Component> getAsciiCompass(double inDegrees, String colorActive, String colorDefault) {
        return getAsciiCompass(getCompassPointForDirection(inDegrees), colorActive, colorDefault);
    }
}
