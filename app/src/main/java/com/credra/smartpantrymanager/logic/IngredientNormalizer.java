package com.credra.smartpantrymanager.logic;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Turns a free-text ingredient name into a canonical key, so that "Tomatoes"
 * and "tomato" compare equal.
 *
 * Cleans the text, drops descriptor words, singularises the head noun, then maps
 * synonyms. The output need not be a real word, only consistent, since both the
 * pantry and the recipe are normalised the same way.
 */
public final class IngredientNormalizer {

    private IngredientNormalizer() {
        // Utility class; not meant to be instantiated.
    }

    /** Words describing preparation, size or packaging rather than identity. */
    private static final Set<String> DESCRIPTORS = new HashSet<>(Arrays.asList(
            "fresh", "frozen", "dried", "chopped", "diced", "sliced", "minced",
            "grated", "crushed", "large", "medium", "small", "ripe", "raw",
            "cooked", "finely", "roughly", "whole", "organic", "tinned",
            "canned", "plain", "unsalted", "salted", "free", "range"
    ));

    /**
     * Everyday variations mapped onto the name the recipes use. Kept deliberately
     * small: only the ingredients this app actually ships with need an entry.
     * Keys are singular because aliasing runs after the plural rules.
     */
    private static final Map<String, String> ALIASES;

    static {
        Map<String, String> m = new HashMap<>();
        m.put("mince", "beef mince");
        m.put("brown onion", "onion");
        m.put("cake flour", "flour");
        m.put("baby marrow", "courgette");
        m.put("zucchini", "courgette");
        ALIASES = Collections.unmodifiableMap(m);
    }

    /** Reduces a raw ingredient name to its canonical key, or "" if unusable. */
    public static String canonical(String raw) {
        if (raw == null) {
            return "";
        }

        String cleaned = stripAccents(raw)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (cleaned.isEmpty()) {
            return "";
        }

        // Never strip everything: an entry of just "fresh" must not vanish.
        String[] words = cleaned.split(" ");
        StringBuilder kept = new StringBuilder();
        for (String word : words) {
            if (!DESCRIPTORS.contains(word)) {
                if (kept.length() > 0) {
                    kept.append(" ");
                }
                kept.append(word);
            }
        }
        String withoutDescriptors = kept.length() == 0 ? cleaned : kept.toString();

        // Only the final word is the head noun: "green onions" -> "green onion".
        int lastSpace = withoutDescriptors.lastIndexOf(" ");
        String singular;
        if (lastSpace == -1) {
            singular = singularise(withoutDescriptors);
        } else {
            singular = withoutDescriptors.substring(0, lastSpace + 1)
                    + singularise(withoutDescriptors.substring(lastSpace + 1));
        }

        String alias = ALIASES.get(singular);
        return alias != null ? alias : singular;
    }

    /**
     * Plural rules, deliberately conservative: words ending in "ss", "us" or
     * "is" are left alone so "couscous" and "hummus" survive intact.
     */
    static String singularise(String word) {
        if (word.length() <= 3) {
            return word;
        }
        if (word.endsWith("llies")) {
            // "chillies" is chilli plus es, not chilly plus ies. Without this the
            // rule below would produce "chilly", which never matches "chilli".
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("ies") && word.length() > 4) {
            // berries -> berry
            return word.substring(0, word.length() - 3) + "y";
        }
        if (word.endsWith("oes") && word.length() > 4) {
            // tomatoes -> tomato
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("shes") || word.endsWith("ches")
                || word.endsWith("xes") || word.endsWith("zes")) {
            // dishes -> dish
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("ss") || word.endsWith("us") || word.endsWith("is")) {
            // couscous, hummus, glass
            return word;
        }
        if (word.endsWith("s")) {
            // eggs -> egg
            return word.substring(0, word.length() - 1);
        }
        return word;
    }

    private static String stripAccents(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}
