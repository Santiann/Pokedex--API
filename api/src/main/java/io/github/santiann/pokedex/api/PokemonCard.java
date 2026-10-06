package io.github.santiann.pokedex.api;

import io.github.santiann.pokedex.core.Pokemon;
import java.util.List;

/** Versão enxuta da ficha, usada na listagem. */
public record PokemonCard(int id, String name, String displayName, List<String> types, String artworkUrl, String spriteUrl) {

    static PokemonCard of(Pokemon p) {
        return new PokemonCard(p.id(), p.name(), p.displayName(), p.types(), p.artworkUrl(), p.spriteUrl());
    }
}
