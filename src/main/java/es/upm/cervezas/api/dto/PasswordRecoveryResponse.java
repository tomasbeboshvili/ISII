package es.upm.cervezas.api.dto;

public record PasswordRecoveryResponse(
        String message,
        String resetToken
) {
}
