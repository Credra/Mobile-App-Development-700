package com.credra.smartpantrymanager.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Wraps SharedPreferences so the rest of the app never deals with keys or
 * defaults directly, and a mistyped key cannot silently read the wrong value.
 */
public final class Prefs {

    private static final String FILE = "smart_pantry_prefs";

    private static final String KEY_HIGHLIGHT_EXPIRING = "highlight_expiring";
    private static final String KEY_DEFAULT_UNIT = "default_unit";

    /** An item is "expiring soon" when it is due within this many days. */
    public static final int EXPIRING_SOON_DAYS = 3;

    private static final boolean DEFAULT_HIGHLIGHT_EXPIRING = true;
    private static final String DEFAULT_UNIT = "g";

    private Prefs() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static boolean isHighlightExpiring(Context context) {
        return prefs(context).getBoolean(KEY_HIGHLIGHT_EXPIRING, DEFAULT_HIGHLIGHT_EXPIRING);
    }

    public static void setHighlightExpiring(Context context, boolean highlight) {
        prefs(context).edit().putBoolean(KEY_HIGHLIGHT_EXPIRING, highlight).apply();
    }

    /** The unit pre-selected when adding a new ingredient. */
    public static String getDefaultUnit(Context context) {
        return prefs(context).getString(KEY_DEFAULT_UNIT, DEFAULT_UNIT);
    }

    public static void setDefaultUnit(Context context, String unit) {
        prefs(context).edit().putString(KEY_DEFAULT_UNIT, unit).apply();
    }
}
