package io.github.santiann.pokedex.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * As 151 artes ASCII feitas à mão na versão original do projeto (2022).
 * Cada uma fica em {@code /ascii/NNN.txt}, onde NNN é o número na Pokédex nacional.
 */
public final class AsciiArt {

    private AsciiArt() {}

    public static Optional<String> of(int id) {
        if (id < 1 || id > Pokedex.TOTAL) {
            return Optional.empty();
        }
        String path = "/ascii/%03d.txt".formatted(id);
        try (InputStream in = AsciiArt.class.getResourceAsStream(path)) {
            return in == null ? Optional.empty() : Optional.of(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler " + path, e);
        }
    }
}
