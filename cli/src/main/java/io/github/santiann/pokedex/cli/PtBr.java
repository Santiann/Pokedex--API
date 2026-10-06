package io.github.santiann.pokedex.cli;

import java.util.Map;

/** Nomes em português e cores oficiais dos tipos e status. */
final class PtBr {

    private record Type(String label, int color) {}

    private static final Map<String, Type> TYPES = Map.ofEntries(
            Map.entry("normal", new Type("Normal", 0xA8A77A)),
            Map.entry("fire", new Type("Fogo", 0xEE8130)),
            Map.entry("water", new Type("Água", 0x6390F0)),
            Map.entry("electric", new Type("Elétrico", 0xF7D02C)),
            Map.entry("grass", new Type("Planta", 0x7AC74C)),
            Map.entry("ice", new Type("Gelo", 0x96D9D6)),
            Map.entry("fighting", new Type("Lutador", 0xC22E28)),
            Map.entry("poison", new Type("Venenoso", 0xA33EA1)),
            Map.entry("ground", new Type("Terrestre", 0xE2BF65)),
            Map.entry("flying", new Type("Voador", 0xA98FF3)),
            Map.entry("psychic", new Type("Psíquico", 0xF95587)),
            Map.entry("bug", new Type("Inseto", 0xA6B91A)),
            Map.entry("rock", new Type("Pedra", 0xB6A136)),
            Map.entry("ghost", new Type("Fantasma", 0x735797)),
            Map.entry("dragon", new Type("Dragão", 0x6F35FC)),
            Map.entry("dark", new Type("Sombrio", 0x705746)),
            Map.entry("steel", new Type("Aço", 0xB7B7CE)),
            Map.entry("fairy", new Type("Fada", 0xD685AD)));

    private static final Map<String, String> STATS = Map.of(
            "hp", "HP",
            "attack", "Ataque",
            "defense", "Defesa",
            "special-attack", "Atq. Esp.",
            "special-defense", "Def. Esp.",
            "speed", "Velocidade");

    private PtBr() {}

    static String type(String type) {
        Type t = TYPES.get(type);
        return t == null ? type : t.label();
    }

    static int typeColor(String type) {
        Type t = TYPES.get(type);
        return t == null ? 0xA8A77A : t.color();
    }

    static String stat(String stat) {
        return STATS.getOrDefault(stat, stat);
    }

    /** {@code lightning-rod} vira {@code Lightning Rod}. */
    static String title(String apiName) {
        StringBuilder out = new StringBuilder();
        for (String word : apiName.split("-")) {
            if (!word.isEmpty()) {
                out.append(out.isEmpty() ? "" : " ").append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
        }
        return out.toString();
    }
}
