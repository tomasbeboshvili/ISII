package es.upm.cervezas.service;

import es.upm.cervezas.api.dto.TastingRequest;
import es.upm.cervezas.api.dto.TastingResponse;
import es.upm.cervezas.domain.Beer;
import es.upm.cervezas.domain.Tasting;
import es.upm.cervezas.domain.User;
import es.upm.cervezas.repository.BeerRepository;
import es.upm.cervezas.repository.TastingRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TastingService {

    private final TastingRepository tastingRepository;
    private final BeerRepository beerRepository;
    private final UserProfileService userProfileService;
    private final AchievementService achievementService;

    private static final Logger log = LoggerFactory.getLogger(TastingService.class);

    public TastingService(TastingRepository tastingRepository,
                          BeerRepository beerRepository,
                          UserProfileService userProfileService,
                          AchievementService achievementService) {
        this.tastingRepository = tastingRepository;
        this.beerRepository = beerRepository;
        this.userProfileService = userProfileService;
        this.achievementService = achievementService;
    }

    @Transactional
    public TastingResponse create(String token, TastingRequest request) {
        User user = userProfileService.requireUser(token);
        Beer beer = beerRepository.findById(request.beerId())
                .orElseThrow(() -> new EntityNotFoundException("Cerveza no encontrada"));

        Tasting tasting = new Tasting();
        tasting.setUser(user);
        tasting.setBeer(beer);
        tasting.setTastingDate(request.tastingDate());
        tasting.setLocation(request.location());
        tasting.setNotes(request.notes());
        tasting.setAromaScore(request.aromaScore());
        tasting.setFlavorScore(request.flavorScore());
        tasting.setAppearanceScore(request.appearanceScore());
        tasting.setCreatedAt(Instant.now());

        tastingRepository.save(tasting);
        log.info("Degustación {} registrada por usuario {}", tasting.getId(), user.getId());
        user.setGamificationPoints(user.getGamificationPoints() + 10);
        achievementService.refreshProgress(user);
        return toResponse(tasting);
    }

    @Transactional(readOnly = true)
    public List<TastingResponse> forCurrentUser(String token) {
        User user = userProfileService.requireUser(token);
        log.debug("Listando degustaciones del usuario {}", user.getId());
        return tastingRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TastingResponse> forBeer(Long beerId) {
        log.debug("Listando degustaciones para cerveza {}", beerId);
        return tastingRepository.findByBeerId(beerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private TastingResponse toResponse(Tasting tasting) {
        return new TastingResponse(
                tasting.getId(),
                tasting.getBeer().getId(),
                tasting.getBeer().getName(),
                tasting.getTastingDate(),
                tasting.getLocation(),
                tasting.getNotes(),
                tasting.getAromaScore(),
                tasting.getFlavorScore(),
                tasting.getAppearanceScore(),
                tasting.getCreatedAt()
        );
    }
}
