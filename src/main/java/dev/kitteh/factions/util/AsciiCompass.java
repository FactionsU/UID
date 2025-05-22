package dev.kitteh.factions.util;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public class AsciiCompass {
    private enum Point {

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

    public static List<Component> of(double inDegrees, String colorActive, String colorDefault) {
        double degrees = (inDegrees - 180) % 360;
        if (degrees < 0) {
            degrees += 360;
        }

        Point point;

        if (0 <= degrees && degrees < 22.5) {
            point = AsciiCompass.Point.N;
        } else if (22.5 <= degrees && degrees < 67.5) {
            point = AsciiCompass.Point.NE;
        } else if (67.5 <= degrees && degrees < 112.5) {
            point = AsciiCompass.Point.E;
        } else if (112.5 <= degrees && degrees < 157.5) {
            point = AsciiCompass.Point.SE;
        } else if (157.5 <= degrees && degrees < 202.5) {
            point = AsciiCompass.Point.S;
        } else if (202.5 <= degrees && degrees < 247.5) {
            point = AsciiCompass.Point.SW;
        } else if (247.5 <= degrees && degrees < 292.5) {
            point = AsciiCompass.Point.W;
        } else if (292.5 <= degrees && degrees < 337.5) {
            point = AsciiCompass.Point.NW;
        } else if (337.5 <= degrees && degrees < 360.0) {
            point = AsciiCompass.Point.N;
        } else {
            point = AsciiCompass.Point.N; // yolo
        }


        ArrayList<Component> ret = new ArrayList<>();
        String row;

        row = "";
        row += Point.NW.toString(Point.NW == point, colorActive, colorDefault);
        row += Point.N.toString(Point.N == point, colorActive, colorDefault);
        row += Point.NE.toString(Point.NE == point, colorActive, colorDefault);
        ret.add(Mini.parse(row.replace("\\", "\\\\")));

        row = "";
        row += Point.W.toString(Point.W == point, colorActive, colorDefault);
        row += colorDefault + "+";
        row += Point.E.toString(Point.E == point, colorActive, colorDefault);
        ret.add(Mini.parse(row.replace("\\", "\\\\")));

        row = "";
        row += Point.SW.toString(Point.SW == point, colorActive, colorDefault);
        row += Point.S.toString(Point.S == point, colorActive, colorDefault);
        row += Point.SE.toString(Point.SE == point, colorActive, colorDefault);
        ret.add(Mini.parse(row.replace("\\", "\\\\")));

        return ret;
    }
}
