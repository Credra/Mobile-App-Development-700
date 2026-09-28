# Smart Pantry Manager

An Android app in Java that suggests recipes you can cook using strictly the
ingredients already in your pantry, to help cut food waste.

Mobile App Development 700, Richfield Graduate Institute of Technology.

## Strict matching

A recipe is only suggested when **every** ingredient it needs is in the pantry in
at least the required quantity. Four ingredients out of five means the recipe does
not appear.

Matching tolerates ordinary differences: `Tomatoes` matches `tomato`, `Beef Mince`
matches `ground beef`, and `1 kg` satisfies `400 g`. It will not match across
incompatible units, so `1 unit` of flour does not satisfy `500 g` of flour.

## Database: SQLite

Used via `SQLiteOpenHelper` and a data source class, rather than Firebase or
PostgreSQL:

- A pantry is private to one person on one phone, so there is nothing to sync.
- The app works with no signal, which matters in a kitchen.
- Matching reads every recipe, which is instant locally and slow over a network.
- Firebase would add accounts and offline caching; PostgreSQL would need a hosted
  REST API a marker could not run offline.

## Setup

Requires Android Studio, SDK Platform 37, and a device running Android 7.0 (API 24)
or higher.

1. `git clone https://github.com/Credra/Mobile-App-Development-700.git`
2. Open in Android Studio and let Gradle sync.
3. Connect a device with USB debugging enabled, or start an emulator.
4. Press **Run**. Recipes seed themselves on first launch.

Run the unit tests with `gradlew test` — no device needed.

## Structure

```
app/src/main/java/com/credra/smartpantrymanager/
├── model/   PantryItem, Recipe, RecipeIngredient, MatchResult
├── logic/   IngredientNormalizer, UnitConverter, RecipeMatcher
├── data/    PantryDBHelper, PantryDataSource, RecipeSeeder
└── ui/      Activities and adapters
```

`logic` has no Android dependencies, so the matching rule is unit tested on the JVM.

## Author

Craig Ransom
