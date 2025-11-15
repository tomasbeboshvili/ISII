package es.upm.cervezas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.upm.cervezas.api.dto.ActivationRequest;
import es.upm.cervezas.api.dto.LoginRequest;
import es.upm.cervezas.api.dto.PasswordRecoveryRequest;
import es.upm.cervezas.api.dto.PasswordResetRequest;
import es.upm.cervezas.api.dto.RegistrationRequest;
import es.upm.cervezas.domain.SessionToken;
import es.upm.cervezas.domain.User;
import es.upm.cervezas.repository.SessionTokenRepository;
import es.upm.cervezas.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private SessionTokenRepository sessionTokenRepository;
    @Mock
    private AgeVerificationService ageVerificationService;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, sessionTokenRepository, ageVerificationService, passwordEncoder);
    }

    @Test
    void registerCreatesPendingUser() {
        RegistrationRequest request = new RegistrationRequest(
                "test@example.com",
                "tester",
                "Tester",
                "Nombre",
                "Apellidos",
                null,
                null,
                null,
                null,
                null,
                "secret",
                LocalDate.now().minusYears(20),
                null,
                "Madrid",
                "España",
                "Bio"
        );
        when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase(request.username())).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
        assertThat(saved.isActivated()).isFalse();
        assertThat(saved.getActivationToken()).isNotBlank();
        assertThat(saved.getPasswordHash()).isEqualTo("encoded");
        assertThat(response.activationToken()).isEqualTo(saved.getActivationToken());
        verify(ageVerificationService).verifyOrThrow(request.birthDate());
    }

    @Test
    void activateMarksUser() {
        User user = new User();
        user.setActivationToken("token");
        when(userRepository.findByActivationToken("token")).thenReturn(Optional.of(user));

        authService.activate(new ActivationRequest("token"));

        assertThat(user.isActivated()).isTrue();
        assertThat(user.getActivationToken()).isNull();
    }

    @Test
    void loginGeneratesSession() {
        User user = new User();
        user.setActivated(true);
        user.setPasswordHash("encoded");
        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);
        when(sessionTokenRepository.save(any(SessionToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = authService.login(new LoginRequest("test@example.com", "secret"));

        assertThat(response.token()).isNotBlank();
        verify(sessionTokenRepository).save(any(SessionToken.class));
    }

    @Test
    void loginFailsIfNotActivated() {
        User user = new User();
        user.setActivated(false);
        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("test@example.com", "x")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void passwordRecoverySilentWhenUnknownEmail() {
        when(userRepository.findByEmailIgnoreCase("none@example.com")).thenReturn(Optional.empty());

        var response = authService.startPasswordRecovery(new PasswordRecoveryRequest("none@example.com"));

        assertThat(response.resetToken()).isNull();
    }

    @Test
    void resetPasswordFailsOnExpiredToken() {
        User user = new User();
        user.setPasswordResetToken("tok");
        user.setPasswordResetExpiresAt(Instant.now().minusSeconds(10));
        when(userRepository.findByPasswordResetToken("tok")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.resetPassword(new PasswordResetRequest("tok", "newpass")))
                .isInstanceOf(IllegalStateException.class);
    }
}
