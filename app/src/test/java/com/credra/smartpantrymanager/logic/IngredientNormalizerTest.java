package com.credra.smartpantrymanager.logic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Tests for the name normalisation that lets "tomato" match "Tomatoes". */
public class IngredientNormalizerTest {

    @Test
    public void pluralAndSingularProduceTheSameKey() {
        assertEquals(IngredientNormalizer.canonical("tomato"),
                IngredientNormalizer.canonical("Tomatoes"));
    }

    @Test
    public void handlesCommonPluralEndings() {
        assertEquals("tomato", IngredientNormalizer.canonical("Tomatoes"));
        assertEquals("potato", IngredientNormalizer.canonical("Potatoes"));
        assertEquals("berry", IngredientNormalizer.canonical("Berries"));
        assertEquals("egg", IngredientNormalizer.canonical("Eggs"));
        assertEquals("onion", IngredientNormalizer.canonical("Onions"));
    }

    @Test
    public void singularisesChilliesToChilli() {
        assertEquals("chilli", IngredientNormalizer.canonical("Chillies"));
        assertEquals(IngredientNormalizer.canonical("chilli"),
                IngredientNormalizer.canonical("Chillies"));
    }

    @Test
    public void doesNotMangleWordsThatMerelyEndInS() {
        // These are not plurals. Stripping the trailing "s" would break matching.
        assertEquals("couscous", IngredientNormalizer.canonical("Couscous"));
        assertEquals("hummus", IngredientNormalizer.canonical("Hummus"));
    }

    @Test
    public void ignoresCaseWhitespaceAndPunctuation() {
        assertEquals("tomato", IngredientNormalizer.canonical("  TOMATO  "));
        assertEquals("tomato", IngredientNormalizer.canonical("Tomato!"));
    }

    @Test
    public void stripsPreparationAndSizeDescriptors() {
        assertEquals("garlic", IngredientNormalizer.canonical("finely chopped garlic"));
        assertEquals("tomato", IngredientNormalizer.canonical("Fresh Tomatoes"));
        assertEquals("tomato", IngredientNormalizer.canonical("tinned tomatoes"));
    }

    @Test
    public void mapsEverydayVariationsOntoTheRecipeName() {
        assertEquals("beef mince", IngredientNormalizer.canonical("Mince"));
        assertEquals("onion", IngredientNormalizer.canonical("Brown Onions"));
        assertEquals("flour", IngredientNormalizer.canonical("Cake Flour"));
        assertEquals("courgette", IngredientNormalizer.canonical("Baby Marrows"));
        assertEquals("courgette", IngredientNormalizer.canonical("Zucchini"));
    }

    @Test
    public void keepsMultiWordNamesIntact() {
        assertEquals("olive oil", IngredientNormalizer.canonical("Olive Oil"));
        assertEquals("beef mince", IngredientNormalizer.canonical("beef mince"));
    }

    @Test
    public void handlesNullAndEmptyInputSafely() {
        assertEquals("", IngredientNormalizer.canonical(null));
        assertEquals("", IngredientNormalizer.canonical("   "));
        assertEquals("", IngredientNormalizer.canonical("!!!"));
    }
}
