package es.upm.cervezas.service;

import es.upm.cervezas.api.dto.ActivationRequest;
import es.upm.cervezas.api.dto.ActivationResponse;
import es.upm.cervezas.api.dto.LoginRequest;
import es.upm.cervezas.api.dto.LoginResponse;
import es.upm.cervezas.api.dto.PasswordRecoveryRequest;
import es.upm.cervezas.api.dto.PasswordRecoveryResponse;
import es.upm.cervezas.api.dto.PasswordResetRequest;
import es.upm.cervezas.api.dto.RegistrationRequest;
import es.upm.cervezas.api.dto.RegistrationResponse;
import es.upm.cervezas.domain.SessionToken;
import es.upm.cervezas.domain.User;
import es.upm.cervezas.repository.SessionTokenRepository;
import es.upm.cervezas.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static final Duration SESSION_DURATION = Duration.ofHours(12);
    private static final Duration RESET_DURATION = Duration.ofMinutes(30);

    private final UserRepository userRepository;
    private final SessionTokenRepository sessionTokenRepository;
    private final AgeVerificationService ageVerificationService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       SessionTokenRepository sessionTokenRepository,
                       AgeVerificationService ageVerificationService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sessionTokenRepository = sessionTokenRepository;
        this.ageVerificationService = ageVerificationService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegistrationResponse register(RegistrationRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Ya existe una cuenta con este email");
        }
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso");
        }

        log.info("Registrando nuevo usuario {}", request.email());
        ageVerificationService.verifyOrThrow(request.birthDate());

        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setUsername(request.username().toLowerCase());
        user.setDisplayName(request.displayName());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhotoUrl(request.photoUrl());
        user.setOrigin(request.origin());
        user.setIntro(request.intro());
        user.setLocation(request.location());
        user.setGender(request.gender());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setBirthDate(request.birthDate());
        user.setBirthday(request.birthday() != null ? request.birthday() : request.birthDate());
        user.setCity(request.city());
        user.setCountry(request.country());
        user.setBio(request.bio());
        user.setActivated(false);
        user.setActivationToken(UUID.randomUUID().toString());
        user.setActivationTokenCreatedAt(Instant.now());

        userRepository.save(user);
        log.debug("Usuario {} registrado pendiente de activación", user.getId());

        return new RegistrationResponse(
                "Registro exitoso. Completa la activación desde el email simulado",
                user.getActivationToken()
        );
    }

    @Transactional
    public ActivationResponse activate(ActivationRequest request) {
        User user = userRepository.findByActivationToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Token de activación inválido"));

        user.setActivated(true);
        user.setActivationToken(null);
        user.setActivationTokenCreatedAt(null);

        log.info("Usuario {} activado correctamente", user.getId());
        return new ActivationResponse("Cuenta activada correctamente");
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (!user.isActivated()) {
            throw new IllegalStateException("Debes activar la cuenta antes de iniciar sesión");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        SessionToken sessionToken = new SessionToken();
        sessionToken.setToken(UUID.randomUUID().toString());
        sessionToken.setUser(user);
        sessionToken.setActive(true);
        sessionToken.setCreatedAt(Instant.now());
        sessionToken.setExpiresAt(Instant.now().plus(SESSION_DURATION));

        sessionTokenRepository.save(sessionToken);

        log.info("Inicio de sesión exitoso para {}", user.getEmail());
        return new LoginResponse(sessionToken.getToken(), "Inicio de sesión correcto");
    }

    @Transactional
    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        Optional<SessionToken> maybeToken = sessionTokenRepository.findByTokenAndActiveTrue(token);
        maybeToken.ifPresent(sessionToken -> {
            sessionToken.setActive(false);
            log.info("Token {} invalidado", sessionToken.getToken());
        });
    }

    @Transactional
    public PasswordRecoveryResponse startPasswordRecovery(PasswordRecoveryRequest request) {
        Optional<User> maybeUser = userRepository.findByEmailIgnoreCase(request.email());
        if (maybeUser.isEmpty()) {
            log.warn("Recuperación solicitada para email inexistente {}", request.email());
            return new PasswordRecoveryResponse("Si el email existe, recibirás instrucciones", null);
        }

        User user = maybeUser.get();
        user.setPasswordResetToken(UUID.randomUUID().toString());
        user.setPasswordResetExpiresAt(Instant.now().plus(RESET_DURATION));

        return new PasswordRecoveryResponse(
                "Solicitud registrada. Usa el token simulado para restablecer tu contraseña",
                user.getPasswordResetToken()
        );
    }

    @Transactional
    public ActivationResponse resetPassword(PasswordResetRequest request) {
        User user = userRepository.findByPasswordResetToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Token de recuperación inválido"));

        if (user.getPasswordResetExpiresAt() == null || user.getPasswordResetExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("El token ha expirado");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);

        log.info("Contraseña actualizada para usuario {}", user.getId());
        return new ActivationResponse("Contraseña actualizada");
    }
}
