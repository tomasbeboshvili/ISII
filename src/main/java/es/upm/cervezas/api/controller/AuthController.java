package es.upm.cervezas.api.controller;

import es.upm.cervezas.api.dto.ActivationRequest;
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
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public RegistrationResponse register(@Valid @RequestBody RegistrationRequest request) {
        return authService.register(request);
    }

    @PostMapping("/activate")
    public ActivationResponse activate(@Valid @RequestBody ActivationRequest request) {
        return authService.activate(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public ActivationResponse logout(@RequestHeader(name = "X-Auth-Token", required = false) String token) {
        authService.logout(token);
        return new ActivationResponse("Sesión cerrada");
    }

    @PostMapping("/password/recover")
    public PasswordRecoveryResponse recover(@Valid @RequestBody PasswordRecoveryRequest request) {
        return authService.startPasswordRecovery(request);
    }

    @PostMapping("/password/reset")
    public ActivationResponse reset(@Valid @RequestBody PasswordResetRequest request) {
        return authService.resetPassword(request);
    }
}
