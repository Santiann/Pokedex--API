package io.github.santiann.pokedex.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Ponto de entrada da Pokédex: busca por número ou nome, com cache em memória.
 * A PokeAPI pede que os clientes guardem cache, e os dados da 1ª geração não mudam.
 */
public class Pokedex {

    public static final int TOTAL = 151;

    /** Quantas requisições simultâneas o {@link #findAll()} faz, para não abusar da PokeAPI. */
    private static final int MAX_CONCURRENT_REQUESTS = 10;

    private final PokeApiClient client;
    private final Map<Integer, Pokemon> cache = new ConcurrentHashMap<>();
    private volatile List<PokemonSummary> summaries;
    private volatile Map<String, Integer> idsByName;

    public Pokedex() {
        this(new PokeApiClient());
    }

    public Pokedex(PokeApiClient client) {
        this.client = client;
    }

    public List<PokemonSummary> summaries() {
        List<PokemonSummary> current = summaries;
        if (current == null) {
            synchronized (this) {
                if (summaries == null) {
                    List<PokemonSummary> fetched = List.copyOf(client.fetchSummaries(TOTAL));
                    idsByName = fetched.stream().collect(Collectors.toUnmodifiableMap(PokemonSummary::name, PokemonSummary::id));
                    summaries = fetched;
                }
                current = summaries;
            }
        }
        return current;
    }

    public Pokemon find(int id) {
        if (id < 1 || id > TOTAL) {
            throw new PokemonNotFoundException(String.valueOf(id));
        }
        Pokemon cached = cache.get(id);
        if (cached != null) {
            return cached;
        }
        Pokemon fetched = client.fetchPokemon(id);
        Pokemon raced = cache.putIfAbsent(id, fetched);
        return raced != null ? raced : fetched;
    }

    /** Aceita número ({@code "25"}, {@code "#025"}) ou nome ({@code "Pikachu"}, {@code "mr. mime"}). */
    public Pokemon find(String query) {
        String trimmed = query.strip().replaceFirst("^#", "");
        if (trimmed.matches("\\d{1,4}")) {
            return find(Integer.parseInt(trimmed));
        }
        summaries();
        Integer id = idsByName.get(Names.normalize(trimmed));
        if (id == null) {
            throw new PokemonNotFoundException(query.strip());
        }
        return find(id);
    }

    /** Busca os 151 em paralelo com virtual threads. Depois da primeira chamada, tudo vem do cache. */
    public List<Pokemon> findAll() {
        Semaphore permits = new Semaphore(MAX_CONCURRENT_REQUESTS);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Pokemon>> futures = IntStream.rangeClosed(1, TOTAL)
                    .mapToObj(id -> executor.submit(() -> {
                        permits.acquire();
                        try {
                            return find(id);
                        } finally {
                            permits.release();
                        }
                    }))
                    .toList();
            return futures.stream().map(Pokedex::await).toList();
        }
    }

    public boolean isFullyLoaded() {
        return cache.size() == TOTAL;
    }

    private static Pokemon await(Future<Pokemon> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PokeApiException("Interrompido enquanto carregava a Pokédex", e);
        } catch (ExecutionException e) {
            throw e.getCause() instanceof RuntimeException cause ? cause : new PokeApiException("Falha ao carregar a Pokédex", e.getCause());
        }
    }
}
