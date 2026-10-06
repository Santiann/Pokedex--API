package io.github.santiann.pokedex.cli;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.santiann.pokedex.core.Pokedex;
import io.github.santiann.pokedex.core.Pokemon;
import io.github.santiann.pokedex.core.PokemonNotFoundException;
import io.github.santiann.pokedex.core.PokemonSummary;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class PokedexCliTest {

    private static final Pokemon PIKACHU = new Pokemon(
            25, "pikachu", "Pikachu", "Mouse Pokémon",
            "When several of these POKéMON gather, their electricity could build and cause lightning storms.",
            List.of("electric"),
            List.of(new Pokemon.Stat("hp", 35), new Pokemon.Stat("attack", 55), new Pokemon.Stat("speed", 90)),
            List.of(new Pokemon.Ability("static", false), new Pokemon.Ability("lightning-rod", true)),
            List.of("thunder-shock", "quick-attack"),
            0.4, 6.0, null, null, null);

    /** Pokédex que só conhece o Pikachu e nunca acessa a rede. */
    private static final Pokedex OFFLINE = new Pokedex() {
        @Override
        public Pokemon find(String query) {
            if (query.strip().equalsIgnoreCase("pikachu") || query.strip().equals("25")) {
                return PIKACHU;
            }
            throw new PokemonNotFoundException(query.strip());
        }

        @Override
        public List<PokemonSummary> summaries() {
            return List.of(new PokemonSummary(25, "pikachu", "Pikachu"), new PokemonSummary(122, "mr-mime", "Mr. Mime"));
        }
    };

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final PokedexCli cli = new PokedexCli(OFFLINE, new Ansi(false), new PrintStream(buffer, true, StandardCharsets.UTF_8));

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void mostraAFichaComArteOriginalTiposEStatus() {
        assertThat(cli.showAll("pikachu")).isZero();

        assertThat(output())
                .contains("#025  PIKACHU", "Mouse Pokémon", "[Elétrico]")
                .contains("Altura 0.4 m", "Peso 6.0 kg")
                .contains("When several of these POKéMON gather")
                .containsPattern("HP +35 █+░+")
                .contains("Velocidade").containsPattern("Total +180")
                .contains("Static · Lightning Rod (oculta)")
                .contains("Golpes (2)", "Thunder Shock, Quick Attack")
                .contains("@@@@@@@@@@");
    }

    @Test
    void devolveCodigoDeErroQuandoNaoEncontra() {
        assertThat(cli.showAll("pikachu", "agumon")).isEqualTo(1);
        assertThat(output()).contains("✗ Nenhum Pokémon da 1ª geração encontrado para \"agumon\"");
    }

    @Test
    void modoInterativoAceitaComandosEAteNomesErrados() {
        cli.interactive(new BufferedReader(new StringReader("""
                lista
                digimon
                25
                sair
                """)));

        assertThat(output())
                .contains("#025 Pikachu", "#122 Mr. Mime")
                .contains("✗ Nenhum Pokémon da 1ª geração encontrado para \"digimon\"")
                .contains("#025  PIKACHU")
                .endsWith("Até a próxima, treinador!\n");
    }

    @Test
    void encerraSemErroQuandoAEntradaAcaba() {
        cli.interactive(new BufferedReader(new StringReader("")));

        assertThat(output()).contains("Digite um número");
    }

    @Test
    void quebraTextoLongoNaLargura() {
        assertThat(CardRenderer.wrap("um dois três quatro cinco", 10)).containsExactly("um dois", "três", "quatro", "cinco");
    }
}
