package com.credra.smartpantrymanager.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The outcome of testing one recipe against the pantry. Carries the failed
 * ingredients so the detail screen can show what is missing.
 */
public class MatchResult {

    public enum Status {
        /** Every ingredient is present in at least the required quantity. */
        STRICT_MATCH,
        /** Exactly one ingredient is missing or short. Shown only in the optional list. */
        ALMOST_THERE,
        /** Two or more ingredients are missing or short. */
        NO_MATCH
    }

    private final Recipe recipe;
    private final Status status;
    private final List<RecipeIngredient> missing;
    private final List<RecipeIngredient> insufficient;

    public MatchResult(Recipe recipe, Status status,
                       List<RecipeIngredient> missing,
                       List<RecipeIngredient> insufficient) {
        this.recipe = recipe;
        this.status = status;
        this.missing = missing;
        this.insufficient = insufficient;
    }

    public Recipe getRecipe() { return recipe; }

    public Status getStatus() { return status; }

    /** Ingredients the pantry does not contain at all. */
    public List<RecipeIngredient> getMissing() { return missing; }

    /** Ingredients the pantry contains, but not enough of. */
    public List<RecipeIngredient> getInsufficient() { return insufficient; }

    public boolean isStrictMatch() {
        return status == Status.STRICT_MATCH;
    }

    /** Every ingredient that failed, for display purposes. */
    public List<RecipeIngredient> getAllShortfalls() {
        List<RecipeIngredient> all = new ArrayList<>(missing);
        all.addAll(insufficient);
        return all;
    }

    /** Short explanation used on the optional "Almost There" list. */
    public String shortfallSummary() {
        List<RecipeIngredient> all = getAllShortfalls();
        if (all.isEmpty()) {
            return "You have everything you need";
        }
        if (all.size() == 1) {
            return "Missing: " + all.get(0).getDisplayName();
        }
        return "Missing " + all.size() + " ingredients";
    }
}
