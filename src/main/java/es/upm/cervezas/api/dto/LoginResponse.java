package es.upm.cervezas.api.dto;

public record LoginResponse(
        String token,
        String message
) {
}
