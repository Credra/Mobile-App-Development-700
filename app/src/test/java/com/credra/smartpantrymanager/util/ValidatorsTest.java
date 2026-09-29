package com.credra.smartpantrymanager.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Tests for the ingredient entry form rules. */
public class ValidatorsTest {

    @Test
    public void acceptsAReasonableName() {
        assertNull(Validators.checkName("Tomatoes"));
        assertNull(Validators.checkName("Olive Oil"));
        assertNull(Validators.checkName("  Basil  "));
    }

    @Test
    public void rejectsEmptyOrWhitespaceName() {
        assertEquals(Validators.NameError.EMPTY, Validators.checkName(""));
        assertEquals(Validators.NameError.EMPTY, Validators.checkName("   "));
        assertEquals(Validators.NameError.EMPTY, Validators.checkName(null));
    }

    @Test
    public void rejectsNameThatIsTooShortOrTooLong() {
        assertEquals(Validators.NameError.TOO_SHORT, Validators.checkName("a"));
        assertEquals(Validators.NameError.TOO_LONG,
                Validators.checkName(new String(new char[41]).replace('\0', 'a')));
    }

    @Test
    public void rejectsNameWithNoLetters() {
        assertEquals(Validators.NameError.NO_LETTERS, Validators.checkName("123"));
        assertEquals(Validators.NameError.NO_LETTERS, Validators.checkName("!!!!"));
    }

    @Test
    public void acceptsAReasonableQuantity() {
        assertNull(Validators.checkQuantity("1"));
        assertNull(Validators.checkQuantity("0.5"));
        assertNull(Validators.checkQuantity("9999"));
    }

    @Test
    public void rejectsEmptyQuantity() {
        // This is the case that crashed the form before validation existed.
        assertEquals(Validators.QuantityError.EMPTY, Validators.checkQuantity(""));
        assertEquals(Validators.QuantityError.EMPTY, Validators.checkQuantity(null));
    }

    @Test
    public void rejectsQuantityThatIsNotANumber() {
        assertEquals(Validators.QuantityError.NOT_A_NUMBER, Validators.checkQuantity("abc"));
        assertEquals(Validators.QuantityError.NOT_A_NUMBER, Validators.checkQuantity("1.2.3"));
    }

    @Test
    public void rejectsZeroAndNegativeQuantities() {
        assertEquals(Validators.QuantityError.NOT_POSITIVE, Validators.checkQuantity("0"));
        assertEquals(Validators.QuantityError.NOT_POSITIVE, Validators.checkQuantity("-5"));
    }

    @Test
    public void rejectsQuantityAboveTheLimit() {
        assertEquals(Validators.QuantityError.TOO_LARGE, Validators.checkQuantity("10000"));
    }

    @Test
    public void treatsExpiryAsOptional() {
        assertFalse(Validators.isExpiryInThePast(null, 1_000_000L));
    }

    @Test
    public void rejectsAnExpiryBeforeToday() {
        long startOfToday = 1_000_000L;
        assertTrue(Validators.isExpiryInThePast(startOfToday - 1, startOfToday));
        assertFalse(Validators.isExpiryInThePast(startOfToday, startOfToday));
        assertFalse(Validators.isExpiryInThePast(startOfToday + 1, startOfToday));
    }
}
