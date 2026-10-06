package io.github.santiann.pokedex.core;

/** Entrada da lista da Pokédex: só o número e o nome, sem buscar a ficha completa. */
public record PokemonSummary(int id, String name, String displayName) {}
