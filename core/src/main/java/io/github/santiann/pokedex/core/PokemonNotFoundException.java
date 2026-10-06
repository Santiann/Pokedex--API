package io.github.santiann.pokedex.core;

public class PokemonNotFoundException extends RuntimeException {

    public PokemonNotFoundException(String query) {
        super("Nenhum Pokémon da 1ª geração encontrado para \"" + query + "\". Use um número de 1 a "
                + Pokedex.TOTAL + " ou um nome, como \"pikachu\".");
    }
}
