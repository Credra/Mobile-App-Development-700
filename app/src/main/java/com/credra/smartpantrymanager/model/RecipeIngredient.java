package com.credra.smartpantrymanager.model;

/** One line of a recipe's ingredient list, e.g. "400 g spaghetti". */
public class RecipeIngredient {

    private long id;
    private long recipeId;
    private String displayName;
    private String canonicalName;
    private double quantity;
    private String unit;
    private String baseUnit;
    private double baseQuantity;

    public RecipeIngredient() {
    }

    public RecipeIngredient(String displayName, double quantity, String unit) {
        this.displayName = displayName;
        this.quantity = quantity;
        this.unit = unit;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getRecipeId() { return recipeId; }
    public void setRecipeId(long recipeId) { this.recipeId = recipeId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getCanonicalName() { return canonicalName; }
    public void setCanonicalName(String canonicalName) { this.canonicalName = canonicalName; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getBaseUnit() { return baseUnit; }
    public void setBaseUnit(String baseUnit) { this.baseUnit = baseUnit; }

    public double getBaseQuantity() { return baseQuantity; }
    public void setBaseQuantity(double baseQuantity) { this.baseQuantity = baseQuantity; }

    /** Must be built the same way as PantryItem.matchKey() for matching to work. */
    public String matchKey() {
        return canonicalName + "|" + baseUnit;
    }

    /** Human readable amount for the recipe detail screen, e.g. "400 g spaghetti". */
    public String describe() {
        String amount = (quantity == Math.floor(quantity))
                ? String.valueOf((long) quantity)
                : String.valueOf(quantity);
        return amount + " " + unit + " " + displayName;
    }
}
