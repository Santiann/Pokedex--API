package io.github.santiann.pokedex.api;

import io.github.santiann.pokedex.core.PokeApiException;
import io.github.santiann.pokedex.core.PokemonNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Erros no formato Problem Details (RFC 9457). */
@RestControllerAdvice
class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(PokemonNotFoundException.class)
    ProblemDetail notFound(PokemonNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, "Pokémon não encontrado", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail badRequest(MethodArgumentTypeMismatchException e) {
        return problem(HttpStatus.BAD_REQUEST, "Parâmetro inválido", "\"" + e.getValue() + "\" não é um número válido.");
    }

    @ExceptionHandler(PokeApiException.class)
    ProblemDetail upstream(PokeApiException e) {
        log.warn("Falha na PokeAPI", e);
        return problem(HttpStatus.BAD_GATEWAY, "PokeAPI indisponível", "Não foi possível consultar a PokeAPI agora. Tente de novo em instantes.");
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
