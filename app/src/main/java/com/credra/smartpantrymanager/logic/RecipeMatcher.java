package com.credra.smartpantrymanager.logic;

import com.credra.smartpantrymanager.model.MatchResult;
import com.credra.smartpantrymanager.model.PantryItem;
import com.credra.smartpantrymanager.model.Recipe;
import com.credra.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Decides whether a recipe can be cooked from the current pantry alone.
 *
 * The strict rule is absolute: a recipe qualifies only when EVERY ingredient is
 * present in at least the required quantity. Four out of five never appears.
 * Recipes short by exactly one are flagged ALMOST_THERE for the separate
 * optional list, never mixed into the strict results.
 */
public final class RecipeMatcher {

    private RecipeMatcher() {
        // Utility class; not meant to be instantiated.
    }

    /**
     * Unit conversion yields values like 399.99999 for 0.4 kg, so exact matches
     * need a tolerance or they would be wrongly rejected.
     */
    private static final double EPSILON = 1e-6;

    /**
     * Totals the pantry by canonical name and base unit, summing duplicates so
     * two separate entries of three and two eggs satisfy a recipe needing five.
     */
    public static Map<String, Double> buildPantryIndex(List<PantryItem> pantry) {
        Map<String, Double> totals = new HashMap<>();
        if (pantry == null) {
            return totals;
        }
        for (PantryItem item : pantry) {
            String key = item.matchKey();
            Double running = totals.get(key);
            totals.put(key, running == null ? item.getBaseQuantity()
                                            : running + item.getBaseQuantity());
        }
        return totals;
    }

    /** Tests one recipe against a pre-built pantry index. */
    public static MatchResult match(Map<String, Double> pantryIndex, Recipe recipe) {
        List<RecipeIngredient> missing = new ArrayList<>();
        List<RecipeIngredient> insufficient = new ArrayList<>();

        for (RecipeIngredient required : recipe.getIngredients()) {
            Double available = pantryIndex.get(required.matchKey());

            if (available == null) {
                // Not in the pantry at all, or held in an incompatible unit.
                missing.add(required);
            } else if (available + EPSILON < required.getBaseQuantity()) {
                // Present, but not enough of it.
                insufficient.add(required);
            }
        }

        int shortfalls = missing.size() + insufficient.size();
        MatchResult.Status status;
        if (shortfalls == 0) {
            status = MatchResult.Status.STRICT_MATCH;
        } else if (shortfalls == 1) {
            status = MatchResult.Status.ALMOST_THERE;
        } else {
            status = MatchResult.Status.NO_MATCH;
        }

        return new MatchResult(recipe, status, missing, insufficient);
    }

    /** Convenience overload that builds the index for a single comparison. */
    public static MatchResult match(List<PantryItem> pantry, Recipe recipe) {
        return match(buildPantryIndex(pantry), recipe);
    }

    /** Returns only the recipes the user can cook right now, sorted by name. */
    public static List<MatchResult> findStrictMatches(List<PantryItem> pantry,
                                                      List<Recipe> recipes) {
        Map<String, Double> pantryIndex = buildPantryIndex(pantry);
        List<MatchResult> matches = new ArrayList<>();

        for (Recipe recipe : recipes) {
            MatchResult result = match(pantryIndex, recipe);
            if (result.isStrictMatch()) {
                matches.add(result);
            }
        }

        sortByRecipeName(matches);
        return matches;
    }

    /** Recipes short by exactly one ingredient, for the separate optional list. */
    public static List<MatchResult> findAlmostThere(List<PantryItem> pantry,
                                                    List<Recipe> recipes) {
        Map<String, Double> pantryIndex = buildPantryIndex(pantry);
        List<MatchResult> almost = new ArrayList<>();

        for (Recipe recipe : recipes) {
            MatchResult result = match(pantryIndex, recipe);
            if (result.getStatus() == MatchResult.Status.ALMOST_THERE) {
                almost.add(result);
            }
        }

        sortByRecipeName(almost);
        return almost;
    }

    private static void sortByRecipeName(List<MatchResult> results) {
        Collections.sort(results, (left, right) ->
                left.getRecipe().getName().compareToIgnoreCase(right.getRecipe().getName()));
    }
}
