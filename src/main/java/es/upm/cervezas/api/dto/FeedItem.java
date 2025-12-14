package es.upm.cervezas.api.dto;

import java.time.Instant;

public class FeedItem {
	private String type; // "TASTING" or "RATING"
	private Long id;
	private Long userId;
	private String username;
	private String userPhotoUrl;
	private Long beerId;
	private String beerName;
	private Instant timestamp;
	private Object details; // Tasting or Rating details

	public FeedItem(String type, Long id, Long userId, String username, String userPhotoUrl, Long beerId,
			String beerName, Instant timestamp, Object details) {
		this.type = type;
		this.id = id;
		this.userId = userId;
		this.username = username;
		this.userPhotoUrl = userPhotoUrl;
		this.beerId = beerId;
		this.beerName = beerName;
		this.timestamp = timestamp;
		this.details = details;
	}

	// Getters
	public String getType() {
		return type;
	}

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
	}

	public String getUserPhotoUrl() {
		return userPhotoUrl;
	}

	public Long getBeerId() {
		return beerId;
	}

	public String getBeerName() {
		return beerName;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public Object getDetails() {
		return details;
	}
}
