package io.github.santiann.pokedex.core;

import java.util.List;

/**
 * Ficha completa de um Pokémon da 1ª geração.
 *
 * @param id          número na Pokédex nacional (1 a 151)
 * @param name        nome como a PokeAPI usa, por exemplo {@code mr-mime}
 * @param displayName nome para exibição, por exemplo {@code Mr. Mime}
 * @param genus       categoria, por exemplo {@code Mouse Pokémon}
 * @param description texto da Pokédex de Pokémon Red
 * @param heightM     altura em metros
 * @param weightKg    peso em quilos
 */
public record Pokemon(
        int id,
        String name,
        String displayName,
        String genus,
        String description,
        List<String> types,
        List<Stat> stats,
        List<Ability> abilities,
        List<String> moves,
        double heightM,
        double weightKg,
        String spriteUrl,
        String artworkUrl,
        String cryUrl) {

    public Pokemon {
        types = List.copyOf(types);
        stats = List.copyOf(stats);
        abilities = List.copyOf(abilities);
        moves = List.copyOf(moves);
    }

    public int totalStats() {
        return stats.stream().mapToInt(Stat::base).sum();
    }

    public record Stat(String name, int base) {}

    public record Ability(String name, boolean hidden) {}
}
