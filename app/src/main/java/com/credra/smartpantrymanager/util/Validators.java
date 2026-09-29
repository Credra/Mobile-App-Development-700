package com.credra.smartpantrymanager.util;

/**
 * Validation rules for the ingredient entry form.
 *
 * Kept free of Android classes so the rules can be unit tested on the JVM.
 * Each method returns null when the value is acceptable, or a message key
 * describing what is wrong.
 */
public final class Validators {

    private Validators() {
    }

    public static final int NAME_MIN_LENGTH = 2;
    public static final int NAME_MAX_LENGTH = 40;
    public static final double QUANTITY_MAX = 9999;

    public enum NameError {
        EMPTY, TOO_SHORT, TOO_LONG, NO_LETTERS
    }

    public enum QuantityError {
        EMPTY, NOT_A_NUMBER, NOT_POSITIVE, TOO_LARGE
    }

    /** @return the problem with the name, or null if it is acceptable. */
    public static NameError checkName(String raw) {
        String name = raw == null ? "" : raw.trim();
        if (name.isEmpty()) {
            return NameError.EMPTY;
        }
        if (name.length() < NAME_MIN_LENGTH) {
            return NameError.TOO_SHORT;
        }
        if (name.length() > NAME_MAX_LENGTH) {
            return NameError.TOO_LONG;
        }
        // A name of only digits or punctuation cannot be normalised into
        // anything the matcher can use.
        if (!name.matches(".*[A-Za-z].*")) {
            return NameError.NO_LETTERS;
        }
        return null;
    }

    /** @return the problem with the quantity, or null if it is acceptable. */
    public static QuantityError checkQuantity(String raw) {
        String quantity = raw == null ? "" : raw.trim();
        if (quantity.isEmpty()) {
            return QuantityError.EMPTY;
        }

        double value;
        try {
            value = Double.parseDouble(quantity);
        } catch (NumberFormatException e) {
            return QuantityError.NOT_A_NUMBER;
        }

        if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0) {
            return QuantityError.NOT_POSITIVE;
        }
        if (value > QUANTITY_MAX) {
            return QuantityError.TOO_LARGE;
        }
        return null;
    }

    /**
     * Expiry is optional, but a date already in the past is almost always a
     * mis-tap on the date picker rather than a deliberate entry.
     *
     * @param expiry     the chosen date, or null if none was set
     * @param startOfDay midnight today, in the same epoch millis
     */
    public static boolean isExpiryInThePast(Long expiry, long startOfDay) {
        return expiry != null && expiry < startOfDay;
    }
}
