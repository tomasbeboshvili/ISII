package es.upm.cervezas.service;

import es.upm.cervezas.api.dto.AchievementClaimResponse;
import es.upm.cervezas.api.dto.AchievementRequest;
import es.upm.cervezas.api.dto.AchievementResponse;
import es.upm.cervezas.api.dto.UserAchievementResponse;
import es.upm.cervezas.domain.Achievement;
import es.upm.cervezas.domain.User;
import es.upm.cervezas.domain.UserAchievement;
import es.upm.cervezas.repository.AchievementRepository;
import es.upm.cervezas.repository.UserAchievementRepository;
import es.upm.cervezas.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio que administra la definición de galardones y los logros obtenidos por cada usuario.
 * Permite crear logros, listarlos, reclamarlos manualmente y asignarlos automáticamente cuando
 * se cumplen los hitos configurados (criterios/threshold).
 */
@Service
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final TokenAuthenticationService tokenAuthenticationService;
    private final UserRepository userRepository;
    private final UserAchievementRepository userAchievementRepository;
    private static final Logger log = LoggerFactory.getLogger(AchievementService.class);

    public AchievementService(AchievementRepository achievementRepository,
                              TokenAuthenticationService tokenAuthenticationService,
                              UserRepository userRepository,
                              UserAchievementRepository userAchievementRepository) {
        this.achievementRepository = achievementRepository;
        this.tokenAuthenticationService = tokenAuthenticationService;
        this.userRepository = userRepository;
        this.userAchievementRepository = userAchievementRepository;
    }

    @Transactional
    public AchievementResponse create(AchievementRequest request) {
        Achievement achievement = new Achievement();
        achievement.setName(request.name());
        achievement.setTopic(request.topic());
        achievement.setImageUrl(request.imageUrl());
        achievement.setLevel(request.level());
        achievement.setCriteria(request.criteria());
        achievement.setThreshold(request.threshold());

        achievementRepository.save(achievement);
        log.info("Galardón {} creado con umbral {}", achievement.getName(), achievement.getThreshold());
        return toResponse(achievement);
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> getAll() {
        List<AchievementResponse> responses = achievementRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        log.debug("Se devolvieron {} galardones", responses.size());
        return responses;
    }

    @Transactional
    public AchievementClaimResponse assignToCurrentUser(String token, Long achievementId) {
        User user = tokenAuthenticationService.findUserByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido o caducado"));

        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new EntityNotFoundException("Galardón no encontrado"));

        assignAchievement(user, achievement);
        user.setCurrentAchievementId(achievement.getId());
        user.setBadgeLevel(achievement.getLevel());
        userRepository.save(user);

        log.info("Galardón {} asignado a usuario {}", achievementId, user.getId());
        return new AchievementClaimResponse("Galardón asignado", achievement.getId());
    }

    @Transactional
    public void refreshProgress(User user) {
        achievementRepository.findFirstByThresholdLessThanEqualOrderByThresholdDesc(user.getGamificationPoints())
                .ifPresent(achievement -> {
                    user.setCurrentAchievementId(achievement.getId());
                    user.setBadgeLevel(achievement.getLevel());
                    userRepository.save(user);
                    log.debug("Progreso actualizado: usuario {} ahora tiene galardón {}", user.getId(), achievement.getId());
                    assignAchievement(user, achievement);
                });
    }

    @Transactional
    public void checkMilestone(User user, String criteria, int currentValue) {
        achievementRepository.findFirstByCriteriaAndThresholdLessThanEqualOrderByThresholdDesc(criteria, currentValue)
                .ifPresent(achievement -> assignAchievement(user, achievement));
    }

    @Transactional(readOnly = true)
    public List<UserAchievementResponse> getForCurrentUser(String token) {
        User user = tokenAuthenticationService.findUserByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido o caducado"));
        return userAchievementRepository.findByUserId(user.getId()).stream()
                .map(ua -> new UserAchievementResponse(
                        ua.getAchievement().getId(),
                        ua.getAchievement().getName(),
                        ua.getAchievement().getCriteria(),
                        ua.getAchievement().getThreshold(),
                        ua.getAwardedAt()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Registra que un usuario ha obtenido un galardón determinado, evitando duplicados.
     */
    private void assignAchievement(User user, Achievement achievement) {
        if (userAchievementRepository.existsByUserIdAndAchievementId(user.getId(), achievement.getId())) {
            return;
        }
        UserAchievement userAchievement = new UserAchievement();
        userAchievement.setUser(user);
        userAchievement.setAchievement(achievement);
        userAchievementRepository.save(userAchievement);
        log.info("Usuario {} ganó el galardón {}", user.getId(), achievement.getName());
    }

    private AchievementResponse toResponse(Achievement achievement) {
        return new AchievementResponse(
                achievement.getId(),
                achievement.getName(),
                achievement.getTopic(),
                achievement.getImageUrl(),
                achievement.getLevel(),
                achievement.getCriteria(),
                achievement.getThreshold()
        );
    }
}
