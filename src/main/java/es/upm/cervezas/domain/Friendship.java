package es.upm.cervezas.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "friendships", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "requester_id", "addressee_id" })
})
public class Friendship {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "requester_id", nullable = false)
	private User requester;

	@ManyToOne
	@JoinColumn(name = "addressee_id", nullable = false)
	private User addressee;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status;

	private Instant createdAt;
	private Instant updatedAt;

	public enum Status {
		PENDING,
		ACCEPTED,
		REJECTED
	}

	@PrePersist
	public void onCreate() {
		this.createdAt = Instant.now();
		this.updatedAt = Instant.now();
	}

	@PreUpdate
	public void onUpdate() {
		this.updatedAt = Instant.now();
	}

	// Getters and Setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getRequester() {
		return requester;
	}

	public void setRequester(User requester) {
		this.requester = requester;
	}

	public User getAddressee() {
		return addressee;
	}

	public void setAddressee(User addressee) {
		this.addressee = addressee;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
