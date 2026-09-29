package com.credra.smartpantrymanager.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.credra.smartpantrymanager.R;
import com.credra.smartpantrymanager.model.MatchResult;
import com.credra.smartpantrymanager.model.Recipe;

import java.util.List;

/**
 * Binds match results to the recipe list.
 *
 * It takes {@link MatchResult} rather than {@link Recipe} so the same adapter
 * serves both the strict suggestions and the optional Almost There list, where
 * the missing ingredient needs to be shown.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private List<MatchResult> results;
    private final OnRecipeClickListener listener;

    public RecipeAdapter(List<MatchResult> results, OnRecipeClickListener listener) {
        this.results = results;
        this.listener = listener;
    }

    public void setResults(List<MatchResult> results) {
        this.results = results;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        holder.bind(results.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return results == null ? 0 : results.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameView;
        private final TextView metaView;
        private final TextView shortfallView;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.recipeName);
            metaView = itemView.findViewById(R.id.recipeMeta);
            shortfallView = itemView.findViewById(R.id.recipeShortfall);
        }

        void bind(final MatchResult result, final OnRecipeClickListener listener) {
            final Recipe recipe = result.getRecipe();
            nameView.setText(recipe.getName());
            metaView.setText(itemView.getContext().getString(R.string.recipe_meta,
                    recipe.getCategory(),
                    recipe.getPrepMinutes(),
                    recipe.getIngredients().size()));

            // Only the Almost There list has anything to report here.
            if (result.isStrictMatch()) {
                shortfallView.setVisibility(View.GONE);
            } else {
                shortfallView.setVisibility(View.VISIBLE);
                shortfallView.setText(result.shortfallSummary());
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onRecipeClick(recipe);
                    }
                }
            });
        }
    }
}
