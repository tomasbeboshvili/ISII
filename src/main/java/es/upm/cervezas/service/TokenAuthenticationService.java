package es.upm.cervezas.service;

import es.upm.cervezas.domain.SessionToken;
import es.upm.cervezas.domain.User;
import es.upm.cervezas.repository.SessionTokenRepository;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenAuthenticationService {

    private final SessionTokenRepository sessionTokenRepository;

    public TokenAuthenticationService(SessionTokenRepository sessionTokenRepository) {
        this.sessionTokenRepository = sessionTokenRepository;
    }

    @Transactional(readOnly = true)
    public Optional<User> findUserByToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        return sessionTokenRepository.findByTokenAndActiveTrue(token)
                .filter(st -> st.getExpiresAt().isAfter(Instant.now()))
                .map(SessionToken::getUser);
    }
}
