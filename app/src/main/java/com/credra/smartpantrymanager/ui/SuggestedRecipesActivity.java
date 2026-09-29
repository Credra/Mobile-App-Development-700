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
import com.credra.smartpantrymanager.model.Recipe;
import com.credra.smartpantrymanager.ui.adapter.RecipeAdapter;
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

        adapter = new RecipeAdapter(new ArrayList<MatchResult>(), this);
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        dataSource.close();
    }

    private void loadSuggestions() {
        List<MatchResult> matches = RecipeMatcher.findStrictMatches(
                dataSource.getAllPantryItems(), dataSource.getAllRecipes());
        adapter.setResults(matches);

        boolean empty = matches.isEmpty();
        emptyStateText.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
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
                    // Reuse the existing pantry screen rather than stacking a new one.
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    return true;
                }
                return item.getItemId() == R.id.nav_recipes;
            }
        });
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        // Opens the detail screen once RecipeDetailActivity exists.
    }
}
