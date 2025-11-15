package es.upm.cervezas.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

public record RegistrationRequest(
        @NotBlank @Email String email,
        @NotBlank String username,
        @NotBlank String displayName,
        String firstName,
        String lastName,
        String photoUrl,
        String origin,
        String intro,
        String location,
        String gender,
        @NotBlank String password,
        @NotNull @Past LocalDate birthDate,
        LocalDate birthday,
        String city,
        String country,
        String bio
) {
}
