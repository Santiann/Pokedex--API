package io.github.santiann.pokedex.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PokedexTest {

    private FakePokeApi api;
    private Pokedex pokedex;

    @BeforeEach
    void setUp() {
        api = new FakePokeApi();
        pokedex = new Pokedex(new PokeApiClient(api.baseUri()));
    }

    @AfterEach
    void tearDown() {
        api.close();
    }

    @Test
    void montaAFichaCompletaAPartirDaPokeApi() {
        Pokemon pikachu = pokedex.find(25);

        assertThat(pikachu.id()).isEqualTo(25);
        assertThat(pikachu.displayName()).isEqualTo("Pikachu");
        assertThat(pikachu.genus()).isEqualTo("Mouse Pokémon");
        assertThat(pikachu.description()).startsWith("When several of these POKéMON gather").doesNotContain("\n", "\f");
        assertThat(pikachu.types()).containsExactly("electric");
        assertThat(pikachu.stats()).contains(new Pokemon.Stat("hp", 35), new Pokemon.Stat("speed", 90));
        assertThat(pikachu.totalStats()).isEqualTo(320);
        assertThat(pikachu.abilities()).containsExactly(
                new Pokemon.Ability("static", false), new Pokemon.Ability("lightning-rod", true));
        assertThat(pikachu.moves()).hasSize(4).doesNotHaveDuplicates();
        assertThat(pikachu.heightM()).isEqualTo(0.4);
        assertThat(pikachu.weightKg()).isEqualTo(6.0);
        assertThat(pikachu.artworkUrl()).endsWith("/official-artwork/25.png");
        assertThat(pikachu.cryUrl()).contains("/legacy/");
    }

    @Test
    void guardaCacheParaNaoRepetirRequisicoes() {
        pokedex.find(25);
        pokedex.find("pikachu");
        pokedex.find("#025");

        assertThat(api.hits("pokemon/25")).isEqualTo(1);
        assertThat(api.hits("pokemon-species/25")).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"25", "#25", "#025", "pikachu", "  PIKACHU  ", "Pikachu"})
    void encontraPorNumeroOuNome(String query) {
        assertThat(pokedex.find(query).id()).isEqualTo(25);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 152, 9999})
    void recusaNumerosForaDaPrimeiraGeracaoSemChamarAApi(int id) {
        assertThatThrownBy(() -> pokedex.find(id)).isInstanceOf(PokemonNotFoundException.class);
        assertThat(api.hits("pokemon/" + id)).isZero();
    }

    @Test
    void recusaNomesQueNaoSaoDaPrimeiraGeracao() {
        assertThatThrownBy(() -> pokedex.find("agumon"))
                .isInstanceOf(PokemonNotFoundException.class)
                .hasMessageContaining("agumon");
    }

    @Test
    void listaComNomesDeExibicao() {
        assertThat(pokedex.summaries()).containsExactly(
                new PokemonSummary(1, "bulbasaur", "Bulbasaur"),
                new PokemonSummary(25, "pikachu", "Pikachu"),
                new PokemonSummary(29, "nidoran-f", "Nidoran♀"),
                new PokemonSummary(122, "mr-mime", "Mr. Mime"));
    }

    @Test
    void tentaDeNovoQuandoAPokeApiFalhaUmaVez() {
        api.failNext("pokemon/25", 1);

        assertThat(pokedex.find(25).name()).isEqualTo("pikachu");
        assertThat(api.hits("pokemon/25")).isEqualTo(2);
    }

    @Test
    void desisteDepoisDeTresFalhas() {
        api.failNext("pokemon/25", 3);

        assertThatThrownBy(() -> pokedex.find(25))
                .isInstanceOf(PokeApiException.class)
                .hasMessageContaining("500");
    }
}
