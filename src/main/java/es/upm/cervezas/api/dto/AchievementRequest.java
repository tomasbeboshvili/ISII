package es.upm.cervezas.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AchievementRequest(
        @NotBlank String name,
        String topic,
        String imageUrl,
        @Min(0) int level,
        String criteria,
        @Min(0) int threshold
) {
}
