package es.upm.cervezas.repository;

import es.upm.cervezas.domain.UserAchievement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    boolean existsByUserIdAndAchievementId(Long userId, Long achievementId);

    List<UserAchievement> findByUserId(Long userId);
}
