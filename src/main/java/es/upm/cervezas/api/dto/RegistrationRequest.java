package es.upm.cervezas.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record RegistrationRequest(
        @NotBlank @Email String email,
        @NotBlank String username,
        @NotBlank String displayName,
        String firstName,
        String lastName,
        String intro,
        @Pattern(regexp = "Masculino|Femenino|Prefiero no decirlo", message = "Género inválido")
        String gender,
        @NotBlank String password,
        @NotBlank String confirmPassword,
        @NotNull @Past LocalDate birthDate,
        String city,
        String country
) {
}
