package es.upm.cervezas.repository;

import es.upm.cervezas.domain.Achievement;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    Optional<Achievement> findFirstByThresholdLessThanEqualOrderByThresholdDesc(int points);
}
