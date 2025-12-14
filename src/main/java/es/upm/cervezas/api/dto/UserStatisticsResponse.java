package es.upm.cervezas.api.dto;

public record UserStatisticsResponse(
		long totalBeersRated,
		long totalTastings,
		double averageBeerRating,
		double averageTastingRating) {
}
