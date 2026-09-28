package com.credra.smartpantrymanager.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Converts quantities into a base unit so pantry and recipe amounts compare
 * numerically: MASS to grams, VOLUME to millilitres, COUNT to whole items.
 *
 * Different dimensions never match. The app will not guess what "1 bag" of flour
 * weighs, so it reports the recipe as not makeable rather than assuming.
 */
public final class UnitConverter {

    private UnitConverter() {
        // Utility class; not meant to be instantiated.
    }

    public enum Dimension {
        MASS, VOLUME, COUNT, UNKNOWN
    }

    public static final String BASE_MASS = "g";
    public static final String BASE_VOLUME = "ml";
    public static final String BASE_COUNT = "unit";

    /** How many base units one of each supported unit represents. */
    private static final Map<String, Double> FACTORS;

    /** Which dimension each supported unit belongs to. */
    private static final Map<String, Dimension> DIMENSIONS;

    /** Units offered in the Add/Edit spinner, in the order they appear. */
    private static final List<String> SELECTABLE = Collections.unmodifiableList(Arrays.asList(
            "g", "kg", "ml", "l", "tsp", "tbsp", "cup", "unit"
    ));

    static {
        Map<String, Double> f = new HashMap<>();
        Map<String, Dimension> d = new HashMap<>();

        // Mass, base gram.
        f.put("mg", 0.001);      d.put("mg", Dimension.MASS);
        f.put("g", 1.0);         d.put("g", Dimension.MASS);
        f.put("gram", 1.0);      d.put("gram", Dimension.MASS);
        f.put("grams", 1.0);     d.put("grams", Dimension.MASS);
        f.put("kg", 1000.0);     d.put("kg", Dimension.MASS);
        f.put("oz", 28.3495);    d.put("oz", Dimension.MASS);
        f.put("lb", 453.592);    d.put("lb", Dimension.MASS);

        // Volume, base millilitre. A metric cup is 250 ml in South Africa.
        f.put("ml", 1.0);        d.put("ml", Dimension.VOLUME);
        f.put("l", 1000.0);      d.put("l", Dimension.VOLUME);
        f.put("litre", 1000.0);  d.put("litre", Dimension.VOLUME);
        f.put("tsp", 5.0);       d.put("tsp", Dimension.VOLUME);
        f.put("tbsp", 15.0);     d.put("tbsp", Dimension.VOLUME);
        f.put("cup", 250.0);     d.put("cup", Dimension.VOLUME);
        f.put("floz", 29.5735);  d.put("floz", Dimension.VOLUME);

        // Countable things, base whole item.
        f.put("unit", 1.0);      d.put("unit", Dimension.COUNT);
        f.put("units", 1.0);     d.put("units", Dimension.COUNT);
        f.put("piece", 1.0);     d.put("piece", Dimension.COUNT);
        f.put("pieces", 1.0);    d.put("pieces", Dimension.COUNT);
        f.put("clove", 1.0);     d.put("clove", Dimension.COUNT);
        f.put("cloves", 1.0);    d.put("cloves", Dimension.COUNT);
        f.put("slice", 1.0);     d.put("slice", Dimension.COUNT);
        f.put("slices", 1.0);    d.put("slices", Dimension.COUNT);
        f.put("pinch", 1.0);     d.put("pinch", Dimension.COUNT);

        FACTORS = Collections.unmodifiableMap(f);
        DIMENSIONS = Collections.unmodifiableMap(d);
    }

    /** Normalises unit spelling so that "KG", "Kg" and "kg" are all the same. */
    private static String clean(String unit) {
        if (unit == null) {
            return "";
        }
        return unit.trim().toLowerCase(Locale.ROOT);
    }

    public static Dimension dimensionOf(String unit) {
        Dimension dimension = DIMENSIONS.get(clean(unit));
        return dimension == null ? Dimension.UNKNOWN : dimension;
    }

    public static boolean isSupported(String unit) {
        return DIMENSIONS.containsKey(clean(unit));
    }

    /** Unknown units become their own base, so they still match themselves. */
    public static String baseUnitFor(String unit) {
        switch (dimensionOf(unit)) {
            case MASS:
                return BASE_MASS;
            case VOLUME:
                return BASE_VOLUME;
            case COUNT:
                return BASE_COUNT;
            default:
                return clean(unit);
        }
    }

    /** Converts a quantity into the base unit for its dimension. */
    public static double toBase(double quantity, String unit) {
        Double factor = FACTORS.get(clean(unit));
        return factor == null ? quantity : quantity * factor;
    }

    /** Units offered to the user in the Add/Edit Ingredient spinner. */
    public static List<String> selectableUnits() {
        return new ArrayList<>(SELECTABLE);
    }
}
