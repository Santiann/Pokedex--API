package io.github.santiann.pokedex.core;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Cliente HTTP da <a href="https://pokeapi.co">PokeAPI</a>. Não guarda cache: isso é papel do {@link Pokedex}. */
public class PokeApiClient {

    public static final URI DEFAULT_BASE_URI = URI.create("https://pokeapi.co/api/v2/");

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final int MAX_ATTEMPTS = 3;

    private final URI baseUri;
    private final HttpClient http;
    private final JsonMapper json = JsonMapper.builder().build();

    public PokeApiClient() {
        this(DEFAULT_BASE_URI);
    }

    public PokeApiClient(URI baseUri) {
        this.baseUri = baseUri.toString().endsWith("/") ? baseUri : URI.create(baseUri + "/");
        this.http = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public List<PokemonSummary> fetchSummaries(int limit) {
        JsonNode results = getAsync("pokemon?limit=" + limit).join().path("results");
        List<PokemonSummary> summaries = new ArrayList<>(results.size());
        for (JsonNode entry : results) {
            String name = entry.path("name").asString();
            summaries.add(new PokemonSummary(idFromUrl(entry.path("url").asString()), name, Names.display(name)));
        }
        return summaries;
    }

    public Pokemon fetchPokemon(int id) {
        CompletableFuture<JsonNode> pokemon = getAsync("pokemon/" + id);
        CompletableFuture<JsonNode> species = getAsync("pokemon-species/" + id);
        try {
            return toPokemon(pokemon.join(), species.join());
        } catch (CompletionException e) {
            throw e.getCause() instanceof RuntimeException cause ? cause : e;
        }
    }

    private Pokemon toPokemon(JsonNode p, JsonNode species) {
        String name = p.path("name").asString();

        List<String> types = new ArrayList<>();
        p.path("types").forEach(t -> types.add(t.path("type").path("name").asString()));

        List<Pokemon.Stat> stats = new ArrayList<>();
        p.path("stats").forEach(s -> stats.add(new Pokemon.Stat(s.path("stat").path("name").asString(), s.path("base_stat").asInt())));

        List<Pokemon.Ability> abilities = new ArrayList<>();
        p.path("abilities").forEach(a -> abilities.add(new Pokemon.Ability(a.path("ability").path("name").asString(), a.path("is_hidden").asBoolean())));

        Set<String> moves = new LinkedHashSet<>();
        p.path("moves").forEach(m -> moves.add(m.path("move").path("name").asString()));

        JsonNode sprites = p.path("sprites");
        String legacyCry = textOrNull(p.path("cries").path("legacy"));

        return new Pokemon(
                p.path("id").asInt(),
                name,
                Names.display(name),
                englishGenus(species),
                description(species),
                types,
                stats,
                abilities,
                List.copyOf(moves),
                p.path("height").asInt() / 10.0,
                p.path("weight").asInt() / 10.0,
                textOrNull(sprites.path("front_default")),
                textOrNull(sprites.path("other").path("official-artwork").path("front_default")),
                legacyCry != null ? legacyCry : textOrNull(p.path("cries").path("latest")));
    }

    private static String englishGenus(JsonNode species) {
        for (JsonNode g : species.path("genera")) {
            if ("en".equals(g.path("language").path("name").asString())) {
                return g.path("genus").asString();
            }
        }
        return null;
    }

    /** Prefere o texto de Pokémon Red; se não houver, usa a primeira entrada em inglês. */
    private static String description(JsonNode species) {
        String fallback = null;
        for (JsonNode entry : species.path("flavor_text_entries")) {
            if (!"en".equals(entry.path("language").path("name").asString())) {
                continue;
            }
            String text = entry.path("flavor_text").asString().replaceAll("[\\f\\n\\r\\u00ad\\s]+", " ").strip();
            if ("red".equals(entry.path("version").path("name").asString())) {
                return text;
            }
            if (fallback == null) {
                fallback = text;
            }
        }
        return fallback;
    }

    private static String textOrNull(JsonNode node) {
        return node.isString() ? node.asString() : null;
    }

    private static int idFromUrl(String url) {
        String trimmed = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        return Integer.parseInt(trimmed.substring(trimmed.lastIndexOf('/') + 1));
    }

    private CompletableFuture<JsonNode> getAsync(String path) {
        HttpRequest request = HttpRequest.newBuilder(baseUri.resolve(path))
                .timeout(TIMEOUT)
                .header("Accept", "application/json")
                .header("User-Agent", "pokedex-santiann/2.0 (+https://github.com/Santiann/Pokedex--API)")
                .GET()
                .build();
        return send(request, 1);
    }

    private CompletableFuture<JsonNode> send(HttpRequest request, int attempt) {
        return http.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .handle((response, error) -> {
                    boolean retryable = error != null || response.statusCode() >= 500;
                    if (retryable && attempt < MAX_ATTEMPTS) {
                        return send(request, attempt + 1);
                    }
                    if (error != null) {
                        Throwable cause = error instanceof CompletionException ? error.getCause() : error;
                        throw new PokeApiException("Falha ao acessar " + request.uri(), cause);
                    }
                    if (response.statusCode() != 200) {
                        throw new PokeApiException("PokeAPI respondeu " + response.statusCode() + " para " + request.uri());
                    }
                    return CompletableFuture.completedFuture(parse(response));
                })
                .thenCompose(future -> future);
    }

    private JsonNode parse(HttpResponse<String> response) {
        try {
            return json.readTree(response.body());
        } catch (JacksonException e) {
            throw new PokeApiException("Resposta inválida de " + response.uri(), e);
        }
    }
}
