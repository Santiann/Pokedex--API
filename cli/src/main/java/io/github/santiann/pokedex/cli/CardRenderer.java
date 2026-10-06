package io.github.santiann.pokedex.cli;

import io.github.santiann.pokedex.core.AsciiArt;
import io.github.santiann.pokedex.core.Pokemon;
import io.github.santiann.pokedex.core.PokemonSummary;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** Monta a ficha de um Pokémon para o terminal. */
final class CardRenderer {

    /** Caracteres das artes originais, do mais escuro ao mais claro. O {@code @} é o fundo. */
    private static final String RAMP = " .:^~!7?JY5PGB#&@";
    private static final int OUTLINE = 0x202020;
    private static final int HIGHLIGHT = 0xF4F4F4;
    private static final int BACKGROUND = 0x3A3A3A;

    private static final int BAR_WIDTH = 30;
    private static final int BAR_MAX = 180;
    private static final int TEXT_WIDTH = 72;

    private final Ansi ansi;

    CardRenderer(Ansi ansi) {
        this.ansi = ansi;
    }

    String render(Pokemon p) {
        int color = PtBr.typeColor(p.types().getFirst());
        StringBuilder out = new StringBuilder();

        AsciiArt.of(p.id()).ifPresent(art -> out.append(colorize(art, color)).append('\n'));

        out.append(ansi.fg(color, ansi.bold("#%03d  %s".formatted(p.id(), p.displayName().toUpperCase(Locale.ROOT)))));
        if (p.genus() != null) {
            out.append("  ").append(ansi.dim(p.genus()));
        }
        out.append("\n\n ");
        out.append(p.types().stream().map(t -> ansi.badge(PtBr.typeColor(t), PtBr.type(t))).collect(Collectors.joining(" ")));
        out.append("   ").append(ansi.dim("Altura")).append(" %.1f m   ".formatted(p.heightM()))
                .append(ansi.dim("Peso")).append(" %.1f kg\n".formatted(p.weightKg()));

        if (p.description() != null) {
            out.append('\n');
            wrap("“" + p.description() + "”", TEXT_WIDTH).forEach(line -> out.append(' ').append(ansi.dim(line)).append('\n'));
        }

        out.append('\n').append(section("Status"));
        for (Pokemon.Stat stat : p.stats()) {
            out.append(" %-11s %3d ".formatted(PtBr.stat(stat.name()), stat.base())).append(bar(stat.base())).append('\n');
        }
        out.append(" %-11s %3d\n".formatted("Total", p.totalStats()));

        out.append('\n').append(section("Habilidades")).append(' ')
                .append(p.abilities().stream()
                        .map(a -> PtBr.title(a.name()) + (a.hidden() ? ansi.dim(" (oculta)") : ""))
                        .collect(Collectors.joining(" · ")))
                .append('\n');

        out.append('\n').append(section("Golpes (" + p.moves().size() + ")"));
        String moves = p.moves().stream().map(PtBr::title).collect(Collectors.joining(", "));
        wrap(moves, TEXT_WIDTH).forEach(line -> out.append(' ').append(line).append('\n'));

        return out.toString();
    }

    String renderList(List<PokemonSummary> summaries) {
        int columns = 4;
        int rows = (summaries.size() + columns - 1) / columns;
        StringBuilder out = new StringBuilder();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int index = col * rows + row;
                if (index < summaries.size()) {
                    PokemonSummary s = summaries.get(index);
                    out.append(ansi.dim("#%03d ".formatted(s.id()))).append("%-14s".formatted(s.displayName()));
                }
            }
            out.append('\n');
        }
        return out.toString();
    }

    private String section(String title) {
        return ansi.bold(title) + "\n";
    }

    private String colorize(String art, int typeColor) {
        if (!ansi.enabled()) {
            return art;
        }
        StringBuilder out = new StringBuilder(art.length() * 4);
        for (String line : art.split("\n")) {
            int i = 0;
            while (i < line.length()) {
                char c = line.charAt(i);
                int j = i;
                while (j < line.length() && line.charAt(j) == c) {
                    j++;
                }
                out.append(ansi.fg(shade(c, typeColor), line.substring(i, j)));
                i = j;
            }
            out.append('\n');
        }
        return out.toString();
    }

    private static int shade(char c, int typeColor) {
        if (c == '@') {
            return BACKGROUND;
        }
        int index = RAMP.indexOf(c);
        double t = index < 0 ? 0.5 : index / (double) (RAMP.length() - 2);
        return t < 0.5 ? Ansi.mix(OUTLINE, typeColor, t * 2) : Ansi.mix(typeColor, HIGHLIGHT, (t - 0.5) * 2 * 0.6);
    }

    private String bar(int value) {
        int filled = Math.max(1, Math.min(BAR_WIDTH, Math.round(value / (float) BAR_MAX * BAR_WIDTH)));
        int color = value < 50 ? 0xF34444 : value < 80 ? 0xFF7F0F : value < 100 ? 0xFFDD57 : value < 120 ? 0xA0E515 : 0x23CD5E;
        return ansi.fg(color, "█".repeat(filled)) + ansi.dim("░".repeat(BAR_WIDTH - filled));
    }

    static List<String> wrap(String text, int width) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            if (!line.isEmpty() && line.length() + 1 + word.length() > width) {
                lines.add(line.toString());
                line.setLength(0);
            }
            line.append(line.isEmpty() ? "" : " ").append(word);
        }
        if (!line.isEmpty()) {
            lines.add(line.toString());
        }
        return lines;
    }
}
