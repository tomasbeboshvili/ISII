package es.upm.cervezas.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record BeerResponse(
        Long id,
        String name,
        String style,
        String brewery,
        String originCountry,
        BigDecimal abv,
        Integer ibu,
        String description,
        Instant createdAt,
        Double averageScore,
        Long ratingsCount
) {
}
