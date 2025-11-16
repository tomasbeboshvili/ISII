package es.upm.cervezas.service;

import es.upm.cervezas.api.dto.BeerRequest;
import es.upm.cervezas.api.dto.BeerResponse;
import es.upm.cervezas.api.dto.RatingRequest;
import es.upm.cervezas.domain.Beer;
import es.upm.cervezas.domain.BeerRating;
import es.upm.cervezas.domain.User;
import es.upm.cervezas.repository.BeerRatingRepository;
import es.upm.cervezas.repository.BeerRepository;
import es.upm.cervezas.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio principal para gestionar el ciclo de vida de las cervezas (alta, listado, valoración
 * y borrado). Además mantiene los contadores por usuario y dispara los galardones asociados.
 */
@Service
public class BeerService {

    private final BeerRepository beerRepository;
    private final BeerRatingRepository beerRatingRepository;
    private final UserProfileService userProfileService;
    private final AchievementService achievementService;
    private final UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(BeerService.class);

    public BeerService(BeerRepository beerRepository,
                       BeerRatingRepository beerRatingRepository,
                       UserProfileService userProfileService,
                       AchievementService achievementService,
                       UserRepository userRepository) {
        this.beerRepository = beerRepository;
        this.beerRatingRepository = beerRatingRepository;
        this.userProfileService = userProfileService;
        this.achievementService = achievementService;
        this.userRepository = userRepository;
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
        log.info("Cerveza {} creada por usuario {}", beer.getName(), user.getId());
        user.setGamificationPoints(user.getGamificationPoints() + 15);
        user.setBeersCreatedCount((int) beerRepository.countByCreatedById(user.getId()));
        userRepository.save(user);
        achievementService.refreshProgress(user);
        achievementService.checkMilestone(user, "BEERS_CREATED", user.getBeersCreatedCount());
        return toResponse(beer);
    }

    @Transactional(readOnly = true)
    public List<BeerResponse> getAllBeers() {
        List<BeerResponse> beers = beerRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        log.info("Listado de cervezas actualizado: {} elementos", beers.size());
        return beers;
    }

    @Transactional(readOnly = true)
    public BeerResponse getBeer(Long id) {
        Beer beer = beerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cerveza no encontrada"));
        log.debug("Cerveza {} cargada", id);
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
        log.info("Valoración registrada para cerveza {} (puntuación {}) por usuario {}", beer.getId(), request.score(), user.getId());
        user.setGamificationPoints(user.getGamificationPoints() + 5);
        user.setRatingsCount((int) beerRatingRepository.countByUserId(user.getId()));
        userRepository.save(user);
        achievementService.refreshProgress(user);
        achievementService.checkMilestone(user, "RATINGS", user.getRatingsCount());
        return toResponse(beer);
    }

    /**
     * Elimina la cerveza indicada (y sus valoraciones asociadas) siempre que el token sea válido.
     */
    @Transactional
    public void deleteBeer(String token, Long beerId) {
        User user = userProfileService.requireUser(token);
        Beer beer = beerRepository.findById(beerId)
                .orElseThrow(() -> new EntityNotFoundException("Cerveza no encontrada"));
        beerRatingRepository.deleteByBeerId(beerId);
        beerRepository.delete(beer);
        log.info("Cerveza {} eliminada por usuario {}", beerId, user.getId());
        user.setBeersCreatedCount((int) beerRepository.countByCreatedById(user.getId()));
        userRepository.save(user);
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
                avg,
                count
        );
    }
}
