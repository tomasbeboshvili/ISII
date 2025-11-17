package es.upm.cervezas.api.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
        @Size(min = 2, max = 120) String displayName,
        String firstName,
        String lastName,
        @Size(max = 500) String photoUrl,
        String origin,
        @Size(max = 500) String intro,
        String location,
        @Pattern(regexp = "Masculino|Femenino|Prefiero no decirlo")
        String gender,
        String city,
        String country,
        @Size(max = 1000) String bio
) {
}
