package es.upm.cervezas.repository;

import es.upm.cervezas.domain.Friendship;
import es.upm.cervezas.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

	// Check if a relationship already exists (in either direction)
	@Query("SELECT f FROM Friendship f WHERE (f.requester = :u1 AND f.addressee = :u2) OR (f.requester = :u2 AND f.addressee = :u1)")
	Optional<Friendship> findRelationship(@Param("u1") User u1, @Param("u2") User u2);

	// Find pending requests received by user
	List<Friendship> findByAddresseeAndStatus(User addressee, Friendship.Status status);

	// Find all accepted friendships for a user (either requester or addressee)
	@Query("SELECT f FROM Friendship f WHERE (f.requester = :user OR f.addressee = :user) AND f.status = 'ACCEPTED'")
	List<Friendship> findAllAcceptedFriendships(@Param("user") User user);
}
