package io.github.santiann.pokedex.cli;

import io.github.santiann.pokedex.core.PokeApiException;
import io.github.santiann.pokedex.core.Pokedex;
import io.github.santiann.pokedex.core.PokemonNotFoundException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A Pokédex de terminal.
 *
 * <pre>
 * java -jar pokedex-cli.jar            # modo interativo
 * java -jar pokedex-cli.jar pikachu 1  # mostra as fichas e sai
 * </pre>
 */
public final class PokedexCli {

    private static final String LOGO = """
             ____   ___  _  _______ ____  _______  __
            |  _ \\ / _ \\| |/ / ____|  _ \\| ____\\ \\/ /
            | |_) | | | | ' /|  _| | | | |  _|  \\  /
            |  __/| |_| | . \\| |___| |_| | |___ /  \\
            |_|    \\___/|_|\\_\\_____|____/|_____/_/\\_\\
            """;

    private static final String HELP = """
            Digite um número (1 a 151) ou um nome para ver a ficha.
              lista      mostra os 151 Pokémon
              aleatorio  sorteia um Pokémon
              ajuda      mostra esta mensagem
              sair       fecha a Pokédex
            """;

    private final Pokedex pokedex;
    private final CardRenderer renderer;
    private final Ansi ansi;
    private final PrintStream out;

    PokedexCli(Pokedex pokedex, Ansi ansi, PrintStream out) {
        this.pokedex = pokedex;
        this.renderer = new CardRenderer(ansi);
        this.ansi = ansi;
        this.out = out;
    }

    public static void main(String[] args) {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        PokedexCli cli = new PokedexCli(new Pokedex(), Ansi.detect(), out);
        if (args.length > 0) {
            System.exit(cli.showAll(args));
        }
        cli.interactive(new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)));
    }

    int showAll(String... queries) {
        int status = 0;
        for (String query : queries) {
            if (!show(query)) {
                status = 1;
            }
        }
        return status;
    }

    void interactive(BufferedReader in) {
        out.println(ansi.fg(0xE3350D, LOGO));
        out.println(ansi.dim("Pokédex da 1ª geração · arte ASCII feita à mão em 2022 · dados da PokeAPI"));
        out.println();
        out.print(HELP);

        while (true) {
            out.print("\n" + ansi.fg(0xE3350D, "pokédex") + " › ");
            out.flush();
            String line = readLine(in);
            if (line == null) {
                out.println();
                return;
            }
            String command = line.strip().toLowerCase(Locale.ROOT);
            switch (command) {
                case "" -> {}
                case "sair", "exit", "quit", "q" -> {
                    out.println("Até a próxima, treinador!");
                    return;
                }
                case "ajuda", "help", "h" -> out.print(HELP);
                case "lista", "list", "l" -> list();
                case "aleatorio", "aleatório", "random", "a" -> show(String.valueOf(ThreadLocalRandom.current().nextInt(1, Pokedex.TOTAL + 1)));
                default -> show(line);
            }
        }
    }

    private void list() {
        try {
            out.print(renderer.renderList(pokedex.summaries()));
        } catch (PokeApiException e) {
            error("Não consegui falar com a PokeAPI agora. Confira sua conexão e tente de novo.");
        }
    }

    private boolean show(String query) {
        try {
            out.println();
            out.print(renderer.render(pokedex.find(query)));
            return true;
        } catch (PokemonNotFoundException e) {
            error(e.getMessage());
        } catch (PokeApiException e) {
            error("Não consegui falar com a PokeAPI agora. Confira sua conexão e tente de novo.");
        }
        return false;
    }

    private void error(String message) {
        out.println(ansi.fg(0xF34444, "✗ ") + message);
    }

    private static String readLine(BufferedReader in) {
        try {
            return in.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
