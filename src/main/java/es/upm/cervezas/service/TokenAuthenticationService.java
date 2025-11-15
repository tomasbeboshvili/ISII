package es.upm.cervezas.service;

import es.upm.cervezas.domain.SessionToken;
import es.upm.cervezas.domain.User;
import es.upm.cervezas.repository.SessionTokenRepository;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenAuthenticationService {

    private final SessionTokenRepository sessionTokenRepository;
    private static final Logger log = LoggerFactory.getLogger(TokenAuthenticationService.class);

    public TokenAuthenticationService(SessionTokenRepository sessionTokenRepository) {
        this.sessionTokenRepository = sessionTokenRepository;
    }

    @Transactional(readOnly = true)
    public Optional<User> findUserByToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        log.debug("Validando token {}", token);
        return sessionTokenRepository.findByTokenAndActiveTrue(token)
                .filter(st -> st.getExpiresAt().isAfter(Instant.now()))
                .map(SessionToken::getUser);
    }
}
