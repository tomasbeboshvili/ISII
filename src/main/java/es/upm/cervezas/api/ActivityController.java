package es.upm.cervezas.api;

import es.upm.cervezas.api.dto.FeedItem;
import es.upm.cervezas.domain.User;
import es.upm.cervezas.service.ActivityService;
import es.upm.cervezas.service.UserProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

	private final ActivityService activityService;
	private final UserProfileService userProfileService;

	public ActivityController(ActivityService activityService, UserProfileService userProfileService) {
		this.activityService = activityService;
		this.userProfileService = userProfileService;
	}

	@GetMapping("/feed")
	public List<FeedItem> getFriendFeed(@RequestHeader("X-Auth-Token") String token) {
		User user = userProfileService.requireUser(token);
		return activityService.getFriendActivity(user);
	}
}
