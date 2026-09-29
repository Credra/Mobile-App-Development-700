package com.credra.smartpantrymanager.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Tests for how a recipe ingredient reads on the detail screen. */
public class RecipeIngredientTest {

    @Test
    public void keepsTheUnitForMeasuredAmounts() {
        assertEquals("400 g spaghetti",
                new RecipeIngredient("spaghetti", 400, "g").describe());
        assertEquals("300 ml milk",
                new RecipeIngredient("milk", 300, "ml").describe());
    }

    @Test
    public void dropsTheUnitLabelForCountedThings() {
        assertEquals("2 eggs", new RecipeIngredient("eggs", 2, "unit").describe());
    }

    @Test
    public void keepsNamedCountUnitsThatReadNaturally() {
        assertEquals("2 cloves garlic",
                new RecipeIngredient("garlic", 2, "cloves").describe());
        assertEquals("4 slices bread",
                new RecipeIngredient("bread", 4, "slices").describe());
    }

    @Test
    public void showsWholeNumbersWithoutADecimalPoint() {
        assertEquals("1 lemon", new RecipeIngredient("lemon", 1, "unit").describe());
        assertEquals("0.5 kg flour",
                new RecipeIngredient("flour", 0.5, "kg").describe());
    }
}
