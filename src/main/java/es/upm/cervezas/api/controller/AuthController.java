package es.upm.cervezas.api.controller;

import es.upm.cervezas.api.dto.ActivationResponse;
import es.upm.cervezas.api.dto.LoginRequest;
import es.upm.cervezas.api.dto.LoginResponse;
import es.upm.cervezas.api.dto.PasswordRecoveryRequest;
import es.upm.cervezas.api.dto.PasswordRecoveryResponse;
import es.upm.cervezas.api.dto.PasswordResetRequest;
import es.upm.cervezas.api.dto.RegistrationRequest;
import es.upm.cervezas.api.dto.RegistrationResponse;
import es.upm.cervezas.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints públicos para registrar, activar, iniciar sesión y gestionar las credenciales
 * de los usuarios (recuperar / resetear contraseña).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public RegistrationResponse register(@Valid @RequestBody RegistrationRequest request) {
        log.info("Nueva solicitud de registro para {}", request.email());
        return authService.register(request);
    }

    @PostMapping("/activate")
    public ActivationResponse activate() {
        log.info("Solicitud de activación recibida (modo compatibilidad)");
        return authService.activate();
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        log.info("Intento de login para {}", request.email());
        return authService.login(request);
    }

    @PostMapping("/logout")
    public ActivationResponse logout(@RequestHeader(name = "X-Auth-Token", required = false) String token) {
        log.info("Logout solicitado");
        authService.logout(token);
        return new ActivationResponse("Sesión cerrada");
    }

    @PostMapping("/password/recover")
    public PasswordRecoveryResponse recover(@Valid @RequestBody PasswordRecoveryRequest request) {
        log.info("Inicio de recuperación para {}", request.email());
        return authService.startPasswordRecovery(request);
    }

    @PostMapping("/password/reset")
    public ActivationResponse reset(@Valid @RequestBody PasswordResetRequest request) {
        log.info("Solicitud de reseteo de contraseña recibida");
        return authService.resetPassword(request);
    }
}
