package es.upm.cervezas.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record BeerRequest(
        @NotBlank String name,
        @NotBlank String style,
        String brewery,
        String originCountry,
        @NotNull @PositiveOrZero BigDecimal abv,
        Integer ibu,
        String description
) {
}
