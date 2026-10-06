package io.github.santiann.pokedex.cli;

/**
 * Cores ANSI em 24 bits. Com {@code enabled = false}, devolve o texto puro (pipe, arquivo ou NO_COLOR).
 * Segue as convenções <a href="https://no-color.org">NO_COLOR</a> e FORCE_COLOR.
 */
record Ansi(boolean enabled) {

    private static final String RESET = "\u001B[0m";

    static Ansi detect() {
        if (System.getenv("NO_COLOR") != null) {
            return new Ansi(false);
        }
        return new Ansi(System.getenv("FORCE_COLOR") != null || System.console() != null);
    }

    String fg(int rgb, String text) {
        return enabled ? "\u001B[38;2;%d;%d;%dm%s%s".formatted(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, text, RESET) : text;
    }

    String badge(int rgb, String text) {
        if (!enabled) {
            return "[" + text + "]";
        }
        int r = rgb >> 16 & 0xFF, g = rgb >> 8 & 0xFF, b = rgb & 0xFF;
        // Texto escuro em fundos claros (Elétrico, Gelo), branco nos escuros.
        int ink = 0.2126 * r + 0.7152 * g + 0.0722 * b > 150 ? 20 : 255;
        return "\u001B[1;38;2;%d;%d;%d;48;2;%d;%d;%dm %s %s".formatted(ink, ink, ink, r, g, b, text, RESET);
    }

    String bold(String text) {
        return enabled ? "\u001B[1m" + text + RESET : text;
    }

    String dim(String text) {
        return enabled ? "\u001B[2m" + text + RESET : text;
    }

    /** Mistura duas cores: {@code t = 0} devolve {@code from}, {@code t = 1} devolve {@code to}. */
    static int mix(int from, int to, double t) {
        int r = (int) Math.round((from >> 16 & 0xFF) + ((to >> 16 & 0xFF) - (from >> 16 & 0xFF)) * t);
        int g = (int) Math.round((from >> 8 & 0xFF) + ((to >> 8 & 0xFF) - (from >> 8 & 0xFF)) * t);
        int b = (int) Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return r << 16 | g << 8 | b;
    }
}
