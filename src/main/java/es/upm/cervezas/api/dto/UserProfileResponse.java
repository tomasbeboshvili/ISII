package es.upm.cervezas.api.dto;

import java.time.Instant;
import java.time.LocalDate;

public record UserProfileResponse(
        Long id,
        String email,
        String username,
        String displayName,
        String firstName,
        String lastName,
        String photoUrl,
        String origin,
        String intro,
        String location,
        String gender,
        LocalDate birthDate,
        String city,
        String country,
        String bio,
        Integer badgeLevel,
        int gamificationPoints,
        Long currentAchievementId,
        boolean activated,
        Instant createdAt,
        Instant updatedAt,
        int beersCreatedCount,
        int tastingsCount,
        int ratingsCount
) {
}
