package io.github.santiann.pokedex.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NamesTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "Pikachu       | pikachu",
            "' MEW '       | mew",
            "Mr. Mime      | mr-mime",
            "mr mime       | mr-mime",
            "Nidoran♀      | nidoran-f",
            "nidoran ♂     | nidoran-m",
            "Farfetch'd    | farfetchd",
    })
    void normalizaOQueAPessoaDigita(String input, String expected) {
        assertThat(Names.normalize(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "bulbasaur | Bulbasaur",
            "mr-mime   | Mr. Mime",
            "nidoran-m | Nidoran♂",
            "farfetchd | Farfetch'd",
    })
    void geraNomeDeExibicao(String apiName, String expected) {
        assertThat(Names.display(apiName)).isEqualTo(expected);
    }
}
