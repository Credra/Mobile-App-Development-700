package com.credra.smartpantrymanager.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.credra.smartpantrymanager.R;
import com.credra.smartpantrymanager.data.PantryDataSource;
import com.credra.smartpantrymanager.model.PantryItem;
import com.credra.smartpantrymanager.ui.adapter.PantryAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * The app's home screen: everything currently in the pantry, read from the
 * database into a RecyclerView.
 */
public class PantryListActivity extends AppCompatActivity
        implements PantryAdapter.OnItemClickListener {

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

        adapter = new PantryAdapter(new ArrayList<PantryItem>(), this);
        recyclerView.setAdapter(adapter);

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_pantry);
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

    @Override
    public void onItemClick(PantryItem item) {
        // Opens the edit screen once AddEditIngredientActivity exists.
    }
}
