package es.upm.cervezas.api.controller;

import es.upm.cervezas.api.dto.BeerRequest;
import es.upm.cervezas.api.dto.BeerResponse;
import es.upm.cervezas.api.dto.RatingRequest;
import es.upm.cervezas.service.BeerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST para operar con cervezas: alta, listado, detalle, valoración y eliminación.
 */
@RestController
@RequestMapping("/api/beers")
public class BeerController {

    private static final Logger log = LoggerFactory.getLogger(BeerController.class);

    private final BeerService beerService;

    public BeerController(BeerService beerService) {
        this.beerService = beerService;
    }

    @GetMapping
    public List<BeerResponse> list() {
        log.debug("Listando cervezas");
        return beerService.getAllBeers();
    }

    @GetMapping("/{id}")
    public BeerResponse detail(@PathVariable Long id) {
        log.debug("Consultando cerveza {}", id);
        return beerService.getBeer(id);
    }

    @PostMapping
    public BeerResponse create(@RequestHeader("X-Auth-Token") String token,
                               @Valid @RequestBody BeerRequest request) {
        log.info("Creación de cerveza solicitada por token {}", token);
        return beerService.createBeer(token, request);
    }

    @PostMapping("/rate")
    public BeerResponse rate(@RequestHeader("X-Auth-Token") String token,
                             @Valid @RequestBody RatingRequest request) {
        log.info("Nueva valoración para cerveza {}", request.beerId());
        return beerService.rateBeer(token, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@RequestHeader("X-Auth-Token") String token, @PathVariable Long id) {
        log.info("Solicitud de borrado para cerveza {}", id);
        beerService.deleteBeer(token, id);
    }
}
