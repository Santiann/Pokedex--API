package io.github.santiann.pokedex.core;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** PokeAPI de mentira, servindo fixtures de {@code src/test/resources/fixtures}. */
final class FakePokeApi implements AutoCloseable {

    private final HttpServer server;
    private final Map<String, AtomicInteger> hits = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> failuresLeft = new ConcurrentHashMap<>();

    FakePokeApi() {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        server.createContext("/api/v2/", exchange -> {
            String path = exchange.getRequestURI().toString().substring("/api/v2/".length());
            hits.computeIfAbsent(path, k -> new AtomicInteger()).incrementAndGet();

            AtomicInteger failures = failuresLeft.get(path);
            byte[] body = failures != null && failures.getAndDecrement() > 0 ? null : fixtureFor(path);
            int status = body != null ? 200 : failures != null ? 500 : 404;
            if (body == null) {
                body = "{}".getBytes(StandardCharsets.UTF_8);
            }
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
    }

    URI baseUri() {
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/api/v2/");
    }

    int hits(String path) {
        AtomicInteger count = hits.get(path);
        return count == null ? 0 : count.get();
    }

    /** As próximas {@code times} requisições para {@code path} respondem 500. */
    void failNext(String path, int times) {
        failuresLeft.put(path, new AtomicInteger(times));
    }

    private static byte[] fixtureFor(String path) {
        String file = switch (path) {
            case "pokemon?limit=151" -> "pokemon-list.json";
            case "pokemon/25" -> "pokemon-25.json";
            case "pokemon-species/25" -> "species-25.json";
            default -> null;
        };
        if (file == null) {
            return null;
        }
        try (InputStream in = FakePokeApi.class.getResourceAsStream("/fixtures/" + file)) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
