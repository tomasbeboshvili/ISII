package es.upm.cervezas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.upm.cervezas.api.dto.AchievementRequest;
import es.upm.cervezas.domain.Achievement;
import es.upm.cervezas.domain.User;
import es.upm.cervezas.repository.AchievementRepository;
import es.upm.cervezas.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock
    private AchievementRepository achievementRepository;
    @Mock
    private TokenAuthenticationService tokenAuthenticationService;
    @Mock
    private UserRepository userRepository;

    private AchievementService service;

    @BeforeEach
    void setUp() {
        service = new AchievementService(achievementRepository, tokenAuthenticationService, userRepository);
    }

    @Test
    void createPersistsAchievement() {
        AchievementRequest request = new AchievementRequest("Explorador", "Degustaciones", null, 1, "10 catas", 10);
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(request);

        assertThat(response.name()).isEqualTo("Explorador");
        ArgumentCaptor<Achievement> captor = ArgumentCaptor.forClass(Achievement.class);
        verify(achievementRepository).save(captor.capture());
        assertThat(captor.getValue().getThreshold()).isEqualTo(10);
    }

    @Test
    void assignUpdatesUser() {
        User user = new User();
        user.setId(1L);
        Achievement achievement = new Achievement();
        achievement.setId(5L);
        achievement.setLevel(2);
        when(tokenAuthenticationService.findUserByToken("token")).thenReturn(Optional.of(user));
        when(achievementRepository.findById(5L)).thenReturn(Optional.of(achievement));

        var response = service.assignToCurrentUser("token", 5L);

        assertThat(response.achievementId()).isEqualTo(5L);
        assertThat(user.getBadgeLevel()).isEqualTo(2);
        verify(userRepository).save(user);
    }

    @Test
    void refreshProgressChoosesBestAchievement() {
        User user = new User();
        user.setGamificationPoints(50);
        Achievement achievement = new Achievement();
        achievement.setId(7L);
        achievement.setLevel(3);
        when(achievementRepository.findFirstByThresholdLessThanEqualOrderByThresholdDesc(50))
                .thenReturn(Optional.of(achievement));

        service.refreshProgress(user);

        assertThat(user.getCurrentAchievementId()).isEqualTo(7L);
        assertThat(user.getBadgeLevel()).isEqualTo(3);
        verify(userRepository).save(user);
    }
}
