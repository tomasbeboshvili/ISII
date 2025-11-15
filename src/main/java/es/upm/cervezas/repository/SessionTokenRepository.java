package es.upm.cervezas.repository;

import es.upm.cervezas.domain.SessionToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionTokenRepository extends JpaRepository<SessionToken, Long> {
    Optional<SessionToken> findByTokenAndActiveTrue(String token);

    void deleteByExpiresAtBefore(Instant cutoff);
}
