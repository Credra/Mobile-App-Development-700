package com.credra.smartpantrymanager.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Tests for unit conversion, which lets amounts in different units be compared. */
public class UnitConverterTest {

    private static final double DELTA = 0.0001;

    @Test
    public void convertsMassToGrams() {
        assertEquals(1000.0, UnitConverter.toBase(1, "kg"), DELTA);
        assertEquals(500.0, UnitConverter.toBase(500, "g"), DELTA);
        assertEquals(0.5, UnitConverter.toBase(500, "mg"), DELTA);
    }

    @Test
    public void convertsVolumeToMillilitres() {
        assertEquals(2000.0, UnitConverter.toBase(2, "l"), DELTA);
        assertEquals(15.0, UnitConverter.toBase(1, "tbsp"), DELTA);
        assertEquals(5.0, UnitConverter.toBase(1, "tsp"), DELTA);
        assertEquals(250.0, UnitConverter.toBase(1, "cup"), DELTA);
    }

    @Test
    public void treatsCountableThingsAsWholeUnits() {
        assertEquals(3.0, UnitConverter.toBase(3, "unit"), DELTA);
        assertEquals(2.0, UnitConverter.toBase(2, "cloves"), DELTA);
        assertEquals(4.0, UnitConverter.toBase(4, "slices"), DELTA);
    }

    @Test
    public void unitSpellingAndCaseDoNotMatter() {
        assertEquals("g", UnitConverter.baseUnitFor("KG"));
        assertEquals("g", UnitConverter.baseUnitFor(" kg "));
        assertEquals("unit", UnitConverter.baseUnitFor("clove"));
        assertEquals("unit", UnitConverter.baseUnitFor("cloves"));
    }

    @Test
    public void groupsUnitsIntoTheCorrectDimension() {
        assertEquals(UnitConverter.Dimension.MASS, UnitConverter.dimensionOf("kg"));
        assertEquals(UnitConverter.Dimension.VOLUME, UnitConverter.dimensionOf("ml"));
        assertEquals(UnitConverter.Dimension.COUNT, UnitConverter.dimensionOf("unit"));
        assertEquals(UnitConverter.Dimension.UNKNOWN, UnitConverter.dimensionOf("handful"));
    }

    @Test
    public void massAndVolumeNeverShareABaseUnit() {
        // This is what stops 200 ml of milk satisfying a recipe asking for 200 g.
        assertFalse(UnitConverter.baseUnitFor("ml").equals(UnitConverter.baseUnitFor("g")));
    }

    @Test
    public void unknownUnitsAreLeftAloneRatherThanGuessed() {
        assertEquals(2.0, UnitConverter.toBase(2, "handful"), DELTA);
        assertEquals("handful", UnitConverter.baseUnitFor("handful"));
        assertFalse(UnitConverter.isSupported("handful"));
        assertTrue(UnitConverter.isSupported("kg"));
    }

    @Test
    public void spinnerOffersTheExpectedUnits() {
        assertTrue(UnitConverter.selectableUnits().contains("g"));
        assertTrue(UnitConverter.selectableUnits().contains("ml"));
        assertTrue(UnitConverter.selectableUnits().contains("unit"));
    }
}
