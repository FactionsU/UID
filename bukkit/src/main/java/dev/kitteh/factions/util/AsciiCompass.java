package dev.kitteh.factions.util;

import dev.kitteh.factions.config.Confs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
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
            return this.getTranslation();
        }

        public String getTranslation() {
            if (this == N) {
                return Confs.tl().commands().map().getCompassLetterNorth();
            }
            if (this == E) {
                return Confs.tl().commands().map().getCompassLetterEast();
            }
            if (this == S) {
                return Confs.tl().commands().map().getCompassLetterSouth();
            }
            if (this == W) {
                return Confs.tl().commands().map().getCompassLetterWest();
            }
            return String.valueOf(this.asciiChar);
        }
    }

    public static List<Component> of(double inDegrees) {
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

        TextColor colorActive = Confs.tl().commands().map().getCompassColorActive();
        TextColor colorDefault = Confs.tl().commands().map().getCompassColorDefault();

        ArrayList<Component> ret = new ArrayList<>();

        ret.add(Component.textOfChildren(
                Component.text(Point.NW.getTranslation()).color(Point.NW == point ? colorActive: colorDefault),
                Component.text(Point.N.getTranslation()).color(Point.N == point ? colorActive: colorDefault),
                Component.text(Point.NE.getTranslation()).color(Point.NE == point ? colorActive: colorDefault)
        ));
        ret.add(Component.textOfChildren(
                Component.text(Point.W.getTranslation()).color(Point.W == point ? colorActive: colorDefault),
                Component.text("+").color(colorDefault),
                Component.text(Point.E.getTranslation()).color(Point.E == point ? colorActive: colorDefault)
        ));
        ret.add(Component.textOfChildren(
                Component.text(Point.SW.getTranslation()).color(Point.SW == point ? colorActive: colorDefault),
                Component.text(Point.S.getTranslation()).color(Point.S == point ? colorActive: colorDefault),
                Component.text(Point.SE.getTranslation()).color(Point.SE == point ? colorActive: colorDefault)
        ));

        return ret;
    }
}
