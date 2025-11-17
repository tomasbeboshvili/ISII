package es.upm.cervezas.service;

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

/**
 * Servicio encargado de gestionar el ciclo de vida de los usuarios: registro (con activación
 * automática), login/logout y recuperación de credenciales. Encapsula todas las reglas de
 * negocio relacionadas con la autenticación.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static final Duration SESSION_DURATION = Duration.ofHours(12);
    private static final Duration RESET_DURATION = Duration.ofMinutes(30);

    private final UserRepository userRepository;
    private final SessionTokenRepository sessionTokenRepository;
    private final AgeVerificationService ageVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    public AuthService(UserRepository userRepository,
                       SessionTokenRepository sessionTokenRepository,
                       AgeVerificationService ageVerificationService,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.sessionTokenRepository = sessionTokenRepository;
        this.ageVerificationService = ageVerificationService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Registra un nuevo usuario (comprobando edad y unicidad de email/username) y lo deja listo
     * para iniciar sesión inmediatamente. Se envía un correo simulado de bienvenida sin necesidad
     * de validar ningún token.
     */
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
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setBirthDate(request.birthDate());
        user.setBirthday(request.birthday() != null ? request.birthday() : request.birthDate());
        user.setCity(request.city());
        user.setCountry(request.country());
        user.setBio(request.bio());
        user.setActivated(true);
        user.setActivationToken(null);
        user.setActivationTokenCreatedAt(null);

        userRepository.save(user);
        log.debug("Usuario {} registrado y activado automáticamente", user.getId());
        emailService.send(user.getEmail(), "Bienvenido a BeerSP",
                "Tu cuenta ya está activa y puedes iniciar sesión de inmediato.");

        return new RegistrationResponse(
                "Registro exitoso. Tu cuenta está activa y lista.",
                true
        );
    }

    /**
     * Endpoint de compatibilidad: se mantiene para informar de que la activación manual
     * ya no es necesaria.
     */
    @Transactional
    public ActivationResponse activate() {
        log.info("Solicitud de activación ignorada: todas las cuentas se activan al registrarse");
        return new ActivationResponse("La activación manual ya no es necesaria.");
    }

    /**
     * Inicia sesión comprobando credenciales y devuelve el token de sesión generado.
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (!user.isActivated()) {
            throw new IllegalStateException("Debes contactar con soporte: la cuenta no figura activa");
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

    /**
     * Marca como inactivo el token proporcionado (si existe).
     */
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

        emailService.send(user.getEmail(), "Recupera tu contraseña BeerSP",
                "Tu código de recuperación es: " + user.getPasswordResetToken());

        return new PasswordRecoveryResponse(
                "Solicitud registrada. Revisa el correo simulado con tu código.",
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
