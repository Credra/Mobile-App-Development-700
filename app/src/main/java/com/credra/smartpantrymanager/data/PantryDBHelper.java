package com.credra.smartpantrymanager.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Creates and upgrades the SQLite database. Its only job is the schema; all
 * queries live in {@link PantryDataSource}.
 */
public class PantryDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "PantryDBHelper";

    private static final String DATABASE_NAME = "smartpantry.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_PANTRY = "pantry_item";
    public static final String TABLE_RECIPE = "recipe";
    public static final String TABLE_RECIPE_INGREDIENT = "recipe_ingredient";

    // Shared column names. Pantry items and recipe ingredients deliberately use
    // the same names so both can be read with the same cursor code.
    public static final String COL_ID = "_id";
    public static final String COL_DISPLAY_NAME = "display_name";
    public static final String COL_CANONICAL_NAME = "canonical_name";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_UNIT = "unit";
    public static final String COL_BASE_UNIT = "base_unit";
    public static final String COL_BASE_QUANTITY = "base_quantity";

    public static final String COL_EXPIRY_DATE = "expiry_date";
    public static final String COL_DATE_ADDED = "date_added";

    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_CATEGORY = "category";
    public static final String COL_SERVINGS = "servings";
    public static final String COL_PREP_MINUTES = "prep_minutes";
    public static final String COL_METHOD = "method";
    public static final String COL_RECIPE_ID = "recipe_id";

    private static final String CREATE_TABLE_PANTRY =
            "CREATE TABLE " + TABLE_PANTRY + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_DISPLAY_NAME + " TEXT NOT NULL, "
                    + COL_CANONICAL_NAME + " TEXT NOT NULL, "
                    + COL_QUANTITY + " REAL NOT NULL, "
                    + COL_UNIT + " TEXT NOT NULL, "
                    + COL_BASE_UNIT + " TEXT NOT NULL, "
                    + COL_BASE_QUANTITY + " REAL NOT NULL, "
                    + COL_EXPIRY_DATE + " INTEGER, "
                    + COL_DATE_ADDED + " INTEGER NOT NULL)";

    private static final String CREATE_TABLE_RECIPE =
            "CREATE TABLE " + TABLE_RECIPE + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_RECIPE_NAME + " TEXT NOT NULL, "
                    + COL_CATEGORY + " TEXT, "
                    + COL_SERVINGS + " INTEGER, "
                    + COL_PREP_MINUTES + " INTEGER, "
                    + COL_METHOD + " TEXT NOT NULL)";

    private static final String CREATE_TABLE_RECIPE_INGREDIENT =
            "CREATE TABLE " + TABLE_RECIPE_INGREDIENT + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_RECIPE_ID + " INTEGER NOT NULL, "
                    + COL_DISPLAY_NAME + " TEXT NOT NULL, "
                    + COL_CANONICAL_NAME + " TEXT NOT NULL, "
                    + COL_QUANTITY + " REAL NOT NULL, "
                    + COL_UNIT + " TEXT NOT NULL, "
                    + COL_BASE_UNIT + " TEXT NOT NULL, "
                    + COL_BASE_QUANTITY + " REAL NOT NULL, "
                    + "FOREIGN KEY (" + COL_RECIPE_ID + ") REFERENCES "
                    + TABLE_RECIPE + "(" + COL_ID + ") ON DELETE CASCADE)";

    // Matching looks ingredients up by canonical name, so both tables are indexed on it.
    private static final String CREATE_INDEX_PANTRY_NAME =
            "CREATE INDEX idx_pantry_canonical ON " + TABLE_PANTRY + "(" + COL_CANONICAL_NAME + ")";

    private static final String CREATE_INDEX_INGREDIENT_RECIPE =
            "CREATE INDEX idx_ingredient_recipe ON " + TABLE_RECIPE_INGREDIENT + "(" + COL_RECIPE_ID + ")";

    private final Context context;

    public PantryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PANTRY);
        db.execSQL(CREATE_TABLE_RECIPE);
        db.execSQL(CREATE_TABLE_RECIPE_INGREDIENT);
        db.execSQL(CREATE_INDEX_PANTRY_NAME);
        db.execSQL(CREATE_INDEX_INGREDIENT_RECIPE);

        // The recipe collection ships with the app, so it is loaded here rather
        // than leaving the user with an empty Suggested Recipes screen.
        RecipeSeeder.seed(context, db);
    }

    /**
     * Rebuilds the recipe collection, which ships with the app, and leaves the
     * pantry alone because that is the user's own data.
     *
     * Without this the seeded recipes would be frozen at whatever shipped when
     * the database was first created, since onCreate only runs once.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE);
        db.execSQL(CREATE_TABLE_RECIPE);
        db.execSQL(CREATE_TABLE_RECIPE_INGREDIENT);
        db.execSQL(CREATE_INDEX_INGREDIENT_RECIPE);
        RecipeSeeder.seed(context, db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Needed for the ON DELETE CASCADE on recipe_ingredient to take effect.
        db.setForeignKeyConstraintsEnabled(true);
    }
}
