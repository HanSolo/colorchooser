/*
 * Copyright (c) 2018 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.fx.colorchooser.tool;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Helper {
    public  static final double  MIN_FONT_SIZE = 5;
    public  static final double  HALF_PI       = Math.PI * 0.5;
    public  static final double  TWO_PI        = Math.PI + Math.PI;
    public  static final double  THREE_PI      = TWO_PI + Math.PI;
    private static final double  EPSILON       = 1E-6;
    private static final Pattern FLOAT_PATTERN = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+");
    private static final Matcher FLOAT_MATCHER = FLOAT_PATTERN.matcher("");
    private static final Pattern HEX_PATTERN   = Pattern.compile("#?([A-Fa-f0-9]{8}|[A-Fa-f0-9]{6})");
    private static final Matcher HEX_MATCHER   = HEX_PATTERN.matcher("");

    public static final <T extends Number> T clamp(final T min, final T max, final T value) {
        if (value.doubleValue() < min.doubleValue()) return min;
        if (value.doubleValue() > max.doubleValue()) return max;
        return value;
    }

    public static final int clamp(final int min, final int max, final int value) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
    public static final long clamp(final long min, final long max, final long value) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
    public static final double clamp(final double min, final double max, final double value) {
        if (Double.compare(value, min) < 0) return min;
        if (Double.compare(value, max) > 0) return max;
        return value;
    }

    public static final double clampMin(final double min, final double value) {
        if (value < min) return min;
        return value;
    }
    public static final double clampMax(final double max, final double value) {
        if (value > max) return max;
        return value;
    }

    public static final double round(final double value, final int precision) {
        final int SCALE = (int) Math.pow(10, precision);
        return (double) Math.round(value * SCALE) / SCALE;
    }

    public static final double roundTo(final double value, final double target) { return target * (Math.round(value / target)); }

    public static final double roundToHalf(final double value) { return Math.round(value * 2) / 2.0; }

    public static final double nearest(final double smaller, final double value, final double larger) {
        return (value - smaller) < (larger - value) ? smaller : larger;
    }

    public static int roundDoubleToInt(final double value){
        double dAbs = Math.abs(value);
        int    i      = (int) dAbs;
        double result = dAbs - (double) i;
        if (result < 0.5) {
            return value < 0 ? -i : i;
        } else {
            return value < 0 ? -(i + 1) : i + 1;
        }
    }

    public static final double[] calcAutoScale(final double minValue, final double maxValue) {
        double maxNoOfMajorTicks = 10;
        double maxNoOfMinorTicks = 10;
        double niceMinValue;
        double niceMaxValue;
        double niceRange;
        double majorTickSpace;
        double minorTickSpace;
        niceRange      = (calcNiceNumber((maxValue - minValue), false));
        majorTickSpace = calcNiceNumber(niceRange / (maxNoOfMajorTicks - 1), true);
        niceMinValue   = (Math.floor(minValue / majorTickSpace) * majorTickSpace);
        niceMaxValue   = (Math.ceil(maxValue / majorTickSpace) * majorTickSpace);
        minorTickSpace = calcNiceNumber(majorTickSpace / (maxNoOfMinorTicks - 1), true);
        return new double[]{ niceMinValue, niceMaxValue, majorTickSpace, minorTickSpace };
    }

    /**
     * Can be used to implement discrete steps e.g. on a slider.
     * @param minValue          The min value of the range
     * @param maxValue          The max value of the range
     * @param value             The value to snap
     * @param newMinorTickCount The number of ticks between 2 major tick marks
     * @param newMajorTickUnit  The distance between 2 major tick marks
     * @return The value snapped to the next tick mark defined by the given parameters
     */
    public static double snapToTicks(final double minValue, final double maxValue, final double value, final int newMinorTickCount, final double newMajorTickUnit) {
        double v = value;
        int    minorTickCount = clamp(0, 10, newMinorTickCount);
        double majorTickUnit  = Double.compare(newMajorTickUnit, 0.0) <= 0 ? 0.25 : newMajorTickUnit;
        double tickSpacing;

        if (minorTickCount != 0) {
            tickSpacing = majorTickUnit / (Math.max(minorTickCount, 0) + 1);
        } else {
            tickSpacing = majorTickUnit;
        }

        int    prevTick      = (int) ((v - minValue) / tickSpacing);
        double prevTickValue = prevTick * tickSpacing + minValue;
        double nextTickValue = (prevTick + 1) * tickSpacing + minValue;

        v = nearest(prevTickValue, v, nextTickValue);

        return clamp(minValue, maxValue, v);
    }

    /**
     * Returns a "niceScaling" number approximately equal to the range.
     * Rounds the number if ROUND == true.
     * Takes the ceiling if ROUND = false.
     *
     * @param range the value range (maxValue - minValue)
     * @param round whether to round the result or ceil
     * @return a "niceScaling" number to be used for the value range
     */
    public static final double calcNiceNumber(final double range, final boolean round) {
        double niceFraction;
        double exponent = Math.floor(Math.log10(range));   // exponent of range
        double fraction = range / Math.pow(10, exponent);  // fractional part of range

        if (round) {
            if (Double.compare(fraction, 1.5) < 0) {
                niceFraction = 1;
            } else if (Double.compare(fraction, 3)  < 0) {
                niceFraction = 2;
            } else if (Double.compare(fraction, 7) < 0) {
                niceFraction = 5;
            } else {
                niceFraction = 10;
            }
        } else {
            if (Double.compare(fraction, 1) <= 0) {
                niceFraction = 1;
            } else if (Double.compare(fraction, 2) <= 0) {
                niceFraction = 2;
            } else if (Double.compare(fraction, 5) <= 0) {
                niceFraction = 5;
            } else {
                niceFraction = 10;
            }
        }
        return niceFraction * Math.pow(10, exponent);
    }

    public static final double[] toHSL(final Color COLOR) {
        return rgbToHSL(COLOR.getRed(), COLOR.getGreen(), COLOR.getBlue());
    }
    public static final double[] rgbToHSL(final double RED, final double GREEN, final double BLUE) {
        //	Minimum and Maximum RGB values are used in the HSL calculations
        double min = Math.min(RED, Math.min(GREEN, BLUE));
        double max = Math.max(RED, Math.max(GREEN, BLUE));

        //  Calculate the Hue
        double hue = 0;

        if (max == min) {
            hue = 0;
        } else if (max == RED) {
            hue = ((60 * (GREEN - BLUE) / (max - min)) + 360) % 360;
        } else if (max == GREEN) {
            hue = (60 * (BLUE - RED) / (max - min)) + 120;
        } else if (max == BLUE) {
            hue = (60 * (RED - GREEN) / (max - min)) + 240;
        }

        //  Calculate the Luminance
        double luminance = (max + min) / 2;

        //  Calculate the Saturation
        double saturation = 0;
        if (Double.compare(max, min)  == 0) {
            saturation = 0;
        } else if (luminance <= .5) {
            saturation = (max - min) / (max + min);
        } else {
            saturation = (max - min) / (2 - max - min);
        }

        return new double[] { hue, saturation, luminance};
    }

    public static final Color hslToRGB(double hue, double saturation, double luminance) {
        return hslToRGB(hue, saturation, luminance, 1);
    }
    public static Color hslToRGB(double hue, double saturation, double luminance, double opacity) {
        saturation = clamp(0, 1, saturation);
        luminance  = clamp(0, 1, luminance);
        opacity    = clamp(0, 1, opacity);

        hue = hue % 360.0;
        hue /= 360;

        double q = luminance < 0.5 ? luminance * (1 + saturation) : (luminance + saturation) - (saturation * luminance);
        double p = 2 * luminance - q;

        double r = clamp(0, 1, hueToRGB(p, q, hue + (1.0/3.0)));
        double g = clamp(0, 1, hueToRGB(p, q, hue));
        double b = clamp(0, 1, hueToRGB(p, q, hue - (1.0/3.0)));

        return Color.color(r, g, b, opacity);
    }
    private static final double hueToRGB(double p, double q, double t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (6 * t < 1) { return p + ((q - p) * 6 * t); }
        if (2 * t < 1) { return q; }
        if (3 * t < 2) { return p + ((q - p) * 6 * ((2.0/3.0) - t)); }
        return p;
    }

    public static final String colorToRGB(final Color COLOR) {
        String hex      = COLOR.toString().replace("0x", "");
        String hexRed   = hex.substring(0, 2).toUpperCase();
        String hexGreen = hex.substring(2, 4).toUpperCase();
        String hexBlue  = hex.substring(4, 6).toUpperCase();

        String intRed   = Integer.toString(Integer.parseInt(hexRed, 16));
        String intGreen = Integer.toString(Integer.parseInt(hexGreen, 16));
        String intBlue  = Integer.toString(Integer.parseInt(hexBlue, 16));

        return String.join("", "colorToRGB(", intRed, ", ", intGreen, ", ", intBlue, ")");
    }

    public static final String colorToRGBA(final Color COLOR) { return colorToRGBA(COLOR, COLOR.getOpacity()); }
    public static final String colorToRGBA(final Color COLOR, final double ALPHA) {
        String hex      = COLOR.toString().replace("0x", "");
        String hexRed   = hex.substring(0, 2).toUpperCase();
        String hexGreen = hex.substring(2, 4).toUpperCase();
        String hexBlue  = hex.substring(4, 6).toUpperCase();

        String intRed   = Integer.toString(Integer.parseInt(hexRed, 16));
        String intGreen = Integer.toString(Integer.parseInt(hexGreen, 16));
        String intBlue  = Integer.toString(Integer.parseInt(hexBlue, 16));
        String alpha    = String.format(Locale.US, "%.3f", clamp(0, 1, ALPHA));

        return String.join("", "colorToRGBA(", intRed, ", ", intGreen, ", ", intBlue, ",", alpha, ")");
    }

    public static final String colorToWeb(final Color COLOR) { return COLOR.toString().replace("0x", "#").substring(0, 7); }

    public static final void adjustTextSize(final Text text, final double maxWidth, final double fontSize) {
        final String FONT_NAME          = text.getFont().getName();
        double       adjustableFontSize = fontSize;

        while (text.getBoundsInLocal().getWidth() > maxWidth && adjustableFontSize > MIN_FONT_SIZE) {
            adjustableFontSize -= 0.05;
            text.setFont(new Font(FONT_NAME, adjustableFontSize));
        }
    }
    public static final void adjustTextSize(final Label text, final double maxWidth, final double fontSize) {
        final String FONT_NAME          = text.getFont().getName();
        double       adjustableFontSize = fontSize;

        while (text.getBoundsInLocal().getWidth() > maxWidth && adjustableFontSize > MIN_FONT_SIZE) {
            adjustableFontSize -= 0.05;
            text.setFont(new Font(FONT_NAME, adjustableFontSize));
        }
    }

    public static final void fitNodeWidth(final Node node, final double maxWidth) {
        node.setVisible(node.getLayoutBounds().getWidth() < maxWidth);
        //enableNode(NODE, NODE.getLayoutBounds().getWidth() < MAX_WIDTH);
    }

    public static final DateTimeFormatter getDateFormat(final Locale locale) {
        if (Locale.US == locale) {
            return DateTimeFormatter.ofPattern("MM/dd/YYYY");
        } else if (Locale.CHINA == locale) {
            return DateTimeFormatter.ofPattern("YYYY.MM.dd");
        } else {
            return DateTimeFormatter.ofPattern("dd.MM.YYYY");
        }
    }
    public static final DateTimeFormatter getLocalizedDateFormat(final Locale locale) {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale);
    }

    public static final void enableNode(final Node node, final boolean enable) {
        node.setManaged(enable);
        node.setVisible(enable);
    }

    public static final String colorToCss(final Color color) {
        return color.toString().replace("0x", "#");
    }

    public static final ThreadFactory getThreadFactory(final String threadName, final boolean isDaemon) {
        return runnable -> {
            Thread thread = new Thread(runnable, threadName);
            thread.setDaemon(isDaemon);
            return thread;
        };
    }

    public static final void stopTask(ScheduledFuture<?> task) {
        if (null == task) return;
        task.cancel(true);
        task = null;
    }

    public static final boolean isMonochrome(final Color color) {
        return Double.compare(color.getRed(), color.getGreen()) == 0 && Double.compare(color.getGreen(), color.getBlue()) == 0;
    }

    public static final double colorDistance(final Color color1, final Color color2) {
        final double DELTA_R = (color2.getRed()   - color1.getRed());
        final double DELTA_G = (color2.getGreen() - color1.getGreen());
        final double DELTA_B = (color2.getBlue()  - color1.getBlue());

        return Math.sqrt(DELTA_R * DELTA_R + DELTA_G * DELTA_G + DELTA_B * DELTA_B);
    }

    public static double[] colorToYUV(final Color color) {
        final double WEIGHT_FACTOR_RED   = 0.299;
        final double WEIGHT_FACTOR_GREEN = 0.587;
        final double WEIGHT_FACTOR_BLUE  = 0.144;
        final double U_MAX               = 0.436;
        final double V_MAX               = 0.615;
        double y = clamp(0, 1, WEIGHT_FACTOR_RED * color.getRed() + WEIGHT_FACTOR_GREEN * color.getGreen() + WEIGHT_FACTOR_BLUE * color.getBlue());
        double u = clamp(-U_MAX, U_MAX, U_MAX * ((color.getBlue() - y) / (1 - WEIGHT_FACTOR_BLUE)));
        double v = clamp(-V_MAX, V_MAX, V_MAX * ((color.getRed() - y) / (1 - WEIGHT_FACTOR_RED)));
        return new double[] { y, u, v };
    }

    public static final boolean isBright(final Color color) { return Double.compare(colorToYUV(color)[0], 0.5) >= 0.0; }
    public static final boolean isDark(final Color color) { return colorToYUV(color)[0] < 0.5; }

    public static final Color getContrastColor(final Color color) {
        return color.getBrightness() > 0.5 ? Color.BLACK : Color.WHITE;
    }

    public static final Color getColorWithOpacity(final Color color, final double opacity) {
        return Color.color(color.getRed(), color.getGreen(), color.getBlue(), clamp(0.0, 1.0, opacity));
    }

    public static final List<Color> createColorPalette(final Color fromColor, final Color toColor, final int noOfColors) {
        int    steps        = clamp(1, 12, noOfColors) - 1;
        double step         = 1.0 / steps;
        double deltaRed     = (toColor.getRed()     - fromColor.getRed())     * step;
        double deltaGreen   = (toColor.getGreen()   - fromColor.getGreen())   * step;
        double deltaBlue    = (toColor.getBlue()    - fromColor.getBlue())    * step;
        double deltaOpacity = (toColor.getOpacity() - fromColor.getOpacity()) * step;

        List<Color> palette      = new ArrayList<>(noOfColors);
        Color       currentColor = fromColor;
        palette.add(currentColor);
        for (int i = 0 ; i < steps ; i++) {
            double red     = clamp(0d, 1d, (currentColor.getRed()     + deltaRed));
            double green   = clamp(0d, 1d, (currentColor.getGreen()   + deltaGreen));
            double blue    = clamp(0d, 1d, (currentColor.getBlue()    + deltaBlue));
            double opacity = clamp(0d, 1d, (currentColor.getOpacity() + deltaOpacity));
            currentColor   = Color.color(red, green, blue, opacity);
            palette.add(currentColor);
        }
        return palette;
    }

    public static final Color[] createColorVariations(final Color color, final int newNoOfColors) {
        int    noOfColors = clamp(1, 12, newNoOfColors);
        double step       = 0.8 / noOfColors;
        double hue        = color.getHue();
        double brg        = color.getBrightness();
        Color[] colors = new Color[noOfColors];
        for (int i = 0 ; i < noOfColors ; i++) { colors[i] = Color.hsb(hue, 0.2 + i * step, brg); }
        return colors;
    }

    public static final Color getColorAt(final List<Stop> stopList, final double positionOfColor) {
        Map<Double, Stop> STOPS = new TreeMap<>();
        for (Stop stop : stopList) { STOPS.put(stop.getOffset(), stop); }

        if (STOPS.isEmpty()) return Color.BLACK;

        double minFraction = Collections.min(STOPS.keySet());
        double maxFraction = Collections.max(STOPS.keySet());

        if (Double.compare(minFraction, 0d) > 0) { STOPS.put(0.0, new Stop(0.0, STOPS.get(minFraction).getColor())); }
        if (Double.compare(maxFraction, 1d) < 0) { STOPS.put(1.0, new Stop(1.0, STOPS.get(maxFraction).getColor())); }

        final double POSITION = clamp(0d, 1d, positionOfColor);
        final Color COLOR;
        if (STOPS.size() == 1) {
            final Map<Double, Color> ONE_ENTRY = (Map<Double, Color>) STOPS.entrySet().iterator().next();
            COLOR = STOPS.get(ONE_ENTRY.keySet().iterator().next()).getColor();
        } else {
            Stop lowerBound = STOPS.get(0.0);
            Stop upperBound = STOPS.get(1.0);
            for (Double fraction : STOPS.keySet()) {
                if (Double.compare(fraction,POSITION) < 0) {
                    lowerBound = STOPS.get(fraction);
                }
                if (Double.compare(fraction, POSITION) > 0) {
                    upperBound = STOPS.get(fraction);
                    break;
                }
            }
            COLOR = interpolateColor(lowerBound, upperBound, POSITION);
        }
        return COLOR;
    }
    public static final Color interpolateColor(final Stop lowerBound, final Stop upperBound, final double position) {
        final double POS  = (position - lowerBound.getOffset()) / (upperBound.getOffset() - lowerBound.getOffset());

        final double DELTA_RED     = (upperBound.getColor().getRed()     - lowerBound.getColor().getRed())     * POS;
        final double DELTA_GREEN   = (upperBound.getColor().getGreen()   - lowerBound.getColor().getGreen())   * POS;
        final double DELTA_BLUE    = (upperBound.getColor().getBlue()    - lowerBound.getColor().getBlue())    * POS;
        final double DELTA_OPACITY = (upperBound.getColor().getOpacity() - lowerBound.getColor().getOpacity()) * POS;

        double red     = clamp(0, 1, (lowerBound.getColor().getRed()     + DELTA_RED));
        double green   = clamp(0, 1, (lowerBound.getColor().getGreen()   + DELTA_GREEN));
        double blue    = clamp(0, 1, (lowerBound.getColor().getBlue()    + DELTA_BLUE));
        double opacity = clamp(0, 1, (lowerBound.getColor().getOpacity() + DELTA_OPACITY));

        return Color.color(red, green, blue, opacity);
    }

    public static final void scaleNodeTo(final Node node, final double targetWidth, final double targetHeight) {
        node.setScaleX(targetWidth / node.getLayoutBounds().getWidth());
        node.setScaleY(targetHeight / node.getLayoutBounds().getHeight());
    }

    public static final String normalize(final String text) {
        String normalized = text.replaceAll("\u00fc", "ue")
                                .replaceAll("\u00f6", "oe")
                                .replaceAll("\u00e4", "ae")
                                .replaceAll("\u00df", "ss");

        normalized = normalized.replaceAll("\u00dc(?=[a-z\u00fc\u00f6\u00e4\u00df ])", "Ue")
                               .replaceAll("\u00d6(?=[a-z\u00fc\u00f6\u00e4\u00df ])", "Oe")
                               .replaceAll("\u00c4(?=[a-z\u00fc\u00f6\u00e4\u00df ])", "Ae");

        normalized = normalized.replaceAll("\u00dc", "UE")
                               .replaceAll("\u00d6", "OE")
                               .replaceAll("\u00c4", "AE");
        return normalized;
    }

    public static final boolean equals(final double a, final double b) { return a == b || Math.abs(a - b) < EPSILON; }
    public static final boolean biggerThan(final double a, final double b) { return (a - b) > EPSILON; }
    public static final boolean lessThan(final double a, final double b) { return (b - a) > EPSILON; }

    public static final Properties readProperties(final String fileName) {
        final ClassLoader LOADER     = Thread.currentThread().getContextClassLoader();
        final Properties  PROPERTIES = new Properties();
        try(InputStream resourceStream = LOADER.getResourceAsStream(fileName)) {
            PROPERTIES.load(resourceStream);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return PROPERTIES;
    }

    public static final <T> Predicate<T> not(Predicate<T> predicate) { return predicate.negate(); }

    public static final double getNumberFromText(final String text) {
        FLOAT_MATCHER.reset(text);
        String result = "";
        try {
            while (FLOAT_MATCHER.find()) {
                result = FLOAT_MATCHER.group(0);
            }
        } catch (IllegalStateException ex) {
            return 0;
        }
        return Double.parseDouble(result);
    }

    public static final String getHexColorFromString(final String text) {
        HEX_MATCHER.reset(text);
        String result = "";
        try {
            while (HEX_MATCHER.find()) {
                result = HEX_MATCHER.group(0);
            }
        } catch (IllegalStateException ex) {
            return "-";
        }
        return result;
    }
}
