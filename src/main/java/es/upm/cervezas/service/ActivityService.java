package es.upm.cervezas.service;

import es.upm.cervezas.api.dto.FeedItem;
import es.upm.cervezas.domain.BeerRating;
import es.upm.cervezas.domain.Tasting;
import es.upm.cervezas.domain.User;
import es.upm.cervezas.repository.BeerRatingRepository;
import es.upm.cervezas.repository.TastingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ActivityService {

	private final TastingRepository tastingRepository;
	private final BeerRatingRepository beerRatingRepository;
	private final FriendshipService friendshipService;

	public ActivityService(TastingRepository tastingRepository, BeerRatingRepository beerRatingRepository,
			FriendshipService friendshipService) {
		this.tastingRepository = tastingRepository;
		this.beerRatingRepository = beerRatingRepository;
		this.friendshipService = friendshipService;
	}

	public List<FeedItem> getFriendActivity(User user) {
		List<User> friends = friendshipService.getFriends(user);
		if (friends.isEmpty()) {
			return new ArrayList<>();
		}

		List<Tasting> tastings = tastingRepository.findByUserInOrderByTastingDateDesc(friends);
		List<BeerRating> ratings = beerRatingRepository.findByUserInOrderByCreatedAtDesc(friends);

		List<FeedItem> feed = new ArrayList<>();

		for (Tasting t : tastings) {
			feed.add(new FeedItem(
					"TASTING",
					t.getId(),
					t.getUser().getId(),
					t.getUser().getUsername(),
					t.getUser().getPhotoUrl(),
					t.getBeer().getId(),
					t.getBeer().getName(),
					t.getTastingDate().atZone(ZoneId.systemDefault()).toInstant(),
					t));
		}

		for (BeerRating r : ratings) {
			feed.add(new FeedItem(
					"RATING",
					r.getId(),
					r.getUser().getId(),
					r.getUser().getUsername(),
					r.getUser().getPhotoUrl(),
					r.getBeer().getId(),
					r.getBeer().getName(),
					r.getCreatedAt(),
					r));
		}

		return feed.stream()
				.sorted(Comparator.comparing(FeedItem::getTimestamp).reversed())
				.collect(Collectors.toList());
	}
}
