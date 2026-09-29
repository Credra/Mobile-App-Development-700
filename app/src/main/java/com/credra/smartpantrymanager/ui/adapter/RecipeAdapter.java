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

import java.util.ArrayList;
import java.util.List;

/**
 * Binds match results to the recipe list.
 *
 * It takes {@link MatchResult} rather than {@link Recipe} so the same adapter
 * serves both the strict suggestions and the optional Almost There list, where
 * the missing ingredient needs to be shown. Section headers are a second view
 * type, which is what keeps the two lists visibly separate.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_RECIPE = 1;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    /** One line of the list: either a section heading or a recipe. */
    public static final class Row {

        private final String header;
        private final MatchResult result;

        private Row(String header, MatchResult result) {
            this.header = header;
            this.result = result;
        }

        public static Row header(String title) {
            return new Row(title, null);
        }

        public static Row recipe(MatchResult result) {
            return new Row(null, result);
        }

        boolean isHeader() {
            return header != null;
        }
    }

    private List<Row> rows = new ArrayList<>();
    private final OnRecipeClickListener listener;

    public RecipeAdapter(List<Row> rows, OnRecipeClickListener listener) {
        this.rows = rows;
        this.listener = listener;
    }

    public void setRows(List<Row> rows) {
        this.rows = rows;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).isHeader() ? TYPE_HEADER : TYPE_RECIPE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(
                    inflater.inflate(R.layout.item_section_header, parent, false));
        }
        return new RecipeViewHolder(inflater.inflate(R.layout.item_recipe, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (row.isHeader()) {
            ((HeaderViewHolder) holder).bind(row.header);
        } else {
            ((RecipeViewHolder) holder).bind(row.result, listener);
        }
    }

    @Override
    public int getItemCount() {
        return rows == null ? 0 : rows.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleView;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.sectionTitle);
        }

        void bind(String title) {
            titleView.setText(title);
        }
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

            // Only the Almost There rows have anything to report here.
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
