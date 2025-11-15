package es.upm.cervezas.service;

import es.upm.cervezas.api.dto.UserProfileResponse;
import es.upm.cervezas.api.dto.UserProfileUpdateRequest;
import es.upm.cervezas.domain.User;
import es.upm.cervezas.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final TokenAuthenticationService tokenAuthenticationService;
    private final UserRepository userRepository;

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
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(String token, UserProfileUpdateRequest request) {
        User user = requireUser(token);
        if (request.displayName() != null) {
            user.setDisplayName(request.displayName());
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
        return toResponse(user);
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getBirthDate(),
                user.getCity(),
                user.getCountry(),
                user.getBio(),
                user.isActivated(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
