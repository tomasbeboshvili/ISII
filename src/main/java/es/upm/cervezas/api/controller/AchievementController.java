package es.upm.cervezas.api.controller;

import es.upm.cervezas.api.dto.AchievementClaimResponse;
import es.upm.cervezas.api.dto.AchievementRequest;
import es.upm.cervezas.api.dto.AchievementResponse;
import es.upm.cervezas.api.dto.UserAchievementResponse;
import es.upm.cervezas.service.AchievementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API para consultar la biblioteca de galardones, crear nuevos (escenario admin),
 * reclamarlos manualmente y listar los alcanzados por el usuario actual.
 */
@RestController
@RequestMapping("/api/achievements")
public class AchievementController {

    private static final Logger log = LoggerFactory.getLogger(AchievementController.class);

    private final AchievementService achievementService;

    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    @GetMapping
    public List<AchievementResponse> list() {
        log.debug("Listando galardones");
        return achievementService.getAll();
    }

    @GetMapping("/user")
    public List<UserAchievementResponse> userAchievements(@RequestHeader("X-Auth-Token") String token) {
        log.debug("Listando galardones del usuario actual");
        return achievementService.getForCurrentUser(token);
    }

    @PostMapping
    public AchievementResponse create(@Valid @RequestBody AchievementRequest request) {
        log.info("Creación de galardón {}", request.name());
        return achievementService.create(request);
    }

    @PostMapping("/{achievementId}/claim")
    public AchievementClaimResponse claim(@RequestHeader("X-Auth-Token") String token,
                                          @PathVariable Long achievementId) {
        log.info("Asignando galardón {} a usuario actual", achievementId);
        return achievementService.assignToCurrentUser(token, achievementId);
    }
}
