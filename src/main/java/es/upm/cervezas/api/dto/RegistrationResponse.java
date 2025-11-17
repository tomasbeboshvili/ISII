package es.upm.cervezas.api.dto;

public record RegistrationResponse(
        String message,
        boolean activated
) {
}
