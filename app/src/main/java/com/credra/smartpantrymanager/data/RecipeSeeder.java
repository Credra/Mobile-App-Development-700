package com.credra.smartpantrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.credra.smartpantrymanager.R;
import com.credra.smartpantrymanager.logic.IngredientNormalizer;
import com.credra.smartpantrymanager.logic.UnitConverter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Loads the bundled recipe collection into the database the first time it is
 * created, so the app is usable immediately rather than starting empty.
 *
 * Canonical names and base quantities are computed here, at write time, so that
 * matching later needs no conversion.
 */
public final class RecipeSeeder {

    private static final String TAG = "RecipeSeeder";

    private RecipeSeeder() {
    }

    public static void seed(Context context, SQLiteDatabase db) {
        try {
            JSONArray recipes = new JSONArray(readRawResource(context, R.raw.recipes));
            for (int i = 0; i < recipes.length(); i++) {
                insertRecipe(db, recipes.getJSONObject(i));
            }
            Log.i(TAG, "Seeded " + recipes.length() + " recipes");
        } catch (Exception e) {
            // A failure here leaves the app with an empty recipe list rather
            // than crashing on first launch.
            Log.e(TAG, "Could not seed recipes", e);
        }
    }

    private static void insertRecipe(SQLiteDatabase db, JSONObject recipe) throws Exception {
        ContentValues values = new ContentValues();
        values.put(PantryDBHelper.COL_RECIPE_NAME, recipe.getString("name"));
        values.put(PantryDBHelper.COL_CATEGORY, recipe.optString("category", ""));
        values.put(PantryDBHelper.COL_SERVINGS, recipe.optInt("servings", 0));
        values.put(PantryDBHelper.COL_PREP_MINUTES, recipe.optInt("prepMinutes", 0));
        values.put(PantryDBHelper.COL_METHOD, recipe.getString("method"));

        long recipeId = db.insert(PantryDBHelper.TABLE_RECIPE, null, values);
        if (recipeId == -1) {
            Log.e(TAG, "Failed to insert recipe " + recipe.getString("name"));
            return;
        }

        JSONArray ingredients = recipe.getJSONArray("ingredients");
        for (int i = 0; i < ingredients.length(); i++) {
            JSONObject ingredient = ingredients.getJSONObject(i);
            String name = ingredient.getString("name");
            double quantity = ingredient.getDouble("quantity");
            String unit = ingredient.getString("unit");

            ContentValues row = new ContentValues();
            row.put(PantryDBHelper.COL_RECIPE_ID, recipeId);
            row.put(PantryDBHelper.COL_DISPLAY_NAME, name);
            row.put(PantryDBHelper.COL_CANONICAL_NAME, IngredientNormalizer.canonical(name));
            row.put(PantryDBHelper.COL_QUANTITY, quantity);
            row.put(PantryDBHelper.COL_UNIT, unit);
            row.put(PantryDBHelper.COL_BASE_UNIT, UnitConverter.baseUnitFor(unit));
            row.put(PantryDBHelper.COL_BASE_QUANTITY, UnitConverter.toBase(quantity, unit));
            db.insert(PantryDBHelper.TABLE_RECIPE_INGREDIENT, null, row);
        }
    }

    private static String readRawResource(Context context, int resourceId) throws IOException {
        InputStream input = context.getResources().openRawResource(resourceId);
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int read;
            while ((read = input.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            return buffer.toString(Charset.forName("UTF-8").name());
        } finally {
            input.close();
        }
    }
}
