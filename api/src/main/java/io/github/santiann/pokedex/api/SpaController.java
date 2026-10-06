package io.github.santiann.pokedex.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Links diretos como /pokemon/25: o React cuida da rota, então o servidor devolve o index.html. */
@Controller
class SpaController {

    @GetMapping("/pokemon/{query}")
    String forward() {
        return "forward:/index.html";
    }
}
