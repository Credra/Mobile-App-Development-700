package com.credra.smartpantrymanager.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.credra.smartpantrymanager.model.MatchResult;
import com.credra.smartpantrymanager.model.PantryItem;
import com.credra.smartpantrymanager.model.Recipe;
import com.credra.smartpantrymanager.model.RecipeIngredient;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tests for the strict-matching rule: a recipe is suggested only when the pantry
 * holds every ingredient it needs, in at least the required quantity.
 */
public class RecipeMatcherTest {

    private Recipe pasta;
    private List<Recipe> recipes;

    /** Builds a pantry item with its canonical and base values filled in. */
    private static PantryItem pantryItem(String name, double quantity, String unit) {
        PantryItem item = new PantryItem();
        item.setDisplayName(name);
        item.setCanonicalName(IngredientNormalizer.canonical(name));
        item.setQuantity(quantity);
        item.setUnit(unit);
        item.setBaseUnit(UnitConverter.baseUnitFor(unit));
        item.setBaseQuantity(UnitConverter.toBase(quantity, unit));
        return item;
    }

    /** Builds a recipe ingredient the same way the seeder does. */
    private static RecipeIngredient ingredient(String name, double quantity, String unit) {
        RecipeIngredient ing = new RecipeIngredient(name, quantity, unit);
        ing.setCanonicalName(IngredientNormalizer.canonical(name));
        ing.setBaseUnit(UnitConverter.baseUnitFor(unit));
        ing.setBaseQuantity(UnitConverter.toBase(quantity, unit));
        return ing;
    }

    /** A pantry holding everything the test recipe needs, and then some. */
    private static List<PantryItem> fullPantry() {
        return new ArrayList<>(Arrays.asList(
                pantryItem("Spaghetti", 1, "kg"),
                pantryItem("Tomato", 6, "unit"),
                pantryItem("Garlic", 5, "cloves"),
                pantryItem("Olive Oil", 500, "ml"),
                pantryItem("Basil", 1, "unit")
        ));
    }

    @Before
    public void setUp() {
        pasta = new Recipe();
        pasta.setName("Tomato and Basil Pasta");
        pasta.addIngredient(ingredient("spaghetti", 400, "g"));
        pasta.addIngredient(ingredient("tomatoes", 4, "unit"));
        pasta.addIngredient(ingredient("garlic", 2, "cloves"));
        pasta.addIngredient(ingredient("olive oil", 2, "tbsp"));
        pasta.addIngredient(ingredient("basil", 1, "unit"));

        recipes = new ArrayList<>();
        recipes.add(pasta);
    }

    @Test
    public void suggestsRecipeWhenEveryIngredientIsPresent() {
        MatchResult result = RecipeMatcher.match(fullPantry(), pasta);

        assertTrue(result.isStrictMatch());
        assertTrue(result.getMissing().isEmpty());
        assertTrue(result.getInsufficient().isEmpty());
    }

    @Test
    public void excludesRecipeWhenOneIngredientIsMissing() {
        // Four of the five ingredients are present. The brief is explicit that
        // this recipe must NOT appear in the suggestions list.
        List<PantryItem> pantry = fullPantry();
        pantry.remove(4); // basil

        assertEquals(0, RecipeMatcher.findStrictMatches(pantry, recipes).size());
    }

    @Test
    public void excludesRecipeWhenSeveralIngredientsAreMissing() {
        List<PantryItem> pantry = new ArrayList<>();
        pantry.add(pantryItem("Spaghetti", 1, "kg"));

        MatchResult result = RecipeMatcher.match(pantry, pasta);

        assertFalse(result.isStrictMatch());
        assertEquals(MatchResult.Status.NO_MATCH, result.getStatus());
        assertEquals(4, result.getMissing().size());
    }

