package es.upm.cervezas.repository;

import es.upm.cervezas.domain.Beer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeerRepository extends JpaRepository<Beer, Long> {

    long countByCreatedById(Long userId);
}
