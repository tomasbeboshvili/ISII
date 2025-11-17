package es.upm.cervezas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    @Mock
    private EmailService emailService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, sessionTokenRepository, ageVerificationService, passwordEncoder, emailService);
    }

    @Test
    void registerCreatesActiveUser() {
        RegistrationRequest request = new RegistrationRequest(
                "test@example.com",
                "tester",
                "Tester",
                "Nombre",
                "Apellidos",
                null,
                "secret",
                LocalDate.now().minusYears(20),
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
        assertThat(saved.isActivated()).isTrue();
        assertThat(saved.getActivationToken()).isNull();
        assertThat(saved.getPasswordHash()).isEqualTo("encoded");
        assertThat(response.activated()).isTrue();
        verify(ageVerificationService).verifyOrThrow(request.birthDate());
        verify(emailService).send(eq("test@example.com"), any(), any());
    }

    @Test
    void activateEndpointIsNoOp() {
        var response = authService.activate();
        assertThat(response.message()).contains("no es necesaria");
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
