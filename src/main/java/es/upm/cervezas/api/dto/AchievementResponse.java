package es.upm.cervezas.api.dto;

public record AchievementResponse(
        Long id,
        String name,
        String topic,
        String imageUrl,
        int level,
        String criteria,
        int threshold
) {
}
