package es.upm.cervezas.api.controller;

import es.upm.cervezas.api.dto.UserProfileResponse;
import es.upm.cervezas.api.dto.UserProfileUpdateRequest;
import es.upm.cervezas.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserProfileService userProfileService;

    public UserController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public UserProfileResponse profile(@RequestHeader("X-Auth-Token") String token) {
        return userProfileService.getProfile(token);
    }

    @PutMapping
    public UserProfileResponse update(@RequestHeader("X-Auth-Token") String token,
                                      @Valid @RequestBody UserProfileUpdateRequest request) {
        return userProfileService.updateProfile(token, request);
    }
}
