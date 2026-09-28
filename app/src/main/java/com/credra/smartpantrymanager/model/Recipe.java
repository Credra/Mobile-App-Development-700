package com.credra.smartpantrymanager.model;

import java.util.ArrayList;
import java.util.List;

/** A recipe with its required ingredients and method. Seeded on first run. */
public class Recipe {

    private long id;
    private String name;
    private String category;
    private int servings;
    private int prepMinutes;
    private String method;
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe() {
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getServings() { return servings; }
    public void setServings(int servings) { this.servings = servings; }

    public int getPrepMinutes() { return prepMinutes; }
    public void setPrepMinutes(int prepMinutes) { this.prepMinutes = prepMinutes; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredient> ingredients) { this.ingredients = ingredients; }

    public void addIngredient(RecipeIngredient ingredient) {
        ingredients.add(ingredient);
    }

    /** The method is stored as one string; steps are separated by newlines. */
    public List<String> getSteps() {
        List<String> steps = new ArrayList<>();
        if (method == null) {
            return steps;
        }
        for (String line : method.split("\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                steps.add(trimmed);
            }
        }
        return steps;
    }
}
