package es.upm.cervezas.repository;

import es.upm.cervezas.domain.BeerRating;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BeerRatingRepository extends JpaRepository<BeerRating, Long> {
    Optional<BeerRating> findByBeerIdAndUserId(Long beerId, Long userId);

    List<BeerRating> findByBeerId(Long beerId);

    @Query("select avg(br.score) from BeerRating br where br.beer.id = :beerId")
    Double findAverageScoreByBeerId(Long beerId);

    long countByBeerId(Long beerId);
}
