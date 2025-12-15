package es.upm.cervezas.service;

import es.upm.cervezas.domain.Friendship;
import es.upm.cervezas.domain.User;
import es.upm.cervezas.repository.FriendshipRepository;
import es.upm.cervezas.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

	@Mock
	private FriendshipRepository friendshipRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private FriendshipService friendshipService;

	private User user1;
	private User user2;

	@BeforeEach
	void setUp() {
		user1 = new User();
		user1.setId(1L);
		user1.setUsername("user1");

		user2 = new User();
		user2.setId(2L);
		user2.setUsername("user2");
	}

	@Test
	void sendRequest_Success() {
		when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
		when(friendshipRepository.findRelationship(user1, user2)).thenReturn(Optional.empty());

		friendshipService.sendRequest(user1, 2L);

		verify(friendshipRepository).save(any(Friendship.class));
	}

	@Test
	void sendRequest_ToSelf_ThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> friendshipService.sendRequest(user1, 1L));
	}

	@Test
	void sendRequest_AlreadyExists_ThrowsException() {
		when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
		when(friendshipRepository.findRelationship(user1, user2)).thenReturn(Optional.of(new Friendship()));

		assertThrows(IllegalStateException.class, () -> friendshipService.sendRequest(user1, 2L));
	}

	@Test
	void resolveRequest_Accept_Success() {
		Friendship request = new Friendship();
		request.setId(10L);
		request.setRequester(user2);
		request.setAddressee(user1);
		request.setStatus(Friendship.Status.PENDING);

		when(friendshipRepository.findById(10L)).thenReturn(Optional.of(request));

		friendshipService.resolveRequest(user1, 10L, true);

		assertEquals(Friendship.Status.ACCEPTED, request.getStatus());
		verify(friendshipRepository).save(request);
	}

	@Test
	void resolveRequest_Reject_Success() {
		Friendship request = new Friendship();
		request.setId(10L);
		request.setRequester(user2);
		request.setAddressee(user1);
		request.setStatus(Friendship.Status.PENDING);

		when(friendshipRepository.findById(10L)).thenReturn(Optional.of(request));

		friendshipService.resolveRequest(user1, 10L, false);

		verify(friendshipRepository).delete(request);
	}

	@Test
	void resolveRequest_NotAuthorized_ThrowsException() {
		Friendship request = new Friendship();
		request.setId(10L);
		request.setRequester(user2);
		request.setAddressee(user2); // Addressee is user2, but user1 tries to resolve
		request.setStatus(Friendship.Status.PENDING);

		when(friendshipRepository.findById(10L)).thenReturn(Optional.of(request));

		assertThrows(SecurityException.class, () -> friendshipService.resolveRequest(user1, 10L, true));
	}

	@Test
	void getFriends_ReturnsList() {
		Friendship f1 = new Friendship();
		f1.setRequester(user1);
		f1.setAddressee(user2);
		f1.setStatus(Friendship.Status.ACCEPTED);

		when(friendshipRepository.findAllAcceptedFriendships(user1)).thenReturn(List.of(f1));

		List<User> friends = friendshipService.getFriends(user1);

		assertEquals(1, friends.size());
		assertEquals(user2, friends.get(0));
	}
}
