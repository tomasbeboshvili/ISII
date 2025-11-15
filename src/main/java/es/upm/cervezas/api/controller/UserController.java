package es.upm.cervezas.api.controller;

import es.upm.cervezas.api.dto.UserProfileResponse;
import es.upm.cervezas.api.dto.UserProfileUpdateRequest;
import es.upm.cervezas.service.UserProfileService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserProfileService userProfileService;

    public UserController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public UserProfileResponse profile(@RequestHeader("X-Auth-Token") String token) {
        log.debug("Consulta de perfil para token {}", token);
        return userProfileService.getProfile(token);
    }

    @PutMapping
    public UserProfileResponse update(@RequestHeader("X-Auth-Token") String token,
                                      @Valid @RequestBody UserProfileUpdateRequest request) {
        log.info("Actualización de perfil solicitada");
        return userProfileService.updateProfile(token, request);
    }
}
