package es.upm.cervezas.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record TastingRequest(
        @NotNull Long beerId,
        @NotNull LocalDateTime tastingDate,
        String location,
        String notes,
        @Min(1) @Max(5) int aromaScore,
        @Min(1) @Max(5) int flavorScore,
        @Min(1) @Max(5) int appearanceScore
) {
}
