package es.upm.cervezas.api;

import es.upm.cervezas.domain.Friendship;
import es.upm.cervezas.domain.User;
import es.upm.cervezas.service.FriendshipService;
import es.upm.cervezas.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
public class FriendshipController {

	private final FriendshipService friendshipService;
	private final UserProfileService userProfileService;

	public FriendshipController(FriendshipService friendshipService, UserProfileService userProfileService) {
		this.friendshipService = friendshipService;
		this.userProfileService = userProfileService;
	}

	@GetMapping("/search")
	public List<User> searchUsers(@RequestParam String q) {
		return friendshipService.searchUsers(q);
	}

	@PostMapping("/request/{userId}")
	public ResponseEntity<Void> sendRequest(@RequestHeader("X-Auth-Token") String token, @PathVariable Long userId) {
		User user = userProfileService.requireUser(token);
		friendshipService.sendRequest(user, userId);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/pending")
	public List<Friendship> getPendingRequests(@RequestHeader("X-Auth-Token") String token) {
		User user = userProfileService.requireUser(token);
		return friendshipService.getPendingRequests(user);
	}

	@GetMapping
	public List<User> getFriends(@RequestHeader("X-Auth-Token") String token) {
		User user = userProfileService.requireUser(token);
		return friendshipService.getFriends(user);
	}

	@PostMapping("/request/{requestId}/resolve")
	public ResponseEntity<Void> resolveRequest(@RequestHeader("X-Auth-Token") String token,
			@PathVariable Long requestId,
			@RequestBody Map<String, Boolean> body) {
		User user = userProfileService.requireUser(token);
		boolean accept = body.getOrDefault("accept", false);
		friendshipService.resolveRequest(user, requestId, accept);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{friendId}")
	public ResponseEntity<Void> deleteFriend(@RequestHeader("X-Auth-Token") String token, @PathVariable Long friendId) {
		User user = userProfileService.requireUser(token);
		friendshipService.deleteFriendship(user, friendId);
		return ResponseEntity.ok().build();
	}
}
