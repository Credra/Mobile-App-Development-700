package com.credra.smartpantrymanager.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.credra.smartpantrymanager.R;
import com.credra.smartpantrymanager.data.PantryDataSource;
import com.credra.smartpantrymanager.logic.UnitConverter;
import com.credra.smartpantrymanager.model.PantryItem;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adds a new pantry item or edits an existing one.
 *
 * The calling activity passes {@link #EXTRA_ITEM_ID}; a value of
 * {@link PantryItem#NEW_ITEM_ID} means this is a new item.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "pantry_item_id";

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

    private PantryDataSource dataSource;
    private PantryItem currentItem;

    private TextInputEditText nameInput;
    private TextInputEditText quantityInput;
    private TextInputEditText expiryInput;
    private Spinner unitSpinner;

    /** Null until the user picks a date, since expiry is optional. */
    private Long selectedExpiry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        nameInput = findViewById(R.id.nameInput);
        quantityInput = findViewById(R.id.quantityInput);
        expiryInput = findViewById(R.id.expiryInput);
        unitSpinner = findViewById(R.id.unitSpinner);

        setUpUnitSpinner();

        dataSource = new PantryDataSource(this);
        dataSource.open();

        long itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, PantryItem.NEW_ITEM_ID);
        if (itemId == PantryItem.NEW_ITEM_ID) {
            currentItem = new PantryItem();
            setTitle(R.string.title_add_ingredient);
        } else {
            currentItem = dataSource.getPantryItem(itemId);
            setTitle(R.string.title_edit_ingredient);
            if (currentItem == null) {
                // The row was deleted while this screen was opening.
                Toast.makeText(this, R.string.item_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            populateFields();
        }

        expiryInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        Button clearExpiry = findViewById(R.id.clearExpiryButton);
        clearExpiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedExpiry = null;
                expiryInput.setText("");
            }
        });

        Button save = findViewById(R.id.saveButton);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveItem();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataSource.close();
    }

    private void setUpUnitSpinner() {
        List<String> units = UnitConverter.selectableUnits();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, units);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(adapter);
    }

    /** Fills the form from the item being edited. */
    private void populateFields() {
        nameInput.setText(currentItem.getDisplayName());
        quantityInput.setText(formatQuantity(currentItem.getQuantity()));

        int unitPosition = UnitConverter.selectableUnits().indexOf(currentItem.getUnit());
        if (unitPosition >= 0) {
            unitSpinner.setSelection(unitPosition);
        }

        selectedExpiry = currentItem.getExpiryDate();
        if (selectedExpiry != null) {
            expiryInput.setText(DATE_FORMAT.format(new Date(selectedExpiry)));
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedExpiry != null) {
            calendar.setTimeInMillis(selectedExpiry);
        }

        DatePickerDialog dialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        Calendar picked = Calendar.getInstance();
                        picked.set(year, month, day, 0, 0, 0);
                        picked.set(Calendar.MILLISECOND, 0);
                        selectedExpiry = picked.getTimeInMillis();
                        expiryInput.setText(DATE_FORMAT.format(picked.getTime()));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveItem() {
        currentItem.setDisplayName(text(nameInput));
        currentItem.setQuantity(Double.parseDouble(text(quantityInput)));
        currentItem.setUnit((String) unitSpinner.getSelectedItem());
        currentItem.setExpiryDate(selectedExpiry);

        boolean saved = currentItem.isNew()
                ? dataSource.insertPantryItem(currentItem)
                : dataSource.updatePantryItem(currentItem);

        if (saved) {
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private String text(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    /** Shows "2" rather than "2.0" when the amount is a whole number. */
    private String formatQuantity(double quantity) {
        return quantity == Math.floor(quantity)
                ? String.valueOf((long) quantity)
                : String.valueOf(quantity);
    }

    /** Builds the intent the pantry list uses to open this screen. */
    public static Intent intentFor(android.content.Context context, long itemId) {
        Intent intent = new Intent(context, AddEditIngredientActivity.class);
        intent.putExtra(EXTRA_ITEM_ID, itemId);
        return intent;
    }
}
