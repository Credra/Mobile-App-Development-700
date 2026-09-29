package com.credra.smartpantrymanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.credra.smartpantrymanager.R;
import com.credra.smartpantrymanager.data.PantryDataSource;
import com.credra.smartpantrymanager.logic.RecipeMatcher;
import com.credra.smartpantrymanager.model.MatchResult;
import com.credra.smartpantrymanager.model.PantryItem;
import com.credra.smartpantrymanager.model.Recipe;
import com.credra.smartpantrymanager.ui.adapter.RecipeAdapter;
import com.credra.smartpantrymanager.util.Prefs;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows only the recipes the user can cook right now.
 *
 * This screen is where the strict-matching rule becomes visible: a recipe
 * appears only when every ingredient it needs is already in the pantry, in at
 * least the required quantity.
 */
public class SuggestedRecipesActivity extends AppCompatActivity
        implements RecipeAdapter.OnRecipeClickListener {

    private PantryDataSource dataSource;
    private RecipeAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyStateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        setTitle(R.string.title_recipes);

        dataSource = new PantryDataSource(this);

        recyclerView = findViewById(R.id.recipeRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        adapter = new RecipeAdapter(new ArrayList<RecipeAdapter.Row>(), this);
        recyclerView.setAdapter(adapter);

        setUpBottomNavigation();
    }

    /**
     * Recalculated on every return to the screen, so editing the pantry and
     * coming back immediately shows the effect on the suggestions.
     */
    @Override
    protected void onResume() {
        super.onResume();
        dataSource.open();
        loadSuggestions();

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_recipes);
    }

    @Override
    protected void onPause() {
        super.onPause();
        dataSource.close();
    }

    private void loadSuggestions() {
        List<PantryItem> pantry = dataSource.getAllPantryItems();
        List<Recipe> recipes = dataSource.getAllRecipes();

        List<MatchResult> strict = RecipeMatcher.findStrictMatches(pantry, recipes);
        List<RecipeAdapter.Row> rows = new ArrayList<>();

        // The strict suggestions always come first and are never mixed with
        // anything the user cannot actually cook right now.
        if (!strict.isEmpty()) {
            rows.add(RecipeAdapter.Row.header(getString(R.string.section_suggested)));
            for (MatchResult match : strict) {
                rows.add(RecipeAdapter.Row.recipe(match));
            }
        }

        if (Prefs.isShowAlmostThere(this)) {
            List<MatchResult> almost = RecipeMatcher.findAlmostThere(pantry, recipes);
            if (!almost.isEmpty()) {
                rows.add(RecipeAdapter.Row.header(getString(R.string.section_almost_there)));
                for (MatchResult match : almost) {
                    rows.add(RecipeAdapter.Row.recipe(match));
                }
            }
        }

        adapter.setRows(rows);

        // The empty state is about the strict list: having only Almost There
        // results still means there is nothing you can cook.
        boolean nothingToCook = strict.isEmpty();
        emptyStateText.setVisibility(nothingToCook && rows.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(rows.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void setUpBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_recipes);
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_pantry) {
                    Intent intent = new Intent(SuggestedRecipesActivity.this,
                            PantryListActivity.class);
                    // Reuse the existing screen instead of stacking a new copy.
                    // REORDER_TO_FRONT was skipping the transition on every
                    // switch after the first.
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    // Cross-fade rather than a hard cut, so switching
                    // tabs does not flash between windows.
                    overridePendingTransition(android.R.anim.fade_in,
                            android.R.anim.fade_out);
                    return false;
                }
                if (item.getItemId() == R.id.nav_settings) {
                    Intent settings = new Intent(SuggestedRecipesActivity.this, SettingsActivity.class);
                    settings.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(settings);
                    overridePendingTransition(android.R.anim.fade_in,
                            android.R.anim.fade_out);
                    return false;
                }
                return item.getItemId() == R.id.nav_recipes;
            }
        });
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        startActivity(RecipeDetailActivity.intentFor(this, recipe.getId()));
    }
}
