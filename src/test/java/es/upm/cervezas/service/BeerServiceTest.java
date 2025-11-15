package es.upm.cervezas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.upm.cervezas.api.dto.BeerRequest;
import es.upm.cervezas.api.dto.RatingRequest;
import es.upm.cervezas.domain.Beer;
import es.upm.cervezas.domain.BeerRating;
import es.upm.cervezas.domain.User;
import es.upm.cervezas.repository.BeerRatingRepository;
import es.upm.cervezas.repository.BeerRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BeerServiceTest {

    @Mock
    private BeerRepository beerRepository;
    @Mock
    private BeerRatingRepository beerRatingRepository;
    @Mock
    private UserProfileService userProfileService;
    @Mock
    private AchievementService achievementService;

    private BeerService beerService;

    @BeforeEach
    void setUp() {
        beerService = new BeerService(beerRepository, beerRatingRepository, userProfileService, achievementService);
    }

    @Test
    void createBeerPersistsData() {
        User user = sampleUser();
        BeerRequest request = new BeerRequest("IPA", "IPA", "Brew", "ES", BigDecimal.valueOf(6.5), 60, "Notas");
        when(userProfileService.requireUser("token")).thenReturn(user);
        when(beerRepository.save(any(Beer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = beerService.createBeer("token", request);

        ArgumentCaptor<Beer> captor = ArgumentCaptor.forClass(Beer.class);
        verify(beerRepository).save(captor.capture());
        Beer saved = captor.getValue();
        assertThat(saved.getCreatedBy()).isEqualTo(user);
        assertThat(response.name()).isEqualTo("IPA");
        verify(achievementService).refreshProgress(user);
    }

    @Test
    void getBeerThrowsWhenMissing() {
        when(beerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> beerService.getBeer(1L));
    }

    @Test
    void listReturnsRatings() {
        Beer beer = new Beer();
        beer.setId(1L);
        beer.setName("Lager");
        beer.setStyle("Lager");
        beer.setCreatedAt(Instant.now());
        when(beerRepository.findAll()).thenReturn(List.of(beer));
        when(beerRatingRepository.findAverageScoreByBeerId(1L)).thenReturn(4.25);
        when(beerRatingRepository.countByBeerId(1L)).thenReturn(3L);

        var beers = beerService.getAllBeers();

        assertThat(beers).hasSize(1);
        assertThat(beers.get(0).averageScore()).isEqualTo(4.3);
        assertThat(beers.get(0).ratingsCount()).isEqualTo(3);
    }

    @Test
    void rateBeerCreatesOrUpdatesRating() {
        User user = sampleUser();
        Beer beer = new Beer();
        beer.setId(7L);
        when(userProfileService.requireUser("token")).thenReturn(user);
        when(beerRepository.findById(7L)).thenReturn(Optional.of(beer));
        when(beerRatingRepository.findByBeerIdAndUserId(7L, user.getId())).thenReturn(Optional.empty());
        when(beerRatingRepository.save(any(BeerRating.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = beerService.rateBeer("token", new RatingRequest(7L, 5, "Excelente"));

        verify(beerRatingRepository).save(any(BeerRating.class));
        assertThat(response.id()).isEqualTo(beer.getId());
        verify(achievementService).refreshProgress(user);
    }

    private User sampleUser() {
        User user = new User();
        user.setId(99L);
        user.setDisplayName("User");
        return user;
    }
}
