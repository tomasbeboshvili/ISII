package es.upm.cervezas.service;

import es.upm.cervezas.api.dto.UserStatisticsResponse;
import es.upm.cervezas.domain.BeerRating;
import es.upm.cervezas.domain.Tasting;
import es.upm.cervezas.domain.User;
import es.upm.cervezas.repository.BeerRatingRepository;
import es.upm.cervezas.repository.TastingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StatisticsService {

	private final BeerRatingRepository beerRatingRepository;
	private final TastingRepository tastingRepository;

	public StatisticsService(BeerRatingRepository beerRatingRepository, TastingRepository tastingRepository) {
		this.beerRatingRepository = beerRatingRepository;
		this.tastingRepository = tastingRepository;
	}

	public UserStatisticsResponse getUserStatistics(User user) {
		List<BeerRating> ratings = beerRatingRepository.findByUser(user);
		List<Tasting> tastings = tastingRepository.findByUserOrderByTastingDateDesc(user);

		long totalBeersRated = ratings.size();
		double averageBeerRating = ratings.stream()
				.mapToInt(BeerRating::getScore)
				.average()
				.orElse(0.0);

		long totalTastings = tastings.size();
		double averageTastingRating = tastings.stream()
				.mapToDouble(t -> (t.getAromaScore() + t.getFlavorScore() + t.getAppearanceScore()) / 3.0)
				.average()
				.orElse(0.0);

		return new UserStatisticsResponse(
				totalBeersRated,
				totalTastings,
				Math.round(averageBeerRating * 100.0) / 100.0,
				Math.round(averageTastingRating * 100.0) / 100.0);
	}
}
