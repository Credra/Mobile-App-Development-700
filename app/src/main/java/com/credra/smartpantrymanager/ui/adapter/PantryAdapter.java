package com.credra.smartpantrymanager.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.credra.smartpantrymanager.R;
import com.credra.smartpantrymanager.model.PantryItem;
import com.credra.smartpantrymanager.util.Prefs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Locale;

/** Binds pantry items to the list rows on the pantry screen. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /** Implemented by the activity so it decides what a tap means. */
    public interface OnItemClickListener {
        void onItemClick(PantryItem item);
    }

    /** Long press is the delete gesture, handled by the activity. */
    public interface OnItemLongClickListener {
        void onItemLongClick(PantryItem item);
    }

    private static final SimpleDateFormat EXPIRY_FORMAT =
            new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

    private List<PantryItem> items;
    private final OnItemClickListener clickListener;
    private final OnItemLongClickListener longClickListener;

    public PantryAdapter(List<PantryItem> items,
                         OnItemClickListener clickListener,
                         OnItemLongClickListener longClickListener) {
        this.items = items;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
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
        holder.bind(items.get(position), clickListener, longClickListener);
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

        void bind(final PantryItem item,
                  final OnItemClickListener clickListener,
                  final OnItemLongClickListener longClickListener) {
            nameView.setText(item.getDisplayName());
            quantityView.setText(formatQuantity(item));

            bindExpiry(item);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onItemClick(item);
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (longClickListener == null) {
                        return false;
                    }
                    longClickListener.onItemLongClick(item);
                    // Returning true stops the tap listener also firing.
                    return true;
                }
            });
        }

        /**
         * Shows the expiry date, tinted when it is close or past, but only if
         * the user has left that preference switched on.
         */
        private void bindExpiry(PantryItem item) {
            if (item.getExpiryDate() == null) {
                expiryView.setVisibility(View.GONE);
                return;
            }

            android.content.Context context = itemView.getContext();
            expiryView.setVisibility(View.VISIBLE);

            long daysLeft = TimeUnit.MILLISECONDS.toDays(
                    item.getExpiryDate() - System.currentTimeMillis());
            String date = EXPIRY_FORMAT.format(new Date(item.getExpiryDate()));

            int colour = R.color.text_secondary;
            if (daysLeft < 0) {
                expiryView.setText(context.getString(R.string.expired_on, date));
                colour = R.color.expired;
            } else {
                expiryView.setText(context.getString(R.string.expires_on, date));
                if (daysLeft < Prefs.EXPIRING_SOON_DAYS) {
                    colour = R.color.expiring_soon;
                }
            }

            if (!Prefs.isHighlightExpiring(context)) {
                colour = R.color.text_secondary;
            }
            expiryView.setTextColor(ContextCompat.getColor(context, colour));
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
