package io.github.santiann.pokedex.core;

import java.util.Locale;
import java.util.Map;

/** Conversão entre os nomes da PokeAPI ({@code nidoran-f}) e os nomes de exibição ({@code Nidoran♀}). */
public final class Names {

    private static final Map<String, String> SPECIAL = Map.of(
            "nidoran-f", "Nidoran♀",
            "nidoran-m", "Nidoran♂",
            "mr-mime", "Mr. Mime",
            "farfetchd", "Farfetch'd");

    private Names() {}

    public static String display(String apiName) {
        String special = SPECIAL.get(apiName);
        if (special != null) {
            return special;
        }
        return apiName.isEmpty() ? apiName : Character.toUpperCase(apiName.charAt(0)) + apiName.substring(1);
    }

    /** Transforma o que a pessoa digitou ({@code " Mr. Mime "}, {@code "Nidoran♀"}) no nome da PokeAPI. */
    public static String normalize(String input) {
        return input.strip()
                .toLowerCase(Locale.ROOT)
                .replace("♀", "-f")
                .replace("♂", "-m")
                .replace("'", "")
                .replaceAll("[\\s.]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
    }
}
