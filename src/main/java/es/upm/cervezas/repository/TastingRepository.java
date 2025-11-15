package es.upm.cervezas.repository;

import es.upm.cervezas.domain.Tasting;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TastingRepository extends JpaRepository<Tasting, Long> {
    List<Tasting> findByUserId(Long userId);

    List<Tasting> findByBeerId(Long beerId);
}
