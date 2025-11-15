package es.upm.cervezas.api.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public record TastingResponse(
        Long id,
        Long beerId,
        String beerName,
        LocalDateTime tastingDate,
        String location,
        String notes,
        int aromaScore,
        int flavorScore,
        int appearanceScore,
        Instant createdAt
) {
}
