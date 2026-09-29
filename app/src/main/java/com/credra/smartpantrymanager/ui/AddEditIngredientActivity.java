package com.credra.smartpantrymanager.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.credra.smartpantrymanager.util.Validators;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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
    private TextInputLayout nameLayout;
    private TextInputLayout quantityLayout;
    private TextInputLayout expiryLayout;

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
        nameLayout = findViewById(R.id.nameLayout);
        quantityLayout = findViewById(R.id.quantityLayout);
        expiryLayout = findViewById(R.id.expiryLayout);

        // An error should disappear the moment the user starts fixing it,
        // rather than lingering until the next save attempt.
        nameInput.addTextChangedListener(new ClearErrorWatcher(nameLayout));
        quantityInput.addTextChangedListener(new ClearErrorWatcher(quantityLayout));

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
                expiryLayout.setError(null);
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
                        expiryLayout.setError(null);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveItem() {
        if (!isFormValid()) {
            return;
        }

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

    /**
     * Checks every field and shows the first problem against the field it
     * belongs to. Returns false without saving if anything is wrong.
     */
    private boolean isFormValid() {
        nameLayout.setError(null);
        quantityLayout.setError(null);
        expiryLayout.setError(null);
        boolean valid = true;

        Validators.NameError nameError = Validators.checkName(text(nameInput));
        if (nameError != null) {
            nameLayout.setError(getString(messageFor(nameError)));
            valid = false;
        }

        Validators.QuantityError quantityError = Validators.checkQuantity(text(quantityInput));
        if (quantityError != null) {
            quantityLayout.setError(getString(messageFor(quantityError)));
            valid = false;
        }

        if (Validators.isExpiryInThePast(selectedExpiry, startOfToday())) {
            expiryLayout.setError(getString(R.string.error_expiry_past));
            valid = false;
        }

        return valid;
    }

    private int messageFor(Validators.NameError error) {
        switch (error) {
            case EMPTY:
                return R.string.error_name_empty;
            case TOO_SHORT:
                return R.string.error_name_short;
            case TOO_LONG:
                return R.string.error_name_long;
            default:
                return R.string.error_name_letters;
        }
    }

    private int messageFor(Validators.QuantityError error) {
        switch (error) {
            case EMPTY:
                return R.string.error_quantity_empty;
            case NOT_A_NUMBER:
                return R.string.error_quantity_number;
            case NOT_POSITIVE:
                return R.string.error_quantity_positive;
            default:
                return R.string.error_quantity_large;
        }
    }

    /** Midnight today, so an expiry set for today still counts as valid. */
    private long startOfToday() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today.getTimeInMillis();
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

    /** Clears one field's error as soon as its text changes. */
    private static class ClearErrorWatcher implements TextWatcher {

        private final TextInputLayout layout;

        ClearErrorWatcher(TextInputLayout layout) {
            this.layout = layout;
        }

        @Override
        public void onTextChanged(CharSequence text, int start, int before, int count) {
            layout.setError(null);
        }

        @Override
        public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            // Not needed.
        }

        @Override
        public void afterTextChanged(Editable text) {
            // Not needed.
        }
    }
}
