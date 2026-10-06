package io.github.santiann.pokedex.api;

import io.github.santiann.pokedex.core.Pokedex;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class PokedexConfig {

    private static final Logger log = LoggerFactory.getLogger(PokedexConfig.class);

    @Bean
    Pokedex pokedex() {
        return new Pokedex();
    }

    /** Carrega os 151 em segundo plano na subida, para a primeira visita já encontrar tudo em cache. */
    @Bean
    @ConditionalOnProperty(name = "pokedex.warmup", havingValue = "true", matchIfMissing = true)
    ApplicationRunner warmup(Pokedex pokedex) {
        return args -> Thread.ofVirtual().name("pokedex-warmup").start(() -> {
            long start = System.nanoTime();
            try {
                pokedex.findAll();
                log.info("Pokédex aquecida: {} Pokémon em cache em {} ms", Pokedex.TOTAL, (System.nanoTime() - start) / 1_000_000);
            } catch (RuntimeException e) {
                log.warn("Não deu para aquecer o cache agora; os dados serão buscados sob demanda", e);
            }
        });
    }

    @Bean
    OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("Pokédex API")
                .version("2.0.0")
                .description("Os 151 Pokémon da 1ª geração, com dados da PokeAPI e arte ASCII feita à mão."));
    }
}
