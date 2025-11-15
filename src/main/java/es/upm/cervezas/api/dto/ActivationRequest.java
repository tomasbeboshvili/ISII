package es.upm.cervezas.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ActivationRequest(@NotBlank String token) {
}
