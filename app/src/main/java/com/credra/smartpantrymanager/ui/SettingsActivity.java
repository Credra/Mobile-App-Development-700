package com.credra.smartpantrymanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.credra.smartpantrymanager.R;
import com.credra.smartpantrymanager.logic.UnitConverter;
import com.credra.smartpantrymanager.util.Prefs;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

/**
 * User preferences, stored with SharedPreferences rather than in the database
 * because they are small, single-valued, and not the user's own data.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.title_settings);

        setUpExpiringSwitch();
        setUpDefaultUnitSpinner();
        setUpBottomNavigation();
    }

    private void setUpExpiringSwitch() {
        SwitchMaterial toggle = findViewById(R.id.highlightExpiringSwitch);
        toggle.setChecked(Prefs.isHighlightExpiring(this));
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean checked) {
                Prefs.setHighlightExpiring(SettingsActivity.this, checked);
            }
        });
    }

    private void setUpDefaultUnitSpinner() {
        final List<String> units = UnitConverter.selectableUnits();
        Spinner spinner = findViewById(R.id.defaultUnitSpinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, units);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        int current = units.indexOf(Prefs.getDefaultUnit(this));
        if (current >= 0) {
            spinner.setSelection(current);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Prefs.setDefaultUnit(SettingsActivity.this, units.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing to do; the stored value stays as it was.
            }
        });
    }

    private void setUpBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_settings);
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_settings) {
                    return true;
                }
                Class<?> target = item.getItemId() == R.id.nav_recipes
                        ? SuggestedRecipesActivity.class
                        : PantryListActivity.class;
                Intent intent = new Intent(SettingsActivity.this, target);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_settings);
    }
}
