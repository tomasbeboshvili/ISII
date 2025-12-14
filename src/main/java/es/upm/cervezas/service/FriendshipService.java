package es.upm.cervezas.service;

import es.upm.cervezas.domain.Friendship;
import es.upm.cervezas.domain.User;
import es.upm.cervezas.repository.FriendshipRepository;
import es.upm.cervezas.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class FriendshipService {

	private final FriendshipRepository friendshipRepository;
	private final UserRepository userRepository;

	public FriendshipService(FriendshipRepository friendshipRepository, UserRepository userRepository) {
		this.friendshipRepository = friendshipRepository;
		this.userRepository = userRepository;
	}

	public List<User> searchUsers(String query) {
		return userRepository.findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(query, query);
	}

	public void sendRequest(User requester, Long addresseeId) {
		if (requester.getId().equals(addresseeId)) {
			throw new IllegalArgumentException("Cannot send friend request to yourself");
		}

		User addressee = userRepository.findById(addresseeId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		Optional<Friendship> existing = friendshipRepository.findRelationship(requester, addressee);
		if (existing.isPresent()) {
			throw new IllegalStateException("Friendship or request already exists");
		}

		Friendship friendship = new Friendship();
		friendship.setRequester(requester);
		friendship.setAddressee(addressee);
		friendship.setStatus(Friendship.Status.PENDING);
		friendshipRepository.save(friendship);
	}

	public List<Friendship> getPendingRequests(User user) {
		return friendshipRepository.findByAddresseeAndStatus(user, Friendship.Status.PENDING);
	}

	public List<User> getFriends(User user) {
		List<Friendship> friendships = friendshipRepository.findAllAcceptedFriendships(user);
		return friendships.stream()
				.map(f -> f.getRequester().equals(user) ? f.getAddressee() : f.getRequester())
				.collect(Collectors.toList());
	}

	public void resolveRequest(User user, Long requestId, boolean accept) {
		Friendship request = friendshipRepository.findById(requestId)
				.orElseThrow(() -> new IllegalArgumentException("Request not found"));

		if (!request.getAddressee().getId().equals(user.getId())) {
			throw new SecurityException("Not authorized to resolve this request");
		}

		if (request.getStatus() != Friendship.Status.PENDING) {
			throw new IllegalStateException("Request is not pending");
		}

		if (accept) {
			request.setStatus(Friendship.Status.ACCEPTED);
			friendshipRepository.save(request);
		} else {
			friendshipRepository.delete(request); // Or set to REJECTED
		}
	}

	public void deleteFriendship(User user, Long friendId) {
		User friend = userRepository.findById(friendId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		Friendship friendship = friendshipRepository.findRelationship(user, friend)
				.orElseThrow(() -> new IllegalArgumentException("Friendship not found"));

		friendshipRepository.delete(friendship);
	}
}
