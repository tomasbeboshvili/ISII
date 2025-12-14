package es.upm.cervezas.repository;

import es.upm.cervezas.domain.Tasting;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import es.upm.cervezas.domain.User;

public interface TastingRepository extends JpaRepository<Tasting, Long> {
	List<Tasting> findByUserId(Long userId);

	List<Tasting> findByBeerId(Long beerId);

	long countByUserId(Long userId);

	List<Tasting> findByUserInOrderByTastingDateDesc(List<es.upm.cervezas.domain.User> users);

	List<Tasting> findByUserOrderByTastingDateDesc(User user);
}
