package io.github.santiann.pokedex.api;

import io.github.santiann.pokedex.core.AsciiArt;
import io.github.santiann.pokedex.core.Pokedex;
import io.github.santiann.pokedex.core.Pokemon;
import io.github.santiann.pokedex.core.PokemonNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pokemon")
@Tag(name = "Pokémon")
class PokemonController {

    /** Os dados da 1ª geração não mudam, então o navegador e a CDN podem guardar por um dia. */
    private static final CacheControl CACHE = CacheControl.maxAge(Duration.ofDays(1)).cachePublic();

    private final Pokedex pokedex;

    PokemonController(Pokedex pokedex) {
        this.pokedex = pokedex;
    }

    @GetMapping
    @Operation(summary = "Lista os 151 Pokémon com tipos e imagem")
    ResponseEntity<List<PokemonCard>> list() {
        return ResponseEntity.ok().cacheControl(CACHE).body(pokedex.findAll().stream().map(PokemonCard::of).toList());
    }

    @GetMapping("/{query}")
    @Operation(summary = "Ficha completa de um Pokémon")
    ResponseEntity<Pokemon> find(@Parameter(description = "Número (1 a 151) ou nome", example = "pikachu") @PathVariable String query) {
        return ResponseEntity.ok().cacheControl(CACHE).body(pokedex.find(query));
    }

    @GetMapping(value = "/{id}/ascii", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Arte ASCII original, feita à mão", description = "Experimente no terminal: curl <host>/api/pokemon/25/ascii")
    ResponseEntity<String> ascii(@Parameter(description = "Número de 1 a 151", example = "25") @PathVariable int id) {
        String art = AsciiArt.of(id).orElseThrow(() -> new PokemonNotFoundException(String.valueOf(id)));
        return ResponseEntity.ok().cacheControl(CACHE).contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8)).body(art);
    }
}
