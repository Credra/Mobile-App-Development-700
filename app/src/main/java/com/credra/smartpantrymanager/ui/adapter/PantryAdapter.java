package com.credra.smartpantrymanager.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.credra.smartpantrymanager.R;
import com.credra.smartpantrymanager.model.PantryItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Binds pantry items to the list rows on the pantry screen. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /** Implemented by the activity so it decides what a tap means. */
    public interface OnItemClickListener {
        void onItemClick(PantryItem item);
    }

    private static final SimpleDateFormat EXPIRY_FORMAT =
            new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

    private List<PantryItem> items;
    private final OnItemClickListener listener;

    public PantryAdapter(List<PantryItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    /** Replaces the backing list after the database has changed. */
    public void setItems(List<PantryItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public PantryItem getItem(int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class PantryViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameView;
        private final TextView quantityView;
        private final TextView expiryView;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.itemName);
            quantityView = itemView.findViewById(R.id.itemQuantity);
            expiryView = itemView.findViewById(R.id.itemExpiry);
        }

        void bind(final PantryItem item, final OnItemClickListener listener) {
            nameView.setText(item.getDisplayName());
            quantityView.setText(formatQuantity(item));

            if (item.getExpiryDate() == null) {
                expiryView.setVisibility(View.GONE);
            } else {
                expiryView.setVisibility(View.VISIBLE);
                expiryView.setText(itemView.getContext().getString(R.string.expires_on,
                        EXPIRY_FORMAT.format(new Date(item.getExpiryDate()))));
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(item);
                    }
                }
            });
        }

        /** Shows "2 kg" rather than "2.0 kg" when the amount is a whole number. */
        private String formatQuantity(PantryItem item) {
            double quantity = item.getQuantity();
            String amount = quantity == Math.floor(quantity)
                    ? String.valueOf((long) quantity)
                    : String.valueOf(quantity);
            return amount + " " + item.getUnit();
        }
    }
}
