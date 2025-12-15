package es.upm.cervezas.service;

import es.upm.cervezas.api.dto.FeedItem;
import es.upm.cervezas.domain.Beer;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

	@Mock
	private TastingRepository tastingRepository;

	@Mock
	private BeerRatingRepository beerRatingRepository;

	@Mock
	private FriendshipService friendshipService;

	@InjectMocks
	private ActivityService activityService;

	private User user;
	private User friend;
	private Beer beer;

	@BeforeEach
	void setUp() {
		user = new User();
		user.setId(1L);

		friend = new User();
		friend.setId(2L);
		friend.setUsername("friend");

		beer = new Beer();
		beer.setId(100L);
		beer.setName("Test Beer");
	}

	@Test
	void getFriendActivity_ReturnsSortedFeed() {
		// Mock Friends
		when(friendshipService.getFriends(user)).thenReturn(List.of(friend));

		// Mock Tastings
		Tasting t1 = new Tasting();
		t1.setId(1L);
		t1.setUser(friend);
		t1.setBeer(beer);
		t1.setTastingDate(LocalDateTime.now().minusDays(1)); // Yesterday
		when(tastingRepository.findByUserInOrderByTastingDateDesc(List.of(friend))).thenReturn(List.of(t1));

		// Mock Ratings
		BeerRating r1 = new BeerRating();
		r1.setId(2L);
		r1.setUser(friend);
		r1.setBeer(beer);
		r1.setCreatedAt(Instant.now()); // Today (Newer)
		when(beerRatingRepository.findByUserInOrderByCreatedAtDesc(List.of(friend))).thenReturn(List.of(r1));

		List<FeedItem> feed = activityService.getFriendActivity(user);

		assertEquals(2, feed.size());

		// Should be sorted by date desc (Rating first, then Tasting)
		assertEquals("RATING", feed.get(0).getType());
		assertEquals("TASTING", feed.get(1).getType());
	}

	@Test
	void getFriendActivity_NoFriends_ReturnsEmpty() {
		when(friendshipService.getFriends(user)).thenReturn(List.of());

		List<FeedItem> feed = activityService.getFriendActivity(user);

		assertTrue(feed.isEmpty());
		verifyNoInteractions(tastingRepository);
		verifyNoInteractions(beerRatingRepository);
	}
}
