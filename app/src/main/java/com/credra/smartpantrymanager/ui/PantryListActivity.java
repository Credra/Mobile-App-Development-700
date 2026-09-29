package com.credra.smartpantrymanager.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.credra.smartpantrymanager.R;
import com.credra.smartpantrymanager.data.PantryDataSource;
import com.credra.smartpantrymanager.model.PantryItem;
import com.credra.smartpantrymanager.ui.adapter.PantryAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

/**
 * The app's home screen: everything currently in the pantry, read from the
 * database into a RecyclerView.
 */
public class PantryListActivity extends AppCompatActivity
        implements PantryAdapter.OnItemClickListener, PantryAdapter.OnItemLongClickListener {

    private PantryDataSource dataSource;
    private PantryAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyStateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);
        setTitle(R.string.title_pantry);

        dataSource = new PantryDataSource(this);

        recyclerView = findViewById(R.id.pantryRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        adapter = new PantryAdapter(new ArrayList<PantryItem>(), this, this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton addButton = findViewById(R.id.addIngredientButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(AddEditIngredientActivity.intentFor(
                        PantryListActivity.this, PantryItem.NEW_ITEM_ID));
            }
        });

        setUpBottomNavigation();
    }

    private void setUpBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_pantry);
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_recipes) {
                    Intent intent = new Intent(PantryListActivity.this,
                            SuggestedRecipesActivity.class);
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
                    // Returning false leaves this screen's highlight on Pantry,
                    // which is where the user comes back to.
                    return false;
                }
                return item.getItemId() == R.id.nav_pantry;
            }
        });
    }

    /**
     * Reloading here rather than in onCreate means the list is correct after
     * returning from the add or edit screen.
     */
    @Override
    protected void onResume() {
        super.onResume();
        dataSource.open();
        loadPantryItems();

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_pantry);
    }

    @Override
    protected void onPause() {
        super.onPause();
        dataSource.close();
    }

    private void loadPantryItems() {
        List<PantryItem> items = dataSource.getAllPantryItems();
        adapter.setItems(items);

        boolean empty = items.isEmpty();
        emptyStateText.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    /** A tap opens the item for editing. */
    @Override
    public void onItemClick(PantryItem item) {
        startActivity(AddEditIngredientActivity.intentFor(this, item.getId()));
    }

    /** A long press offers to delete it, after confirming. */
    @Override
    public void onItemLongClick(final PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, item.getDisplayName()))
                .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteItem(item);
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void deleteItem(PantryItem item) {
        if (dataSource.deletePantryItem(item.getId())) {
            Toast.makeText(this, getString(R.string.deleted, item.getDisplayName()),
                    Toast.LENGTH_SHORT).show();
            loadPantryItems();
        } else {
            Toast.makeText(this, R.string.delete_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
