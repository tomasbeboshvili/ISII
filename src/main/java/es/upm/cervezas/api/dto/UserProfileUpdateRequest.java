package es.upm.cervezas.api.dto;

import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
        @Size(min = 2, max = 120) String displayName,
        String city,
        String country,
        @Size(max = 1000) String bio
) {
}
