package es.upm.cervezas.service;

import es.upm.cervezas.api.dto.UserStatisticsResponse;
import es.upm.cervezas.domain.BeerRating;
import es.upm.cervezas.domain.Tasting;
import es.upm.cervezas.domain.User;
import es.upm.cervezas.repository.BeerRatingRepository;
import es.upm.cervezas.repository.TastingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

	@Mock
	private BeerRatingRepository beerRatingRepository;

	@Mock
	private TastingRepository tastingRepository;

	@InjectMocks
	private StatisticsService statisticsService;

	private User user;

	@BeforeEach
	void setUp() {
		user = new User();
		user.setId(1L);
	}

	@Test
	void getUserStatistics_CalculatesCorrectly() {
		// Mock Ratings
		BeerRating r1 = new BeerRating();
		r1.setScore(8);
		BeerRating r2 = new BeerRating();
		r2.setScore(10);
		when(beerRatingRepository.findByUser(user)).thenReturn(List.of(r1, r2));

		// Mock Tastings
		Tasting t1 = new Tasting();
		t1.setAromaScore(8);
		t1.setFlavorScore(8);
		t1.setAppearanceScore(8); // Avg 8

		Tasting t2 = new Tasting();
		t2.setAromaScore(10);
		t2.setFlavorScore(10);
		t2.setAppearanceScore(10); // Avg 10

		when(tastingRepository.findByUserOrderByTastingDateDesc(user)).thenReturn(List.of(t1, t2));

		UserStatisticsResponse stats = statisticsService.getUserStatistics(user);

		assertEquals(2, stats.totalBeersRated());
		assertEquals(9.0, stats.averageBeerRating());
		assertEquals(2, stats.totalTastings());
		assertEquals(9.0, stats.averageTastingRating());
	}

	@Test
	void getUserStatistics_EmptyData_ReturnsZeros() {
		when(beerRatingRepository.findByUser(user)).thenReturn(List.of());
		when(tastingRepository.findByUserOrderByTastingDateDesc(user)).thenReturn(List.of());

		UserStatisticsResponse stats = statisticsService.getUserStatistics(user);

		assertEquals(0, stats.totalBeersRated());
		assertEquals(0.0, stats.averageBeerRating());
		assertEquals(0, stats.totalTastings());
		assertEquals(0.0, stats.averageTastingRating());
	}
}
