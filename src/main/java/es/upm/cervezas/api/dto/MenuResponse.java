package es.upm.cervezas.api.dto;

import java.util.List;

public record MenuResponse(
        boolean emailVerified,
        boolean profileCompleted,
        boolean canCreateBeer,
        boolean canCreateTasting,
        List<String> availableActions
) {
}
