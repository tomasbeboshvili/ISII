package es.upm.cervezas.service;

import es.upm.cervezas.api.dto.BeerRequest;
import es.upm.cervezas.api.dto.BeerResponse;
import es.upm.cervezas.api.dto.RatingRequest;
import es.upm.cervezas.domain.Beer;
import es.upm.cervezas.domain.BeerRating;
import es.upm.cervezas.domain.User;
import es.upm.cervezas.repository.BeerRatingRepository;
import es.upm.cervezas.repository.BeerRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BeerService {

    private final BeerRepository beerRepository;
    private final BeerRatingRepository beerRatingRepository;
    private final UserProfileService userProfileService;

    public BeerService(BeerRepository beerRepository,
                       BeerRatingRepository beerRatingRepository,
                       UserProfileService userProfileService) {
        this.beerRepository = beerRepository;
        this.beerRatingRepository = beerRatingRepository;
        this.userProfileService = userProfileService;
    }

    @Transactional
    public BeerResponse createBeer(String token, BeerRequest request) {
        User user = userProfileService.requireUser(token);
        Beer beer = new Beer();
        beer.setName(request.name());
        beer.setStyle(request.style());
        beer.setBrewery(request.brewery());
        beer.setOriginCountry(request.originCountry());
        beer.setAbv(request.abv());
        beer.setIbu(request.ibu());
        beer.setDescription(request.description());
        beer.setCreatedBy(user);
        beer.setCreatedAt(Instant.now());

        beerRepository.save(beer);
        return toResponse(beer);
    }

    @Transactional(readOnly = true)
    public List<BeerResponse> getAllBeers() {
        return beerRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BeerResponse getBeer(Long id) {
        Beer beer = beerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cerveza no encontrada"));
        return toResponse(beer);
    }

    @Transactional
    public BeerResponse rateBeer(String token, RatingRequest request) {
        User user = userProfileService.requireUser(token);
        Beer beer = beerRepository.findById(request.beerId())
                .orElseThrow(() -> new EntityNotFoundException("Cerveza no encontrada"));

        BeerRating rating = beerRatingRepository.findByBeerIdAndUserId(beer.getId(), user.getId())
                .orElseGet(BeerRating::new);

        rating.setUser(user);
        rating.setBeer(beer);
        rating.setScore(request.score());
        rating.setComment(request.comment());
        rating.setCreatedAt(Instant.now());

        beerRatingRepository.save(rating);
        return toResponse(beer);
    }

    private BeerResponse toResponse(Beer beer) {
        Double avg = beerRatingRepository.findAverageScoreByBeerId(beer.getId());
        long count = beerRatingRepository.countByBeerId(beer.getId());
        return new BeerResponse(
                beer.getId(),
                beer.getName(),
                beer.getStyle(),
                beer.getBrewery(),
                beer.getOriginCountry(),
                beer.getAbv(),
                beer.getIbu(),
                beer.getDescription(),
                beer.getCreatedAt(),
                avg != null ? Math.round(avg * 10d) / 10d : null,
                count
        );
    }
}
