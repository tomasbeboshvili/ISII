package es.upm.cervezas.api.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserProfileUpdateRequest(
        @Size(min = 2, max = 120) String displayName,
        String firstName,
        String lastName,
        @Size(max = 500) String intro,
        String city,
        String country,
        @Size(max = 1000) String bio
) {
}
