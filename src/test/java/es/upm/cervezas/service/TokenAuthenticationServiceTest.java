package es.upm.cervezas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import es.upm.cervezas.domain.SessionToken;
import es.upm.cervezas.domain.User;
import es.upm.cervezas.repository.SessionTokenRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationServiceTest {

    @Mock
    private SessionTokenRepository sessionTokenRepository;

    private TokenAuthenticationService service;

    @BeforeEach
    void setUp() {
        service = new TokenAuthenticationService(sessionTokenRepository);
    }

    @Test
    void returnsEmptyForUnknownToken() {
        when(sessionTokenRepository.findByTokenAndActiveTrue("bad")).thenReturn(Optional.empty());
        assertThat(service.findUserByToken("bad")).isEmpty();
    }

    @Test
    void returnsUserWhenTokenValid() {
        User user = new User();
        user.setId(1L);
        SessionToken token = new SessionToken();
        token.setUser(user);
        token.setExpiresAt(Instant.now().plusSeconds(60));

        when(sessionTokenRepository.findByTokenAndActiveTrue("good")).thenReturn(Optional.of(token));

        assertThat(service.findUserByToken("good")).contains(user);
    }

    @Test
    void returnsEmptyWhenExpired() {
        User user = new User();
        SessionToken token = new SessionToken();
        token.setUser(user);
        token.setExpiresAt(Instant.now().minusSeconds(5));

        when(sessionTokenRepository.findByTokenAndActiveTrue("expired")).thenReturn(Optional.of(token));

        assertThat(service.findUserByToken("expired")).isEmpty();
    }
}
