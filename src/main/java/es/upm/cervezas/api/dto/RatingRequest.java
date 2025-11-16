package es.upm.cervezas.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RatingRequest(
        @NotNull Long beerId,
        @Min(1) @Max(10) int score,
        String comment
) {
}
