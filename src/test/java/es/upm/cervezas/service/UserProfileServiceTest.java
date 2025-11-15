package es.upm.cervezas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import es.upm.cervezas.api.dto.UserProfileUpdateRequest;
import es.upm.cervezas.domain.User;
import es.upm.cervezas.repository.UserRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private TokenAuthenticationService tokenAuthenticationService;
    @Mock
    private UserRepository userRepository;

    private UserProfileService service;

    @BeforeEach
    void setUp() {
        service = new UserProfileService(tokenAuthenticationService, userRepository);
    }

    @Test
    void requireUserThrowsWhenTokenInvalid() {
        when(tokenAuthenticationService.findUserByToken("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requireUser("bad"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void profileResponseContainsUserData() {
        User user = buildUser();
        when(tokenAuthenticationService.findUserByToken("good")).thenReturn(Optional.of(user));

        var response = service.getProfile("good");

        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.displayName()).isEqualTo("User");
        assertThat(response.birthDate()).isEqualTo(user.getBirthDate());
        assertThat(response.username()).isEqualTo("userhandle");
    }

    @Test
    void updateProfileMutatesEntity() {
        User user = buildUser();
        when(tokenAuthenticationService.findUserByToken(any())).thenReturn(Optional.of(user));

        var request = new UserProfileUpdateRequest(
                "Nuevo",
                "NuevoNombre",
                "Apellidos",
                "http://foto",
                "Origen",
                "Intro",
                "Madrid",
                "M",
                LocalDate.now().minusYears(25),
                "Madrid",
                "ES",
                "Bio"
        );
        var response = service.updateProfile("token", request);

        assertThat(user.getDisplayName()).isEqualTo("Nuevo");
        assertThat(response.city()).isEqualTo("Madrid");
        assertThat(response.country()).isEqualTo("ES");
        assertThat(response.bio()).isEqualTo("Bio");
        assertThat(user.getFirstName()).isEqualTo("NuevoNombre");
        assertThat(user.getPhotoUrl()).isEqualTo("http://foto");
    }

    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setUsername("userhandle");
        user.setDisplayName("User");
        user.setBirthDate(LocalDate.now().minusYears(25));
        user.setActivated(true);
        return user;
    }
}
