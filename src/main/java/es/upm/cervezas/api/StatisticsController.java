package es.upm.cervezas.api;

import es.upm.cervezas.api.dto.UserStatisticsResponse;
import es.upm.cervezas.domain.User;
import es.upm.cervezas.service.StatisticsService;
import es.upm.cervezas.service.UserProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

	private final StatisticsService statisticsService;
	private final UserProfileService userProfileService;

	public StatisticsController(StatisticsService statisticsService, UserProfileService userProfileService) {
		this.statisticsService = statisticsService;
		this.userProfileService = userProfileService;
	}

	@GetMapping("/me")
	public UserStatisticsResponse getMyStatistics(@RequestHeader("X-Auth-Token") String token) {
		User user = userProfileService.requireUser(token);
		return statisticsService.getUserStatistics(user);
	}
}
