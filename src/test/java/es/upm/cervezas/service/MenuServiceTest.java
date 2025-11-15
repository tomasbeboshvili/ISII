package es.upm.cervezas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import es.upm.cervezas.domain.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private TokenAuthenticationService tokenAuthenticationService;

    private MenuService menuService;

    @BeforeEach
    void setUp() {
        menuService = new MenuService(tokenAuthenticationService);
    }

    @Test
    void anonymousMenuShowsRegistrationActions() {
        when(tokenAuthenticationService.findUserByToken(null)).thenReturn(Optional.empty());

        var menu = menuService.buildMenu(null);

        assertThat(menu.availableActions()).contains("Registrarse");
        assertThat(menu.emailVerified()).isFalse();
    }

    @Test
    void activatedUserSeesAdvancedActions() {
        User user = new User();
        user.setActivated(true);
        user.setCity("Madrid");
        user.setCountry("ES");
        when(tokenAuthenticationService.findUserByToken("token")).thenReturn(Optional.of(user));

        var menu = menuService.buildMenu("token");

        assertThat(menu.emailVerified()).isTrue();
        assertThat(menu.availableActions()).contains("Crear degustación");
    }
}
