package com.credra.smartpantrymanager.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.credra.smartpantrymanager.R;
import com.credra.smartpantrymanager.data.PantryDataSource;
import com.credra.smartpantrymanager.logic.RecipeMatcher;
import com.credra.smartpantrymanager.model.MatchResult;
import com.credra.smartpantrymanager.model.Recipe;
import com.credra.smartpantrymanager.model.RecipeIngredient;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Shows one recipe in full: what it needs, whether the pantry covers each
 * ingredient, and how to make it.
 *
 * Marking each ingredient rather than only listing them means the user can see
 * why a recipe did or did not qualify, instead of the rule being invisible.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    private static final String HAVE = "✓";     // tick
    private static final String MISSING = "✗";  // cross

    private PantryDataSource dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);

        dataSource = new PantryDataSource(this);
        dataSource.open();

        Recipe recipe = dataSource.getRecipe(recipeId);
        if (recipe == null) {
            Toast.makeText(this, R.string.recipe_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setTitle(recipe.getName());
        showRecipe(recipe);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataSource.close();
    }

    private void showRecipe(Recipe recipe) {
        ((TextView) findViewById(R.id.detailName)).setText(recipe.getName());
        ((TextView) findViewById(R.id.detailMeta)).setText(getString(R.string.recipe_detail_meta,
                recipe.getCategory(), recipe.getServings(), recipe.getPrepMinutes()));

        showIngredients(recipe);
        showMethod(recipe);
    }

    /** Lists every ingredient, ticked when the pantry already covers it. */
    private void showIngredients(Recipe recipe) {
        MatchResult result = RecipeMatcher.match(dataSource.getAllPantryItems(), recipe);

        // Anything the matcher reported as missing or short is not covered.
        Set<Long> shortfalls = new HashSet<>();
        for (RecipeIngredient ingredient : result.getAllShortfalls()) {
            shortfalls.add(ingredient.getId());
        }

        LinearLayout container = findViewById(R.id.ingredientContainer);
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            View row = inflater.inflate(R.layout.item_recipe_ingredient, container, false);
            TextView status = row.findViewById(R.id.ingredientStatus);
            TextView text = row.findViewById(R.id.ingredientText);

            boolean covered = !shortfalls.contains(ingredient.getId());
            status.setText(covered ? HAVE : MISSING);
            status.setTextColor(ContextCompat.getColor(this,
                    covered ? R.color.ingredient_have : R.color.ingredient_missing));
            text.setText(ingredient.describe());

            container.addView(row);
        }
    }

    private void showMethod(Recipe recipe) {
        LinearLayout container = findViewById(R.id.methodContainer);
        container.removeAllViews();

        List<String> steps = recipe.getSteps();
        for (int i = 0; i < steps.size(); i++) {
            TextView step = new TextView(this);
            step.setText(getString(R.string.method_step, i + 1, steps.get(i)));
            step.setTextSize(15);
            step.setPadding(0, 0, 0, 16);
            container.addView(step);
        }
    }

    /** Builds the intent the suggestions list uses to open this screen. */
    public static Intent intentFor(Context context, long recipeId) {
        Intent intent = new Intent(context, RecipeDetailActivity.class);
        intent.putExtra(EXTRA_RECIPE_ID, recipeId);
        return intent;
    }
}
