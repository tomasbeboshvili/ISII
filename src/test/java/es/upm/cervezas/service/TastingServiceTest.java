package es.upm.cervezas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.upm.cervezas.api.dto.TastingRequest;
import es.upm.cervezas.domain.Beer;
import es.upm.cervezas.domain.Tasting;
import es.upm.cervezas.domain.User;
import es.upm.cervezas.repository.BeerRepository;
import es.upm.cervezas.repository.TastingRepository;
import es.upm.cervezas.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TastingServiceTest {

    @Mock
    private TastingRepository tastingRepository;
    @Mock
    private BeerRepository beerRepository;
    @Mock
    private UserProfileService userProfileService;
    @Mock
    private AchievementService achievementService;
    @Mock
    private UserRepository userRepository;

    private TastingService service;

    @BeforeEach
    void setUp() {
        service = new TastingService(tastingRepository, beerRepository, userProfileService, achievementService, userRepository);
    }

    @Test
    void createPersistsTasting() {
        User user = new User();
        user.setId(1L);
        Beer beer = new Beer();
        beer.setId(2L);
        when(userProfileService.requireUser("token")).thenReturn(user);
        when(beerRepository.findById(2L)).thenReturn(Optional.of(beer));
        when(tastingRepository.save(any(Tasting.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tastingRepository.countByUserId(user.getId())).thenReturn(1L);

        var request = new TastingRequest(2L, LocalDateTime.now(), "Madrid", "Notas", 4, 5, 4);
        var response = service.create("token", request);

        ArgumentCaptor<Tasting> captor = ArgumentCaptor.forClass(Tasting.class);
        verify(tastingRepository).save(captor.capture());
        assertThat(captor.getValue().getBeer()).isEqualTo(beer);
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(response.beerId()).isEqualTo(2L);
        verify(achievementService).refreshProgress(user);
        verify(achievementService).checkMilestone(eq(user), eq("TASTINGS"), anyInt());
        verify(userRepository).save(user);
    }

    @Test
    void listsForUser() {
        User user = new User();
        user.setId(1L);
        when(userProfileService.requireUser("token")).thenReturn(user);
        Tasting tasting = new Tasting();
        tasting.setBeer(new Beer());
        tasting.getBeer().setId(3L);
        tasting.getBeer().setName("Lager");
        when(tastingRepository.findByUserId(1L)).thenReturn(List.of(tasting));

        var tastings = service.forCurrentUser("token");

        assertThat(tastings).hasSize(1);
        assertThat(tastings.get(0).beerName()).isEqualTo("Lager");
    }

    @Test
    void listsForBeer() {
        Tasting tasting = new Tasting();
        tasting.setBeer(new Beer());
        tasting.getBeer().setId(5L);
        tasting.getBeer().setName("Stout");
        when(tastingRepository.findByBeerId(5L)).thenReturn(List.of(tasting));

        var tastings = service.forBeer(5L);

        assertThat(tastings.get(0).beerName()).isEqualTo("Stout");
    }
}
