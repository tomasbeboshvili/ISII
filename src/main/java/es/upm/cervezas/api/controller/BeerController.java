package es.upm.cervezas.api.controller;

import es.upm.cervezas.api.dto.BeerRequest;
import es.upm.cervezas.api.dto.BeerResponse;
import es.upm.cervezas.api.dto.RatingRequest;
import es.upm.cervezas.service.BeerService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/beers")
public class BeerController {

    private final BeerService beerService;

    public BeerController(BeerService beerService) {
        this.beerService = beerService;
    }

    @GetMapping
    public List<BeerResponse> list() {
        return beerService.getAllBeers();
    }

    @GetMapping("/{id}")
    public BeerResponse detail(@PathVariable Long id) {
        return beerService.getBeer(id);
    }

    @PostMapping
    public BeerResponse create(@RequestHeader("X-Auth-Token") String token,
                               @Valid @RequestBody BeerRequest request) {
        return beerService.createBeer(token, request);
    }

    @PostMapping("/rate")
    public BeerResponse rate(@RequestHeader("X-Auth-Token") String token,
                             @Valid @RequestBody RatingRequest request) {
        return beerService.rateBeer(token, request);
    }
}
