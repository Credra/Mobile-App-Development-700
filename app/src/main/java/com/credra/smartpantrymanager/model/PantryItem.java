package com.credra.smartpantrymanager.model;

/**
 * An ingredient the user currently has at home.
 *
 * Stores both the values the user typed ("Tomatoes", 2, "kg") and the canonical
 * values used for matching ("tomato", 2000, "g"), computed once on save.
 */
public class PantryItem {

    /** Marker for an item that has not been written to the database yet. */
    public static final long NEW_ITEM_ID = -1;

    private long id;
    private String displayName;
    private String canonicalName;
    private double quantity;
    private String unit;
    private String baseUnit;
    private double baseQuantity;
    private Long expiryDate;   // epoch millis; null means "no expiry tracked"
    private long dateAdded;

    public PantryItem() {
        this.id = NEW_ITEM_ID;
        this.dateAdded = System.currentTimeMillis();
    }

    public boolean isNew() {
        return id == NEW_ITEM_ID;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getCanonicalName() { return canonicalName; }
    public void setCanonicalName(String canonicalName) { this.canonicalName = canonicalName; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getBaseUnit() { return baseUnit; }
    public void setBaseUnit(String baseUnit) { this.baseUnit = baseUnit; }

    public double getBaseQuantity() { return baseQuantity; }
    public void setBaseQuantity(double baseQuantity) { this.baseQuantity = baseQuantity; }

    public Long getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Long expiryDate) { this.expiryDate = expiryDate; }

    public long getDateAdded() { return dateAdded; }
    public void setDateAdded(long dateAdded) { this.dateAdded = dateAdded; }

    /** Key used to group pantry entries that refer to the same thing in the same dimension. */
    public String matchKey() {
        return canonicalName + "|" + baseUnit;
    }
}
