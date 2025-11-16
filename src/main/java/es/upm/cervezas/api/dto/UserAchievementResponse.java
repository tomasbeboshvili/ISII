package es.upm.cervezas.api.dto;

import java.time.Instant;

public record UserAchievementResponse(
        Long achievementId,
        String name,
        String criteria,
        int threshold,
        Instant awardedAt
) {
}