    @Test
    public void excludesRecipeWhenQuantityIsTooLow() {
        // The ingredient is present, but there is not enough of it.
        List<PantryItem> pantry = fullPantry();
        pantry.set(0, pantryItem("Spaghetti", 100, "g"));

        MatchResult result = RecipeMatcher.match(pantry, pasta);

        assertFalse(result.isStrictMatch());
        assertEquals(1, result.getInsufficient().size());
        assertEquals("spaghetti", result.getInsufficient().get(0).getCanonicalName());
    }

    @Test
    public void exactlyEnoughStillCounts() {
        // 0.4 kg converts to 399.99999... in floating point, so a naive
        // comparison would wrongly reject a pantry holding exactly enough.
        List<PantryItem> pantry = fullPantry();
        pantry.set(0, pantryItem("Spaghetti", 0.4, "kg"));

        assertTrue(RecipeMatcher.match(pantry, pasta).isStrictMatch());
    }

    @Test
    public void matchesAcrossDifferentUnitsOfTheSameKind() {
        // Recipe asks for grams and tablespoons; pantry holds kilograms and millilitres.
        assertTrue(RecipeMatcher.match(fullPantry(), pasta).isStrictMatch());
    }

    @Test
    public void matchesDespiteSingularAndPluralDifferences() {
        // Pantry says "Tomato", recipe says "tomatoes".
        MatchResult result = RecipeMatcher.match(fullPantry(), pasta);

        assertTrue(result.isStrictMatch());
    }

    @Test
    public void addsUpDuplicatePantryEntries() {
        // Two separate entries of one clove each must satisfy a recipe needing two.
        List<PantryItem> pantry = fullPantry();
        pantry.set(2, pantryItem("Garlic", 1, "clove"));
        pantry.add(pantryItem("Garlic", 1, "clove"));

        assertTrue(RecipeMatcher.match(pantry, pasta).isStrictMatch());
    }

    @Test
    public void doesNotMatchAcrossIncompatibleUnits() {
        // One "unit" of flour is not proof of 500 g of flour, so this must fail
        // rather than the app guessing what a bag weighs.
        Recipe bread = new Recipe();
        bread.setName("Bread");
        bread.addIngredient(ingredient("flour", 500, "g"));

        List<PantryItem> pantry = new ArrayList<>();
        pantry.add(pantryItem("Flour", 1, "unit"));

        assertFalse(RecipeMatcher.match(pantry, bread).isStrictMatch());
    }

    @Test
    public void reportsRecipeMissingExactlyOneIngredientAsAlmostThere() {
        List<PantryItem> pantry = fullPantry();
        pantry.remove(4); // basil

        MatchResult result = RecipeMatcher.match(pantry, pasta);

        assertEquals(MatchResult.Status.ALMOST_THERE, result.getStatus());
        assertEquals(1, RecipeMatcher.findAlmostThere(pantry, recipes).size());
    }

    @Test
    public void almostThereIsNeverMixedIntoStrictSuggestions() {
        List<PantryItem> pantry = fullPantry();
        pantry.remove(4);

        assertEquals(0, RecipeMatcher.findStrictMatches(pantry, recipes).size());
        assertEquals(1, RecipeMatcher.findAlmostThere(pantry, recipes).size());
    }

    @Test
    public void emptyPantrySuggestsNothingAndDoesNotCrash() {
        List<PantryItem> empty = new ArrayList<>();

        assertEquals(0, RecipeMatcher.findStrictMatches(empty, recipes).size());
        assertEquals(0, RecipeMatcher.buildPantryIndex(null).size());
    }

    @Test
    public void suggestionsAreSortedByName() {
        Recipe toast = new Recipe();
        toast.setName("Avocado Toast");
        toast.addIngredient(ingredient("bread", 2, "slices"));

        List<PantryItem> pantry = fullPantry();
        pantry.add(pantryItem("Bread", 10, "slices"));

        List<Recipe> both = Arrays.asList(pasta, toast);
        List<MatchResult> matches = RecipeMatcher.findStrictMatches(pantry, both);

        assertEquals(2, matches.size());
        assertEquals("Avocado Toast", matches.get(0).getRecipe().getName());
    }
}
