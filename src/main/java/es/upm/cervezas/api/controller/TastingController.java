package es.upm.cervezas.api.controller;

import es.upm.cervezas.api.dto.TastingRequest;
import es.upm.cervezas.api.dto.TastingResponse;
import es.upm.cervezas.service.TastingService;
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
@RequestMapping("/api/tastings")
public class TastingController {

    private final TastingService tastingService;

    public TastingController(TastingService tastingService) {
        this.tastingService = tastingService;
    }

    @PostMapping
    public TastingResponse create(@RequestHeader("X-Auth-Token") String token,
                                  @Valid @RequestBody TastingRequest request) {
        return tastingService.create(token, request);
    }

    @GetMapping("/me")
    public List<TastingResponse> mine(@RequestHeader("X-Auth-Token") String token) {
        return tastingService.forCurrentUser(token);
    }

    @GetMapping("/beer/{beerId}")
    public List<TastingResponse> byBeer(@PathVariable Long beerId) {
        return tastingService.forBeer(beerId);
    }
}
