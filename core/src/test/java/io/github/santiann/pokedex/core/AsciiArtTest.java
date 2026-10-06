package io.github.santiann.pokedex.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AsciiArtTest {

    @Test
    void todasAs151ArtesOriginaisEstaoPresentes() {
        IntStream.rangeClosed(1, Pokedex.TOTAL).forEach(id ->
                assertThat(AsciiArt.of(id))
                        .as("arte do #%03d", id)
                        .hasValueSatisfying(art -> assertThat(art.lines().count()).isGreaterThan(30)));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 152, -7})
    void naoExisteArteForaDaPrimeiraGeracao(int id) {
        assertThat(AsciiArt.of(id)).isEmpty();
    }
}
