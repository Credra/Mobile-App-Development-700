package com.credra.smartpantrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.credra.smartpantrymanager.logic.IngredientNormalizer;
import com.credra.smartpantrymanager.logic.UnitConverter;
import com.credra.smartpantrymanager.model.PantryItem;
import com.credra.smartpantrymanager.model.Recipe;
import com.credra.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Opens and closes the database and holds every query the app makes.
 * Callers work with model objects and never see a Cursor.
 */
public class PantryDataSource {

    private static final String TAG = "PantryDataSource";

    private SQLiteDatabase database;
    private final PantryDBHelper dbHelper;

    public PantryDataSource(Context context) {
        dbHelper = new PantryDBHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // ---------------------------------------------------------------- pantry

    /** Inserts a new pantry item. Returns false if the insert failed. */
    public boolean insertPantryItem(PantryItem item) {
        try {
            long id = database.insert(PantryDBHelper.TABLE_PANTRY, null, toValues(item));
            if (id == -1) {
                return false;
            }
            item.setId(id);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Insert failed", e);
            return false;
        }
    }

    /** Overwrites an existing pantry item, matched on its id. */
    public boolean updatePantryItem(PantryItem item) {
        try {
            int rows = database.update(PantryDBHelper.TABLE_PANTRY, toValues(item),
                    PantryDBHelper.COL_ID + " = ?",
                    new String[]{String.valueOf(item.getId())});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Update failed", e);
            return false;
        }
    }

    public boolean deletePantryItem(long id) {
        try {
            int rows = database.delete(PantryDBHelper.TABLE_PANTRY,
                    PantryDBHelper.COL_ID + " = ?", new String[]{String.valueOf(id)});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Delete failed", e);
            return false;
        }
    }

    /** Every pantry item, ordered by name. */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        Cursor cursor = database.query(PantryDBHelper.TABLE_PANTRY, null, null, null, null, null,
                PantryDBHelper.COL_DISPLAY_NAME + " COLLATE NOCASE ASC");
        try {
            while (cursor.moveToNext()) {
                items.add(readPantryItem(cursor));
            }
        } finally {
            cursor.close();
        }
        return items;
    }

