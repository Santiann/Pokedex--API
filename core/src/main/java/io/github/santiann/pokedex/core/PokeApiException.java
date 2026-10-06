package io.github.santiann.pokedex.core;

/** A PokeAPI não respondeu ou respondeu algo que não dá para usar. */
public class PokeApiException extends RuntimeException {

    public PokeApiException(String message) {
        super(message);
    }

    public PokeApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
