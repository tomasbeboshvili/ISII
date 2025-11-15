package es.upm.cervezas.api.dto;

import java.time.Instant;
import java.time.LocalDate;

public record UserProfileResponse(
        Long id,
        String email,
        String displayName,
        LocalDate birthDate,
        String city,
        String country,
        String bio,
        boolean activated,
        Instant createdAt,
        Instant updatedAt
) {
}