    /** A single pantry item, or null if the id is not in the table. */
    public PantryItem getPantryItem(long id) {
        Cursor cursor = database.query(PantryDBHelper.TABLE_PANTRY, null,
                PantryDBHelper.COL_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null);
        try {
            return cursor.moveToFirst() ? readPantryItem(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    public int countPantryItems() {
        Cursor cursor = database.rawQuery(
                "SELECT COUNT(*) FROM " + PantryDBHelper.TABLE_PANTRY, null);
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    /** Fills in the canonical name and base quantity used for matching. */
    private ContentValues toValues(PantryItem item) {
        item.setCanonicalName(IngredientNormalizer.canonical(item.getDisplayName()));
        item.setBaseUnit(UnitConverter.baseUnitFor(item.getUnit()));
        item.setBaseQuantity(UnitConverter.toBase(item.getQuantity(), item.getUnit()));

        ContentValues values = new ContentValues();
        values.put(PantryDBHelper.COL_DISPLAY_NAME, item.getDisplayName());
        values.put(PantryDBHelper.COL_CANONICAL_NAME, item.getCanonicalName());
        values.put(PantryDBHelper.COL_QUANTITY, item.getQuantity());
        values.put(PantryDBHelper.COL_UNIT, item.getUnit());
        values.put(PantryDBHelper.COL_BASE_UNIT, item.getBaseUnit());
        values.put(PantryDBHelper.COL_BASE_QUANTITY, item.getBaseQuantity());
        values.put(PantryDBHelper.COL_EXPIRY_DATE, item.getExpiryDate());
        values.put(PantryDBHelper.COL_DATE_ADDED, item.getDateAdded());
        return values;
    }

    private PantryItem readPantryItem(Cursor cursor) {
        PantryItem item = new PantryItem();
        item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_ID)));
        item.setDisplayName(cursor.getString(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_DISPLAY_NAME)));
        item.setCanonicalName(cursor.getString(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_CANONICAL_NAME)));
        item.setQuantity(cursor.getDouble(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_QUANTITY)));
        item.setUnit(cursor.getString(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_UNIT)));
        item.setBaseUnit(cursor.getString(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_BASE_UNIT)));
        item.setBaseQuantity(cursor.getDouble(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_BASE_QUANTITY)));
        item.setDateAdded(cursor.getLong(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_DATE_ADDED)));

        int expiryColumn = cursor.getColumnIndexOrThrow(PantryDBHelper.COL_EXPIRY_DATE);
        item.setExpiryDate(cursor.isNull(expiryColumn) ? null : cursor.getLong(expiryColumn));
        return item;
    }

    // --------------------------------------------------------------- recipes

    /** Every recipe with its ingredients attached, ready for matching. */
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        Cursor cursor = database.query(PantryDBHelper.TABLE_RECIPE, null, null, null, null, null,
                PantryDBHelper.COL_RECIPE_NAME + " COLLATE NOCASE ASC");
        try {
            while (cursor.moveToNext()) {
                Recipe recipe = readRecipe(cursor);
                recipe.setIngredients(getIngredientsFor(recipe.getId()));
                recipes.add(recipe);
            }
        } finally {
            cursor.close();
        }
        return recipes;
    }

    /** A single recipe with its ingredients, or null if not found. */
    public Recipe getRecipe(long id) {
        Cursor cursor = database.query(PantryDBHelper.TABLE_RECIPE, null,
                PantryDBHelper.COL_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null);
        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            Recipe recipe = readRecipe(cursor);
            recipe.setIngredients(getIngredientsFor(recipe.getId()));
            return recipe;
        } finally {
            cursor.close();
        }
    }

    public int countRecipes() {
        Cursor cursor = database.rawQuery(
                "SELECT COUNT(*) FROM " + PantryDBHelper.TABLE_RECIPE, null);
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    private List<RecipeIngredient> getIngredientsFor(long recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        Cursor cursor = database.query(PantryDBHelper.TABLE_RECIPE_INGREDIENT, null,
                PantryDBHelper.COL_RECIPE_ID + " = ?", new String[]{String.valueOf(recipeId)},
                null, null, PantryDBHelper.COL_ID + " ASC");
        try {
            while (cursor.moveToNext()) {
                RecipeIngredient ingredient = new RecipeIngredient();
                ingredient.setId(cursor.getLong(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_ID)));
                ingredient.setRecipeId(recipeId);
                ingredient.setDisplayName(cursor.getString(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_DISPLAY_NAME)));
                ingredient.setCanonicalName(cursor.getString(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_CANONICAL_NAME)));
                ingredient.setQuantity(cursor.getDouble(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_QUANTITY)));
                ingredient.setUnit(cursor.getString(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_UNIT)));
                ingredient.setBaseUnit(cursor.getString(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_BASE_UNIT)));
                ingredient.setBaseQuantity(cursor.getDouble(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_BASE_QUANTITY)));
                ingredients.add(ingredient);
            }
        } finally {
            cursor.close();
        }
        return ingredients;
    }

    private Recipe readRecipe(Cursor cursor) {
        Recipe recipe = new Recipe();
        recipe.setId(cursor.getLong(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_ID)));
        recipe.setName(cursor.getString(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_RECIPE_NAME)));
        recipe.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_CATEGORY)));
        recipe.setServings(cursor.getInt(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_SERVINGS)));
        recipe.setPrepMinutes(cursor.getInt(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_PREP_MINUTES)));
        recipe.setMethod(cursor.getString(cursor.getColumnIndexOrThrow(PantryDBHelper.COL_METHOD)));
        return recipe;
    }
}
