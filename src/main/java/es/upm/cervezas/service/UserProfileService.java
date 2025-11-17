package es.upm.cervezas.service;

import es.upm.cervezas.api.dto.UserProfileResponse;
import es.upm.cervezas.api.dto.UserProfileUpdateRequest;
import es.upm.cervezas.domain.User;
import es.upm.cervezas.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final TokenAuthenticationService tokenAuthenticationService;
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(UserProfileService.class);

    public UserProfileService(TokenAuthenticationService tokenAuthenticationService,
                              UserRepository userRepository) {
        this.tokenAuthenticationService = tokenAuthenticationService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User requireUser(String token) {
        return tokenAuthenticationService.findUserByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido o caducado"));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String token) {
        User user = requireUser(token);
        log.debug("Perfil recuperado para {}", user.getEmail());
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(String token, UserProfileUpdateRequest request) {
        User user = requireUser(token);
        if (request.displayName() != null) {
            user.setDisplayName(request.displayName());
        }
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.photoUrl() != null) {
            user.setPhotoUrl(request.photoUrl());
        }
        if (request.origin() != null) {
            user.setOrigin(request.origin());
        }
        if (request.intro() != null) {
            user.setIntro(request.intro());
        }
        if (request.location() != null) {
            user.setLocation(request.location());
        }
        if (request.gender() != null) {
            user.setGender(request.gender());
        }
        if (request.city() != null) {
            user.setCity(request.city());
        }
        if (request.country() != null) {
            user.setCountry(request.country());
        }
        if (request.bio() != null) {
            user.setBio(request.bio());
        }
        // persist changes so the userRepository field is used
        userRepository.save(user);
        log.info("Perfil actualizado para usuario {}", user.getId());
        return toResponse(user);
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getDisplayName(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhotoUrl(),
                user.getOrigin(),
                user.getIntro(),
                user.getLocation(),
                user.getGender(),
                user.getBirthDate(),
                user.getCity(),
                user.getCountry(),
                user.getBio(),
                user.getBadgeLevel(),
                user.getGamificationPoints(),
                user.getCurrentAchievementId(),
                user.isActivated(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getBeersCreatedCount(),
                user.getTastingsCount(),
                user.getRatingsCount()
        );
    }
}
