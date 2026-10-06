package io.github.santiann.pokedex.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import io.github.santiann.pokedex.core.PokeApiException;
import io.github.santiann.pokedex.core.Pokedex;
import io.github.santiann.pokedex.core.Pokemon;
import io.github.santiann.pokedex.core.PokemonNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest
class PokemonControllerTest {

    private static final Pokemon PIKACHU = new Pokemon(
            25, "pikachu", "Pikachu", "Mouse Pokémon", "When several of these POKéMON gather...",
            List.of("electric"),
            List.of(new Pokemon.Stat("hp", 35), new Pokemon.Stat("speed", 90)),
            List.of(new Pokemon.Ability("static", false)),
            List.of("thunder-shock"),
            0.4, 6.0, "https://img/sprite.png", "https://img/artwork.png", "https://snd/cry.ogg");

    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    Pokedex pokedex;

    @Test
    void listaOsPokemonComTiposEImagem() {
        given(pokedex.findAll()).willReturn(List.of(PIKACHU));

        assertThat(mvc.get().uri("/api/pokemon"))
                .hasStatusOk()
                .hasHeader("Cache-Control", "max-age=86400, public")
                .bodyJson()
                .isLenientlyEqualTo("""
                        [{"id": 25, "name": "pikachu", "displayName": "Pikachu", "types": ["electric"],
                          "artworkUrl": "https://img/artwork.png", "spriteUrl": "https://img/sprite.png"}]
                        """);
    }

    @Test
    void devolveAFichaCompleta() {
        given(pokedex.find("pikachu")).willReturn(PIKACHU);

        assertThat(mvc.get().uri("/api/pokemon/pikachu"))
                .hasStatusOk()
                .bodyJson()
                .isLenientlyEqualTo("""
                        {"id": 25, "genus": "Mouse Pokémon", "heightM": 0.4, "weightKg": 6.0,
                         "stats": [{"name": "hp", "base": 35}, {"name": "speed", "base": 90}],
                         "abilities": [{"name": "static", "hidden": false}],
                         "cryUrl": "https://snd/cry.ogg"}
                        """);
    }

    @Test
    void respondeProblemDetail404QuandoNaoEncontra() {
        given(pokedex.find("agumon")).willThrow(new PokemonNotFoundException("agumon"));

        assertThat(mvc.get().uri("/api/pokemon/agumon"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .extractingPath("$.title").isEqualTo("Pokémon não encontrado");
    }

    @Test
    void respondeBadGatewayQuandoAPokeApiCai() {
        given(pokedex.findAll()).willThrow(new PokeApiException("timeout"));

        assertThat(mvc.get().uri("/api/pokemon"))
                .hasStatus(HttpStatus.BAD_GATEWAY)
                .bodyJson()
                .extractingPath("$.title").isEqualTo("PokeAPI indisponível");
    }

    @Test
    void serveAArteAsciiOriginalEmTextoPuro() {
        assertThat(mvc.get().uri("/api/pokemon/25/ascii"))
                .hasStatusOk()
                .hasContentType("text/plain;charset=UTF-8")
                .bodyText()
                .contains("@@@@@@@@@@");
    }

    @Test
    void arteAsciiForaDaPrimeiraGeracaoDa404() {
        assertThat(mvc.get().uri("/api/pokemon/152/ascii")).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(mvc.get().uri("/api/pokemon/abc/ascii")).hasStatus(HttpStatus.BAD_REQUEST);
    }
}
